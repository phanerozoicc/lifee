package com.lifee.knowledge.application.commands

import com.lifee.common.cqrs.commands.Command
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 添加文档命令
 */
data class AddDocumentCommand(
    @field:NotBlank(message = "Knowledge base ID cannot be blank")
    val knowledgeBaseId: String,
    
    @field:NotBlank(message = "Document ID cannot be blank")
    val documentId: String,
    
    @field:NotBlank(message = "Document title cannot be blank")
    @field:Size(min = 1, max = 200, message = "Document title must be between 1 and 200 characters")
    val title: String,
    
    @field:NotBlank(message = "Document content cannot be blank")
    @field:Size(max = 1_000_000, message = "Document content cannot exceed 1,000,000 characters")
    val content: String,
    
    @field:NotBlank(message = "Document type cannot be blank")
    val type: String,
    
    @field:NotBlank(message = "User ID cannot be blank")
    val userId: String
) : Command