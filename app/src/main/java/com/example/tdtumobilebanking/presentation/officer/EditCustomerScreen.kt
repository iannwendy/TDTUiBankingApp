package com.example.tdtumobilebanking.presentation.officer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed

@Composable
fun EditCustomerScreen(
    state: EditCustomerUiState,
    onEvent: (EditCustomerEvent) -> Unit,
    onBack: () -> Unit,
    customerUid: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandBlue)
            }
            Text(
                text = "Chỉnh sửa thông tin khách hàng",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = { onEvent(EditCustomerEvent.FullNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Họ và tên") },
                    enabled = !state.isLoading
                )

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onEvent(EditCustomerEvent.EmailChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    enabled = !state.isLoading
                )

                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = { onEvent(EditCustomerEvent.PhoneNumberChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Số điện thoại") },
                    enabled = !state.isLoading
                )

                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                state.error?.let {
                    Text(it, color = BrandRed, style = MaterialTheme.typography.bodySmall)
                }

                if (state.success) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = BrandBlue)
                        Text(
                            "Cập nhật thành công!",
                            color = BrandBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(EditCustomerEvent.Save) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            enabled = !state.isLoading
        ) {
            Text("Lưu thay đổi", color = Color.White, fontSize = 16.sp)
        }
    }
}

