package com.cryptodrop.dto

import jakarta.validation.constraints.*

data class ReviewCreateDto(
    @field:NotBlank(message = "Product ID is required")
    val productId: String,
    
    @field:NotNull(message = "Rating is required")
    @field:Min(value = 1, message = "Rating must be at least 1")
    @field:Max(value = 5, message = "Rating must be at most 5")
    val rating: Int,
    
    @field:NotBlank(message = "Comment is required")
    @field:Size(max = 1000, message = "Comment must not exceed 1000 characters")
    val comment: String
)

data class ReviewResponseDto(
    val id: String,
    val productId: String,
    val authorId: String,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String,
    val updatedAt: String
)

