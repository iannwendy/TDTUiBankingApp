package com.example.tdtumobilebanking.domain.repository

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.Bill

interface BillRepository {
    suspend fun lookupBill(billCode: String): ResultState<Bill>
    suspend fun markBillAsPaid(billId: String): ResultState<Unit>
    suspend fun seedMockBills(): ResultState<Unit>
    suspend fun importBillFromMap(data: Map<String, Any?>): ResultState<Unit>
}

