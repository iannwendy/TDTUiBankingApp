package com.example.tdtumobilebanking.domain.model

data class Bill(
    val billId: String = "",
    val billCode: String = "",
    val billType: String = "", // ELECTRIC, WATER, INTERNET, etc.
    val customerName: String = "",
    val customerCode: String = "",
    val provider: String = "",
    val amount: Double = 0.0,
    val status: BillStatus = BillStatus.UNPAID,
    val dueDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null,
    val description: String = ""
)

enum class BillStatus {
    UNPAID,
    PAID,
    OVERDUE,
    CANCELLED
}

