package com.example.tdtumobilebanking.data.mapper

import com.example.tdtumobilebanking.data.remote.dto.BankDto
import com.example.tdtumobilebanking.domain.model.BankInfo

fun BankDto.toDomain(): BankInfo = BankInfo(
    name = name.orEmpty(),
    code = code.orEmpty(),
    shortName = shortName.orEmpty(),
    logo = logo.orEmpty()
)

