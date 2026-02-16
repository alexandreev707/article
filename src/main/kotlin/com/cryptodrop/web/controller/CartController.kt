package com.cryptodrop.web.controller

import com.cryptodrop.service.CartService
import com.cryptodrop.service.UserService
import com.cryptodrop.service.dto.CartAddDto
import com.cryptodrop.service.dto.CartResponseDto
import com.cryptodrop.service.dto.CartUpdateQuantityDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartService: CartService,
    private val userService: UserService
) {

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun getCart(): ResponseEntity<CartResponseDto> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        return ResponseEntity.ok(cartService.getCart(userId))
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun getCartCount(): ResponseEntity<Map<String, Int>> {
        val userId = userService.getCurrentUserId() ?: return ResponseEntity.ok(mapOf("count" to 0))
        return ResponseEntity.ok(mapOf("count" to cartService.getCartCount(userId)))
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun addItem(@RequestBody dto: CartAddDto): ResponseEntity<CartResponseDto> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        cartService.addItem(userId, UUID.fromString(dto.productId), dto.quantity.coerceAtLeast(1))
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.getCart(userId))
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun updateQuantity(@PathVariable productId: String, @RequestBody dto: CartUpdateQuantityDto): ResponseEntity<CartResponseDto> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        cartService.updateQuantity(userId, UUID.fromString(productId), dto.quantity.coerceAtLeast(1))
        return ResponseEntity.ok(cartService.getCart(userId))
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun removeItem(@PathVariable productId: String): ResponseEntity<CartResponseDto> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        cartService.removeItem(userId, UUID.fromString(productId))
        return ResponseEntity.ok(cartService.getCart(userId))
    }
}
