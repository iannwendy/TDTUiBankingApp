package com.example.tdtumobilebanking.domain.model

data class Account(
    val accountId: String = "",
    val ownerId: String = "",
    val accountType: AccountType = AccountType.CHECKING,
    val balance: Double = 0.0,
    val currency: String = "VND",
    val interestRate: Double? = null,
    val termMonth: Int? = null,
    val principalAmount: Double? = null,
    val mortgageRate: Double? = null,
    val termMonths: Int? = null,
    val startDate: Long? = null
)

