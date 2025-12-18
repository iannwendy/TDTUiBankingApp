package com.example.tdtumobilebanking.domain.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.Branch
import kotlinx.coroutines.flow.Flow

interface BranchRepository {
    fun getBranches(): Flow<ResultState<List<Branch>>>
    suspend fun seedBranches(branches: List<Branch>): ResultState<Unit>
}

