package com.cryptodrop.api.dto

import com.cryptodrop.domain.model.Product
import com.cryptodrop.domain.model.ProductStatus
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.Instant

data class ProductCreateRequest(
    @field:NotNull(message = "Category ID is required")
    val categoryId: Long,
    
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 255, message = "Title must be less than 255 characters")
    val title: String,
    
    val description: String? = null,
    
    @field:NotNull(message = "Price is required")
    @field:DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    val priceUsd: BigDecimal,
    
    val images: List<String>? = null,
    
    val specs: String? = null,
    
    val shippingProfiles: String? = null
) {
    fun toProduct(sellerWallet: String): Product {
        val imagesJson = images?.let { "[\"${it.joinToString("\",\"")}\"]" }
        
        return Product(
            sellerWallet = sellerWallet,
            categoryId = categoryId,
            title = title,
            description = description,
            priceUsd = priceUsd,
            images = imagesJson,
            specs = specs,
            shippingProfiles = shippingProfiles,
            status = ProductStatus.ACTIVE,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
