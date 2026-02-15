package com.cryptodrop.repository

import com.cryptodrop.model.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByUserId(userId: Long): List<CartItem>
    fun findByUserIdAndProductId(userId: Long, productId: Long): CartItem?
    fun deleteByUserIdAndProductId(userId: Long, productId: Long)
}
