package com.cryptodrop.persistence.product

import com.cryptodrop.persistence.category.Category
import com.cryptodrop.persistence.user.User
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "products", indexes = [
    Index(name = "idx_seller_id", columnList = "seller_id"),
    Index(name = "idx_category_id", columnList = "category_id"),
    Index(name = "idx_active_created", columnList = "active,created_at")
])
data class Product(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    val seller: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val slug: String,

    @Column(columnDefinition = "TEXT")
    val description: String = "",

    @Column(nullable = false, precision = 19, scale = 2)
    val price: BigDecimal,

    @Column(name = "discount_price", precision = 19, scale = 2)
    val discountPrice: BigDecimal? = null,

    @Column(name = "discount_percentage")
    val discountPercentage: Int? = null,

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = [JoinColumn(name = "product_id")])
    @OrderColumn(name = "image_order")
    @Column(name = "image_url", length = 500)
    val images: MutableList<String> = mutableListOf(),

    @Column(precision = 3, scale = 2)
    val rating: BigDecimal = BigDecimal.ZERO,

    @Column(name = "review_count")
    val reviewCount: Int = 0,

    @Column(name = "view_count")
    val viewCount: Int = 0,

    @ElementCollection
    @CollectionTable(name = "product_attributes", joinColumns = [JoinColumn(name = "product_id")])
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value", length = 1000)
    val attributes: MutableMap<String, String> = mutableMapOf(),

    @Column(nullable = false)
    val stock: Int = 0,

    @Column(unique = true)
    val sku: String? = null,

    val brand: String? = null,

    @Column(name = "weight_grams")
    val weightGrams: Int? = null,

    val active: Boolean = true,

    @Column(name = "is_new")
    val isNew: Boolean = false,

    @Column(name = "is_featured")
    val isFeatured: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
