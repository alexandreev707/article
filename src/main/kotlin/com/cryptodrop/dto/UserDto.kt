package com.cryptodrop.dto

data class UserResponseDto(
    val id: String,
    val email: String,
    val username: String,
    val roles: Set<String>,
    val blocked: Boolean
)

data class UserUpdateDto(
    val roles: Set<String>? = null,
    val blocked: Boolean? = null
)




