package com.example.tdtumobilebanking.presentation.billpayment

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillPaymentResultScreen(
    state: BillPaymentUiState,
    onNewPayment: () -> Unit,
    onBackHome: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Animation for success icon
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50).copy(alpha = 0.1f),
                        Color.White
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Success Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF4CAF50)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Thanh toán thành công!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hóa đơn của bạn đã được thanh toán",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Transaction Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bill info
                state.bill?.let { bill ->
                    DetailRow(label = "Mã hóa đơn", value = bill.billCode)
                    DetailRow(label = "Loại hóa đơn", value = getBillTypeName(bill.billType))
                    DetailRow(label = "Nhà cung cấp", value = bill.provider)
                    DetailRow(label = "Khách hàng", value = bill.customerName)

                    Divider(color = Color.Gray.copy(alpha = 0.1f))

                    // Amount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Số tiền",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "${formatter.format(bill.amount.toLong())} VND",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                        )
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.1f))

                // Transaction ID and time
                state.transactionId?.let { txnId ->
                    DetailRow(label = "Mã giao dịch", value = txnId)
                }
                DetailRow(
                    label = "Thời gian",
                    value = dateFormatter.format(Date())
                )
                DetailRow(
                    label = "Từ tài khoản",
                    value = state.accountNumber
                )
                DetailRow(
                    label = "Số dư còn lại",
                    value = "${formatter.format(state.currentBalance.toLong())} VND"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNewPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Icon(
                    Icons.Rounded.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Thanh toán hóa đơn khác",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue)
            ) {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Về trang chủ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

private fun getBillTypeName(type: String) = when (type) {
    "ELECTRIC" -> "Hóa đơn điện"
    "WATER" -> "Hóa đơn nước"
    "INTERNET" -> "Hóa đơn Internet"
    else -> "Hóa đơn"
}

