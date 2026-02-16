package com.cryptodrop.persistence.wishlist

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface WishlistRepository : JpaRepository<Wishlist, UUID> {
    fun findByUserId(userId: UUID): List<Wishlist>
    fun findByUserIdAndProductId(userId: UUID, productId: UUID): Optional<Wishlist>
    fun deleteByUserIdAndProductId(userId: UUID, productId: UUID)
}
