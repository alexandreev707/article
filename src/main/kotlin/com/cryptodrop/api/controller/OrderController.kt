package com.cryptodrop.api.controller

import com.cryptodrop.api.dto.OrderCreateRequest
import com.cryptodrop.api.dto.OrderResponse
import com.cryptodrop.api.dto.OrdersPageResponse
import com.cryptodrop.domain.model.Order
import com.cryptodrop.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/orders")
class OrderController(
    private val orderService: OrderService
) {
    
    @PostMapping
    fun createOrder(
        @Valid @RequestBody request: OrderCreateRequest,
        @RequestHeader("X-Wallet-Address") buyerWallet: String
    ): ResponseEntity<OrderResponse> {
        val order = orderService.createOrder(
            productId = request.productId,
            buyerWallet = buyerWallet,
            shippingCountry = request.shippingCountry,
            shippingAddress = request.shippingAddress
        )
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(order.toResponse())
    }
    
    @GetMapping("/{id}")
    fun getOrder(
        @PathVariable id: Long,
        @RequestHeader("X-Wallet-Address") wallet: String
    ): ResponseEntity<OrderResponse> {
        val order = orderService.getOrderById(id)
            ?: return ResponseEntity.notFound().build()
        
        // Verify wallet has access
        if (order.buyerWallet != wallet && order.sellerWallet != wallet) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        
        return ResponseEntity.ok(order.toResponse())
    }
    
    @PostMapping("/{id}/confirm-payment")
    fun confirmPayment(
        @PathVariable id: Long,
        @RequestParam txSignature: String,
        @RequestHeader("X-Wallet-Address") buyerWallet: String
    ): ResponseEntity<OrderResponse> {
        val order = orderService.confirmPayment(id, txSignature)
        
        if (order.buyerWallet != buyerWallet) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        
        return ResponseEntity.ok(order.toResponse())
    }
    
    @PostMapping("/{id}/confirm-delivery")
    fun confirmDelivery(
        @PathVariable id: Long,
        @RequestHeader("X-Wallet-Address") buyerWallet: String
    ): ResponseEntity<OrderResponse> {
        val order = orderService.confirmDelivery(id, buyerWallet)
        return ResponseEntity.ok(order.toResponse())
    }
    
    @GetMapping("/buyer/my-orders")
    fun getMyOrders(
        @RequestHeader("X-Wallet-Address") buyerWallet: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<OrdersPageResponse> {
        val ordersPage = orderService.getBuyerOrders(buyerWallet, page, limit)
        
        val response = OrdersPageResponse(
            orders = ordersPage.content.map { it.toResponse() },
            total = ordersPage.totalElements,
            page = ordersPage.number,
            limit = ordersPage.size
        )
        
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/seller/my-orders")
    fun getSellerOrders(
        @RequestHeader("X-Wallet-Address") sellerWallet: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<OrdersPageResponse> {
        val ordersPage = orderService.getSellerOrders(sellerWallet, page, limit)
        
        val response = OrdersPageResponse(
            orders = ordersPage.content.map { it.toResponse() },
            total = ordersPage.totalElements,
            page = ordersPage.number,
            limit = ordersPage.size
        )
        
        return ResponseEntity.ok(response)
    }
    
    private fun Order.toResponse() = OrderResponse(
        id = id,
        productId = productId,
        buyerWallet = buyerWallet,
        sellerWallet = sellerWallet,
        amountUsdc = amountUsdc.toString(),
        shippingCountry = shippingCountry,
        shippingAddress = shippingAddress,
        shippingCostUsd = shippingCostUsd?.toString(),
        solanaEscrow = solanaEscrow,
        solanaTxId = solanaTxId,
        status = status.name.lowercase(),
        trackingNumber = trackingNumber,
        createdAt = createdAt.toString(),
        shippedAt = shippedAt?.toString(),
        deliveredAt = deliveredAt?.toString(),
        completedAt = completedAt?.toString()
    )
}
