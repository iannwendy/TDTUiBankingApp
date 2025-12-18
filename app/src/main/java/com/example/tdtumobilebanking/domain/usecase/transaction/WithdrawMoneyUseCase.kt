package com.example.tdtumobilebanking.domain.usecase.transaction

import com.example.tdtumobilebanking.domain.repository.AccountRepository
import com.example.tdtumobilebanking.domain.repository.TransactionRepository
import javax.inject.Inject

class WithdrawMoneyUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        accountId: String,
        amount: Double,
        description: String = "Rút tiền"
    ): com.example.tdtumobilebanking.core.common.ResultState<Unit> {
        // Get current account balance
        val accountResult = accountRepository.getAccount(accountId)
        return when (accountResult) {
            is com.example.tdtumobilebanking.core.common.ResultState.Success -> {
                val account = accountResult.data
                if (account == null) {
                    com.example.tdtumobilebanking.core.common.ResultState.Error(Exception("Tài khoản không tồn tại"))
                } else if (account.balance < amount) {
                    com.example.tdtumobilebanking.core.common.ResultState.Error(Exception("Số dư không đủ"))
                } else {
                    // Calculate new balance (subtract amount)
                    val newBalance = account.balance - amount
                    
                    // Update account balance
                    val updateResult = accountRepository.updateBalance(accountId, newBalance)
                    if (updateResult is com.example.tdtumobilebanking.core.common.ResultState.Success) {
                        // Create transaction record for withdraw
                        transactionRepository.createDepositWithdrawTransaction(
                            accountId = accountId,
                            amount = amount,
                            description = description,
                            type = "WITHDRAW"
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

