package com.cryptodrop.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val buyerId: Long,
    
    @Column(nullable = false)
    val sellerId: Long,
    
    @Column(nullable = false)
    val productId: Long,
    
    val quantity: Int = 1,
    
    @Column(nullable = false, precision = 19, scale = 2)
    val totalPrice: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    val status: OrderStatus = OrderStatus.PENDING,
    
    @Embedded
    val shippingAddress: Address,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

@Embeddable
data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)
