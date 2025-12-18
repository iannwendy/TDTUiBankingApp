package com.example.tdtumobilebanking.domain.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.BankInfo

interface BankRepository {
    suspend fun getBanks(): ResultState<List<BankInfo>>
}

