package com.cryptodrop.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDateTime

@Document(collection = "orders")
data class Order(
    @Id
    val id: String? = null,
    val buyerId: String,
    val sellerId: String,
    val productId: String,
    val quantity: Int = 1,
    val totalPrice: BigDecimal,
    val status: OrderStatus = OrderStatus.PENDING,
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

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
)

