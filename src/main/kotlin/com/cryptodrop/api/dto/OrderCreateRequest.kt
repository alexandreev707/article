package com.cryptodrop.api.dto

import jakarta.validation.constraints.NotNull

data class OrderCreateRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: Long,
    
    val shippingCountry: String? = null,
    
    val shippingAddress: String? = null
)
