package com.cryptodrop.persistence.promotion

import com.cryptodrop.persistence.category.Category
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "promotions")
data class Promotion(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(unique = true)
    val code: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    val discountType: DiscountType,

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 2)
    val discountValue: BigDecimal,

    @Column(name = "min_order_amount", precision = 19, scale = 2)
    val minOrderAmount: BigDecimal? = null,

    @Column(name = "max_discount_amount", precision = 19, scale = 2)
    val maxDiscountAmount: BigDecimal? = null,

    @Column(name = "start_date", nullable = false)
    val startDate: LocalDateTime,

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDateTime,

    @Column(name = "usage_limit")
    val usageLimit: Int? = null,

    @Column(name = "usage_count")
    var usageCount: Int = 0,

    @ManyToMany
    @JoinTable(
        name = "promotion_categories",
        joinColumns = [JoinColumn(name = "promotion_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    val applicableCategories: MutableSet<Category> = mutableSetOf(),

    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT
}
