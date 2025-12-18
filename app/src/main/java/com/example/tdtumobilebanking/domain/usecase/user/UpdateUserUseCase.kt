package com.example.tdtumobilebanking.domain.usecase.user

import com.example.tdtumobilebanking.domain.model.User
import com.example.tdtumobilebanking.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User) = userRepository.createOrUpdateUser(user)
}

