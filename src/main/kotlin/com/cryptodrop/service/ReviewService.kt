package com.cryptodrop.service

import com.cryptodrop.persistence.product.Product
import com.cryptodrop.persistence.product.ProductRepository
import com.cryptodrop.persistence.review.Review
import com.cryptodrop.persistence.review.ReviewRepository
import com.cryptodrop.service.dto.ReviewCreateDto
import com.cryptodrop.service.dto.ReviewResponseDto
import com.cryptodrop.service.dto.ReviewUpdateDto
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

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
    fun createReview(authorId: UUID, dto: ReviewCreateDto): Review {
        val productId = UUID.fromString(dto.productId)
        val existingReview = reviewRepository.findByProductIdAndAuthorId(productId, authorId)
        if (existingReview.isNotEmpty()) {
            throw IllegalStateException("User has already reviewed this product")
        }

        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: ${dto.productId}") }
        val author = userService.findById(authorId)

        val review = Review(
            product = product,
            author = author,
            rating = dto.rating,
            comment = dto.comment
        )

        val savedReview = reviewRepository.save(review)
        updateProductRating(product)
        logger.info("Review created for product: ${dto.productId} by user: $authorId")
        return savedReview
    }

    @Transactional
    private fun updateProductRating(product: Product) {
        val reviews = reviewRepository.findByProductId(product.id!!, org.springframework.data.domain.PageRequest.of(0, 1000))
        val averageRating = reviews.content.map { it.rating }.average().toBigDecimal()
        val reviewCount = reviews.totalElements.toInt()

        productRepository.save(product.copy(
            rating = averageRating,
            reviewCount = reviewCount,
            updatedAt = LocalDateTime.now()
        ))
    }

    fun findByProduct(productId: UUID, pageable: Pageable): Page<Review> {
        return reviewRepository.findByProductId(productId, pageable)
    }

    fun findByAuthor(authorId: UUID, pageable: Pageable): Page<Review> {
        return reviewRepository.findByAuthorId(authorId, pageable)
    }

    fun findById(reviewId: UUID): Review {
        return reviewRepository.findById(reviewId)
            .orElseThrow { IllegalArgumentException("Review not found: $reviewId") }
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun updateReview(reviewId: UUID, userId: UUID, dto: ReviewUpdateDto): Review {
        val review = findById(reviewId)
        if (review.author.id != userId && !userService.isAdmin(userId)) {
            throw IllegalStateException("Only review author or admin can update it")
        }
        val updated = review.copy(
            rating = dto.rating ?: review.rating,
            comment = dto.comment ?: review.comment,
            updatedAt = LocalDateTime.now()
        )
        val saved = reviewRepository.save(updated)
        updateProductRating(review.product)
        return saved
    }

    @CacheEvict(value = ["products"], allEntries = true)
    @Transactional
    fun deleteReview(reviewId: UUID, userId: UUID) {
        val review = findById(reviewId)
        if (review.author.id != userId && !userService.isAdmin(userId)) {
            throw IllegalStateException("Only review author or admin can delete it")
        }
        reviewRepository.deleteById(reviewId)
        updateProductRating(review.product)
    }

    fun toDto(review: Review): ReviewResponseDto {
        return ReviewResponseDto(
            id = review.id.toString(),
            productId = review.product.id!!.toString(),
            authorId = review.author.id!!.toString(),
            authorName = review.author.username,
            rating = review.rating,
            comment = review.comment,
            createdAt = review.createdAt.format(dateFormatter),
            updatedAt = review.updatedAt.format(dateFormatter)
        )
    }
}
