package com.cryptodrop.persistence.cartitem

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface CartItemRepository : JpaRepository<CartItem, UUID> {
    fun findByCartId(cartId: UUID): List<CartItem>
    fun findByCartIdAndProductId(cartId: UUID, productId: UUID): Optional<CartItem>

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM CartItem ci WHERE ci.cart.user.id = :userId AND ci.product.id = :productId")
    fun deleteByUserIdAndProductId(@Param("userId") userId: UUID, @Param("productId") productId: UUID)
}
