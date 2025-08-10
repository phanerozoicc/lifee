package com.lifee.knowledge.application.queries

import com.lifee.common.cqrs.queries.Query

/**
 * 获取文档查询
 */
data class GetDocumentQuery(
    val knowledgeBaseId: String,
    val documentId: String,
    val userId: String
) : Query