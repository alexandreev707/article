package com.cryptodrop.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.LocalDateTime

@Document(collection = "products")
data class Product(
    @Id
    val id: String? = null,
    val sellerId: String,
    val title: String,
    val description: String,
    val price: BigDecimal,
    val category: String,
    val images: MutableList<String> = mutableListOf(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val attributes: Map<String, String> = emptyMap(),
    val stock: Int = 0,
    val active: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

