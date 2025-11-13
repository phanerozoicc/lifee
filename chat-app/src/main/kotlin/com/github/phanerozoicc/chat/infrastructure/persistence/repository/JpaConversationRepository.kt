package com.github.phanerozoicc.chat.infrastructure.persistence.repository

import com.github.phanerozoicc.chat.infrastructure.persistence.entity.ConversationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaConversationRepository: JpaRepository<ConversationEntity, String> {

}