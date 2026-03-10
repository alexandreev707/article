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

/** Profile data for current user (read-only email/username, editable fullName/phone/walletAddress) */
data class ProfileDto(
    val id: String,
    val email: String,
    val username: String,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val walletAddress: String? = null
)

data class UpdateProfileDto(
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val walletAddress: String? = null
)
