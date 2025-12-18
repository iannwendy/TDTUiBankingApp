package com.example.tdtumobilebanking.data.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.data.mapper.toDomain
import com.example.tdtumobilebanking.data.mapper.toDto
import com.example.tdtumobilebanking.data.remote.dto.AccountDto
import com.example.tdtumobilebanking.data.remote.dto.TransactionDto
import com.example.tdtumobilebanking.domain.model.Transaction
import com.example.tdtumobilebanking.domain.model.TransactionStatus
import com.example.tdtumobilebanking.domain.model.TransactionType
import com.example.tdtumobilebanking.domain.repository.TransactionRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException
import java.util.UUID
import javax.inject.Inject
import android.util.Log

class TransactionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TransactionRepository {

    override fun getTransactionsForAccount(accountId: String): Flow<ResultState<List<Transaction>>> = flow {
        emit(ResultState.Loading)
        try {
            Log.d("TxnRepo", "getTransactionsForAccount accountId=$accountId")
            val snapshot = firestore.collection(COLLECTION)
                .whereArrayContains("participants", accountId)
                .get()
                .await()
            val transactions = snapshot.documents
                .mapNotNull { it.toObject(TransactionDto::class.java)?.toDomain() }
                .sortedByDescending { it.timestamp }
            Log.d("TxnRepo", "transactions fetched count=${transactions.size}")
            emit(ResultState.Success(transactions))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("TxnRepo", "getTransactionsForAccount error: ${e.message}", e)
            emit(ResultState.Error(e))
        }
    }

    override suspend fun transferInternal(
        senderAccountId: String,
        receiverAccountId: String,
        amount: Double,
        description: String
    ): ResultState<Unit> = try {
        firestore.runTransaction { transaction ->
            val senderRef = firestore.collection(ACCOUNT_COLLECTION).document(senderAccountId)
            val receiverRef = firestore.collection(ACCOUNT_COLLECTION).document(receiverAccountId)

            val senderSnap = transaction.get(senderRef)
            val receiverSnap = transaction.get(receiverRef)

            val sender = senderSnap.toObject(AccountDto::class.java)
                ?: throw IllegalStateException("Sender not found")
            val receiver = receiverSnap.toObject(AccountDto::class.java)
                ?: throw IllegalStateException("Receiver not found")

            val senderBalance = sender.balance ?: 0.0
            if (senderBalance < amount) throw IllegalStateException("Insufficient balance")

            transaction.update(senderRef, "balance", senderBalance - amount)
            transaction.update(receiverRef, "balance", (receiver.balance ?: 0.0) + amount)

            val newTransactionId = UUID.randomUUID().toString()
            val txRef = firestore.collection(COLLECTION).document(newTransactionId)
            transaction.set(
                txRef,
                TransactionDto(
                    transactionId = newTransactionId,
                    senderAccountId = senderAccountId,
                    receiverAccountId = receiverAccountId,
                    amount = amount,
                    type = TransactionType.TRANSFER_INTERNAL.name,
                    status = TransactionStatus.SUCCESS.name,
                    timestamp = System.currentTimeMillis(),
                    description = description
                ).copy().toMapWithParticipants()
            )
        }.await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(e)
    }

    private fun TransactionDto.toMapWithParticipants(): Map<String, Any?> = mapOf(
        "transactionId" to transactionId,
        "senderAccountId" to senderAccountId,
        "receiverAccountId" to receiverAccountId,
        "amount" to amount,
        "type" to type,
        "status" to status,
        "timestamp" to timestamp,
        "description" to description,
        "participants" to listOfNotNull(senderAccountId, receiverAccountId),
        "serverTimestamp" to FieldValue.serverTimestamp()
    )

    companion object {
        private const val COLLECTION = "transactions"
        private const val ACCOUNT_COLLECTION = "accounts"
    }

    override suspend fun createDepositWithdrawTransaction(
        accountId: String,
        amount: Double,
        description: String,
        type: String
    ): ResultState<Unit> = try {
        val newTransactionId = UUID.randomUUID().toString()
        val txRef = firestore.collection(COLLECTION).document(newTransactionId)
        
        val senderAccountId = if (type == "DEPOSIT") "SYSTEM" else accountId
        val receiverAccountId = if (type == "DEPOSIT") accountId else "SYSTEM"
        
        txRef.set(
            TransactionDto(
                transactionId = newTransactionId,
                senderAccountId = senderAccountId,
                receiverAccountId = receiverAccountId,
                amount = amount,
                type = if (type == "DEPOSIT") TransactionType.DEPOSIT.name else TransactionType.WITHDRAWAL.name,
                status = TransactionStatus.SUCCESS.name,
                timestamp = System.currentTimeMillis(),
                description = description
            ).copy().toMapWithParticipants()
        ).await()
        
        ResultState.Success(Unit)
    } catch (e: Exception) {
        Log.e("TxnRepo", "createDepositWithdrawTransaction error: ${e.message}", e)
        ResultState.Error(e)
    }

    override suspend fun createUtilityTransaction(
        accountId: String,
        utilityType: String,
        amount: Double,
        provider: String,
        customerCode: String,
        phoneNumber: String,
        description: String
    ): ResultState<Unit> = try {
        val newTransactionId = UUID.randomUUID().toString()
        val txRef = firestore.collection(COLLECTION).document(newTransactionId)
        
        // Build description with utility details
        val fullDescription = buildString {
            append(description)
            if (provider.isNotBlank()) {
                append(" - Nhà cung cấp: $provider")
            }
            if (customerCode.isNotBlank()) {
                append(" - Mã khách hàng: $customerCode")
            }
            if (phoneNumber.isNotBlank()) {
                append(" - Số điện thoại: $phoneNumber")
            }
        }
        
        txRef.set(
            TransactionDto(
                transactionId = newTransactionId,
                senderAccountId = accountId,
                receiverAccountId = "UTILITY_PROVIDER",
                amount = amount,
                type = TransactionType.BILL_PAYMENT.name,
                status = TransactionStatus.SUCCESS.name,
                timestamp = System.currentTimeMillis(),
                description = fullDescription
            ).copy().toMapWithParticipants()
        ).await()
        
        ResultState.Success(Unit)
    } catch (e: Exception) {
        Log.e("TxnRepo", "createUtilityTransaction error: ${e.message}", e)
        ResultState.Error(e)
    }
}

