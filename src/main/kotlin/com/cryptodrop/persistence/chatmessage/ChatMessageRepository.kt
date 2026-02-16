package com.cryptodrop.persistence.chatmessage

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, UUID> {
    fun findByConversationIdOrderByTimestampDesc(conversationId: UUID, pageable: Pageable): Page<ChatMessage>

    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp DESC")
    fun findByConversation(@Param("conversationId") conversationId: UUID, pageable: Pageable): Page<ChatMessage>

    @Query("""
        SELECT m FROM ChatMessage m 
        WHERE m.read = false AND m.sender.id != :userId 
        AND (m.conversation.user1.id = :userId OR m.conversation.user2.id = :userId)
    """)
    fun findByReceiverIdAndReadFalse(@Param("userId") userId: UUID): List<ChatMessage>
}
