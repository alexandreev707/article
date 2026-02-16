package com.cryptodrop.persistence.order

import com.cryptodrop.persistence.product.Product
import com.cryptodrop.persistence.productvariant.ProductVariant
import com.cryptodrop.persistence.user.User
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "order_items")
data class OrderItem(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    val seller: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    val variant: ProductVariant? = null,

    @Column(nullable = false)
    val quantity: Int = 1,

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    val unitPrice: BigDecimal,

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    val totalPrice: BigDecimal,

    @Enumerated(EnumType.STRING)
    val status: OrderItemStatus = OrderItemStatus.PENDING
)

enum class OrderItemStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
}
