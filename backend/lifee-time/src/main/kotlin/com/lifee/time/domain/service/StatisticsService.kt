package com.lifee.time.domain.service

import com.lifee.time.domain.*
import com.lifee.time.domain.repository.*
import com.lifee.user.domain.UserId
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 统计服务
 */
@Service
class StatisticsService(
    private val timeEntryRepository: TimeEntryRepository,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val teamRepository: TeamRepository
) {

    /**
     * 获取用户统计信息
     */
    suspend fun getUserStatistics(userId: UserId, timeRange: TimeRange): UserStatistics {
        val timeEntries = timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
        val projects = timeEntryRepository.findProjectsByUserId(userId)
        val tasks = taskRepository.findByAssigneeId(userId)
        
        return UserStatistics(
            userId = userId,
            timeRange = timeRange,
            totalTimeTracked = calculateTotalDuration(timeEntries),
            billableTime = calculateBillableDuration(timeEntries),
            totalRevenue = calculateTotalRevenue(timeEntries),
            projectCount = projects.size,
            activeProjectCount = projects.count { it.isActive },
            taskCount = tasks.size,
            completedTaskCount = tasks.count { it.status == TaskStatus.COMPLETED },
            averageDailyHours = calculateAverageDailyHours(timeEntries, timeRange),
            mostProductiveDay = findMostProductiveDay(timeEntries),
            longestSession = findLongestSession(timeEntries),
            totalSessions = timeEntries.size,
            averageSessionDuration = calculateAverageSessionDuration(timeEntries)
        )
    }

    /**
     * 获取项目统计信息
     */
    suspend fun getProjectStatistics(projectId: ProjectId, timeRange: TimeRange): ProjectStatistics {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("Project not found: $projectId")
        
        val timeEntries = timeEntryRepository.findByProjectIdAndTimeRange(projectId, timeRange)
        val tasks = taskRepository.findByProjectId(projectId)
        val members = project.members
        
        return ProjectStatistics(
            projectId = projectId,
            timeRange = timeRange,
            totalTimeTracked = calculateTotalDuration(timeEntries),
            billableTime = calculateBillableDuration(timeEntries),
            totalRevenue = calculateTotalRevenue(timeEntries),
            memberCount = members.size,
            activeMemberCount = members.count { it.isActive },
            taskCount = tasks.size,
            completedTaskCount = tasks.count { it.status == TaskStatus.COMPLETED },
            inProgressTaskCount = tasks.count { it.status == TaskStatus.IN_PROGRESS },
            overdueTaskCount = tasks.count { it.dueDate?.isBefore(Instant.now()) == true && it.status != TaskStatus.COMPLETED },
            averageTaskCompletionTime = calculateAverageTaskCompletionTime(tasks, timeEntries),
            budgetUtilization = calculateBudgetUtilization(project, timeEntries),
            teamProductivity = calculateTeamProductivity(timeEntries, members.size, timeRange)
        )
    }

    /**
     * 获取团队统计信息
     */
    suspend fun getTeamStatistics(teamId: TeamId, timeRange: TimeRange): TeamStatistics {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("Team not found: $teamId")
        
        val projects = projectRepository.findByTeamId(teamId)
        val allTimeEntries = projects.flatMap { project ->
            timeEntryRepository.findByProjectIdAndTimeRange(project.projectId, timeRange)
        }
        val allTasks = projects.flatMap { project ->
            taskRepository.findByProjectId(project.projectId)
        }
        
        return TeamStatistics(
            teamId = teamId,
            timeRange = timeRange,
            totalTimeTracked = calculateTotalDuration(allTimeEntries),
            billableTime = calculateBillableDuration(allTimeEntries),
            totalRevenue = calculateTotalRevenue(allTimeEntries),
            memberCount = team.members.size,
            activeMemberCount = team.members.count { it.isActive },
            projectCount = projects.size,
            activeProjectCount = projects.count { it.isActive },
            taskCount = allTasks.size,
            completedTaskCount = allTasks.count { it.status == TaskStatus.COMPLETED },
            averageProjectProgress = calculateAverageProjectProgress(projects, allTasks),
            teamEfficiency = calculateTeamEfficiency(allTimeEntries, allTasks),
            memberUtilization = calculateMemberUtilization(allTimeEntries, team.members, timeRange)
        )
    }

    /**
     * 获取任务统计信息
     */
    suspend fun getTaskStatistics(taskId: TaskId): TaskStatistics {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("Task not found: $taskId")
        
        val timeEntries = timeEntryRepository.findByTaskId(taskId)
        
        return TaskStatistics(
            taskId = taskId,
            totalTimeSpent = calculateTotalDuration(timeEntries),
            estimatedTime = task.estimatedDuration,
            timeVariance = calculateTimeVariance(task.estimatedDuration, calculateTotalDuration(timeEntries)),
            sessionCount = timeEntries.size,
            averageSessionDuration = calculateAverageSessionDuration(timeEntries),
            contributorCount = timeEntries.map { it.userId }.distinct().size,
            isOverBudget = isTaskOverBudget(task.estimatedDuration, calculateTotalDuration(timeEntries)),
            completionPercentage = calculateTaskCompletionPercentage(task, timeEntries),
            dailyProgress = calculateDailyProgress(timeEntries)
        )
    }

    /**
     * 获取时间分布统计
     */
    suspend fun getTimeDistribution(userId: UserId, timeRange: TimeRange): TimeDistribution {
        val timeEntries = timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
        
        return TimeDistribution(
            byProject = calculateProjectDistribution(timeEntries),
            byTask = calculateTaskDistribution(timeEntries),
            byDay = calculateDayDistribution(timeEntries),
            byHour = calculateHourDistribution(timeEntries),
            byTag = calculateTagDistribution(timeEntries),
            billableVsNonBillable = calculateBillableDistribution(timeEntries)
        )
    }

    /**
     * 获取生产力趋势
     */
    suspend fun getProductivityTrend(userId: UserId, days: Int): ProductivityTrend {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        val timeRange = TimeRange.of(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay())
        
        val timeEntries = timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
        
        val dailyData = (0 until days).map { dayOffset ->
            val date = startDate.plusDays(dayOffset.toLong())
            val dayEntries = timeEntries.filter { it.startTime.toLocalDate() == date }
            
            DailyProductivity(
                date = date,
                totalHours = dayEntries.sumOf { it.duration?.toHours() ?: 0.0 },
                billableHours = dayEntries.filter { it.billable }.sumOf { it.duration?.toHours() ?: 0.0 },
                sessionCount = dayEntries.size,
                projectCount = dayEntries.mapNotNull { it.projectId }.distinct().size,
                taskCount = dayEntries.mapNotNull { it.taskId }.distinct().size,
                efficiency = calculateDailyEfficiency(dayEntries)
            )
        }
        
        return ProductivityTrend(
            timeRange = timeRange,
            dailyData = dailyData,
            averageDaily = dailyData.map { it.totalHours }.average(),
            trend = calculateTrend(dailyData.map { it.totalHours }),
            peakDay = dailyData.maxByOrNull { it.totalHours }?.date,
            lowDay = dailyData.minByOrNull { it.totalHours }?.date
        )
    }

    /**
     * 获取收入统计
     */
    suspend fun getRevenueStatistics(userId: UserId?, projectId: ProjectId?, timeRange: TimeRange): RevenueStatistics {
        val timeEntries = when {
            userId != null -> timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
            projectId != null -> timeEntryRepository.findByProjectIdAndTimeRange(projectId, timeRange)
            else -> timeEntryRepository.findByTimeRange(timeRange)
        }.filter { it.billable }
        
        val totalRevenue = calculateTotalRevenue(timeEntries)
        val averageHourlyRate = calculateAverageHourlyRate(timeEntries)
        
        return RevenueStatistics(
            timeRange = timeRange,
            totalRevenue = totalRevenue,
            billableHours = calculateBillableDuration(timeEntries).toHours(),
            averageHourlyRate = averageHourlyRate,
            highestRate = timeEntries.maxOfOrNull { it.hourlyRate ?: BigDecimal.ZERO } ?: BigDecimal.ZERO,
            lowestRate = timeEntries.minOfOrNull { it.hourlyRate ?: BigDecimal.ZERO } ?: BigDecimal.ZERO,
            clientBreakdown = calculateClientRevenueBreakdown(timeEntries),
            projectBreakdown = calculateProjectRevenueBreakdown(timeEntries),
            monthlyTrend = calculateMonthlyRevenueTrend(timeEntries)
        )
    }

    /**
     * 获取效率指标
     */
    suspend fun getEfficiencyMetrics(userId: UserId, timeRange: TimeRange): EfficiencyMetrics {
        val timeEntries = timeEntryRepository.findByUserIdAndTimeRange(userId, timeRange)
        val tasks = timeEntries.mapNotNull { it.taskId }.distinct()
            .mapNotNull { taskRepository.findById(it) }
        
        return EfficiencyMetrics(
            timeRange = timeRange,
            focusTime = calculateFocusTime(timeEntries),
            interruptionRate = calculateInterruptionRate(timeEntries),
            taskSwitchingRate = calculateTaskSwitchingRate(timeEntries),
            averageTaskDuration = calculateAverageTaskDuration(timeEntries),
            completionRate = calculateCompletionRate(tasks),
            estimationAccuracy = calculateEstimationAccuracy(tasks, timeEntries),
            billableRatio = calculateBillableRatio(timeEntries),
            productivityScore = calculateProductivityScore(timeEntries, tasks)
        )
    }

    // 私有辅助方法

    private fun calculateTotalDuration(timeEntries: List<TimeEntry>): Duration {
        return Duration.ofMinutes(timeEntries.sumOf { it.duration?.toMinutes() ?: 0L })
    }

    private fun calculateBillableDuration(timeEntries: List<TimeEntry>): Duration {
        return Duration.ofMinutes(
            timeEntries.filter { it.billable }.sumOf { it.duration?.toMinutes() ?: 0L }
        )
    }

    private fun calculateTotalRevenue(timeEntries: List<TimeEntry>): BigDecimal {
        return timeEntries.filter { it.billable }.sumOf { entry ->
            val hours = (entry.duration?.toMinutes() ?: 0L) / 60.0
            val rate = entry.hourlyRate ?: BigDecimal.ZERO
            rate.multiply(BigDecimal.valueOf(hours))
        }
    }

    private fun calculateAverageDailyHours(timeEntries: List<TimeEntry>, timeRange: TimeRange): Double {
        val days = ChronoUnit.DAYS.between(timeRange.startTime.toLocalDate(), timeRange.endTime.toLocalDate())
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        return if (days > 0) totalHours / days else 0.0
    }

    private fun findMostProductiveDay(timeEntries: List<TimeEntry>): LocalDate? {
        return timeEntries.groupBy { it.startTime.toLocalDate() }
            .mapValues { (_, entries) -> entries.sumOf { it.duration?.toHours() ?: 0.0 } }
            .maxByOrNull { it.value }?.key
    }

    private fun findLongestSession(timeEntries: List<TimeEntry>): Duration? {
        return timeEntries.maxOfOrNull { it.duration ?: Duration.ZERO }
    }

    private fun calculateAverageSessionDuration(timeEntries: List<TimeEntry>): Duration {
        if (timeEntries.isEmpty()) return Duration.ZERO
        val totalMinutes = timeEntries.sumOf { it.duration?.toMinutes() ?: 0L }
        return Duration.ofMinutes(totalMinutes / timeEntries.size)
    }

    private fun calculateAverageTaskCompletionTime(tasks: List<Task>, timeEntries: List<TimeEntry>): Duration? {
        val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }
        if (completedTasks.isEmpty()) return null
        
        val totalTime = completedTasks.sumOf { task ->
            timeEntries.filter { it.taskId == task.taskId }
                .sumOf { it.duration?.toMinutes() ?: 0L }
        }
        
        return Duration.ofMinutes(totalTime / completedTasks.size)
    }

    private fun calculateBudgetUtilization(project: Project, timeEntries: List<TimeEntry>): Double? {
        // 需要项目预算信息，暂时返回null
        return null
    }

    private fun calculateTeamProductivity(timeEntries: List<TimeEntry>, memberCount: Int, timeRange: TimeRange): Double {
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        val days = ChronoUnit.DAYS.between(timeRange.startTime.toLocalDate(), timeRange.endTime.toLocalDate())
        val expectedHours = memberCount * days * 8.0 // 假设每天8小时
        return if (expectedHours > 0) totalHours / expectedHours else 0.0
    }

    private fun calculateAverageProjectProgress(projects: List<Project>, tasks: List<Task>): Double {
        if (projects.isEmpty()) return 0.0
        
        val projectProgress = projects.map { project ->
            val projectTasks = tasks.filter { it.projectId == project.projectId }
            if (projectTasks.isEmpty()) 0.0
            else projectTasks.count { it.status == TaskStatus.COMPLETED }.toDouble() / projectTasks.size
        }
        
        return projectProgress.average()
    }

    private fun calculateTeamEfficiency(timeEntries: List<TimeEntry>, tasks: List<Task>): Double {
        val billableHours = timeEntries.filter { it.billable }.sumOf { it.duration?.toHours() ?: 0.0 }
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        val completionRate = if (tasks.isNotEmpty()) {
            tasks.count { it.status == TaskStatus.COMPLETED }.toDouble() / tasks.size
        } else 0.0
        
        val billableRatio = if (totalHours > 0) billableHours / totalHours else 0.0
        return (billableRatio + completionRate) / 2
    }

    private fun calculateMemberUtilization(timeEntries: List<TimeEntry>, members: Set<TeamMember>, timeRange: TimeRange): Double {
        val activeMembers = members.filter { it.isActive }
        if (activeMembers.isEmpty()) return 0.0
        
        val memberHours = activeMembers.map { member ->
            timeEntries.filter { it.userId == member.userId }
                .sumOf { it.duration?.toHours() ?: 0.0 }
        }
        
        val days = ChronoUnit.DAYS.between(timeRange.startTime.toLocalDate(), timeRange.endTime.toLocalDate())
        val expectedHours = days * 8.0 // 每天8小时
        
        return memberHours.map { hours ->
            if (expectedHours > 0) hours / expectedHours else 0.0
        }.average()
    }

    private fun calculateTimeVariance(estimated: Duration?, actual: Duration): Double {
        if (estimated == null) return 0.0
        val estimatedHours = estimated.toHours()
        val actualHours = actual.toHours()
        return if (estimatedHours > 0) (actualHours - estimatedHours) / estimatedHours else 0.0
    }

    private fun isTaskOverBudget(estimated: Duration?, actual: Duration): Boolean {
        return estimated?.let { actual > it } ?: false
    }

    private fun calculateTaskCompletionPercentage(task: Task, timeEntries: List<TimeEntry>): Double {
        return when (task.status) {
            TaskStatus.COMPLETED -> 100.0
            TaskStatus.CANCELLED -> 0.0
            else -> {
                val actualTime = calculateTotalDuration(timeEntries)
                val estimatedTime = task.estimatedDuration
                if (estimatedTime != null && estimatedTime.toMinutes() > 0) {
                    (actualTime.toMinutes().toDouble() / estimatedTime.toMinutes() * 100).coerceAtMost(100.0)
                } else 0.0
            }
        }
    }

    private fun calculateDailyProgress(timeEntries: List<TimeEntry>): List<DailyProgress> {
        return timeEntries.groupBy { it.startTime.toLocalDate() }
            .map { (date, entries) ->
                DailyProgress(
                    date = date,
                    duration = calculateTotalDuration(entries),
                    sessionCount = entries.size
                )
            }.sortedBy { it.date }
    }

    private fun calculateProjectDistribution(timeEntries: List<TimeEntry>): List<DistributionItem> {
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        return timeEntries.groupBy { it.projectId }
            .map { (projectId, entries) ->
                val hours = entries.sumOf { it.duration?.toHours() ?: 0.0 }
                DistributionItem(
                    label = projectId?.value ?: "无项目",
                    value = hours,
                    percentage = if (totalHours > 0) (hours / totalHours) * 100 else 0.0
                )
            }.sortedByDescending { it.value }
    }

    private fun calculateTaskDistribution(timeEntries: List<TimeEntry>): List<DistributionItem> {
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        return timeEntries.groupBy { it.taskId }
            .map { (taskId, entries) ->
                val hours = entries.sumOf { it.duration?.toHours() ?: 0.0 }
                DistributionItem(
                    label = taskId?.value ?: "无任务",
                    value = hours,
                    percentage = if (totalHours > 0) (hours / totalHours) * 100 else 0.0
                )
            }.sortedByDescending { it.value }
    }

    private fun calculateDayDistribution(timeEntries: List<TimeEntry>): List<DistributionItem> {
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        return timeEntries.groupBy { it.startTime.dayOfWeek }
            .map { (dayOfWeek, entries) ->
                val hours = entries.sumOf { it.duration?.toHours() ?: 0.0 }
                DistributionItem(
                    label = dayOfWeek.name,
                    value = hours,
                    percentage = if (totalHours > 0) (hours / totalHours) * 100 else 0.0
                )
            }.sortedByDescending { it.value }
    }

    private fun calculateHourDistribution(timeEntries: List<TimeEntry>): List<DistributionItem> {
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        return timeEntries.groupBy { it.startTime.hour }
            .map { (hour, entries) ->
                val hours = entries.sumOf { it.duration?.toHours() ?: 0.0 }
                DistributionItem(
                    label = "${hour}:00",
                    value = hours,
                    percentage = if (totalHours > 0) (hours / totalHours) * 100 else 0.0
                )
            }.sortedBy { it.label }
    }

    private fun calculateTagDistribution(timeEntries: List<TimeEntry>): List<DistributionItem> {
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        val tagEntries = timeEntries.flatMap { entry ->
            entry.tags.map { tag -> tag to entry }
        }
        
        return tagEntries.groupBy { it.first }
            .map { (tag, entries) ->
                val hours = entries.sumOf { it.second.duration?.toHours() ?: 0.0 }
                DistributionItem(
                    label = tag,
                    value = hours,
                    percentage = if (totalHours > 0) (hours / totalHours) * 100 else 0.0
                )
            }.sortedByDescending { it.value }
    }

    private fun calculateBillableDistribution(timeEntries: List<TimeEntry>): BillableDistribution {
        val billableHours = timeEntries.filter { it.billable }.sumOf { it.duration?.toHours() ?: 0.0 }
        val nonBillableHours = timeEntries.filter { !it.billable }.sumOf { it.duration?.toHours() ?: 0.0 }
        val totalHours = billableHours + nonBillableHours
        
        return BillableDistribution(
            billableHours = billableHours,
            nonBillableHours = nonBillableHours,
            billablePercentage = if (totalHours > 0) (billableHours / totalHours) * 100 else 0.0,
            nonBillablePercentage = if (totalHours > 0) (nonBillableHours / totalHours) * 100 else 0.0
        )
    }

    private fun calculateDailyEfficiency(timeEntries: List<TimeEntry>): Double {
        if (timeEntries.isEmpty()) return 0.0
        val billableHours = timeEntries.filter { it.billable }.sumOf { it.duration?.toHours() ?: 0.0 }
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        return if (totalHours > 0) billableHours / totalHours else 0.0
    }

    private fun calculateTrend(values: List<Double>): String {
        if (values.size < 2) return "stable"
        val firstHalf = values.take(values.size / 2).average()
        val secondHalf = values.drop(values.size / 2).average()
        return when {
            secondHalf > firstHalf * 1.1 -> "increasing"
            secondHalf < firstHalf * 0.9 -> "decreasing"
            else -> "stable"
        }
    }

    private fun calculateAverageHourlyRate(timeEntries: List<TimeEntry>): BigDecimal {
        val ratedEntries = timeEntries.filter { it.hourlyRate != null }
        return if (ratedEntries.isNotEmpty()) {
            ratedEntries.map { it.hourlyRate!! }.reduce { acc, rate -> acc.add(rate) }
                .divide(BigDecimal.valueOf(ratedEntries.size.toLong()), 2, java.math.RoundingMode.HALF_UP)
        } else BigDecimal.ZERO
    }

    private fun calculateClientRevenueBreakdown(timeEntries: List<TimeEntry>): List<RevenueBreakdownItem> {
        // 需要客户信息，暂时返回空列表
        return emptyList()
    }

    private fun calculateProjectRevenueBreakdown(timeEntries: List<TimeEntry>): List<RevenueBreakdownItem> {
        return timeEntries.groupBy { it.projectId }
            .map { (projectId, entries) ->
                val revenue = calculateTotalRevenue(entries)
                RevenueBreakdownItem(
                    label = projectId?.value ?: "无项目",
                    revenue = revenue,
                    hours = entries.sumOf { it.duration?.toHours() ?: 0.0 }
                )
            }.sortedByDescending { it.revenue }
    }

    private fun calculateMonthlyRevenueTrend(timeEntries: List<TimeEntry>): List<MonthlyRevenue> {
        return timeEntries.groupBy { 
            val date = it.startTime.toLocalDate()
            "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
        }.map { (month, entries) ->
            MonthlyRevenue(
                month = month,
                revenue = calculateTotalRevenue(entries),
                hours = entries.sumOf { it.duration?.toHours() ?: 0.0 }
            )
        }.sortedBy { it.month }
    }

    private fun calculateFocusTime(timeEntries: List<TimeEntry>): Duration {
        // 定义专注时间为连续工作超过25分钟的时间段
        val focusMinutes = timeEntries.filter { (it.duration?.toMinutes() ?: 0L) >= 25 }
            .sumOf { it.duration?.toMinutes() ?: 0L }
        return Duration.ofMinutes(focusMinutes)
    }

    private fun calculateInterruptionRate(timeEntries: List<TimeEntry>): Double {
        // 定义中断为短于15分钟的时间记录
        val shortSessions = timeEntries.count { (it.duration?.toMinutes() ?: 0L) < 15 }
        return if (timeEntries.isNotEmpty()) shortSessions.toDouble() / timeEntries.size else 0.0
    }

    private fun calculateTaskSwitchingRate(timeEntries: List<TimeEntry>): Double {
        if (timeEntries.size < 2) return 0.0
        val sortedEntries = timeEntries.sortedBy { it.startTime }
        val switches = sortedEntries.zipWithNext().count { (current, next) ->
            current.taskId != next.taskId
        }
        return switches.toDouble() / (timeEntries.size - 1)
    }

    private fun calculateAverageTaskDuration(timeEntries: List<TimeEntry>): Duration {
        val taskDurations = timeEntries.groupBy { it.taskId }
            .mapValues { (_, entries) -> entries.sumOf { it.duration?.toMinutes() ?: 0L } }
        
        return if (taskDurations.isNotEmpty()) {
            Duration.ofMinutes(taskDurations.values.sum() / taskDurations.size)
        } else Duration.ZERO
    }

    private fun calculateCompletionRate(tasks: List<Task>): Double {
        return if (tasks.isNotEmpty()) {
            tasks.count { it.status == TaskStatus.COMPLETED }.toDouble() / tasks.size
        } else 0.0
    }

    private fun calculateEstimationAccuracy(tasks: List<Task>, timeEntries: List<TimeEntry>): Double {
        val tasksWithEstimates = tasks.filter { it.estimatedDuration != null }
        if (tasksWithEstimates.isEmpty()) return 0.0
        
        val accuracies = tasksWithEstimates.map { task ->
            val actualTime = timeEntries.filter { it.taskId == task.taskId }
                .sumOf { it.duration?.toMinutes() ?: 0L }
            val estimatedTime = task.estimatedDuration!!.toMinutes()
            
            if (estimatedTime > 0) {
                1.0 - kotlin.math.abs(actualTime - estimatedTime).toDouble() / estimatedTime
            } else 0.0
        }
        
        return accuracies.average().coerceAtLeast(0.0)
    }

    private fun calculateBillableRatio(timeEntries: List<TimeEntry>): Double {
        val totalHours = timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }
        val billableHours = timeEntries.filter { it.billable }.sumOf { it.duration?.toHours() ?: 0.0 }
        return if (totalHours > 0) billableHours / totalHours else 0.0
    }

    private fun calculateProductivityScore(timeEntries: List<TimeEntry>, tasks: List<Task>): Double {
        val billableRatio = calculateBillableRatio(timeEntries)
        val completionRate = calculateCompletionRate(tasks)
        val focusRatio = calculateFocusTime(timeEntries).toHours() / 
            timeEntries.sumOf { it.duration?.toHours() ?: 0.0 }.coerceAtLeast(1.0)
        
        return (billableRatio + completionRate + focusRatio) / 3
    }
}

// 统计数据类

data class UserStatistics(
    val userId: UserId,
    val timeRange: TimeRange,
    val totalTimeTracked: Duration,
    val billableTime: Duration,
    val totalRevenue: BigDecimal,
    val projectCount: Int,
    val activeProjectCount: Int,
    val taskCount: Int,
    val completedTaskCount: Int,
    val averageDailyHours: Double,
    val mostProductiveDay: LocalDate?,
    val longestSession: Duration?,
    val totalSessions: Int,
    val averageSessionDuration: Duration
)

data class ProjectStatistics(
    val projectId: ProjectId,
    val timeRange: TimeRange,
    val totalTimeTracked: Duration,
    val billableTime: Duration,
    val totalRevenue: BigDecimal,
    val memberCount: Int,
    val activeMemberCount: Int,
    val taskCount: Int,
    val completedTaskCount: Int,
    val inProgressTaskCount: Int,
    val overdueTaskCount: Int,
    val averageTaskCompletionTime: Duration?,
    val budgetUtilization: Double?,
    val teamProductivity: Double
)

data class TeamStatistics(
    val teamId: TeamId,
    val timeRange: TimeRange,
    val totalTimeTracked: Duration,
    val billableTime: Duration,
    val totalRevenue: BigDecimal,
    val memberCount: Int,
    val activeMemberCount: Int,
    val projectCount: Int,
    val activeProjectCount: Int,
    val taskCount: Int,
    val completedTaskCount: Int,
    val averageProjectProgress: Double,
    val teamEfficiency: Double,
    val memberUtilization: Double
)

data class TaskStatistics(
    val taskId: TaskId,
    val totalTimeSpent: Duration,
    val estimatedTime: Duration?,
    val timeVariance: Double,
    val sessionCount: Int,
    val averageSessionDuration: Duration,
    val contributorCount: Int,
    val isOverBudget: Boolean,
    val completionPercentage: Double,
    val dailyProgress: List<DailyProgress>
)

data class DailyProgress(
    val date: LocalDate,
    val duration: Duration,
    val sessionCount: Int
)

data class TimeDistribution(
    val byProject: List<DistributionItem>,
    val byTask: List<DistributionItem>,
    val byDay: List<DistributionItem>,
    val byHour: List<DistributionItem>,
    val byTag: List<DistributionItem>,
    val billableVsNonBillable: BillableDistribution
)

data class DistributionItem(
    val label: String,
    val value: Double,
    val percentage: Double
)

data class BillableDistribution(
    val billableHours: Double,
    val nonBillableHours: Double,
    val billablePercentage: Double,
    val nonBillablePercentage: Double
)

data class ProductivityTrend(
    val timeRange: TimeRange,
    val dailyData: List<DailyProductivity>,
    val averageDaily: Double,
    val trend: String,
    val peakDay: LocalDate?,
    val lowDay: LocalDate?
)

data class DailyProductivity(
    val date: LocalDate,
    val totalHours: Double,
    val billableHours: Double,
    val sessionCount: Int,
    val projectCount: Int,
    val taskCount: Int,
    val efficiency: Double
)

data class RevenueStatistics(
    val timeRange: TimeRange,
    val totalRevenue: BigDecimal,
    val billableHours: Double,
    val averageHourlyRate: BigDecimal,
    val highestRate: BigDecimal,
    val lowestRate: BigDecimal,
    val clientBreakdown: List<RevenueBreakdownItem>,
    val projectBreakdown: List<RevenueBreakdownItem>,
    val monthlyTrend: List<MonthlyRevenue>
)

data class RevenueBreakdownItem(
    val label: String,
    val revenue: BigDecimal,
    val hours: Double
)

data class MonthlyRevenue(
    val month: String,
    val revenue: BigDecimal,
    val hours: Double
)

data class EfficiencyMetrics(
    val timeRange: TimeRange,
    val focusTime: Duration,
    val interruptionRate: Double,
    val taskSwitchingRate: Double,
    val averageTaskDuration: Duration,
    val completionRate: Double,
    val estimationAccuracy: Double,
    val billableRatio: Double,
    val productivityScore: Double
)