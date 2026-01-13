package com.cryptodrop.domain.repository

import com.cryptodrop.domain.model.Product
import com.cryptodrop.domain.model.ProductStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    
    fun findByStatus(status: ProductStatus, pageable: Pageable): Page<Product>
    
    fun findByCategoryIdAndStatus(categoryId: Long, status: ProductStatus, pageable: Pageable): Page<Product>
    
    fun findBySellerWallet(sellerWallet: String, pageable: Pageable): Page<Product>
    
    @Query("""
        SELECT p FROM Product p 
        WHERE p.status = :status 
        AND p.priceUsd <= :maxPrice
        AND (:categoryId IS NULL OR p.categoryId = :categoryId)
        ORDER BY p.createdAt DESC
    """)
    fun findActiveProducts(
        @Param("status") status: ProductStatus,
        @Param("maxPrice") maxPrice: BigDecimal?,
        @Param("categoryId") categoryId: Long?,
        pageable: Pageable
    ): Page<Product>
    
    @Query("""
        SELECT p FROM Product p 
        WHERE p.status = :status 
        AND LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY p.createdAt DESC
    """)
    fun searchProducts(
        @Param("status") status: ProductStatus,
        @Param("query") query: String,
        pageable: Pageable
    ): Page<Product>
}
