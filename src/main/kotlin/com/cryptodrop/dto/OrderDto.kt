package com.cryptodrop.dto

import com.cryptodrop.model.Address
import com.cryptodrop.model.OrderStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.math.BigDecimal

data class OrderCreateDto(
    @field:NotBlank(message = "Product ID is required")
    val productId: String,
    
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int = 1,
    
    @field:NotNull(message = "Shipping address is required")
    @field:Valid
    val shippingAddress: AddressDto
)

data class AddressDto(
    @field:NotBlank(message = "Street is required")
    val street: String,
    
    @field:NotBlank(message = "City is required")
    val city: String,
    
    @field:NotBlank(message = "State is required")
    val state: String,
    
    @field:NotBlank(message = "ZIP code is required")
    val zipCode: String,
    
    @field:NotBlank(message = "Country is required")
    val country: String
)

data class OrderResponseDto(
    val id: String,
    val buyerId: String,
    val sellerId: String,
    val productId: String,
    val productTitle: String? = null,
    val quantity: Int,
    val totalPrice: BigDecimal,
    val discountAmount: BigDecimal? = null,
    val status: OrderStatus,
    val shippingAddress: AddressDto,
    val createdAt: String,
    val updatedAt: String
)

data class OrderStatusUpdateDto(
    @field:NotNull(message = "Status is required")
    val status: OrderStatus
)

data class CheckoutDto(
    val deliveryOptionId: Long,
    val shippingAddress: AddressDto? = null,
    val discountAmount: BigDecimal? = null,
    val paymentMethodId: String? = null
)




