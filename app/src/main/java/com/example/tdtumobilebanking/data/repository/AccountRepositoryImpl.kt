package com.example.tdtumobilebanking.data.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.data.mapper.toDomain
import com.example.tdtumobilebanking.data.mapper.toDto
import com.example.tdtumobilebanking.data.remote.dto.AccountDto
import com.example.tdtumobilebanking.domain.model.Account
import com.example.tdtumobilebanking.domain.model.AccountType
import com.example.tdtumobilebanking.domain.repository.AccountRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AccountRepository {

    override fun getAccountsForUser(ownerId: String): Flow<ResultState<List<Account>>> = flow {
        emit(ResultState.Loading)
        try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()
            val accounts = snapshot.documents.mapNotNull { it.toObject(AccountDto::class.java)?.toDomain() }
            emit(ResultState.Success(accounts))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(ResultState.Error(e))
        }
    }

    override suspend fun createOrUpdateAccount(account: Account): ResultState<Unit> = try {
        firestore.collection(COLLECTION).document(account.accountId).set(account.toDto()).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    override suspend fun updateBalance(accountId: String, newBalance: Double): ResultState<Unit> = try {
        firestore.collection(COLLECTION).document(accountId)
            .update("balance", newBalance)
            .await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    override suspend fun getAccount(accountId: String): ResultState<Account?> = try {
        val snapshot = firestore.collection(COLLECTION).document(accountId).get().await()
        ResultState.Success(snapshot.toObject(AccountDto::class.java)?.toDomain())
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    override suspend fun importAccounts(accounts: List<Account>): ResultState<Unit> = try {
        val batch = firestore.batch()
        accounts.forEach { acc ->
            val doc = firestore.collection(COLLECTION).document(acc.accountId)
            batch.set(doc, acc.toDto())
        }
        batch.commit().await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    override suspend fun updateInterestRateForAccountType(accountType: AccountType, newInterestRate: Double): ResultState<Int> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("accountType", accountType.name)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                ResultState.Success(0)
            } else {
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "interestRate", newInterestRate)
                }
                batch.commit().await()
                ResultState.Success(snapshot.size())
            }
        } catch (e: Exception) {
            ResultState.Error(e)
        }
    }

    companion object {
        private const val COLLECTION = "accounts"
    }
}

