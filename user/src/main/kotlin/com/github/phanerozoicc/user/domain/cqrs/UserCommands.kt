package com.github.phanerozoicc.user.domain.cqrs

import com.github.phanerozoicc.user.domain.model.UserId
import java.time.LocalDateTime

/**
 * 用户相关的命令定义
 * 命令表示对系统状态的修改操作
 */

/**
 * 命令基类
 */
abstract class UserCommand {
    abstract val commandId: String
    abstract val timestamp: LocalDateTime
    abstract val userId: UserId?
    abstract val ipAddress: String?
    abstract val userAgent: String?
}

/**
 * 用户注册命令
 */
data class RegisterUserCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId? = null,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val email: String,
    val password: String,
    val nickname: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val acceptTerms: Boolean = true,
    val marketingConsent: Boolean = false
) : UserCommand()

/**
 * 用户登录命令
 */
data class LoginUserCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId? = null,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val email: String,
    val password: String,
    val rememberMe: Boolean = false,
    val sessionId: String? = null
) : UserCommand()

/**
 * 更新用户资料命令
 */
data class UpdateUserProfileCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val nickname: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val birthDate: String? = null, // ISO格式日期字符串
    val gender: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val website: String? = null,
    val updatedBy: UserId? = null // 如果是管理员操作
) : UserCommand()

/**
 * 修改密码命令
 */
data class ChangePasswordCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val oldPassword: String,
    val newPassword: String,
    val confirmPassword: String
) : UserCommand()

/**
 * 重置密码命令
 */
data class ResetPasswordCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val newPassword: String,
    val resetToken: String? = null,
    val resetBy: UserId? = null // 管理员重置时使用
) : UserCommand()

/**
 * 激活用户命令
 */
data class ActivateUserCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val activatedBy: UserId? = null,
    val reason: String? = null
) : UserCommand()

/**
 * 停用用户命令
 */
data class DeactivateUserCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val reason: String,
    val deactivatedBy: UserId? = null
) : UserCommand()

/**
 * 锁定用户命令
 */
data class LockUserCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val reason: String,
    val lockedBy: UserId? = null,
    val lockDuration: Long? = null // 锁定时长（分钟）
) : UserCommand()

/**
 * 解锁用户命令
 */
data class UnlockUserCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val unlockedBy: UserId,
    val reason: String? = null
) : UserCommand()

/**
 * 验证邮箱命令
 */
data class VerifyEmailCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val verificationToken: String
) : UserCommand()

/**
 * 更新用户偏好设置命令
 */
data class UpdateUserPreferencesCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val language: String? = null,
    val timezone: String? = null,
    val theme: String? = null,
    val dateFormat: String? = null,
    val emailNotifications: Boolean? = null,
    val pushNotifications: Boolean? = null,
    val smsNotifications: Boolean? = null,
    val marketingEmails: Boolean? = null,
    val securityAlerts: Boolean? = null
) : UserCommand()

/**
 * 删除用户命令
 */
data class DeleteUserCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val reason: String = "用户主动删除",
    val deletedBy: UserId? = null,
    val isHardDelete: Boolean = false,
    val confirmPassword: String? = null // 用户主动删除时需要确认密码
) : UserCommand()

/**
 * 批量操作命令
 */
data class BatchUserOperationCommand(
    override val commandId: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val userId: UserId? = null,
    override val ipAddress: String? = null,
    override val userAgent: String? = null,
    val operationType: BatchOperationType,
    val targetUserIds: List<UserId>,
    val operatedBy: UserId,
    val reason: String? = null,
    val parameters: Map<String, Any>? = null
) : UserCommand()

/**
 * 批量操作类型
 */
enum class BatchOperationType {
    ACTIVATE,
    DEACTIVATE,
    LOCK,
    UNLOCK,
    DELETE,
    RESET_PASSWORD,
    SEND_VERIFICATION_EMAIL,
    EXPORT_DATA
}

/**
 * 命令执行结果
 */
sealed class CommandResult {
    data class Success(val message: String? = null, val data: Any? = null) : CommandResult()
    data class Failure(val error: String, val errorCode: String? = null, val details: Map<String, Any>? = null) : CommandResult()
    data class ValidationError(val errors: Map<String, List<String>>) : CommandResult()
}

/**
 * 命令处理器接口
 */
interface CommandHandler<T : UserCommand> {
    /**
     * 处理命令
     * @param command 要处理的命令
     * @return 命令执行结果
     */
    suspend fun handle(command: T): CommandResult
    
    /**
     * 验证命令
     * @param command 要验证的命令
     * @return 验证结果
     */
    fun validate(command: T): CommandResult
}

/**
 * 命令总线接口
 */
interface CommandBus {
    /**
     * 发送命令
     * @param command 要发送的命令
     * @return 命令执行结果
     */
    suspend fun <T : UserCommand> send(command: T): CommandResult
    
    /**
     * 注册命令处理器
     * @param commandClass 命令类型
     * @param handler 命令处理器
     */
    fun <T : UserCommand> register(commandClass: Class<T>, handler: CommandHandler<T>)
}