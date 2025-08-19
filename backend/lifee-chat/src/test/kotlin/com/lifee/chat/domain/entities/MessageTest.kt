package com.lifee.chat.domain.entities

import com.lifee.chat.domain.valueobjects.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.assertions.throwables.shouldThrow
import java.time.Instant
import java.util.UUID

/**
 * 消息实体测试
 */
class MessageTest : BehaviorSpec({
    
    given("消息实体") {
        
        `when`("创建用户消息") {
            val messageId = MessageId(UUID.randomUUID())
            val content = MessageContent("这是用户的消息")
            val now = Instant.now()
            
            val message = Message.createUserMessage(
                id = messageId,
                content = content,
                timestamp = now
            )
            
            then("应该正确初始化用户消息") {
                message.getId() shouldBe messageId
                message.getContent() shouldBe content
                message.getType() shouldBe MessageType.USER
                message.getTimestamp() shouldBe now
                message.getUpdatedAt() shouldBe now
            }
        }
        
        `when`("创建助手消息") {
            val messageId = MessageId(UUID.randomUUID())
            val content = MessageContent("这是助手的回复")
            val now = Instant.now()
            
            val message = Message.createAssistantMessage(
                id = messageId,
                content = content,
                timestamp = now
            )
            
            then("应该正确初始化助手消息") {
                message.getId() shouldBe messageId
                message.getContent() shouldBe content
                message.getType() shouldBe MessageType.ASSISTANT
                message.getTimestamp() shouldBe now
                message.getUpdatedAt() shouldBe now
            }
        }
        
        `when`("创建系统消息") {
            val messageId = MessageId(UUID.randomUUID())
            val content = MessageContent("这是系统消息")
            val now = Instant.now()
            
            val message = Message.createSystemMessage(
                id = messageId,
                content = content,
                timestamp = now
            )
            
            then("应该正确初始化系统消息") {
                message.getId() shouldBe messageId
                message.getContent() shouldBe content
                message.getType() shouldBe MessageType.SYSTEM
                message.getTimestamp() shouldBe now
                message.getUpdatedAt() shouldBe now
            }
        }
        
        `when`("更新消息内容") {
            val message = createTestMessage()
            val newContent = MessageContent("更新后的内容")
            val originalUpdatedAt = message.getUpdatedAt()
            
            // 等待一毫秒确保时间戳不同
            Thread.sleep(1)
            
            message.updateContent(newContent)
            
            then("应该成功更新内容") {
                message.getContent() shouldBe newContent
                message.getUpdatedAt() shouldNotBe originalUpdatedAt
            }
        }
        
        `when`("标记消息为已删除") {
            val message = createTestMessage()
            
            message.markAsDeleted()
            
            then("应该标记为已删除") {
                message.isDeleted() shouldBe true
            }
        }
        
        `when`("检查消息相等性") {
            val messageId = MessageId(UUID.randomUUID())
            val content1 = MessageContent("内容1")
            val content2 = MessageContent("内容2")
            val now = Instant.now()
            
            val message1 = Message.createUserMessage(messageId, content1, now)
            val message2 = Message.createUserMessage(messageId, content2, now) // 相同ID，不同内容
            
            then("具有相同ID的消息应该相等") {
                message1 shouldBe message2
                message1.hashCode() shouldBe message2.hashCode()
            }
        }
        
        `when`("检查消息不相等性") {
            val message1 = createTestMessage()
            val message2 = createTestMessage() // 不同ID
            
            then("具有不同ID的消息应该不相等") {
                message1 shouldNotBe message2
            }
        }
        
        `when`("获取消息摘要") {
            val longContent = "这是一条很长的消息内容，" + "用于测试消息摘要功能。".repeat(10)
            val message = Message.createUserMessage(
                id = MessageId(UUID.randomUUID()),
                content = MessageContent(longContent),
                timestamp = Instant.now()
            )
            
            then("应该返回截断的摘要") {
                val summary = message.getSummary(50)
                summary.length shouldBe 50
                summary shouldBe longContent.substring(0, 50)
            }
        }
        
        `when`("获取短消息的摘要") {
            val shortContent = "短消息"
            val message = Message.createUserMessage(
                id = MessageId(UUID.randomUUID()),
                content = MessageContent(shortContent),
                timestamp = Instant.now()
            )
            
            then("应该返回完整内容") {
                val summary = message.getSummary(50)
                summary shouldBe shortContent
            }
        }
        
        `when`("检查消息是否为空") {
            val emptyMessage = Message.createUserMessage(
                id = MessageId(UUID.randomUUID()),
                content = MessageContent(""),
                timestamp = Instant.now()
            )
            
            val nonEmptyMessage = createTestMessage()
            
            then("空消息应该被识别为空") {
                emptyMessage.isEmpty() shouldBe true
                nonEmptyMessage.isEmpty() shouldBe false
            }
        }
        
        `when`("检查消息类型判断") {
            val userMessage = Message.createUserMessage(
                id = MessageId(UUID.randomUUID()),
                content = MessageContent("用户消息"),
                timestamp = Instant.now()
            )
            
            val assistantMessage = Message.createAssistantMessage(
                id = MessageId(UUID.randomUUID()),
                content = MessageContent("助手消息"),
                timestamp = Instant.now()
            )
            
            val systemMessage = Message.createSystemMessage(
                id = MessageId(UUID.randomUUID()),
                content = MessageContent("系统消息"),
                timestamp = Instant.now()
            )
            
            then("应该正确判断消息类型") {
                userMessage.isUserMessage() shouldBe true
                userMessage.isAssistantMessage() shouldBe false
                userMessage.isSystemMessage() shouldBe false
                
                assistantMessage.isUserMessage() shouldBe false
                assistantMessage.isAssistantMessage() shouldBe true
                assistantMessage.isSystemMessage() shouldBe false
                
                systemMessage.isUserMessage() shouldBe false
                systemMessage.isAssistantMessage() shouldBe false
                systemMessage.isSystemMessage() shouldBe true
            }
        }
        
        `when`("获取消息字符数") {
            val content = "这是测试消息内容"
            val message = Message.createUserMessage(
                id = MessageId(UUID.randomUUID()),
                content = MessageContent(content),
                timestamp = Instant.now()
            )
            
            then("应该返回正确的字符数") {
                message.getCharacterCount() shouldBe content.length
            }
        }
        
        `when`("检查消息是否最近创建") {
            val recentMessage = Message.createUserMessage(
                id = MessageId(UUID.randomUUID()),
                content = MessageContent("最近的消息"),
                timestamp = Instant.now()
            )
            
            val oldMessage = Message.createUserMessage(
                id = MessageId(UUID.randomUUID()),
                content = MessageContent("旧消息"),
                timestamp = Instant.now().minusSeconds(3600) // 1小时前
            )
            
            then("应该正确判断消息是否最近创建") {
                recentMessage.isRecentlyCreated(300) shouldBe true // 5分钟内
                oldMessage.isRecentlyCreated(300) shouldBe false
            }
        }
    }
})

/**
 * 创建测试用的消息
 */
private fun createTestMessage(): Message {
    return Message.createUserMessage(
        id = MessageId(UUID.randomUUID()),
        content = MessageContent("测试消息内容"),
        timestamp = Instant.now()
    )
}