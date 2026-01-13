package com.cryptodrop.domain.repository

import com.cryptodrop.domain.model.Order
import com.cryptodrop.domain.model.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    
    fun findByBuyerWallet(buyerWallet: String, pageable: Pageable): Page<Order>
    
    fun findBySellerWallet(sellerWallet: String, pageable: Pageable): Page<Order>
    
    fun findByStatus(status: OrderStatus, pageable: Pageable): Page<Order>
    
    fun findBySolanaEscrow(solanaEscrow: String): Order?
    
    fun findBySolanaTxId(solanaTxId: String): Order?
}
