package com.cryptodrop.web.controller

import com.cryptodrop.service.OrderService
import com.cryptodrop.service.OxapayService
import com.cryptodrop.service.UserService
import com.cryptodrop.service.dto.CheckoutDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/checkout")
class CheckoutController(
    private val orderService: OrderService,
    private val userService: UserService,
    private val oxapayService: OxapayService
) {

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun checkout(@Valid @RequestBody dto: CheckoutDto): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val orders = orderService.createOrdersFromCart(userId, dto)
        val paymentUrl = orders.firstOrNull()?.oxapayPaymentUrl
            ?: throw IllegalStateException("OxaPay payment url was not generated")
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "orders" to orders.map { orderService.toDto(it) },
                "orderIds" to orders.map { it.id!!.toString() },
                "paymentUrl" to paymentUrl,
                "message" to "Order(s) created. Proceed to payment."
            )
        )
    }

    @PostMapping("/oxapay/callback")
    fun oxapayCallback(@RequestBody payload: Map<String, Any?>): ResponseEntity<Map<String, String>> {
        val trackId = oxapayService.extractTrackId(payload)
            ?: return ResponseEntity.badRequest().body(mapOf("message" to "trackId missing"))
        if (!oxapayService.isPaidCallback(payload)) {
            return ResponseEntity.ok(mapOf("message" to "ignored"))
        }
        orderService.confirmPaymentByTrackId(trackId)
        return ResponseEntity.ok(mapOf("message" to "ok"))
    }

    @PostMapping("/confirm-payment")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun confirmPayment(@RequestBody body: Map<String, List<String>>): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val orderIds = body["orderIds"] ?: throw IllegalArgumentException("orderIds required")
        val uuids = orderIds.map { java.util.UUID.fromString(it) }
        val orders = orderService.confirmPayment(uuids, userId)
        return ResponseEntity.ok(
            mapOf(
                "orders" to orders.map { orderService.toDto(it) },
                "message" to "Payment confirmed successfully"
            )
        )
    }
}
