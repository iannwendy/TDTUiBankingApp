package com.example.tdtumobilebanking.presentation.billpayment

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.example.tdtumobilebanking.presentation.billpayment.StripePaymentManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillPaymentOtpScreen(
    state: BillPaymentUiState,
    onEvent: (BillPaymentEvent) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onAutoFillOtp: () -> Unit = {}
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    // Khi đã có clientSecret từ backend -> hiển thị PaymentSheet
    LaunchedEffect(state.paymentClientSecret) {
        val clientSecret = state.paymentClientSecret
        if (clientSecret != null && !state.paymentSuccess && activity != null) {
            try {
                StripePaymentManager.present(clientSecret) { result ->
                    when (result) {
                        is PaymentSheetResult.Completed ->
                            onEvent(BillPaymentEvent.PaymentCompleted)
                        is PaymentSheetResult.Failed ->
                            onEvent(
                                BillPaymentEvent.PaymentFailed(
                                    result.error.localizedMessage ?: "Thanh toán thất bại"
                                )
                            )
                        PaymentSheetResult.Canceled ->
                            onEvent(BillPaymentEvent.PaymentFailed("Đã hủy thanh toán"))
                    }
                }
            } catch (e: Exception) {
                onEvent(
                    BillPaymentEvent.PaymentFailed(
                        "Không thể mở màn hình thanh toán Stripe: ${e.message}"
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandBlue)
            }
            Text(
                text = "Xác thực OTP - Thanh toán hóa đơn",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OTP Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 8.dp),
                    tint = BrandBlue
                )

                Text(
                    text = "Nhập mã OTP",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                // OTP Display Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(2.dp, BrandBlue)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Mã OTP của bạn",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                        if (state.generatedOtp != null) {
                            Text(
                                text = state.generatedOtp,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = BrandRed,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 4.sp
                                )
                            )
                        } else {
                            Text(
                                text = "Đang tạo mã...",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        // Countdown timer
                        if (state.otpCountdown > 0) {
                            Text(
                                text = "Còn lại: ${state.otpCountdown}s",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (state.otpCountdown <= 5) BrandRed else BrandBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Auto-fill OTP button
                Button(
                    onClick = onAutoFillOtp,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    enabled = !state.otpExpired && state.otpCountdown > 0 && state.generatedOtp != null
                ) {
                    Text("Nhập OTP", color = Color.White, fontSize = 16.sp)
                }

                OutlinedTextField(
                    value = state.enteredOtp,
                    onValueChange = { onEvent(BillPaymentEvent.OtpChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("Nhập mã OTP") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !state.otpExpired
                )

                // Bill Info Summary
                state.bill?.let { bill ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Thông tin hóa đơn",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue
                                )
                            )

                            // Mã hóa đơn
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Mã hóa đơn:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    text = bill.billCode,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.weight(0.6f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }

                            // Nhà cung cấp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Nhà cung cấp:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    text = bill.provider,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.weight(0.6f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }

                            // Số tiền
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Số tiền:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    text = "${formatter.format(bill.amount.toLong())} VND",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BrandRed
                                    ),
                                    modifier = Modifier.weight(0.6f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }

                            // Tài khoản
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Tài khoản:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    text = state.accountNumber,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.weight(0.6f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment error
                if (state.paymentError != null) {
                    Text(
                        text = state.paymentError,
                        color = BrandRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Error message
                state.lookupError?.let {
                    Text(
                        text = it,
                        color = BrandRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Confirm button
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    enabled = !state.otpExpired && state.enteredOtp.isNotBlank() && !state.isProcessingPayment
                ) {
                    Text(
                        text = when {
                            state.isProcessingPayment -> "Đang xử lý thanh toán..."
                            else -> "Xác nhận"
                        },
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

