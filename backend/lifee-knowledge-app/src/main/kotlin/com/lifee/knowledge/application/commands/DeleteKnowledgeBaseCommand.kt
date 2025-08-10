package com.lifee.knowledge.application.commands

import com.lifee.common.cqrs.commands.Command
import jakarta.validation.constraints.NotBlank

/**
 * 删除知识库命令
 */
data class DeleteKnowledgeBaseCommand(
    @field:NotBlank(message = "Knowledge base ID cannot be blank")
    val knowledgeBaseId: String,
    
    @field:NotBlank(message = "User ID cannot be blank")
    val userId: String
) : Command