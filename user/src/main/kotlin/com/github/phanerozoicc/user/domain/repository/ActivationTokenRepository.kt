package com.github.phanerozoicc.user.domain.repository

import com.github.phanerozoicc.user.domain.model.ActivationToken

interface ActivationTokenRepository {
    fun save(activationToken: ActivationToken)
}