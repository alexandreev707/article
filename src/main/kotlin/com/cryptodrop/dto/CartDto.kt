package com.cryptodrop.dto

import java.math.BigDecimal

data class CartItemResponseDto(
    val cartItemId: Long,
    val productId: Long,
    val title: String,
    val price: BigDecimal,
    val quantity: Int,
    val imageUrl: String?,
    val stock: Int,
    val sellerId: Long
)

data class CartResponseDto(
    val items: List<CartItemResponseDto>,
    val totalItems: Int,
    val subtotal: BigDecimal
)

data class CartAddDto(
    val productId: Long,
    val quantity: Int = 1
)

data class CartUpdateQuantityDto(
    val quantity: Int
)
