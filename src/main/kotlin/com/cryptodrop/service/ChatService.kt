package com.cryptodrop.service

import com.cryptodrop.persistence.chatconversation.ChatConversation
import com.cryptodrop.persistence.chatconversation.ChatConversationRepository
import com.cryptodrop.persistence.chatmessage.ChatMessage
import com.cryptodrop.persistence.chatmessage.ChatMessageRepository
import com.cryptodrop.service.dto.ChatMessageCreateDto
import com.cryptodrop.service.dto.ChatMessageResponseDto
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class ChatService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatConversationRepository: ChatConversationRepository,
    private val userService: UserService,
    private val productService: ProductService,
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @Transactional
    fun sendMessage(senderId: UUID, dto: ChatMessageCreateDto): ChatMessage {
        val sender = userService.findById(senderId)
        val receiver = userService.findById(UUID.fromString(dto.receiverId))
        val product = dto.productId?.let { productService.findById(UUID.fromString(it)) }

        val conversation = chatConversationRepository.findByUser1AndUser2(senderId, receiver.id!!).orElseGet {
            val conv = ChatConversation(
                user1 = sender,
                user2 = receiver,
                product = product
            )
            chatConversationRepository.save(conv)
        }

        val message = ChatMessage(
            conversation = conversation,
            sender = sender,
            text = dto.text
        )
        val savedMessage = chatMessageRepository.save(message)
        conversation.lastMessageAt = LocalDateTime.now()
        chatConversationRepository.save(conversation)

        val responseDto = toDto(savedMessage)
        messagingTemplate.convertAndSend("/topic/chat/${dto.receiverId}", responseDto)
        messagingTemplate.convertAndSend("/topic/chat/$senderId", responseDto)

        logger.info("Message sent from $senderId to ${dto.receiverId}")
        return savedMessage
    }

    fun getConversation(userId1: UUID, userId2: UUID, productId: UUID?, pageable: Pageable): Page<ChatMessage> {
        val conversation = chatConversationRepository.findByUser1AndUser2(userId1, userId2)
        return if (conversation.isPresent) {
            chatMessageRepository.findByConversation(conversation.get().id!!, pageable)
        } else {
            org.springframework.data.domain.PageImpl(emptyList(), pageable, 0)
        }
    }

    @Transactional
    fun markAsRead(messageId: UUID, userId: UUID) {
        val message = chatMessageRepository.findById(messageId)
            .orElseThrow { IllegalArgumentException("Message not found: $messageId") }
        val isReceiver = message.conversation.user1.id == userId || message.conversation.user2.id == userId
        val isFromOther = message.sender.id != userId
        if (isReceiver && isFromOther && !message.read) {
            message.read = true
            chatMessageRepository.save(message)
        }
    }

    fun getUnreadMessages(userId: UUID): List<ChatMessage> {
        return chatMessageRepository.findByReceiverIdAndReadFalse(userId)
    }

    fun toDto(message: ChatMessage): ChatMessageResponseDto {
        val receiver = if (message.conversation.user1.id == message.sender.id) {
            message.conversation.user2
        } else {
            message.conversation.user1
        }
        val productTitle = message.conversation.product?.title

        return ChatMessageResponseDto(
            id = message.id!!.toString(),
            senderId = message.sender.id!!.toString(),
            senderName = message.sender.username,
            receiverId = receiver.id!!.toString(),
            receiverName = receiver.username,
            productId = message.conversation.product?.id?.toString(),
            productTitle = productTitle,
            text = message.text,
            timestamp = message.timestamp.format(dateFormatter),
            read = message.read
        )
    }
}
