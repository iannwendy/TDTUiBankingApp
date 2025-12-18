package com.example.tdtumobilebanking.presentation.branches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed

@Composable
fun BranchMapScreen(
    state: BranchUiState,
    onRefresh: () -> Unit
) {
    val bg = Brush.verticalGradient(listOf(BrandBlue.copy(alpha = 0.06f), BrandRed.copy(alpha = 0.04f)))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Chi nhánh & Bản đồ",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "Xem danh sách chi nhánh và điều hướng Google Maps.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text("Làm mới", modifier = Modifier.padding(start = 8.dp))
        }
        if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        state.error?.let { Text("Error: $it", color = BrandRed) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.branches) { branch ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(branch.name, style = MaterialTheme.typography.titleMedium)
                        Text(branch.address, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "Lat: ${branch.latitude}, Lng: ${branch.longitude}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { /* TODO: deep link to Maps */ },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Text("Mở Google Maps", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

