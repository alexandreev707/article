package com.cryptodrop.repository

import com.cryptodrop.model.ChatMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : MongoRepository<ChatMessage, String> {
    @Query("{'\$or': [{'senderId': ?0, 'receiverId': ?1}, {'senderId': ?1, 'receiverId': ?0}]}")
    fun findConversation(senderId: String, receiverId: String, pageable: Pageable): Page<ChatMessage>
    
    @Query("{'\$or': [{'senderId': ?0, 'receiverId': ?1, 'productId': ?2}, {'senderId': ?1, 'receiverId': ?0, 'productId': ?2}]}")
    fun findConversationByProduct(senderId: String, receiverId: String, productId: String, pageable: Pageable): Page<ChatMessage>
    
    fun findByReceiverIdAndReadFalse(receiverId: String): List<ChatMessage>
}

