package com.lifee.user.app.commands

import com.lifee.common.cqrs.commands.Command
import java.time.LocalDate
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size

/**
 * 更新用户档案命令
 */
data class UpdateUserProfileCommand(
    @field:NotBlank(message = "用户ID不能为空")
    val userId: String,
    
    @field:Size(max = 50, message = "名字长度不能超过50个字符")
    val firstName: String? = null,
    
    @field:Size(max = 50, message = "姓氏长度不能超过50个字符")
    val lastName: String? = null,
    
    @field:Past(message = "出生日期不能是未来日期")
    val dateOfBirth: LocalDate? = null,
    
    @field:Size(max = 20, message = "电话号码长度不能超过20个字符")
    val phoneNumber: String? = null,
    
    @field:Size(max = 500, message = "头像URL长度不能超过500个字符")
    val avatar: String? = null
) : Command