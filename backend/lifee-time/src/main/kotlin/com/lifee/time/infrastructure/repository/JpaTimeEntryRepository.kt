package com.lifee.time.infrastructure.repository

import com.lifee.time.domain.*
import com.lifee.time.domain.repository.TimeEntryRepository
import com.lifee.time.infrastructure.entity.TimeEntryEntity
import com.lifee.time.infrastructure.entity.toEntity
import com.lifee.time.infrastructure.entity.toDomain
import com.lifee.user.domain.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDate

/**
 * JPA时间记录Repository接口
 */
interface JpaTimeEntryRepositoryInterface : JpaRepository<TimeEntryEntity, String> {
    
    @Query("SELECT t FROM TimeEntryEntity t WHERE t.userId = :userId ORDER BY t.startTime DESC")
    fun findByUserId(@Param("userId") userId: String): List<TimeEntryEntity>
    
    @Query("SELECT t FROM TimeEntryEntity t WHERE t.projectId = :projectId ORDER BY t.startTime DESC")
    fun findByProjectId(@Param("projectId") projectId: String): List<TimeEntryEntity>
    
    @Query("SELECT t FROM TimeEntryEntity t WHERE t.taskId = :taskId ORDER BY t.startTime DESC")
    fun findByTaskId(@Param("taskId") taskId: String): List<TimeEntryEntity>
    
    @Query("SELECT t FROM TimeEntryEntity t WHERE t.userId = :userId AND t.startTime >= :startTime AND t.startTime < :endTime ORDER BY t.startTime DESC")
    fun findByUserIdAndTimeRange(
        @Param("userId") userId: String,
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): List<TimeEntryEntity>
    
    @Query("SELECT t FROM TimeEntryEntity t WHERE t.projectId = :projectId AND t.startTime >= :startTime AND t.startTime < :endTime ORDER BY t.startTime DESC")
    fun findByProjectIdAndTimeRange(
        @Param("projectId") projectId: String,
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): List<TimeEntryEntity>
    
    @Query("SELECT t FROM TimeEntryEntity t WHERE t.startTime >= :startTime AND t.startTime < :endTime ORDER BY t.startTime DESC")
    fun findByTimeRange(
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): List<TimeEntryEntity>
    
    @Query("SELECT t FROM TimeEntryEntity t WHERE t.billable = :billable ORDER BY t.startTime DESC")
    fun findByBillable(@Param("billable") billable: Boolean): List<TimeEntryEntity>
    
    @Query("SELECT t FROM TimeEntryEntity t WHERE t.userId = :userId AND t.endTime IS NULL")
    fun findRunningByUserId(@Param("userId") userId: String): TimeEntryEntity?
    
    @Query("SELECT COUNT(t) FROM TimeEntryEntity t WHERE t.userId = :userId")
    fun countByUserId(@Param("userId") userId: String): Long
    
    @Query("SELECT COUNT(t) FROM TimeEntryEntity t WHERE t.projectId = :projectId")
    fun countByProjectId(@Param("projectId") projectId: String): Long
    
    @Query("SELECT COUNT(t) FROM TimeEntryEntity t WHERE t.taskId = :taskId")
    fun countByTaskId(@Param("taskId") taskId: String): Long
    
    @Query("SELECT COUNT(t) FROM TimeEntryEntity t WHERE t.billable = :billable")
    fun countByBillable(@Param("billable") billable: Boolean): Long
    
    @Query("SELECT DISTINCT t.projectId FROM TimeEntryEntity t WHERE t.userId = :userId AND t.projectId IS NOT NULL")
    fun findProjectIdsByUserId(@Param("userId") userId: String): List<String>
    
    @Query("""
        SELECT t FROM TimeEntryEntity t 
        WHERE EXISTS (
            SELECT 1 FROM t.tags tag WHERE tag IN :tags
        )
        ORDER BY t.startTime DESC
    """)
    fun findByTagsIn(@Param("tags") tags: Set<String>): List<TimeEntryEntity>
}

/**
 * JPA时间记录Repository实现
 */
@Repository
class JpaTimeEntryRepository(
    private val jpaRepository: JpaTimeEntryRepositoryInterface
) : TimeEntryRepository {
    
    override suspend fun save(timeEntry: TimeEntry): TimeEntry {
        val entity = timeEntry.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override suspend fun findById(timeEntryId: TimeEntryId): TimeEntry? {
        return jpaRepository.findById(timeEntryId.value)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override suspend fun findByUserId(userId: UserId): List<TimeEntry> {
        return jpaRepository.findByUserId(userId.value)
            .map { it.toDomain() }
    }
    
    override suspend fun findByProjectId(projectId: ProjectId): List<TimeEntry> {
        return jpaRepository.findByProjectId(projectId.value)
            .map { it.toDomain() }
    }
    
    override suspend fun findByTaskId(taskId: TaskId): List<TimeEntry> {
        return jpaRepository.findByTaskId(taskId.value)
            .map { it.toDomain() }
    }
    
    override suspend fun findByUserIdAndTimeRange(userId: UserId, timeRange: TimeRange): List<TimeEntry> {
        return jpaRepository.findByUserIdAndTimeRange(
            userId.value,
            timeRange.startTime,
            timeRange.endTime
        ).map { it.toDomain() }
    }
    
    override suspend fun findByProjectIdAndTimeRange(projectId: ProjectId, timeRange: TimeRange): List<TimeEntry> {
        return jpaRepository.findByProjectIdAndTimeRange(
            projectId.value,
            timeRange.startTime,
            timeRange.endTime
        ).map { it.toDomain() }
    }
    
    override suspend fun findByTimeRange(timeRange: TimeRange): List<TimeEntry> {
        return jpaRepository.findByTimeRange(
            timeRange.startTime,
            timeRange.endTime
        ).map { it.toDomain() }
    }
    
    override suspend fun findByTags(tags: Set<String>): List<TimeEntry> {
        return jpaRepository.findByTagsIn(tags)
            .map { it.toDomain() }
    }
    
    override suspend fun findByBillable(billable: Boolean): List<TimeEntry> {
        return jpaRepository.findByBillable(billable)
            .map { it.toDomain() }
    }
    
    override suspend fun findRunningByUserId(userId: UserId): TimeEntry? {
        return jpaRepository.findRunningByUserId(userId.value)
            ?.toDomain()
    }
    
    override suspend fun countByUserId(userId: UserId): Long {
        return jpaRepository.countByUserId(userId.value)
    }
    
    override suspend fun countByProjectId(projectId: ProjectId): Long {
        return jpaRepository.countByProjectId(projectId.value)
    }
    
    override suspend fun countByTaskId(taskId: TaskId): Long {
        return jpaRepository.countByTaskId(taskId.value)
    }
    
    override suspend fun countByBillable(billable: Boolean): Long {
        return jpaRepository.countByBillable(billable)
    }
    
    override suspend fun delete(timeEntryId: TimeEntryId) {
        jpaRepository.deleteById(timeEntryId.value)
    }
    
    override suspend fun exists(timeEntryId: TimeEntryId): Boolean {
        return jpaRepository.existsById(timeEntryId.value)
    }
    
    override suspend fun findProjectsByUserId(userId: UserId): List<ProjectId> {
        return jpaRepository.findProjectIdsByUserId(userId.value)
            .map { ProjectId(it) }
    }
}