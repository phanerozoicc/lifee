package com.lifee.time.application

import com.lifee.time.domain.*
import com.lifee.time.domain.service.*
import com.lifee.time.domain.repository.*
import com.lifee.time.domain.query.*
import com.lifee.user.domain.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * 时间追踪应用服务
 */
@Service
@Transactional
class TimeTrackingApplicationService(
    private val timeTrackingService: TimeTrackingService,
    private val projectService: ProjectService,
    private val taskService: TaskService,
    private val teamService: TeamService,
    private val reportService: ReportService,
    private val statisticsService: StatisticsService,
    private val timeEntryRepository: TimeEntryRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val teamRepository: TeamRepository
) {

    // 时间追踪相关操作

    /**
     * 开始时间追踪
     */
    suspend fun startTimeTracking(
        userId: UserId,
        projectId: ProjectId?,
        taskId: TaskId?,
        description: String?,
        tags: Set<String> = emptySet(),
        billable: Boolean = false
    ): TimeEntryId {
        // 停止当前正在运行的时间记录
        val currentEntry = timeTrackingService.getCurrentRunningEntry(userId)
        currentEntry?.let {
            timeTrackingService.stopTimeTracking(it.timeEntryId, Instant.now())
        }

        // 验证项目和任务的有效性
        projectId?.let { validateProjectAccess(userId, it) }
        taskId?.let { validateTaskAccess(userId, it) }

        return timeTrackingService.startTimeTracking(
            userId = userId,
            projectId = projectId,
            taskId = taskId,
            description = description,
            tags = tags,
            billable = billable
        )
    }

    /**
     * 停止时间追踪
     */
    suspend fun stopTimeTracking(timeEntryId: TimeEntryId, endTime: Instant? = null): TimeEntry {
        return timeTrackingService.stopTimeTracking(timeEntryId, endTime ?: Instant.now())
    }

    /**
     * 暂停时间追踪
     */
    suspend fun pauseTimeTracking(timeEntryId: TimeEntryId): TimeEntry {
        return timeTrackingService.pauseTimeTracking(timeEntryId)
    }

    /**
     * 恢复时间追踪
     */
    suspend fun resumeTimeTracking(timeEntryId: TimeEntryId): TimeEntry {
        return timeTrackingService.resumeTimeTracking(timeEntryId)
    }

    /**
     * 更新时间记录
     */
    suspend fun updateTimeEntry(
        timeEntryId: TimeEntryId,
        userId: UserId,
        description: String? = null,
        projectId: ProjectId? = null,
        taskId: TaskId? = null,
        tags: Set<String>? = null,
        billable: Boolean? = null,
        startTime: Instant? = null,
        endTime: Instant? = null
    ): TimeEntry {
        val timeEntry = timeEntryRepository.findById(timeEntryId)
            ?: throw IllegalArgumentException("Time entry not found: $timeEntryId")
        
        // 验证权限
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("No permission to update this time entry")
        }

        // 验证项目和任务的有效性
        projectId?.let { validateProjectAccess(userId, it) }
        taskId?.let { validateTaskAccess(userId, it) }

        var updatedEntry = timeEntry
        
        description?.let {
            updatedEntry = timeTrackingService.updateDescription(timeEntryId, it)
        }
        
        projectId?.let {
            updatedEntry = timeTrackingService.updateProject(timeEntryId, it)
        }
        
        tags?.let {
            updatedEntry = timeTrackingService.updateTags(timeEntryId, it)
        }
        
        billable?.let {
            updatedEntry = timeTrackingService.updateBillableStatus(timeEntryId, it)
        }

        // 更新时间范围
        if (startTime != null || endTime != null) {
            updatedEntry = updatedEntry.updateTimeRange(
                startTime ?: updatedEntry.startTime,
                endTime ?: updatedEntry.endTime
            )
            timeEntryRepository.save(updatedEntry)
        }

        return updatedEntry
    }

    /**
     * 删除时间记录
     */
    suspend fun deleteTimeEntry(timeEntryId: TimeEntryId, userId: UserId) {
        val timeEntry = timeEntryRepository.findById(timeEntryId)
            ?: throw IllegalArgumentException("Time entry not found: $timeEntryId")
        
        // 验证权限
        if (timeEntry.userId != userId) {
            throw IllegalArgumentException("No permission to delete this time entry")
        }

        timeTrackingService.deleteTimeEntry(timeEntryId)
    }

    /**
     * 获取用户当前运行的时间记录
     */
    suspend fun getCurrentRunningEntry(userId: UserId): TimeEntry? {
        return timeTrackingService.getCurrentRunningEntry(userId)
    }

    /**
     * 获取用户时间记录列表
     */
    suspend fun getUserTimeEntries(
        userId: UserId,
        projectId: ProjectId? = null,
        taskId: TaskId? = null,
        timeRange: TimeRange? = null,
        tags: Set<String>? = null,
        billable: Boolean? = null,
        page: Int = 0,
        size: Int = 20
    ): List<TimeEntry> {
        return when {
            projectId != null -> timeEntryRepository.findByProjectId(projectId)
            taskId != null -> timeEntryRepository.findByTaskId(taskId)
            timeRange != null -> timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
            tags != null -> timeEntryRepository.findByTags(tags)
            billable != null -> timeEntryRepository.findByBillable(billable)
            else -> timeEntryRepository.findByUserId(userId)
        }.filter { it.userId == userId }
         .drop(page * size)
         .take(size)
    }

    // 项目管理相关操作

    /**
     * 创建项目
     */
    suspend fun createProject(
        name: String,
        description: String?,
        ownerId: UserId,
        teamId: TeamId?,
        clientName: String?,
        hourlyRate: java.math.BigDecimal?,
        estimatedHours: Duration?,
        color: String?
    ): ProjectId {
        // 验证团队权限
        teamId?.let { validateTeamManagePermission(ownerId, it) }

        return projectService.createProject(
            name = name,
            description = description,
            ownerId = ownerId,
            teamId = teamId,
            clientName = clientName,
            hourlyRate = hourlyRate,
            estimatedHours = estimatedHours,
            color = color
        )
    }

    /**
     * 更新项目
     */
    suspend fun updateProject(
        projectId: ProjectId,
        userId: UserId,
        name: String? = null,
        description: String? = null,
        clientName: String? = null,
        hourlyRate: java.math.BigDecimal? = null,
        estimatedHours: Duration? = null,
        color: String? = null
    ): Project {
        validateProjectManagePermission(userId, projectId)
        
        return projectService.updateProject(
            projectId = projectId,
            name = name,
            description = description,
            clientName = clientName,
            hourlyRate = hourlyRate,
            estimatedHours = estimatedHours,
            color = color
        )
    }

    /**
     * 归档项目
     */
    suspend fun archiveProject(projectId: ProjectId, userId: UserId): Project {
        validateProjectManagePermission(userId, projectId)
        return projectService.archiveProject(projectId)
    }

    /**
     * 恢复项目
     */
    suspend fun restoreProject(projectId: ProjectId, userId: UserId): Project {
        validateProjectManagePermission(userId, projectId)
        return projectService.restoreProject(projectId)
    }

    /**
     * 删除项目
     */
    suspend fun deleteProject(projectId: ProjectId, userId: UserId) {
        validateProjectManagePermission(userId, projectId)
        projectService.deleteProject(projectId)
    }

    /**
     * 添加项目成员
     */
    suspend fun addProjectMember(
        projectId: ProjectId,
        userId: UserId,
        memberId: UserId,
        role: ProjectMemberRole,
        hourlyRate: java.math.BigDecimal?
    ): Project {
        validateProjectManagePermission(userId, projectId)
        return projectService.addMember(projectId, memberId, role, hourlyRate)
    }

    /**
     * 移除项目成员
     */
    suspend fun removeProjectMember(
        projectId: ProjectId,
        userId: UserId,
        memberId: UserId
    ): Project {
        validateProjectManagePermission(userId, projectId)
        return projectService.removeMember(projectId, memberId)
    }

    /**
     * 获取用户可访问的项目列表
     */
    suspend fun getUserProjects(
        userId: UserId,
        includeArchived: Boolean = false,
        teamId: TeamId? = null
    ): List<Project> {
        return when {
            teamId != null -> projectRepository.findByTeamId(teamId)
            includeArchived -> projectRepository.findByUserIdAccessible(userId)
            else -> projectRepository.findActiveByUserId(userId)
        }
    }

    // 任务管理相关操作

    /**
     * 创建任务
     */
    suspend fun createTask(
        name: String,
        description: String?,
        projectId: ProjectId,
        assigneeId: UserId?,
        creatorId: UserId,
        priority: TaskPriority = TaskPriority.MEDIUM,
        estimatedDuration: Duration?,
        dueDate: Instant?,
        tags: Set<String> = emptySet()
    ): TaskId {
        validateProjectAccess(creatorId, projectId)
        assigneeId?.let { validateProjectAccess(it, projectId) }

        return taskService.createTask(
            name = name,
            description = description,
            projectId = projectId,
            assigneeId = assigneeId,
            creatorId = creatorId,
            priority = priority,
            estimatedDuration = estimatedDuration,
            dueDate = dueDate,
            tags = tags
        )
    }

    /**
     * 更新任务
     */
    suspend fun updateTask(
        taskId: TaskId,
        userId: UserId,
        name: String? = null,
        description: String? = null,
        priority: TaskPriority? = null,
        estimatedDuration: Duration? = null,
        dueDate: Instant? = null,
        tags: Set<String>? = null
    ): Task {
        validateTaskAccess(userId, taskId)
        
        return taskService.updateTask(
            taskId = taskId,
            name = name,
            description = description,
            priority = priority,
            estimatedDuration = estimatedDuration,
            dueDate = dueDate,
            tags = tags
        )
    }

    /**
     * 分配任务
     */
    suspend fun assignTask(taskId: TaskId, userId: UserId, assigneeId: UserId): Task {
        validateTaskAccess(userId, taskId)
        val task = taskRepository.findById(taskId)!!
        validateProjectAccess(assigneeId, task.projectId)
        
        return taskService.assignTask(taskId, assigneeId)
    }

    /**
     * 开始任务
     */
    suspend fun startTask(taskId: TaskId, userId: UserId): Task {
        validateTaskAccess(userId, taskId)
        return taskService.startTask(taskId)
    }

    /**
     * 完成任务
     */
    suspend fun completeTask(taskId: TaskId, userId: UserId): Task {
        validateTaskAccess(userId, taskId)
        return taskService.completeTask(taskId)
    }

    /**
     * 获取用户任务列表
     */
    suspend fun getUserTasks(
        userId: UserId,
        projectId: ProjectId? = null,
        status: TaskStatus? = null,
        priority: TaskPriority? = null,
        includeCompleted: Boolean = false
    ): List<Task> {
        return when {
            projectId != null -> taskRepository.findByProjectId(projectId)
            status != null -> taskRepository.findByStatus(status)
            priority != null -> taskRepository.findByPriority(priority)
            else -> taskRepository.findByAssigneeId(userId)
        }.filter { task ->
            (task.assigneeId == userId || task.creatorId == userId) &&
            (includeCompleted || task.status != TaskStatus.COMPLETED)
        }
    }

    /**
     * 获取逾期任务
     */
    suspend fun getOverdueTasks(userId: UserId): List<Task> {
        return taskService.getOverdueTasks(userId)
    }

    // 团队管理相关操作

    /**
     * 创建团队
     */
    suspend fun createTeam(
        name: String,
        description: String?,
        ownerId: UserId
    ): TeamId {
        return teamService.createTeam(name, description, ownerId)
    }

    /**
     * 添加团队成员
     */
    suspend fun addTeamMember(
        teamId: TeamId,
        userId: UserId,
        memberId: UserId,
        role: TeamMemberRole
    ): Team {
        validateTeamManagePermission(userId, teamId)
        return teamService.addMember(teamId, memberId, role, userId)
    }

    /**
     * 获取用户团队列表
     */
    suspend fun getUserTeams(userId: UserId): List<Team> {
        return teamService.getUserTeams(userId)
    }

    // 报告和统计相关操作

    /**
     * 生成时间报告
     */
    suspend fun generateTimeReport(
        userId: UserId,
        query: ReportQuery
    ): TimeReport {
        // 验证权限
        query.projectIds?.forEach { validateProjectAccess(userId, it) }
        query.teamIds?.forEach { validateTeamAccess(userId, it) }
        
        return reportService.generateTimeReport(query)
    }

    /**
     * 生成项目报告
     */
    suspend fun generateProjectReport(
        userId: UserId,
        projectId: ProjectId,
        timeRange: TimeRange
    ): ProjectReport {
        validateProjectAccess(userId, projectId)
        
        val query = ReportQuery(
            projectIds = setOf(projectId),
            timeRange = timeRange
        )
        
        return reportService.generateProjectReport(query)
    }

    /**
     * 生成团队报告
     */
    suspend fun generateTeamReport(
        userId: UserId,
        teamId: TeamId,
        timeRange: TimeRange
    ): TeamReport {
        validateTeamAccess(userId, teamId)
        
        val query = ReportQuery(
            teamIds = setOf(teamId),
            timeRange = timeRange
        )
        
        return reportService.generateTeamReport(query)
    }

    /**
     * 获取用户统计信息
     */
    suspend fun getUserStatistics(
        userId: UserId,
        timeRange: TimeRange
    ): UserStatistics {
        return statisticsService.getUserStatistics(userId, timeRange)
    }

    /**
     * 获取项目统计信息
     */
    suspend fun getProjectStatistics(
        userId: UserId,
        projectId: ProjectId,
        timeRange: TimeRange
    ): ProjectStatistics {
        validateProjectAccess(userId, projectId)
        return statisticsService.getProjectStatistics(projectId, timeRange)
    }

    /**
     * 获取生产力趋势
     */
    suspend fun getProductivityTrend(
        userId: UserId,
        days: Int = 30
    ): ProductivityTrend {
        return statisticsService.getProductivityTrend(userId, days)
    }

    /**
     * 获取时间分布
     */
    suspend fun getTimeDistribution(
        userId: UserId,
        timeRange: TimeRange
    ): TimeDistribution {
        return statisticsService.getTimeDistribution(userId, timeRange)
    }

    // 私有辅助方法

    private suspend fun validateProjectAccess(userId: UserId, projectId: ProjectId) {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("Project not found: $projectId")
        
        if (!project.canUserAccess(userId)) {
            throw IllegalArgumentException("No access to project: $projectId")
        }
    }

    private suspend fun validateProjectManagePermission(userId: UserId, projectId: ProjectId) {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("Project not found: $projectId")
        
        if (!project.canUserManage(userId)) {
            throw IllegalArgumentException("No permission to manage project: $projectId")
        }
    }

    private suspend fun validateTaskAccess(userId: UserId, taskId: TaskId) {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("Task not found: $taskId")
        
        // 检查是否是任务的分配者或创建者
        if (task.assigneeId != userId && task.creatorId != userId) {
            // 检查是否有项目访问权限
            validateProjectAccess(userId, task.projectId)
        }
    }

    private suspend fun validateTeamAccess(userId: UserId, teamId: TeamId) {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("Team not found: $teamId")
        
        if (!team.isMember(userId)) {
            throw IllegalArgumentException("No access to team: $teamId")
        }
    }

    private suspend fun validateTeamManagePermission(userId: UserId, teamId: TeamId) {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("Team not found: $teamId")
        
        if (!team.canManageMembers(userId)) {
            throw IllegalArgumentException("No permission to manage team: $teamId")
        }
    }
}