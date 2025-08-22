package com.lifee.time.web

import com.lifee.time.application.TimeTrackingApplicationService
import com.lifee.time.domain.*
import com.lifee.time.web.dto.*
import com.lifee.user.domain.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

/**
 * 时间追踪控制器
 */
@RestController
@RequestMapping("/api/time-tracking")
class TimeTrackingController(
    private val timeTrackingService: TimeTrackingApplicationService
) {
    
    /**
     * 开始时间追踪
     */
    @PostMapping("/start")
    fun startTimeTracking(
        @RequestBody request: StartTimeTrackingRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeEntryResponse> {
        val timeEntry = timeTrackingService.startTimeTracking(
            userId = UserId(userId),
            projectId = request.projectId?.let { ProjectId(it) },
            taskId = request.taskId?.let { TaskId(it) },
            description = request.description,
            tags = request.tags ?: emptySet(),
            billable = request.billable ?: false,
            hourlyRate = request.hourlyRate
        )
        return ResponseEntity.ok(timeEntry.toResponse())
    }
    
    /**
     * 停止时间追踪
     */
    @PostMapping("/stop/{timeEntryId}")
    fun stopTimeTracking(
        @PathVariable timeEntryId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeEntryResponse> {
        val timeEntry = timeTrackingService.stopTimeTracking(
            timeEntryId = TimeEntryId(timeEntryId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(timeEntry.toResponse())
    }
    
    /**
     * 暂停时间追踪
     */
    @PostMapping("/pause/{timeEntryId}")
    fun pauseTimeTracking(
        @PathVariable timeEntryId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeEntryResponse> {
        val timeEntry = timeTrackingService.pauseTimeTracking(
            timeEntryId = TimeEntryId(timeEntryId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(timeEntry.toResponse())
    }
    
    /**
     * 恢复时间追踪
     */
    @PostMapping("/resume/{timeEntryId}")
    fun resumeTimeTracking(
        @PathVariable timeEntryId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeEntryResponse> {
        val timeEntry = timeTrackingService.resumeTimeTracking(
            timeEntryId = TimeEntryId(timeEntryId),
            userId = UserId(userId)
        )
        return ResponseEntity.ok(timeEntry.toResponse())
    }
    
    /**
     * 更新时间记录描述
     */
    @PutMapping("/{timeEntryId}/description")
    fun updateDescription(
        @PathVariable timeEntryId: String,
        @RequestBody request: UpdateDescriptionRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeEntryResponse> {
        val timeEntry = timeTrackingService.updateDescription(
            timeEntryId = TimeEntryId(timeEntryId),
            description = request.description,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(timeEntry.toResponse())
    }
    
    /**
     * 更新时间记录项目
     */
    @PutMapping("/{timeEntryId}/project")
    fun updateProject(
        @PathVariable timeEntryId: String,
        @RequestBody request: UpdateProjectRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeEntryResponse> {
        val timeEntry = timeTrackingService.updateProject(
            timeEntryId = TimeEntryId(timeEntryId),
            projectId = request.projectId?.let { ProjectId(it) },
            userId = UserId(userId)
        )
        return ResponseEntity.ok(timeEntry.toResponse())
    }
    
    /**
     * 更新时间记录标签
     */
    @PutMapping("/{timeEntryId}/tags")
    fun updateTags(
        @PathVariable timeEntryId: String,
        @RequestBody request: UpdateTagsRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeEntryResponse> {
        val timeEntry = timeTrackingService.updateTags(
            timeEntryId = TimeEntryId(timeEntryId),
            tags = request.tags,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(timeEntry.toResponse())
    }
    
    /**
     * 更新计费状态
     */
    @PutMapping("/{timeEntryId}/billable")
    fun updateBillableStatus(
        @PathVariable timeEntryId: String,
        @RequestBody request: UpdateBillableRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeEntryResponse> {
        val timeEntry = timeTrackingService.updateBillableStatus(
            timeEntryId = TimeEntryId(timeEntryId),
            billable = request.billable,
            hourlyRate = request.hourlyRate,
            userId = UserId(userId)
        )
        return ResponseEntity.ok(timeEntry.toResponse())
    }
    
    /**
     * 删除时间记录
     */
    @DeleteMapping("/{timeEntryId}")
    fun deleteTimeEntry(
        @PathVariable timeEntryId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Void> {
        timeTrackingService.deleteTimeEntry(
            timeEntryId = TimeEntryId(timeEntryId),
            userId = UserId(userId)
        )
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 获取当前运行的时间记录
     */
    @GetMapping("/current")
    fun getCurrentRunningEntry(
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeEntryResponse?> {
        val timeEntry = timeTrackingService.getCurrentRunningEntry(UserId(userId))
        return ResponseEntity.ok(timeEntry?.toResponse())
    }
    
    /**
     * 获取用户时间记录列表
     */
    @GetMapping
    fun getTimeEntries(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) taskId: String?,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(required = false) billable: Boolean?,
        @RequestParam(required = false) tags: Set<String>?
    ): ResponseEntity<List<TimeEntryResponse>> {
        val timeEntries = timeTrackingService.getUserTimeEntries(
            userId = UserId(userId),
            projectId = projectId?.let { ProjectId(it) },
            taskId = taskId?.let { TaskId(it) },
            startDate = startDate?.let { Instant.parse(it) },
            endDate = endDate?.let { Instant.parse(it) },
            billable = billable,
            tags = tags
        )
        return ResponseEntity.ok(timeEntries.map { it.toResponse() })
    }
    
    /**
     * 计算总时长
     */
    @GetMapping("/duration/total")
    fun calculateTotalDuration(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): ResponseEntity<DurationResponse> {
        val duration = timeTrackingService.calculateTotalDuration(
            userId = UserId(userId),
            projectId = projectId?.let { ProjectId(it) },
            startDate = startDate?.let { Instant.parse(it) },
            endDate = endDate?.let { Instant.parse(it) }
        )
        return ResponseEntity.ok(DurationResponse(duration.toMinutes()))
    }
    
    /**
     * 计算项目总时长
     */
    @GetMapping("/duration/project/{projectId}")
    fun calculateProjectTotalDuration(
        @PathVariable projectId: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): ResponseEntity<DurationResponse> {
        val duration = timeTrackingService.calculateProjectTotalDuration(
            projectId = ProjectId(projectId),
            userId = UserId(userId),
            startDate = startDate?.let { Instant.parse(it) },
            endDate = endDate?.let { Instant.parse(it) }
        )
        return ResponseEntity.ok(DurationResponse(duration.toMinutes()))
    }
}

/**
 * 扩展函数：领域对象转响应DTO
 */
fun TimeEntry.toResponse(): TimeEntryResponse {
    return TimeEntryResponse(
        timeEntryId = timeEntryId.value,
        userId = userId.value,
        projectId = projectId?.value,
        taskId = taskId?.value,
        description = description,
        tags = tags,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = duration?.toMinutes(),
        billable = billable,
        hourlyRate = hourlyRate,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}