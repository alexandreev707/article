package com.cryptodrop.persistence.review

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReviewRepository : JpaRepository<Review, UUID> {
    fun findByProductId(productId: UUID, pageable: Pageable): Page<Review>
    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<Review>
    fun findByProductIdAndAuthorId(productId: UUID, authorId: UUID): List<Review>
}
