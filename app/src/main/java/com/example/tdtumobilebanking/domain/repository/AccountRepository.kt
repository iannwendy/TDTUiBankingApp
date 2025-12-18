package com.example.tdtumobilebanking.domain.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.Account
import com.example.tdtumobilebanking.domain.model.AccountType
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccountsForUser(ownerId: String): Flow<ResultState<List<Account>>>
    suspend fun createOrUpdateAccount(account: Account): ResultState<Unit>
    suspend fun updateBalance(accountId: String, newBalance: Double): ResultState<Unit>
    suspend fun getAccount(accountId: String): ResultState<Account?>
    suspend fun importAccounts(accounts: List<Account>): ResultState<Unit>
    suspend fun updateInterestRateForAccountType(accountType: AccountType, newInterestRate: Double): ResultState<Int>
}

