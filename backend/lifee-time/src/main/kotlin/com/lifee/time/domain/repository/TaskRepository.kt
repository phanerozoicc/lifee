package com.lifee.time.domain.repository

import com.lifee.time.domain.Task
import com.lifee.time.domain.TaskId
import com.lifee.time.domain.ProjectId
import com.lifee.time.domain.TaskStatus
import com.lifee.time.domain.TaskPriority
import com.lifee.user.domain.UserId
import java.time.LocalDate
import java.util.*

/**
 * 任务仓储接口
 */
interface TaskRepository {
    
    /**
     * 保存任务
     */
    suspend fun save(task: Task)
    
    /**
     * 根据ID查找任务
     */
    suspend fun findById(taskId: TaskId): Task?
    
    /**
     * 根据项目ID查找任务列表
     */
    suspend fun findByProjectId(projectId: ProjectId, limit: Int = 50, offset: Int = 0): List<Task>
    
    /**
     * 根据分配人ID查找任务列表
     */
    suspend fun findByAssigneeId(assigneeId: UserId, limit: Int = 50, offset: Int = 0): List<Task>
    
    /**
     * 根据创建人ID查找任务列表
     */
    suspend fun findByCreatorId(creatorId: UserId, limit: Int = 50, offset: Int = 0): List<Task>
    
    /**
     * 根据状态查找任务
     */
    suspend fun findByStatus(
        status: TaskStatus,
        userId: UserId? = null,
        projectId: ProjectId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Task>
    
    /**
     * 根据优先级查找任务
     */
    suspend fun findByPriority(
        priority: TaskPriority,
        userId: UserId? = null,
        projectId: ProjectId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Task>
    
    /**
     * 根据截止日期查找任务
     */
    suspend fun findByDueDate(
        dueDate: LocalDate,
        userId: UserId? = null,
        projectId: ProjectId? = null
    ): List<Task>
    
    /**
     * 查找逾期任务
     */
    suspend fun findOverdueTasks(
        userId: UserId? = null,
        projectId: ProjectId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Task>
    
    /**
     * 查找即将到期的任务（指定天数内）
     */
    suspend fun findTasksDueSoon(
        days: Int = 7,
        userId: UserId? = null,
        projectId: ProjectId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Task>
    
    /**
     * 根据标签查找任务
     */
    suspend fun findByTags(
        tags: Set<String>,
        userId: UserId? = null,
        projectId: ProjectId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Task>
    
    /**
     * 根据名称搜索任务
     */
    suspend fun findByNameContaining(
        name: String,
        userId: UserId? = null,
        projectId: ProjectId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Task>
    
    /**
     * 查找用户的活跃任务
     */
    suspend fun findActiveTasksByUserId(
        userId: UserId,
        limit: Int = 50,
        offset: Int = 0
    ): List<Task>
    
    /**
     * 查找项目的活跃任务
     */
    suspend fun findActiveTasksByProjectId(
        projectId: ProjectId,
        limit: Int = 50,
        offset: Int = 0
    ): List<Task>
    
    /**
     * 统计项目的任务数量
     */
    suspend fun countByProjectId(projectId: ProjectId): Long
    
    /**
     * 统计用户分配的任务数量
     */
    suspend fun countByAssigneeId(assigneeId: UserId): Long
    
    /**
     * 统计用户创建的任务数量
     */
    suspend fun countByCreatorId(creatorId: UserId): Long
    
    /**
     * 统计指定状态的任务数量
     */
    suspend fun countByStatus(
        status: TaskStatus,
        userId: UserId? = null,
        projectId: ProjectId? = null
    ): Long
    
    /**
     * 统计逾期任务数量
     */
    suspend fun countOverdueTasks(
        userId: UserId? = null,
        projectId: ProjectId? = null
    ): Long
    
    /**
     * 删除任务
     */
    suspend fun delete(taskId: TaskId)
    
    /**
     * 检查任务是否存在
     */
    suspend fun exists(taskId: TaskId): Boolean
}