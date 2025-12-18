package com.example.tdtumobilebanking.data.remote.dto

data class AccountDto(
    val accountId: String? = null,
    val ownerId: String? = null,
    val accountType: String? = null,
    val balance: Double? = null,
    val currency: String? = null,
    val interestRate: Double? = null,
    val termMonth: Int? = null,
    val principalAmount: Double? = null,
    val mortgageRate: Double? = null,
    val termMonths: Int? = null,
    val startDate: Long? = null
)

