package com.cryptodrop.persistence.order

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OrderRepository : JpaRepository<Order, UUID> {
    fun findByBuyerId(buyerId: UUID, pageable: Pageable): Page<Order>

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.seller.id = :sellerId")
    fun findBySellerId(@Param("sellerId") sellerId: UUID, pageable: Pageable): Page<Order>

    fun findByBuyerIdAndStatus(buyerId: UUID, status: OrderStatus, pageable: Pageable): Page<Order>

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items oi WHERE oi.seller.id = :sellerId AND o.status = :status")
    fun findBySellerIdAndStatus(
        @Param("sellerId") sellerId: UUID,
        @Param("status") status: OrderStatus,
        pageable: Pageable
    ): Page<Order>
}
