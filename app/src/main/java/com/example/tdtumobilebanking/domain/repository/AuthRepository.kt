package com.example.tdtumobilebanking.domain.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): ResultState<User>
    suspend fun logout(): ResultState<Unit>
    fun currentUser(): Flow<User?>
}

