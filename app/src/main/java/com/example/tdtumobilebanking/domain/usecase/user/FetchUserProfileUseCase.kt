package com.example.tdtumobilebanking.domain.usecase.user

import com.example.tdtumobilebanking.domain.repository.UserRepository
import javax.inject.Inject

class FetchUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(uid: String) = repository.getUserProfile(uid)
}

