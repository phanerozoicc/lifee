package com.github.phanerozoicc.user.application.service

import com.github.phanerozoicc.user.domain.cqrs.*
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.cqrs.BatchOperationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 用户应用服务
 * 负责协调命令和查询的执行，是应用层的门面
 */
@Service
@Transactional
class UserApplicationService(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    // ==================== 命令操作 ====================
    
    /**
     * 用户注册
     */
    suspend fun registerUser(command: RegisterUserCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 用户登录
     */
    suspend fun loginUser(command: LoginUserCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 更新用户资料
     */
    suspend fun updateUserProfile(command: UpdateUserProfileCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 修改密码
     */
    suspend fun changePassword(command: ChangePasswordCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 重置密码
     */
    suspend fun resetPassword(command: ResetPasswordCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 激活用户
     */
    suspend fun activateUser(command: ActivateUserCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 停用用户
     */
    suspend fun deactivateUser(command: DeactivateUserCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 锁定用户
     */
    suspend fun lockUser(command: LockUserCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 解锁用户
     */
    suspend fun unlockUser(command: UnlockUserCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 验证邮箱
     */
    suspend fun verifyEmail(command: VerifyEmailCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 更新用户偏好设置
     */
    suspend fun updateUserPreferences(command: UpdateUserPreferencesCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 删除用户
     */
    suspend fun deleteUser(command: DeleteUserCommand): CommandResult {
        return commandBus.send(command)
    }
    
    /**
     * 批量操作用户
     */
    suspend fun batchOperateUsers(command: BatchUserOperationCommand): CommandResult {
        return commandBus.send(command)
    }
    
    // ==================== 查询操作 ====================
    
    /**
     * 获取用户资料
     */
    @Transactional(readOnly = true)
    suspend fun getUserProfile(query: GetUserProfileQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 获取用户权限
     */
    @Transactional(readOnly = true)
    suspend fun getUserPermissions(query: GetUserPermissionsQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 获取用户列表
     */
    @Transactional(readOnly = true)
    suspend fun getUserList(query: GetUserListQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 搜索用户
     */
    @Transactional(readOnly = true)
    suspend fun searchUsers(query: SearchUsersQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 获取用户统计
     */
    @Transactional(readOnly = true)
    suspend fun getUserStatistics(query: GetUserStatisticsQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 获取用户偏好设置
     */
    @Transactional(readOnly = true)
    suspend fun getUserPreferences(query: GetUserPreferencesQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 获取用户活动记录
     */
    @Transactional(readOnly = true)
    suspend fun getUserActivity(query: GetUserActivityQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 获取用户安全报告
     */
    @Transactional(readOnly = true)
    suspend fun getUserSecurityReport(query: GetUserSecurityReportQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 验证唯一性
     */
    @Transactional(readOnly = true)
    suspend fun validateUniqueness(query: ValidateUniquenessQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 获取登录历史
     */
    @Transactional(readOnly = true)
    suspend fun getLoginHistory(query: GetLoginHistoryQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 获取需要关注的用户
     */
    @Transactional(readOnly = true)
    suspend fun getUsersNeedingAttention(query: GetUsersNeedingAttentionQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    /**
     * 导出用户数据
     */
    @Transactional(readOnly = true)
    suspend fun exportUserData(query: ExportUserDataQuery): QueryResult {
        return queryBus.execute(query)
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 快速注册用户（简化版）
     */
    suspend fun quickRegister(
        email: String,
        password: String,
        nickname: String,
        ipAddress: String = "unknown",
        userAgent: String = "unknown"
    ): CommandResult {
        val command = RegisterUserCommand(
            email = email,
            password = password,
            nickname = nickname,
            ipAddress = ipAddress,
            userAgent = userAgent,
            acceptTerms = true
        )
        return registerUser(command)
    }
    
    /**
     * 快速登录（简化版）
     */
    suspend fun quickLogin(
        email: String,
        password: String,
        ipAddress: String = "unknown",
        userAgent: String = "unknown"
    ): CommandResult {
        val command = LoginUserCommand(
            email = email,
            password = password,
            ipAddress = ipAddress,
            userAgent = userAgent,
            sessionId = java.util.UUID.randomUUID().toString()
        )
        return loginUser(command)
    }
    
    /**
     * 检查邮箱是否可用
     */
    @Transactional(readOnly = true)
    suspend fun isEmailAvailable(email: String): Boolean {
        val query = ValidateUniquenessQuery(
            field = "email",
            value = email
        )
        val result = validateUniqueness(query)
        return when (result) {
            is QueryResult.Success -> {
                val data = result.data as Map<String, Any>
                data["isUnique"] as Boolean
            }
            else -> false
        }
    }
    
    /**
     * 检查昵称是否可用
     */
    @Transactional(readOnly = true)
    suspend fun isNicknameAvailable(nickname: String): Boolean {
        val query = ValidateUniquenessQuery(
            field = "nickname",
            value = nickname
        )
        val result = validateUniqueness(query)
        return when (result) {
            is QueryResult.Success -> {
                val data = result.data as Map<String, Any>
                data["isUnique"] as Boolean
            }
            else -> false
        }
    }
    
    /**
     * 获取用户基本信息
     */
    @Transactional(readOnly = true)
    suspend fun getUserBasicInfo(userId: String): QueryResult {
        return getUserProfile(GetUserProfileQuery(userId))
    }
    
    /**
     * 获取用户完整信息（包含权限和偏好设置）
     */
    @Transactional(readOnly = true)
    suspend fun getUserFullInfo(userId: String): QueryResult<Map<String, Any?>> {
        try {
            val userIdObj = UserId.of(userId)
            val profileResult = getUserProfile(GetUserProfileQuery(
                queryId = UUID.randomUUID().toString(),
                userId = userIdObj
            ))
            if (profileResult !is QueryResult.Success) {
                return profileResult
            }
            
            val permissionsResult = getUserPermissions(GetUserPermissionsQuery(
                queryId = UUID.randomUUID().toString(),
                userId = userIdObj
            ))
            val preferencesResult = getUserPreferences(GetUserPreferencesQuery(
                queryId = UUID.randomUUID().toString(),
                userId = userIdObj
            ))
            
            val fullInfo = mutableMapOf<String, Any?>()
        
        if (profileResult is QueryResult.Success<*>) {
            fullInfo["profile"] = profileResult.data
        }
        
        if (permissionsResult is QueryResult.Success<*>) {
            fullInfo["permissions"] = permissionsResult.data
        }
        
        if (preferencesResult is QueryResult.Success<*>) {
            fullInfo["preferences"] = preferencesResult.data
        }
        
        return QueryResult.Success<Map<String, Any?>>(fullInfo)
            
        } catch (e: Exception) {
            return QueryResult.Error("获取用户完整信息失败: ${e.message}")
        }
    }
    
    /**
     * 批量激活用户
     */
    suspend fun batchActivateUsers(userIds: List<String>, operatorId: String): CommandResult {
        val command = BatchUserOperationCommand(
            commandId = UUID.randomUUID().toString(),
            operationType = BatchOperationType.ACTIVATE,
            targetUserIds = userIds.map { UserId.of(it) },
            operatedBy = UserId.of(operatorId)
        )
        return batchOperateUsers(command)
    }
    
    /**
     * 批量停用用户
     */
    suspend fun batchDeactivateUsers(userIds: List<String>, operatorId: String, reason: String? = null): CommandResult {
        val command = BatchUserOperationCommand(
            commandId = UUID.randomUUID().toString(),
            operationType = BatchOperationType.DEACTIVATE,
            targetUserIds = userIds.map { UserId.of(it) },
            operatedBy = UserId.of(operatorId),
            reason = reason
        )
        return batchOperateUsers(command)
    }
    
    /**
     * 批量删除用户
     */
    suspend fun batchDeleteUsers(userIds: List<String>, operatorId: String, reason: String? = null): CommandResult {
        val command = BatchUserOperationCommand(
            commandId = UUID.randomUUID().toString(),
            operationType = BatchOperationType.DELETE,
            targetUserIds = userIds.map { UserId.of(it) },
            operatedBy = UserId.of(operatorId),
            reason = reason
        )
        return batchOperateUsers(command)
    }
}