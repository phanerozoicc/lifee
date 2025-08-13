package com.github.phanerozoicc.user.domain.service

import com.github.phanerozoicc.user.domain.model.UserId

interface UserIdGenerate {
    /*·
     * 生成新的UserId
     */
    fun generateNext(): UserId

    /*
     * 获取当前的UserId序列号 全局唯一
     */
    fun getCurrentSequence(): Long
}
