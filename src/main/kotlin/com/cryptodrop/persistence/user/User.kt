package com.cryptodrop.persistence.user

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String,

    @Column(name = "full_name")
    val fullName: String? = null,

    @Column(name = "phone_number")
    val phoneNumber: String? = null,

    @Column(name = "avatar_url")
    val avatarUrl: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    val roles: Set<UserRole> = setOf(UserRole.CUSTOMER),

    val blocked: Boolean = false,

    @Column(name = "email_verified")
    val emailVerified: Boolean = false,

    @Column(name = "bank_account")
    val bankAccount: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class UserRole {
    CUSTOMER,
    SELLER,
    ADMIN
}
