package com.cryptodrop.web.controller

import com.cryptodrop.service.ProductService
import com.cryptodrop.service.UserService
import com.cryptodrop.service.WishlistService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/favorites")
class FavoriteController(
    private val userService: UserService,
    private val productService: ProductService,
    private val wishlistService: WishlistService
) {

    @PostMapping("/toggle/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun toggleFavorite(@PathVariable productId: String): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val user = userService.findById(userId)
        val product = productService.findById(UUID.fromString(productId))
        val isFavorite = wishlistService.toggleFavorite(user, product)
        val favoriteIds = wishlistService.getFavoriteProductIds(userId)
        return ResponseEntity.ok(mapOf(
            "productId" to productId,
            "isFavorite" to isFavorite,
            "favorites" to favoriteIds.map { it.toString() }
        ))
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun removeFavorite(@PathVariable productId: String): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        wishlistService.removeFromWishlist(userId, UUID.fromString(productId))
        val favoriteIds = wishlistService.getFavoriteProductIds(userId)
        return ResponseEntity.ok(mapOf(
            "productId" to productId,
            "isFavorite" to false,
            "favorites" to favoriteIds.map { it.toString() }
        ))
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun getFavorites(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "12") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val favoriteIds = wishlistService.getFavoriteProductIds(userId)
        val favoriteProducts = productService.findByIds(favoriteIds, PageRequest.of(page, size))
        return ResponseEntity.ok(mapOf(
            "products" to favoriteProducts,
            "totalPages" to 1,
            "currentPage" to page,
            "count" to favoriteProducts.size
        ))
    }
}
