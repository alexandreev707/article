package com.cryptodrop.model

import jakarta.persistence.*

@Entity
@Table(name = "cart_items", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_id", "product_id"])
])
data class  CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val productId: Long,

    @Column(nullable = false)
    var quantity: Int = 1
)
