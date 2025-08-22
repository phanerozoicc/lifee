package com.lifee.time.web

import com.lifee.time.application.TimeTrackingApplicationService
import com.lifee.time.domain.*
import com.lifee.time.domain.query.*
import com.lifee.time.web.dto.*
import com.lifee.user.domain.UserId
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * 报告分析控制器
 */
@RestController
@RequestMapping("/api/reports")
class ReportController(
    private val timeTrackingService: TimeTrackingApplicationService
) {
    
    /**
     * 生成时间报告
     */
    @PostMapping("/time")
    fun generateTimeReport(
        @RequestBody request: GenerateTimeReportRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeReportResponse> {
        val query = ReportQuery(
            userId = request.userId?.let { UserId(it) },
            projectId = request.projectId?.let { ProjectId(it) },
            teamId = request.teamId?.let { TeamId(it) },
            taskId = request.taskId?.let { TaskId(it) },
            startDate = request.startDate,
            endDate = request.endDate,
            billableOnly = request.billableOnly,
            tags = request.tags,
            groupBy = request.groupBy?.let { ReportGroupBy.valueOf(it) } ?: ReportGroupBy.DAY
        )
        
        val report = timeTrackingService.generateTimeReport(
            query = query,
            requesterId = UserId(userId)
        )
        return ResponseEntity.ok(report.toResponse())
    }
    
    /**
     * 生成项目报告
     */
    @PostMapping("/project")
    fun generateProjectReport(
        @RequestBody request: GenerateProjectReportRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ProjectReportResponse> {
        val query = ReportQuery(
            projectId = ProjectId(request.projectId),
            startDate = request.startDate,
            endDate = request.endDate,
            billableOnly = request.billableOnly,
            groupBy = request.groupBy?.let { ReportGroupBy.valueOf(it) } ?: ReportGroupBy.DAY
        )
        
        val report = timeTrackingService.generateProjectReport(
            query = query,
            requesterId = UserId(userId)
        )
        return ResponseEntity.ok(report.toResponse())
    }
    
    /**
     * 生成团队报告
     */
    @PostMapping("/team")
    fun generateTeamReport(
        @RequestBody request: GenerateTeamReportRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TeamReportResponse> {
        val query = ReportQuery(
            teamId = TeamId(request.teamId),
            startDate = request.startDate,
            endDate = request.endDate,
            billableOnly = request.billableOnly,
            groupBy = request.groupBy?.let { ReportGroupBy.valueOf(it) } ?: ReportGroupBy.DAY
        )
        
        val report = timeTrackingService.generateTeamReport(
            query = query,
            requesterId = UserId(userId)
        )
        return ResponseEntity.ok(report.toResponse())
    }
    
    /**
     * 生成用户报告
     */
    @PostMapping("/user")
    fun generateUserReport(
        @RequestBody request: GenerateUserReportRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<UserReportResponse> {
        val query = ReportQuery(
            userId = request.userId?.let { UserId(it) } ?: UserId(userId),
            startDate = request.startDate,
            endDate = request.endDate,
            billableOnly = request.billableOnly,
            groupBy = request.groupBy?.let { ReportGroupBy.valueOf(it) } ?: ReportGroupBy.DAY
        )
        
        val report = timeTrackingService.generateUserReport(
            query = query,
            requesterId = UserId(userId)
        )
        return ResponseEntity.ok(report.toResponse())
    }
    
    /**
     * 生成时间追踪分析
     */
    @PostMapping("/analysis")
    fun generateTimeTrackingAnalysis(
        @RequestBody request: GenerateAnalysisRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<TimeTrackingAnalysisResponse> {
        val query = ReportQuery(
            userId = request.userId?.let { UserId(it) },
            projectId = request.projectId?.let { ProjectId(it) },
            teamId = request.teamId?.let { TeamId(it) },
            startDate = request.startDate,
            endDate = request.endDate
        )
        
        val analysis = timeTrackingService.generateTimeTrackingAnalysis(
            query = query,
            requesterId = UserId(userId)
        )
        return ResponseEntity.ok(analysis.toResponse())
    }
    
    /**
     * 获取生产力趋势
     */
    @GetMapping("/productivity-trend")
    fun getProductivityTrend(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) projectId: String?,
        @RequestParam(required = false) teamId: String?,
        @RequestHeader("X-User-Id") requesterId: String
    ): ResponseEntity<List<ChartDataResponse>> {
        val chartData = timeTrackingService.getProductivityTrend(
            startDate = startDate,
            endDate = endDate,
            userId = userId?.let { UserId(it) },
            projectId = projectId?.let { ProjectId(it) },
            teamId = teamId?.let { TeamId(it) },
            requesterId = UserId(requesterId)
        )
        return ResponseEntity.ok(chartData.map { it.toResponse() })
    }
    
    /**
     * 获取项目时间分布
     */
    @GetMapping("/project-time-distribution")
    fun getProjectTimeDistribution(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) teamId: String?,
        @RequestHeader("X-User-Id") requesterId: String
    ): ResponseEntity<List<ChartDataResponse>> {
        val chartData = timeTrackingService.getProjectTimeDistribution(
            startDate = startDate,
            endDate = endDate,
            userId = userId?.let { UserId(it) },
            teamId = teamId?.let { TeamId(it) },
            requesterId = UserId(requesterId)
        )
        return ResponseEntity.ok(chartData.map { it.toResponse() })
    }
    
    /**
     * 导出时间报告
     */
    @PostMapping("/time/export")
    fun exportTimeReport(
        @RequestBody request: ExportTimeReportRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ByteArray> {
        val query = ReportQuery(
            userId = request.userId?.let { UserId(it) },
            projectId = request.projectId?.let { ProjectId(it) },
            teamId = request.teamId?.let { TeamId(it) },
            startDate = request.startDate,
            endDate = request.endDate,
            billableOnly = request.billableOnly,
            tags = request.tags,
            groupBy = request.groupBy?.let { ReportGroupBy.valueOf(it) } ?: ReportGroupBy.DAY
        )
        
        val exportData = timeTrackingService.exportTimeReport(
            query = query,
            format = ExportFormat.valueOf(request.format),
            requesterId = UserId(userId)
        )
        
        val headers = org.springframework.http.HttpHeaders()
        headers.add("Content-Disposition", "attachment; filename=time-report.${request.format.lowercase()}")
        headers.add("Content-Type", getContentType(request.format))
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(exportData)
    }
    
    /**
     * 导出项目报告
     */
    @PostMapping("/project/export")
    fun exportProjectReport(
        @RequestBody request: ExportProjectReportRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ByteArray> {
        val query = ReportQuery(
            projectId = ProjectId(request.projectId),
            startDate = request.startDate,
            endDate = request.endDate,
            billableOnly = request.billableOnly,
            groupBy = request.groupBy?.let { ReportGroupBy.valueOf(it) } ?: ReportGroupBy.DAY
        )
        
        val exportData = timeTrackingService.exportProjectReport(
            query = query,
            format = ExportFormat.valueOf(request.format),
            requesterId = UserId(userId)
        )
        
        val headers = org.springframework.http.HttpHeaders()
        headers.add("Content-Disposition", "attachment; filename=project-report.${request.format.lowercase()}")
        headers.add("Content-Type", getContentType(request.format))
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(exportData)
    }
    
    private fun getContentType(format: String): String {
        return when (format.uppercase()) {
            "CSV" -> "text/csv"
            "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "PDF" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }
}

/**
 * 扩展函数：领域对象转响应DTO
 */
fun TimeReport.toResponse(): TimeReportResponse {
    return TimeReportResponse(
        summary = summary.toResponse(),
        details = details.map { it.toResponse() },
        chartData = chartData.map { it.toResponse() }
    )
}

fun TimeSummary.toResponse(): TimeSummaryResponse {
    return TimeSummaryResponse(
        totalDuration = totalDuration,
        billableDuration = billableDuration,
        nonBillableDuration = nonBillableDuration,
        totalRevenue = totalRevenue,
        averageHourlyRate = averageHourlyRate,
        entryCount = entryCount
    )
}

fun TimeReportDetail.toResponse(): TimeReportDetailResponse {
    return TimeReportDetailResponse(
        date = date,
        projectName = projectName,
        taskName = taskName,
        description = description,
        duration = duration,
        billable = billable,
        hourlyRate = hourlyRate,
        revenue = revenue,
        tags = tags
    )
}

fun ChartData.toResponse(): ChartDataResponse {
    return ChartDataResponse(
        label = label,
        dataPoints = dataPoints.map { it.toResponse() }
    )
}

fun ChartDataPoint.toResponse(): ChartDataPointResponse {
    return ChartDataPointResponse(
        x = x,
        y = y,
        label = label
    )
}

fun ProjectReport.toResponse(): ProjectReportResponse {
    return ProjectReportResponse(
        projectSummary = projectSummary.toResponse(),
        memberReports = memberReports.map { it.toResponse() },
        taskReports = taskReports.map { it.toResponse() },
        dailyBreakdown = dailyBreakdown.map { it.toResponse() },
        chartData = chartData.map { it.toResponse() }
    )
}

fun ProjectSummary.toResponse(): ProjectSummaryResponse {
    return ProjectSummaryResponse(
        projectId = projectId.value,
        projectName = projectName,
        totalDuration = totalDuration,
        billableDuration = billableDuration,
        totalRevenue = totalRevenue,
        memberCount = memberCount,
        taskCount = taskCount,
        completedTaskCount = completedTaskCount,
        progress = progress
    )
}

fun MemberReport.toResponse(): MemberReportResponse {
    return MemberReportResponse(
        userId = userId.value,
        userName = userName,
        totalDuration = totalDuration,
        billableDuration = billableDuration,
        totalRevenue = totalRevenue,
        taskCount = taskCount,
        completedTaskCount = completedTaskCount
    )
}

fun TaskReport.toResponse(): TaskReportResponse {
    return TaskReportResponse(
        taskId = taskId.value,
        taskName = taskName,
        status = status,
        assigneeName = assigneeName,
        totalDuration = totalDuration,
        estimatedDuration = estimatedDuration,
        progress = progress
    )
}

fun DailyBreakdown.toResponse(): DailyBreakdownResponse {
    return DailyBreakdownResponse(
        date = date,
        totalDuration = totalDuration,
        billableDuration = billableDuration,
        entryCount = entryCount
    )
}

fun TeamReport.toResponse(): TeamReportResponse {
    return TeamReportResponse(
        teamSummary = teamSummary.toResponse(),
        memberReports = memberReports.map { it.toResponse() },
        projectReports = projectReports.map { it.toResponse() },
        productivityMetrics = productivityMetrics.toResponse(),
        chartData = chartData.map { it.toResponse() }
    )
}

fun TeamSummary.toResponse(): TeamSummaryResponse {
    return TeamSummaryResponse(
        teamId = teamId.value,
        teamName = teamName,
        totalDuration = totalDuration,
        billableDuration = billableDuration,
        totalRevenue = totalRevenue,
        memberCount = memberCount,
        projectCount = projectCount,
        averageProductivity = averageProductivity
    )
}

fun ProductivityMetrics.toResponse(): ProductivityMetricsResponse {
    return ProductivityMetricsResponse(
        averageDailyHours = averageDailyHours,
        peakProductivityHour = peakProductivityHour,
        mostProductiveMember = mostProductiveMember,
        leastProductiveMember = leastProductiveMember,
        productivityTrend = productivityTrend
    )
}

fun UserReport.toResponse(): UserReportResponse {
    return UserReportResponse(
        userSummary = userSummary.toResponse(),
        projectBreakdown = projectBreakdown.map { it.toResponse() },
        dailyActivity = dailyActivity.map { it.toResponse() },
        goals = goals.toResponse(),
        chartData = chartData.map { it.toResponse() }
    )
}

fun UserSummary.toResponse(): UserSummaryResponse {
    return UserSummaryResponse(
        userId = userId.value,
        userName = userName,
        totalDuration = totalDuration,
        billableDuration = billableDuration,
        totalRevenue = totalRevenue,
        projectCount = projectCount,
        taskCount = taskCount,
        completedTaskCount = completedTaskCount,
        averageDailyHours = averageDailyHours
    )
}

fun ProjectBreakdown.toResponse(): ProjectBreakdownResponse {
    return ProjectBreakdownResponse(
        projectId = projectId.value,
        projectName = projectName,
        duration = duration,
        percentage = percentage,
        revenue = revenue
    )
}

fun DailyActivity.toResponse(): DailyActivityResponse {
    return DailyActivityResponse(
        date = date,
        duration = duration,
        entryCount = entryCount,
        projectCount = projectCount
    )
}

fun UserGoals.toResponse(): UserGoalsResponse {
    return UserGoalsResponse(
        dailyHoursGoal = dailyHoursGoal,
        weeklyHoursGoal = weeklyHoursGoal,
        monthlyHoursGoal = monthlyHoursGoal,
        dailyProgress = dailyProgress,
        weeklyProgress = weeklyProgress,
        monthlyProgress = monthlyProgress
    )
}

fun TimeTrackingAnalysis.toResponse(): TimeTrackingAnalysisResponse {
    return TimeTrackingAnalysisResponse(
        patterns = patterns.map { it.toResponse() },
        insights = insights.map { it.toResponse() },
        recommendations = recommendations.map { it.toResponse() }
    )
}

fun TimePattern.toResponse(): TimePatternResponse {
    return TimePatternResponse(
        type = type,
        description = description,
        frequency = frequency,
        impact = impact
    )
}

fun Insight.toResponse(): InsightResponse {
    return InsightResponse(
        type = type,
        title = title,
        description = description,
        value = value,
        trend = trend
    )
}

fun Recommendation.toResponse(): RecommendationResponse {
    return RecommendationResponse(
        type = type,
        title = title,
        description = description,
        priority = priority,
        actionItems = actionItems
    )
}

// 导出格式枚举
enum class ExportFormat {
    CSV, EXCEL, PDF
}