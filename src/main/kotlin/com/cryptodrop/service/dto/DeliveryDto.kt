package com.cryptodrop.service.dto

import com.cryptodrop.persistence.deliveryoption.DeliveryType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class DeliveryOptionCreateDto(
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:NotNull(message = "Type is required")
    val type: DeliveryType,
    val description: String? = null,
    @field:DecimalMin(value = "0", message = "Price must be non-negative")
    val price: BigDecimal = BigDecimal.ZERO,
    val freeShippingThreshold: BigDecimal? = null,
    @field:Min(0)
    val estimatedDaysMin: Int? = null,
    @field:Min(0)
    val estimatedDaysMax: Int? = null,
    val pickupAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val active: Boolean = true
)

data class DeliveryOptionUpdateDto(
    val name: String? = null,
    val type: DeliveryType? = null,
    val description: String? = null,
    @field:DecimalMin(value = "0", message = "Price must be non-negative")
    val price: BigDecimal? = null,
    val freeShippingThreshold: BigDecimal? = null,
    @field:Min(0)
    val estimatedDaysMin: Int? = null,
    @field:Min(0)
    val estimatedDaysMax: Int? = null,
    val pickupAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val active: Boolean? = null
)

data class DeliveryOptionDto(
    val id: String,
    val name: String,
    val type: DeliveryType,
    val price: BigDecimal,
    val estimatedDays: Int?,
    val addressLine: String?,
    val city: String?,
    val region: String?,
    val zipCode: String?,
    val country: String?
)
