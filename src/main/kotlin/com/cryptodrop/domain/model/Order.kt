package com.cryptodrop.domain.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "orders", indexes = [
    Index(name = "idx_buyer", columnList = "buyer_wallet"),
    Index(name = "idx_seller", columnList = "seller_wallet"),
    Index(name = "idx_status", columnList = "status")
])
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "product_id", nullable = false)
    val productId: Long,
    
    @Column(name = "buyer_wallet", nullable = false, length = 66)
    val buyerWallet: String,
    
    @Column(name = "seller_wallet", nullable = false, length = 66)
    val sellerWallet: String,
    
    @Column(name = "amount_usdc", nullable = false, precision = 18, scale = 6)
    val amountUsdc: BigDecimal,
    
    @Column(name = "shipping_country", length = 2)
    val shippingCountry: String? = null,
    
    @Column(name = "shipping_address", columnDefinition = "JSONB")
    val shippingAddress: String? = null,
    
    @Column(name = "shipping_cost_usd", precision = 8, scale = 2)
    val shippingCostUsd: BigDecimal? = null,
    
    @Column(name = "solana_escrow", length = 44)
    val solanaEscrow: String? = null,
    
    @Column(name = "solana_tx_id", length = 88)
    val solanaTxId: String? = null,
    
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    val status: OrderStatus = OrderStatus.PENDING_PAYMENT,
    
    @Column(name = "tracking_number", length = 50)
    val trackingNumber: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "shipped_at")
    val shippedAt: Instant? = null,
    
    @Column(name = "delivered_at")
    val deliveredAt: Instant? = null,
    
    @Column(name = "completed_at")
    val completedAt: Instant? = null
)

enum class OrderStatus {
    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    REFUNDED
}
