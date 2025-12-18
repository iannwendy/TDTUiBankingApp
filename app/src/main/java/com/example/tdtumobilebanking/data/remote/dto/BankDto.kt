package com.example.tdtumobilebanking.data.remote.dto

data class BankDto(
    val id: Int?,
    val name: String?,
    val code: String?,
    val bin: String?,
    val shortName: String?,
    val logo: String?,
    val transferSupported: Int?,
    val lookupSupported: Int?
)

data class BankListResponse(
    val code: String?,
    val desc: String?,
    val data: List<BankDto>?
)

