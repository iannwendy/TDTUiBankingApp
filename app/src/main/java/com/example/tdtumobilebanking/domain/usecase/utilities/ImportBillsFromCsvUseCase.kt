package com.example.tdtumobilebanking.domain.usecase.utilities

import android.content.Context
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.repository.BillRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import javax.inject.Inject

class ImportBillsFromCsvUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billRepository: BillRepository
) {

    suspend operator fun invoke(assetFileName: String = "sample_bills.csv"): ResultState<Unit> {
        return try {
            val inputStream = context.assets.open(assetFileName)
            val reader = BufferedReader(inputStream.reader())

            // Bỏ qua dòng header
            reader.readLine()

            var line: String? = reader.readLine()
            var count = 0

            while (line != null) {
                // CSV đơn giản, không có dấu phẩy bên trong field
                val parts = line.split(",")
                if (parts.size >= 12) {
                    val map = mutableMapOf<String, Any?>()
                    map["billId"] = parts[0]
                    map["billCode"] = parts[1]
                    map["billType"] = parts[2]
                    map["customerName"] = parts[3]
                    map["customerCode"] = parts[4]
                    map["provider"] = parts[5]
                    map["amount"] = parts[6].toDoubleOrNull() ?: 0.0
                    map["status"] = parts[7]
                    map["dueDate"] = parts[8].toLongOrNull() ?: 0L
                    map["createdAt"] = parts[9].toLongOrNull() ?: 0L
                    map["paidAt"] = parts[10].takeIf { it.isNotBlank() }?.toLongOrNull()
                    map["description"] = parts[11]

                    billRepository.importBillFromMap(map)
                    count++
                }
                line = reader.readLine()
            }

            reader.close()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e)
        }
    }
}


