package com.example.tdtumobilebanking.data.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.data.mapper.toDomain
import com.example.tdtumobilebanking.data.remote.api.BankApi
import com.example.tdtumobilebanking.domain.model.BankInfo
import com.example.tdtumobilebanking.domain.repository.BankRepository
import javax.inject.Inject

class BankRepositoryImpl @Inject constructor(
    private val bankApi: BankApi
) : BankRepository {
    override suspend fun getBanks(): ResultState<List<BankInfo>> = try {
        val response = bankApi.getBanks()
        val banks = response.data?.map { it.toDomain() }.orEmpty()
        ResultState.Success(banks)
    } catch (e: Exception) {
        ResultState.Error(e)
    }
}

