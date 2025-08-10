package com.lifee.knowledge.domain.valueobjects

import java.util.*

/**
 * 文档ID值对象
 */
data class DocumentId(
    val value: UUID
) {
    companion object {
        fun generate(): DocumentId = DocumentId(UUID.randomUUID())
        
        fun fromString(value: String): DocumentId {
            return try {
                DocumentId(UUID.fromString(value))
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid DocumentId format: $value", e)
            }
        }
    }
    
    override fun toString(): String = value.toString()
}