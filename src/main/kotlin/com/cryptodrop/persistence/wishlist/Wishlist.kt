package com.cryptodrop.persistence.wishlist

import com.cryptodrop.persistence.product.Product
import com.cryptodrop.persistence.user.User
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "wishlists", uniqueConstraints = [
    UniqueConstraint(columnNames = ["user_id", "product_id"])
])
data class Wishlist(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "added_at", nullable = false, updatable = false)
    val addedAt: LocalDateTime = LocalDateTime.now()
)
