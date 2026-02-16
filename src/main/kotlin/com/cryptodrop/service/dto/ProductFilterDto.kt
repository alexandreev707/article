package com.cryptodrop.service.dto

data class ProductFilterDto(
    val category: String? = null,
    val minPrice: java.math.BigDecimal? = null,
    val maxPrice: java.math.BigDecimal? = null,
    val minRating: Double? = null,
    val search: String? = null,
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc"
)
