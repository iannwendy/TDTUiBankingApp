package com.example.tdtumobilebanking.presentation.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed

private val OfficerScreenBg = Color(0xFFF8F9FA)
private val OfficerTextPrimary = Color(0xFF0F172A)
private val OfficerTextSecondary = Color(0xFF7C8493)

private data class OfficerQuickAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color,
    val container: Color,
    val onClick: () -> Unit
)

@Composable
fun OfficerDashboardScreen(
    state: DashboardUiState,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onCreateAccountClick: () -> Unit = {},
    onEditCustomerClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onUpdateInterestRateClick: () -> Unit = {},
    onImportBillsClick: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    val filteredCustomers = remember(state.customers, searchText) {
        val query = searchText.trim().lowercase()
        if (query.isBlank()) state.customers
        else state.customers.filter { user ->
            user.fullName.lowercase().contains(query) ||
                    user.email.lowercase().contains(query) ||
                    user.phoneNumber.lowercase().contains(query) ||
                    user.uid.lowercase().contains(query)
        }
    }
    val actions = listOf(
        OfficerQuickAction(
            label = "Tạo tài khoản",
            icon = Icons.Default.Add,
            tint = Color(0xFF4C6FFF),
            container = Color(0xFFE9EDFF),
            onClick = onCreateAccountClick
        ),
        OfficerQuickAction(
            label = "Sửa lãi suất",
            icon = Icons.Default.TrendingUp,
            tint = Color(0xFFFF9800),
            container = Color(0xFFFFF3E0),
            onClick = onUpdateInterestRateClick
        ),
        OfficerQuickAction(
            label = "Tải danh sách",
            icon = Icons.Default.Refresh,
            tint = Color(0xFF2BAE66),
            container = Color(0xFFE7F6EE),
            onClick = {
                showSearch = true
                onRefresh()
            }
        ),
        OfficerQuickAction(
            label = "Đăng xuất",
            icon = Icons.Rounded.Person,
            tint = Color(0xFFE53935),
            container = Color(0xFFFFEBEE),
            onClick = onLogout
        )
    )

    Scaffold(
        containerColor = OfficerScreenBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                OfficerHeaderSection(
                    officerName = state.user?.fullName?.takeIf { it.isNotBlank() } ?: "Nhân viên",
                    onProfileClick = { state.user?.uid?.let { onProfileClick(it) } }
                )
            }

            item {
                OfficerQuickActionsRow(actions = actions)
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Danh sách khách hàng",
                            color = OfficerTextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.isLoading) {
                            Text(
                                text = "Đang tải...",
                                color = OfficerTextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (showSearch) {
                        SearchField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = "Tìm theo tên, email, số ĐT, UID"
                        )
                    }
                }
            }

            if (state.isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    )
                }
            }

            state.error?.let { err ->
                item {
                    Text(
                        text = err,
                        color = BrandRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            if (!state.isLoading && filteredCustomers.isEmpty() && state.error == null) {
                item {
                    Text(
                        text = "Chưa có khách hàng",
                        color = OfficerTextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            items(filteredCustomers) { customer ->
                OfficerCustomerCard(
                    fullName = customer.fullName.ifBlank { "Khách hàng" },
                    email = customer.email,
                    phone = customer.phoneNumber,
                    kyc = customer.kycStatus.toString(),
                    role = customer.role.toString(),
                    onEdit = { onEditCustomerClick(customer.uid) }
                )
            }
        }
    }
}

@Composable
private fun OfficerHeaderSection(
    officerName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Chào buổi sáng,",
                color = OfficerTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = officerName,
                color = OfficerTextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Bảng điều khiển nhân viên",
                color = OfficerTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            // Notification button (visual parity with customer)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, Color(0xFFE6E8EC), CircleShape)
                    .background(Color.White, CircleShape),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = OfficerTextPrimary
                )
            }
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFEEF2FF), CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = BrandBlue
                )
            }
        }
    }
}

@Composable
private fun OfficerQuickActionsRow(
    actions: List<OfficerQuickAction>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tính năng nhanh",
            color = OfficerTextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            actions.forEach { action ->
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                        .clickable { action.onClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(action.container, RoundedCornerShape(16.dp)),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.label,
                            tint = action.tint,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = action.label,
                        color = OfficerTextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun OfficerCustomerCard(
    fullName: String,
    email: String,
    phone: String,
    kyc: String,
    role: String,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color(0xFFE9EDFF), CircleShape),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = Color(0xFF4C6FFF)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = fullName,
                    color = OfficerTextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = email,
                    color = OfficerTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = phone,
                    color = OfficerTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TagPill(label = "KYC: $kyc", bg = Color(0xFFE3F2FD), fg = Color(0xFF1565C0))
                    TagPill(label = "Role: $role", bg = Color(0xFFF2EBFF), fg = Color(0xFF6D28D9))
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Chỉnh sửa",
                    tint = BrandBlue
                )
            }
        }
    }
}

@Composable
private fun TagPill(label: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE6E8EC), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = OfficerTextSecondary
        )
        androidx.compose.material3.TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = OfficerTextSecondary) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = BrandBlue,
                focusedTextColor = OfficerTextPrimary,
                unfocusedTextColor = OfficerTextPrimary,
                focusedPlaceholderColor = OfficerTextSecondary,
                unfocusedPlaceholderColor = OfficerTextSecondary
            )
        )
    }
}

