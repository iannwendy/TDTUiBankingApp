package com.example.tdtumobilebanking.domain.usecase.transaction

import com.example.tdtumobilebanking.domain.repository.AccountRepository
import com.example.tdtumobilebanking.domain.repository.TransactionRepository
import javax.inject.Inject

class DepositMoneyUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        accountId: String,
        amount: Double,
        description: String = "Nạp tiền"
    ): com.example.tdtumobilebanking.core.common.ResultState<Unit> {
        // Get current account balance
        val accountResult = accountRepository.getAccount(accountId)
        return when (accountResult) {
            is com.example.tdtumobilebanking.core.common.ResultState.Success -> {
                val account = accountResult.data
                if (account == null) {
                    com.example.tdtumobilebanking.core.common.ResultState.Error(Exception("Tài khoản không tồn tại"))
                } else {
                    // Calculate new balance (add amount)
                    val newBalance = account.balance + amount
                    
                    // Update account balance
                    val updateResult = accountRepository.updateBalance(accountId, newBalance)
                    if (updateResult is com.example.tdtumobilebanking.core.common.ResultState.Success) {
                        // Create transaction record for deposit
                        transactionRepository.createDepositWithdrawTransaction(
                            accountId = accountId,
                            amount = amount,
                            description = description,
                            type = "DEPOSIT"
                        )
                    } else {
                        updateResult
                    }
                }
            }
            is com.example.tdtumobilebanking.core.common.ResultState.Error -> accountResult
            else -> com.example.tdtumobilebanking.core.common.ResultState.Error(Exception("Đang xử lý..."))
        }
    }
}

