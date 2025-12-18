package com.example.tdtumobilebanking.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed

@Composable
fun OtpScreen(
    state: TransferUiState,
    onEvent: (TransferEvent) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onAutoFillOtp: () -> Unit = {}
) {
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
                text = "Xác thực OTP",
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
                
                // OTP Display Section - Always show, even if OTP is null
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
                    onValueChange = { onEvent(TransferEvent.OtpChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    placeholder = { Text("Nhập mã OTP") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !state.otpExpired
                )
                
                // Transaction Summary
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
                            text = "Thông tin giao dịch",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                        )
                        
                        // Số tài khoản - Ngân hàng
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "Số tài khoản:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(0.4f)
                            )
                            Column(
                                modifier = Modifier.weight(0.6f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = state.receiverAccountId.ifBlank { "Chưa xác định" },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                                if (state.selectedBankName.isNotBlank() || state.selectedBank.isNotBlank()) {
                                    Text(
                                        text = state.selectedBankName.ifBlank { state.selectedBank },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.Gray
                                        ),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )
                                }
                            }
                        }
                        
                        // Người nhận
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "Người nhận:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(0.4f)
                            )
                            Text(
                                text = (state.receiverFullName.ifBlank { state.receiverName })
                                    .takeIf { it.isNotBlank() && !it.startsWith("Tài khoản") }
                                    ?: "Chưa xác định",
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
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                state.amount.replace(",", "").toDoubleOrNull()?.let { amount ->
                                    "${java.text.DecimalFormat("#,###").format(amount.toLong())} VND"
                                } ?: "0 VND",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandRed
                                )
                            )
                        }
                        
                        // Nội dung giao dịch
                        if (state.description.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    "Nội dung:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    text = state.description,
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
                
                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                
                // Error message display
                state.error?.let { errorMessage ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandRed.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandRed)
                    ) {
                        Text(
                            text = errorMessage,
                            color = BrandRed,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                if (state.success) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue)
                    ) {
                        Text(
                            text = "Giao dịch thành công!",
                            color = BrandBlue,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = BrandBlue
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
            ) {
                Text("Quay lại", color = BrandBlue)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Xác nhận", color = Color.White)
            }
        }
    }
}

