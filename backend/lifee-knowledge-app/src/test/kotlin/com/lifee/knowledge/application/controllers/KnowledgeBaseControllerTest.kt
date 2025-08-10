package com.lifee.knowledge.application.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.knowledge.application.dto.KnowledgeBaseDto
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.util.*

class KnowledgeBaseControllerTest : BehaviorSpec({
    
    given("知识库控制器") {
        val commandBus = mockk<CommandBus>()
        val queryBus = mockk<QueryBus>()
        val controller = KnowledgeBaseController(commandBus, queryBus)
        val mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
        val objectMapper = ObjectMapper()
        
        `when`("创建知识库") {
            val request = CreateKnowledgeBaseRequest(
                name = "测试知识库",
                description = "这是一个测试知识库"
            )
            val userId = UUID.randomUUID().toString()
            
            every { commandBus.send(any()) } returns Unit
            
            val result = mockMvc.perform(
                post("/api/v1/knowledge-bases")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", userId)
                    .content(objectMapper.writeValueAsString(request))
            )
            
            then("应该返回201状态码") {
                result.andExpect(status().isCreated)
                    .andExpect(jsonPath("$.knowledgeBaseId").exists())
            }
            
            then("应该调用命令总线") {
                verify { commandBus.send(any()) }
            }
        }
        
        `when`("获取知识库详情") {
            val knowledgeBaseId = UUID.randomUUID().toString()
            val userId = UUID.randomUUID().toString()
            
            val knowledgeBaseDto = KnowledgeBaseDto(
                id = knowledgeBaseId,
                name = "测试知识库",
                description = "这是一个测试知识库",
                ownerId = userId,
                documentCount = 0,
                totalSize = 0,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            
            every { queryBus.send(any()) } returns knowledgeBaseDto
            
            val result = mockMvc.perform(
                get("/api/v1/knowledge-bases/{knowledgeBaseId}", knowledgeBaseId)
                    .header("X-User-Id", userId)
            )
            
            then("应该返回200状态码和知识库信息") {
                result.andExpect(status().isOk)
                    .andExpect(jsonPath("$.id").value(knowledgeBaseId))
                    .andExpect(jsonPath("$.name").value("测试知识库"))
                    .andExpect(jsonPath("$.description").value("这是一个测试知识库"))
                    .andExpect(jsonPath("$.ownerId").value(userId))
                    .andExpect(jsonPath("$.documentCount").value(0))
                    .andExpect(jsonPath("$.totalSize").value(0))
            }
            
            then("应该调用查询总线") {
                verify { queryBus.send(any()) }
            }
        }
        
        `when`("获取用户知识库列表") {
            val userId = UUID.randomUUID().toString()
            
            val knowledgeBaseDtos = listOf(
                KnowledgeBaseDto(
                    id = UUID.randomUUID().toString(),
                    name = "知识库1",
                    description = "描述1",
                    ownerId = userId,
                    documentCount = 5,
                    totalSize = 1000,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                ),
                KnowledgeBaseDto(
                    id = UUID.randomUUID().toString(),
                    name = "知识库2",
                    description = "描述2",
                    ownerId = userId,
                    documentCount = 3,
                    totalSize = 500,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
            )
            
            every { queryBus.send(any()) } returns knowledgeBaseDtos
            
            val result = mockMvc.perform(
                get("/api/v1/knowledge-bases")
                    .header("X-User-Id", userId)
                    .param("offset", "0")
                    .param("limit", "20")
            )
            
            then("应该返回200状态码和知识库列表") {
                result.andExpect(status().isOk)
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("知识库1"))
                    .andExpect(jsonPath("$[1].name").value("知识库2"))
            }
            
            then("应该调用查询总线") {
                verify { queryBus.send(any()) }
            }
        }
        
        `when`("删除知识库") {
            val knowledgeBaseId = UUID.randomUUID().toString()
            val userId = UUID.randomUUID().toString()
            
            every { commandBus.send(any()) } returns Unit
            
            val result = mockMvc.perform(
                delete("/api/v1/knowledge-bases/{knowledgeBaseId}", knowledgeBaseId)
                    .header("X-User-Id", userId)
            )
            
            then("应该返回204状态码") {
                result.andExpect(status().isNoContent)
            }
            
            then("应该调用命令总线") {
                verify { commandBus.send(any()) }
            }
        }
    }
})