package com.github.phanerozoicc.user.bak.domain.cqrs

import com.github.phanerozoicc.user.domain.model.UserId
import java.time.LocalDateTime

/**
 * 用户相关的查询定义
 * 查询表示对系统状态的读取操作
 */

/**
 * 查询基类
 */
abstract class UserQuery {
    abstract val queryId: String
    abstract val timestamp: LocalDateTime
    abstract val requestedBy: UserId?
}



/**
 * 获取用户权限查询
 */
data class GetUserPermissionsQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val userId: UserId,
    val includeRoles: Boolean = true,
    val includePermissions: Boolean = true
) : UserQuery()


/**
 * 搜索用户查询
 */
data class SearchUsersQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val keyword: String,
    val searchFields: List<String> = listOf("nickname", "email", "firstName", "lastName"),
    val pageSize: Int = 20,
    val pageNumber: Int = 1,
    val sortBy: String = "relevance",
    val sortDirection: String = "DESC",
    val filters: Map<String, Any>? = null
) : UserQuery()

/**
 * 获取用户统计查询
 */
data class GetUserStatisticsQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val dateRange: DateRange? = null,
    val groupBy: String? = null, // day, week, month
    val includeInactive: Boolean = false
) : UserQuery()

/**
 * 获取用户偏好设置查询
 */
data class GetUserPreferencesQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val userId: UserId
) : UserQuery()

/**
 * 获取用户活动记录查询
 */
data class GetUserActivityQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val userId: UserId,
    val activityTypes: List<String>? = null,
    val dateRange: DateRange? = null,
    val pageSize: Int = 50,
    val pageNumber: Int = 1
) : UserQuery()

/**
 * 获取用户安全报告查询
 */
data class GetUserSecurityReportQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val userId: UserId,
    val includeRecommendations: Boolean = true
) : UserQuery()

/**
 * 验证用户唯一性查询
 */
data class ValidateUserUniquenessQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val email: String? = null,
    val nickname: String? = null,
    val excludeUserId: UserId? = null // 更新时排除当前用户
) : UserQuery()

/**
 * 获取用户登录历史查询
 */
data class GetUserLoginHistoryQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val userId: UserId,
    val dateRange: DateRange? = null,
    val includeFailedAttempts: Boolean = false,
    val pageSize: Int = 50,
    val pageNumber: Int = 1
) : UserQuery()

/**
 * 获取需要关注的用户查询
 */
data class GetUsersRequiringAttentionQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val attentionTypes: List<AttentionType> = AttentionType.values().toList(),
    val pageSize: Int = 50,
    val pageNumber: Int = 1
) : UserQuery()

/**
 * 获取用户导出数据查询
 */
data class ExportUserDataQuery(
    override val queryId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val requestedBy: UserId? = null,
    val userIds: List<UserId>? = null,
    val criteria: Map<String, Any>? = null,
    val format: ExportFormat = ExportFormat.CSV,
    val includeFields: List<String>? = null,
    val excludeFields: List<String>? = null
) : UserQuery()

/**
 * 日期范围
 */
data class DateRange(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
) {
    init {
        require(startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
            "开始日期必须早于或等于结束日期"
        }
    }
    
    companion object {
        fun today(): DateRange {
            val now = LocalDateTime.now()
            val startOfDay = now.toLocalDate().atStartOfDay()
            val endOfDay = startOfDay.plusDays(1).minusNanos(1)
            return DateRange(startOfDay, endOfDay)
        }
        
        fun thisWeek(): DateRange {
            val now = LocalDateTime.now()
            val startOfWeek = now.toLocalDate().minusDays(now.dayOfWeek.value - 1L).atStartOfDay()
            val endOfWeek = startOfWeek.plusWeeks(1).minusNanos(1)
            return DateRange(startOfWeek, endOfWeek)
        }
        
        fun thisMonth(): DateRange {
            val now = LocalDateTime.now()
            val startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
            val endOfMonth = startOfMonth.plusMonths(1).minusNanos(1)
            return DateRange(startOfMonth, endOfMonth)
        }
        
        fun lastNDays(days: Int): DateRange {
            val now = LocalDateTime.now()
            val startDate = now.minusDays(days.toLong())
            return DateRange(startDate, now)
        }
    }
}

/**
 * 需要关注的用户类型
 */
enum class AttentionType {
    PASSWORD_EXPIRING, // 密码即将过期
    LONG_TIME_INACTIVE, // 长期未登录
    MULTIPLE_LOGIN_FAILURES, // 多次登录失败
    UNVERIFIED_EMAIL, // 未验证邮箱
    ACCOUNT_LOCKED, // 账户被锁定
    SUSPICIOUS_ACTIVITY, // 可疑活动
    INCOMPLETE_PROFILE // 资料不完整
}

/**
 * 导出格式
 */
enum class ExportFormat {
    CSV,
    EXCEL,
    JSON,
    PDF
}

///**
// * 查询结果
// */
//sealed class QueryResult<T> {
//    data class Success<T>(val data: T) : QueryResult<T>()
//    data class NotFound<T>(val message: String = "未找到数据") : QueryResult<T>()
//    data class Error<T>(val error: String, val errorCode: String? = null) : QueryResult<T>()
//    data class Unauthorized<T>(val message: String = "无权限访问") : QueryResult<T>()
//}

/**
 * 查询处理器接口
 */
//interface QueryHandler<TQuery : UserQuery, TResult> {
//    /**
//     * 处理查询
//     * @param query 要处理的查询
//     * @return 查询结果
//     */
//    suspend fun handle(query: TQuery): QueryResult<TResult>
//
//}


///**
// * 分页查询结果
// */
//data class PagedResult<T>(
//    val items: List<T>,
//    val totalCount: Long,
//    val pageSize: Int,
//    val pageNumber: Int,
//    val totalPages: Int,
//    val hasNext: Boolean,
//    val hasPrevious: Boolean
//) {
//    companion object {
//        fun <T> create(
//            items: List<T>,
//            totalCount: Long,
//            pageSize: Int,
//            pageNumber: Int
//        ): PagedResult<T> {
//            val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt()
//
//            return PagedResult(
//                items = items,
//                totalCount = totalCount,
//                pageSize = pageSize,
//                pageNumber = pageNumber,
//                totalPages = totalPages,
//                hasNext = pageNumber < totalPages,
//                hasPrevious = pageNumber > 1
//            )
//        }
//
//        fun <T> empty(pageSize: Int = 20, pageNumber: Int = 1): PagedResult<T> {
//            return PagedResult(
//                items = emptyList(),
//                totalCount = 0,
//                pageSize = pageSize,
//                pageNumber = pageNumber,
//                totalPages = 0,
//                hasNext = false,
//                hasPrevious = false
//            )
//        }
//    }
//}