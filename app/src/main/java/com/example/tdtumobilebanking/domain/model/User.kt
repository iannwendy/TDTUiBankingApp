package com.example.tdtumobilebanking.domain.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val role: String = "",
    val phoneNumber: String = "",
    val kycStatus: KycStatus = KycStatus.NONE,
    val avatarUrl: String = ""
)

