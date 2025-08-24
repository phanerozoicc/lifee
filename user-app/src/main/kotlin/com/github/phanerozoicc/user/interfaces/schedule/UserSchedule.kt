package com.github.phanerozoicc.user.interfaces.schedule

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UserSchedule {

    /**
     * 定期将为激活的用户标记为锁定状态 等待用户再次登录时触发激活
     * 可用的激活周期为14天 这里暂时设置7天检查一次每周三凌晨3点触发
     */
    @Scheduled(cron = "0 0 3 * * 3")
    fun lockInactiveUsers() {
        // TODO: 待实现
    }
}