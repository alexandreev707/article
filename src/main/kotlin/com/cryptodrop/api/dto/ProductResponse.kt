package com.cryptodrop.api.dto

import java.math.BigDecimal

data class ProductResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val sellerWallet: String,
    val priceUsd: BigDecimal,
    val images: List<String>,
    val specs: String?,
    val shippingProfiles: String?,
    val categoryId: Long,
    val status: String,
    val createdAt: String
)

data class ProductsPageResponse(
    val products: List<ProductResponse>,
    val total: Long,
    val page: Int,
    val limit: Int
)
