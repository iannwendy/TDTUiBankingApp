package com.example.tdtumobilebanking.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed
import java.text.NumberFormat
import java.util.Locale
import java.text.DecimalFormat

@Composable
fun TransferScreen(
    state: TransferUiState,
    onEvent: (TransferEvent) -> Unit,
    onBack: () -> Unit,
    onProceedToOtp: () -> Unit = {}
) {
    val filteredBanks = state.banks.filter {
        val query = state.bankSearch.lowercase()
        query.isBlank() || it.name.lowercase().contains(query) || 
        it.code.lowercase().contains(query) || 
        it.shortName.lowercase().contains(query)
    }
    
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                text = "Chuyển tiền tới số tài khoản",
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
            // Nguồn chuyển tiền Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (state.availableAccounts.isNotEmpty()) {
                            onEvent(TransferEvent.ShowAccountSelector)
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
                        text = "Nguồn chuyển tiền",
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
                                modifier = Modifier.clickable { onEvent(TransferEvent.ShowAccountSelector) }
                            )
                        } else if (state.availableAccounts.isEmpty()) {
                            Text(
                                text = "Đang tải...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                    Text(
                        text = "TÀI KHOẢN THANH TOÁN - ${state.senderAccountNumber.ifBlank { state.senderAccountId }}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BrandBlue,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "${formatter.format(state.senderBalance.toLong())} VND",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
            }
            
            // Account Selector Dialog
            androidx.compose.runtime.LaunchedEffect(state.showAccountSelector, state.availableAccounts.size) {
                android.util.Log.d("TransferScreen", "showAccountSelector=${state.showAccountSelector}, availableAccounts=${state.availableAccounts.size}, senderAccountId=${state.senderAccountId}")
            }
            if (state.showAccountSelector) {
                android.util.Log.d("TransferScreen", "Showing TransferAccountSelectorDialog with ${state.availableAccounts.size} accounts")
                TransferAccountSelectorDialog(
                    accounts = state.availableAccounts,
                    currentAccountId = state.senderAccountId,
                    onAccountSelected = { accountId: String ->
                        android.util.Log.d("TransferScreen", "Account selected in dialog: $accountId")
                        onEvent(TransferEvent.SelectAccount(accountId))
                    },
                    onDismiss = { 
                        android.util.Log.d("TransferScreen", "Dialog dismissed")
                        onEvent(TransferEvent.HideAccountSelector) 
                    }
                )
            }
            
            // Chuyển đến Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Chuyển đến",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    // Ngân hàng
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Ngân hàng",
                            style = MaterialTheme.typography.bodySmall
                        )
                        BankSelector(
                            search = state.bankSearch,
                            expanded = state.bankDropdownExpanded,
                            onSearchChange = { onEvent(TransferEvent.BankSearchChanged(it)) },
                            onToggle = { onEvent(TransferEvent.ToggleBankDropdown) },
                            onSelect = { onEvent(TransferEvent.BankSelected(it)) },
                            banks = filteredBanks,
                            selectedBank = state.selectedBankName.ifBlank { state.selectedBank }
                        )
                    }
                    
                    // Số tài khoản
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Số tài khoản",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = state.receiverAccountId,
                            onValueChange = { onEvent(TransferEvent.ReceiverChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            trailingIcon = {
                                Row {
                                    if (state.receiverAccountId.isNotBlank()) {
                                        IconButton(onClick = { onEvent(TransferEvent.ReceiverChanged("")) }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                    IconButton(onClick = { /* Scan QR or select contact */ }) {
                                        Icon(Icons.Default.Person, contentDescription = "Select contact")
                                    }
                                }
                            },
                        )
                        // Kiểm tra tài khoản button
                        Button(
                            onClick = { onEvent(TransferEvent.LookupReceiver) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Text("Kiểm tra tài khoản")
                        }
                        // Hiển thị tên người nhận
                        if (state.receiverFullName.isNotBlank()) {
                            Text(
                                text = state.receiverFullName.uppercase(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
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
                            val cleaned = it.replace(",", "").replace(" VND", "").trim()
                            onEvent(TransferEvent.AmountChanged(cleaned))
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
            
            // Nội dung chuyển tiền Section
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
                        text = "Nội dung chuyển tiền",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BrandBlue
                        )
                    )
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { onEvent(TransferEvent.DescriptionChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (state.description.isNotBlank()) {
                                IconButton(onClick = { onEvent(TransferEvent.DescriptionChanged("")) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            val canContinue = state.isReceiverValid && state.error == null
            
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
                    enabled = canContinue && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        disabledContainerColor = BrandBlue.copy(alpha = 0.4f)
                    )
                ) {
                    Text("Tiếp tục", color = Color.White)
                }
            }
            
            // Error message
            state.error?.let {
                Text(
                    text = "Error: $it",
                    color = BrandRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BankSelector(
    search: String,
    expanded: Boolean,
    onSearchChange: (String) -> Unit,
    onToggle: () -> Unit,
    onSelect: (String) -> Unit,
    banks: List<com.example.tdtumobilebanking.domain.model.BankInfo>,
    selectedBank: String
) {
    val filteredBanks = banks.filter {
        val query = search.lowercase().trim()
        if (query.isBlank()) return@filter true
        
        // Check if query matches the full format "shortName - name"
        // Example: "mb - ngân hàng quân đội" should match bank with shortName="MB" and name contains "Ngân hàng Quân đội"
        val parts = query.split(" - ")
        val matchesFullFormat = if (parts.size == 2) {
            val shortNamePart = parts[0].trim()
            val namePart = parts[1].trim()
            // Both shortName and name should match
            (it.shortName.lowercase() == shortNamePart || it.shortName.lowercase().contains(shortNamePart)) &&
            (it.name.lowercase().contains(namePart) || namePart.contains(it.name.lowercase()))
        } else {
            false
        }
        
        // Standard search in name, code, shortName (for partial matches)
        val matchesStandard = it.name.lowercase().contains(query) || 
            it.code.lowercase().contains(query) || 
            it.shortName.lowercase().contains(query)
        
        // If query contains " - ", only use full format matching
        // Otherwise, use standard search
        if (query.contains(" - ")) {
            matchesFullFormat
        } else {
            matchesStandard
        }
    }.take(50) // Limit to 50 for performance
    
    Column {
        // Search field - auto-expand when typing
        OutlinedTextField(
            value = search,
            onValueChange = { 
                onSearchChange(it)
                // Auto-expand dropdown when user types (only if not already expanded)
                if (!expanded && it.isNotBlank()) {
                    onToggle()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text("Tìm kiếm ngân hàng...") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { onToggle() }) {
                    Icon(
                        Icons.Default.ArrowDropDown, 
                        contentDescription = "Select bank"
                    )
                }
            }
        )
        
        // Dropdown menu - only show when expanded (user is actively searching/selecting)
        // ViewModel will set expanded = false after selection, so dropdown will close automatically
        if (expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { 
                    // Close dropdown when dismissed (clicking outside or pressing back)
                    onToggle()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                val displayBanks = if (search.isBlank()) {
                    filteredBanks.take(5)
                } else {
                    filteredBanks
                }
                
                if (displayBanks.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Không tìm thấy ngân hàng", color = Color.Gray) },
                        onClick = { }
                    )
                } else {
                    displayBanks.forEach { bank ->
                        DropdownMenuItem(
                            text = { 
                                Text("${bank.shortName} - ${bank.name}")
                            },
                            onClick = { 
                                onSelect(bank.shortName)
                                // ViewModel will handle filling the search field and closing dropdown
                            }
                        )
                    }
                }
                
                if (filteredBanks.size > 5 && search.isBlank()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "... và ${filteredBanks.size - 5} ngân hàng khác (gõ để tìm)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        },
                        onClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferAccountSelectorDialog(
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

@Composable
private fun formatAmount(amount: String): String {
    if (amount.isBlank()) return ""
    val cleaned = amount.replace(",", "").replace(" ", "")
    val number = cleaned.toLongOrNull() ?: return amount
    val formatter = DecimalFormat("#,###")
    return formatter.format(number)
}
