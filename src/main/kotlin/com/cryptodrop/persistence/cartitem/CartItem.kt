package com.cryptodrop.persistence.cartitem

import com.cryptodrop.persistence.cart.Cart
import com.cryptodrop.persistence.product.Product
import com.cryptodrop.persistence.productvariant.ProductVariant
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "cart_items", uniqueConstraints = [
    UniqueConstraint(columnNames = ["cart_id", "product_id"])
])
data class CartItem(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    val cart: Cart,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    val variant: ProductVariant? = null,

    @Column(nullable = false)
    var quantity: Int = 1,

    @Column(name = "added_at", nullable = false, updatable = false)
    val addedAt: LocalDateTime = LocalDateTime.now()
)
