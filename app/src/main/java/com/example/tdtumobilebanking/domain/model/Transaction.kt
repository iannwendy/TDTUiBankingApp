package com.example.tdtumobilebanking.domain.model

data class Transaction(
    val transactionId: String = "",
    val senderAccountId: String = "",
    val receiverAccountId: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.TRANSFER_INTERNAL,
    val status: TransactionStatus = TransactionStatus.SUCCESS,
    val timestamp: Long = 0L,
    val description: String = ""
)

