package com.lifee.knowledge.application.commands.handlers

import com.lifee.knowledge.application.commands.CreateKnowledgeBaseCommand
import com.lifee.knowledge.domain.exceptions.DuplicateKnowledgeBaseNameException
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.UserId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.*

class CreateKnowledgeBaseCommandHandlerTest : BehaviorSpec({
    
    given("创建知识库命令处理器") {
        val repository = mockk<KnowledgeBaseRepository>()
        val handler = CreateKnowledgeBaseCommandHandler(repository)
        
        `when`("处理创建知识库命令") {
            val command = CreateKnowledgeBaseCommand(
                knowledgeBaseId = UUID.randomUUID().toString(),
                name = "测试知识库",
                description = "这是一个测试知识库",
                ownerId = UUID.randomUUID().toString()
            )
            
            val knowledgeBaseSlot = slot<com.lifee.knowledge.domain.aggregates.KnowledgeBase>()
            
            every { 
                repository.existsByOwnerIdAndName(any(), any()) 
            } returns false
            
            every { 
                repository.save(capture(knowledgeBaseSlot)) 
            } answers { knowledgeBaseSlot.captured }
            
            handler.handle(command)
            
            then("应该检查重复名称") {
                verify { 
                    repository.existsByOwnerIdAndName(
                        UserId.fromString(command.ownerId),
                        command.name
                    )
                }
            }
            
            then("应该保存知识库") {
                verify { repository.save(any()) }
                
                val savedKnowledgeBase = knowledgeBaseSlot.captured
                savedKnowledgeBase.getId() shouldBe KnowledgeBaseId.fromString(command.knowledgeBaseId)
                savedKnowledgeBase.getName().value shouldBe command.name
                savedKnowledgeBase.getDescription().value shouldBe command.description
                savedKnowledgeBase.getOwnerId() shouldBe UserId.fromString(command.ownerId)
                savedKnowledgeBase.getCreatedAt() shouldNotBe null
                savedKnowledgeBase.getUpdatedAt() shouldNotBe null
            }
        }
        
        `when`("知识库名称已存在") {
            val command = CreateKnowledgeBaseCommand(
                knowledgeBaseId = UUID.randomUUID().toString(),
                name = "已存在的知识库",
                description = "描述",
                ownerId = UUID.randomUUID().toString()
            )
            
            every { 
                repository.existsByOwnerIdAndName(any(), any()) 
            } returns true
            
            then("应该抛出重复名称异常") {
                shouldThrow<DuplicateKnowledgeBaseNameException> {
                    handler.handle(command)
                }
            }
        }
    }
})