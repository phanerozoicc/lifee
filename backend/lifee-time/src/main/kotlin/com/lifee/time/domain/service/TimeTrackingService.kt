package com.lifee.time.domain.service

import com.lifee.time.domain.*
import com.lifee.time.domain.repository.TimeEntryRepository
import com.lifee.time.domain.repository.ProjectRepository
import com.lifee.time.domain.exception.TimeEntryNotFoundException
import com.lifee.user.domain.UserId
import java.time.Instant

import java.math.BigDecimal

/**
 * 时间追踪领域服务
 */
class TimeTrackingService(
    private val timeEntryRepository: TimeEntryRepository,
    private val projectRepository: ProjectRepository
) {
    
    /**
     * 开始时间追踪
     */
    suspend fun startTimeTracking(
        userId: UserId,
        description: String,
        projectId: ProjectId? = null,
        taskId: TaskId? = null,
        tags: Set<String> = emptySet(),
        billable: Boolean = false,
        hourlyRate: BigDecimal? = null
    ): TimeEntry {
        // 检查是否有正在运行的时间条目
        val runningEntry = timeEntryRepository.findRunningByUserId(userId)
        if (runningEntry != null) {
            throw IllegalStateException("用户已有正在运行的时间条目: ${runningEntry.id}")
        }
        
        // 验证项目访问权限
        if (projectId != null) {
            val project = projectRepository.findById(projectId)
                ?: throw IllegalArgumentException("项目不存在: $projectId")
            
            if (!project.canAccess(userId)) {
                throw IllegalArgumentException("用户无权访问项目: $projectId")
            }
        }
        
        // 创建新的时间条目
        val timeEntry = TimeEntry.create(
            userId = userId,
            description = description,
            projectId = projectId,
            taskId = taskId?.value,
            tags = tags,
            billable = billable,
            hourlyRate = hourlyRate?.toDouble()
        )
        
        // 开始时间追踪
        timeEntry.start()
        
        // 保存时间条目
        timeEntryRepository.save(timeEntry)
        
        return timeEntry
    }
    
    /**
     * 停止时间追踪
     */
    suspend fun stopTimeTracking(userId: UserId, timeEntryId: TimeEntryId? = null): TimeEntry {
        val timeEntry = if (timeEntryId != null) {
            // 停止指定的时间条目
            timeEntryRepository.findById(timeEntryId)
                ?: throw IllegalArgumentException("时间条目不存在: $timeEntryId")
        } else {
            // 停止当前正在运行的时间条目
            timeEntryRepository.findRunningByUserId(userId)
                ?: throw IllegalStateException("用户没有正在运行的时间条目")
        }
        
        // 验证用户权限
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("用户无权操作此时间条目")
        }
        
        // 停止时间追踪
        timeEntry.stop()
        
        // 保存时间条目
        timeEntryRepository.save(timeEntry)
        
        return timeEntry
    }
    
    /**
     * 暂停时间追踪
     */
    suspend fun pauseTimeTracking(userId: UserId, timeEntryId: TimeEntryId? = null): TimeEntry {
        val timeEntry = if (timeEntryId != null) {
            timeEntryRepository.findById(timeEntryId)
                ?: throw IllegalArgumentException("时间条目不存在: $timeEntryId")
        } else {
            timeEntryRepository.findRunningByUserId(userId)
                ?: throw IllegalStateException("用户没有正在运行的时间条目")
        }
        
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("用户无权操作此时间条目")
        }
        
        timeEntry.pause()
        timeEntryRepository.save(timeEntry)
        
        return timeEntry
    }
    
    /**
     * 恢复时间追踪
     */
    suspend fun resumeTimeTracking(userId: UserId, timeEntryId: TimeEntryId): TimeEntry {
        // 检查是否有其他正在运行的时间条目
        val runningEntry = timeEntryRepository.findRunningByUserId(userId)
        if (runningEntry != null && runningEntry.id != timeEntryId) {
            throw IllegalStateException("用户已有正在运行的时间条目: ${runningEntry.id}")
        }
        
        val timeEntry = timeEntryRepository.findById(timeEntryId)
            ?: throw TimeEntryNotFoundException("TimeEntry not found: $timeEntryId")
        
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("用户无权操作此时间条目")
        }
        
        timeEntry.resume()
        timeEntryRepository.save(timeEntry)
        
        return timeEntry
    }
    
    /**
     * 更新时间条目描述
     */
    suspend fun updateDescription(
        userId: UserId,
        timeEntryId: TimeEntryId,
        description: String
    ): TimeEntry {
        val timeEntry = timeEntryRepository.findById(timeEntryId)
            ?: throw IllegalArgumentException("时间条目不存在: $timeEntryId")
        
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("用户无权操作此时间条目")
        }
        
        timeEntry.updateDescription(description)
        timeEntryRepository.save(timeEntry)
        
        return timeEntry
    }
    
    /**
     * 更新时间条目项目
     */
    suspend fun updateProject(
        userId: UserId,
        timeEntryId: TimeEntryId,
        projectId: ProjectId?
    ): TimeEntry {
        val timeEntry = timeEntryRepository.findById(timeEntryId)
            ?: throw IllegalArgumentException("时间条目不存在: $timeEntryId")
        
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("用户无权操作此时间条目")
        }
        
        // 验证项目访问权限
        if (projectId != null) {
            val project = projectRepository.findById(projectId)
                ?: throw IllegalArgumentException("项目不存在: $projectId")
            
            if (!project.canAccess(userId)) {
                throw IllegalArgumentException("用户无权访问项目: $projectId")
            }
        }
        
        timeEntry.updateProject(projectId)
        timeEntryRepository.save(timeEntry)
        
        return timeEntry
    }
    
    /**
     * 更新时间条目标签
     */
    suspend fun updateTags(
        userId: UserId,
        timeEntryId: TimeEntryId,
        tags: Set<String>
    ): TimeEntry {
        val timeEntry = timeEntryRepository.findById(timeEntryId)
            ?: throw IllegalArgumentException("时间条目不存在: $timeEntryId")
        
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("用户无权操作此时间条目")
        }
        
        timeEntry.updateTags(tags)
        timeEntryRepository.save(timeEntry)
        
        return timeEntry
    }
    
    /**
     * 更新计费状态
     */
    suspend fun updateBillableStatus(
        userId: UserId,
        timeEntryId: TimeEntryId,
        billable: Boolean,
        hourlyRate: BigDecimal? = null
    ): TimeEntry {
        val timeEntry = timeEntryRepository.findById(timeEntryId)
            ?: throw IllegalArgumentException("时间条目不存在: $timeEntryId")
        
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("用户无权操作此时间条目")
        }
        
        timeEntry.updateBillable(billable, hourlyRate?.toDouble())
        timeEntryRepository.save(timeEntry)
        
        return timeEntry
    }
    
    /**
     * 删除时间条目
     */
    suspend fun deleteTimeEntry(userId: UserId, timeEntryId: TimeEntryId) {
        val timeEntry = timeEntryRepository.findById(timeEntryId)
            ?: throw IllegalArgumentException("时间条目不存在: $timeEntryId")
        
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("用户无权操作此时间条目")
        }
        
        timeEntry.delete()
        timeEntryRepository.save(timeEntry)
    }
    
    /**
     * 获取用户当前正在运行的时间条目
     */
    suspend fun getCurrentRunningEntry(userId: UserId): TimeEntry? {
        return timeEntryRepository.findRunningByUserId(userId)
    }
    
    /**
     * 计算时间范围内的总时长
     */
    suspend fun calculateTotalDuration(
        userId: UserId,
        timeRange: TimeRange
    ): java.time.Duration {
        val timeEntries = timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
        return timeEntries
            .filter { it.status == TimeEntryStatus.STOPPED }
            .mapNotNull { it.getTotalDuration() }
            .map { it.toJavaDuration() }
            .fold(java.time.Duration.ZERO) { acc, duration -> acc.plus(duration) }
    }
    
    /**
     * 计算项目的总时长
     */
    suspend fun calculateProjectTotalDuration(
        projectId: ProjectId,
        timeRange: TimeRange? = null
    ): java.time.Duration {
        val timeEntries = if (timeRange != null) {
            timeEntryRepository.findByProjectIdAndTimeRange(projectId, timeRange)
        } else {
            timeEntryRepository.findByProjectId(projectId)
        }
        
        return timeEntries
            .filter { it.status == TimeEntryStatus.STOPPED }
            .mapNotNull { it.getTotalDuration() }
            .map { it.toJavaDuration() }
            .fold(java.time.Duration.ZERO) { acc, duration -> acc.plus(duration) }
    }
}