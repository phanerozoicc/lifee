package com.lifee.chat.domain.aggregates

import com.lifee.chat.domain.entities.Message
import com.lifee.chat.domain.valueobjects.*
import com.lifee.chat.domain.events.*
import com.lifee.chat.domain.exceptions.InvalidMessageContentException
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.assertions.throwables.shouldThrow
import java.time.Instant
import java.util.UUID

/**
 * 对话聚合根测试
 */
class ConversationTest : BehaviorSpec({
    
    given("对话聚合根") {
        
        `when`("创建新对话") {
            val conversationId = ConversationId(UUID.randomUUID())
            val title = ConversationTitle("测试对话")
            val userId = "user-123"
            val knowledgeBaseId = "kb-456"
            
            val conversation = Conversation.create(
                id = conversationId,
                title = title,
                userId = userId,
                knowledgeBaseId = knowledgeBaseId
            )
            
            then("应该正确初始化对话") {
                conversation.getId() shouldBe conversationId
                conversation.getTitle() shouldBe title
                conversation.getUserId() shouldBe userId
                conversation.getKnowledgeBaseId() shouldBe knowledgeBaseId
                conversation.getMessages().size shouldBe 0
                conversation.getCreatedAt() shouldNotBe null
                conversation.getUpdatedAt() shouldNotBe null
            }
            
            then("应该发布对话创建事件") {
                val events = conversation.getUncommittedEvents()
                events.size shouldBe 1
                events[0] shouldBe ConversationCreatedEvent::class
            }
        }
        
        `when`("向对话添加用户消息") {
            val conversation = createTestConversation()
            val messageContent = MessageContent("这是用户的消息")
            
            conversation.addUserMessage(messageContent)
            
            then("应该成功添加消息") {
                conversation.getMessages().size shouldBe 1
                val message = conversation.getMessages()[0]
                message.getContent() shouldBe messageContent
                message.getType() shouldBe MessageType.USER
            }
            
            then("应该发布消息添加事件") {
                val events = conversation.getUncommittedEvents()
                // 第一个事件是创建事件，第二个是添加消息事件
                events.size shouldBe 2
                events[1] shouldBe MessageAddedEvent::class
            }
        }
        
        `when`("向对话添加助手回复") {
            val conversation = createTestConversation()
            val userMessage = MessageContent("用户问题")
            val assistantReply = MessageContent("助手回复")
            
            conversation.addUserMessage(userMessage)
            conversation.addAssistantMessage(assistantReply)
            
            then("应该成功添加助手回复") {
                conversation.getMessages().size shouldBe 2
                val assistantMessage = conversation.getMessages()[1]
                assistantMessage.getContent() shouldBe assistantReply
                assistantMessage.getType() shouldBe MessageType.ASSISTANT
            }
            
            then("应该发布响应生成事件") {
                val events = conversation.getUncommittedEvents()
                // 创建、用户消息、助手消息三个事件
                events.size shouldBe 3
                events[2] shouldBe ResponseGeneratedEvent::class
            }
        }
        
        `when`("添加空消息内容") {
            val conversation = createTestConversation()
            
            then("应该抛出异常") {
                shouldThrow<InvalidMessageContentException> {
                    conversation.addUserMessage(MessageContent(""))
                }
            }
        }
        
        `when`("更新对话标题") {
            val conversation = createTestConversation()
            val newTitle = ConversationTitle("更新后的标题")
            
            conversation.updateTitle(newTitle)
            
            then("应该成功更新标题") {
                conversation.getTitle() shouldBe newTitle
            }
            
            then("应该发布标题更新事件") {
                val events = conversation.getUncommittedEvents()
                events.size shouldBe 2
                events[1] shouldBe ConversationTitleUpdatedEvent::class
            }
        }
        
        `when`("删除消息") {
            val conversation = createTestConversation()
            val messageContent = MessageContent("要删除的消息")
            
            conversation.addUserMessage(messageContent)
            val messageId = conversation.getMessages()[0].getId()
            
            conversation.deleteMessage(messageId)
            
            then("应该成功删除消息") {
                conversation.getMessages().size shouldBe 0
            }
            
            then("应该发布消息删除事件") {
                val events = conversation.getUncommittedEvents()
                // 创建、添加、删除三个事件
                events.size shouldBe 3
                events[2] shouldBe MessageDeletedEvent::class
            }
        }
        
        `when`("删除不存在的消息") {
            val conversation = createTestConversation()
            val nonExistentMessageId = MessageId(UUID.randomUUID())
            
            then("应该抛出异常") {
                shouldThrow<IllegalArgumentException> {
                    conversation.deleteMessage(nonExistentMessageId)
                }
            }
        }
        
        `when`("更新消息内容") {
            val conversation = createTestConversation()
            val originalContent = MessageContent("原始消息")
            val updatedContent = MessageContent("更新后的消息")
            
            conversation.addUserMessage(originalContent)
            val messageId = conversation.getMessages()[0].getId()
            
            conversation.updateMessage(messageId, updatedContent)
            
            then("应该成功更新消息") {
                val message = conversation.getMessages()[0]
                message.getContent() shouldBe updatedContent
            }
            
            then("应该发布消息更新事件") {
                val events = conversation.getUncommittedEvents()
                // 创建、添加、更新三个事件
                events.size shouldBe 3
                events[2] shouldBe MessageUpdatedEvent::class
            }
        }
        
        `when`("获取最后一条消息") {
            val conversation = createTestConversation()
            
            then("空对话应该返回null") {
                conversation.getLastMessage() shouldBe null
            }
            
            conversation.addUserMessage(MessageContent("第一条消息"))
            conversation.addAssistantMessage(MessageContent("第二条消息"))
            
            then("应该返回最后一条消息") {
                val lastMessage = conversation.getLastMessage()
                lastMessage shouldNotBe null
                lastMessage!!.getContent().value shouldBe "第二条消息"
                lastMessage.getType() shouldBe MessageType.ASSISTANT
            }
        }
        
        `when`("获取消息数量") {
            val conversation = createTestConversation()
            
            then("初始消息数量应该为0") {
                conversation.getMessageCount() shouldBe 0
            }
            
            conversation.addUserMessage(MessageContent("消息1"))
            conversation.addAssistantMessage(MessageContent("消息2"))
            
            then("添加消息后数量应该正确") {
                conversation.getMessageCount() shouldBe 2
            }
        }
        
        `when`("检查对话是否为空") {
            val conversation = createTestConversation()
            
            then("新对话应该为空") {
                conversation.isEmpty() shouldBe true
            }
            
            conversation.addUserMessage(MessageContent("消息"))
            
            then("有消息的对话不应该为空") {
                conversation.isEmpty() shouldBe false
            }
        }
    }
})

/**
 * 创建测试用的对话
 */
private fun createTestConversation(): Conversation {
    return Conversation.create(
        id = ConversationId(UUID.randomUUID()),
        title = ConversationTitle("测试对话"),
        userId = "user-123",
        knowledgeBaseId = "kb-456"
    )
}