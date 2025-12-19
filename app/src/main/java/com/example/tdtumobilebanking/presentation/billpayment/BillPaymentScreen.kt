package com.example.tdtumobilebanking.presentation.billpayment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdtumobilebanking.domain.model.Bill
import com.example.tdtumobilebanking.domain.model.BillStatus
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillPaymentScreen(
    state: BillPaymentUiState,
    onEvent: (BillPaymentEvent) -> Unit,
    onBack: () -> Unit,
    onPayWithStripe: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val focusManager = LocalFocusManager.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Thanh toán hóa đơn",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = BrandBlue
                    )
                }
            },
            actions = {
                // Seed mock data button (for testing)
                IconButton(onClick = { onEvent(BillPaymentEvent.SeedMockBills) }) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Seed Data",
                        tint = BrandBlue
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Info Card
            AccountInfoCard(
                accountNumber = state.accountNumber,
                balance = state.currentBalance,
                hasMultipleAccounts = state.availableAccounts.size > 1,
                onSelectAccount = { onEvent(BillPaymentEvent.ShowAccountSelector(true)) }
            )

            // Bill Code Input
            BillCodeInputCard(
                billCode = state.billCode,
                isLoading = state.isLookingUp,
                error = state.lookupError,
                onBillCodeChanged = { onEvent(BillPaymentEvent.BillCodeChanged(it)) },
                onLookup = {
                    focusManager.clearFocus()
                    onEvent(BillPaymentEvent.LookupBill)
                }
            )

            // Bill Info Card (shown when bill is found)
            AnimatedVisibility(
                visible = state.bill != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                state.bill?.let { bill ->
                    BillInfoCard(bill = bill, formatter = formatter)
                }
            }

            // Payment Error
            state.paymentError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Error,
                            contentDescription = null,
                            tint = BrandRed
                        )
                        Text(
                            text = error,
                            color = BrandRed,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue to OTP Button
            Button(
                onClick = { onPayWithStripe() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                enabled = state.bill != null && state.currentBalance >= (state.bill?.amount ?: 0.0)
            ) {
                Icon(
                    Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tiếp tục",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Balance warning
            if (state.bill != null && state.currentBalance < state.bill.amount) {
                Text(
                    text = "⚠️ Số dư tài khoản không đủ để thanh toán hóa đơn này",
                    color = BrandRed,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Account Selector Dialog
    if (state.showAccountSelector) {
        AccountSelectorDialog(
            accounts = state.availableAccounts,
            selectedAccountId = state.selectedAccountId,
            onAccountSelected = { onEvent(BillPaymentEvent.SelectAccount(it)) },
            onDismiss = { onEvent(BillPaymentEvent.ShowAccountSelector(false)) }
        )
    }
}

@Composable
private fun AccountInfoCard(
    accountNumber: String,
    balance: Double,
    hasMultipleAccounts: Boolean,
    onSelectAccount: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasMultipleAccounts) { onSelectAccount() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandBlue.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tài khoản thanh toán",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                )
                if (hasMultipleAccounts) {
                    Text(
                        text = "Đổi TK",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BrandBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
            Text(
                text = "STK: $accountNumber",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
            Text(
                text = "${formatter.format(balance.toLong())} VND",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
        }
    }
}

@Composable
private fun BillCodeInputCard(
    billCode: String,
    isLoading: Boolean,
    error: String?,
    onBillCodeChanged: (String) -> Unit,
    onLookup: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tra cứu hóa đơn",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            )

            OutlinedTextField(
                value = billCode,
                onValueChange = onBillCodeChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhập mã hóa đơn (VD: DIEN202512)") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onLookup() }),
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Receipt,
                        contentDescription = null,
                        tint = BrandBlue
                    )
                },
                trailingIcon = {
                    if (billCode.isNotBlank()) {
                        IconButton(onClick = { onBillCodeChanged("") }) {
                            Icon(
                                Icons.Rounded.Clear,
                                contentDescription = "Clear",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                isError = error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandBlue,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                )
            )

            Button(
                onClick = onLookup,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                enabled = billCode.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isLoading) "Đang tra cứu..." else "Kiểm tra",
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Error message
            error?.let {
                Text(
                    text = it,
                    color = BrandRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BillInfoCard(
    bill: Bill,
    formatter: NumberFormat
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = getBillTypeColor(bill.billType).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getBillTypeIcon(bill.billType),
                            contentDescription = null,
                            tint = getBillTypeColor(bill.billType),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = getBillTypeName(bill.billType),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = bill.provider,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (bill.status == BillStatus.UNPAID) 
                        Color(0xFFFF9800).copy(alpha = 0.1f) 
                    else 
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (bill.status == BillStatus.UNPAID) "Chưa thanh toán" else "Đã thanh toán",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (bill.status == BillStatus.UNPAID) 
                                Color(0xFFFF9800) 
                            else 
                                Color(0xFF4CAF50),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.1f))

            // Bill Details
            BillDetailRow(label = "Mã hóa đơn", value = bill.billCode)
            BillDetailRow(label = "Khách hàng", value = bill.customerName)
            BillDetailRow(label = "Mã khách hàng", value = bill.customerCode)
            BillDetailRow(label = "Hạn thanh toán", value = dateFormatter.format(Date(bill.dueDate)))
            BillDetailRow(label = "Nội dung", value = bill.description)

            Divider(color = Color.Gray.copy(alpha = 0.1f))

            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Số tiền thanh toán",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "${formatter.format(bill.amount.toLong())} VND",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                )
            }
        }
    }
}

@Composable
private fun BillDetailRow(label: String, value: String) {
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

private fun getBillTypeIcon(type: String) = when (type) {
    "ELECTRIC" -> Icons.Rounded.ElectricBolt
    "WATER" -> Icons.Rounded.WaterDrop
    "INTERNET" -> Icons.Rounded.Wifi
    else -> Icons.Rounded.Receipt
}

private fun getBillTypeColor(type: String) = when (type) {
    "ELECTRIC" -> Color(0xFFFFB800)
    "WATER" -> Color(0xFF2196F3)
    "INTERNET" -> Color(0xFF9C27B0)
    else -> Color(0xFF607D8B)
}

private fun getBillTypeName(type: String) = when (type) {
    "ELECTRIC" -> "Hóa đơn điện"
    "WATER" -> "Hóa đơn nước"
    "INTERNET" -> "Hóa đơn Internet"
    else -> "Hóa đơn"
}

@Composable
private fun AccountSelectorDialog(
    accounts: List<com.example.tdtumobilebanking.domain.model.Account>,
    selectedAccountId: String,
    onAccountSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))

    AlertDialog(
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
                            containerColor = if (account.accountId == selectedAccountId)
                                BrandBlue.copy(alpha = 0.1f) else Color.White
                        ),
                        border = BorderStroke(
                            width = if (account.accountId == selectedAccountId) 2.dp else 1.dp,
                            color = if (account.accountId == selectedAccountId) BrandBlue else Color.Gray.copy(alpha = 0.3f)
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

