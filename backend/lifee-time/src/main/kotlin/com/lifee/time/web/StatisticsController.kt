package com.lifee.time.web

import com.lifee.time.application.TimeTrackingApplicationService
import com.lifee.time.domain.*
import com.lifee.time.web.dto.*
import com.lifee.user.domain.UserId
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/api/statistics")
class StatisticsController(
    private val timeTrackingService: TimeTrackingApplicationService
) {
    
    /**
     * 获取用户统计
     */
    @GetMapping("/user")
    fun getUserStatistics(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<UserStatisticsResponse> {
        val statistics = timeTrackingService.getUserStatistics(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate
        )
        return ResponseEntity.ok(statistics.toResponse())
    }
    
    /**
     * 获取项目统计
     */
    @GetMapping("/project/{projectId}")
    fun getProjectStatistics(
        @PathVariable projectId: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<ProjectStatisticsResponse> {
        val statistics = timeTrackingService.getProjectStatistics(
            projectId = ProjectId(projectId),
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate
        )
        return ResponseEntity.ok(statistics.toResponse())
    }
    
    /**
     * 获取团队统计
     */
    @GetMapping("/team/{teamId}")
    fun getTeamStatistics(
        @PathVariable teamId: String,
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<TeamStatisticsResponse> {
        val statistics = timeTrackingService.getTeamStatistics(
            teamId = TeamId(teamId),
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate
        )
        return ResponseEntity.ok(statistics.toResponse())
    }
    
    /**
     * 获取任务统计
     */
    @GetMapping("/task")
    fun getTaskStatistics(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ): ResponseEntity<TaskStatisticsResponse> {
        val statistics = timeTrackingService.getTaskStatistics(
            userId = UserId(userId),
            projectId = projectId?.let { ProjectId(it) },
            startDate = startDate,
            endDate = endDate
        )
        return ResponseEntity.ok(statistics.toResponse())
    }
    
    /**
     * 获取时间分布
     */
    @GetMapping("/time-distribution")
    fun getTimeDistribution(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) teamId: String?
    ): ResponseEntity<TimeDistributionResponse> {
        val distribution = timeTrackingService.getTimeDistribution(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate,
            projectId = projectId?.let { ProjectId(it) },
            teamId = teamId?.let { TeamId(it) }
        )
        return ResponseEntity.ok(distribution.toResponse())
    }
    
    /**
     * 获取生产力趋势
     */
    @GetMapping("/productivity-trend")
    fun getProductivityTrend(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) teamId: String?
    ): ResponseEntity<ProductivityTrendResponse> {
        val trend = timeTrackingService.getProductivityTrend(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate,
            projectId = projectId?.let { ProjectId(it) },
            teamId = teamId?.let { TeamId(it) }
        )
        return ResponseEntity.ok(trend.toResponse())
    }
    
    /**
     * 获取收入统计
     */
    @GetMapping("/revenue")
    fun getRevenueStatistics(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) teamId: String?
    ): ResponseEntity<RevenueStatisticsResponse> {
        val statistics = timeTrackingService.getRevenueStatistics(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate,
            projectId = projectId?.let { ProjectId(it) },
            teamId = teamId?.let { TeamId(it) }
        )
        return ResponseEntity.ok(statistics.toResponse())
    }
    
    /**
     * 获取效率指标
     */
    @GetMapping("/efficiency")
    fun getEfficiencyMetrics(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) teamId: String?
    ): ResponseEntity<EfficiencyMetricsResponse> {
        val metrics = timeTrackingService.getEfficiencyMetrics(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate,
            projectId = projectId?.let { ProjectId(it) },
            teamId = teamId?.let { TeamId(it) }
        )
        return ResponseEntity.ok(metrics.toResponse())
    }
    
    /**
     * 获取仪表板统计
     */
    @GetMapping("/dashboard")
    fun getDashboardStatistics(
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<DashboardStatisticsResponse> {
        val userStats = timeTrackingService.getUserStatistics(
            userId = UserId(userId),
            startDate = null,
            endDate = null
        )
        
        val timeDistribution = timeTrackingService.getTimeDistribution(
            userId = UserId(userId),
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now(),
            projectId = null,
            teamId = null
        )
        
        val productivityTrend = timeTrackingService.getProductivityTrend(
            userId = UserId(userId),
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now(),
            projectId = null,
            teamId = null
        )
        
        val dashboard = DashboardStatistics(
            userStatistics = userStats,
            timeDistribution = timeDistribution,
            productivityTrend = productivityTrend,
            recentActivity = emptyList() // 可以添加最近活动数据
        )
        
        return ResponseEntity.ok(dashboard.toResponse())
    }
    
    /**
     * 获取周统计
     */
    @GetMapping("/weekly")
    fun getWeeklyStatistics(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) weekStart: LocalDate?
    ): ResponseEntity<WeeklyStatisticsResponse> {
        val startDate = weekStart ?: LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value - 1L)
        val endDate = startDate.plusDays(6)
        
        val userStats = timeTrackingService.getUserStatistics(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate
        )
        
        val timeDistribution = timeTrackingService.getTimeDistribution(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate,
            projectId = null,
            teamId = null
        )
        
        val weekly = WeeklyStatistics(
            weekStart = startDate,
            weekEnd = endDate,
            userStatistics = userStats,
            timeDistribution = timeDistribution,
            dailyBreakdown = emptyList() // 可以添加每日分解数据
        )
        
        return ResponseEntity.ok(weekly.toResponse())
    }
    
    /**
     * 获取月统计
     */
    @GetMapping("/monthly")
    fun getMonthlyStatistics(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(required = false) year: Int?,
        @RequestParam(required = false) month: Int?
    ): ResponseEntity<MonthlyStatisticsResponse> {
        val targetYear = year ?: LocalDate.now().year
        val targetMonth = month ?: LocalDate.now().monthValue
        val startDate = LocalDate.of(targetYear, targetMonth, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        
        val userStats = timeTrackingService.getUserStatistics(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate
        )
        
        val timeDistribution = timeTrackingService.getTimeDistribution(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate,
            projectId = null,
            teamId = null
        )
        
        val revenueStats = timeTrackingService.getRevenueStatistics(
            userId = UserId(userId),
            startDate = startDate,
            endDate = endDate,
            projectId = null,
            teamId = null
        )
        
        val monthly = MonthlyStatistics(
            year = targetYear,
            month = targetMonth,
            userStatistics = userStats,
            timeDistribution = timeDistribution,
            revenueStatistics = revenueStats,
            weeklyBreakdown = emptyList() // 可以添加周分解数据
        )
        
        return ResponseEntity.ok(monthly.toResponse())
    }
}

/**
 * 扩展函数：领域对象转响应DTO
 */
fun com.lifee.time.domain.service.UserStatistics.toResponse(): UserStatisticsResponse {
    return UserStatisticsResponse(
        totalTimeTracked = totalTimeTracked,
        billableTime = billableTime,
        nonBillableTime = nonBillableTime,
        totalRevenue = totalRevenue,
        averageHourlyRate = averageHourlyRate,
        projectCount = projectCount,
        taskCount = taskCount,
        completedTaskCount = completedTaskCount,
        dailyProgress = dailyProgress.map { it.toResponse() },
        weeklyAverage = weeklyAverage,
        monthlyAverage = monthlyAverage
    )
}

fun com.lifee.time.domain.service.ProjectStatistics.toResponse(): ProjectStatisticsResponse {
    return ProjectStatisticsResponse(
        totalTimeSpent = totalTimeSpent,
        billableTime = billableTime,
        totalRevenue = totalRevenue,
        memberCount = memberCount,
        taskCount = taskCount,
        completedTaskCount = completedTaskCount,
        progress = progress,
        estimatedCompletion = estimatedCompletion,
        budgetUtilization = budgetUtilization
    )
}

fun com.lifee.time.domain.service.TeamStatistics.toResponse(): TeamStatisticsResponse {
    return TeamStatisticsResponse(
        totalMembers = totalMembers,
        activeMembers = activeMembers,
        totalProjects = totalProjects,
        totalTimeSpent = totalTimeSpent,
        totalRevenue = totalRevenue,
        averageProductivity = averageProductivity,
        topPerformer = topPerformer,
        teamEfficiency = teamEfficiency
    )
}

fun com.lifee.time.domain.service.TaskStatistics.toResponse(): TaskStatisticsResponse {
    return TaskStatisticsResponse(
        totalTasks = totalTasks,
        completedTasks = completedTasks,
        inProgressTasks = inProgressTasks,
        overdueTasks = overdueTasks,
        averageCompletionTime = averageCompletionTime,
        completionRate = completionRate,
        onTimeDeliveryRate = onTimeDeliveryRate
    )
}

fun com.lifee.time.domain.service.DailyProgress.toResponse(): DailyProgressResponse {
    return DailyProgressResponse(
        date = date,
        duration = duration,
        goal = goal,
        progress = progress
    )
}

fun com.lifee.time.domain.service.TimeDistribution.toResponse(): TimeDistributionResponse {
    return TimeDistributionResponse(
        totalDuration = totalDuration,
        projectDistribution = projectDistribution.map { it.toResponse() },
        billableDistribution = billableDistribution.toResponse()
    )
}

fun com.lifee.time.domain.service.DistributionItem.toResponse(): DistributionItemResponse {
    return DistributionItemResponse(
        name = name,
        duration = duration,
        percentage = percentage,
        revenue = revenue
    )
}

fun com.lifee.time.domain.service.BillableDistribution.toResponse(): BillableDistributionResponse {
    return BillableDistributionResponse(
        billableDuration = billableDuration,
        nonBillableDuration = nonBillableDuration,
        billablePercentage = billablePercentage,
        billableRevenue = billableRevenue
    )
}

fun com.lifee.time.domain.service.ProductivityTrend.toResponse(): ProductivityTrendResponse {
    return ProductivityTrendResponse(
        dailyProductivity = dailyProductivity.map { it.toResponse() },
        averageProductivity = averageProductivity,
        trend = trend,
        peakDay = peakDay,
        lowDay = lowDay
    )
}

fun com.lifee.time.domain.service.DailyProductivity.toResponse(): DailyProductivityResponse {
    return DailyProductivityResponse(
        date = date,
        duration = duration,
        efficiency = efficiency,
        taskCount = taskCount
    )
}

fun com.lifee.time.domain.service.RevenueStatistics.toResponse(): RevenueStatisticsResponse {
    return RevenueStatisticsResponse(
        totalRevenue = totalRevenue,
        billableRevenue = billableRevenue,
        averageHourlyRate = averageHourlyRate,
        revenueBreakdown = revenueBreakdown.map { it.toResponse() },
        monthlyRevenue = monthlyRevenue.map { it.toResponse() },
        revenueGrowth = revenueGrowth
    )
}

fun com.lifee.time.domain.service.RevenueBreakdownItem.toResponse(): RevenueBreakdownItemResponse {
    return RevenueBreakdownItemResponse(
        category = category,
        revenue = revenue,
        percentage = percentage
    )
}

fun com.lifee.time.domain.service.MonthlyRevenue.toResponse(): MonthlyRevenueResponse {
    return MonthlyRevenueResponse(
        month = month,
        revenue = revenue,
        growth = growth
    )
}

fun com.lifee.time.domain.service.EfficiencyMetrics.toResponse(): EfficiencyMetricsResponse {
    return EfficiencyMetricsResponse(
        overallEfficiency = overallEfficiency,
        timeUtilization = timeUtilization,
        taskCompletionRate = taskCompletionRate,
        averageTaskDuration = averageTaskDuration,
        focusTime = focusTime,
        distractionTime = distractionTime,
        productiveHours = productiveHours
    )
}

// 仪表板统计数据类
data class DashboardStatistics(
    val userStatistics: com.lifee.time.domain.service.UserStatistics,
    val timeDistribution: com.lifee.time.domain.service.TimeDistribution,
    val productivityTrend: com.lifee.time.domain.service.ProductivityTrend,
    val recentActivity: List<Any> // 可以定义具体的活动类型
)

fun DashboardStatistics.toResponse(): DashboardStatisticsResponse {
    return DashboardStatisticsResponse(
        userStatistics = userStatistics.toResponse(),
        timeDistribution = timeDistribution.toResponse(),
        productivityTrend = productivityTrend.toResponse(),
        recentActivity = recentActivity
    )
}

// 周统计数据类
data class WeeklyStatistics(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val userStatistics: com.lifee.time.domain.service.UserStatistics,
    val timeDistribution: com.lifee.time.domain.service.TimeDistribution,
    val dailyBreakdown: List<Any> // 可以定义具体的每日分解类型
)

fun WeeklyStatistics.toResponse(): WeeklyStatisticsResponse {
    return WeeklyStatisticsResponse(
        weekStart = weekStart,
        weekEnd = weekEnd,
        userStatistics = userStatistics.toResponse(),
        timeDistribution = timeDistribution.toResponse(),
        dailyBreakdown = dailyBreakdown
    )
}

// 月统计数据类
data class MonthlyStatistics(
    val year: Int,
    val month: Int,
    val userStatistics: com.lifee.time.domain.service.UserStatistics,
    val timeDistribution: com.lifee.time.domain.service.TimeDistribution,
    val revenueStatistics: com.lifee.time.domain.service.RevenueStatistics,
    val weeklyBreakdown: List<Any> // 可以定义具体的周分解类型
)

fun MonthlyStatistics.toResponse(): MonthlyStatisticsResponse {
    return MonthlyStatisticsResponse(
        year = year,
        month = month,
        userStatistics = userStatistics.toResponse(),
        timeDistribution = timeDistribution.toResponse(),
        revenueStatistics = revenueStatistics.toResponse(),
        weeklyBreakdown = weeklyBreakdown
    )
}