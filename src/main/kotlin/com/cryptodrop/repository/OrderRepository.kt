package com.cryptodrop.repository

import com.cryptodrop.model.Order
import com.cryptodrop.model.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : MongoRepository<Order, String> {
    fun findByBuyerId(buyerId: String, pageable: Pageable): Page<Order>
    fun findBySellerId(sellerId: String, pageable: Pageable): Page<Order>
    fun findByBuyerIdAndStatus(buyerId: String, status: OrderStatus, pageable: Pageable): Page<Order>
    fun findBySellerIdAndStatus(sellerId: String, status: OrderStatus, pageable: Pageable): Page<Order>
}

