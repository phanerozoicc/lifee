package com.lifee.time.domain.query

import com.lifee.time.domain.*
import com.lifee.user.domain.UserId
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 报告查询条件
 */
data class ReportQuery(
    val userId: UserId? = null,
    val projectId: ProjectId? = null,
    val teamId: TeamId? = null,
    val taskId: TaskId? = null,
    val timeRange: TimeRange,
    val tags: Set<String> = emptySet(),
    val billableOnly: Boolean = false,
    val clientName: String? = null,
    val groupBy: GroupBy = GroupBy.DAY
) {
    enum class GroupBy {
        HOUR, DAY, WEEK, MONTH, YEAR, PROJECT, USER, TAG
    }
}

/**
 * 时间报告数据
 */
data class TimeReport(
    val query: ReportQuery,
    val summary: TimeSummary,
    val details: List<TimeReportDetail>,
    val charts: List<ChartData>
)

/**
 * 时间汇总信息
 */
data class TimeSummary(
    val totalDuration: Duration,
    val billableDuration: Duration,
    val nonBillableDuration: Duration,
    val totalRevenue: java.math.BigDecimal,
    val averageDailyHours: Double,
    val workingDays: Int,
    val projectCount: Int,
    val taskCount: Int,
    val timeEntryCount: Int
)

/**
 * 时间报告详情
 */
data class TimeReportDetail(
    val date: LocalDate,
    val projectId: ProjectId?,
    val projectName: String?,
    val taskId: TaskId?,
    val taskName: String?,
    val userId: UserId,
    val userName: String?,
    val description: String,
    val duration: Duration,
    val billable: Boolean,
    val hourlyRate: java.math.BigDecimal?,
    val revenue: java.math.BigDecimal?,
    val tags: Set<String>
)

/**
 * 图表数据
 */
data class ChartData(
    val type: ChartType,
    val title: String,
    val data: List<ChartDataPoint>
) {
    enum class ChartType {
        LINE, BAR, PIE, AREA
    }
}

/**
 * 图表数据点
 */
data class ChartDataPoint(
    val label: String,
    val value: Double,
    val color: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 项目报告
 */
data class ProjectReport(
    val projectId: ProjectId,
    val projectName: String,
    val timeRange: TimeRange,
    val summary: ProjectSummary,
    val memberReports: List<MemberReport>,
    val taskReports: List<TaskReport>,
    val dailyBreakdown: List<DailyBreakdown>
)

/**
 * 项目汇总信息
 */
data class ProjectSummary(
    val totalDuration: Duration,
    val billableDuration: Duration,
    val totalRevenue: java.math.BigDecimal,
    val budgetUsed: Double?, // 预算使用百分比
    val memberCount: Int,
    val activeTaskCount: Int,
    val completedTaskCount: Int,
    val averageTaskDuration: Duration?
)

/**
 * 成员报告
 */
data class MemberReport(
    val userId: UserId,
    val userName: String?,
    val totalDuration: Duration,
    val billableDuration: Duration,
    val revenue: java.math.BigDecimal,
    val taskCount: Int,
    val completedTaskCount: Int,
    val averageDailyHours: Double
)

/**
 * 任务报告
 */
data class TaskReport(
    val taskId: TaskId,
    val taskName: String,
    val status: TaskStatus,
    val assigneeId: UserId?,
    val assigneeName: String?,
    val totalDuration: Duration,
    val estimatedDuration: Duration?,
    val completionRate: Double?, // 完成百分比
    val isOverdue: Boolean,
    val dueDate: Instant?
)

/**
 * 日常分解报告
 */
data class DailyBreakdown(
    val date: LocalDate,
    val totalDuration: Duration,
    val billableDuration: Duration,
    val revenue: java.math.BigDecimal,
    val memberCount: Int,
    val taskCount: Int
)

/**
 * 团队报告
 */
data class TeamReport(
    val teamId: TeamId,
    val teamName: String,
    val timeRange: TimeRange,
    val summary: TeamSummary,
    val memberReports: List<MemberReport>,
    val projectReports: List<ProjectSummary>,
    val productivityMetrics: ProductivityMetrics
)

/**
 * 团队汇总信息
 */
data class TeamSummary(
    val totalDuration: Duration,
    val billableDuration: Duration,
    val totalRevenue: java.math.BigDecimal,
    val memberCount: Int,
    val activeProjectCount: Int,
    val completedTaskCount: Int,
    val averageTeamProductivity: Double
)

/**
 * 生产力指标
 */
data class ProductivityMetrics(
    val averageHoursPerDay: Double,
    val billableRatio: Double, // 计费时间比例
    val taskCompletionRate: Double,
    val projectDeliveryRate: Double,
    val memberUtilizationRate: Double, // 成员利用率
    val overallEfficiency: Double // 整体效率
)

/**
 * 用户报告
 */
data class UserReport(
    val userId: UserId,
    val userName: String?,
    val timeRange: TimeRange,
    val summary: UserSummary,
    val projectBreakdown: List<ProjectBreakdown>,
    val dailyActivity: List<DailyActivity>,
    val goals: UserGoals?
)

/**
 * 用户汇总信息
 */
data class UserSummary(
    val totalDuration: Duration,
    val billableDuration: Duration,
    val totalRevenue: java.math.BigDecimal,
    val projectCount: Int,
    val taskCount: Int,
    val completedTaskCount: Int,
    val averageDailyHours: Double,
    val mostProductiveDay: LocalDate?,
    val longestSession: Duration?
)

/**
 * 项目分解
 */
data class ProjectBreakdown(
    val projectId: ProjectId,
    val projectName: String,
    val duration: Duration,
    val percentage: Double,
    val revenue: java.math.BigDecimal,
    val taskCount: Int
)

/**
 * 日常活动
 */
data class DailyActivity(
    val date: LocalDate,
    val duration: Duration,
    val sessionCount: Int,
    val projectCount: Int,
    val taskCount: Int,
    val firstActivity: Instant?,
    val lastActivity: Instant?
)

/**
 * 用户目标
 */
data class UserGoals(
    val dailyHoursTarget: Double?,
    val weeklyHoursTarget: Double?,
    val monthlyHoursTarget: Double?,
    val dailyProgress: Double,
    val weeklyProgress: Double,
    val monthlyProgress: Double
)

/**
 * 时间追踪分析
 */
data class TimeTrackingAnalysis(
    val timeRange: TimeRange,
    val patterns: List<TimePattern>,
    val insights: List<Insight>,
    val recommendations: List<Recommendation>
)

/**
 * 时间模式
 */
data class TimePattern(
    val type: PatternType,
    val description: String,
    val frequency: Double,
    val impact: PatternImpact
) {
    enum class PatternType {
        PEAK_HOURS, LOW_PRODUCTIVITY, LONG_SESSIONS, SHORT_SESSIONS, 
        FREQUENT_BREAKS, OVERTIME, WEEKEND_WORK, LATE_NIGHT
    }
    
    enum class PatternImpact {
        POSITIVE, NEGATIVE, NEUTRAL
    }
}

/**
 * 洞察
 */
data class Insight(
    val type: InsightType,
    val title: String,
    val description: String,
    val value: Double?,
    val trend: Trend?
) {
    enum class InsightType {
        PRODUCTIVITY_INCREASE, PRODUCTIVITY_DECREASE, NEW_PEAK_HOUR,
        OVERTIME_ALERT, GOAL_ACHIEVEMENT, EFFICIENCY_IMPROVEMENT
    }
    
    enum class Trend {
        INCREASING, DECREASING, STABLE
    }
}

/**
 * 建议
 */
data class Recommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: Priority,
    val actionable: Boolean
) {
    enum class RecommendationType {
        SCHEDULE_OPTIMIZATION, BREAK_MANAGEMENT, TASK_PRIORITIZATION,
        GOAL_ADJUSTMENT, TOOL_USAGE, WORKFLOW_IMPROVEMENT
    }
    
    enum class Priority {
        HIGH, MEDIUM, LOW
    }
}