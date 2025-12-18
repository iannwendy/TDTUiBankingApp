package com.example.tdtumobilebanking.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tdtumobilebanking.R
import com.example.tdtumobilebanking.ui.theme.BrandRed

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    nextDestination: () -> LoginDestination,
    onNavigateCustomer: () -> Unit,
    onNavigateOfficer: () -> Unit,
    onNavigateKyc: () -> Unit
) {
    var hasNavigated by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.loggedInUser) {
        if (state.loggedInUser != null && !hasNavigated) {
            hasNavigated = true
            when (nextDestination()) {
                LoginDestination.Customer -> onNavigateCustomer()
                LoginDestination.Officer -> onNavigateOfficer()
                LoginDestination.Kyc -> onNavigateKyc()
                else -> Unit
            }
        }
    }

    Box(
            modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F4EF))
            .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            IconBadge()
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "TDTU iBanking",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Color(0xFF1F2A44),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Your trusted financial partner",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )
            IconRow()
            Spacer(modifier = Modifier.height(24.dp))
            LoginForm(
                state = state,
                isPasswordVisible = isPasswordVisible,
                onTogglePassword = { isPasswordVisible = !isPasswordVisible },
                onEvent = onEvent,
                focusManager = focusManager
            )
            Spacer(modifier = Modifier.height(12.dp))
            state.error?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = BrandRed,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "© 2025 TDTU iBanking",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun IconBadge() {
    Box(
        modifier = Modifier
            .size(90.dp)
        .background(
            brush = Brush.linearGradient(listOf(Color(0xFF2758E7), Color(0xFF1F47C4))),
            shape = RoundedCornerShape(22.dp)
        ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.tdtu_logo),
            contentDescription = "TDTU iBanking",
            modifier = Modifier.size(56.dp)
        )
    }
}

@Composable
private fun IconRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIcon(
            background = Color(0xFFFFF0E5),
            tint = Color(0xFFFC9803),
            icon = Icons.Outlined.Key
        )
        CircleIcon(
            background = Color(0xFFE7F0FF),
            tint = Color(0xFF1D4ED8),
            icon = Icons.Outlined.Lock
        )
        CircleIcon(
            background = Color(0xFFEAF8EE),
            tint = Color(0xFF22C55E),
            icon = Icons.Outlined.CheckCircle
        )
    }
}

@Composable
private fun CircleIcon(
    background: Color,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(background, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun LoginForm(
    state: LoginUiState,
    isPasswordVisible: Boolean,
    onTogglePassword: () -> Unit,
    onEvent: (LoginEvent) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
                Text(
            text = "Email or Username",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF1F2A44)
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF9CA3AF)) },
            placeholder = { Text("Enter your email or username", color = Color(0xFF9CA3AF)) },
            singleLine = true,
                    shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Text(
            text = "Password",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF1F2A44)
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF9CA3AF)) },
            placeholder = { Text("Enter your password", color = Color(0xFF9CA3AF)) },
            singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onEvent(LoginEvent.Submit)
                            focusManager.clearFocus()
                        }
                    ),
                    trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                .height(56.dp),
                    onClick = { onEvent(LoginEvent.Submit) },
                    enabled = !state.isLoading,
            shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1F4CE6),
                disabledContainerColor = Color(0xFF1F4CE6).copy(alpha = 0.5f)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                    Text(
                    "Login",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

