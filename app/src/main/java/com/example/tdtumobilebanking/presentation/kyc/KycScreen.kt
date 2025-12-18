package com.example.tdtumobilebanking.presentation.kyc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed
import com.example.tdtumobilebanking.ui.theme.SurfaceHigh

@Composable
fun KycScreen(onCompleted: () -> Unit) {
    val bg = Brush.verticalGradient(listOf(BrandBlue.copy(alpha = 0.08f), BrandRed.copy(alpha = 0.05f)))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Định danh eKYC", style = MaterialTheme.typography.titleLarge)
        Text(
            "Chụp CMND/CCCD và ảnh selfie.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceHigh),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Bước 1: Chụp mặt trước & sau CCCD", style = MaterialTheme.typography.bodyLarge)
                Text("Bước 2: Chụp selfie nhận diện", style = MaterialTheme.typography.bodyLarge)
                Text("Bước 3: Upload lên /kyc/{uid}/", style = MaterialTheme.typography.bodyLarge)
                Text("Bước 4: Chờ Officer duyệt KYC", style = MaterialTheme.typography.bodyLarge)
                Button(
                    onClick = onCompleted,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đánh dấu đã tải lên")
                }
            }
        }
    }
}

