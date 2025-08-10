package com.lifee.knowledge.application.queries

import com.lifee.common.cqrs.queries.Query

/**
 * 获取用户知识库列表查询
 */
data class GetUserKnowledgeBasesQuery(
    val userId: String,
    val offset: Int = 0,
    val limit: Int = 20
) : Query