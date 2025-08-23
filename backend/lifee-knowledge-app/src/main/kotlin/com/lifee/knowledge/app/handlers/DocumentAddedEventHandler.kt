package com.lifee.knowledge.app.handlers

import com.lifee.common.cqrs.events.EventHandler
import com.lifee.common.cqrs.events.Idempotent
import com.lifee.common.cqrs.events.IdempotentKeyStrategy
import com.lifee.knowledge.domain.events.DocumentAddedEvent
import com.lifee.knowledge.app.services.DocumentProcessingService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 文档添加事件处理器
 * 负责在文档添加后启动处理流程（向量化和索引构建）
 */
@Component
class DocumentAddedEventHandler(
    private val documentProcessingService: DocumentProcessingService
) : EventHandler<DocumentAddedEvent> {
    
    private val logger = LoggerFactory.getLogger(DocumentAddedEventHandler::class.java)
    
    @Idempotent(keyStrategy = IdempotentKeyStrategy.AGGREGATE_EVENT_TYPE)
    override fun handle(event: DocumentAddedEvent) {
        logger.info("处理文档添加事件: documentId={}, knowledgeBaseId={}", 
            event.documentId.value, event.knowledgeBaseId.value)
        
        try {
            runBlocking {
                // 启动文档处理流程
                documentProcessingService.processDocument(
                    knowledgeBaseId = event.knowledgeBaseId,
                    documentId = event.documentId,
                    userId = event.userId,
                    title = event.title.value,
                    content = event.content.value,
                    type = event.type.value
                )
            }
            
            logger.info("文档处理流程启动成功: documentId={}", event.documentId.value)
        } catch (e: Exception) {
            logger.error("文档处理流程启动失败: documentId={}", event.documentId.value, e)
            // 这里可以实现重试机制或将任务放入死信队列
            throw e
        }
    }
}