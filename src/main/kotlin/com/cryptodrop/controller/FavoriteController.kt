package com.cryptodrop.controller

import com.cryptodrop.security.KeycloakUserService
import com.cryptodrop.service.ProductService
import com.cryptodrop.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favorites")
class FavoriteController(
    private val userService: UserService,
    private val productService: ProductService,
    private val keycloakUserService: KeycloakUserService
) {

    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun toggleFavorite(@PathVariable productId: String): ResponseEntity<Map<String, Any>> {
        val userId = keycloakUserService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val user = userService.toggleFavorite(userId, productId)
        val isFavorite = user.favoriteProductIds.contains(productId)

        return ResponseEntity.ok(mapOf(
            "productId" to productId,
            "isFavorite" to isFavorite,
            "favorites" to user.favoriteProductIds
        ))
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun getFavorites(): ResponseEntity<Map<String, Any>> {
        val userId = keycloakUserService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val user = userService.findById(userId)
        val products = if (user.favoriteProductIds.isNotEmpty()) {
            user.favoriteProductIds.mapNotNull { productId ->
                try {
                    productService.findById(productId)
                } catch (e: Exception) {
                    null
                }
            }.map { productService.toDto(it) }
        } else {
            emptyList()
        }

        return ResponseEntity.ok(mapOf(
            "products" to products,
            "count" to products.size
        ))
    }
}

