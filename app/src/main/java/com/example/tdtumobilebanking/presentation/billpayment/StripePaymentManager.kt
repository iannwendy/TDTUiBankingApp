package com.example.tdtumobilebanking.presentation.billpayment

import androidx.activity.ComponentActivity
import android.util.Log
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

/**
 * Quản lý PaymentSheet ở cấp Activity để tránh lỗi LifecycleOwner phải đăng ký
 * trước khi ở trạng thái RESUMED.
 */
object StripePaymentManager {

    private const val TAG = "StripePaymentManager"

    private var paymentSheet: PaymentSheet? = null
    private var resultCallback: ((PaymentSheetResult) -> Unit)? = null

    fun init(activity: ComponentActivity) {
        if (paymentSheet != null) return

        try {
            paymentSheet = PaymentSheet(activity) { result ->
                resultCallback?.invoke(result)
            }
            Log.d(TAG, "PaymentSheet initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize PaymentSheet", e)
        }
    }

    fun present(
        clientSecret: String,
        onResult: (PaymentSheetResult) -> Unit
    ) {
        val sheet = paymentSheet
        if (sheet == null) {
            Log.e(TAG, "PaymentSheet is null. Call init(activity) first.")
            return
        }
        resultCallback = onResult
        try {
            sheet.presentWithPaymentIntent(
                clientSecret,
                PaymentSheet.Configuration(
                    merchantDisplayName = "TDTU Mobile Banking"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error presenting PaymentSheet", e)
            onResult(PaymentSheetResult.Failed(e))
        }
    }
}


