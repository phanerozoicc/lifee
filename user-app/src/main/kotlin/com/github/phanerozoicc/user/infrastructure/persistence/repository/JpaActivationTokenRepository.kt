package com.github.phanerozoicc.user.infrastructure.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository

class JpaActivationTokenRepository: JpaRepository<ActivationTokenEntity, String> {
}