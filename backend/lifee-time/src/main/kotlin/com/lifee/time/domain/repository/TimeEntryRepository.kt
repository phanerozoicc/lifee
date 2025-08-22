package com.lifee.time.domain.repository

import com.lifee.time.domain.TimeEntry
import com.lifee.time.domain.TimeEntryId
import com.lifee.time.domain.ProjectId
import com.lifee.time.domain.TimeRange
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 时间条目仓储接口
 */
interface TimeEntryRepository {
    
    /**
     * 保存时间条目
     */
    suspend fun save(timeEntry: TimeEntry)
    
    /**
     * 根据ID查找时间条目
     */
    suspend fun findById(timeEntryId: TimeEntryId): TimeEntry?
    
    /**
     * 根据用户ID查找时间条目列表
     */
    suspend fun findByUserId(userId: UserId, limit: Int = 50, offset: Int = 0): List<TimeEntry>
    
    /**
     * 根据项目ID查找时间条目列表
     */
    suspend fun findByProjectId(projectId: ProjectId, limit: Int = 50, offset: Int = 0): List<TimeEntry>
    
    /**
     * 根据用户ID和时间范围查找时间条目
     */
    suspend fun findByUserIdAndTimeRange(
        userId: UserId,
        timeRange: TimeRange,
        limit: Int = 50,
        offset: Int = 0
    ): List<TimeEntry>
    
    /**
     * 根据项目ID和时间范围查找时间条目
     */
    suspend fun findByProjectIdAndTimeRange(
        projectId: ProjectId,
        timeRange: TimeRange,
        limit: Int = 50,
        offset: Int = 0
    ): List<TimeEntry>
    
    /**
     * 查找用户当前正在运行的时间条目
     */
    suspend fun findRunningByUserId(userId: UserId): TimeEntry?
    
    /**
     * 查找用户在指定时间段内的所有时间条目
     */
    suspend fun findByUserIdBetween(
        userId: UserId,
        startTime: Instant,
        endTime: Instant
    ): List<TimeEntry>
    
    /**
     * 查找项目在指定时间段内的所有时间条目
     */
    suspend fun findByProjectIdBetween(
        projectId: ProjectId,
        startTime: Instant,
        endTime: Instant
    ): List<TimeEntry>
    
    /**
     * 根据标签查找时间条目
     */
    suspend fun findByTags(
        tags: Set<String>,
        userId: UserId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<TimeEntry>
    
    /**
     * 查找可计费的时间条目
     */
    suspend fun findBillableByUserId(
        userId: UserId,
        startTime: Instant? = null,
        endTime: Instant? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<TimeEntry>
    
    /**
     * 统计用户的时间条目数量
     */
    suspend fun countByUserId(userId: UserId): Long
    
    /**
     * 统计项目的时间条目数量
     */
    suspend fun countByProjectId(projectId: ProjectId): Long
    
    /**
     * 删除时间条目
     */
    suspend fun delete(timeEntryId: TimeEntryId)
    
    /**
     * 检查时间条目是否存在
     */
    suspend fun exists(timeEntryId: TimeEntryId): Boolean
}