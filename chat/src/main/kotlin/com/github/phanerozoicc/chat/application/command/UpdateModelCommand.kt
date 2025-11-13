package com.github.phanerozoicc.chat.application.command

import com.github.phanerozoicc.chat.domain.model.ConversationId

class UpdateModelCommand(
    val conversationId: ConversationId,
    val modelName: String, // 模型名称
    val enableWebSearch: Boolean, // 是否启用网络搜索
    val enableVectorDB: Boolean // 是否启用向量数据库
)