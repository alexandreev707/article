package com.cryptodrop.repository

import com.cryptodrop.model.Review
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : MongoRepository<Review, String> {
    fun findByProductId(productId: String, pageable: Pageable): Page<Review>
    fun findByAuthorId(authorId: String, pageable: Pageable): Page<Review>
    fun findByProductIdAndAuthorId(productId: String, authorId: String): List<Review>
}

