package com.example.tdtumobilebanking.presentation.utilities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocalMovies
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tdtumobilebanking.presentation.dashboard.CustomerBottomNavBar
import com.example.tdtumobilebanking.presentation.dashboard.CustomerTab
import com.example.tdtumobilebanking.ui.theme.BrandBlue

data class UtilityOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconTint: Color,
    val containerColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilitiesScreen(
    onBack: () -> Unit,
    onUtilitySelected: (String) -> Unit,
    onBillPaymentClick: (() -> Unit)? = null,
    onHomeClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onUtilitiesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val utilities = listOf(
        UtilityOption(
            id = "BILL_PAYMENT_STRIPE",
            title = "Thanh toán hóa đơn",
            subtitle = "Tra cứu & thanh toán qua Stripe",
            icon = Icons.Rounded.Receipt,
            iconTint = Color(0xFF6772E5),
            containerColor = Color(0xFFEDE7F6)
        ),
        UtilityOption(
            id = "ELECTRIC_BILL",
            title = "Thanh toán hóa đơn điện",
            subtitle = "Thanh toán hóa đơn điện lực",
            icon = Icons.Rounded.ElectricBolt,
            iconTint = Color(0xFFFFB800),
            containerColor = Color(0xFFFFF8E1)
        ),
        UtilityOption(
            id = "WATER_BILL",
            title = "Thanh toán hóa đơn nước",
            subtitle = "Thanh toán hóa đơn nước",
            icon = Icons.Rounded.WaterDrop,
            iconTint = Color(0xFF2196F3),
            containerColor = Color(0xFFE3F2FD)
        ),
        UtilityOption(
            id = "PHONE_TOPUP",
            title = "Nạp tiền điện thoại",
            subtitle = "Nạp tiền cho số điện thoại",
            icon = Icons.Rounded.PhoneAndroid,
            iconTint = Color(0xFF4CAF50),
            containerColor = Color(0xFFE8F5E9)
        ),
        UtilityOption(
            id = "FLIGHT_TICKET",
            title = "Mua vé máy bay",
            subtitle = "Đặt và thanh toán vé máy bay",
            icon = Icons.Rounded.Flight,
            iconTint = Color(0xFF9C27B0),
            containerColor = Color(0xFFF3E5F5)
        ),
        UtilityOption(
            id = "MOVIE_TICKET",
            title = "Mua vé xem phim",
            subtitle = "Đặt và thanh toán vé xem phim",
            icon = Icons.Rounded.LocalMovies,
            iconTint = Color(0xFFE91E63),
            containerColor = Color(0xFFFCE4EC)
        ),
        UtilityOption(
            id = "HOTEL_BOOKING",
            title = "Đặt phòng khách sạn",
            subtitle = "Đặt và thanh toán phòng khách sạn",
            icon = Icons.Rounded.Hotel,
            iconTint = Color(0xFFFF5722),
            containerColor = Color(0xFFFFEBEE)
        ),
        UtilityOption(
            id = "E_COMMERCE",
            title = "Thanh toán thương mại điện tử",
            subtitle = "Thanh toán đơn hàng online",
            icon = Icons.Rounded.ShoppingCart,
            iconTint = Color(0xFF00BCD4),
            containerColor = Color(0xFFE0F7FA)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tiện ích",
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
        },
        bottomBar = {
            CustomerBottomNavBar(
                selectedTab = CustomerTab.UTILITIES,
                onHomeClick = onHomeClick,
                onAccountsClick = onAccountsClick,
                onUtilitiesClick = onUtilitiesClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(utilities) { utility ->
                UtilityCard(
                    utility = utility,
                    onClick = {
                        if (utility.id == "BILL_PAYMENT_STRIPE") {
                            onBillPaymentClick?.invoke() ?: onUtilitySelected(utility.id)
                        } else {
                            onUtilitySelected(utility.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun UtilityCard(
    utility: UtilityOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        utility.containerColor,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = utility.icon,
                    contentDescription = null,
                    tint = utility.iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = utility.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                )
                Text(
                    text = utility.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF7C8493)
                    )
                )
            }
        }
    }
}

