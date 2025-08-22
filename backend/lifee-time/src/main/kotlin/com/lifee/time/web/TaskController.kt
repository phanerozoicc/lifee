package com.lifee.time.web

import com.lifee.time.application.TimeTrackingApplicationService
import com.lifee.time.domain.*
import com.lifee.time.web.dto.*
import com.lifee.user.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * 任务管理控制器
 */
@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val timeTrackingService: TimeTrackingApplicationService
) {
    
    /**
     * 创建任务
     */
    @PostMapping
    fun createTask(
        @RequestBody request: CreateTaskRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.createTask(
            name = request.name,
            description = request.description,
            projectId = ProjectId(request.projectId),
            assigneeId = request.assigneeId?.let { UserId(it) },
            priority = TaskPriority.valueOf(request.priority),
            dueDate = request.dueDate,
            estimatedDuration = request.estimatedHours?.let { Duration.ofHours(it) },
            tags = request.tags,
            creatorId = UserId(userId)
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(task.toResponse())
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    fun getTask(
        @PathVariable taskId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.getTask(
            taskId = TaskId(taskId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(task.toResponse())
    }
    
    /**
     * 更新任务
     */
    @PutMapping("/{taskId}")
    fun updateTask(
        @PathVariable taskId: String,
        @RequestBody request: UpdateTaskRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.updateTask(
            taskId = TaskId(taskId),
            name = request.name,
            description = request.description,
            priority = request.priority?.let { TaskPriority.valueOf(it) },
            dueDate = request.dueDate,
            estimatedDuration = request.estimatedHours?.let { Duration.ofHours(it) },
            tags = request.tags,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(task.toResponse())
    }
    
    /**
     * 分配任务
     */
    @PostMapping("/{taskId}/assign")
    fun assignTask(
        @PathVariable taskId: String,
        @RequestBody request: AssignTaskRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.assignTask(
            taskId = TaskId(taskId),
            assigneeId = UserId(request.assigneeId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(task.toResponse())
    }
    
    /**
     * 取消分配任务
     */
    @PostMapping("/{taskId}/unassign")
    fun unassignTask(
        @PathVariable taskId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.unassignTask(
            taskId = TaskId(taskId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(task.toResponse())
    }
    
    /**
     * 开始任务
     */
    @PostMapping("/{taskId}/start")
    fun startTask(
        @PathVariable taskId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.startTask(
            taskId = TaskId(taskId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(task.toResponse())
    }
    
    /**
     * 完成任务
     */
    @PostMapping("/{taskId}/complete")
    fun completeTask(
        @PathVariable taskId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.completeTask(
            taskId = TaskId(taskId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(task.toResponse())
    }
    
    /**
     * 取消任务
     */
    @PostMapping("/{taskId}/cancel")
    fun cancelTask(
        @PathVariable taskId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.cancelTask(
            taskId = TaskId(taskId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(task.toResponse())
    }
    
    /**
     * 重新开始任务
     */
    @PostMapping("/{taskId}/restart")
    fun restartTask(
        @PathVariable taskId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.restartTask(
            taskId = TaskId(taskId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(task.toResponse())
    }
    
    /**
     * 更新任务标签
     */
    @PutMapping("/{taskId}/tags")
    fun updateTaskTags(
        @PathVariable taskId: String,
        @RequestBody request: UpdateTaskTagsRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TaskResponse> {
        val task = timeTrackingService.updateTaskTags(
            taskId = TaskId(taskId),
            tags = request.tags,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(task.toResponse())
    }
    
    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    fun deleteTask(
        @PathVariable taskId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Void> {
        timeTrackingService.deleteTask(
            taskId = TaskId(taskId),
            userId = UserId(userId)
        )
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 获取用户任务列表
     */
    @GetMapping
    fun getUserTasks(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) priority: String?,
        @RequestParam(required = false) assignedToMe: Boolean?
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = timeTrackingService.getUserTasks(
            userId = UserId(userId),
            projectId = projectId?.let { ProjectId(it) },
            status = status?.let { TaskStatus.valueOf(it) },
            priority = priority?.let { TaskPriority.valueOf(it) },
            assignedToMe = assignedToMe ?: false
        )
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }
    
    /**
     * 获取项目任务列表
     */
    @GetMapping("/project/{projectId}")
    fun getProjectTasks(
        @PathVariable projectId: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) assigneeId: String?
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = timeTrackingService.getProjectTasks(
            projectId = ProjectId(projectId),
            userId = UserId(userId),
            status = status?.let { TaskStatus.valueOf(it) },
            assigneeId = assigneeId?.let { UserId(it) }
        )
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }
    
    /**
     * 获取逾期任务
     */
    @GetMapping("/overdue")
    fun getOverdueTasks(
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = timeTrackingService.getOverdueTasks(
            userId = UserId(userId)
        )
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }
    
    /**
     * 获取即将到期的任务
     */
    @GetMapping("/due-soon")
    fun getTasksDueSoon(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(defaultValue = "7") days: Int
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = timeTrackingService.getTasksDueSoon(
            userId = UserId(userId),
            days = days
        )
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }
    
    /**
     * 搜索任务
     */
    @GetMapping("/search")
    fun searchTasks(
        @RequestParam query: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) projectId: String?
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = timeTrackingService.searchTasks(
            query = query,
            userId = UserId(userId),
            projectId = projectId?.let { ProjectId(it) }
        )
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }
    
    /**
     * 获取任务统计
     */
    @GetMapping("/statistics")
    fun getTaskStatistics(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) projectId: String?
    ): ResponseEntity<TaskStatisticsResponse> {
        val statistics = timeTrackingService.getTaskStatistics(
            userId = UserId(userId),
            projectId = projectId?.let { ProjectId(it) }
        )
        return ResponseEntity.ok(statistics.toResponse())
    }
    
    /**
     * 批量更新任务状态
     */
    @PutMapping("/batch/status")
    fun batchUpdateTaskStatus(
        @RequestBody request: BatchUpdateTaskStatusRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<List<TaskResponse>> {
        val tasks = timeTrackingService.batchUpdateTaskStatus(
            taskIds = request.taskIds.map { TaskId(it) },
            status = TaskStatus.valueOf(request.status),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }
}

/**
 * 扩展函数：领域对象转响应DTO
 */
fun Task.toResponse(): TaskResponse {
    return TaskResponse(
        taskId = taskId.value,
        name = name,
        description = description,
        projectId = projectId.value,
        assigneeId = assigneeId?.value,
        creatorId = creatorId.value,
        status = status.name,
        priority = priority.name,
        dueDate = dueDate,
        estimatedDuration = estimatedDuration?.toHours(),
        actualDuration = actualDuration?.toHours(),
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        startedAt = startedAt,
        completedAt = completedAt
    )
}

// 假设有TaskStatistics类
data class TaskStatistics(
    val totalTasks: Long,
    val completedTasks: Long,
    val inProgressTasks: Long,
    val overdueTasks: Long,
    val averageCompletionTime: Long?
)

fun TaskStatistics.toResponse(): TaskStatisticsResponse {
    return TaskStatisticsResponse(
        totalTasks = totalTasks,
        completedTasks = completedTasks,
        inProgressTasks = inProgressTasks,
        overdueTasks = overdueTasks,
        averageCompletionTime = averageCompletionTime
    )
}