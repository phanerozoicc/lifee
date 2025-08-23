package com.lifee.knowledge.application.dto

import java.time.Instant

/**
 * 知识库数据传输对象
 */
data class KnowledgeBaseDto(
    val id: String,
    val name: String,
    val description: String,
    val ownerId: String,
    val documentCount: Int,
    val totalSize: Long,
    val embeddingModel: String = "text-embedding-ada-002",
    val rerankModel: String = "bge-reranker-large",
    val createdAt: Instant,
    val updatedAt: Instant,
    val documents: List<DocumentSummaryDto> = emptyList()
)

/**
 * 文档摘要数据传输对象
 */
data class DocumentSummaryDto(
    val id: String,
    val title: String,
    val type: String,
    val size: Int,
    val summary: String,
    val createdAt: Instant,
    val updatedAt: Instant
)