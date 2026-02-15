package com.cryptodrop.model

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "delivery_options")
data class DeliveryOption(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: DeliveryType,

    @Column(nullable = false, precision = 19, scale = 2)
    val price: BigDecimal = BigDecimal.ZERO,

    @Column(name = "estimated_days")
    val estimatedDays: Int? = null,

    @Column(columnDefinition = "TEXT")
    val addressLine: String? = null,

    @Column
    val city: String? = null,

    @Column
    val region: String? = null,

    @Column
    val zipCode: String? = null,

    @Column
    val country: String? = null,

    val active: Boolean = true
)

enum class DeliveryType {
    PICKUP,
    COURIER
}
