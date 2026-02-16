package com.cryptodrop.persistence.cart

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface CartRepository : JpaRepository<Cart, UUID> {
    fun findByUserId(userId: UUID): Optional<Cart>
}
