package com.cryptodrop.service.dto

data class UserResponseDto(
    val id: String,
    val email: String,
    val username: String,
    val roles: Set<String>,
    val blocked: Boolean,
    val emailVerified: Boolean = false
)

data class UserUpdateDto(
    val roles: Set<String>? = null,
    val blocked: Boolean? = null,
    val emailVerified: Boolean? = null
)
