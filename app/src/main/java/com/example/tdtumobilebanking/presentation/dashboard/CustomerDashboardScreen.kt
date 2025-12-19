package com.example.tdtumobilebanking.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tdtumobilebanking.R
import com.example.tdtumobilebanking.domain.model.Account
import com.example.tdtumobilebanking.domain.model.AccountType
import com.example.tdtumobilebanking.domain.model.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import java.util.Calendar

private val ScreenBg = Color(0xFFF8F9FA)
private val CardGradient = Brush.linearGradient(
    listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))
)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF7C8493)
private val Positive = Color(0xFF14A44D)

data class BalanceCard(
    val title: String,
    val amount: String,
    val subtitle: String,
    val maskedNumber: String,
    val isPrimary: Boolean = false,
    val pillText: String? = null,
    val accountId: String? = null,
    val currencyCode: String? = null,
    val accountType: AccountType = AccountType.CHECKING,
    val interestRate: Double? = null,
    val monthlyValue: Double? = null,
    val mortgagePayment: Double? = null
)

data class QuickAction(
    val label: String,
    val iconTint: Color,
    val container: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class TransactionUi(
    val title: String,
    val subtitle: String,
    val amount: String,
    val isPositive: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val background: Color
)

@Composable
@Suppress("UNUSED_PARAMETER")
fun CustomerDashboardScreen(
    state: DashboardUiState,
    onRefresh: () -> Unit,
    onTransferClick: () -> Unit,
    onPayBillClick: () -> Unit,
    onMapClick: () -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit = {},
    onAccountSelected: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onUtilitiesClick: () -> Unit = {},
    onProfileNavClick: () -> Unit = {},
    onDepositClick: (String?) -> Unit = {},
    onWithdrawClick: (String?) -> Unit = {}
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        android.util.Log.d("DashboardUI", "Screen launched -> trigger onRefresh()")
        onRefresh()
    }

    val quickActions = remember {
        listOf(
            QuickAction("Chuyển tiền", Color(0xFF4C6FFF), Color(0xFFE9EDFF), Icons.AutoMirrored.Rounded.Send),
            QuickAction("Nạp tiền", Color(0xFF2BAE66), Color(0xFFE7F6EE), Icons.Rounded.AccountBalanceWallet),
            QuickAction("Thanh toán", Color(0xFFFB8C00), Color(0xFFFFF2DF), Icons.AutoMirrored.Rounded.ReceiptLong),
            QuickAction("Thêm", Color(0xFF8B6CFF), Color(0xFFF2EBFF), Icons.Rounded.MoreHoriz)
        )
    }

    val primaryAccount = state.accounts.firstOrNull()
    val totalBalance = state.accounts
        .filter { it.accountType == AccountType.CHECKING }
        .takeIf { it.isNotEmpty() }
        ?.sumOf { it.balance }

    val cards = remember(state.accounts, state.monthlyProfitPreview, state.mortgagePreview) {
        buildList {
            if (totalBalance != null && primaryAccount != null) {
                add(
                    BalanceCard(
                        title = "Tổng số dư",
                        amount = formatCurrency(totalBalance, primaryAccount.currency),
                        subtitle = "Tất cả tài khoản thanh toán",
                        maskedNumber = "",
                        isPrimary = true,
                        accountId = primaryAccount.accountId,
                        currencyCode = primaryAccount.currency
                    )
    )
            }
            state.accounts.forEach { acc ->
                add(
                    acc.toBalanceCard(
                        isPrimary = false,
                        monthlyProfit = state.monthlyProfitPreview[acc.accountId],
                        mortgagePayment = state.mortgagePreview[acc.accountId]
                    )
                )
            }
        }
    }

    val selectedAccountId = state.selectedAccountId ?: primaryAccount?.accountId
    val selectedCurrency = state.accounts.firstOrNull { it.accountId == selectedAccountId }?.currency ?: "USD"
    val transactions = remember(state.transactions, selectedAccountId) {
        android.util.Log.d(
            "DashboardUI",
            "transactions map size=${state.transactions.size}, selectedAccount=$selectedAccountId, currency=$selectedCurrency"
        )
        state.transactions.map { it.toUi(selectedAccountId, selectedCurrency) }
    }

    Scaffold(
        containerColor = ScreenBg,
        bottomBar = {
            CustomerBottomNavBar(
                selectedTab = CustomerTab.HOME,
                onHomeClick = onHomeClick,
                onAccountsClick = onAccountsClick,
                onUtilitiesClick = onUtilitiesClick,
                onProfileClick = onProfileNavClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                HeaderSection(
                    name = state.user?.fullName?.takeIf { it.isNotBlank() } ?: "Khách hàng",
                    avatarUrl = state.user?.avatarUrl.orEmpty(),
                    onProfileClick = onProfileClick,
                    onLogoutClick = onLogout
                )
            }
                item {
                BalanceCarousel(
                    cards = cards,
                    selectedAccountId = selectedAccountId,
                    onAccountSelected = onAccountSelected
                    )
                }
                item {
                QuickActionsRow(
                    quickActions, 
                    onTransferClick, 
                    { onDepositClick(primaryAccount?.accountId) },
                    onPayBillClick,
                    { showMoreMenu = true }
                ) 
            }
            item { TransactionsSection(transactions, state.isTransactionsLoading) }
        }
        
        // More Menu Dialog - outside LazyColumn
        if (showMoreMenu) {
            MoreMenuDialog(
                onDismiss = { showMoreMenu = false },
                onWithdrawClick = {
                    showMoreMenu = false
                    onWithdrawClick(primaryAccount?.accountId)
                },
                onProfileClick = {
                    showMoreMenu = false
                    onProfileNavClick()
                }
            )
        }
    }
}

@Composable
private fun HeaderSection(
    name: String,
    avatarUrl: String,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Chào buổi sáng,",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = name,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            LogoutButton(onLogoutClick = onLogoutClick)
        }
    }
}

@Composable
private fun LogoutButton(onLogoutClick: () -> Unit) {
        Box(
            modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFFFEBEE))
            .border(1.dp, Color(0xFFE53935), RoundedCornerShape(999.dp))
            .clickable { onLogoutClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
        Text(
            text = "Đăng xuất",
            color = Color(0xFFD32F2F),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun BalanceCarousel(
    cards: List<BalanceCard>,
    selectedAccountId: String?,
    onAccountSelected: (String) -> Unit
) {
    if (cards.isEmpty()) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chưa có tài khoản",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cards.size) { index ->
                val card = cards[index]
                val isSelected = card.accountId != null && card.accountId == selectedAccountId
                if (card.isPrimary) {
                    PrimaryBalanceCard(card)
                } else {
                    SecondaryBalanceCard(
                        card = card,
                        isSelected = isSelected,
                        onClick = { card.accountId?.let(onAccountSelected) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrimaryBalanceCard(card: BalanceCard) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CardGradient)
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.title,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                ) {
                        Icon(
                            imageVector = Icons.Rounded.CreditCard,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = card.amount,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                    modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = card.subtitle,
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = card.maskedNumber,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.24f)),
                            contentAlignment = Alignment.Center
            ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryBalanceCard(
    card: BalanceCard,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(card.title, color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Text(card.amount, color = TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFFDDE7FF) else Color(0xFFEAF7EE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Eco,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF3F51B5) else Color(0xFF2BAE66)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(card.subtitle, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    Text(card.maskedNumber, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    when (card.accountType) {
                        AccountType.SAVING -> {
                            val rate = card.interestRate
                            val monthly = card.monthlyValue
                            if (rate != null) {
                                Text(
                                    text = "Interest: ${formatPercent(rate)}",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (monthly != null) {
                                Text(
                                    text = "Monthly profit: ${formatCurrency(monthly, card.currencyCode ?: "USD")}",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        AccountType.MORTGAGE -> {
                            val payment = card.mortgagePayment
                            if (payment != null) {
                                Text(
                                    text = "Monthly payment: ${formatCurrency(payment, card.currencyCode ?: "USD")}",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        else -> {}
                    }
                }
                card.pillText?.let { pill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE3F3E7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pill,
                            color = Color(0xFF2BAE66),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    actions: List<QuickAction>,
    onTransferClick: () -> Unit,
    onTopUpClick: () -> Unit,
    onPayBillClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Tính năng nhanh",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            actions.forEach { action ->
                val onClick: () -> Unit = when (action.label) {
                    "Chuyển tiền" -> onTransferClick
                    "Nạp tiền" -> onTopUpClick
                    "Thanh toán" -> onPayBillClick
                    else -> onMoreClick
                }
                ActionItem(action, onClick)
            }
        }
    }
}

@Composable
private fun ActionItem(action: QuickAction, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(action.container)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = action.iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
                    Text(
            text = action.label,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TransactionsSection(
    transactions: List<TransactionUi>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Giao dịch",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
                    )
            Text(
                text = "Xem tất cả",
                color = Color(0xFF3F51B5),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        when {
            isLoading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            transactions.isEmpty() -> Text(
                text = "Chưa có giao dịch",
                color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                transactions.forEach { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: TransactionUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(transaction.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = null,
                    tint = TextPrimary
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transaction.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transaction.subtitle,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = transaction.amount,
                color = if (transaction.isPositive) Positive else TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun Account.toBalanceCard(
    isPrimary: Boolean,
    monthlyProfit: Double?,
    mortgagePayment: Double?
): BalanceCard {
    val amountText = formatCurrency(balance, currency)
    val subtitle = accountTypeLabel(accountType)
    val pillText = when (accountType) {
        AccountType.SAVING -> monthlyProfit?.let { "+${formatCurrency(it, currency)} /mo" }
        AccountType.MORTGAGE -> mortgagePayment?.let { "-${formatCurrency(it, currency)} /mo" }
        else -> null
    }
    return BalanceCard(
        title = if (isPrimary) "Total Balance" else subtitle,
        amount = amountText,
        subtitle = subtitle,
        maskedNumber = maskAccountNumber(accountId),
        isPrimary = isPrimary,
        pillText = pillText,
        accountId = accountId,
        currencyCode = currency,
        accountType = accountType,
        interestRate = interestRate,
        monthlyValue = monthlyProfit,
        mortgagePayment = mortgagePayment
    )
}

private fun Transaction.toUi(accountId: String?, currency: String): TransactionUi {
    val isOutgoing = accountId != null && senderAccountId == accountId
    val amountText = formatCurrency(amount, currency)
    val signedAmount = (if (isOutgoing) "-" else "+") + amountText
    val icon = if (isOutgoing) Icons.AutoMirrored.Rounded.Send else Icons.Rounded.AccountBalance
    val background = if (isOutgoing) Color(0xFFFFF3E8) else Color(0xFFE9F2FF)
    val title = if (description.isNotBlank()) description else "Transaction"

    return TransactionUi(
        title = title,
        subtitle = formatTimestampReadable(timestamp),
        amount = signedAmount,
        isPositive = !isOutgoing,
        icon = icon,
        background = background
    )
}

private fun formatCurrency(value: Double, currencyCode: String): String {
    return runCatching {
        // Use NumberFormat to avoid scientific notation
        val format = NumberFormat.getNumberInstance(Locale.US)
        format.maximumFractionDigits = 0
        format.minimumFractionDigits = 0
        "${format.format(value)} $currencyCode"
    }.getOrElse { 
        // Fallback: format without scientific notation
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        "${formatter.format(value)} $currencyCode"
    }
}

private fun formatPercent(rate: Double): String {
    return runCatching {
        val format = NumberFormat.getPercentInstance(Locale.US)
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = 2
        format.format(rate)
    }.getOrElse { "${rate}%" }
}

private fun maskAccountNumber(accountId: String): String {
    if (accountId.length < 4) return "**** **** **** $accountId"
    val last = accountId.takeLast(4)
    return "**** **** **** $last"
}

private fun formatTimestampReadable(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { time = Date(timestamp) }

    val timeFormatter = SimpleDateFormat("h:mm a", Locale.US)
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

    val isSameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    val isYesterday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1

    val timePart = timeFormatter.format(Date(timestamp))
    return when {
        isSameDay -> "Hôm nay, $timePart"
        isYesterday -> "Hôm qua, $timePart"
        else -> dateFormatter.format(Date(timestamp))
    }
}

private fun accountTypeLabel(type: AccountType): String = when (type) {
    AccountType.CHECKING -> "Tài khoản thanh toán"
    AccountType.SAVING -> "Tài khoản tiết kiệm"
    AccountType.MORTGAGE -> "Khoản vay thế chấp"
    else -> type.name.lowercase().replaceFirstChar { it.uppercase() }
}

@Composable
private fun BottomNavBar(
    onHomeClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onUtilitiesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            NavItem("Trang chủ", Icons.Rounded.Home, selected = true, onClick = onHomeClick),
            NavItem("Tài khoản", Icons.Rounded.AccountBalance, selected = false, onClick = onAccountsClick),
            NavItem("Tiện ích", Icons.Rounded.GridView, selected = false, onClick = onUtilitiesClick),
            NavItem("Hồ sơ", Icons.Rounded.Person, selected = false, onClick = onProfileClick),
        )
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color(0xFF3F51B5),
                    selectedTextColor = Color(0xFF3F51B5),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (item.selected) Color(0xFF3F51B5) else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (item.selected) Color(0xFF3F51B5) else TextSecondary,
                        fontWeight = if (item.selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

enum class CustomerTab {
    HOME, ACCOUNTS, UTILITIES, PROFILE
}

@Composable
fun CustomerBottomNavBar(
    selectedTab: CustomerTab,
    onHomeClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onUtilitiesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            NavItem("Trang chủ", Icons.Rounded.Home, selected = selectedTab == CustomerTab.HOME, onClick = onHomeClick),
            NavItem("Tài khoản", Icons.Rounded.AccountBalance, selected = selectedTab == CustomerTab.ACCOUNTS, onClick = onAccountsClick),
            NavItem("Tiện ích", Icons.Rounded.GridView, selected = selectedTab == CustomerTab.UTILITIES, onClick = onUtilitiesClick),
            NavItem("Hồ sơ", Icons.Rounded.Person, selected = selectedTab == CustomerTab.PROFILE, onClick = onProfileClick),
        )
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Color(0xFF3F51B5),
                    selectedTextColor = Color(0xFF3F51B5),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (item.selected) Color(0xFF3F51B5) else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (item.selected) Color(0xFF3F51B5) else TextSecondary,
                        fontWeight = if (item.selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}

private data class NavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
private fun MoreMenuDialog(
    onDismiss: () -> Unit,
    onWithdrawClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Thêm chức năng",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Rút tiền
                MoreMenuItem(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "Rút tiền",
                    subtitle = "Rút tiền từ tài khoản",
                    onClick = {
                        onWithdrawClick()
                        onDismiss()
                    }
                )
                
                // Thay đổi thông tin
                MoreMenuItem(
                    icon = Icons.Rounded.Person,
                    title = "Thay đổi thông tin",
                    subtitle = "Cập nhật thông tin cá nhân",
                    onClick = {
                        onProfileClick()
                        onDismiss()
                    }
                )
                
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Đóng", color = TextPrimary)
            }
        }
    )
}

@Composable
private fun MoreMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2EBFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF8B6CFF),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
private fun CustomerDashboardPreview() {
    CustomerDashboardScreen(
        state = DashboardUiState(),
        onRefresh = {},
        onTransferClick = {},
        onPayBillClick = {},
        onMapClick = {},
        onLogout = {},
        onProfileClick = {}
    )
}

