package com.lifee.knowledge.domain.valueobjects

import java.util.*

/**
 * 知识库ID值对象
 */
data class KnowledgeBaseId(
    val value: UUID
) {
    companion object {
        fun generate(): KnowledgeBaseId = KnowledgeBaseId(UUID.randomUUID())
        
        fun fromString(value: String): KnowledgeBaseId {
            return try {
                KnowledgeBaseId(UUID.fromString(value))
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid KnowledgeBaseId format: $value", e)
            }
        }
    }
    
    override fun toString(): String = value.toString()
}