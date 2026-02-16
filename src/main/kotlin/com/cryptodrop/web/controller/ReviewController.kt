package com.cryptodrop.web.controller

import com.cryptodrop.service.ReviewService
import com.cryptodrop.service.UserService
import com.cryptodrop.service.dto.ReviewCreateDto
import com.cryptodrop.service.dto.ReviewResponseDto
import com.cryptodrop.service.dto.ReviewUpdateDto
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService,
    private val userService: UserService
) {

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun createReview(@Valid @RequestBody dto: ReviewCreateDto): ResponseEntity<ReviewResponseDto> {
        val authorId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val review = reviewService.createReview(authorId, dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.toDto(review))
    }

    @GetMapping("/product/{productId}")
    fun getProductReviews(
        @PathVariable productId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val reviews = reviewService.findByProduct(UUID.fromString(productId), PageRequest.of(page, size))
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
        val reviews = reviewService.findByAuthor(UUID.fromString(userId), PageRequest.of(page, size))
        return ResponseEntity.ok(mapOf(
            "reviews" to reviews.map { reviewService.toDto(it) },
            "totalPages" to reviews.totalPages,
            "currentPage" to page
        ))
    }

    @GetMapping("/{id}")
    fun getReview(@PathVariable id: String): ResponseEntity<ReviewResponseDto> {
        val review = reviewService.findById(UUID.fromString(id))
        return ResponseEntity.ok(reviewService.toDto(review))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun updateReview(
        @PathVariable id: String,
        @Valid @RequestBody dto: ReviewUpdateDto
    ): ResponseEntity<ReviewResponseDto> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val review = reviewService.updateReview(UUID.fromString(id), userId, dto)
        return ResponseEntity.ok(reviewService.toDto(review))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    fun deleteReview(@PathVariable id: String): ResponseEntity<Void> {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        reviewService.deleteReview(UUID.fromString(id), userId)
        return ResponseEntity.noContent().build()
    }
}
