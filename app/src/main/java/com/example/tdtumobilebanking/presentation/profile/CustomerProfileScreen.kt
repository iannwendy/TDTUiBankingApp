package com.example.tdtumobilebanking.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.tdtumobilebanking.presentation.dashboard.CustomerBottomNavBar
import com.example.tdtumobilebanking.presentation.dashboard.CustomerTab
import com.example.tdtumobilebanking.ui.theme.BrandBlue
import com.example.tdtumobilebanking.ui.theme.BrandRed

@Composable
fun CustomerProfileScreen(
    state: CustomerProfileUiState,
    onEvent: (CustomerProfileEvent) -> Unit,
    onBack: () -> Unit,
    onHomeClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onUtilitiesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    LaunchedEffect(state.success) {
        if (state.success) {
            // Có thể thêm snackbar hoặc toast ở đây
        }
    }

    Scaffold(
        bottomBar = {
            CustomerBottomNavBar(
                selectedTab = CustomerTab.PROFILE,
                onHomeClick = onHomeClick,
                onAccountsClick = onAccountsClick,
                onUtilitiesClick = onUtilitiesClick,
                onProfileClick = onProfileClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandBlue)
            }
            Text(
                text = "Thông tin cá nhân",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
                val imagePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        selectedImageUri = it
                        onEvent(CustomerProfileEvent.AvatarUriSelected(it))
                    }
                }
                
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isUploadingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(120.dp),
                            color = BrandBlue
                        )
                    } else {
                        val imageUri = selectedImageUri?.toString() ?: state.avatarUrl
                        if (imageUri.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(imageUri)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, BrandBlue, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(BrandBlue.copy(alpha = 0.2f))
                                    .border(3.dp, BrandBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(60.dp),
                                    tint = BrandBlue
                                )
                            }
                        }
                    }
                    
                    // Upload button overlay
                    if (!state.isUploadingAvatar) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .clickable { imagePicker.launch("image/*") }
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Upload avatar",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(8.dp)
                                    .background(BrandBlue, CircleShape)
                                    .padding(6.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
                
                Text(
                    text = "Nhấn để cập nhật ảnh đại diện",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Full Name
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Họ và tên",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = { onEvent(CustomerProfileEvent.FullNameChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("Nhập họ và tên") },
                        singleLine = true,
                        enabled = !state.isLoading
                    )
                }

                // Email
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { onEvent(CustomerProfileEvent.EmailChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("Nhập email") },
                        singleLine = true,
                        enabled = !state.isLoading
                    )
                }

                // Phone Number
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Số điện thoại",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = { onEvent(CustomerProfileEvent.PhoneNumberChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrandBlue, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        placeholder = { Text("Nhập số điện thoại") },
                        singleLine = true,
                        enabled = !state.isLoading
                    )
                }

                if (state.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                state.error?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandRed.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandRed)
                    ) {
                        Text(
                            text = it,
                            color = BrandRed,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (state.success) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = BrandBlue
                            )
                            Text(
                                text = "Cập nhật thông tin thành công!",
                                color = BrandBlue,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = { onEvent(CustomerProfileEvent.Save) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            enabled = !state.isLoading
        ) {
            Text("Lưu thông tin", color = Color.White, fontSize = 16.sp)
        }
    }
    }
}

sealed class CustomerProfileEvent {
    data class FullNameChanged(val value: String) : CustomerProfileEvent()
    data class EmailChanged(val value: String) : CustomerProfileEvent()
    data class PhoneNumberChanged(val value: String) : CustomerProfileEvent()
    data class AvatarUriSelected(val uri: Uri) : CustomerProfileEvent()
    data object Save : CustomerProfileEvent()
}

