package com.cryptodrop.web.controller

import com.cryptodrop.persistence.order.OrderStatus
import com.cryptodrop.service.OrderService
import com.cryptodrop.service.UserService
import com.cryptodrop.service.dto.OrderCreateDto
import com.cryptodrop.service.dto.OrderResponseDto
import com.cryptodrop.service.dto.OrderStatusUpdateDto
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val userService: UserService
) {

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun createOrder(@Valid @RequestBody dto: OrderCreateDto): ResponseEntity<OrderResponseDto> {
        val buyerId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val order = orderService.createOrder(buyerId, dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.toDto(order))
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    fun getMyOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val isSeller = userService.hasRole("SELLER") || userService.hasRole("ADMIN")
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        return if (isSeller) {
            val ordersPage = orderService.findBySeller(userId, pageable)
            ResponseEntity.ok(
                mapOf(
                    "orders" to ordersPage.map { orderService.toDto(it) },
                    "totalPages" to ordersPage.totalPages,
                    "currentPage" to page
                )
            )
        } else {
            val pageResult = orderService.findByBuyer(userId, pageable)
            val statusFilter = status?.uppercase()
            val activeStatuses = setOf(
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                OrderStatus.PROCESSING,
                OrderStatus.SHIPPED
            )
            val filteredContent = when {
                statusFilter == null || statusFilter == "ACTIVE" -> pageResult.content.filter { it.status in activeStatuses }
                statusFilter == "ALL" -> pageResult.content
                else -> {
                    val parsed = runCatching { OrderStatus.valueOf(statusFilter) }.getOrNull()
                    if (parsed != null) pageResult.content.filter { it.status == parsed } else pageResult.content
                }
            }
            ResponseEntity.ok(
                mapOf(
                    "orders" to filteredContent.map { orderService.toDto(it) },
                    "totalPages" to pageResult.totalPages,
                    "currentPage" to page
                )
            )
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    fun getOrder(@PathVariable id: String): ResponseEntity<OrderResponseDto> {
        val order = orderService.findById(UUID.fromString(id))
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val hasSellerItem = order.items.any { it.seller.id == userId }
        if (order.buyer.id != userId && !hasSellerItem && !userService.hasRole("ADMIN")) {
            throw IllegalStateException("Access denied")
        }
        return ResponseEntity.ok(orderService.toDto(order))
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun updateOrderStatus(@PathVariable id: String, @Valid @RequestBody dto: OrderStatusUpdateDto): ResponseEntity<OrderResponseDto> {
        val sellerId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val order = orderService.updateOrderStatus(UUID.fromString(id), sellerId, dto)
        return ResponseEntity.ok(orderService.toDto(order))
    }
}
