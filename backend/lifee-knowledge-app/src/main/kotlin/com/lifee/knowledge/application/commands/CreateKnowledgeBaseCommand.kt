package com.lifee.knowledge.application.commands

import com.lifee.common.cqrs.commands.Command
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 创建知识库命令
 */
data class CreateKnowledgeBaseCommand(
    @field:NotBlank(message = "Knowledge base ID cannot be blank")
    val knowledgeBaseId: String,
    
    @field:NotBlank(message = "Knowledge base name cannot be blank")
    @field:Size(min = 2, max = 100, message = "Knowledge base name must be between 2 and 100 characters")
    val name: String,
    
    @field:Size(max = 500, message = "Description cannot exceed 500 characters")
    val description: String = "",
    
    @field:NotBlank(message = "Owner ID cannot be blank")
    val ownerId: String
) : Command