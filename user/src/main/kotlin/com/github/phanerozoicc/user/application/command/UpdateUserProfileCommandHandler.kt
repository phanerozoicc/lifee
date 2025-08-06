package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.domain.DomainEventPublisher
import com.github.phanerozoicc.user.bak.domain.cqrs.CommandResult
import com.github.phanerozoicc.user.bak.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.service.UserDomainService
import com.github.phanerozoicc.user.domain.model.Gender
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.model.UserProfile
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime


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
 * 更新用户资料命令处理器
 */
@Service
class UpdateUserProfileCommandHandler(
    private val userRepository: UserRepository,
    private val userDomainService: UserDomainService,
    private val domainEventPublisher: DomainEventPublisher
) : CommandHandler<UpdateUserProfileCommand> {

    override suspend fun handle(command: UpdateUserProfileCommand): CommandResult {
        try {
            // 验证命令
            val validationResult = validate(command)
            if (validationResult is CommandResult.ValidationError) {
                return validationResult
            }

            val user = userRepository.findById(command.userId)
                ?: return CommandResult.Failure("用户不存在", "USER_NOT_FOUND")

            // 验证昵称唯一性
            if (command.nickname != null) {
                userDomainService.validateProfileUpdateUniqueness(command.userId, command.nickname)
            }

            // 构建新的用户资料
            val currentProfile = user.getProfile()
            val newProfile = UserProfile.create(
                nickname = command.nickname ?: currentProfile.getNickname(),
                firstName = command.firstName ?: currentProfile.getFirstName(),
                lastName = command.lastName ?: currentProfile.getLastName(),
                avatar = command.avatar ?: currentProfile.getAvatar(),
                bio = command.bio ?: currentProfile.getBio(),
                birthDate = command.birthDate?.let { LocalDate.parse(it) } ?: currentProfile.getBirthDate(),
                gender = command.gender?.let { Gender.valueOf(it) } ?: currentProfile.getGender(),
                phoneNumber = command.phoneNumber ?: currentProfile.getPhoneNumber(),
                address = command.address ?: currentProfile.getAddress(),
                website = command.website ?: currentProfile.getWebsite()
            )

            // 更新用户资料
            user.updateProfile(newProfile, command.updatedBy)

            // 保存用户
            val savedUser = userRepository.save(user)

            // 发布领域事件
            savedUser.getDomainEvents().forEach { event ->
                domainEventPublisher.publish(event)
            }
            savedUser.clearDomainEvents()

            return CommandResult.Success("用户资料更新成功")

        } catch (e: IllegalArgumentException) {
            return CommandResult.Failure(e.message ?: "资料更新失败", "PROFILE_UPDATE_FAILED")
        } catch (e: Exception) {
            return CommandResult.Failure("系统错误，请稍后重试", "SYSTEM_ERROR")
        }
    }

    override fun validate(command: UpdateUserProfileCommand): CommandResult {
        val errors = mutableMapOf<String, MutableList<String>>()

        // 验证生日格式
        if (command.birthDate != null) {
            try {
                LocalDate.parse(command.birthDate)
            } catch (e: Exception) {
                errors.getOrPut("birthDate") { mutableListOf() }.add("生日格式不正确")
            }
        }

        // 验证性别
        if (command.gender != null) {
            try {
                Gender.valueOf(command.gender)
            } catch (e: Exception) {
                errors.getOrPut("gender") { mutableListOf() }.add("性别值不正确")
            }
        }

        return if (errors.isEmpty()) {
            CommandResult.Success()
        } else {
            CommandResult.ValidationError(errors)
        }
    }
}
