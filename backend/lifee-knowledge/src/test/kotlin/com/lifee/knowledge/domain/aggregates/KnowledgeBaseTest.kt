package com.lifee.knowledge.domain.aggregates

import com.lifee.knowledge.domain.entities.Document
import com.lifee.knowledge.domain.valueobjects.*
import com.lifee.knowledge.domain.events.KnowledgeBaseCreatedEvent
import com.lifee.knowledge.domain.events.DocumentAddedEvent
import com.lifee.knowledge.domain.events.DocumentRemovedEvent
import com.lifee.knowledge.domain.exceptions.InvalidDocumentException
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.assertions.throwables.shouldThrow
import java.time.Instant
import java.util.UUID

/**
 * 知识库聚合根测试
 */
class KnowledgeBaseTest : BehaviorSpec({
    
    given("知识库聚合根") {
        
        `when`("创建新的知识库") {
            val knowledgeBaseId = KnowledgeBaseId(UUID.randomUUID())
            val name = KnowledgeBaseName("测试知识库")
            val description = KnowledgeBaseDescription("这是一个测试知识库")
            val ownerId = UserId(UUID.randomUUID())
            
            val knowledgeBase = KnowledgeBase.create(
                id = knowledgeBaseId,
                name = name,
                description = description,
                ownerId = ownerId
            )
            
            then("应该正确初始化知识库") {
                knowledgeBase.getId() shouldBe knowledgeBaseId
                knowledgeBase.getName() shouldBe name
                knowledgeBase.getDescription() shouldBe description
                knowledgeBase.getOwnerId() shouldBe ownerId
                knowledgeBase.getDocuments().size shouldBe 0
                knowledgeBase.getCreatedAt() shouldNotBe null
                knowledgeBase.getUpdatedAt() shouldNotBe null
            }
            
            then("应该发布知识库创建事件") {
                val events = knowledgeBase.getUncommittedEvents()
                events.size shouldBe 1
                events[0] shouldBe KnowledgeBaseCreatedEvent::class
            }
        }
        
        `when`("向知识库添加文档") {
            val knowledgeBase = createTestKnowledgeBase()
            val document = createTestDocument()
            
            knowledgeBase.addDocument(document)
            
            then("应该成功添加文档") {
                knowledgeBase.getDocuments().size shouldBe 1
                knowledgeBase.getDocuments().values shouldContain document
            }
            
            then("应该发布文档添加事件") {
                val events = knowledgeBase.getUncommittedEvents()
                // 第一个事件是创建事件，第二个是添加文档事件
                events.size shouldBe 2
                events[1] shouldBe DocumentAddedEvent::class
            }
        }
        
        `when`("添加重复的文档") {
            val knowledgeBase = createTestKnowledgeBase()
            val document = createTestDocument()
            
            knowledgeBase.addDocument(document)
            
            then("应该抛出异常") {
                shouldThrow<InvalidDocumentException> {
                    knowledgeBase.addDocument(document)
                }
            }
        }
        
        `when`("从知识库移除文档") {
            val knowledgeBase = createTestKnowledgeBase()
            val document = createTestDocument()
            
            knowledgeBase.addDocument(document)
            knowledgeBase.removeDocument(document.getId())
            
            then("应该成功移除文档") {
                knowledgeBase.getDocuments().size shouldBe 0
                knowledgeBase.getDocuments().values shouldNotContain document
            }
            
            then("应该发布文档移除事件") {
                val events = knowledgeBase.getUncommittedEvents()
                // 创建、添加、移除三个事件
                events.size shouldBe 3
                events[2] shouldBe DocumentRemovedEvent::class
            }
        }
        
        `when`("移除不存在的文档") {
            val knowledgeBase = createTestKnowledgeBase()
            val nonExistentDocumentId = DocumentId(UUID.randomUUID())
            
            then("应该抛出异常") {
                shouldThrow<InvalidDocumentException> {
                    knowledgeBase.removeDocument(nonExistentDocumentId)
                }
            }
        }
        
        `when`("更新知识库信息") {
            val knowledgeBase = createTestKnowledgeBase()
            val newName = KnowledgeBaseName("更新后的知识库")
            val newDescription = KnowledgeBaseDescription("更新后的描述")
            
            knowledgeBase.updateInfo(newName, newDescription)
            
            then("应该成功更新信息") {
                knowledgeBase.getName() shouldBe newName
                knowledgeBase.getDescription() shouldBe newDescription
            }
        }
        
        `when`("查找文档") {
            val knowledgeBase = createTestKnowledgeBase()
            val document = createTestDocument()
            
            knowledgeBase.addDocument(document)
            
            then("应该能找到已添加的文档") {
                val foundDocument = knowledgeBase.findDocument(document.getId())
                foundDocument shouldBe document
            }
            
            then("查找不存在的文档应该返回null") {
                val nonExistentId = DocumentId(UUID.randomUUID())
                val foundDocument = knowledgeBase.findDocument(nonExistentId)
                foundDocument shouldBe null
            }
        }
    }
})

/**
 * 创建测试用的知识库
 */
private fun createTestKnowledgeBase(): KnowledgeBase {
    return KnowledgeBase.create(
        id = KnowledgeBaseId(UUID.randomUUID()),
        name = KnowledgeBaseName("测试知识库"),
        description = KnowledgeBaseDescription("测试描述"),
        ownerId = UserId(UUID.randomUUID())
    )
}

/**
 * 创建测试用的文档
 */
private fun createTestDocument(): Document {
    return Document(
        id = DocumentId(UUID.randomUUID()),
        title = DocumentTitle("测试文档"),
        content = DocumentContent("这是测试文档的内容"),
        type = DocumentType.TEXT,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )
}