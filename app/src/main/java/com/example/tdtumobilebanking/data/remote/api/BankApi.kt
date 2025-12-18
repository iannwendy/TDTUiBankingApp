package com.example.tdtumobilebanking.data.remote.api

import com.example.tdtumobilebanking.data.remote.dto.BankListResponse
import retrofit2.http.GET

interface BankApi {
    @GET("v2/banks")
    suspend fun getBanks(): BankListResponse
}

