package com.cryptodrop.service

import com.cryptodrop.persistence.cart.Cart
import com.cryptodrop.persistence.cart.CartRepository
import com.cryptodrop.persistence.cartitem.CartItem
import com.cryptodrop.persistence.cartitem.CartItemRepository
import com.cryptodrop.persistence.product.Product
import com.cryptodrop.persistence.product.ProductRepository
import com.cryptodrop.service.dto.CartItemResponseDto
import com.cryptodrop.service.dto.CartResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val userService: UserService
) {

    private fun getOrCreateCart(userId: UUID): Cart {
        return cartRepository.findByUserId(userId)
            .orElseGet {
                val user = userService.findById(userId)
                cartRepository.save(Cart(user = user))
            }
    }

    fun getCart(userId: UUID): CartResponseDto {
        val cart = cartRepository.findByUserId(userId).orElse(null)
            ?: return CartResponseDto(items = emptyList(), totalItems = 0, subtotal = BigDecimal.ZERO)
        val items = cartItemRepository.findByCartId(cart.id!!)
        if (items.isEmpty()) {
            return CartResponseDto(items = emptyList(), totalItems = 0, subtotal = BigDecimal.ZERO)
        }
        val products = productRepository.findByIdIn(items.map { it.product.id!! }).associateBy { it.id!! }
        val itemDtos = items.mapNotNull { item ->
            val product = products[item.product.id] ?: return@mapNotNull null
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
    fun addItem(userId: UUID, productId: UUID, quantity: Int): CartItem {
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        if (!product.active) throw IllegalStateException("Product is not available")
        if (product.stock < quantity) throw IllegalStateException("Insufficient stock")

        val cart = getOrCreateCart(userId)
        val existing = cartItemRepository.findByCartIdAndProductId(cart.id!!, productId)
        return if (existing.isPresent) {
            val item = existing.get()
            val newQty = (item.quantity + quantity).coerceAtMost(product.stock)
            item.quantity = newQty
            cart.updatedAt = LocalDateTime.now()
            cartRepository.save(cart)
            cartItemRepository.save(item)
            item
        } else {
            val item = CartItem(cart = cart, product = product, quantity = quantity.coerceAtLeast(1))
            cart.items.add(item)
            cart.updatedAt = LocalDateTime.now()
            cartRepository.save(cart)
            cartItemRepository.save(item)
            item
        }
    }

    @Transactional
    fun updateQuantity(userId: UUID, productId: UUID, quantity: Int): CartItem {
        val cart = getOrCreateCart(userId)
        val item = cartItemRepository.findByCartIdAndProductId(cart.id!!, productId)
            .orElseThrow { IllegalArgumentException("Cart item not found") }
        val product = productRepository.findById(productId)
            .orElseThrow { IllegalArgumentException("Product not found: $productId") }
        val newQty = quantity.coerceIn(1, product.stock)
        item.quantity = newQty
        cart.updatedAt = LocalDateTime.now()
        cartRepository.save(cart)
        return cartItemRepository.save(item)
    }

    @Transactional
    fun removeItem(userId: UUID, productId: UUID) {
        val cart = cartRepository.findByUserId(userId).orElse(null) ?: return
        val item = cartItemRepository.findByCartIdAndProductId(cart.id!!, productId).orElse(null) ?: return
        cart.items.remove(item)
        cartItemRepository.delete(item)
        cart.updatedAt = LocalDateTime.now()
        cartRepository.save(cart)
    }

    fun getCartCount(userId: UUID): Int {
        val cart = cartRepository.findByUserId(userId).orElse(null) ?: return 0
        return cartItemRepository.findByCartId(cart.id!!).sumOf { it.quantity }
    }

    internal fun getCartItems(userId: UUID): List<CartItem> {
        val cart = cartRepository.findByUserId(userId).orElse(null) ?: return emptyList()
        return cartItemRepository.findByCartId(cart.id!!)
    }

    private fun toItemDto(item: CartItem, product: Product): CartItemResponseDto {
        return CartItemResponseDto(
            cartItemId = item.id!!.toString(),
            productId = product.id!!.toString(),
            title = product.title,
            price = product.price,
            quantity = item.quantity,
            imageUrl = product.images.firstOrNull(),
            stock = product.stock,
            sellerId = product.seller.id!!.toString()
        )
    }
}
