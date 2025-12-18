package com.example.tdtumobilebanking.domain.usecase.account

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.AccountType
import com.example.tdtumobilebanking.domain.repository.AccountRepository
import javax.inject.Inject

class UpdateInterestRateUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(accountType: AccountType, newInterestRate: Double): ResultState<Int> {
        if (newInterestRate < 0 || newInterestRate > 100) {
            return ResultState.Error(Exception("Lãi suất phải từ 0% đến 100%"))
        }
        return accountRepository.updateInterestRateForAccountType(accountType, newInterestRate)
    }
}

