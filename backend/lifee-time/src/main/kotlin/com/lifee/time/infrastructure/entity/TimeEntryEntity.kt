package com.lifee.time.infrastructure.entity

import com.lifee.time.domain.*
import com.lifee.user.domain.UserId
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

/**
 * 时间记录实体
 */
@Entity
@Table(name = "time_entries")
data class TimeEntryEntity(
    @Id
    @Column(name = "time_entry_id")
    val timeEntryId: String,
    
    @Column(name = "user_id", nullable = false)
    val userId: String,
    
    @Column(name = "project_id")
    val projectId: String?,
    
    @Column(name = "task_id")
    val taskId: String?,
    
    @Column(name = "description")
    val description: String?,
    
    @ElementCollection
    @CollectionTable(name = "time_entry_tags", joinColumns = [JoinColumn(name = "time_entry_id")])
    @Column(name = "tag")
    val tags: Set<String> = emptySet(),
    
    @Column(name = "start_time", nullable = false)
    val startTime: Instant,
    
    @Column(name = "end_time")
    val endTime: Instant?,
    
    @Column(name = "duration_minutes")
    val durationMinutes: Long?,
    
    @Column(name = "billable", nullable = false)
    val billable: Boolean = false,
    
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    val hourlyRate: BigDecimal?,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: TimeEntryStatus = TimeEntryStatus.RUNNING,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)

/**
 * 项目实体
 */
@Entity
@Table(name = "projects")
data class ProjectEntity(
    @Id
    @Column(name = "project_id")
    val projectId: String,
    
    @Column(name = "name", nullable = false)
    val name: String,
    
    @Column(name = "description")
    val description: String?,
    
    @Column(name = "owner_id", nullable = false)
    val ownerId: String,
    
    @Column(name = "team_id")
    val teamId: String?,
    
    @Column(name = "client_name")
    val clientName: String?,
    
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    val hourlyRate: BigDecimal?,
    
    @Column(name = "estimated_hours")
    val estimatedHours: Long?,
    
    @Column(name = "color")
    val color: String?,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "is_archived", nullable = false)
    val isArchived: Boolean = false,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
    
    @OneToMany(mappedBy = "projectId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val members: Set<ProjectMemberEntity> = emptySet()
)

/**
 * 项目成员实体
 */
@Entity
@Table(name = "project_members")
data class ProjectMemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "project_id", nullable = false)
    val projectId: String,
    
    @Column(name = "user_id", nullable = false)
    val userId: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: ProjectMemberRole,
    
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    val hourlyRate: BigDecimal?,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "joined_at", nullable = false)
    val joinedAt: Instant,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)

/**
 * 任务实体
 */
@Entity
@Table(name = "tasks")
data class TaskEntity(
    @Id
    @Column(name = "task_id")
    val taskId: String,
    
    @Column(name = "name", nullable = false)
    val name: String,
    
    @Column(name = "description")
    val description: String?,
    
    @Column(name = "project_id", nullable = false)
    val projectId: String,
    
    @Column(name = "assignee_id")
    val assigneeId: String?,
    
    @Column(name = "creator_id", nullable = false)
    val creatorId: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: TaskStatus = TaskStatus.TODO,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    val priority: TaskPriority = TaskPriority.MEDIUM,
    
    @Column(name = "estimated_duration_minutes")
    val estimatedDurationMinutes: Long?,
    
    @Column(name = "due_date")
    val dueDate: Instant?,
    
    @ElementCollection
    @CollectionTable(name = "task_tags", joinColumns = [JoinColumn(name = "task_id")])
    @Column(name = "tag")
    val tags: Set<String> = emptySet(),
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)

/**
 * 团队实体
 */
@Entity
@Table(name = "teams")
data class TeamEntity(
    @Id
    @Column(name = "team_id")
    val teamId: String,
    
    @Column(name = "name", nullable = false)
    val name: String,
    
    @Column(name = "description")
    val description: String?,
    
    @Column(name = "owner_id", nullable = false)
    val ownerId: String,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
    
    @OneToMany(mappedBy = "teamId", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val members: Set<TeamMemberEntity> = emptySet()
)

/**
 * 团队成员实体
 */
@Entity
@Table(name = "team_members")
data class TeamMemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "team_id", nullable = false)
    val teamId: String,
    
    @Column(name = "user_id", nullable = false)
    val userId: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: TeamMemberRole,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "invited_by", nullable = false)
    val invitedBy: String,
    
    @Column(name = "joined_at", nullable = false)
    val joinedAt: Instant,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
)

// 扩展函数：实体转领域对象

fun TimeEntryEntity.toDomain(): TimeEntry {
    return TimeEntry.reconstruct(
        timeEntryId = TimeEntryId(timeEntryId),
        userId = UserId(userId),
        projectId = projectId?.let { ProjectId(it) },
        taskId = taskId?.let { TaskId(it) },
        description = description,
        tags = tags,
        startTime = startTime,
        endTime = endTime,
        duration = durationMinutes?.let { Duration.ofMinutes(it) },
        billable = billable,
        hourlyRate = hourlyRate,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TimeEntry.toEntity(): TimeEntryEntity {
    return TimeEntryEntity(
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

fun ProjectEntity.toDomain(): Project {
    return Project.reconstruct(
        projectId = ProjectId(projectId),
        name = name,
        description = description,
        ownerId = UserId(ownerId),
        teamId = teamId?.let { TeamId(it) },
        clientName = clientName,
        hourlyRate = hourlyRate,
        estimatedHours = estimatedHours?.let { Duration.ofHours(it) },
        color = color,
        isActive = isActive,
        isArchived = isArchived,
        members = members.map { it.toDomain() }.toSet(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Project.toEntity(): ProjectEntity {
    return ProjectEntity(
        projectId = projectId.value,
        name = name,
        description = description,
        ownerId = ownerId.value,
        teamId = teamId?.value,
        clientName = clientName,
        hourlyRate = hourlyRate,
        estimatedHours = estimatedHours?.toHours(),
        color = color,
        isActive = isActive,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt,
        members = members.map { it.toEntity(projectId.value) }.toSet()
    )
}

fun ProjectMemberEntity.toDomain(): ProjectMember {
    return ProjectMember(
        userId = UserId(userId),
        role = role,
        hourlyRate = hourlyRate,
        isActive = isActive,
        joinedAt = joinedAt,
        updatedAt = updatedAt
    )
}

fun ProjectMember.toEntity(projectId: String): ProjectMemberEntity {
    return ProjectMemberEntity(
        projectId = projectId,
        userId = userId.value,
        role = role,
        hourlyRate = hourlyRate,
        isActive = isActive,
        joinedAt = joinedAt,
        updatedAt = updatedAt
    )
}

fun TaskEntity.toDomain(): Task {
    return Task.reconstruct(
        taskId = TaskId(taskId),
        name = name,
        description = description,
        projectId = ProjectId(projectId),
        assigneeId = assigneeId?.let { UserId(it) },
        creatorId = UserId(creatorId),
        status = status,
        priority = priority,
        estimatedDuration = estimatedDurationMinutes?.let { Duration.ofMinutes(it) },
        dueDate = dueDate,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        taskId = taskId.value,
        name = name,
        description = description,
        projectId = projectId.value,
        assigneeId = assigneeId?.value,
        creatorId = creatorId.value,
        status = status,
        priority = priority,
        estimatedDurationMinutes = estimatedDuration?.toMinutes(),
        dueDate = dueDate,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TeamEntity.toDomain(): Team {
    return Team.reconstruct(
        teamId = TeamId(teamId),
        name = name,
        description = description,
        ownerId = UserId(ownerId),
        members = members.map { it.toDomain() }.toSet(),
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Team.toEntity(): TeamEntity {
    return TeamEntity(
        teamId = teamId.value,
        name = name,
        description = description,
        ownerId = ownerId.value,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        members = members.map { it.toEntity(teamId.value) }.toSet()
    )
}

fun TeamMemberEntity.toDomain(): TeamMember {
    return TeamMember(
        userId = UserId(userId),
        role = role,
        isActive = isActive,
        invitedBy = UserId(invitedBy),
        joinedAt = joinedAt,
        updatedAt = updatedAt
    )
}

fun TeamMember.toEntity(teamId: String): TeamMemberEntity {
    return TeamMemberEntity(
        teamId = teamId,
        userId = userId.value,
        role = role,
        isActive = isActive,
        invitedBy = invitedBy.value,
        joinedAt = joinedAt,
        updatedAt = updatedAt
    )
}