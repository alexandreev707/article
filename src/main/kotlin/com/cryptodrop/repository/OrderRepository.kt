package com.cryptodrop.repository

import com.cryptodrop.model.Order
import com.cryptodrop.model.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByBuyerId(buyerId: Long, pageable: Pageable): Page<Order>
    fun findBySellerId(sellerId: Long, pageable: Pageable): Page<Order>
    fun findByBuyerIdAndStatus(buyerId: Long, status: OrderStatus, pageable: Pageable): Page<Order>
    fun findBySellerIdAndStatus(sellerId: Long, status: OrderStatus, pageable: Pageable): Page<Order>
}
