package com.cryptodrop.persistence.review

import com.cryptodrop.persistence.order.Order
import com.cryptodrop.persistence.product.Product
import com.cryptodrop.persistence.user.User
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "reviews", uniqueConstraints = [
    UniqueConstraint(columnNames = ["product_id", "author_id"])
], indexes = [
    Index(name = "idx_product_id", columnList = "product_id")
])
data class Review(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    val order: Order? = null,

    @Column(nullable = false)
    val rating: Int,

    @Column(columnDefinition = "TEXT")
    val comment: String = "",

    @ElementCollection
    @CollectionTable(name = "review_images", joinColumns = [JoinColumn(name = "review_id")])
    @OrderColumn(name = "image_order")
    @Column(name = "image_url", length = 500)
    val images: MutableList<String> = mutableListOf(),

    @Column(name = "likes_count")
    val likesCount: Int = 0,

    @Column(name = "dislikes_count")
    val dislikesCount: Int = 0,

    @Column(name = "verified_purchase")
    val verifiedPurchase: Boolean = false,

    @Column(name = "seller_response", columnDefinition = "TEXT")
    val sellerResponse: String? = null,

    @Column(name = "seller_response_at")
    val sellerResponseAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
