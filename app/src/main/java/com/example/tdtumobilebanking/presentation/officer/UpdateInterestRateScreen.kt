package com.example.tdtumobilebanking.presentation.officer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tdtumobilebanking.domain.model.AccountType
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateInterestRateScreen(
    state: UpdateInterestRateUiState,
    onEvent: (UpdateInterestRateEvent) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(state.success) {
        if (state.success) {
            onEvent(UpdateInterestRateEvent.ResetSuccess)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sửa lãi suất",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = BrandBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Cập nhật lãi suất theo chính sách ngân hàng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    // Account Type Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Loại tài khoản",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AccountType.values().forEach { type ->
                                FilterChip(
                                    selected = state.accountType == type,
                                    onClick = { onEvent(UpdateInterestRateEvent.AccountTypeChanged(type)) },
                                    label = {
                                        Text(
                                            text = when (type) {
                                                AccountType.CHECKING -> "Thanh toán"
                                                AccountType.SAVING -> "Tiết kiệm"
                                                AccountType.MORTGAGE -> "Vay thế chấp"
                                            },
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Interest Rate Input
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Lãi suất mới (%)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = state.interestRate,
                            onValueChange = { onEvent(UpdateInterestRateEvent.InterestRateChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nhập lãi suất (0-100%)") },
                            placeholder = { Text("Ví dụ: 5.5") },
                            singleLine = true,
                            enabled = !state.isLoading,
                            suffix = { Text("%", color = Color(0xFF7C8493)) },
                            isError = state.error != null
                        )
                        if (state.error != null) {
                            Text(
                                text = state.error,
                                color = BrandRed,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }

                    // Info Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = BrandBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Lưu ý",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue
                                )
                            }
                            Text(
                                text = "Lãi suất mới sẽ được áp dụng cho tất cả tài khoản ${when (state.accountType) {
                                    AccountType.CHECKING -> "thanh toán"
                                    AccountType.SAVING -> "tiết kiệm"
                                    AccountType.MORTGAGE -> "vay thế chấp"
                                }} trong hệ thống.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    // Update Button
                    Button(
                        onClick = { onEvent(UpdateInterestRateEvent.UpdateInterestRate) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading && state.interestRate.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = if (state.isLoading) "Đang cập nhật..." else "Cập nhật lãi suất",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Success Message
            if (state.success && state.updatedAccountsCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F6EE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF2BAE66),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Cập nhật thành công!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2BAE66)
                            )
                        }
                        Text(
                            text = "Đã cập nhật lãi suất cho ${state.updatedAccountsCount} tài khoản ${when (state.accountType) {
                                AccountType.CHECKING -> "thanh toán"
                                AccountType.SAVING -> "tiết kiệm"
                                AccountType.MORTGAGE -> "vay thế chấp"
                            }}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }
    }
}

