package com.example.tdtumobilebanking.domain.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.KycStatus
import com.example.tdtumobilebanking.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(uid: String): Flow<ResultState<User>>
    suspend fun createOrUpdateUser(user: User): ResultState<Unit>
    suspend fun updateKycStatus(uid: String, status: KycStatus, avatarUrl: String?): ResultState<Unit>
    fun getAllCustomers(): Flow<ResultState<List<User>>>
}

