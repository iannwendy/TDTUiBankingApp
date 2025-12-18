package com.example.tdtumobilebanking.data.remote.dto

data class TransactionDto(
    val transactionId: String? = null,
    val senderAccountId: String? = null,
    val receiverAccountId: String? = null,
    val amount: Double? = null,
    val type: String? = null,
    val status: String? = null,
    val timestamp: Long? = null,
    val description: String? = null
)

