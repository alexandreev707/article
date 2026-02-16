package com.cryptodrop.persistence.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
interface ProductRepository : JpaRepository<Product, UUID> {
    fun findBySellerId(sellerId: UUID, pageable: Pageable): Page<Product>
    fun findByCategoryId(categoryId: UUID, pageable: Pageable): Page<Product>
    fun findByCategoryName(categoryName: String, pageable: Pageable): Page<Product>
    fun findByActive(active: Boolean, pageable: Pageable): Page<Product>
    fun findByIdIn(productIds: List<UUID>): List<Product>

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.name = :category AND p.price BETWEEN :minPrice AND :maxPrice")
    fun findByCategoryAndPriceBetween(
        @Param("category") category: String,
        @Param("minPrice") minPrice: BigDecimal,
        @Param("maxPrice") maxPrice: BigDecimal,
        pageable: Pageable
    ): Page<Product>

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.name = :category")
    fun findByCategoryNameOnly(@Param("category") category: String, pageable: Pageable): Page<Product>

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.rating >= :minRating")
    fun findByRatingGreaterThanEqual(@Param("minRating") minRating: BigDecimal, pageable: Pageable): Page<Product>

    @Query("SELECT p FROM Product p WHERE p.active = true")
    fun findAllActive(pageable: Pageable): Page<Product>
}
