package com.cryptodrop.dto

import jakarta.validation.constraints.*

data class ChatMessageCreateDto(
    @field:NotBlank(message = "Receiver ID is required")
    val receiverId: String,
    
    val productId: String? = null,
    
    @field:NotBlank(message = "Message text is required")
    @field:Size(max = 1000, message = "Message must not exceed 1000 characters")
    val text: String
)

data class ChatMessageResponseDto(
    val id: String,
    val senderId: String,
    val senderName: String? = null,
    val receiverId: String,
    val receiverName: String? = null,
    val productId: String? = null,
    val productTitle: String? = null,
    val text: String,
    val timestamp: String,
    val read: Boolean
)

