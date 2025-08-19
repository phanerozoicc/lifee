package com.lifee.user.domain

import com.lifee.common.exceptions.BusinessRuleException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDate

/**
 * UserProfile值对象测试
 */
class UserProfileTest : BehaviorSpec({
    
    given("有效的用户档案信息") {
        val firstName = "John"
        val lastName = "Doe"
        val dateOfBirth = LocalDate.of(1990, 1, 15)
        val phoneNumber = "+1234567890"
        val avatar = "https://example.com/avatar.jpg"
        
        `when`("创建完整的用户档案") {
            val profile = UserProfile.create(
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = dateOfBirth,
                phoneNumber = phoneNumber,
                avatar = avatar
            )
            
            then("应该正确设置所有属性") {
                profile.getFirstName() shouldBe firstName
                profile.getLastName() shouldBe lastName
                profile.getFullName() shouldBe "$firstName $lastName"
                profile.getDateOfBirth() shouldBe dateOfBirth
                profile.getPhoneNumber() shouldBe phoneNumber
                profile.getAvatar() shouldBe avatar
            }
        }
        
        `when`("创建最小用户档案（只有姓名）") {
            val profile = UserProfile.create(
                firstName = firstName,
                lastName = lastName
            )
            
            then("应该正确设置必需属性") {
                profile.getFirstName() shouldBe firstName
                profile.getLastName() shouldBe lastName
                profile.getFullName() shouldBe "$firstName $lastName"
                profile.getDateOfBirth() shouldBe null
                profile.getPhoneNumber() shouldBe null
                profile.getAvatar() shouldBe null
            }
        }
    }
    
    given("无效的用户档案信息") {
        `when`("名字为空或空白") {
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    UserProfile.create("", "Doe")
                }
                
                shouldThrow<BusinessRuleException> {
                    UserProfile.create("   ", "Doe")
                }
            }
        }
        
        `when`("姓氏为空或空白") {
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    UserProfile.create("John", "")
                }
                
                shouldThrow<BusinessRuleException> {
                    UserProfile.create("John", "   ")
                }
            }
        }
        
        `when`("名字过长") {
            val longName = "a".repeat(51)
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    UserProfile.create(longName, "Doe")
                }
            }
        }
        
        `when`("姓氏过长") {
            val longLastName = "a".repeat(51)
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    UserProfile.create("John", longLastName)
                }
            }
        }
        
        `when`("出生日期在未来") {
            val futureDate = LocalDate.now().plusDays(1)
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    UserProfile.create(
                        firstName = "John",
                        lastName = "Doe",
                        dateOfBirth = futureDate
                    )
                }
            }
        }
        
        `when`("年龄小于13岁") {
            val tooYoungDate = LocalDate.now().minusYears(12)
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    UserProfile.create(
                        firstName = "John",
                        lastName = "Doe",
                        dateOfBirth = tooYoungDate
                    )
                }
            }
        }
        
        `when`("年龄超过150岁") {
            val tooOldDate = LocalDate.now().minusYears(151)
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    UserProfile.create(
                        firstName = "John",
                        lastName = "Doe",
                        dateOfBirth = tooOldDate
                    )
                }
            }
        }
        
        `when`("电话号码格式无效") {
            val invalidPhones = listOf(
                "123", // 太短
                "abc123456789", // 包含字母
                "123-456-7890-1234-5678", // 太长
                "++1234567890", // 多个加号
                "1234 5678 9012 3456" // 太长
            )
            
            invalidPhones.forEach { invalidPhone ->
                then("应该抛出业务规则异常: $invalidPhone") {
                    shouldThrow<BusinessRuleException> {
                        UserProfile.create(
                            firstName = "John",
                            lastName = "Doe",
                            phoneNumber = invalidPhone
                        )
                    }
                }
            }
        }
        
        `when`("头像URL格式无效") {
            val invalidAvatars = listOf(
                "not-a-url",
                "ftp://example.com/avatar.jpg", // 不支持的协议
                "http://", // 不完整的URL
                "https://" // 不完整的URL
            )
            
            invalidAvatars.forEach { invalidAvatar ->
                then("应该抛出业务规则异常: $invalidAvatar") {
                    shouldThrow<BusinessRuleException> {
                        UserProfile.create(
                            firstName = "John",
                            lastName = "Doe",
                            avatar = invalidAvatar
                        )
                    }
                }
            }
        }
    }
    
    given("有效的电话号码格式") {
        val validPhones = listOf(
            "+1234567890",
            "+86-138-0013-8000",
            "(555) 123-4567",
            "555-123-4567",
            "555.123.4567",
            "555 123 4567",
            "+1 (555) 123-4567"
        )
        
        `when`("使用有效的电话号码") {
            validPhones.forEach { validPhone ->
                then("应该成功创建档案: $validPhone") {
                    val profile = UserProfile.create(
                        firstName = "John",
                        lastName = "Doe",
                        phoneNumber = validPhone
                    )
                    profile.getPhoneNumber() shouldBe validPhone
                }
            }
        }
    }
    
    given("有效的头像URL格式") {
        val validAvatars = listOf(
            "https://example.com/avatar.jpg",
            "http://example.com/avatar.png",
            "https://cdn.example.com/users/123/avatar.gif",
            "https://example.com/avatar.jpeg",
            "https://example.com/avatar.webp"
        )
        
        `when`("使用有效的头像URL") {
            validAvatars.forEach { validAvatar ->
                then("应该成功创建档案: $validAvatar") {
                    val profile = UserProfile.create(
                        firstName = "John",
                        lastName = "Doe",
                        avatar = validAvatar
                    )
                    profile.getAvatar() shouldBe validAvatar
                }
            }
        }
    }
    
    given("用户档案更新") {
        val originalProfile = UserProfile.create(
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(1990, 1, 15),
            phoneNumber = "+1234567890",
            avatar = "https://example.com/old-avatar.jpg"
        )
        
        `when`("更新部分信息") {
            val updatedProfile = originalProfile.updateProfile(
                firstName = "Jane",
                avatar = "https://example.com/new-avatar.jpg"
            )
            
            then("应该只更新指定的字段") {
                updatedProfile.getFirstName() shouldBe "Jane"
                updatedProfile.getLastName() shouldBe "Doe" // 保持不变
                updatedProfile.getDateOfBirth() shouldBe LocalDate.of(1990, 1, 15) // 保持不变
                updatedProfile.getPhoneNumber() shouldBe "+1234567890" // 保持不变
                updatedProfile.getAvatar() shouldBe "https://example.com/new-avatar.jpg"
            }
        }
        
        `when`("更新所有信息") {
            val newDateOfBirth = LocalDate.of(1985, 12, 25)
            val updatedProfile = originalProfile.updateProfile(
                firstName = "Jane",
                lastName = "Smith",
                dateOfBirth = newDateOfBirth,
                phoneNumber = "+9876543210",
                avatar = "https://example.com/new-avatar.jpg"
            )
            
            then("应该更新所有字段") {
                updatedProfile.getFirstName() shouldBe "Jane"
                updatedProfile.getLastName() shouldBe "Smith"
                updatedProfile.getFullName() shouldBe "Jane Smith"
                updatedProfile.getDateOfBirth() shouldBe newDateOfBirth
                updatedProfile.getPhoneNumber() shouldBe "+9876543210"
                updatedProfile.getAvatar() shouldBe "https://example.com/new-avatar.jpg"
            }
        }
        
        `when`("不提供任何更新参数") {
            val updatedProfile = originalProfile.updateProfile()
            
            then("应该返回相同的档案") {
                updatedProfile shouldBe originalProfile
            }
        }
    }
    
    given("两个相同的用户档案") {
        val profile1 = UserProfile.create(
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(1990, 1, 15),
            phoneNumber = "+1234567890",
            avatar = "https://example.com/avatar.jpg"
        )
        
        val profile2 = UserProfile.create(
            firstName = "John",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(1990, 1, 15),
            phoneNumber = "+1234567890",
            avatar = "https://example.com/avatar.jpg"
        )
        
        `when`("比较两个档案") {
            then("应该相等") {
                profile1 shouldBe profile2
                profile1.hashCode() shouldBe profile2.hashCode()
            }
        }
    }
    
    given("两个不同的用户档案") {
        val profile1 = UserProfile.create("John", "Doe")
        val profile2 = UserProfile.create("Jane", "Smith")
        
        `when`("比较两个档案") {
            then("应该不相等") {
                profile1 shouldNotBe profile2
            }
        }
    }
})