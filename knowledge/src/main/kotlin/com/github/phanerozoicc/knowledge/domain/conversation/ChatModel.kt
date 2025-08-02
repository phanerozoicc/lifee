package com.github.phanerozoicc.knowledge.domain.conversation

data class ChatModel(
    private val modelType: ModelTypeEnum
) {

    fun getModelName(): String {
        return modelType.modelName
    }
}