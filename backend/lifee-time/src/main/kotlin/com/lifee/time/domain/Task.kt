package com.lifee.time.domain

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.events.*
import com.lifee.user.domain.UserId
import java.time.Instant
import java.time.LocalDate
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 任务聚合根
 */
class Task private constructor(
    id: TaskId,
    val name: String,
    val description: String?,
    val projectId: ProjectId?,
    val assigneeId: UserId?,
    val creatorId: UserId,
    val status: TaskStatus,
    val priority: TaskPriority,
    val tags: Set<String>,
    val dueDate: LocalDate?,
    val estimatedDuration: Duration?,
    val actualDuration: Duration?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant?
) : EventSourcedAggregateRoot<TaskId>(id) {

    companion object {
        /**
         * 创建新任务
         */
        fun create(
            name: String,
            description: String? = null,
            projectId: ProjectId? = null,
            assigneeId: UserId? = null,
            creatorId: UserId,
            priority: TaskPriority = TaskPriority.NORMAL,
            tags: Set<String> = emptySet(),
            dueDate: LocalDate? = null,
            estimatedDuration: Duration? = null
        ): Task {
            require(name.isNotBlank()) { "任务名称不能为空" }
            require(tags.all { it.isNotBlank() }) { "标签不能为空" }
            
            val taskId = TaskId.generate()
            val now = Instant.now()
            
            val task = Task(
                id = taskId,
                name = name,
                description = description,
                projectId = projectId,
                assigneeId = assigneeId,
                creatorId = creatorId,
                status = TaskStatus.TODO,
                priority = priority,
                tags = tags,
                dueDate = dueDate,
                estimatedDuration = estimatedDuration,
                actualDuration = null,
                createdAt = now,
                updatedAt = now,
                completedAt = null
            )
            
            task.addEvent(
                TaskCreatedEvent(
                    taskId = taskId,
                    name = name,
                    description = description,
                    projectId = projectId,
                    assigneeId = assigneeId,
                    creatorId = creatorId,
                    priority = priority,
                    tags = tags,
                    dueDate = dueDate,
                    estimatedDuration = estimatedDuration
                )
            )
            
            return task
        }
        
        /**
         * 从事件重建任务
         */
        @JsonCreator
        fun reconstruct(
            @JsonProperty("id") id: TaskId,
            @JsonProperty("name") name: String,
            @JsonProperty("description") description: String?,
            @JsonProperty("projectId") projectId: ProjectId?,
            @JsonProperty("assigneeId") assigneeId: UserId?,
            @JsonProperty("creatorId") creatorId: UserId,
            @JsonProperty("status") status: TaskStatus,
            @JsonProperty("priority") priority: TaskPriority,
            @JsonProperty("tags") tags: Set<String>,
            @JsonProperty("dueDate") dueDate: LocalDate?,
            @JsonProperty("estimatedDuration") estimatedDuration: Duration?,
            @JsonProperty("actualDuration") actualDuration: Duration?,
            @JsonProperty("createdAt") createdAt: Instant,
            @JsonProperty("updatedAt") updatedAt: Instant,
            @JsonProperty("completedAt") completedAt: Instant?
        ): Task {
            return Task(
                id = id,
                name = name,
                description = description,
                projectId = projectId,
                assigneeId = assigneeId,
                creatorId = creatorId,
                status = status,
                priority = priority,
                tags = tags,
                dueDate = dueDate,
                estimatedDuration = estimatedDuration,
                actualDuration = actualDuration,
                createdAt = createdAt,
                updatedAt = updatedAt,
                completedAt = completedAt
            )
        }
    }
    
    /**
     * 开始任务
     */
    fun start(): Task {
        require(status.canStart()) { "任务当前状态不允许开始: $status" }
        
        val updated = copy(
            status = TaskStatus.IN_PROGRESS,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            TaskStartedEvent(
                taskId = id,
                startedAt = Instant.now()
            )
        )
        
        return updated
    }
    
    /**
     * 完成任务
     */
    fun complete(actualDuration: Duration? = null): Task {
        require(status.canComplete()) { "任务当前状态不允许完成: $status" }
        
        val now = Instant.now()
        val updated = copy(
            status = TaskStatus.COMPLETED,
            actualDuration = actualDuration ?: this.actualDuration,
            updatedAt = now,
            completedAt = now
        )
        
        updated.addEvent(
            TaskCompletedEvent(
                taskId = id,
                completedAt = now,
                actualDuration = actualDuration
            )
        )
        
        return updated
    }
    
    /**
     * 取消任务
     */
    fun cancel(): Task {
        require(status.canCancel()) { "任务当前状态不允许取消: $status" }
        
        val updated = copy(
            status = TaskStatus.CANCELLED,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            TaskCancelledEvent(
                taskId = id,
                cancelledAt = Instant.now()
            )
        )
        
        return updated
    }
    
    /**
     * 重新开始任务
     */
    fun restart(): Task {
        require(status.canRestart()) { "任务当前状态不允许重新开始: $status" }
        
        val updated = copy(
            status = TaskStatus.TODO,
            actualDuration = null,
            updatedAt = Instant.now(),
            completedAt = null
        )
        
        updated.addEvent(
            TaskRestartedEvent(
                taskId = id,
                restartedAt = Instant.now()
            )
        )
        
        return updated
    }
    
    /**
     * 更新任务信息
     */
    fun updateInfo(
        name: String? = null,
        description: String? = null,
        priority: TaskPriority? = null,
        dueDate: LocalDate? = null,
        estimatedDuration: Duration? = null
    ): Task {
        val newName = name ?: this.name
        val newDescription = description ?: this.description
        val newPriority = priority ?: this.priority
        
        require(newName.isNotBlank()) { "任务名称不能为空" }
        
        val updated = copy(
            name = newName,
            description = newDescription,
            priority = newPriority,
            dueDate = dueDate,
            estimatedDuration = estimatedDuration ?: this.estimatedDuration,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            TaskUpdatedEvent(
                taskId = id,
                name = newName,
                description = newDescription,
                priority = newPriority,
                dueDate = dueDate,
                estimatedDuration = estimatedDuration
            )
        )
        
        return updated
    }
    
    /**
     * 分配任务
     */
    fun assignTo(assigneeId: UserId): Task {
        val updated = copy(
            assigneeId = assigneeId,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            TaskAssignedEvent(
                taskId = id,
                assigneeId = assigneeId,
                assignedAt = Instant.now()
            )
        )
        
        return updated
    }
    
    /**
     * 取消分配
     */
    fun unassign(): Task {
        require(assigneeId != null) { "任务未分配给任何人" }
        
        val updated = copy(
            assigneeId = null,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            TaskUnassignedEvent(
                taskId = id,
                previousAssigneeId = assigneeId!!,
                unassignedAt = Instant.now()
            )
        )
        
        return updated
    }
    
    /**
     * 更新标签
     */
    fun updateTags(tags: Set<String>): Task {
        require(tags.all { it.isNotBlank() }) { "标签不能为空" }
        
        val updated = copy(
            tags = tags,
            updatedAt = Instant.now()
        )
        
        updated.addEvent(
            TaskTagsUpdatedEvent(
                taskId = id,
                tags = tags
            )
        )
        
        return updated
    }
    
    /**
     * 检查任务是否逾期
     */
    fun isOverdue(): Boolean {
        return dueDate != null && LocalDate.now().isAfter(dueDate) && !status.isFinished()
    }
    
    /**
     * 检查用户是否有权限访问任务
     */
    fun canAccess(userId: UserId): Boolean {
        return creatorId == userId || assigneeId == userId
    }
    
    /**
     * 检查用户是否可以编辑任务
     */
    fun canEdit(userId: UserId): Boolean {
        return creatorId == userId
    }
    
    private fun copy(
        name: String = this.name,
        description: String? = this.description,
        projectId: ProjectId? = this.projectId,
        assigneeId: UserId? = this.assigneeId,
        status: TaskStatus = this.status,
        priority: TaskPriority = this.priority,
        tags: Set<String> = this.tags,
        dueDate: LocalDate? = this.dueDate,
        estimatedDuration: Duration? = this.estimatedDuration,
        actualDuration: Duration? = this.actualDuration,
        updatedAt: Instant = this.updatedAt,
        completedAt: Instant? = this.completedAt
    ): Task {
        return Task(
            id = id,
            name = name,
            description = description,
            projectId = projectId,
            assigneeId = assigneeId,
            creatorId = creatorId,
            status = status,
            priority = priority,
            tags = tags,
            dueDate = dueDate,
            estimatedDuration = estimatedDuration,
            actualDuration = actualDuration,
            createdAt = createdAt,
            updatedAt = updatedAt,
            completedAt = completedAt
        )
    }
    
    override fun applyEvent(event: DomainEvent) {
        // 任务事件应用逻辑
    }
    
    override fun serializeState(): String {
        return """
            {
                "id": "${id.value}",
                "name": "$name",
                "description": ${description?.let { "\"$it\"" } ?: "null"},
                "projectId": ${projectId?.let { "\"${it.value}\"" } ?: "null"},
                "assigneeId": ${assigneeId?.let { "\"${it.value}\"" } ?: "null"},
                "creatorId": "${creatorId.value}",
                "status": "$status",
                "priority": "$priority",
                "tags": [${tags.joinToString(",") { "\"$it\"" }}],
                "dueDate": ${dueDate?.let { "\"$it\"" } ?: "null"},
                "estimatedDuration": ${estimatedDuration?.let { "\"${it.toSeconds()}\"" } ?: "null"},
                "actualDuration": ${actualDuration?.let { "\"${it.toSeconds()}\"" } ?: "null"},
                "createdAt": "$createdAt",
                "updatedAt": "$updatedAt",
                "completedAt": ${completedAt?.let { "\"$it\"" } ?: "null"}
            }
        """.trimIndent()
    }
    
    override fun deserializeState(state: String): Task {
        // JSON反序列化逻辑
        return this
    }
}