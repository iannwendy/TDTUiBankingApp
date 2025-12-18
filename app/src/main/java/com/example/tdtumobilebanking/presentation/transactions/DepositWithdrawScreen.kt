package com.example.tdtumobilebanking.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DepositWithdrawScreen(
    state: DepositWithdrawUiState,
    onEvent: (DepositWithdrawEvent) -> Unit,
    onBack: () -> Unit,
    onProceedToOtp: () -> Unit = {}
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val screenTitle = if (state.type == DepositWithdrawType.DEPOSIT) "Nạp tiền" else "Rút tiền"
    
    // Nút "Tiếp tục" luôn luôn được mở (chỉ disable khi đang loading)
    val canContinue = !state.isLoading
    
    // Debug logs for button state
    androidx.compose.runtime.LaunchedEffect(state.isLoading) {
        android.util.Log.d("DepositWithdrawScreen", "Button state: isLoading=${state.isLoading}, canContinue=$canContinue, enabled=${!state.isLoading}")
    }
    
    // Debug logs
    androidx.compose.runtime.LaunchedEffect(state.accountId, state.accountNumber, state.currentBalance, state.availableAccounts.size) {
        android.util.Log.d("DepositWithdrawScreen", "State updated: accountId=${state.accountId}, accountNumber=${state.accountNumber}, balance=${state.currentBalance}, availableAccounts=${state.availableAccounts.size}, showSelector=${state.showAccountSelector}")
        state.availableAccounts.forEachIndexed { index, acc ->
            android.util.Log.d("DepositWithdrawScreen", "  Account[$index]: ${acc.accountId}, balance=${acc.balance}, type=${acc.accountType.name}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandBlue)
            }
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Thông tin tài khoản Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (state.availableAccounts.size >= 2) {
                            onEvent(DepositWithdrawEvent.ShowAccountSelector)
                        }
                    },
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (state.type == DepositWithdrawType.DEPOSIT) "Tài khoản nhận tiền" else "Tài khoản rút tiền",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (state.availableAccounts.size >= 2) {
                            Text(
                                text = "Chọn tài khoản",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrandBlue,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.clickable {
                                    android.util.Log.d("DepositWithdrawScreen", "Click 'Chọn tài khoản', availableAccounts=${state.availableAccounts.size}, current showSelector=${state.showAccountSelector}")
                                    onEvent(DepositWithdrawEvent.ShowAccountSelector)
                                    android.util.Log.d("DepositWithdrawScreen", "After ShowAccountSelector event")
                                }
                            )
                        }
                    }
                    Text(
                        text = "TÀI KHOẢN THANH TOÁN - ${state.accountNumber}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BrandBlue,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "${formatter.format(state.currentBalance.toLong())} VND",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
            }
            
            // Account Selector Dialog
            androidx.compose.runtime.LaunchedEffect(state.showAccountSelector) {
                android.util.Log.d("DepositWithdrawScreen", "showAccountSelector changed: ${state.showAccountSelector}, availableAccounts=${state.availableAccounts.size}")
            }
            if (state.showAccountSelector) {
                android.util.Log.d("DepositWithdrawScreen", "Showing account selector dialog with ${state.availableAccounts.size} accounts")
                AccountSelectorDialog(
                    accounts = state.availableAccounts,
                    currentAccountId = state.accountId,
                    onAccountSelected = { accountId ->
                        android.util.Log.d("DepositWithdrawScreen", "Account selected: $accountId")
                        onEvent(DepositWithdrawEvent.SelectAccount(accountId))
                    },
                    onDismiss = { 
                        android.util.Log.d("DepositWithdrawScreen", "Account selector dialog dismissed")
                        onEvent(DepositWithdrawEvent.HideAccountSelector) 
                    }
                )
            }

            // Số tiền Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = formatAmount(state.amount),
                        onValueChange = {
                            // Remove all formatting characters and spaces
                            val cleaned = it.replace(",", "").replace(" ", "").replace("VND", "").trim()
                            onEvent(DepositWithdrawEvent.AmountChanged(cleaned))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                        placeholder = { Text("Nhập số tiền") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            Text(
                                text = "VND",
                                modifier = Modifier.padding(end = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrandBlue
                            )
                        }
                    )
                    IconButton(onClick = { /* Show info */ }) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = BrandBlue)
                    }
                }
            }

            // Nội dung Section
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
                    Text(
                        text = "Nội dung",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BrandBlue
                        )
                    )
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { onEvent(DepositWithdrawEvent.DescriptionChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (state.description.isNotBlank()) {
                                IconButton(onClick = { onEvent(DepositWithdrawEvent.DescriptionChanged("")) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    onClick = { onProceedToOtp() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    enabled = !state.isLoading
                ) {
                    Text("Tiếp tục", color = Color.White)
                }
            }

            // Error message
            state.error?.let {
                Text(
                    text = it,
                    color = BrandRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatAmount(amount: String): String {
    if (amount.isBlank()) return ""
    val cleaned = amount.replace(",", "").replace(" ", "")
    val number = cleaned.toLongOrNull() ?: return amount
    val formatter = java.text.DecimalFormat("#,###")
    return formatter.format(number)
}

@Composable
private fun AccountSelectorDialog(
    accounts: List<com.example.tdtumobilebanking.domain.model.Account>,
    currentAccountId: String,
    onAccountSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Chọn tài khoản",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                accounts.forEach { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccountSelected(account.accountId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (account.accountId == currentAccountId) 
                                BrandBlue.copy(alpha = 0.1f) else Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (account.accountId == currentAccountId) 2.dp else 1.dp,
                            color = if (account.accountId == currentAccountId) BrandBlue else Color.Gray.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "TÀI KHOẢN THANH TOÁN - ${account.accountId}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = BrandBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "${formatter.format(account.balance.toLong())} VND",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Đóng")
            }
        }
    )
}

