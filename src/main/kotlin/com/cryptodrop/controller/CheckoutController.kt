package com.cryptodrop.controller

import com.cryptodrop.dto.CheckoutDto
import com.cryptodrop.service.OrderService
import com.cryptodrop.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/checkout")
class CheckoutController(
    private val orderService: OrderService,
    private val userService: UserService
) {

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun checkout(@Valid @RequestBody dto: CheckoutDto): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        val orders = orderService.createOrdersFromCart(userId, dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "orders" to orders.map { orderService.toDto(it) },
                "message" to "Order(s) created successfully"
            )
        )
    }
}
