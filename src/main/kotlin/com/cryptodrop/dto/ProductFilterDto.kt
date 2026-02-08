package com.cryptodrop.dto

data class ProductFilterDto(
    val category: String? = null,
    val minPrice: java.math.BigDecimal? = null,
    val maxPrice: java.math.BigDecimal? = null,
    val minRating: Double? = null,
    val search: String? = null,
    val sortBy: String = "createdAt", // createdAt, price, rating, reviewCount
    val sortOrder: String = "desc" // asc, desc
)




