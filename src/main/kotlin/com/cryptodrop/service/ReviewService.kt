package com.cryptodrop.service

import com.cryptodrop.dto.ReviewCreateDto
import com.cryptodrop.dto.ReviewResponseDto
import com.cryptodrop.model.Product
import com.cryptodrop.model.Review
import com.cryptodrop.repository.ProductRepository
import com.cryptodrop.repository.ReviewRepository
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val productRepository: ProductRepository,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun createReview(authorId: Long, dto: ReviewCreateDto): Review {
        val existingReview = reviewRepository.findByProductIdAndAuthorId(dto.productId.toLong(), authorId)
        if (existingReview.isNotEmpty()) {
            throw IllegalStateException("User has already reviewed this product")
        }

        val product = productRepository.findById(dto.productId.toLong())
            .orElseThrow { IllegalArgumentException("Product not found: ${dto.productId}") }

        val author = userService.findById(authorId)
        val review = Review(
            productId = product.id!!,
            authorId = authorId,
            authorName = author.username,
            rating = dto.rating,
            comment = dto.comment
        )

        val savedReview = reviewRepository.save(review)
        
        // Update product rating
        updateProductRating(product)
        
        logger.info("Review created for product: ${dto.productId} by user: $authorId")
        return savedReview
    }

    @Transactional
    private fun updateProductRating(product: Product) {
        val reviews = reviewRepository.findByProductId(product.id!!, org.springframework.data.domain.PageRequest.of(0, 1000))
        val averageRating = reviews.content.map { it.rating }.average()
        val reviewCount = reviews.totalElements.toInt()
        
        val updatedProduct = product.copy(
            rating = averageRating,
            reviewCount = reviewCount,
            updatedAt = LocalDateTime.now()
        )
        productRepository.save(updatedProduct)
    }

    fun findByProduct(productId: Long, pageable: Pageable): Page<Review> {
        return reviewRepository.findByProductId(productId, pageable)
    }

    fun findByAuthor(authorId: Long, pageable: Pageable): Page<Review> {
        return reviewRepository.findByAuthorId(authorId, pageable)
    }

    fun toDto(review: Review): ReviewResponseDto {
        return ReviewResponseDto(
            id = review.id.toString(),
            productId = review.productId.toString(),
            authorId = review.authorId.toString(),
            authorName = review.authorName,
            rating = review.rating,
            comment = review.comment,
            createdAt = review.createdAt.format(dateFormatter),
            updatedAt = review.updatedAt.format(dateFormatter)
        )
    }
}
