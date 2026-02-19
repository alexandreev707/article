package com.cryptodrop.service.dto

import com.cryptodrop.persistence.product.ProductStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal

data class ProductCreateDto(
    @field:NotBlank(message = "Title is required")
    val title: String,
    @field:NotBlank(message = "Description is required")
    val description: String,
    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be greater than 0")
    val price: BigDecimal,
    @field:NotBlank(message = "Category is required")
    val category: String,
    val images: List<String> = emptyList(),
    val attributes: Map<String, String> = emptyMap(),
    @field:Min(value = 0, message = "Stock must be non-negative")
    val stock: Int = 0
)

data class ProductUpdateDto(
    val title: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val category: String? = null,
    val images: List<String>? = null,
    val attributes: Map<String, String>? = null,
    val stock: Int? = null,
    val active: Boolean? = null,
    val status: ProductStatus? = null
)

data class ProductResponseDto(
    val id: String,
    val sellerId: String,
    val sellerName: String? = null,
    val title: String,
    val description: String,
    val price: BigDecimal,
    val category: String,
    val images: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val attributes: Map<String, String>,
    val stock: Int,
    val active: Boolean,
    val status: String,
    val isFeatured: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)
