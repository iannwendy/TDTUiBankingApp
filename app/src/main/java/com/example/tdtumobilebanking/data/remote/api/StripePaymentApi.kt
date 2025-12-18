package com.example.tdtumobilebanking.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST

data class CreatePaymentIntentRequest(
    val amount: Long,
    val currency: String = "vnd",
    val billCode: String
)

data class CreatePaymentIntentResponse(
    val clientSecret: String
)

interface StripePaymentApi {

    @POST("create-payment-intent")
    suspend fun createPaymentIntent(
        @Body request: CreatePaymentIntentRequest
    ): CreatePaymentIntentResponse
}


