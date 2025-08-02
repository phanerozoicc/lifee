package com.github.phanerozoicc.knowledge.chat

import com.github.phanerozoicc.knowledge.service.LLMService
import com.github.phanerozoicc.knowledge.KnowledgeAppApplication
import com.github.phanerozoicc.knowledge.domain.conversation.ChatModel
import com.github.phanerozoicc.knowledge.domain.conversation.ModelTypeEnum
import mu.KLogging
import org.springframework.ai.openai.OpenAiEmbeddingModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import kotlin.test.Test


@SpringBootTest(classes = [KnowledgeAppApplication::class])
class LLMServiceTest(
) {

    companion object : KLogging()

    private val model = ChatModel(ModelTypeEnum.DEEPSEEK_V3)

    @Autowired
    private lateinit var llmService: LLMService
    @Test
    fun testLLMServiceSync() {
        val generateResponse = llmService.generateResponse("讲个笑话", model)
        logger().debug { generateResponse!! }
    }

    @Test
    fun testLLMServiceStream() {
        // 测试流式接口的完整输出
        val generateResponse =
            llmService.generateResponseStream("讲个笑话", model)
        val collectList = generateResponse.collectList().timeout(Duration.ofSeconds(30))
        collectList.block()?.forEach {
            logger().debug {
                it
            }
        }
    }

    @Autowired
    private lateinit var openAiEmbeddingModel: OpenAiEmbeddingModel

    @Test
    fun testEmbeddingModel() {
        val embedForResponse = openAiEmbeddingModel.embedForResponse(listOf("Tell me a joke"))
        logger().debug{
            embedForResponse
        }
    }
}
