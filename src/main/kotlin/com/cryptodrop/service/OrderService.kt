package com.cryptodrop.service

import com.cryptodrop.dto.AddressDto
import com.cryptodrop.dto.CheckoutDto
import com.cryptodrop.dto.OrderCreateDto
import com.cryptodrop.dto.OrderResponseDto
import com.cryptodrop.dto.OrderStatusUpdateDto
import com.cryptodrop.model.Address
import com.cryptodrop.model.Order
import com.cryptodrop.model.OrderStatus
import com.cryptodrop.repository.OrderRepository
import com.cryptodrop.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val cartService: CartService,
    private val deliveryOptionService: com.cryptodrop.service.DeliveryOptionService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Transactional
    fun createOrder(buyerId: Long, dto: OrderCreateDto): Order {
        val product = productRepository.findById(dto.productId.toLong())
            .orElseThrow { IllegalArgumentException("Product not found: ${dto.productId}") }

        if (!product.active) {
            throw IllegalStateException("Product is not available")
        }

        if (product.stock < dto.quantity) {
            throw IllegalStateException("Insufficient stock")
        }

        val totalPrice = product.price.multiply(BigDecimal.valueOf(dto.quantity.toLong()))
        val address = Address(
            street = dto.shippingAddress.street,
            city = dto.shippingAddress.city,
            state = dto.shippingAddress.state,
            zipCode = dto.shippingAddress.zipCode,
            country = dto.shippingAddress.country
        )

        val order = Order(
            buyerId = buyerId,
            sellerId = product.sellerId,
            productId = product.id!!,
            quantity = dto.quantity,
            totalPrice = totalPrice,
            shippingAddress = address
        )

        // Update product stock
        val updatedProduct = product.copy(
            stock = product.stock - dto.quantity,
            updatedAt = LocalDateTime.now()
        )
        productRepository.save(updatedProduct)

        logger.info("Order created: ${order.id} by buyer: $buyerId")
        return orderRepository.save(order)
    }

    @Transactional
    fun updateOrderStatus(orderId: Long, sellerId: Long, dto: OrderStatusUpdateDto): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }

        if (order.sellerId != sellerId) {
            throw IllegalStateException("Only order seller can update status")
        }

        val updatedOrder = order.copy(
            status = dto.status,
            updatedAt = LocalDateTime.now()
        )

        logger.info("Order status updated: $orderId to ${dto.status}")
        return orderRepository.save(updatedOrder)
    }

    @Transactional
    fun createOrdersFromCart(buyerId: Long, dto: CheckoutDto): List<Order> {
        val cart = cartService.getCart(buyerId)
        if (cart.items.isEmpty()) throw IllegalStateException("Cart is empty")

        val delivery = deliveryOptionService.findById(dto.deliveryOptionId)
        val address = if (dto.shippingAddress != null) {
            Address(
                street = dto.shippingAddress.street,
                city = dto.shippingAddress.city,
                state = dto.shippingAddress.state,
                zipCode = dto.shippingAddress.zipCode,
                country = dto.shippingAddress.country
            )
        } else {
            Address(
                street = delivery.addressLine ?: "",
                city = delivery.city ?: "",
                state = delivery.region ?: "",
                zipCode = delivery.zipCode ?: "",
                country = delivery.country ?: ""
            )
        }

        val totalDiscount = dto.discountAmount ?: BigDecimal.ZERO
        val subtotal = cart.subtotal
        val orders = mutableListOf<Order>()

        for (item in cart.items) {
            val product = productRepository.findById(item.productId)
                .orElseThrow { IllegalArgumentException("Product not found: ${item.productId}") }
            if (!product.active) throw IllegalStateException("Product ${product.title} is not available")
            if (product.stock < item.quantity) throw IllegalStateException("Insufficient stock for ${product.title}")

            val itemSubtotal = product.price.multiply(BigDecimal(item.quantity))
            val itemDiscount = if (subtotal > BigDecimal.ZERO && totalDiscount > BigDecimal.ZERO) {
                itemSubtotal.multiply(totalDiscount).divide(subtotal, 2, java.math.RoundingMode.HALF_UP)
            } else BigDecimal.ZERO
            val totalPrice = itemSubtotal.subtract(itemDiscount)

            val order = Order(
                buyerId = buyerId,
                sellerId = product.sellerId,
                productId = product.id!!,
                quantity = item.quantity,
                totalPrice = totalPrice,
                discountAmount = if (itemDiscount > BigDecimal.ZERO) itemDiscount else null,
                shippingAddress = address
            )
            orders.add(orderRepository.save(order))

            val updatedProduct = product.copy(
                stock = product.stock - item.quantity,
                updatedAt = LocalDateTime.now()
            )
            productRepository.save(updatedProduct)
        }

        cart.items.forEach { cartService.removeItem(buyerId, it.productId) }
        logger.info("Created ${orders.size} orders from cart for buyer: $buyerId")
        return orders
    }

    fun findByBuyer(buyerId: Long, pageable: Pageable): Page<Order> {
        return orderRepository.findByBuyerId(buyerId, pageable)
    }

    fun findBySeller(sellerId: Long, pageable: Pageable): Page<Order> {
        return orderRepository.findBySellerId(sellerId, pageable)
    }

    fun findById(orderId: Long): Order {
        return orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found: $orderId") }
    }

    fun toDto(order: Order, productTitle: String? = null): OrderResponseDto {
        val product = productTitle ?: productService.findById(order.productId).title
        return OrderResponseDto(
            id = order.id.toString(),
            buyerId = order.buyerId.toString(),
            sellerId = order.sellerId.toString(),
            productId = order.productId.toString(),
            productTitle = product,
            quantity = order.quantity,
            totalPrice = order.totalPrice,
            discountAmount = order.discountAmount,
            status = order.status,
            shippingAddress = AddressDto(
                street = order.shippingAddress.street,
                city = order.shippingAddress.city,
                state = order.shippingAddress.state,
                zipCode = order.shippingAddress.zipCode,
                country = order.shippingAddress.country
            ),
            createdAt = order.createdAt.format(dateFormatter),
            updatedAt = order.updatedAt.format(dateFormatter)
        )
    }
}
