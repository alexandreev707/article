package com.cryptodrop.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "reviews")
data class Review(
    @Id
    val id: String? = null,
    val productId: String,
    val authorId: String,
    val authorName: String,
    val rating: Int, // 1-5
    val comment: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

