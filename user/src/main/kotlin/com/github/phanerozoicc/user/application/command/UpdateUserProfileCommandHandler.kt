package com.github.phanerozoicc.user.application.command

import com.github.phanerozoicc.base.command.Command
import com.github.phanerozoicc.base.command.CommandHandler
import com.github.phanerozoicc.base.event.EventBus
import com.github.phanerozoicc.user.domain.model.Gender
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.model.UserProfile
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.service.UserDomainService
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate


/**
 * 更新用户资料命令
 */
data class UpdateUserProfileCommand(
    val userId: UserId,
    val nickname: String?,
    val firstName: String?,
    val lastName: String?,
    val avatar: String?,
    val bio: String?,
    val birthDate: LocalDate?,
    val age: Int?,
    val gender: String?,
    val phoneNumber: String?,
    val address: String?,
    val website: String?,
) : Command()


/**
 * 更新用户资料命令处理器
 */
@Service
class UpdateUserProfileCommandHandler(
    private val userDomainService: UserDomainService,
    private val userRepository: UserRepository,
    private val eventBus: EventBus,
    private val transitionTemplate: TransactionTemplate
) : CommandHandler<UpdateUserProfileCommand, Unit> {

    companion object: KLogging()

    override fun handle(command: UpdateUserProfileCommand) {
        try {
            // 验证命令
            val validationResult = validate(command)

            val user = userRepository.findById(command.userId)
                ?: throw IllegalStateException("用户不存在")

            // 验证昵称唯一性
            if (command.nickname != null) {
                userDomainService.validateProfileUpdateUniqueness(command.userId, command.nickname)
            }

            // 构建新的用户资料
            val currentProfile = user.getProfile()
            val newProfile = UserProfile.create(
                nickname = command.nickname ?: currentProfile.nickname,
                firstName = command.firstName ?: currentProfile.firstName,
                lastName = command.lastName ?: currentProfile.lastName,
                avatar = command.avatar ?: currentProfile.avatar,
                bio = command.bio ?: currentProfile.bio,
                birthDate = command.birthDate ?: currentProfile.birthDate,
                gender = command.gender?.let { Gender.valueOf(it) } ?: currentProfile.gender,
                phoneNumber = command.phoneNumber ?: currentProfile.phoneNumber,
                address = command.address ?: currentProfile.address,
                website = command.website ?: currentProfile.website
            )
            user.updateProfile(newProfile)

            // 更新用户资料
            transitionTemplate.execute {
                val savedUser = runBlocking {
                    // 保存用户
                    userRepository.save(user)
                }
                // 发布领域事件
                eventBus.publishAll(savedUser.getUnCommittedEvents())
                savedUser.markEventsAsCommitted()
            }
        } catch (e: Exception) {
            logger.error("更新用户资料失败: userId=${command.userId.value}", e)
            throw e
        }
    }

    fun validate(command: UpdateUserProfileCommand) {
        val errors = mutableMapOf<String, MutableList<String>>()
        // 验证性别
        if (command.gender != null) {
            try {
                Gender.valueOf(command.gender)
            } catch (e: Exception) {
                errors.getOrPut("gender") { mutableListOf() }.add("性别值不正确")
            }
        }
        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.toString())
        }
    }
}
