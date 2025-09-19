package com.github.phanerozoicc.chat.domain

/**
 * 模型配置类，包含模型名称、温度、最大token数等参数 用于缓存的对话中
 * 应该通过接口从配置模块获取
 */
class ModelConfiguration(
    val modelName: String, // 模型名称
    val temperature: Double = 0.7, // 模型温度
    val maxTokens: Int = 2048, // 最大
    val topP: Double = 1.0,
    val frequencyPenalty: Double = 0.0,
    val presencePenalty: Double = 0.0
) {
   init {
       require(modelName.isNotBlank()) {"关联的模型名称不能为空"}
       require(temperature in 0.0..2.0) {"模型温度必须在0.0到2.0之间"}
       require(maxTokens > 0) { "最大token数必须大于0" }
       require(topP in 0.0..1.0) { "TopP参数必须在0.0到1.0之间" }
       require(frequencyPenalty in -2.0..2.0) { "频率惩罚必须在-2.0到2.0之间" }
       require(presencePenalty in -2.0..2.0) { "存在惩罚必须在-2.0到2.0之间" }
   }

    companion object {

        fun default(): ModelConfiguration = ModelConfiguration(
            modelName = "gpt-3.5-turbo",
            temperature = 0.7,
            maxTokens = 2048,
            topP = 1.0
        )

        fun creative() = ModelConfiguration(
            modelName = "gpt-4",
            temperature = 1.2,
            maxTokens = 4096,
            topP = 0.9
        )

        fun precise() = ModelConfiguration(
            modelName = "gpt-4",
            temperature = 0.1,
            maxTokens = 2048,
            topP = 0.1
        )
    }
}