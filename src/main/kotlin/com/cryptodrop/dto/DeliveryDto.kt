package com.cryptodrop.dto

import com.cryptodrop.model.DeliveryType
import java.math.BigDecimal

data class DeliveryOptionDto(
    val id: Long,
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
