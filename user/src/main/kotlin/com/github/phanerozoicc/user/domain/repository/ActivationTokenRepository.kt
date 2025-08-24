package com.github.phanerozoicc.user.domain.repository

import com.github.phanerozoicc.user.domain.model.ActivationToken
import com.github.phanerozoicc.user.domain.model.UserId

interface ActivationTokenRepository {
    fun save(activationToken: ActivationToken)
    fun findByToken(token: String): ActivationToken?
    fun delete(activationToken: ActivationToken)
    fun findByUserId(id: UserId): ActivationToken?
}