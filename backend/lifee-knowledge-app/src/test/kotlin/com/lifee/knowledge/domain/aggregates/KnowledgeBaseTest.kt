package com.lifee.knowledge.domain.aggregates

import com.lifee.knowledge.domain.entities.Document
import com.lifee.knowledge.domain.exceptions.DocumentAlreadyExistsException
import com.lifee.knowledge.domain.exceptions.DocumentNotFoundException
import com.lifee.knowledge.domain.valueobjects.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*

class KnowledgeBaseTest : BehaviorSpec({
    
    given("一个知识库") {
        val knowledgeBaseId = KnowledgeBaseId.generate()
        val name = KnowledgeBaseName("测试知识库")
        val description = KnowledgeBaseDescription("这是一个测试知识库")
        val ownerId = UserId.generate()
        
        `when`("创建知识库") {
            val knowledgeBase = KnowledgeBase.create(
                id = knowledgeBaseId,
                name = name,
                description = description,
                ownerId = ownerId
            )
            
            then("应该正确设置属性") {
                knowledgeBase.getId() shouldBe knowledgeBaseId
                knowledgeBase.getName() shouldBe name
                knowledgeBase.getDescription() shouldBe description
                knowledgeBase.getOwnerId() shouldBe ownerId
                knowledgeBase.getDocumentCount() shouldBe 0
                knowledgeBase.getTotalSize() shouldBe 0
                knowledgeBase.isEmpty() shouldBe true
                knowledgeBase.getCreatedAt() shouldNotBe null
                knowledgeBase.getUpdatedAt() shouldNotBe null
            }
            
            then("应该发布知识库创建事件") {
                val events = knowledgeBase.getDomainEvents()
                events.size shouldBe 1
                events.first().javaClass.simpleName shouldBe "KnowledgeBaseCreatedEvent"
            }
        }
        
        `when`("添加文档") {
            val knowledgeBase = KnowledgeBase.create(knowledgeBaseId, name, description, ownerId)
            val documentId = DocumentId.generate()
            val title = DocumentTitle("测试文档")
            val content = DocumentContent("这是测试内容")
            val type = DocumentType.TEXT
            
            knowledgeBase.addDocument(documentId, title, content, type)
            
            then("应该成功添加文档") {
                knowledgeBase.getDocumentCount() shouldBe 1
                knowledgeBase.getTotalSize() shouldBe content.getLength()
                knowledgeBase.isEmpty() shouldBe false
                knowledgeBase.hasDocument(documentId) shouldBe true
                
                val document = knowledgeBase.getDocument(documentId)
                document shouldNotBe null
                document!!.getTitle() shouldBe title
                document.getContent() shouldBe content
                document.getType() shouldBe type
            }
            
            then("应该发布文档添加事件") {
                val events = knowledgeBase.getDomainEvents()
                events.size shouldBe 2 // 创建事件 + 添加文档事件
                events.last().javaClass.simpleName shouldBe "DocumentAddedEvent"
            }
        }
        
        `when`("添加重复文档") {
            val knowledgeBase = KnowledgeBase.create(knowledgeBaseId, name, description, ownerId)
            val documentId = DocumentId.generate()
            val title = DocumentTitle("测试文档")
            val content = DocumentContent("这是测试内容")
            val type = DocumentType.TEXT
            
            knowledgeBase.addDocument(documentId, title, content, type)
            
            then("应该抛出异常") {
                shouldThrow<DocumentAlreadyExistsException> {
                    knowledgeBase.addDocument(documentId, title, content, type)
                }
            }
        }
        
        `when`("更新文档") {
            val knowledgeBase = KnowledgeBase.create(knowledgeBaseId, name, description, ownerId)
            val documentId = DocumentId.generate()
            val originalTitle = DocumentTitle("原始标题")
            val originalContent = DocumentContent("原始内容")
            val type = DocumentType.TEXT
            
            knowledgeBase.addDocument(documentId, originalTitle, originalContent, type)
            
            val newTitle = DocumentTitle("新标题")
            val newContent = DocumentContent("新内容")
            
            knowledgeBase.updateDocument(documentId, newTitle, newContent)
            
            then("应该成功更新文档") {
                val document = knowledgeBase.getDocument(documentId)
                document shouldNotBe null
                document!!.getTitle() shouldBe newTitle
                document.getContent() shouldBe newContent
                knowledgeBase.getTotalSize() shouldBe newContent.getLength()
            }
            
            then("应该发布文档更新事件") {
                val events = knowledgeBase.getDomainEvents()
                events.size shouldBe 3 // 创建 + 添加 + 更新事件
                events.last().javaClass.simpleName shouldBe "DocumentUpdatedEvent"
            }
        }
        
        `when`("更新不存在的文档") {
            val knowledgeBase = KnowledgeBase.create(knowledgeBaseId, name, description, ownerId)
            val documentId = DocumentId.generate()
            val newTitle = DocumentTitle("新标题")
            val newContent = DocumentContent("新内容")
            
            then("应该抛出异常") {
                shouldThrow<DocumentNotFoundException> {
                    knowledgeBase.updateDocument(documentId, newTitle, newContent)
                }
            }
        }
        
        `when`("删除文档") {
            val knowledgeBase = KnowledgeBase.create(knowledgeBaseId, name, description, ownerId)
            val documentId = DocumentId.generate()
            val title = DocumentTitle("测试文档")
            val content = DocumentContent("这是测试内容")
            val type = DocumentType.TEXT
            
            knowledgeBase.addDocument(documentId, title, content, type)
            knowledgeBase.removeDocument(documentId)
            
            then("应该成功删除文档") {
                knowledgeBase.getDocumentCount() shouldBe 0
                knowledgeBase.getTotalSize() shouldBe 0
                knowledgeBase.isEmpty() shouldBe true
                knowledgeBase.hasDocument(documentId) shouldBe false
                knowledgeBase.getDocument(documentId) shouldBe null
            }
            
            then("应该发布文档删除事件") {
                val events = knowledgeBase.getDomainEvents()
                events.size shouldBe 3 // 创建 + 添加 + 删除事件
                events.last().javaClass.simpleName shouldBe "DocumentRemovedEvent"
            }
        }
        
        `when`("删除不存在的文档") {
            val knowledgeBase = KnowledgeBase.create(knowledgeBaseId, name, description, ownerId)
            val documentId = DocumentId.generate()
            
            then("应该抛出异常") {
                shouldThrow<DocumentNotFoundException> {
                    knowledgeBase.removeDocument(documentId)
                }
            }
        }
        
        `when`("验证所有权") {
            val knowledgeBase = KnowledgeBase.create(knowledgeBaseId, name, description, ownerId)
            val otherUserId = UserId.generate()
            
            then("所有者应该有权限") {
                knowledgeBase.isOwnedBy(ownerId) shouldBe true
            }
            
            then("其他用户应该没有权限") {
                knowledgeBase.isOwnedBy(otherUserId) shouldBe false
            }
        }
    }
})