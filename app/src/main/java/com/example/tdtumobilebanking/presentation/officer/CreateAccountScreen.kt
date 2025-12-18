package com.example.tdtumobilebanking.presentation.officer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdtumobilebanking.domain.model.AccountType
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    state: CreateAccountUiState,
    onEvent: (CreateAccountEvent) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        onEvent(CreateAccountEvent.LoadCustomers)
    }

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
                text = "Tạo tài khoản mới",
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
                // Customer Selection with Search
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Chọn khách hàng", style = MaterialTheme.typography.bodySmall)
                    
                    ExposedDropdownMenuBox(
                        expanded = state.isCustomerDropdownExpanded,
                        onExpandedChange = { onEvent(CreateAccountEvent.CustomerDropdownExpandedChanged(it)) }
                    ) {
                        OutlinedTextField(
                            value = state.customerSearchQuery,
                            onValueChange = { onEvent(CreateAccountEvent.CustomerSearchQueryChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Tìm kiếm khách hàng") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = { 
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { 
                                        onEvent(CreateAccountEvent.CustomerDropdownExpandedChanged(!state.isCustomerDropdownExpanded))
                                    }
                                )
                            },
                            enabled = !state.isLoading && state.customers.isNotEmpty(),
                            readOnly = false
                        )
                        
                        val filteredCustomers = remember(state.customerSearchQuery, state.customers) {
                            if (state.customerSearchQuery.isBlank()) {
                                state.customers
                            } else {
                                state.customers.filter { customer ->
                                    customer.fullName.contains(state.customerSearchQuery, ignoreCase = true) ||
                                    customer.email.contains(state.customerSearchQuery, ignoreCase = true) ||
                                    customer.phoneNumber.contains(state.customerSearchQuery, ignoreCase = true)
                                }
                            }
                        }
                        
                        ExposedDropdownMenu(
                            expanded = state.isCustomerDropdownExpanded,
                            onDismissRequest = { onEvent(CreateAccountEvent.CustomerDropdownExpandedChanged(false)) }
                        ) {
                            if (filteredCustomers.isEmpty()) {
                                Text(
                                    text = "Không tìm thấy khách hàng",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                filteredCustomers.forEach { customer ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(
                                                    text = customer.fullName.ifBlank { "Khách hàng" },
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${customer.email} - ${customer.phoneNumber}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            onEvent(CreateAccountEvent.CustomerSelected(customer))
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Show selected customer info
                    state.selectedCustomer?.let { customer ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = BrandBlue.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Đã chọn: ${customer.fullName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${customer.email} - ${customer.phoneNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Account ID
                OutlinedTextField(
                    value = state.accountId,
                    onValueChange = { onEvent(CreateAccountEvent.AccountIdChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Số tài khoản") },
                    enabled = !state.isLoading
                )

                // Account Type
                Text("Loại tài khoản", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountType.values().forEach { type ->
                        FilterChip(
                            selected = state.accountType == type,
                            onClick = { onEvent(CreateAccountEvent.AccountTypeChanged(type)) },
                            label = { Text(type.name) }
                        )
                    }
                }

                // Balance
                OutlinedTextField(
                    value = state.balance,
                    onValueChange = { onEvent(CreateAccountEvent.BalanceChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Số dư ban đầu") },
                    enabled = !state.isLoading
                )

                // Currency
                OutlinedTextField(
                    value = state.currency,
                    onValueChange = { onEvent(CreateAccountEvent.CurrencyChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Loại tiền tệ") },
                    enabled = !state.isLoading
                )

                if (state.accountType == AccountType.SAVING) {
                    OutlinedTextField(
                        value = state.interestRate,
                        onValueChange = { onEvent(CreateAccountEvent.InterestRateChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lãi suất (%)") },
                        enabled = !state.isLoading
                    )
                    OutlinedTextField(
                        value = state.termMonth,
                        onValueChange = { onEvent(CreateAccountEvent.TermMonthChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Kỳ hạn (tháng)") },
                        enabled = !state.isLoading
                    )
                }

                if (state.accountType == AccountType.MORTGAGE) {
                    OutlinedTextField(
                        value = state.termMonth,
                        onValueChange = { onEvent(CreateAccountEvent.TermMonthChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Kỳ hạn (tháng)") },
                        enabled = !state.isLoading
                    )
                }

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
                            "Tạo tài khoản thành công!",
                            color = BrandBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onEvent(CreateAccountEvent.CreateAccount) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            enabled = !state.isLoading
        ) {
            Text("Tạo tài khoản", color = Color.White, fontSize = 16.sp)
        }
    }
}

