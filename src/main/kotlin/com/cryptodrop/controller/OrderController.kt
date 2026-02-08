package com.cryptodrop.controller

import com.cryptodrop.dto.OrderCreateDto
import com.cryptodrop.dto.OrderResponseDto
import com.cryptodrop.dto.OrderStatusUpdateDto
import com.cryptodrop.service.UserService
import com.cryptodrop.service.OrderService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService,
    private val userService: UserService
) {

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun createOrder(@Valid @RequestBody dto: OrderCreateDto): ResponseEntity<OrderResponseDto> {
        val buyerId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        val order = orderService.createOrder(buyerId, dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.toDto(order))
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    fun getMyOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        //val isSeller = userService.hasRole("SELLER") || userService.hasRole("ADMIN")
        val orders = orderService.findByBuyer(userId, PageRequest.of(page, size))

        return ResponseEntity.ok(
            mapOf(
                "orders" to orders.map { orderService.toDto(it) },
                "totalPages" to orders.totalPages,
                "currentPage" to page
            )
        )
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    fun getOrder(@PathVariable id: String): ResponseEntity<OrderResponseDto> {
        val order = orderService.findById(id.toLong())
        val userId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        if (order.buyerId != userId && order.sellerId != userId && !userService.hasRole("ADMIN")) {
            throw IllegalStateException("Access denied")
        }

        return ResponseEntity.ok(orderService.toDto(order))
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    fun updateOrderStatus(
        @PathVariable id: String,
        @Valid @RequestBody dto: OrderStatusUpdateDto
    ): ResponseEntity<OrderResponseDto> {
        val sellerId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        val order = orderService.updateOrderStatus(id.toLong(), sellerId, dto)
        return ResponseEntity.ok(orderService.toDto(order))
    }
}
