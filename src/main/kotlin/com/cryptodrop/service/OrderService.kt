package com.cryptodrop.service

import com.cryptodrop.domain.model.Order
import com.cryptodrop.domain.model.OrderStatus
import com.cryptodrop.domain.repository.OrderRepository
import com.cryptodrop.domain.repository.ProductRepository
import com.cryptodrop.integration.solana.SolanaService
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val solanaService: SolanaService,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    
    fun createOrder(
        productId: Long,
        buyerWallet: String,
        shippingCountry: String?,
        shippingAddress: String?
    ): Order {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        
        val shippingCost = calculateShippingCost(product.shippingProfiles, shippingCountry)
        val totalAmount = product.priceUsd.add(shippingCost)
        
        val order = Order(
            productId = productId,
            buyerWallet = buyerWallet,
            sellerWallet = product.sellerWallet,
            amountUsdc = totalAmount,
            shippingCountry = shippingCountry,
            shippingAddress = shippingAddress,
            shippingCostUsd = shippingCost,
            status = OrderStatus.PENDING_PAYMENT
        )
        
        val savedOrder = orderRepository.save(order)
        
        // Create escrow on Solana
        try {
            val escrowResult = solanaService.createEscrow(
                buyerWallet = buyerWallet,
                sellerWallet = product.sellerWallet,
                amount = totalAmount,
                orderId = savedOrder.id
            )
            
            val updatedOrder = savedOrder.copy(
                solanaEscrow = escrowResult.escrowAddress,
                solanaTxId = escrowResult.txSignature
            )
            val finalOrder = orderRepository.save(updatedOrder)
            
            // Publish event
            kafkaTemplate.send("order-events", """
                {
                    "event": "order.created",
                    "orderId": ${finalOrder.id},
                    "buyerWallet": "${finalOrder.buyerWallet}",
                    "sellerWallet": "${finalOrder.sellerWallet}",
                    "amount": "${finalOrder.amountUsdc}",
                    "escrow": "${finalOrder.solanaEscrow}"
                }
            """.trimIndent())
            
            logger.info { "Order created: ${finalOrder.id} with escrow ${finalOrder.solanaEscrow}" }
            return finalOrder
        } catch (e: Exception) {
            logger.error(e) { "Failed to create escrow for order ${savedOrder.id}" }
            throw RuntimeException("Failed to create escrow", e)
        }
    }
    
    fun confirmPayment(orderId: Long, txSignature: String): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }
        
        if (order.status != OrderStatus.PENDING_PAYMENT) {
            throw IllegalStateException("Order is not in pending payment status")
        }
        
        val updatedOrder = order.copy(
            status = OrderStatus.PAID,
            solanaTxId = txSignature
        )
        
        val savedOrder = orderRepository.save(updatedOrder)
        
        kafkaTemplate.send("order-events", """
            {
                "event": "order.paid",
                "orderId": ${savedOrder.id},
                "txSignature": "$txSignature"
            }
        """.trimIndent())
        
        return savedOrder
    }
    
    fun confirmDelivery(orderId: Long, buyerWallet: String): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }
        
        if (order.buyerWallet != buyerWallet) {
            throw IllegalArgumentException("Buyer wallet mismatch")
        }
        
        if (order.status != OrderStatus.DELIVERED) {
            throw IllegalStateException("Order is not delivered yet")
        }
        
        // Execute payout via Solana
        val payoutResult = solanaService.executePayout(
            escrowAddress = order.solanaEscrow!!,
            sellerWallet = order.sellerWallet,
            amount = order.amountUsdc,
            commissionRate = BigDecimal("0.015")
        )
        
        val updatedOrder = order.copy(
            status = OrderStatus.COMPLETED,
            completedAt = Instant.now()
        )
        
        val savedOrder = orderRepository.save(updatedOrder)
        
        kafkaTemplate.send("order-events", """
            {
                "event": "order.completed",
                "orderId": ${savedOrder.id},
                "payoutTx": "${payoutResult.txSignature}"
            }
        """.trimIndent())
        
        logger.info { "Order completed: ${savedOrder.id}, payout: ${payoutResult.txSignature}" }
        return savedOrder
    }
    
    fun updateTracking(orderId: Long, trackingNumber: String) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }
        
        val updatedOrder = order.copy(
            trackingNumber = trackingNumber,
            status = OrderStatus.SHIPPED,
            shippedAt = Instant.now()
        )
        
        orderRepository.save(updatedOrder)
        
        kafkaTemplate.send("order-events", """
            {
                "event": "order.shipped",
                "orderId": $orderId,
                "trackingNumber": "$trackingNumber"
            }
        """.trimIndent())
    }
    
    fun markDelivered(orderId: Long) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }
        
        val updatedOrder = order.copy(
            status = OrderStatus.DELIVERED,
            deliveredAt = Instant.now()
        )
        
        orderRepository.save(updatedOrder)
        
        kafkaTemplate.send("order-events", """
            {
                "event": "order.delivered",
                "orderId": $orderId
            }
        """.trimIndent())
    }
    
    fun getOrderById(id: Long): Order? {
        return orderRepository.findById(id).orElse(null)
    }
    
    fun getBuyerOrders(buyerWallet: String, page: Int = 0, size: Int = 20): Page<Order> {
        val pageable = PageRequest.of(page, size)
        return orderRepository.findByBuyerWallet(buyerWallet, pageable)
    }
    
    fun getSellerOrders(sellerWallet: String, page: Int = 0, size: Int = 20): Page<Order> {
        val pageable = PageRequest.of(page, size)
        return orderRepository.findBySellerWallet(sellerWallet, pageable)
    }
    
    private fun calculateShippingCost(shippingProfiles: String?, country: String?): BigDecimal {
        // Parse JSON and get shipping cost for country
        // For MVP, simplified logic
        if (country == null || shippingProfiles == null) {
            return BigDecimal("10.00") // Default shipping
        }
        
        // TODO: Parse JSON shipping profiles
        return when (country.uppercase()) {
            "US" -> BigDecimal("8.00")
            "EU" -> BigDecimal("10.00")
            "IN" -> BigDecimal("12.00")
            else -> BigDecimal("15.00")
        }
    }
}
