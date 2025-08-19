package com.lifee.user.domain

import com.lifee.common.exceptions.BusinessRuleException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Email值对象测试
 */
class EmailTest : BehaviorSpec({
    
    given("有效的邮箱地址") {
        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "123@example.com",
            "test.email.with+symbol@example.com"
        )
        
        `when`("创建Email对象") {
            validEmails.forEach { emailStr ->
                then("应该成功创建Email对象: $emailStr") {
                    val email = Email(emailStr)
                    email.value shouldBe emailStr
                }
            }
        }
    }
    
    given("无效的邮箱地址") {
        val invalidEmails = listOf(
            "",
            "   ",
            "invalid-email",
            "@example.com",
            "test@",
            "test..test@example.com",
            "test@example",
            "test@.com",
            "test@example.",
            "test space@example.com",
            "test@exam ple.com"
        )
        
        `when`("尝试创建Email对象") {
            invalidEmails.forEach { emailStr ->
                then("应该抛出业务规则异常: $emailStr") {
                    shouldThrow<BusinessRuleException> {
                        Email(emailStr)
                    }
                }
            }
        }
    }
    
    given("两个相同的邮箱地址") {
        val emailStr = "test@example.com"
        val email1 = Email(emailStr)
        val email2 = Email(emailStr)
        
        `when`("比较两个Email对象") {
            then("应该相等") {
                email1 shouldBe email2
                email1.hashCode() shouldBe email2.hashCode()
            }
        }
    }
    
    given("两个不同的邮箱地址") {
        val email1 = Email("test1@example.com")
        val email2 = Email("test2@example.com")
        
        `when`("比较两个Email对象") {
            then("应该不相等") {
                email1 shouldNotBe email2
            }
        }
    }
    
    given("邮箱地址的大小写") {
        val email1 = Email("Test@Example.COM")
        val email2 = Email("test@example.com")
        
        `when`("比较不同大小写的邮箱") {
            then("应该相等（忽略大小写）") {
                email1 shouldBe email2
            }
        }
        
        `when`("获取邮箱值") {
            then("应该返回小写格式") {
                email1.value shouldBe "test@example.com"
            }
        }
    }
    
    given("邮箱地址的长度限制") {
        `when`("邮箱地址过长") {
            val longEmail = "a".repeat(250) + "@example.com"
            
            then("应该抛出业务规则异常") {
                shouldThrow<BusinessRuleException> {
                    Email(longEmail)
                }
            }
        }
        
        `when`("邮箱地址在合理长度内") {
            val normalEmail = "test@example.com"
            
            then("应该成功创建") {
                val email = Email(normalEmail)
                email.value shouldBe normalEmail
            }
        }
    }
})