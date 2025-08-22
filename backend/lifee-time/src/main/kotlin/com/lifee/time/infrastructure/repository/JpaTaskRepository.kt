package com.lifee.time.infrastructure.repository

import com.lifee.time.domain.*
import com.lifee.time.domain.repository.TaskRepository
import com.lifee.time.infrastructure.entity.TaskEntity
import com.lifee.time.infrastructure.entity.toDomain
import com.lifee.time.infrastructure.entity.toEntity
import com.lifee.user.domain.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * JPA任务Repository接口
 */
interface JpaTaskRepositoryInterface : JpaRepository<TaskEntity, String> {
    
    @Query("SELECT t FROM TaskEntity t WHERE t.projectId = :projectId")
    fun findByProjectId(@Param("projectId") projectId: String): List<TaskEntity>
    
    @Query("SELECT t FROM TaskEntity t WHERE t.assigneeId = :assigneeId")
    fun findByAssigneeId(@Param("assigneeId") assigneeId: String): List<TaskEntity>
    
    @Query("SELECT t FROM TaskEntity t WHERE t.creatorId = :creatorId")
    fun findByCreatorId(@Param("creatorId") creatorId: String): List<TaskEntity>
    
    @Query("SELECT t FROM TaskEntity t WHERE t.status = :status")
    fun findByStatus(@Param("status") status: TaskStatus): List<TaskEntity>
    
    @Query("SELECT t FROM TaskEntity t WHERE t.priority = :priority")
    fun findByPriority(@Param("priority") priority: TaskPriority): List<TaskEntity>
    
    @Query("""
        SELECT t FROM TaskEntity t 
        WHERE t.dueDate BETWEEN :startDate AND :endDate
    """)
    fun findByDueDateBetween(
        @Param("startDate") startDate: Instant,
        @Param("endDate") endDate: Instant
    ): List<TaskEntity>
    
    @Query("""
        SELECT t FROM TaskEntity t 
        WHERE t.dueDate < :currentTime AND t.status != 'COMPLETED'
    """)
    fun findOverdueTasks(@Param("currentTime") currentTime: Instant): List<TaskEntity>
    
    @Query("""
        SELECT t FROM TaskEntity t 
        WHERE t.dueDate BETWEEN :currentTime AND :soonTime AND t.status != 'COMPLETED'
    """)
    fun findTasksDueSoon(
        @Param("currentTime") currentTime: Instant,
        @Param("soonTime") soonTime: Instant
    ): List<TaskEntity>
    
    @Query("""
        SELECT t FROM TaskEntity t 
        WHERE EXISTS (
            SELECT 1 FROM t.tags tag WHERE tag IN :tags
        )
    """)
    fun findByTagsIn(@Param("tags") tags: Set<String>): List<TaskEntity>
    
    @Query("""
        SELECT t FROM TaskEntity t 
        WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    fun findByNameContainingIgnoreCase(@Param("name") name: String): List<TaskEntity>
    
    @Query("""
        SELECT t FROM TaskEntity t 
        WHERE t.assigneeId = :userId AND t.status IN ('TODO', 'IN_PROGRESS')
    """)
    fun findActiveTasksByUserId(@Param("userId") userId: String): List<TaskEntity>
    
    @Query("""
        SELECT t FROM TaskEntity t 
        WHERE t.projectId = :projectId AND t.status IN ('TODO', 'IN_PROGRESS')
    """)
    fun findActiveTasksByProjectId(@Param("projectId") projectId: String): List<TaskEntity>
    
    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.projectId = :projectId")
    fun countByProjectId(@Param("projectId") projectId: String): Long
    
    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.assigneeId = :assigneeId")
    fun countByAssigneeId(@Param("assigneeId") assigneeId: String): Long
    
    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.creatorId = :creatorId")
    fun countByCreatorId(@Param("creatorId") creatorId: String): Long
    
    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.status = :status")
    fun countByStatus(@Param("status") status: TaskStatus): Long
    
    @Query("""
        SELECT COUNT(t) FROM TaskEntity t 
        WHERE t.dueDate < :currentTime AND t.status != 'COMPLETED'
    """)
    fun countOverdueTasks(@Param("currentTime") currentTime: Instant): Long
}

/**
 * 任务Repository实现
 */
@Repository
class JpaTaskRepository(
    private val jpaRepository: JpaTaskRepositoryInterface
) : TaskRepository {
    
    override fun save(task: Task): Task {
        val entity = task.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun findById(taskId: TaskId): Task? {
        return jpaRepository.findById(taskId.value)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findByProjectId(projectId: ProjectId): List<Task> {
        return jpaRepository.findByProjectId(projectId.value)
            .map { it.toDomain() }
    }
    
    override fun findByAssigneeId(assigneeId: UserId): List<Task> {
        return jpaRepository.findByAssigneeId(assigneeId.value)
            .map { it.toDomain() }
    }
    
    override fun findByCreatorId(creatorId: UserId): List<Task> {
        return jpaRepository.findByCreatorId(creatorId.value)
            .map { it.toDomain() }
    }
    
    override fun findByStatus(status: TaskStatus): List<Task> {
        return jpaRepository.findByStatus(status)
            .map { it.toDomain() }
    }
    
    override fun findByPriority(priority: TaskPriority): List<Task> {
        return jpaRepository.findByPriority(priority)
            .map { it.toDomain() }
    }
    
    override fun findByDueDateBetween(startDate: Instant, endDate: Instant): List<Task> {
        return jpaRepository.findByDueDateBetween(startDate, endDate)
            .map { it.toDomain() }
    }
    
    override fun findOverdueTasks(): List<Task> {
        return jpaRepository.findOverdueTasks(Instant.now())
            .map { it.toDomain() }
    }
    
    override fun findTasksDueSoon(hours: Long): List<Task> {
        val now = Instant.now()
        val soonTime = now.plusSeconds(hours * 3600)
        return jpaRepository.findTasksDueSoon(now, soonTime)
            .map { it.toDomain() }
    }
    
    override fun findByTags(tags: Set<String>): List<Task> {
        return jpaRepository.findByTagsIn(tags)
            .map { it.toDomain() }
    }
    
    override fun findByNameContaining(name: String): List<Task> {
        return jpaRepository.findByNameContainingIgnoreCase(name)
            .map { it.toDomain() }
    }
    
    override fun findActiveTasksByUserId(userId: UserId): List<Task> {
        return jpaRepository.findActiveTasksByUserId(userId.value)
            .map { it.toDomain() }
    }
    
    override fun findActiveTasksByProjectId(projectId: ProjectId): List<Task> {
        return jpaRepository.findActiveTasksByProjectId(projectId.value)
            .map { it.toDomain() }
    }
    
    override fun countByProjectId(projectId: ProjectId): Long {
        return jpaRepository.countByProjectId(projectId.value)
    }
    
    override fun countByAssigneeId(assigneeId: UserId): Long {
        return jpaRepository.countByAssigneeId(assigneeId.value)
    }
    
    override fun countByCreatorId(creatorId: UserId): Long {
        return jpaRepository.countByCreatorId(creatorId.value)
    }
    
    override fun countByStatus(status: TaskStatus): Long {
        return jpaRepository.countByStatus(status)
    }
    
    override fun countOverdueTasks(): Long {
        return jpaRepository.countOverdueTasks(Instant.now())
    }
    
    override fun delete(taskId: TaskId) {
        jpaRepository.deleteById(taskId.value)
    }
    
    override fun existsById(taskId: TaskId): Boolean {
        return jpaRepository.existsById(taskId.value)
    }
}