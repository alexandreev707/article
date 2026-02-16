package com.cryptodrop.service

import com.cryptodrop.persistence.product.Product
import com.cryptodrop.persistence.product.ProductRepository
import com.cryptodrop.persistence.productvariant.ProductVariant
import com.cryptodrop.persistence.productvariant.ProductVariantRepository
import com.cryptodrop.service.dto.ProductVariantCreateDto
import com.cryptodrop.service.dto.ProductVariantResponseDto
import com.cryptodrop.service.dto.ProductVariantUpdateDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ProductVariantService(
    private val productVariantRepository: ProductVariantRepository,
    private val productRepository: ProductRepository,
    private val userService: UserService
) {

    fun findByProductId(productId: UUID): List<ProductVariantResponseDto> {
        return productVariantRepository.findByProductId(productId).map { toDto(it) }
    }

    fun findById(variantId: UUID): ProductVariant {
        return productVariantRepository.findById(variantId)
            .orElseThrow { IllegalArgumentException("Product variant not found: $variantId") }
    }

    fun getById(productId: UUID, variantId: UUID): ProductVariantResponseDto {
        val variant = findById(variantId)
        if (variant.product.id != productId) {
            throw IllegalArgumentException("Variant does not belong to product: $productId")
        }
        return toDto(variant)
    }

    @Transactional
    fun create(productId: UUID, dto: ProductVariantCreateDto): ProductVariantResponseDto {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        ensureSellerOrAdmin(product)
        val variant = ProductVariant(
            product = product,
            name = dto.name,
            sku = dto.sku,
            price = dto.price,
            stock = dto.stock,
            attributes = dto.attributes.toMutableMap(),
            imageUrl = dto.imageUrl,
            active = dto.active
        )
        return toDto(productVariantRepository.save(variant))
    }

    @Transactional
    fun update(productId: UUID, variantId: UUID, dto: ProductVariantUpdateDto): ProductVariantResponseDto {
        val variant = findById(variantId)
        if (variant.product.id != productId) {
            throw IllegalArgumentException("Variant does not belong to product: $productId")
        }
        ensureSellerOrAdmin(variant.product)
        val updated = ProductVariant(
            id = variant.id,
            product = variant.product,
            name = dto.name ?: variant.name,
            sku = dto.sku ?: variant.sku,
            price = dto.price ?: variant.price,
            stock = dto.stock ?: variant.stock,
            attributes = (dto.attributes ?: variant.attributes).toMutableMap(),
            imageUrl = dto.imageUrl ?: variant.imageUrl,
            active = dto.active ?: variant.active
        )
        return toDto(productVariantRepository.save(updated))
    }

    @Transactional
    fun delete(productId: UUID, variantId: UUID) {
        val variant = findById(variantId)
        if (variant.product.id != productId) {
            throw IllegalArgumentException("Variant does not belong to product: $productId")
        }
        ensureSellerOrAdmin(variant.product)
        productVariantRepository.deleteById(variantId)
    }

    private fun ensureSellerOrAdmin(product: Product) {
        val userId = userService.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        if (product.seller.id != userId && !userService.isAdmin(userId)) {
            throw IllegalStateException("Only product owner or admin can manage variants")
        }
    }

    fun toDto(v: ProductVariant): ProductVariantResponseDto = ProductVariantResponseDto(
        id = v.id!!.toString(),
        productId = v.product.id!!.toString(),
        name = v.name,
        sku = v.sku,
        price = v.price,
        stock = v.stock,
        attributes = v.attributes,
        imageUrl = v.imageUrl,
        active = v.active
    )
}
