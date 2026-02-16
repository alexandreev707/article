package com.cryptodrop.service.dto

import java.math.BigDecimal

data class CartItemResponseDto(
    val cartItemId: String,
    val productId: String,
    val title: String,
    val price: BigDecimal,
    val quantity: Int,
    val imageUrl: String?,
    val stock: Int,
    val sellerId: String
)

data class CartResponseDto(
    val items: List<CartItemResponseDto>,
    val totalItems: Int,
    val subtotal: BigDecimal
)

data class CartAddDto(
    val productId: String,
    val quantity: Int = 1
)

data class CartUpdateQuantityDto(
    val quantity: Int
)
