package com.lifee.user.app.commands

import com.lifee.common.cqrs.commands.Command
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 用户注册命令
 */
data class RegisterUserCommand(
    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    val email: String,
    
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 8, max = 128, message = "密码长度必须在8-128位之间")
    val password: String,
    
    @field:NotBlank(message = "名字不能为空")
    @field:Size(max = 50, message = "名字长度不能超过50个字符")
    val firstName: String,
    
    @field:NotBlank(message = "姓氏不能为空")
    @field:Size(max = 50, message = "姓氏长度不能超过50个字符")
    val lastName: String
) : Command