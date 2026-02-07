package com.cryptodrop.repository

import com.cryptodrop.model.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :senderId AND m.receiverId = :receiverId) OR (m.senderId = :receiverId AND m.receiverId = :senderId) ORDER BY m.timestamp DESC")
    fun findConversation(@Param("senderId") senderId: Long, @Param("receiverId") receiverId: Long, pageable: Pageable): Page<ChatMessage>
    
    @Query("SELECT m FROM ChatMessage m WHERE ((m.senderId = :senderId AND m.receiverId = :receiverId) OR (m.senderId = :receiverId AND m.receiverId = :senderId)) AND m.productId = :productId ORDER BY m.timestamp DESC")
    fun findConversationByProduct(@Param("senderId") senderId: Long, @Param("receiverId") receiverId: Long, @Param("productId") productId: Long, pageable: Pageable): Page<ChatMessage>
    
    fun findByReceiverIdAndReadFalse(receiverId: Long): List<ChatMessage>
}
