package com.example.tdtumobilebanking.domain.usecase.auth

import com.example.tdtumobilebanking.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.logout()
}

