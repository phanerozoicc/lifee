package com.lifee.user.domain

import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.domain.events.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.Instant
import java.time.LocalDate

/**
 * User聚合根测试
 */
class UserTest : BehaviorSpec({
    
    given("一个新创建的用户") {
        val userId = UserId.generate()
        val email = Email("test@example.com")
        val password = Password("Password123!")
        val user = User.create(
            id = userId,
            email = email,
            password = password,
            firstName = "John",
            lastName = "Doe"
        )
        
        `when`("创建用户时") {
            then("应该设置正确的初始状态") {
                user.getId() shouldBe userId
                user.getEmail() shouldBe email
                user.getPassword() shouldBe password
                user.getStatus() shouldBe UserStatus.PENDING_ACTIVATION
                user.isEmailVerified() shouldBe false
                user.getLastLoginAt() shouldBe null
                user.getActivatedAt() shouldBe null
                user.isActivated() shouldBe false
            }
            
            then("应该发布用户注册事件") {
                val events = user.getUncommittedEvents()
                events.size shouldBe 1
                events.first().shouldBeInstanceOf<UserRegisteredEvent>()
                
                val event = events.first() as UserRegisteredEvent
                event.userId shouldBe userId
                event.email shouldBe email
                event.firstName shouldBe "John"
                event.lastName shouldBe "Doe"
            }
        }
        
        `when`("激活用户") {
            user.activate()
            
            then("应该更新用户状态") {
                user.getStatus() shouldBe UserStatus.ACTIVE
                user.isEmailVerified() shouldBe true
                user.getActivatedAt() shouldNotBe null
                user.isActivated() shouldBe true
            }
            
            then("应该发布激活事件") {
                val events = user.getUncommittedEvents()
                val activatedEvent = events.find { it is UserActivatedEvent }
                activatedEvent shouldNotBe null
                
                val statusChangedEvent = events.find { it is UserStatusChangedEvent }
                statusChangedEvent shouldNotBe null
                
                val statusEvent = statusChangedEvent as UserStatusChangedEvent
                statusEvent.oldStatus shouldBe UserStatus.PENDING_ACTIVATION
                statusEvent.newStatus shouldBe UserStatus.ACTIVE
            }
        }
        
        `when`("尝试重复激活已激活的用户") {
            user.activate()
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    user.activate()
                }
            }
        }
    }
    
    given("一个已激活的用户") {
        val userId = UserId.generate()
        val email = Email("active@example.com")
        val password = Password("Password123!")
        val user = User.create(
            id = userId,
            email = email,
            password = password,
            firstName = "Jane",
            lastName = "Smith"
        )
        user.activate()
        user.markEventsAsCommitted() // 清除之前的事件
        
        `when`("停用用户") {
            val reason = "违反使用条款"
            user.suspend(reason)
            
            then("应该更新用户状态") {
                user.getStatus() shouldBe UserStatus.SUSPENDED
            }
            
            then("应该发布状态变更事件") {
                val events = user.getUncommittedEvents()
                val statusChangedEvent = events.find { it is UserStatusChangedEvent }
                statusChangedEvent shouldNotBe null
                
                val event = statusChangedEvent as UserStatusChangedEvent
                event.oldStatus shouldBe UserStatus.ACTIVE
                event.newStatus shouldBe UserStatus.SUSPENDED
                event.reason shouldBe reason
            }
        }
        
        `when`("更新用户档案") {
            val newFirstName = "UpdatedJane"
            val newLastName = "UpdatedSmith"
            val dateOfBirth = LocalDate.of(1990, 1, 1)
            val phoneNumber = "+1234567890"
            val avatar = "https://example.com/avatar.jpg"
            
            user.updateProfile(
                firstName = newFirstName,
                lastName = newLastName,
                dateOfBirth = dateOfBirth,
                phoneNumber = phoneNumber,
                avatar = avatar
            )
            
            then("应该更新档案信息") {
                val profile = user.getProfile()
                profile.getFirstName() shouldBe newFirstName
                profile.getLastName() shouldBe newLastName
                profile.getDateOfBirth() shouldBe dateOfBirth
                profile.getPhoneNumber() shouldBe phoneNumber
                profile.getAvatar() shouldBe avatar
            }
            
            then("应该发布档案更新事件") {
                val events = user.getUncommittedEvents()
                val profileUpdatedEvent = events.find { it is UserProfileUpdatedEvent }
                profileUpdatedEvent shouldNotBe null
            }
        }
        
        `when`("软删除用户") {
            val reason = "用户请求删除账户"
            user.delete(reason)
            
            then("应该更新用户状态为已删除") {
                user.getStatus() shouldBe UserStatus.DELETED
            }
            
            then("应该发布状态变更事件") {
                val events = user.getUncommittedEvents()
                val statusChangedEvent = events.find { it is UserStatusChangedEvent }
                statusChangedEvent shouldNotBe null
                
                val event = statusChangedEvent as UserStatusChangedEvent
                event.newStatus shouldBe UserStatus.DELETED
                event.reason shouldBe reason
            }
        }
    }
    
    given("一个被停用的用户") {
        val userId = UserId.generate()
        val email = Email("suspended@example.com")
        val password = Password("Password123!")
        val user = User.create(
            id = userId,
            email = email,
            password = password,
            firstName = "Suspended",
            lastName = "User"
        )
        user.activate()
        user.suspend("测试停用")
        user.markEventsAsCommitted()
        
        `when`("恢复用户") {
            user.reactivate()
            
            then("应该恢复为活跃状态") {
                user.getStatus() shouldBe UserStatus.ACTIVE
            }
            
            then("应该发布状态变更事件") {
                val events = user.getUncommittedEvents()
                val statusChangedEvent = events.find { it is UserStatusChangedEvent }
                statusChangedEvent shouldNotBe null
                
                val event = statusChangedEvent as UserStatusChangedEvent
                event.oldStatus shouldBe UserStatus.SUSPENDED
                event.newStatus shouldBe UserStatus.ACTIVE
            }
        }
    }
    
    given("一个已删除的用户") {
        val userId = UserId.generate()
        val email = Email("deleted@example.com")
        val password = Password("Password123!")
        val user = User.create(
            id = userId,
            email = email,
            password = password,
            firstName = "Deleted",
            lastName = "User"
        )
        user.activate()
        user.delete("测试删除")
        user.markEventsAsCommitted()
        
        `when`("尝试更新已删除用户的档案") {
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    user.updateProfile(firstName = "NewName")
                }
            }
        }
        
        `when`("尝试停用已删除的用户") {
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    user.suspend("不能停用已删除的用户")
                }
            }
        }
        
        `when`("尝试重复删除用户") {
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    user.delete("重复删除")
                }
            }
        }
    }
})