package com.lifee.time.domain.service

import com.lifee.time.domain.*
import com.lifee.time.domain.query.*
import com.lifee.time.domain.repository.*
import com.lifee.user.domain.UserId
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 报告分析服务
 */
@Service
class ReportService(
    private val timeEntryRepository: TimeEntryRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val teamRepository: TeamRepository
) {

    /**
     * 生成时间报告
     */
    suspend fun generateTimeReport(query: ReportQuery): TimeReport {
        val timeEntries = findTimeEntriesByQuery(query)
        val summary = calculateTimeSummary(timeEntries, query)
        val details = generateTimeReportDetails(timeEntries)
        val charts = generateChartData(timeEntries, query)
        
        return TimeReport(
            query = query,
            summary = summary,
            details = details,
            charts = charts
        )
    }

    /**
     * 生成项目报告
     */
    suspend fun generateProjectReport(projectId: ProjectId, timeRange: TimeRange): ProjectReport {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("Project not found: $projectId")
        
        val timeEntries = timeEntryRepository.findByProjectIdAndTimeRange(projectId, timeRange)
        val tasks = taskRepository.findByProjectId(projectId)
        val projectMembers = project.members
        
        val summary = calculateProjectSummary(timeEntries, tasks, project)
        val memberReports = generateMemberReports(timeEntries, projectMembers)
        val taskReports = generateTaskReports(tasks, timeEntries)
        val dailyBreakdown = generateDailyBreakdown(timeEntries, timeRange)
        
        return ProjectReport(
            projectId = projectId,
            projectName = project.name,
            timeRange = timeRange,
            summary = summary,
            memberReports = memberReports,
            taskReports = taskReports,
            dailyBreakdown = dailyBreakdown
        )
    }

    /**
     * 生成团队报告
     */
    suspend fun generateTeamReport(teamId: TeamId, timeRange: TimeRange): TeamReport {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("Team not found: $teamId")
        
        val projects = projectRepository.findByTeamId(teamId)
        val timeEntries = projects.flatMap { project ->
            timeEntryRepository.findByProjectIdAndTimeRange(project.projectId, timeRange)
        }
        
        val summary = calculateTeamSummary(timeEntries, team)
        val memberReports = generateTeamMemberReports(timeEntries, team.members)
        val projectReports = generateTeamProjectReports(projects, timeEntries)
        val productivityMetrics = calculateProductivityMetrics(timeEntries, team, timeRange)
        
        return TeamReport(
            teamId = teamId,
            teamName = team.name,
            timeRange = timeRange,
            summary = summary,
            memberReports = memberReports,
            projectReports = projectReports,
            productivityMetrics = productivityMetrics
        )
    }

    /**
     * 生成用户报告
     */
    suspend fun generateUserReport(userId: UserId, timeRange: TimeRange): UserReport {
        val timeEntries = timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
        val projects = timeEntries.mapNotNull { it.projectId }.distinct()
            .mapNotNull { projectRepository.findById(it) }
        val tasks = timeEntries.mapNotNull { it.taskId }.distinct()
            .mapNotNull { taskRepository.findById(it) }
        
        val summary = calculateUserSummary(timeEntries, projects, tasks, timeRange)
        val projectBreakdown = generateProjectBreakdown(timeEntries, projects)
        val dailyActivity = generateDailyActivity(timeEntries, timeRange)
        val goals = calculateUserGoals(userId, timeEntries, timeRange)
        
        return UserReport(
            userId = userId,
            userName = null, // 需要从用户服务获取
            timeRange = timeRange,
            summary = summary,
            projectBreakdown = projectBreakdown,
            dailyActivity = dailyActivity,
            goals = goals
        )
    }

    /**
     * 生成时间追踪分析
     */
    suspend fun generateTimeTrackingAnalysis(userId: UserId, timeRange: TimeRange): TimeTrackingAnalysis {
        val timeEntries = timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
        
        val patterns = analyzeTimePatterns(timeEntries)
        val insights = generateInsights(timeEntries, timeRange)
        val recommendations = generateRecommendations(patterns, insights)
        
        return TimeTrackingAnalysis(
            timeRange = timeRange,
            patterns = patterns,
            insights = insights,
            recommendations = recommendations
        )
    }

    /**
     * 获取生产力趋势
     */
    suspend fun getProductivityTrend(userId: UserId, days: Int): List<ChartDataPoint> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        val timeRange = TimeRange.of(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay())
        
        val timeEntries = timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
        
        return (0 until days).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            val dayEntries = timeEntries.filter { 
                it.startTime.toLocalDate() == date 
            }
            val totalHours = dayEntries.sumOf { it.duration?.toHours() ?: 0.0 }
            
            ChartDataPoint(
                label = date.toString(),
                value = totalHours
            )
        }
    }

    /**
     * 获取项目时间分布
     */
    suspend fun getProjectTimeDistribution(userId: UserId, timeRange: TimeRange): List<ChartDataPoint> {
        val timeEntries = timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
        val projectTimes = timeEntries.groupBy { it.projectId }
            .mapValues { (_, entries) -> 
                entries.sumOf { it.duration?.toHours() ?: 0.0 }
            }
        
        val totalHours = projectTimes.values.sum()
        
        return projectTimes.map { (projectId, hours) ->
            val projectName = projectId?.let { projectRepository.findById(it)?.name } ?: "无项目"
            val percentage = if (totalHours > 0) (hours / totalHours) * 100 else 0.0
            
            ChartDataPoint(
                label = projectName,
                value = percentage,
                metadata = mapOf(
                    "hours" to hours,
                    "projectId" to (projectId?.value ?: "")
                )
            )
        }.sortedByDescending { it.value }
    }

    // 私有辅助方法

    private suspend fun findTimeEntriesByQuery(query: ReportQuery): List<TimeEntry> {
        return when {
            query.userId != null -> timeEntryRepository.findByUserIdAndTimeRange(query.userId, query.timeRange)
            query.projectId != null -> timeEntryRepository.findByProjectIdAndTimeRange(query.projectId, query.timeRange)
            query.teamId != null -> {
                val projects = projectRepository.findByTeamId(query.teamId)
                projects.flatMap { project ->
                    timeEntryRepository.findByProjectIdAndTimeRange(project.projectId, query.timeRange)
                }
            }
            else -> timeEntryRepository.findByTimeRange(query.timeRange)
        }.filter { entry ->
            (query.tags.isEmpty() || entry.tags.any { it in query.tags }) &&
            (!query.billableOnly || entry.billable)
        }
    }

    private fun calculateTimeSummary(timeEntries: List<TimeEntry>, query: ReportQuery): TimeSummary {
        val totalDuration = timeEntries.sumOf { it.duration?.toMinutes() ?: 0L }
        val billableEntries = timeEntries.filter { it.billable }
        val billableDuration = billableEntries.sumOf { it.duration?.toMinutes() ?: 0L }
        
        val totalRevenue = billableEntries.sumOf { entry ->
            val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
            val rate = entry.hourlyRate ?: BigDecimal.ZERO
            rate.multiply(BigDecimal.valueOf(hours))
        }
        
        val workingDays = timeEntries.map { it.startTime.toLocalDate() }.distinct().size
        val averageDailyHours = if (workingDays > 0) totalDuration / 60.0 / workingDays else 0.0
        
        return TimeSummary(
            totalDuration = Duration.ofMinutes(totalDuration),
            billableDuration = Duration.ofMinutes(billableDuration),
            nonBillableDuration = Duration.ofMinutes(totalDuration - billableDuration),
            totalRevenue = totalRevenue,
            averageDailyHours = averageDailyHours,
            workingDays = workingDays,
            projectCount = timeEntries.mapNotNull { it.projectId }.distinct().size,
            taskCount = timeEntries.mapNotNull { it.taskId }.distinct().size,
            timeEntryCount = timeEntries.size
        )
    }

    private fun generateTimeReportDetails(timeEntries: List<TimeEntry>): List<TimeReportDetail> {
        return timeEntries.map { entry ->
            val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
            val revenue = if (entry.billable && entry.hourlyRate != null) {
                entry.hourlyRate.multiply(BigDecimal.valueOf(hours))
            } else null
            
            TimeReportDetail(
                date = entry.startTime.toLocalDate(),
                projectId = entry.projectId,
                projectName = null, // 需要关联查询
                taskId = entry.taskId,
                taskName = null, // 需要关联查询
                userId = entry.userId,
                userName = null, // 需要关联查询
                description = entry.description,
                duration = entry.duration ?: Duration.ZERO,
                billable = entry.billable,
                hourlyRate = entry.hourlyRate,
                revenue = revenue,
                tags = entry.tags
            )
        }
    }

    private fun generateChartData(timeEntries: List<TimeEntry>, query: ReportQuery): List<ChartData> {
        val charts = mutableListOf<ChartData>()
        
        // 时间趋势图
        when (query.groupBy) {
            ReportQuery.GroupBy.DAY -> {
                val dailyData = timeEntries.groupBy { it.startTime.toLocalDate() }
                    .map { (date, entries) ->
                        ChartDataPoint(
                            label = date.toString(),
                            value = entries.sumOf { it.duration?.toHours() ?: 0.0 }
                        )
                    }.sortedBy { it.label }
                
                charts.add(ChartData(
                    type = ChartData.ChartType.LINE,
                    title = "每日时间趋势",
                    data = dailyData
                ))
            }
            ReportQuery.GroupBy.PROJECT -> {
                val projectData = timeEntries.groupBy { it.projectId }
                    .map { (projectId, entries) ->
                        ChartDataPoint(
                            label = projectId?.value ?: "无项目",
                            value = entries.sumOf { it.duration?.toHours() ?: 0.0 }
                        )
                    }.sortedByDescending { it.value }
                
                charts.add(ChartData(
                    type = ChartData.ChartType.PIE,
                    title = "项目时间分布",
                    data = projectData
                ))
            }
            else -> {}
        }
        
        return charts
    }

    private fun calculateProjectSummary(timeEntries: List<TimeEntry>, tasks: List<Task>, project: Project): ProjectSummary {
        val totalDuration = timeEntries.sumOf { it.duration?.toMinutes() ?: 0L }
        val billableDuration = timeEntries.filter { it.billable }.sumOf { it.duration?.toMinutes() ?: 0L }
        
        val totalRevenue = timeEntries.filter { it.billable }.sumOf { entry ->
            val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
            val rate = entry.hourlyRate ?: BigDecimal.ZERO
            rate.multiply(BigDecimal.valueOf(hours))
        }
        
        val activeTasks = tasks.filter { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
        val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }
        
        val averageTaskDuration = if (completedTasks.isNotEmpty()) {
            val totalTaskTime = completedTasks.mapNotNull { task ->
                timeEntries.filter { it.taskId == task.taskId }
                    .sumOf { it.duration?.toMinutes() ?: 0L }
            }.sum()
            Duration.ofMinutes(totalTaskTime / completedTasks.size)
        } else null
        
        return ProjectSummary(
            totalDuration = Duration.ofMinutes(totalDuration),
            billableDuration = Duration.ofMinutes(billableDuration),
            totalRevenue = totalRevenue,
            budgetUsed = null, // 需要预算信息
            memberCount = project.members.size,
            activeTaskCount = activeTasks.size,
            completedTaskCount = completedTasks.size,
            averageTaskDuration = averageTaskDuration
        )
    }

    private fun generateMemberReports(timeEntries: List<TimeEntry>, members: Set<ProjectMember>): List<MemberReport> {
        return members.map { member ->
            val memberEntries = timeEntries.filter { it.userId == member.userId }
            val totalDuration = memberEntries.sumOf { it.duration?.toMinutes() ?: 0L }
            val billableDuration = memberEntries.filter { it.billable }.sumOf { it.duration?.toMinutes() ?: 0L }
            
            val revenue = memberEntries.filter { it.billable }.sumOf { entry ->
                val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
                val rate = entry.hourlyRate ?: BigDecimal.ZERO
                rate.multiply(BigDecimal.valueOf(hours))
            }
            
            val workingDays = memberEntries.map { it.startTime.toLocalDate() }.distinct().size
            val averageDailyHours = if (workingDays > 0) totalDuration / 60.0 / workingDays else 0.0
            
            MemberReport(
                userId = member.userId,
                userName = null, // 需要从用户服务获取
                totalDuration = Duration.ofMinutes(totalDuration),
                billableDuration = Duration.ofMinutes(billableDuration),
                revenue = revenue,
                taskCount = memberEntries.mapNotNull { it.taskId }.distinct().size,
                completedTaskCount = 0, // 需要任务状态信息
                averageDailyHours = averageDailyHours
            )
        }
    }

    private fun generateTaskReports(tasks: List<Task>, timeEntries: List<TimeEntry>): List<TaskReport> {
        return tasks.map { task ->
            val taskEntries = timeEntries.filter { it.taskId == task.taskId }
            val totalDuration = taskEntries.sumOf { it.duration?.toMinutes() ?: 0L }
            
            val completionRate = if (task.estimatedDuration != null) {
                val estimatedMinutes = task.estimatedDuration.toMinutes()
                if (estimatedMinutes > 0) {
                    (totalDuration.toDouble() / estimatedMinutes * 100).coerceAtMost(100.0)
                } else null
            } else null
            
            TaskReport(
                taskId = task.taskId,
                taskName = task.name,
                status = task.status,
                assigneeId = task.assigneeId,
                assigneeName = null, // 需要从用户服务获取
                totalDuration = Duration.ofMinutes(totalDuration),
                estimatedDuration = task.estimatedDuration,
                completionRate = completionRate,
                isOverdue = task.dueDate?.let { it.isBefore(java.time.Instant.now()) } ?: false,
                dueDate = task.dueDate
            )
        }
    }

    private fun generateDailyBreakdown(timeEntries: List<TimeEntry>, timeRange: TimeRange): List<DailyBreakdown> {
        val startDate = timeRange.startTime.toLocalDate()
        val endDate = timeRange.endTime.toLocalDate()
        
        return generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .map { date ->
                val dayEntries = timeEntries.filter { it.startTime.toLocalDate() == date }
                val totalDuration = dayEntries.sumOf { it.duration?.toMinutes() ?: 0L }
                val billableDuration = dayEntries.filter { it.billable }.sumOf { it.duration?.toMinutes() ?: 0L }
                
                val revenue = dayEntries.filter { it.billable }.sumOf { entry ->
                    val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
                    val rate = entry.hourlyRate ?: BigDecimal.ZERO
                    rate.multiply(BigDecimal.valueOf(hours))
                }
                
                DailyBreakdown(
                    date = date,
                    totalDuration = Duration.ofMinutes(totalDuration),
                    billableDuration = Duration.ofMinutes(billableDuration),
                    revenue = revenue,
                    memberCount = dayEntries.map { it.userId }.distinct().size,
                    taskCount = dayEntries.mapNotNull { it.taskId }.distinct().size
                )
            }.toList()
    }

    private fun calculateTeamSummary(timeEntries: List<TimeEntry>, team: Team): TeamSummary {
        val totalDuration = timeEntries.sumOf { it.duration?.toMinutes() ?: 0L }
        val billableDuration = timeEntries.filter { it.billable }.sumOf { it.duration?.toMinutes() ?: 0L }
        
        val totalRevenue = timeEntries.filter { it.billable }.sumOf { entry ->
            val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
            val rate = entry.hourlyRate ?: BigDecimal.ZERO
            rate.multiply(BigDecimal.valueOf(hours))
        }
        
        return TeamSummary(
            totalDuration = Duration.ofMinutes(totalDuration),
            billableDuration = Duration.ofMinutes(billableDuration),
            totalRevenue = totalRevenue,
            memberCount = team.members.size,
            activeProjectCount = 0, // 需要项目状态信息
            completedTaskCount = 0, // 需要任务状态信息
            averageTeamProductivity = if (team.members.isNotEmpty()) totalDuration / 60.0 / team.members.size else 0.0
        )
    }

    private fun generateTeamMemberReports(timeEntries: List<TimeEntry>, members: Set<TeamMember>): List<MemberReport> {
        return members.map { member ->
            val memberEntries = timeEntries.filter { it.userId == member.userId }
            val totalDuration = memberEntries.sumOf { it.duration?.toMinutes() ?: 0L }
            val billableDuration = memberEntries.filter { it.billable }.sumOf { it.duration?.toMinutes() ?: 0L }
            
            val revenue = memberEntries.filter { it.billable }.sumOf { entry ->
                val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
                val rate = entry.hourlyRate ?: BigDecimal.ZERO
                rate.multiply(BigDecimal.valueOf(hours))
            }
            
            val workingDays = memberEntries.map { it.startTime.toLocalDate() }.distinct().size
            val averageDailyHours = if (workingDays > 0) totalDuration / 60.0 / workingDays else 0.0
            
            MemberReport(
                userId = member.userId,
                userName = null, // 需要从用户服务获取
                totalDuration = Duration.ofMinutes(totalDuration),
                billableDuration = Duration.ofMinutes(billableDuration),
                revenue = revenue,
                taskCount = memberEntries.mapNotNull { it.taskId }.distinct().size,
                completedTaskCount = 0, // 需要任务状态信息
                averageDailyHours = averageDailyHours
            )
        }
    }

    private fun generateTeamProjectReports(projects: List<Project>, timeEntries: List<TimeEntry>): List<ProjectSummary> {
        return projects.map { project ->
            val projectEntries = timeEntries.filter { it.projectId == project.projectId }
            val totalDuration = projectEntries.sumOf { it.duration?.toMinutes() ?: 0L }
            val billableDuration = projectEntries.filter { it.billable }.sumOf { it.duration?.toMinutes() ?: 0L }
            
            val totalRevenue = projectEntries.filter { it.billable }.sumOf { entry ->
                val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
                val rate = entry.hourlyRate ?: BigDecimal.ZERO
                rate.multiply(BigDecimal.valueOf(hours))
            }
            
            ProjectSummary(
                totalDuration = Duration.ofMinutes(totalDuration),
                billableDuration = Duration.ofMinutes(billableDuration),
                totalRevenue = totalRevenue,
                budgetUsed = null,
                memberCount = project.members.size,
                activeTaskCount = 0, // 需要任务状态信息
                completedTaskCount = 0, // 需要任务状态信息
                averageTaskDuration = null
            )
        }
    }

    private fun calculateProductivityMetrics(timeEntries: List<TimeEntry>, team: Team, timeRange: TimeRange): ProductivityMetrics {
        val totalDays = ChronoUnit.DAYS.between(timeRange.startTime.toLocalDate(), timeRange.endTime.toLocalDate())
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        val billableHours = timeEntries.filter { it.billable }.sumOf { it.duration?.toHours() ?: 0.0 }
        
        val averageHoursPerDay = if (totalDays > 0) totalHours / totalDays else 0.0
        val billableRatio = if (totalHours > 0) billableHours / totalHours else 0.0
        
        return ProductivityMetrics(
            averageHoursPerDay = averageHoursPerDay,
            billableRatio = billableRatio,
            taskCompletionRate = 0.0, // 需要任务完成信息
            projectDeliveryRate = 0.0, // 需要项目交付信息
            memberUtilizationRate = if (team.members.isNotEmpty()) totalHours / (team.members.size * totalDays * 8) else 0.0,
            overallEfficiency = (billableRatio + 0.0 + 0.0) / 3 // 简化计算
        )
    }

    private fun calculateUserSummary(timeEntries: List<TimeEntry>, projects: List<Project>, tasks: List<Task>, timeRange: TimeRange): UserSummary {
        val totalDuration = timeEntries.sumOf { it.duration?.toMinutes() ?: 0L }
        val billableDuration = timeEntries.filter { it.billable }.sumOf { it.duration?.toMinutes() ?: 0L }
        
        val totalRevenue = timeEntries.filter { it.billable }.sumOf { entry ->
            val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
            val rate = entry.hourlyRate ?: BigDecimal.ZERO
            rate.multiply(BigDecimal.valueOf(hours))
        }
        
        val workingDays = timeEntries.map { it.startTime.toLocalDate() }.distinct().size
        val averageDailyHours = if (workingDays > 0) totalDuration / 60.0 / workingDays else 0.0
        
        val dailyHours = timeEntries.groupBy { it.startTime.toLocalDate() }
            .mapValues { (_, entries) -> entries.sumOf { it.duration?.toHours() ?: 0.0 } }
        val mostProductiveDay = dailyHours.maxByOrNull { it.value }?.key
        
        val longestSession = timeEntries.maxOfOrNull { it.duration ?: Duration.ZERO }
        
        return UserSummary(
            totalDuration = Duration.ofMinutes(totalDuration),
            billableDuration = Duration.ofMinutes(billableDuration),
            totalRevenue = totalRevenue,
            projectCount = projects.size,
            taskCount = tasks.size,
            completedTaskCount = tasks.count { it.status == TaskStatus.COMPLETED },
            averageDailyHours = averageDailyHours,
            mostProductiveDay = mostProductiveDay,
            longestSession = longestSession
        )
    }

    private fun generateProjectBreakdown(timeEntries: List<TimeEntry>, projects: List<Project>): List<ProjectBreakdown> {
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        
        return projects.map { project ->
            val projectEntries = timeEntries.filter { it.projectId == project.projectId }
            val duration = projectEntries.sumOf { it.duration?.toMinutes() ?: 0L }
            val hours = duration / 60.0
            val percentage = if (totalHours > 0) (hours / totalHours) * 100 else 0.0
            
            val revenue = projectEntries.filter { it.billable }.sumOf { entry ->
                val entryHours = (entry.duration?.toMinutes() ?: 0L) / 60.0
                val rate = entry.hourlyRate ?: BigDecimal.ZERO
                rate.multiply(BigDecimal.valueOf(entryHours))
            }
            
            ProjectBreakdown(
                projectId = project.projectId,
                projectName = project.name,
                duration = Duration.ofMinutes(duration),
                percentage = percentage,
                revenue = revenue,
                taskCount = projectEntries.mapNotNull { it.taskId }.distinct().size
            )
        }.sortedByDescending { it.percentage }
    }

    private fun generateDailyActivity(timeEntries: List<TimeEntry>, timeRange: TimeRange): List<DailyActivity> {
        val startDate = timeRange.startTime.toLocalDate()
        val endDate = timeRange.endTime.toLocalDate()
        
        return generateSequence(startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(endDate) }
            .map { date ->
                val dayEntries = timeEntries.filter { it.startTime.toLocalDate() == date }
                val duration = dayEntries.sumOf { it.duration?.toMinutes() ?: 0L }
                
                DailyActivity(
                    date = date,
                    duration = Duration.ofMinutes(duration),
                    sessionCount = dayEntries.size,
                    projectCount = dayEntries.mapNotNull { it.projectId }.distinct().size,
                    taskCount = dayEntries.mapNotNull { it.taskId }.distinct().size,
                    firstActivity = dayEntries.minOfOrNull { it.startTime },
                    lastActivity = dayEntries.maxOfOrNull { it.endTime ?: it.startTime }
                )
            }.toList()
    }

    private fun calculateUserGoals(userId: UserId, timeEntries: List<TimeEntry>, timeRange: TimeRange): UserGoals? {
        // 这里需要从用户设置中获取目标，暂时返回null
        return null
    }

    private fun analyzeTimePatterns(timeEntries: List<TimeEntry>): List<TimePattern> {
        val patterns = mutableListOf<TimePattern>()
        
        // 分析工作时间模式
        val hourlyDistribution = timeEntries.groupBy { it.startTime.hour }
        val peakHour = hourlyDistribution.maxByOrNull { it.value.size }?.key
        
        if (peakHour != null) {
            patterns.add(TimePattern(
                type = TimePattern.PatternType.PEAK_HOURS,
                description = "高峰工作时间在 $peakHour:00",
                frequency = hourlyDistribution[peakHour]?.size?.toDouble() ?: 0.0,
                impact = TimePattern.PatternImpact.POSITIVE
            ))
        }
        
        // 分析长时间工作会话
        val longSessions = timeEntries.filter { (it.duration?.toHours() ?: 0.0) > 4 }
        if (longSessions.isNotEmpty()) {
            patterns.add(TimePattern(
                type = TimePattern.PatternType.LONG_SESSIONS,
                description = "发现 ${longSessions.size} 个超过4小时的工作会话",
                frequency = longSessions.size.toDouble(),
                impact = TimePattern.PatternImpact.NEGATIVE
            ))
        }
        
        // 分析周末工作
        val weekendWork = timeEntries.filter { 
            val dayOfWeek = it.startTime.dayOfWeek.value
            dayOfWeek == 6 || dayOfWeek == 7
        }
        if (weekendWork.isNotEmpty()) {
            patterns.add(TimePattern(
                type = TimePattern.PatternType.WEEKEND_WORK,
                description = "周末工作 ${weekendWork.size} 次",
                frequency = weekendWork.size.toDouble(),
                impact = TimePattern.PatternImpact.NEGATIVE
            ))
        }
        
        return patterns
    }

    private fun generateInsights(timeEntries: List<TimeEntry>, timeRange: TimeRange): List<Insight> {
        val insights = mutableListOf<Insight>()
        
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        val days = ChronoUnit.DAYS.between(timeRange.startTime.toLocalDate(), timeRange.endTime.toLocalDate())
        val averageDaily = if (days > 0) totalHours / days else 0.0
        
        if (averageDaily > 8) {
            insights.add(Insight(
                type = Insight.InsightType.OVERTIME_ALERT,
                title = "工作时间过长",
                description = "平均每日工作 ${String.format("%.1f", averageDaily)} 小时，建议注意工作与生活平衡",
                value = averageDaily,
                trend = null
            ))
        }
        
        val billableRatio = if (totalHours > 0) {
            timeEntries.filter { it.billable }.sumOf { it.duration?.toHours() ?: 0.0 } / totalHours
        } else 0.0
        
        if (billableRatio > 0.8) {
            insights.add(Insight(
                type = Insight.InsightType.EFFICIENCY_IMPROVEMENT,
                title = "高计费效率",
                description = "计费时间占比 ${String.format("%.1f", billableRatio * 100)}%，效率很高",
                value = billableRatio,
                trend = Insight.Trend.STABLE
            ))
        }
        
        return insights
    }

    private fun generateRecommendations(patterns: List<TimePattern>, insights: List<Insight>): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        
        // 基于模式生成建议
        patterns.forEach { pattern ->
            when (pattern.type) {
                TimePattern.PatternType.LONG_SESSIONS -> {
                    recommendations.add(Recommendation(
                        type = Recommendation.RecommendationType.BREAK_MANAGEMENT,
                        title = "建议增加休息时间",
                        description = "检测到长时间工作会话，建议每2小时休息15分钟",
                        priority = Recommendation.Priority.HIGH,
                        actionable = true
                    ))
                }
                TimePattern.PatternType.WEEKEND_WORK -> {
                    recommendations.add(Recommendation(
                        type = Recommendation.RecommendationType.SCHEDULE_OPTIMIZATION,
                        title = "优化工作安排",
                        description = "建议减少周末工作，提高工作日效率",
                        priority = Recommendation.Priority.MEDIUM,
                        actionable = true
                    ))
                }
                else -> {}
            }
        }
        
        // 基于洞察生成建议
        insights.forEach { insight ->
            when (insight.type) {
                Insight.InsightType.OVERTIME_ALERT -> {
                    recommendations.add(Recommendation(
                        type = Recommendation.RecommendationType.SCHEDULE_OPTIMIZATION,
                        title = "调整工作时间",
                        description = "建议设置每日工作时间上限，避免过度工作",
                        priority = Recommendation.Priority.HIGH,
                        actionable = true
                    ))
                }
                else -> {}
            }
        }
        
        return recommendations
    }
}