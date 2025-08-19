package com.lifee.user.domain

import com.lifee.common.exceptions.BusinessRuleException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Password值对象测试
 */
class PasswordTest : BehaviorSpec({
    
    given("有效的密码") {
        val validPasswords = listOf(
            "Password123!",
            "MySecure@Pass1",
            "Complex#Password2023",
            "Test$Password99",
            "Secure&Pass123"
        )
        
        `when`("创建Password对象") {
            validPasswords.forEach { passwordStr ->
                then("应该成功创建Password对象: $passwordStr") {
                    val password = Password(passwordStr)
                    password.value shouldNotBe passwordStr // 应该是加密后的值
                }
            }
        }
    }
    
    given("无效的密码") {
        val invalidPasswords = mapOf(
            "" to "密码不能为空",
            "   " to "密码不能为空白",
            "123" to "密码长度不足",
            "password" to "密码必须包含大写字母",
            "PASSWORD" to "密码必须包含小写字母",
            "Password" to "密码必须包含数字",
            "Password123" to "密码必须包含特殊字符",
            "Pass!1" to "密码长度不足8位",
            "a".repeat(129) to "密码长度超过限制"
        )
        
        `when`("尝试创建Password对象") {
            invalidPasswords.forEach { (passwordStr, reason) ->
                then("应该抛出业务规则异常: $reason") {
                    shouldThrow<BusinessRuleException> {
                        Password(passwordStr)
                    }
                }
            }
        }
    }
    
    given("密码验证") {
        val plainPassword = "MySecure@Pass123"
        val password = Password(plainPassword)
        
        `when`("使用正确的明文密码验证") {
            then("应该验证成功") {
                password.matches(plainPassword) shouldBe true
            }
        }
        
        `when`("使用错误的明文密码验证") {
            then("应该验证失败") {
                password.matches("WrongPassword123!") shouldBe false
                password.matches("") shouldBe false
                password.matches("MySecure@Pass124") shouldBe false
            }
        }
    }
    
    given("两个相同明文的密码") {
        val plainPassword = "SamePassword123!"
        val password1 = Password(plainPassword)
        val password2 = Password(plainPassword)
        
        `when`("比较两个Password对象") {
            then("加密值应该不同（使用了盐值）") {
                password1.value shouldNotBe password2.value
            }
            
            then("但都能验证相同的明文密码") {
                password1.matches(plainPassword) shouldBe true
                password2.matches(plainPassword) shouldBe true
            }
        }
    }
    
    given("密码强度要求") {
        `when`("密码包含所有必需元素") {
            val strongPassword = "StrongPass123!@#"
            
            then("应该成功创建") {
                val password = Password(strongPassword)
                password.matches(strongPassword) shouldBe true
            }
        }
        
        `when`("密码缺少大写字母") {
            then("应该抛出异常") {
                shouldThrow<BusinessRuleException> {
                    Password("weakpass123!")
                }
            }
        }
        
        `when`("密码缺少小写字母") {
            then("应该抛出异常") {
                shouldThrow<BusinessRuleException> {
                    Password("WEAKPASS123!")
                }
            }
        }
        
        `when`("密码缺少数字") {
            then("应该抛出异常") {
                shouldThrow<BusinessRuleException> {
                    Password("WeakPass!")
                }
            }
        }
        
        `when`("密码缺少特殊字符") {
            then("应该抛出异常") {
                shouldThrow<BusinessRuleException> {
                    Password("WeakPass123")
                }
            }
        }
    }
    
    given("密码长度边界测试") {
        `when`("密码长度正好8位") {
            val minPassword = "Pass123!"
            
            then("应该成功创建") {
                val password = Password(minPassword)
                password.matches(minPassword) shouldBe true
            }
        }
        
        `when`("密码长度正好128位") {
            val maxPassword = "P" + "a".repeat(120) + "123!@#"
            
            then("应该成功创建") {
                val password = Password(maxPassword)
                password.matches(maxPassword) shouldBe true
            }
        }
        
        `when`("密码长度7位") {
            then("应该抛出异常") {
                shouldThrow<BusinessRuleException> {
                    Password("Pass12!")
                }
            }
        }
        
        `when`("密码长度129位") {
            val tooLongPassword = "P" + "a".repeat(121) + "123!@#"
            
            then("应该抛出异常") {
                shouldThrow<BusinessRuleException> {
                    Password(tooLongPassword)
                }
            }
        }
    }
    
    given("常见弱密码检测") {
        val commonWeakPasswords = listOf(
            "Password123!", // 太常见
            "123456789!", // 连续数字
            "Qwerty123!", // 键盘序列
            "Admin123!", // 常见词汇
            "Welcome123!" // 常见词汇
        )
        
        `when`("使用常见弱密码") {
            commonWeakPasswords.forEach { weakPassword ->
                then("可能需要额外的弱密码检测: $weakPassword") {
                    // 注意：这里我们仍然允许创建，但在实际应用中可能需要额外的弱密码检测
                    val password = Password(weakPassword)
                    password.matches(weakPassword) shouldBe true
                }
            }
        }
    }
})