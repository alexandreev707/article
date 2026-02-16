package com.cryptodrop.service.dto

import com.cryptodrop.persistence.promotion.DiscountType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class PromotionCreateDto(
    @field:NotBlank(message = "Title is required")
    val title: String,
    val description: String? = null,
    val code: String? = null,
    @field:NotNull(message = "Discount type is required")
    val discountType: DiscountType,
    @field:NotNull(message = "Discount value is required")
    @field:DecimalMin(value = "0", message = "Discount value must be non-negative")
    val discountValue: BigDecimal,
    val minOrderAmount: BigDecimal? = null,
    val maxDiscountAmount: BigDecimal? = null,
    @field:NotNull(message = "Start date is required")
    val startDate: LocalDateTime,
    @field:NotNull(message = "End date is required")
    val endDate: LocalDateTime,
    @field:Min(0)
    val usageLimit: Int? = null,
    val categoryIds: List<String> = emptyList(),
    val active: Boolean = true
)

data class PromotionUpdateDto(
    val title: String? = null,
    val description: String? = null,
    val code: String? = null,
    val discountType: DiscountType? = null,
    @field:DecimalMin(value = "0", message = "Discount value must be non-negative")
    val discountValue: BigDecimal? = null,
    val minOrderAmount: BigDecimal? = null,
    val maxDiscountAmount: BigDecimal? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    @field:Min(0)
    val usageLimit: Int? = null,
    val categoryIds: List<String>? = null,
    val active: Boolean? = null
)

data class PromotionResponseDto(
    val id: String,
    val title: String,
    val description: String?,
    val code: String?,
    val discountType: DiscountType,
    val discountValue: BigDecimal,
    val minOrderAmount: BigDecimal?,
    val maxDiscountAmount: BigDecimal?,
    val startDate: String,
    val endDate: String,
    val usageLimit: Int?,
    val usageCount: Int,
    val categoryIds: List<String>,
    val active: Boolean,
    val createdAt: String
)
