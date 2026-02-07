package com.cryptodrop.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(unique = true, nullable = false)
    val username: String,
    
    @Column(nullable = false)
    val password: String,
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    val roles: Set<UserRole> = setOf(UserRole.CUSTOMER),
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_favorites", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "product_id")
    val favoriteProductIds: MutableSet<Long> = mutableSetOf(),
    
    val blocked: Boolean = false,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class UserRole {
    CUSTOMER, SELLER, ADMIN
}
