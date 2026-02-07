package com.cryptodrop.controller

import com.cryptodrop.dto.ReviewCreateDto
import com.cryptodrop.dto.ReviewResponseDto
import com.cryptodrop.security.KeycloakUserService
import com.cryptodrop.service.ReviewService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService,
    private val keycloakUserService: KeycloakUserService
) {

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun createReview(@Valid @RequestBody dto: ReviewCreateDto): ResponseEntity<ReviewResponseDto> {
        val authorId = keycloakUserService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        val review = reviewService.createReview(authorId, dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.toDto(review))
    }

    @GetMapping("/product/{productId}")
    fun getProductReviews(
        @PathVariable productId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val reviews = reviewService.findByProduct(productId, PageRequest.of(page, size))
        return ResponseEntity.ok(mapOf(
            "reviews" to reviews.map { reviewService.toDto(it) },
            "totalPages" to reviews.totalPages,
            "currentPage" to page
        ))
    }

    @GetMapping("/user/{userId}")
    fun getUserReviews(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val reviews = reviewService.findByAuthor(userId, PageRequest.of(page, size))
        return ResponseEntity.ok(mapOf(
            "reviews" to reviews.map { reviewService.toDto(it) },
            "totalPages" to reviews.totalPages,
            "currentPage" to page
        ))
    }
}

