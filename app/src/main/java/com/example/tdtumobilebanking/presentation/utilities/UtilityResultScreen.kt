package com.example.tdtumobilebanking.presentation.utilities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed
import java.text.NumberFormat
import java.util.Locale

@Composable
fun UtilityResultScreen(
    state: UtilitiesUiState,
    onNewTransaction: () -> Unit,
    onBackHome: () -> Unit
) {
    val isSuccess = state.success && state.error == null
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val utilityTitle = getUtilityTitle(state.utilityType)
    val amount = state.amount.replace(",", "").toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4EF))
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = null,
                tint = if (isSuccess) Color(0xFF16A34A) else BrandRed,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = if (isSuccess) "$utilityTitle thành công" else "$utilityTitle không thành công",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827)
                ),
                textAlign = TextAlign.Center
            )
            if (!isSuccess && state.error != null) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium.copy(color = BrandRed),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (isSuccess) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Chi tiết giao dịch",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Loại giao dịch:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF6B7280)
                            )
                        )
                        Text(
                            text = utilityTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Số tiền:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF6B7280)
                            )
                        )
                        Text(
                            text = "${formatter.format(amount.toLong())} VND",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                        )
                    }

                    if (state.provider.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Nhà cung cấp:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF6B7280)
                                )
                            )
                            Text(
                                text = state.provider,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF111827)
                                )
                            )
                        }
                    }

                    if (state.customerCode.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Mã khách hàng:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF6B7280)
                                )
                            )
                            Text(
                                text = state.customerCode,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF111827)
                                )
                            )
                        }
                    }

                    if (state.phoneNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Số điện thoại:",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF6B7280)
                                )
                            )
                            Text(
                                text = state.phoneNumber,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF111827)
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tài khoản:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF6B7280)
                            )
                        )
                        Text(
                            text = state.accountNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNewTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text(
                    text = "Thực hiện lại giao dịch khác",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = BrandBlue
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue)
            ) {
                Text(
                    text = "Trở về trang chủ",
                    color = BrandBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun getUtilityTitle(utilityType: String): String {
    return when (utilityType) {
        "ELECTRIC_BILL" -> "Thanh toán hóa đơn điện"
        "WATER_BILL" -> "Thanh toán hóa đơn nước"
        "PHONE_TOPUP" -> "Nạp tiền điện thoại"
        "FLIGHT_TICKET" -> "Mua vé máy bay"
        "MOVIE_TICKET" -> "Mua vé xem phim"
        "HOTEL_BOOKING" -> "Đặt phòng khách sạn"
        "E_COMMERCE" -> "Thanh toán thương mại điện tử"
        else -> "Thanh toán tiện ích"
    }
}

