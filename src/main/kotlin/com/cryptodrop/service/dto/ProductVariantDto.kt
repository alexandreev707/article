package com.cryptodrop.service.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ProductVariantCreateDto(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255)
    val name: String,
    @field:Size(max = 100)
    val sku: String? = null,
    @field:DecimalMin(value = "0", message = "Price must be non-negative")
    val price: BigDecimal? = null,
    @field:Min(value = 0, message = "Stock must be non-negative")
    val stock: Int = 0,
    val attributes: Map<String, String> = emptyMap(),
    val imageUrl: String? = null,
    val active: Boolean = true
)

data class ProductVariantUpdateDto(
    @field:Size(max = 255)
    val name: String? = null,
    @field:Size(max = 100)
    val sku: String? = null,
    @field:DecimalMin(value = "0", message = "Price must be non-negative")
    val price: BigDecimal? = null,
    @field:Min(value = 0, message = "Stock must be non-negative")
    val stock: Int? = null,
    val attributes: Map<String, String>? = null,
    val imageUrl: String? = null,
    val active: Boolean? = null
)

data class ProductVariantResponseDto(
    val id: String,
    val productId: String,
    val name: String,
    val sku: String?,
    val price: BigDecimal?,
    val stock: Int,
    val attributes: Map<String, String>,
    val imageUrl: String?,
    val active: Boolean
)
