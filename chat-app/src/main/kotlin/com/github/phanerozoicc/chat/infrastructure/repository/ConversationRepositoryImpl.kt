package com.github.phanerozoicc.chat.infrastructure.repository

import com.github.phanerozoicc.chat.domain.model.Conversation
import com.github.phanerozoicc.chat.domain.repository.ConversationRepository
import com.github.phanerozoicc.chat.infrastructure.persistence.repository.JpaConversationRepository
import org.springframework.stereotype.Repository


@Repository
class ConversationRepositoryImpl(
    val jpaRepository: JpaConversationRepository
): ConversationRepository {
    override fun findNewByUserId(userId: String): Conversation {
        return jpaRepository.findByUserIdAndStatus(userId, "NEW")
            ?.let {
                return it.toDomain()
            }
    }
}