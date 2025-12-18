package com.example.tdtumobilebanking.domain.usecase.account

import com.example.tdtumobilebanking.domain.model.Account
import javax.inject.Inject

class CalculateSavingMonthlyProfitUseCase @Inject constructor() {
    operator fun invoke(account: Account): Double {
        val rate = account.interestRate ?: return 0.0
        return (account.balance * rate) / 12
    }
}

