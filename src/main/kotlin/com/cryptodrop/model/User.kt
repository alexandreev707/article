package com.cryptodrop.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    val keycloakId: String,
    val email: String,
    val username: String,
    val roles: Set<String> = setOf("ROLE_CUSTOMER"),
    val favoriteProductIds: MutableSet<String> = mutableSetOf(),
    val blocked: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

