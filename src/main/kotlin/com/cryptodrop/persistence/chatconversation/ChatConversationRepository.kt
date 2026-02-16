package com.cryptodrop.persistence.chatconversation

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface ChatConversationRepository : JpaRepository<ChatConversation, UUID> {
    @Query("""
        SELECT c FROM ChatConversation c 
        WHERE (c.user1.id = :user1Id AND c.user2.id = :user2Id) OR (c.user1.id = :user2Id AND c.user2.id = :user1Id)
    """)
    fun findByUser1AndUser2(
        @Param("user1Id") user1Id: UUID,
        @Param("user2Id") user2Id: UUID
    ): Optional<ChatConversation>
}
