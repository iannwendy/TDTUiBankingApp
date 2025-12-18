package com.example.tdtumobilebanking.data.remote.dto

data class BillDto(
    val billId: String = "",
    val billCode: String = "",
    val billType: String = "",
    val customerName: String = "",
    val customerCode: String = "",
    val provider: String = "",
    val amount: Double = 0.0,
    val status: String = "UNPAID",
    val dueDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null,
    val description: String = ""
)

