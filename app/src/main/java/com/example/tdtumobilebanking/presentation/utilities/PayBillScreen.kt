package com.example.tdtumobilebanking.presentation.utilities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.ReceiptLong
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
import com.example.tdtumobilebanking.ui.theme.BrandBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayBillScreen(
    onBack: () -> Unit,
    onUtilitySelected: (String) -> Unit
) {
    // Chỉ hiển thị các mục liên quan Pay Bill
    val payBillItems = listOf(
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
            id = "E_COMMERCE",
            title = "Thanh toán thương mại điện tử",
            subtitle = "Thanh toán đơn hàng online",
            icon = Icons.Rounded.ShoppingCart,
            iconTint = Color(0xFF00BCD4),
            containerColor = Color(0xFFE0F7FA)
        ),
        UtilityOption(
            id = "BILL_OTHER",
            title = "Thanh toán hóa đơn khác",
            subtitle = "Sườn chức năng (tích hợp sau)",
            icon = Icons.Rounded.ReceiptLong,
            iconTint = Color(0xFFFB8C00),
            containerColor = Color(0xFFFFF2DF)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pay Bill",
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
            items(payBillItems) { item ->
                PayBillCard(
                    utility = item,
                    onClick = { onUtilitySelected(item.id) }
                )
            }
        }
    }
}

@Composable
private fun PayBillCard(
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
                        color = Color(0xFF7C8493),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}


