package com.cryptodrop.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_messages")
data class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val senderId: Long,
    
    @Column(nullable = false)
    val receiverId: Long,
    
    val productId: Long? = null,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val text: String = "",
    
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    val read: Boolean = false
)
