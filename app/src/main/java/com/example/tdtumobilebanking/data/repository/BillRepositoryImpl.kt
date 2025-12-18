package com.example.tdtumobilebanking.data.repository

import android.util.Log
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.data.remote.dto.BillDto
import com.example.tdtumobilebanking.domain.model.Bill
import com.example.tdtumobilebanking.domain.model.BillStatus
import com.example.tdtumobilebanking.domain.repository.BillRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class BillRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BillRepository {

    companion object {
        private const val TAG = "BillRepo"
        private const val COLLECTION = "bills"
    }

    override suspend fun lookupBill(billCode: String): ResultState<Bill> {
        return try {
            Log.d(TAG, "Looking up bill with code: $billCode")
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("billCode", billCode)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) {
                Log.d(TAG, "Bill not found: $billCode")
                ResultState.Error(Exception("Mã hóa đơn không tồn tại"))
            } else {
                val dto = snapshot.documents.first().toObject(BillDto::class.java)
                if (dto != null) {
                    val bill = dto.toDomain()
                    Log.d(TAG, "Bill found: $bill")
                    ResultState.Success(bill)
                } else {
                    ResultState.Error(Exception("Không thể đọc thông tin hóa đơn"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error looking up bill", e)
            ResultState.Error(e)
        }
    }

    override suspend fun markBillAsPaid(billId: String): ResultState<Unit> {
        return try {
            Log.d(TAG, "Marking bill as paid: $billId")
            firestore.collection(COLLECTION)
                .document(billId)
                .update(
                    mapOf(
                        "status" to "PAID",
                        "paidAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Log.d(TAG, "Bill marked as paid successfully")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking bill as paid", e)
            ResultState.Error(e)
        }
    }

    override suspend fun seedMockBills(): ResultState<Unit> {
        return try {
            Log.d(TAG, "Seeding mock bills...")
            val mockBills = listOf(
                BillDto(
                    billId = UUID.randomUUID().toString(),
                    billCode = "DIEN202512",
                    billType = "ELECTRIC",
                    customerName = "Nguyễn Văn A",
                    customerCode = "EVN001234",
                    provider = "EVN TPHCM",
                    amount = 500000.0,
                    status = "UNPAID",
                    dueDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000,
                    description = "Hóa đơn tiền điện tháng 12/2025"
                ),
                BillDto(
                    billId = UUID.randomUUID().toString(),
                    billCode = "NUOC202512",
                    billType = "WATER",
                    customerName = "Nguyễn Văn A",
                    customerCode = "SAWACO5678",
                    provider = "SAWACO",
                    amount = 150000.0,
                    status = "UNPAID",
                    dueDate = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000,
                    description = "Hóa đơn tiền nước tháng 12/2025"
                ),
                BillDto(
                    billId = UUID.randomUUID().toString(),
                    billCode = "NET202512",
                    billType = "INTERNET",
                    customerName = "Nguyễn Văn A",
                    customerCode = "FPT999888",
                    provider = "FPT Telecom",
                    amount = 250000.0,
                    status = "UNPAID",
                    dueDate = System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000,
                    description = "Hóa đơn Internet tháng 12/2025"
                ),
                BillDto(
                    billId = UUID.randomUUID().toString(),
                    billCode = "DIEN202511",
                    billType = "ELECTRIC",
                    customerName = "Nguyễn Văn A",
                    customerCode = "EVN001234",
                    provider = "EVN TPHCM",
                    amount = 480000.0,
                    status = "PAID",
                    dueDate = System.currentTimeMillis() - 20 * 24 * 60 * 60 * 1000,
                    paidAt = System.currentTimeMillis() - 25 * 24 * 60 * 60 * 1000,
                    description = "Hóa đơn tiền điện tháng 11/2025"
                )
            )

            for (bill in mockBills) {
                firestore.collection(COLLECTION)
                    .document(bill.billId)
                    .set(bill)
                    .await()
            }
            
            Log.d(TAG, "Mock bills seeded successfully: ${mockBills.size} bills")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding mock bills", e)
            ResultState.Error(e)
        }
    }

    override suspend fun importBillFromMap(data: Map<String, Any?>): ResultState<Unit> {
        return try {
            val billId = (data["billId"] as? String)?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
            val dto = BillDto(
                billId = billId,
                billCode = data["billCode"] as? String ?: "",
                billType = data["billType"] as? String ?: "",
                customerName = data["customerName"] as? String ?: "",
                customerCode = data["customerCode"] as? String ?: "",
                provider = data["provider"] as? String ?: "",
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                status = data["status"] as? String ?: "UNPAID",
                dueDate = (data["dueDate"] as? Number)?.toLong() ?: 0L,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                paidAt = (data["paidAt"] as? Number)?.toLong(),
                description = data["description"] as? String ?: ""
            )

            firestore.collection(COLLECTION)
                .document(billId)
                .set(dto)
                .await()

            ResultState.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing bill from map", e)
            ResultState.Error(e)
        }
    }

    private fun BillDto.toDomain(): Bill {
        return Bill(
            billId = billId,
            billCode = billCode,
            billType = billType,
            customerName = customerName,
            customerCode = customerCode,
            provider = provider,
            amount = amount,
            status = try { BillStatus.valueOf(status) } catch (e: Exception) { BillStatus.UNPAID },
            dueDate = dueDate,
            createdAt = createdAt,
            paidAt = paidAt,
            description = description
        )
    }
}

