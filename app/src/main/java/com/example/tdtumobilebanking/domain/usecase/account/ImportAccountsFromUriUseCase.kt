package com.example.tdtumobilebanking.domain.usecase.account

import android.content.Context
import android.net.Uri
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.Account
import com.example.tdtumobilebanking.domain.model.AccountType
import com.example.tdtumobilebanking.domain.model.KycStatus
import com.example.tdtumobilebanking.domain.model.User
import com.example.tdtumobilebanking.domain.repository.AccountRepository
import com.example.tdtumobilebanking.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class ImportAccountsFromUriUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(uri: Uri): ResultState<Unit> = try {
        val input = context.contentResolver.openInputStream(uri)
            ?: return ResultState.Error(IllegalStateException("Không mở được file"))
        importFromStream(input)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    private suspend fun importFromStream(inputStream: java.io.InputStream): ResultState<Unit> = coroutineScope {
        try {
            val users = mutableListOf<User>()
            val accounts = mutableListOf<Account>()
            inputStream.bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val p = line.split(",")
                    if (p.size >= 10) {
                        val accountId = p[0]
                        val ownerId = p[1]
                        val fullName = p[2]
                        val email = p[3]
                        val phone = p[4]
                        val role = p[5]
                        val kyc = p[6]
                        val accountType = p[7]
                        val balance = p[8]
                        val currency = p[9]
                        users.add(
                            User(
                                uid = ownerId,
                                fullName = fullName,
                                email = email,
                                phoneNumber = phone,
                                role = role,
                                kycStatus = runCatching { KycStatus.valueOf(kyc) }.getOrDefault(KycStatus.NONE),
                                avatarUrl = ""
                            )
                        )
                        accounts.add(
                            Account(
                                accountId = accountId,
                                ownerId = ownerId,
                                accountType = runCatching { AccountType.valueOf(accountType) }.getOrDefault(AccountType.CHECKING),
                                balance = balance.toDoubleOrNull() ?: 0.0,
                                currency = currency
                            )
                        )
                    }
                }
            }
            val userJob = async { users.forEach { userRepository.createOrUpdateUser(it) } }
            val accJob = async { accountRepository.importAccounts(accounts) }
            userJob.await()
            accJob.await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e)
        }
    }
}

