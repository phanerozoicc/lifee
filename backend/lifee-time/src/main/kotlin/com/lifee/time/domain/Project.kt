package com.lifee.time.domain

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.events.*
import com.lifee.user.domain.UserId
import java.math.BigDecimal
import java.time.Instant
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 项目聚合根
 */
class Project private constructor(
    id: ProjectId,
    val name: String,
    val description: String?,
    val color: String,
    val ownerId: UserId,
    val clientName: String?,
    val hourlyRate: BigDecimal?,
    val currency: String,
    val isArchived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) : EventSourcedAggregateRoot<ProjectId>(id) {

    companion object {
        /**
         * 创建新项目
         */
        fun create(
            name: String,
            description: String? = null,
            color: String = "#3498db",
            ownerId: UserId,
            clientName: String? = null,
            hourlyRate: BigDecimal? = null,
            currency: String = "CNY"
        ): Project {
            require(name.isNotBlank()) { "项目名称不能为空" }
            require(color.matches(Regex("^#[0-9A-Fa-f]{6}$"))) { "颜色格式必须为十六进制" }
            require(hourlyRate == null || hourlyRate > BigDecimal.ZERO) { "时薪必须大于0" }
            
            val projectId = ProjectId.generate()
            val now = Instant.now()
            
            val project = Project(
                id = projectId,
                name = name,
                description = description,
                color = color,
                ownerId = ownerId,
                clientName = clientName,
                hourlyRate = hourlyRate,
                currency = currency,
                isArchived = false,
                createdAt = now,
                updatedAt = now
            )
            
            project.addEvent(
                ProjectCreatedEvent(
                    projectId = projectId,
                    name = name,
                    description = description,
                    color = color,
                    ownerId = ownerId,
                    clientName = clientName,
                    hourlyRate = hourlyRate,
                    currency = currency
                )
            )
            
            return project
        }
        
        /**
         * 从事件重建项目
         */
        @JsonCreator
        fun reconstruct(
            @JsonProperty("id") id: ProjectId,
            @JsonProperty("name") name: String,
            @JsonProperty("description") description: String?,
            @JsonProperty("color") color: String,
            @JsonProperty("ownerId") ownerId: UserId,
            @JsonProperty("clientName") clientName: String?,
            @JsonProperty("hourlyRate") hourlyRate: BigDecimal?,
            @JsonProperty("currency") currency: String,
            @JsonProperty("isArchived") isArchived: Boolean,
            @JsonProperty("createdAt") createdAt: Instant,
            @JsonProperty("updatedAt") updatedAt: Instant
        ): Project {
            return Project(
                id = id,
                name = name,
                description = description,
                color = color,
                ownerId = ownerId,
                clientName = clientName,
                hourlyRate = hourlyRate,
                currency = currency,
                isArchived = isArchived,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
    
    /**
     * 更新项目信息
     */
    fun updateInfo(
        name: String? = null,
        description: String? = null,
        color: String? = null,
        clientName: String? = null
    ): Project {
        val newName = name ?: this.name
        val newDescription = description ?: this.description
        val newColor = color ?: this.color
        val newClientName = clientName ?: this.clientName
        
        require(newName.isNotBlank()) { "项目名称不能为空" }
        if (newColor != this.color) {
            require(newColor.matches(Regex("^#[0-9A-Fa-f]{6}$"))) { "颜色格式必须为十六进制" }
        }
        
        val updated = copy(
            name = newName,
            description = newDescription,
            color = newColor,
            clientName = newClientName,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            ProjectUpdatedEvent(
                projectId = id,
                name = newName,
                description = newDescription,
                color = newColor,
                clientName = newClientName
            )
        )
        
        return updated
    }
    
    /**
     * 更新时薪设置
     */
    fun updateHourlyRate(hourlyRate: BigDecimal?, currency: String = this.currency): Project {
        require(hourlyRate == null || hourlyRate > BigDecimal.ZERO) { "时薪必须大于0" }
        
        val updated = copy(
            hourlyRate = hourlyRate,
            currency = currency,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            ProjectHourlyRateUpdatedEvent(
                projectId = id,
                hourlyRate = hourlyRate,
                currency = currency
            )
        )
        
        return updated
    }
    
    /**
     * 归档项目
     */
    fun archive(): Project {
        require(!isArchived) { "项目已经归档" }
        
        val updated = copy(
            isArchived = true,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            ProjectArchivedEvent(
                projectId = id,
                archivedAt = Instant.now()
            )
        )
        
        return updated
    }
    
    /**
     * 恢复项目
     */
    fun restore(): Project {
        require(isArchived) { "项目未归档" }
        
        val updated = copy(
            isArchived = false,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            ProjectRestoredEvent(
                projectId = id,
                restoredAt = Instant.now()
            )
        )
        
        return updated
    }
    
    /**
     * 检查用户是否有权限访问项目
     */
    fun canAccess(userId: UserId): Boolean {
        return ownerId == userId
    }
    
    private fun copy(
        name: String = this.name,
        description: String? = this.description,
        color: String = this.color,
        clientName: String? = this.clientName,
        hourlyRate: BigDecimal? = this.hourlyRate,
        currency: String = this.currency,
        isArchived: Boolean = this.isArchived,
        updatedAt: Instant = this.updatedAt
    ): Project {
        return Project(
            id = id,
            name = name,
            description = description,
            color = color,
            ownerId = ownerId,
            clientName = clientName,
            hourlyRate = hourlyRate,
            currency = currency,
            isArchived = isArchived,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    override fun applyEvent(event: DomainEvent) {
        // 项目事件应用逻辑
    }
    
    override fun serializeState(): String {
        return """
            {
                "id": "${id.value}",
                "name": "$name",
                "description": ${description?.let { "\"$it\"" } ?: "null"},
                "color": "$color",
                "ownerId": "${ownerId.value}",
                "clientName": ${clientName?.let { "\"$it\"" } ?: "null"},
                "hourlyRate": ${hourlyRate?.toString() ?: "null"},
                "currency": "$currency",
                "isArchived": $isArchived,
                "createdAt": "$createdAt",
                "updatedAt": "$updatedAt"
            }
        """.trimIndent()
    }
    
    override fun deserializeState(state: String): Project {
        // JSON反序列化逻辑
        return this
    }
}