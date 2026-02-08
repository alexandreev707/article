package com.cryptodrop.controller

import com.cryptodrop.service.ProductService
import com.cryptodrop.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/favorites")
class FavoriteController(
    private val userService: UserService,
    private val productService: ProductService
) {

    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun addFavorite(
        @PathVariable userId: Long,
        @PathVariable productId: Long
    ): ResponseEntity<Map<String, Any>> {
        val currentUserId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        if (currentUserId != userId) {
            throw IllegalStateException("Access denied")
        }

        val user = userService.toggleFavorite(userId, productId)
        val isFavorite = user.favoriteProductIds.contains(productId)

        return ResponseEntity.ok(mapOf(
            "productId" to productId,
            "isFavorite" to true,
            "favorites" to user.favoriteProductIds.map { it.toString() }
        ))
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun removeFavorite(
        @PathVariable userId: Long,
        @PathVariable productId: Long
    ): ResponseEntity<Map<String, Any>> {
        val currentUserId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        if (currentUserId != userId) {
            throw IllegalStateException("Access denied")
        }

        val user = userService.removeFavorite(userId, productId)
        val isFavorite = user.favoriteProductIds.contains(productId)

        return ResponseEntity.ok(mapOf(
            "productId" to productId,
            "isFavorite" to false,
            "favorites" to user.favoriteProductIds.map { it.toString() }
        ))
    }

    @PostMapping("/toggle/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun toggleFavorite(@PathVariable productId: Long): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val user = userService.toggleFavorite(userId, productId)
        val isFavorite = user.favoriteProductIds.contains(productId)

        return ResponseEntity.ok(mapOf(
            "productId" to productId,
            "isFavorite" to isFavorite,
            "favorites" to user.favoriteProductIds.map { it.toString() }
        ))
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun getFavorites(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "12") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val userId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")

        val user = userService.findById(userId)
        val favoriteIds = user.favoriteProductIds.toList()

        val favoriteProducts = productService.findByIds(favoriteIds, PageRequest.of(page, size))

        return ResponseEntity.ok(mapOf(
            "products" to favoriteProducts,
            "totalPages" to 1,
            "currentPage" to page,
            "count" to favoriteProducts.size
        ))
    }

}
