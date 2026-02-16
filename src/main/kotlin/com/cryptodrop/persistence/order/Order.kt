package com.cryptodrop.persistence.order

import com.cryptodrop.persistence.deliveryoption.DeliveryOption
import com.cryptodrop.persistence.user.User
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "orders", indexes = [
    Index(name = "idx_buyer_id", columnList = "buyer_id"),
    Index(name = "idx_status", columnList = "status"),
    Index(name = "idx_created_at", columnList = "created_at")
])
data class Order(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "order_number", unique = true, nullable = false)
    val orderNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    val buyer: User,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf(),

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 2)
    val subtotal: BigDecimal,

    @Column(name = "shipping_cost", precision = 19, scale = 2)
    val shippingCost: BigDecimal = BigDecimal.ZERO,

    @Column(name = "discount_amount", precision = 19, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_amount", precision = 19, scale = 2)
    val taxAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    val totalPrice: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    val paymentMethod: PaymentMethod? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @Embedded
    val shippingAddress: Address,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_option_id")
    val deliveryOption: DeliveryOption? = null,

    @Column(name = "tracking_number")
    val trackingNumber: String? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "delivered_at")
    val deliveredAt: LocalDateTime? = null
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}

enum class PaymentMethod {
    CARD,
    CASH_ON_DELIVERY,
    BANK_TRANSFER,
    ELECTRONIC_WALLET
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
