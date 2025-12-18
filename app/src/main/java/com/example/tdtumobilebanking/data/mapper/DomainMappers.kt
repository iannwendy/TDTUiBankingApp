package com.example.tdtumobilebanking.data.mapper

import com.example.tdtumobilebanking.data.remote.dto.AccountDto
import com.example.tdtumobilebanking.data.remote.dto.BranchDto
import com.example.tdtumobilebanking.data.remote.dto.TransactionDto
import com.example.tdtumobilebanking.data.remote.dto.UserDto
import com.example.tdtumobilebanking.domain.model.Account
import com.example.tdtumobilebanking.domain.model.AccountType
import com.example.tdtumobilebanking.domain.model.Branch
import com.example.tdtumobilebanking.domain.model.KycStatus
import com.example.tdtumobilebanking.domain.model.Transaction
import com.example.tdtumobilebanking.domain.model.TransactionStatus
import com.example.tdtumobilebanking.domain.model.TransactionType
import com.example.tdtumobilebanking.domain.model.User

fun UserDto.toDomain(): User = User(
    uid = uid.orEmpty(),
    fullName = fullName.orEmpty(),
    email = email.orEmpty(),
    role = role.orEmpty(),
    phoneNumber = phoneNumber.orEmpty(),
    kycStatus = runCatching { KycStatus.valueOf(kycStatus.orEmpty()) }.getOrDefault(KycStatus.NONE),
    avatarUrl = avatarUrl.orEmpty()
)

fun User.toDto(): UserDto = UserDto(
    uid = uid,
    fullName = fullName,
    email = email,
    role = role,
    phoneNumber = phoneNumber,
    kycStatus = kycStatus.name,
    avatarUrl = avatarUrl
)

fun AccountDto.toDomain(): Account = Account(
    accountId = accountId.orEmpty(),
    ownerId = ownerId.orEmpty(),
    accountType = runCatching { AccountType.valueOf(accountType.orEmpty()) }.getOrDefault(AccountType.CHECKING),
    balance = balance ?: 0.0,
    currency = currency ?: "VND",
    interestRate = interestRate,
    termMonth = termMonth,
    principalAmount = principalAmount,
    mortgageRate = mortgageRate,
    termMonths = termMonths,
    startDate = startDate
)

fun Account.toDto(): AccountDto = AccountDto(
    accountId = accountId,
    ownerId = ownerId,
    accountType = accountType.name,
    balance = balance,
    currency = currency,
    interestRate = interestRate,
    termMonth = termMonth,
    principalAmount = principalAmount,
    mortgageRate = mortgageRate,
    termMonths = termMonths,
    startDate = startDate
)

fun TransactionDto.toDomain(): Transaction = Transaction(
    transactionId = transactionId.orEmpty(),
    senderAccountId = senderAccountId.orEmpty(),
    receiverAccountId = receiverAccountId.orEmpty(),
    amount = amount ?: 0.0,
    type = runCatching { TransactionType.valueOf(type.orEmpty()) }.getOrDefault(TransactionType.TRANSFER_INTERNAL),
    status = runCatching { TransactionStatus.valueOf(status.orEmpty()) }.getOrDefault(TransactionStatus.SUCCESS),
    timestamp = timestamp ?: 0L,
    description = description.orEmpty()
)

fun Transaction.toDto(): TransactionDto = TransactionDto(
    transactionId = transactionId,
    senderAccountId = senderAccountId,
    receiverAccountId = receiverAccountId,
    amount = amount,
    type = type.name,
    status = status.name,
    timestamp = timestamp,
    description = description
)

fun BranchDto.toDomain(): Branch = Branch(
    branchId = branchId.orEmpty(),
    name = name.orEmpty(),
    latitude = latitude ?: 0.0,
    longitude = longitude ?: 0.0,
    address = address.orEmpty()
)

fun Branch.toDto(): BranchDto = BranchDto(
    branchId = branchId,
    name = name,
    latitude = latitude,
    longitude = longitude,
    address = address
)

