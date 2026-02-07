package com.cryptodrop.controller

import com.cryptodrop.dto.ChatMessageCreateDto
import com.cryptodrop.dto.ChatMessageResponseDto
import com.cryptodrop.service.UserService
import com.cryptodrop.service.ChatService
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
    private val userService: UserService
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    fun sendMessage(@Valid @RequestBody dto: ChatMessageCreateDto): ResponseEntity<ChatMessageResponseDto> {
        val senderId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        val message = chatService.sendMessage(senderId, dto)
        return ResponseEntity.ok(chatService.toDto(message))
    }

    @GetMapping("/conversation")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    fun getConversation(
        @RequestParam userId: String,
        @RequestParam(required = false) productId: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<Map<String, Any>> {
        val currentUserId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        
        val messages = chatService.getConversation(
            currentUserId, 
            userId.toLong(), 
            productId?.toLong(), 
            PageRequest.of(page, size)
        )
        return ResponseEntity.ok(mapOf(
            "messages" to messages.map { chatService.toDto(it) },
            "totalPages" to messages.totalPages,
            "currentPage" to page
        ))
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    fun getUnreadMessages(): ResponseEntity<List<ChatMessageResponseDto>> {
        val userId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        val messages = chatService.getUnreadMessages(userId)
        return ResponseEntity.ok(messages.map { chatService.toDto(it) })
    }

    @PutMapping("/{messageId}/read")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER', 'ADMIN')")
    fun markAsRead(@PathVariable messageId: String): ResponseEntity<Void> {
        val userId = userService.getCurrentUserId()
            ?: throw IllegalStateException("User not authenticated")
        chatService.markAsRead(messageId.toLong(), userId)
        return ResponseEntity.ok().build()
    }
}

@Controller
class ChatWebSocketController(
    private val chatService: ChatService,
    private val userService: UserService
) {
    @MessageMapping("/chat.send")
    fun sendMessage(@Payload dto: ChatMessageCreateDto) {
        val senderId = userService.getCurrentUserId()
        if (senderId != null) {
            chatService.sendMessage(senderId, dto)
        }
    }
}
