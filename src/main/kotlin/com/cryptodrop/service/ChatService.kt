package com.cryptodrop.service

import com.cryptodrop.dto.ChatMessageCreateDto
import com.cryptodrop.dto.ChatMessageResponseDto
import com.cryptodrop.model.ChatMessage
import com.cryptodrop.repository.ChatMessageRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ChatService(
    private val chatMessageRepository: ChatMessageRepository,
    private val userService: UserService,
    private val productService: ProductService,
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Transactional
    fun sendMessage(senderId: String, dto: ChatMessageCreateDto): ChatMessage {
        val message = ChatMessage(
            senderId = senderId,
            receiverId = dto.receiverId,
            productId = dto.productId,
            text = dto.text
        )

        val savedMessage = chatMessageRepository.save(message)

        // Send via WebSocket
        val responseDto = toDto(savedMessage)
        messagingTemplate.convertAndSend("/topic/chat/${dto.receiverId}", responseDto)
        messagingTemplate.convertAndSend("/topic/chat/$senderId", responseDto)

        logger.info("Message sent from $senderId to ${dto.receiverId}")
        return savedMessage
    }

    fun getConversation(userId1: String, userId2: String, productId: String?, pageable: Pageable): Page<ChatMessage> {
        return if (productId != null) {
            chatMessageRepository.findConversationByProduct(userId1, userId2, productId, pageable)
        } else {
            chatMessageRepository.findConversation(userId1, userId2, pageable)
        }
    }

    @Transactional
    fun markAsRead(messageId: String, userId: String) {
        val message = chatMessageRepository.findById(messageId)
            .orElseThrow { IllegalArgumentException("Message not found: $messageId") }

        if (message.receiverId == userId && !message.read) {
            val updatedMessage = message.copy(read = true)
            chatMessageRepository.save(updatedMessage)
        }
    }

    fun getUnreadMessages(userId: String): List<ChatMessage> {
        return chatMessageRepository.findByReceiverIdAndReadFalse(userId)
    }

    fun toDto(message: ChatMessage): ChatMessageResponseDto {
        val sender = userService.findById(message.senderId)
        val receiver = userService.findById(message.receiverId)
        val productTitle = message.productId?.let { productService.findById(it).title }

        return ChatMessageResponseDto(
            id = message.id!!,
            senderId = message.senderId,
            senderName = sender.username,
            receiverId = message.receiverId,
            receiverName = receiver.username,
            productId = message.productId,
            productTitle = productTitle,
            text = message.text,
            timestamp = message.timestamp.format(dateFormatter),
            read = message.read
        )
    }
}

