package com.example.tdtumobilebanking.domain.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsForAccount(accountId: String): Flow<ResultState<List<Transaction>>>
    suspend fun transferInternal(
        senderAccountId: String,
        receiverAccountId: String,
        amount: Double,
        description: String
    ): ResultState<Unit>
    suspend fun createDepositWithdrawTransaction(
        accountId: String,
        amount: Double,
        description: String,
        type: String // "DEPOSIT" or "WITHDRAW"
    ): ResultState<Unit>
    suspend fun createUtilityTransaction(
        accountId: String,
        utilityType: String,
        amount: Double,
        provider: String,
        customerCode: String,
        phoneNumber: String,
        description: String
    ): ResultState<Unit>
}

