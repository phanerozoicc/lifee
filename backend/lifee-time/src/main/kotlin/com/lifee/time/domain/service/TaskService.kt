package com.lifee.time.domain.service

import com.lifee.time.domain.*
import com.lifee.time.domain.repository.TaskRepository
import com.lifee.time.domain.repository.ProjectRepository
import com.lifee.user.domain.UserId
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * 任务管理领域服务
 */
class TaskService(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository
) {
    
    /**
     * 创建任务
     */
    suspend fun createTask(
        name: String,
        description: String?,
        projectId: ProjectId,
        creatorId: UserId,
        assigneeId: UserId? = null,
        priority: TaskPriority = TaskPriority.NORMAL,
        tags: Set<String> = emptySet(),
        dueDate: Instant? = null,
        estimatedDuration: Duration? = null
    ): Task {
        // 验证项目存在且用户有权限
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!project.canAccess(creatorId)) {
            throw IllegalArgumentException("用户无权在此项目创建任务")
        }
        
        // 验证被分配人权限
        if (assigneeId != null && !project.canAccess(assigneeId)) {
            throw IllegalArgumentException("被分配人无权访问此项目")
        }
        
        // 创建任务
        val task = Task.create(
            name = name,
            description = description,
            projectId = projectId,
            creatorId = creatorId,
            assigneeId = assigneeId,
            priority = priority,
            tags = tags,
            dueDate = dueDate?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate(),
            estimatedDuration = estimatedDuration
        )
        
        taskRepository.save(task)
        
        return task
    }
    
    /**
     * 更新任务信息
     */
    suspend fun updateTask(
        taskId: TaskId,
        userId: UserId,
        name: String? = null,
        description: String? = null,
        priority: TaskPriority? = null,
        dueDate: Instant? = null,
        estimatedDuration: Duration? = null
    ): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("任务不存在: $taskId")
        
        if (!task.canEdit(userId)) {
            throw IllegalArgumentException("用户无权编辑此任务")
        }
        
        task.updateInfo(
            name = name ?: task.name,
            description = description ?: task.description,
            priority = priority ?: task.priority,
            dueDate = dueDate?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate() ?: task.dueDate,
            estimatedDuration = estimatedDuration ?: task.estimatedDuration
        )
        
        taskRepository.save(task)
        
        return task
    }
    
    /**
     * 分配任务
     */
    suspend fun assignTask(
        taskId: TaskId,
        userId: UserId,
        assigneeId: UserId
    ): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("任务不存在: $taskId")
        
        if (!task.canEdit(userId)) {
            throw IllegalArgumentException("用户无权分配此任务")
        }
        
        // 验证被分配人权限
        val project = projectRepository.findById(task.projectId!!)
            ?: throw IllegalArgumentException("项目不存在: ${task.projectId}")
        
        if (!project.canAccess(assigneeId)) {
            throw IllegalArgumentException("被分配人无权访问此项目")
        }
        
        task.assignTo(assigneeId)
        taskRepository.save(task)
        
        return task
    }
    
    /**
     * 取消分配任务
     */
    suspend fun unassignTask(taskId: TaskId, userId: UserId): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("任务不存在: $taskId")
        
        if (!task.canEdit(userId)) {
            throw IllegalArgumentException("用户无权取消分配此任务")
        }
        
        task.unassign()
        taskRepository.save(task)
        
        return task
    }
    
    /**
     * 开始任务
     */
    suspend fun startTask(taskId: TaskId, userId: UserId): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("任务不存在: $taskId")
        
        if (!task.canAccess(userId)) {
            throw IllegalArgumentException("用户无权访问此任务")
        }
        
        task.start()
        taskRepository.save(task)
        
        return task
    }
    
    /**
     * 完成任务
     */
    suspend fun completeTask(
        taskId: TaskId,
        userId: UserId,
        actualDuration: Duration? = null
    ): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("任务不存在: $taskId")
        
        if (!task.canAccess(userId)) {
            throw IllegalArgumentException("用户无权访问此任务")
        }
        
        task.complete(actualDuration)
        taskRepository.save(task)
        
        return task
    }
    
    /**
     * 取消任务
     */
    suspend fun cancelTask(taskId: TaskId, userId: UserId): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("任务不存在: $taskId")
        
        if (!task.canEdit(userId)) {
            throw IllegalArgumentException("用户无权取消此任务")
        }
        
        task.cancel()
        taskRepository.save(task)
        
        return task
    }
    
    /**
     * 重新开始任务
     */
    suspend fun restartTask(taskId: TaskId, userId: UserId): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("任务不存在: $taskId")
        
        if (!task.canEdit(userId)) {
            throw IllegalArgumentException("用户无权重新开始此任务")
        }
        
        task.restart()
        taskRepository.save(task)
        
        return task
    }
    
    /**
     * 更新任务标签
     */
    suspend fun updateTaskTags(
        taskId: TaskId,
        userId: UserId,
        tags: Set<String>
    ): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("任务不存在: $taskId")
        
        if (!task.canEdit(userId)) {
            throw IllegalArgumentException("用户无权编辑此任务")
        }
        
        task.updateTags(tags)
        taskRepository.save(task)
        
        return task
    }
    
    /**
     * 删除任务
     */
    suspend fun deleteTask(taskId: TaskId, userId: UserId) {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("任务不存在: $taskId")
        
        if (!task.canEdit(userId)) {
            throw IllegalArgumentException("用户无权删除此任务")
        }
        
        taskRepository.delete(taskId)
    }
    
    /**
     * 获取用户的任务列表
     */
    suspend fun getUserTasks(
        userId: UserId,
        status: TaskStatus? = null,
        priority: TaskPriority? = null,
        projectId: ProjectId? = null
    ): List<Task> {
        return when {
            status != null && projectId != null -> taskRepository.findByStatus(status, userId, projectId)
            status != null -> taskRepository.findByStatus(status, userId)
            priority != null -> taskRepository.findByPriority(priority, userId, projectId)
            projectId != null -> taskRepository.findByProjectId(projectId).filter { it.assigneeId == userId }
            else -> taskRepository.findByAssigneeId(userId)
        }
    }
    
    /**
     * 获取项目的任务列表
     */
    suspend fun getProjectTasks(
        projectId: ProjectId,
        userId: UserId,
        status: TaskStatus? = null,
        assigneeId: UserId? = null
    ): List<Task> {
        // 验证用户权限
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!project.canAccess(userId)) {
            throw IllegalArgumentException("用户无权访问此项目")
        }
        
        return when {
            status != null && assigneeId != null -> taskRepository.findByStatus(status, assigneeId, projectId)
            status != null -> taskRepository.findByStatus(status, null, projectId)
            assigneeId != null -> taskRepository.findByProjectId(projectId).filter { it.assigneeId == assigneeId }
            else -> taskRepository.findByProjectId(projectId)
        }
    }
    
    /**
     * 获取逾期任务
     */
    suspend fun getOverdueTasks(userId: UserId): List<Task> {
        return taskRepository.findOverdueTasks(userId)
    }
    
    /**
     * 获取即将到期的任务
     */
    suspend fun getTasksDueSoon(
        userId: UserId,
        daysAhead: Long = 3
    ): List<Task> {
        val deadline = Instant.now().plus(daysAhead, ChronoUnit.DAYS)
        return taskRepository.findTasksDueSoon(daysAhead.toInt(), userId)
    }
    
    /**
     * 搜索任务
     */
    suspend fun searchTasks(
        userId: UserId,
        keyword: String,
        projectId: ProjectId? = null
    ): List<Task> {
        val tasks = if (projectId != null) {
            getProjectTasks(projectId, userId)
        } else {
            // 获取用户可访问的所有任务
            val accessibleProjects = projectRepository.findAccessibleByUserId(userId)
            accessibleProjects.flatMap { project ->
                taskRepository.findByProjectId(project.id)
            }.filter { task -> task.canAccess(userId) }
        }
        
        return tasks.filter { task ->
            task.name.contains(keyword, ignoreCase = true) ||
            task.description?.contains(keyword, ignoreCase = true) == true ||
            task.tags.any { it.contains(keyword, ignoreCase = true) }
        }
    }
    
    /**
     * 获取任务统计信息
     */
    suspend fun getTaskStatistics(
        userId: UserId,
        projectId: ProjectId? = null
    ): SimpleTaskStatistics {
        val tasks = if (projectId != null) {
            getProjectTasks(projectId, userId)
        } else {
            getUserTasks(userId)
        }
        
        val totalCount = tasks.size
        val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
        val inProgressCount = tasks.count { it.status == TaskStatus.IN_PROGRESS }
        val overdueCount = tasks.count { it.isOverdue() }
        val highPriorityCount = tasks.count { it.priority.isHighPriority() }
        
        // 注意：这里返回的是简化的统计信息，不是StatisticsService中的TaskStatistics
        // 如果需要详细的任务统计，应该使用StatisticsService
        return SimpleTaskStatistics(
            totalCount = totalCount,
            completedCount = completedCount,
            inProgressCount = inProgressCount,
            overdueCount = overdueCount,
            highPriorityCount = highPriorityCount,
            completionRate = if (totalCount > 0) completedCount.toDouble() / totalCount else 0.0
        )
    }
    
    /**
     * 批量更新任务状态
     */
    suspend fun batchUpdateTaskStatus(
        taskIds: List<TaskId>,
        userId: UserId,
        status: TaskStatus
    ): List<Task> {
        val tasks = taskIds.mapNotNull { taskRepository.findById(it) }
        
        // 验证权限
        tasks.forEach { task ->
            if (!task.canEdit(userId)) {
                throw IllegalArgumentException("用户无权编辑任务: ${task.id}")
            }
        }
        
        // 更新状态
        val updatedTasks = tasks.map { task ->
            when (status) {
                TaskStatus.IN_PROGRESS -> task.start()
                TaskStatus.COMPLETED -> task.complete()
                TaskStatus.CANCELLED -> task.cancel()
                else -> throw IllegalArgumentException("不支持的状态更新: $status")
            }
            taskRepository.save(task)
            task
        }
        
        return updatedTasks
    }
}

/**
 * 简化的任务统计信息
 */
data class SimpleTaskStatistics(
    val totalCount: Int,
    val completedCount: Int,
    val inProgressCount: Int,
    val overdueCount: Int,
    val highPriorityCount: Int,
    val completionRate: Double
)