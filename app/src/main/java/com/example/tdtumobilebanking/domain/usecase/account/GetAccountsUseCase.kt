package com.example.tdtumobilebanking.domain.usecase.account

import com.example.tdtumobilebanking.domain.repository.AccountRepository
import javax.inject.Inject

class GetAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(ownerId: String) = repository.getAccountsForUser(ownerId)
    suspend fun getById(accountId: String) = repository.getAccount(accountId)
}

