package com.example.tdtumobilebanking.domain.usecase.account

import com.example.tdtumobilebanking.domain.model.Account
import com.example.tdtumobilebanking.domain.repository.AccountRepository
import javax.inject.Inject

class CreateAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(account: Account) = accountRepository.createOrUpdateAccount(account)
}

