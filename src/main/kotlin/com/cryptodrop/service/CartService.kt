package com.cryptodrop.service

import com.cryptodrop.dto.CartItemResponseDto
import com.cryptodrop.dto.CartResponseDto
import com.cryptodrop.model.CartItem
import com.cryptodrop.model.Product
import com.cryptodrop.repository.CartItemRepository
import com.cryptodrop.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class CartService(
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val productService: ProductService
) {

    fun getCart(userId: Long): CartResponseDto {
        val items = cartItemRepository.findByUserId(userId)
        if (items.isEmpty()) {
            return CartResponseDto(items = emptyList(), totalItems = 0, subtotal = BigDecimal.ZERO)
        }
        val productIds = items.map { it.productId }
        val products = productRepository.findByIdIn(productIds).associateBy { it.id!! }
        val itemDtos = items.mapNotNull { item ->
            val product = products[item.productId] ?: return@mapNotNull null
            toItemDto(item, product)
        }
        val subtotal = itemDtos.fold(BigDecimal.ZERO) { acc, dto ->
            acc.add(dto.price.multiply(BigDecimal(dto.quantity)))
        }
        return CartResponseDto(
            items = itemDtos,
            totalItems = itemDtos.sumOf { it.quantity },
            subtotal = subtotal
        )
    }

    @Transactional
    fun addItem(userId: Long, productId: Long, quantity: Int): CartItem {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        if (!product.active) throw IllegalStateException("Product is not available")
        if (product.stock < quantity) throw IllegalStateException("Insufficient stock")

        val existing = cartItemRepository.findByUserIdAndProductId(userId, productId)
        return if (existing != null) {
            val newQty = (existing.quantity + quantity).coerceAtMost(product.stock)
            existing.quantity = newQty
            cartItemRepository.save(existing)
        } else {
            cartItemRepository.save(
                CartItem(userId = userId, productId = productId, quantity = quantity.coerceAtLeast(1))
            )
        }
    }

    @Transactional
    fun updateQuantity(userId: Long, productId: Long, quantity: Int): CartItem {
        val item = cartItemRepository.findByUserIdAndProductId(userId, productId)
            ?: throw IllegalArgumentException("Cart item not found")
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        val newQty = quantity.coerceIn(1, product.stock)
        item.quantity = newQty
        return cartItemRepository.save(item)
    }

    @Transactional
    fun removeItem(userId: Long, productId: Long) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId)
    }

    fun getCartCount(userId: Long): Int {
        return cartItemRepository.findByUserId(userId).sumOf { it.quantity }
    }

    private fun toItemDto(item: CartItem, product: Product): CartItemResponseDto {
        return CartItemResponseDto(
            cartItemId = item.id!!,
            productId = product.id!!,
            title = product.title,
            price = product.price,
            quantity = item.quantity,
            imageUrl = product.images.firstOrNull(),
            stock = product.stock,
            sellerId = product.sellerId
        )
    }
}
