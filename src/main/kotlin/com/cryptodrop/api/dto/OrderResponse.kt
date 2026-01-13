package com.cryptodrop.api.dto

data class OrderResponse(
    val id: Long,
    val productId: Long,
    val buyerWallet: String,
    val sellerWallet: String,
    val amountUsdc: String,
    val shippingCountry: String?,
    val shippingAddress: String?,
    val shippingCostUsd: String?,
    val solanaEscrow: String?,
    val solanaTxId: String?,
    val status: String,
    val trackingNumber: String?,
    val createdAt: String,
    val shippedAt: String?,
    val deliveredAt: String?,
    val completedAt: String?
)

data class OrdersPageResponse(
    val orders: List<OrderResponse>,
    val total: Long,
    val page: Int,
    val limit: Int
)
