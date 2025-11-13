package com.github.phanerozoicc.chat.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class ConversationEntity(
    @Id
    @Column(name = "id", length = 36)
    var id: String = "",
    @Column(name = "user_id", length = 36, nullable = false)
    var userId: String = "",
    @Column(name = "model_name", length = 100, nullable = false)
    var modelName: String = "gpt-3.5-turbo",
    @Column(name = "enable_web_search", nullable = false)
    var enableWebSearch: Boolean = false,
    @Column(name = "enable_vector_db", nullable = false)
    var enableVectorDB: Boolean = false
) {

}