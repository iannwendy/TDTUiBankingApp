package com.example.tdtumobilebanking.domain.usecase.account

import com.example.tdtumobilebanking.domain.model.Account
import javax.inject.Inject
import kotlin.math.pow

class CalculateMortgagePaymentUseCase @Inject constructor() {
    operator fun invoke(account: Account): Double {
        val principal = account.principalAmount ?: return 0.0
        val annualRate = account.mortgageRate ?: return 0.0
        val months = account.termMonths ?: return 0.0
        val monthlyRate = annualRate / 12.0
        if (monthlyRate == 0.0 || months == 0) return 0.0
        val numerator = monthlyRate * (1 + monthlyRate).pow(months)
        val denominator = (1 + monthlyRate).pow(months) - 1
        return principal * (numerator / denominator)
    }
}

