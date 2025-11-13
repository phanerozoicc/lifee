package com.github.phanerozoicc.chat.domain.model

/**
 * 模型配置类，包含模型名称、温度、最大token数等参数 用于缓存的对话中
 * 应该通过接口从配置模块获取
 */
class ModelConfiguration(
    private var chatModel: ChatModel, // 关联的聊天模型
    var enableWebSearch: Boolean = false, // 是否启用网络搜索
    var enableVectorDB: Boolean = false, // 是否启用向量数据库
) {
    fun getModelName(): String {
        return chatModel.modelName
    }

    fun updateModel(newModel: ChatModel) {
        this.chatModel = newModel
    }

    fun getChatModel(): ChatModel {
        return chatModel
    }

}


data class ChatModel(
    val modelName: String, // 模型名称
    val modelUrl: String, // 模型URL
    val temperature: Double = 0.7, // 模型温度
    val maxTokens: Int = 2048, // 最大token数
    val topP: Double = 1.0, // top_p参数
    val frequencyPenalty: Double = 0.0, // frequency_penalty参数
    val presencePenalty: Double = 0.0 // presence_penalty参数
)