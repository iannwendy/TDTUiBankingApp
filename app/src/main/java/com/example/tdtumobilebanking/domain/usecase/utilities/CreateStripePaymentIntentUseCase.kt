package com.example.tdtumobilebanking.domain.usecase.utilities

import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.data.remote.api.CreatePaymentIntentRequest
import com.example.tdtumobilebanking.data.remote.api.StripePaymentApi
import javax.inject.Inject

class CreateStripePaymentIntentUseCase @Inject constructor(
    private val stripePaymentApi: StripePaymentApi
) {

    suspend operator fun invoke(
        amount: Double,
        billCode: String? = null,
        description: String? = null
    ): ResultState<String> {
        return try {
            // Stripe yêu cầu amount theo đơn vị nhỏ nhất (VD: VND -> số nguyên)
            val request = CreatePaymentIntentRequest(
                amount = amount.toLong(),
                currency = "vnd",
                billCode = billCode,
                description = description
            )
            val response = stripePaymentApi.createPaymentIntent(request)
            ResultState.Success(response.clientSecret)
        } catch (e: Exception) {
            ResultState.Error(e)
        }
    }
}


