package com.lifee.time.web.dto

import com.lifee.time.domain.TaskPriority
import com.lifee.time.domain.TaskStatus
import com.lifee.time.domain.TeamMemberRole
import com.lifee.time.domain.TimeEntryStatus
import java.math.BigDecimal
import java.time.Instant

/**
 * 时间追踪相关DTO
 */

// 时间记录相关DTO
data class StartTimeTrackingRequest(
    val projectId: String?,
    val taskId: String?,
    val description: String?,
    val tags: Set<String>?,
    val billable: Boolean?,
    val hourlyRate: BigDecimal?
)

data class UpdateDescriptionRequest(
    val description: String?
)

data class UpdateProjectRequest(
    val projectId: String?
)

data class UpdateTagsRequest(
    val tags: Set<String>
)

data class UpdateBillableRequest(
    val billable: Boolean,
    val hourlyRate: BigDecimal?
)

data class TimeEntryResponse(
    val timeEntryId: String,
    val userId: String,
    val projectId: String?,
    val taskId: String?,
    val description: String?,
    val tags: Set<String>,
    val startTime: Instant,
    val endTime: Instant?,
    val durationMinutes: Long?,
    val billable: Boolean,
    val hourlyRate: BigDecimal?,
    val status: TimeEntryStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class DurationResponse(
    val durationMinutes: Long
)

// 项目相关DTO
data class CreateProjectRequest(
    val name: String,
    val description: String?,
    val teamId: String?,
    val clientName: String?,
    val hourlyRate: BigDecimal?,
    val estimatedHours: Long?,
    val color: String?
)

data class UpdateProjectRequest(
    val name: String?,
    val description: String?,
    val clientName: String?,
    val hourlyRate: BigDecimal?,
    val estimatedHours: Long?,
    val color: String?
)

data class AddProjectMemberRequest(
    val userId: String,
    val role: String,
    val hourlyRate: BigDecimal?
)

data class UpdateProjectMemberRoleRequest(
    val role: String,
    val hourlyRate: BigDecimal?
)

data class ProjectResponse(
    val projectId: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val teamId: String?,
    val clientName: String?,
    val hourlyRate: BigDecimal?,
    val estimatedHours: Long?,
    val color: String?,
    val isActive: Boolean,
    val isArchived: Boolean,
    val members: List<ProjectMemberResponse>,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ProjectMemberResponse(
    val userId: String,
    val role: String,
    val hourlyRate: BigDecimal?,
    val isActive: Boolean,
    val joinedAt: Instant,
    val updatedAt: Instant
)

data class ProjectStatisticsResponse(
    val totalProjects: Long,
    val activeProjects: Long,
    val archivedProjects: Long,
    val totalTimeSpent: Long,
    val totalRevenue: BigDecimal?
)

// 任务相关DTO
data class CreateTaskRequest(
    val name: String,
    val description: String?,
    val projectId: String,
    val assigneeId: String?,
    val priority: TaskPriority,
    val estimatedDurationMinutes: Long?,
    val dueDate: Instant?,
    val tags: Set<String>?
)

data class UpdateTaskRequest(
    val name: String?,
    val description: String?,
    val assigneeId: String?,
    val priority: TaskPriority?,
    val estimatedDurationMinutes: Long?,
    val dueDate: Instant?,
    val tags: Set<String>?
)

data class AssignTaskRequest(
    val assigneeId: String
)

data class UpdateTaskStatusRequest(
    val status: TaskStatus
)

data class BatchUpdateTaskStatusRequest(
    val taskIds: List<String>,
    val status: TaskStatus
)

data class TaskResponse(
    val taskId: String,
    val name: String,
    val description: String?,
    val projectId: String,
    val assigneeId: String?,
    val creatorId: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val estimatedDurationMinutes: Long?,
    val dueDate: Instant?,
    val tags: Set<String>,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class TaskStatisticsResponse(
    val totalTasks: Long,
    val todoTasks: Long,
    val inProgressTasks: Long,
    val completedTasks: Long,
    val overdueTasks: Long
)

// 团队相关DTO
data class CreateTeamRequest(
    val name: String,
    val description: String?
)

data class UpdateTeamRequest(
    val name: String?,
    val description: String?
)

data class AddTeamMemberRequest(
    val userId: String,
    val role: TeamMemberRole
)

data class UpdateTeamMemberRoleRequest(
    val role: TeamMemberRole
)

data class BatchAddTeamMembersRequest(
    val members: List<TeamMemberRequest>
)

data class TeamMemberRequest(
    val userId: String,
    val role: TeamMemberRole
)

data class TransferTeamOwnershipRequest(
    val newOwnerId: String
)

data class TeamResponse(
    val teamId: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val members: List<TeamMemberResponse>,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class TeamMemberResponse(
    val userId: String,
    val role: TeamMemberRole,
    val isActive: Boolean,
    val invitedBy: String,
    val joinedAt: Instant,
    val updatedAt: Instant
)

data class TeamStatisticsResponse(
    val totalTeams: Long,
    val activeTeams: Long,
    val totalMembers: Long,
    val totalProjects: Long
)

// 报告相关DTO
data class GenerateReportRequest(
    val reportType: String, // TIME, PROJECT, TEAM, USER
    val startDate: Instant?,
    val endDate: Instant?,
    val projectIds: List<String>?,
    val teamIds: List<String>?,
    val userIds: List<String>?,
    val tags: Set<String>?,
    val billableOnly: Boolean?,
    val groupBy: String? // DAY, WEEK, MONTH, PROJECT, USER
)

data class TimeReportResponse(
    val summary: TimeSummaryResponse,
    val details: List<TimeReportDetailResponse>,
    val chartData: List<ChartDataPointResponse>
)

data class TimeSummaryResponse(
    val totalDuration: Long,
    val billableDuration: Long,
    val nonBillableDuration: Long,
    val totalRevenue: BigDecimal?,
    val averageDailyHours: Double,
    val totalEntries: Long
)

data class TimeReportDetailResponse(
    val date: String,
    val projectName: String?,
    val taskName: String?,
    val description: String?,
    val duration: Long,
    val billable: Boolean,
    val revenue: BigDecimal?
)

data class ChartDataPointResponse(
    val label: String,
    val value: Double,
    val color: String?
)

data class ProjectReportResponse(
    val summary: ProjectSummaryResponse,
    val memberReports: List<MemberReportResponse>,
    val taskReports: List<TaskReportResponse>,
    val dailyBreakdown: List<DailyBreakdownResponse>
)

data class ProjectSummaryResponse(
    val projectId: String,
    val projectName: String,
    val totalDuration: Long,
    val billableDuration: Long,
    val totalRevenue: BigDecimal?,
    val completionPercentage: Double,
    val totalTasks: Long,
    val completedTasks: Long
)

data class MemberReportResponse(
    val userId: String,
    val userName: String?,
    val totalDuration: Long,
    val billableDuration: Long,
    val totalRevenue: BigDecimal?,
    val tasksCompleted: Long
)

data class TaskReportResponse(
    val taskId: String,
    val taskName: String,
    val status: TaskStatus,
    val totalDuration: Long,
    val estimatedDuration: Long?,
    val completionPercentage: Double
)

data class DailyBreakdownResponse(
    val date: String,
    val duration: Long,
    val entries: Long
)

// 统计相关DTO
data class UserStatisticsResponse(
    val totalTimeTracked: Long,
    val totalProjects: Long,
    val totalTasks: Long,
    val completedTasks: Long,
    val averageDailyHours: Double,
    val totalRevenue: BigDecimal?,
    val dailyProgress: List<DailyProgressResponse>
)

data class DailyProgressResponse(
    val date: String,
    val duration: Long,
    val tasksCompleted: Long
)

data class ProductivityTrendResponse(
    val period: String,
    val data: List<DailyProductivityResponse>
)

data class DailyProductivityResponse(
    val date: String,
    val hoursWorked: Double,
    val tasksCompleted: Long,
    val efficiency: Double
)

// 通用响应
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val errors: List<String>?
)

data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)