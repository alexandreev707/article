package com.cryptodrop.persistence.deliveryoption

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "delivery_options")
data class DeliveryOption(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: DeliveryType,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false, precision = 19, scale = 2)
    val price: BigDecimal = BigDecimal.ZERO,

    @Column(name = "free_shipping_threshold", precision = 19, scale = 2)
    val freeShippingThreshold: BigDecimal? = null,

    @Column(name = "estimated_days_min")
    val estimatedDaysMin: Int? = null,

    @Column(name = "estimated_days_max")
    val estimatedDaysMax: Int? = null,

    @Column(name = "pickup_address", columnDefinition = "TEXT")
    val pickupAddress: String? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,

    val active: Boolean = true,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class DeliveryType {
    COURIER,
    PICKUP,
    POST
}
