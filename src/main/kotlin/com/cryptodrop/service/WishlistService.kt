package com.cryptodrop.service

import com.cryptodrop.persistence.product.Product
import com.cryptodrop.persistence.user.User
import com.cryptodrop.persistence.wishlist.Wishlist
import com.cryptodrop.persistence.wishlist.WishlistRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WishlistService(
    private val wishlistRepository: WishlistRepository,
    private val userService: UserService
) {
    fun getFavoriteProductIds(userId: UUID): List<UUID> {
        return wishlistRepository.findByUserId(userId).map { it.product.id!! }
    }

    fun isFavorite(userId: UUID, productId: UUID): Boolean {
        return wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent
    }

    @Transactional
    fun addToWishlist(user: User, product: Product): Wishlist {
        return wishlistRepository.findByUserIdAndProductId(user.id!!, product.id!!)
            .orElseGet {
                wishlistRepository.save(Wishlist(user = user, product = product))
            }
    }

    @Transactional
    fun removeFromWishlist(userId: UUID, productId: UUID) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId)
    }

    @Transactional
    fun toggleFavorite(user: User, product: Product): Boolean {
        val userId = user.id!!
        val productId = product.id!!
        return if (isFavorite(userId, productId)) {
            removeFromWishlist(userId, productId)
            false
        } else {
            addToWishlist(user, product)
            true
        }
    }

}
