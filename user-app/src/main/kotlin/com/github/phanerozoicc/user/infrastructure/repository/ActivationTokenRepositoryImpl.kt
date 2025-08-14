package com.github.phanerozoicc.user.infrastructure.repository

import com.github.phanerozoicc.user.domain.model.ActivationToken
import com.github.phanerozoicc.user.domain.repository.ActivationTokenRepository
import com.github.phanerozoicc.user.infrastructure.persistence.repository.JpaActivationTokenRepository
import org.springframework.stereotype.Repository

@Repository
class ActivationTokenRepositoryImpl(
    private val jpaActivationTokenRepository: JpaActivationTokenRepository
): ActivationTokenRepository {
    override fun save(activationToken: ActivationToken) {
        jpaActivationTokenRepository.save(activationToken)
    }

}

