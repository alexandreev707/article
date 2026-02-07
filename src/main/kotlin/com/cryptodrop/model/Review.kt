package com.cryptodrop.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reviews")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val productId: Long,
    
    @Column(nullable = false)
    val authorId: Long,
    
    @Column(nullable = false)
    val authorName: String,
    
    @Column(nullable = false)
    val rating: Int, // 1-5
    
    @Column(columnDefinition = "TEXT")
    val comment: String = "",
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
