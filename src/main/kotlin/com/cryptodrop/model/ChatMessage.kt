package com.cryptodrop.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "chat_messages")
data class ChatMessage(
    @Id
    val id: String? = null,
    val senderId: String,
    val receiverId: String,
    val productId: String? = null,
    val text: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val read: Boolean = false
)

