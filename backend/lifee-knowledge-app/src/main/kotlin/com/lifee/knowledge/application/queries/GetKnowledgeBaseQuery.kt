package com.lifee.knowledge.application.queries

import com.lifee.common.cqrs.queries.Query

/**
 * 获取知识库查询
 */
data class GetKnowledgeBaseQuery(
    val knowledgeBaseId: String,
    val userId: String
) : Query