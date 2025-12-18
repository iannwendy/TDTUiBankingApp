package com.example.tdtumobilebanking.domain.usecase.transaction

import com.example.tdtumobilebanking.domain.repository.TransactionRepository
import javax.inject.Inject

class TransferMoneyUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        senderAccountId: String,
        receiverAccountId: String,
        amount: Double,
        description: String
    ) = transactionRepository.transferInternal(senderAccountId, receiverAccountId, amount, description)
}

