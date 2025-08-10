package com.lifee.knowledge.application.dto

import java.time.Instant

/**
 * 文档数据传输对象
 */
data class DocumentDto(
    val id: String,
    val title: String,
    val content: String,
    val type: String,
    val size: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val knowledgeBaseId: String
)