package com.cryptodrop.persistence.chatmessage

import com.cryptodrop.persistence.chatconversation.ChatConversation
import com.cryptodrop.persistence.user.User
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "chat_messages", indexes = [
    Index(name = "idx_conversation_id", columnList = "conversation_id"),
    Index(name = "idx_timestamp", columnList = "timestamp")
])
data class ChatMessage(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    val conversation: ChatConversation,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @Column(columnDefinition = "TEXT", nullable = false)
    val text: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    val messageType: MessageType = MessageType.TEXT,

    @Column(name = "attachment_url")
    val attachmentUrl: String? = null,

    var read: Boolean = false,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class MessageType {
    TEXT,
    IMAGE,
    FILE
}
