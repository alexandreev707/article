package com.cryptodrop.repository

import com.cryptodrop.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ProductRepository : MongoRepository<Product, String> {
    fun findBySellerId(sellerId: String, pageable: Pageable): Page<Product>
    fun findByCategory(category: String, pageable: Pageable): Page<Product>
    fun findByActive(active: Boolean, pageable: Pageable): Page<Product>
    fun findByIdIn(productIds: List<String>): List<Product>
    
    @Query("{'active': true, 'category': ?0, 'price': {\$gte: ?1, \$lte: ?2}}")
    fun findByCategoryAndPriceBetween(
        category: String,
        minPrice: BigDecimal,
        maxPrice: BigDecimal,
        pageable: Pageable
    ): Page<Product>
    
    @Query("{'active': true, 'rating': {\$gte: ?0}}")
    fun findByRatingGreaterThanEqual(minRating: Double, pageable: Pageable): Page<Product>
    
    @Query("{'active': true}")
    fun findAllActive(pageable: Pageable): Page<Product>
}

