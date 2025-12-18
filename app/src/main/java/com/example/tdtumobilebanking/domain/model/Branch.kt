package com.example.tdtumobilebanking.domain.model

data class Branch(
    val branchId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = ""
)

