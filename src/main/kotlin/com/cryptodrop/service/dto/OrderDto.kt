package com.cryptodrop.service.dto

import com.cryptodrop.persistence.order.OrderStatus
import com.cryptodrop.persistence.order.PaymentMethod
import com.cryptodrop.persistence.order.PaymentStatus
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
    val productImage: String? = null,
    val quantity: Int,
    val totalPrice: BigDecimal,
    val discountAmount: BigDecimal? = null,
    val status: OrderStatus,
    val paymentStatus: PaymentStatus? = null,
    val paymentMethod: PaymentMethod? = null,
    val shippingAddress: AddressDto,
    val createdAt: String,
    val updatedAt: String,
    /** Set when buyer confirms receipt (order completed) */
    val deliveredAt: String? = null,
    /** OxaPay payout track id after seller withdraws funds */
    val payoutTrackId: String? = null,
    /** OxaPay payout track id after buyer cancels a paid order (refund to wallet) */
    val refundTrackId: String? = null,
)

data class OrderCancelRequestDto(
    val refundWalletAddress: String? = null
)

data class OrderStatusUpdateDto(
    @field:NotNull(message = "Status is required")
    val status: OrderStatus
)

data class CheckoutDto(
    val deliveryOptionId: String,
    val shippingAddress: AddressDto? = null,
    val discountAmount: BigDecimal? = null,
    val paymentMethodId: String? = null,
    val cartProductId: String? = null
)
