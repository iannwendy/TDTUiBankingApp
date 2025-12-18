package com.example.tdtumobilebanking.presentation.utilities

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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityDetailScreen(
    utilityType: String,
    state: UtilitiesUiState,
    onEvent: (UtilitiesEvent) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val utilityInfo = getUtilityInfo(utilityType)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = utilityInfo.title,
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (state.availableAccounts.size > 1) {
                            onEvent(UtilitiesEvent.ShowAccountSelector(true))
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
                            text = "Tài khoản thanh toán",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (state.availableAccounts.size > 1) {
                            Text(
                                text = "Chọn tài khoản",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrandBlue,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.clickable {
                                    onEvent(UtilitiesEvent.ShowAccountSelector(true))
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
            if (state.showAccountSelector) {
                AccountSelectorDialog(
                    accounts = state.availableAccounts,
                    selectedAccountId = state.selectedAccountId,
                    onAccountSelected = { accountId ->
                        onEvent(UtilitiesEvent.SelectAccount(accountId))
                    },
                    onDismiss = {
                        onEvent(UtilitiesEvent.ShowAccountSelector(false))
                    }
                )
            }
            
            // Amount Input
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
                        text = "Số tiền",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BrandBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    OutlinedTextField(
                        value = formatAmount(state.amount),
                        onValueChange = {
                            val cleaned = it.replace(",", "").replace(" ", "").replace("VND", "").trim()
                            onEvent(UtilitiesEvent.AmountChanged(cleaned))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                        placeholder = { Text("Nhập số tiền") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        trailingIcon = {
                            Text(
                                text = "VND",
                                modifier = Modifier.padding(end = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrandBlue
                            )
                        }
                    )
                }
            }
            
            // Provider Input (if needed)
            if (utilityInfo.needsProvider) {
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
                            text = utilityInfo.providerLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        OutlinedTextField(
                            value = state.provider,
                            onValueChange = { onEvent(UtilitiesEvent.ProviderChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                            placeholder = { Text(utilityInfo.providerPlaceholder) },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Customer Code Input (if needed)
            if (utilityInfo.needsCustomerCode) {
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
                            text = utilityInfo.customerCodeLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        OutlinedTextField(
                            value = state.customerCode,
                            onValueChange = { onEvent(UtilitiesEvent.CustomerCodeChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                            placeholder = { Text(utilityInfo.customerCodePlaceholder) },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Phone Number Input (if needed)
            if (utilityInfo.needsPhoneNumber) {
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
                            text = "Số điện thoại",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BrandBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        OutlinedTextField(
                            value = state.phoneNumber,
                            onValueChange = { onEvent(UtilitiesEvent.PhoneNumberChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                            placeholder = { Text("Nhập số điện thoại") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Phone
                            )
                        )
                    }
                }
            }
            
            // Description Input
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
                        text = "Ghi chú",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BrandBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { onEvent(UtilitiesEvent.DescriptionChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                        placeholder = { Text("Nhập ghi chú (tùy chọn)") },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submit Button
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                enabled = !state.isLoading && state.amount.isNotBlank() && 
                    state.amount.replace(",", "").toDoubleOrNull()?.let { it > 0 && it <= state.currentBalance } == true
            ) {
                Text(
                    text = "Thanh toán",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
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

private data class UtilityInfo(
    val title: String,
    val needsProvider: Boolean,
    val providerLabel: String,
    val providerPlaceholder: String,
    val needsCustomerCode: Boolean,
    val customerCodeLabel: String,
    val customerCodePlaceholder: String,
    val needsPhoneNumber: Boolean
)

private fun getUtilityInfo(utilityType: String): UtilityInfo {
    return when (utilityType) {
        "ELECTRIC_BILL" -> UtilityInfo(
            title = "Thanh toán hóa đơn điện",
            needsProvider = true,
            providerLabel = "Nhà cung cấp điện",
            providerPlaceholder = "VD: EVN, SPC, ...",
            needsCustomerCode = true,
            customerCodeLabel = "Mã khách hàng",
            customerCodePlaceholder = "Nhập mã khách hàng",
            needsPhoneNumber = false
        )
        "WATER_BILL" -> UtilityInfo(
            title = "Thanh toán hóa đơn nước",
            needsProvider = true,
            providerLabel = "Nhà cung cấp nước",
            providerPlaceholder = "VD: SAWACO, ...",
            needsCustomerCode = true,
            customerCodeLabel = "Mã khách hàng",
            customerCodePlaceholder = "Nhập mã khách hàng",
            needsPhoneNumber = false
        )
        "PHONE_TOPUP" -> UtilityInfo(
            title = "Nạp tiền điện thoại",
            needsProvider = true,
            providerLabel = "Nhà mạng",
            providerPlaceholder = "VD: Viettel, VinaPhone, Mobifone",
            needsCustomerCode = false,
            customerCodeLabel = "",
            customerCodePlaceholder = "",
            needsPhoneNumber = true
        )
        "FLIGHT_TICKET" -> UtilityInfo(
            title = "Mua vé máy bay",
            needsProvider = true,
            providerLabel = "Hãng hàng không",
            providerPlaceholder = "VD: Vietnam Airlines, VietJet Air",
            needsCustomerCode = false,
            customerCodeLabel = "",
            customerCodePlaceholder = "",
            needsPhoneNumber = true
        )
        "MOVIE_TICKET" -> UtilityInfo(
            title = "Mua vé xem phim",
            needsProvider = true,
            providerLabel = "Rạp chiếu phim",
            providerPlaceholder = "VD: CGV, Lotte Cinema",
            needsCustomerCode = false,
            customerCodeLabel = "",
            customerCodePlaceholder = "",
            needsPhoneNumber = true
        )
        "HOTEL_BOOKING" -> UtilityInfo(
            title = "Đặt phòng khách sạn",
            needsProvider = true,
            providerLabel = "Khách sạn",
            providerPlaceholder = "Nhập tên khách sạn",
            needsCustomerCode = false,
            customerCodeLabel = "",
            customerCodePlaceholder = "",
            needsPhoneNumber = true
        )
        "E_COMMERCE" -> UtilityInfo(
            title = "Thanh toán thương mại điện tử",
            needsProvider = true,
            providerLabel = "Sàn thương mại",
            providerPlaceholder = "VD: Shopee, Lazada, Tiki",
            needsCustomerCode = false,
            customerCodeLabel = "",
            customerCodePlaceholder = "",
            needsPhoneNumber = false
        )
        else -> UtilityInfo(
            title = "Thanh toán tiện ích",
            needsProvider = false,
            providerLabel = "",
            providerPlaceholder = "",
            needsCustomerCode = false,
            customerCodeLabel = "",
            customerCodePlaceholder = "",
            needsPhoneNumber = false
        )
    }
}

@Composable
private fun AccountSelectorDialog(
    accounts: List<com.example.tdtumobilebanking.domain.model.Account>,
    selectedAccountId: String,
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

