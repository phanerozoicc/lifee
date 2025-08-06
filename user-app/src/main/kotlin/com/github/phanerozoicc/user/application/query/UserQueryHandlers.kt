package com.github.phanerozoicc.user.bak.application.query

import com.github.phanerozoicc.user.domain.cqrs.*
import com.github.phanerozoicc.user.domain.model.*
import com.github.phanerozoicc.user.domain.query.*
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.repository.UserSearchCriteria
import com.github.phanerozoicc.user.domain.service.UserDomainService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * 搜索用户查询处理器
 */
@Service
class SearchUsersQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<SearchUsersQuery> {
    
    override suspend fun handle(query: SearchUsersQuery): QueryResult {
        try {
            val pageable = PageRequest.of(
                query.page,
                query.size,
                Sort.by(Sort.Direction.fromString(query.sortDirection), query.sortBy)
            )
            
            val criteria = UserSearchCriteria(
                keyword = query.keyword,
                status = query.status?.let { UserStatus.Status.valueOf(it) },
                createdAfter = query.createdAfter,
                createdBefore = query.createdBefore
            )
            
            val users = userRepository.findByCriteria(criteria, pageable)
            val totalCount = userRepository.countByCriteria(criteria)
            
            val searchResults = users.map { user ->
                UserSearchResultDTO(
                    userId = user.getId().getValue(),
                    email = user.getEmail().getValue(),
                    nickname = user.getProfile().getNickname(),
                    firstName = user.getProfile().getFirstName(),
                    lastName = user.getProfile().getLastName(),
                    avatar = user.getProfile().getAvatar(),
                    status = user.getStatus().getStatus().name,
                    createdAt = user.getCreatedAt(),
                    lastLoginAt = user.getLastLoginAt(),
                    isEmailVerified = user.isEmailVerified(),
                    profileCompleteness = user.getProfile().getCompleteness()
                )
            }
            
            val result = mapOf(
                "results" to searchResults,
                "totalCount" to totalCount,
                "page" to query.page,
                "size" to query.size,
                "totalPages" to (totalCount + query.size - 1) / query.size,
                "keyword" to query.keyword
            )
            
            return QueryResult.Success(result)
            
        } catch (e: Exception) {
            return QueryResult.Error("搜索用户失败: ${e.message}")
        }
    }
}

/**
 * 获取用户统计查询处理器
 */
@Service
class GetUserStatisticsQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<GetUserStatisticsQuery> {
    
    override suspend fun handle(query: GetUserStatisticsQuery): QueryResult {
        try {
            val dateRange = query.dateRange
            
            val totalUsers = userRepository.countTotal()
            val activeUsers = userRepository.countByStatus(UserStatus.Status.ACTIVE)
            val pendingUsers = userRepository.countByStatus(UserStatus.Status.PENDING)
            val inactiveUsers = userRepository.countByStatus(UserStatus.Status.INACTIVE)
            val lockedUsers = userRepository.countByStatus(UserStatus.Status.LOCKED)
            
            val newUsersInPeriod = if (dateRange != null) {
                userRepository.countByRegistrationDateRange(dateRange.startDate, dateRange.endDate)
            } else {
                0L
            }
            
            val activeUsersInPeriod = if (dateRange != null) {
                userRepository.countActiveUsersByDateRange(dateRange.startDate, dateRange.endDate)
            } else {
                0L
            }
            
            val statistics = UserStatisticsDTO(
                totalUsers = totalUsers,
                activeUsers = activeUsers,
                pendingUsers = pendingUsers,
                inactiveUsers = inactiveUsers,
                lockedUsers = lockedUsers,
                newUsersInPeriod = newUsersInPeriod,
                activeUsersInPeriod = activeUsersInPeriod,
                dateRange = dateRange
            )
            
            return QueryResult.Success(statistics)
            
        } catch (e: Exception) {
            return QueryResult.Error("获取用户统计失败: ${e.message}")
        }
    }
}

/**
 * 获取用户偏好设置查询处理器
 */
@Service
class GetUserPreferencesQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<GetUserPreferencesQuery> {
    
    override suspend fun handle(query: GetUserPreferencesQuery): QueryResult {
        try {
            val user = userRepository.findById(query.userId)
                ?: return QueryResult.NotFound("用户不存在")
            
            val preferences = UserPreferencesDTO.fromUserPreferences(user.getPreferences())
            
            return QueryResult.Success(preferences)
            
        } catch (e: Exception) {
            return QueryResult.Error("获取用户偏好设置失败: ${e.message}")
        }
    }
}

/**
 * 获取用户活动记录查询处理器
 */
@Service
class GetUserActivityQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<GetUserActivityQuery> {
    
    override suspend fun handle(query: GetUserActivityQuery): QueryResult {
        try {
            val user = userRepository.findById(query.userId)
                ?: return QueryResult.NotFound("用户不存在")
            
            val activity = UserActivityDTO(
                userId = user.getId().getValue(),
                lastLoginAt = user.getLastLoginAt(),
                lastLoginIp = user.getLastLoginIp(),
                lastLoginUserAgent = user.getLastLoginUserAgent(),
                loginAttempts = user.getLoginAttempts(),
                lastPasswordChangeAt = user.getLastPasswordChangeAt(),
                lastProfileUpdateAt = user.getLastProfileUpdateAt(),
                emailVerifiedAt = user.getEmailVerifiedAt(),
                createdAt = user.getCreatedAt(),
                updatedAt = user.getUpdatedAt()
            )
            
            return QueryResult.Success(activity)
            
        } catch (e: Exception) {
            return QueryResult.Error("获取用户活动记录失败: ${e.message}")
        }
    }
}

/**
 * 获取用户安全报告查询处理器
 */
@Service
class GetUserSecurityReportQueryHandler(
    private val userRepository: UserRepository,
    private val userDomainService: UserDomainService
) : QueryHandler<GetUserSecurityReportQuery> {
    
    override suspend fun handle(query: GetUserSecurityReportQuery): QueryResult {
        try {
            val user = userRepository.findById(query.userId)
                ?: return QueryResult.NotFound("用户不存在")
            
            val securityReport = userDomainService.checkAccountSecurity(user)
            
            val reportDTO = UserSecurityReportDTO(
                userId = user.getId().getValue(),
                securityLevel = securityReport.securityLevel.name,
                score = securityReport.score,
                issues = securityReport.issues.map { issue ->
                    SecurityIssueDTO(
                        type = issue.type.name,
                        severity = issue.severity.name,
                        description = issue.description,
                        recommendation = issue.recommendation
                    )
                },
                lastPasswordChange = user.getLastPasswordChangeAt(),
                isEmailVerified = user.isEmailVerified(),
                recentLoginAttempts = user.getLoginAttempts(),
                accountAge = user.getAccountAge(),
                generatedAt = LocalDateTime.now()
            )
            
            return QueryResult.Success(reportDTO)
            
        } catch (e: Exception) {
            return QueryResult.Error("获取用户安全报告失败: ${e.message}")
        }
    }
}

/**
 * 验证唯一性查询处理器
 */
@Service
class ValidateUniquenessQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<ValidateUniquenessQuery> {
    
    override suspend fun handle(query: ValidateUniquenessQuery): QueryResult {
        try {
            val result = when (query.field) {
                "email" -> {
                    val email = Email.of(query.value)
                    val exists = userRepository.existsByEmail(email)
                    mapOf(
                        "field" to "email",
                        "value" to query.value,
                        "isUnique" to !exists,
                        "message" to if (exists) "邮箱已被使用" else "邮箱可用"
                    )
                }
                "nickname" -> {
                    val exists = userRepository.existsByNickname(query.value)
                    mapOf(
                        "field" to "nickname",
                        "value" to query.value,
                        "isUnique" to !exists,
                        "message" to if (exists) "昵称已被使用" else "昵称可用"
                    )
                }
                else -> {
                    return QueryResult.Error("不支持的字段: ${query.field}")
                }
            }
            
            return QueryResult.Success(result)
            
        } catch (e: Exception) {
            return QueryResult.Error("验证唯一性失败: ${e.message}")
        }
    }
}

/**
 * 获取需要关注的用户查询处理器
 */
@Service
class GetUsersNeedingAttentionQueryHandler(
    private val userRepository: UserRepository,
    private val userDomainService: UserDomainService
) : QueryHandler<GetUsersNeedingAttentionQuery> {
    
    override suspend fun handle(query: GetUsersNeedingAttentionQuery): QueryResult {
        try {
            val pageable = PageRequest.of(query.page, query.size)
            
            val users = when (query.attentionType) {
                AttentionType.PASSWORD_EXPIRING -> {
                    userDomainService.findUsersNeedingPasswordReminder()
                }
                AttentionType.LONG_INACTIVE -> {
                    userDomainService.findLongInactiveUsers(query.days ?: 90)
                }
                AttentionType.UNVERIFIED_EMAIL -> {
                    userDomainService.findAndCleanupExpiredUnverifiedUsers(query.days ?: 7)
                }
                AttentionType.SECURITY_ISSUES -> {
                    // 查找有安全问题的用户
                    val allUsers = userRepository.findAll()
                    allUsers.filter { user ->
                        val report = userDomainService.checkAccountSecurity(user)
                        report.securityLevel == UserDomainService.SecurityLevel.HIGH_RISK ||
                        report.securityLevel == UserDomainService.SecurityLevel.MEDIUM_RISK
                    }
                }
            }
            
            val userSummaries = users.map { UserSummaryDTO.fromUser(it) }
            
            val result = mapOf(
                "users" to userSummaries,
                "attentionType" to query.attentionType.name,
                "totalCount" to users.size,
                "page" to query.page,
                "size" to query.size
            )
            
            return QueryResult.Success(result)
            
        } catch (e: Exception) {
            return QueryResult.Error("获取需要关注的用户失败: ${e.message}")
        }
    }
}

/**
 * 导出用户数据查询处理器
 */
@Service
class ExportUserDataQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<ExportUserDataQuery> {
    
    override suspend fun handle(query: ExportUserDataQuery): QueryResult {
        try {
            val criteria = UserSearchCriteria(
                status = query.status?.let { UserStatus.Status.valueOf(it) },
                createdAfter = query.dateRange?.startDate,
                createdBefore = query.dateRange?.endDate
            )
            
            val users = userRepository.findByCriteria(criteria)
            
            val exportData = when (query.format) {
                ExportFormat.CSV -> {
                    generateCSVData(users)
                }
                ExportFormat.JSON -> {
                    generateJSONData(users)
                }
                ExportFormat.EXCEL -> {
                    generateExcelData(users)
                }
            }
            
            val result = mapOf(
                "format" to query.format.name,
                "data" to exportData,
                "totalRecords" to users.size,
                "generatedAt" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            
            return QueryResult.Success(result)
            
        } catch (e: Exception) {
            return QueryResult.Error("导出用户数据失败: ${e.message}")
        }
    }
    
    private fun generateCSVData(users: List<User>): String {
        val header = "ID,Email,Nickname,FirstName,LastName,Status,CreatedAt,LastLoginAt\n"
        val rows = users.joinToString("\n") { user ->
            "${user.getId().getValue()},${user.getEmail().getValue()},${user.getProfile().getNickname()}," +
            "${user.getProfile().getFirstName() ?: ""},${user.getProfile().getLastName() ?: ""}," +
            "${user.getStatus().getStatus().name},${user.getCreatedAt()},${user.getLastLoginAt() ?: ""}"
        }
        return header + rows
    }
    
    private fun generateJSONData(users: List<User>): List<Map<String, Any?>> {
        return users.map { user ->
            mapOf(
                "id" to user.getId().getValue(),
                "email" to user.getEmail().getValue(),
                "nickname" to user.getProfile().getNickname(),
                "firstName" to user.getProfile().getFirstName(),
                "lastName" to user.getProfile().getLastName(),
                "status" to user.getStatus().getStatus().name,
                "createdAt" to user.getCreatedAt(),
                "lastLoginAt" to user.getLastLoginAt()
            )
        }
    }
    
    private fun generateExcelData(users: List<User>): String {
        // 简化的Excel格式（实际应用中应使用专门的Excel库）
        return generateCSVData(users)
    }
}