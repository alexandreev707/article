package com.cryptodrop.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "products", indexes = [
    Index(name = "idx_seller_wallet", columnList = "seller_wallet"),
    Index(name = "idx_category_id", columnList = "category_id"),
    Index(name = "idx_status", columnList = "status")
])
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "seller_wallet", nullable = false, length = 66)
    val sellerWallet: String,
    
    @Column(name = "category_id", nullable = false)
    val categoryId: Long,
    
    @Column(name = "title", nullable = false, length = 255)
    val title: String,
    
    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(name = "price_usd", nullable = false, precision = 10, scale = 2)
    val priceUsd: BigDecimal,
    
    @Column(name = "images", columnDefinition = "JSONB")
    val images: String? = null, // JSON array
    
    @Column(name = "specs", columnDefinition = "JSONB")
    val specs: String? = null, // EAV characteristics
    
    @Column(name = "shipping_profiles", columnDefinition = "JSONB")
    val shippingProfiles: String? = null, // {US:8, EU:10, IN:12}
    
    @Column(name = "solana_product_id", length = 44)
    val solanaProductId: String? = null,
    
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    val status: ProductStatus = ProductStatus.ACTIVE,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
)

enum class ProductStatus {
    ACTIVE,
    INACTIVE,
    BANNED
}
