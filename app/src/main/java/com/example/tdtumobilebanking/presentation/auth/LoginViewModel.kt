package com.example.tdtumobilebanking.presentation.auth

import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.KycStatus
import com.example.tdtumobilebanking.domain.model.User
import com.example.tdtumobilebanking.domain.repository.AuthRepository
import com.example.tdtumobilebanking.domain.repository.UserRepository
import com.example.tdtumobilebanking.domain.usecase.auth.LoginUseCase
import com.example.tdtumobilebanking.domain.usecase.user.FetchUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedInUser: User? = null
)

sealed class LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent()
    data class PasswordChanged(val value: String) : LoginEvent()
    data object Submit : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val fetchUserProfileUseCase: FetchUserProfileUseCase,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : BaseViewModel<LoginUiState>(LoginUiState()) {

    private var fetchJob: Job? = null

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> setState { copy(email = event.value, error = null) }
            is LoginEvent.PasswordChanged -> setState { copy(password = event.value, error = null) }
            LoginEvent.Submit -> login()
        }
    }

    private fun login() {
        val email = uiState.value.email
        val password = uiState.value.password
        android.util.Log.d("LoginViewModel", "========== [START] login() ==========")
        android.util.Log.d("LoginViewModel", "[STEP 1] Email: '$email'")
        
        if (email.isBlank() || password.isBlank()) {
            android.util.Log.w("LoginViewModel", "[STEP 2] Email or password is blank")
            setState { copy(error = "Email/Password required") }
            return
        }
        
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                android.util.Log.d("LoginViewModel", "[STEP 3] Calling loginUseCase...")
                when (val result = loginUseCase(email, password)) {
                    is ResultState.Error -> {
                        android.util.Log.e("LoginViewModel", "[STEP 4] Login failed: ${result.throwable.message}")
                        setState { copy(isLoading = false, error = result.throwable.message) }
                    }
                    ResultState.Loading -> {
                        android.util.Log.d("LoginViewModel", "[STEP 5] Login in progress...")
                        setState { copy(isLoading = true, error = null) }
                    }
                    is ResultState.Success -> {
                        android.util.Log.d("LoginViewModel", "[STEP 6] Login successful")
                        android.util.Log.d("LoginViewModel", "[STEP 6.1] User UID: '${result.data.uid}'")
                        android.util.Log.d("LoginViewModel", "[STEP 6.2] User email: '${result.data.email}'")
                        android.util.Log.d("LoginViewModel", "[STEP 6.3] User role: '${result.data.role}'")
                        loadProfile(result.data.uid, email)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "[EXCEPTION] in login", e)
                setState { copy(isLoading = false, error = e.message ?: "Login failed") }
            }
        }
    }

    private fun loadProfile(uid: String, email: String) {
        android.util.Log.d("LoginViewModel", "========== [START] loadProfile() ==========")
        android.util.Log.d("LoginViewModel", "[STEP 1] Loading profile for UID: '$uid', Email: '$email'")
        
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                fetchUserProfileUseCase(uid).collectLatest { result ->
                    android.util.Log.d("LoginViewModel", "[STEP 2] Profile result received: ${result::class.simpleName}")
                    
                    when (result) {
                        is ResultState.Error -> {
                            android.util.Log.e("LoginViewModel", "[STEP 3] Profile load failed")
                            android.util.Log.e("LoginViewModel", "[STEP 3.1] Error message: ${result.throwable.message}")
                            android.util.Log.e("LoginViewModel", "[STEP 3.2] Error type: ${result.throwable::class.simpleName}")
                            
                            // If profile is missing, try to create it
                            if (result.throwable.message?.contains("User profile missing") == true) {
                                android.util.Log.w("LoginViewModel", "[STEP 4] Profile missing, attempting to create...")
                                try {
                                    val currentUser = authRepository.currentUser().first()
                                    currentUser?.let { authUser ->
                                        android.util.Log.d("LoginViewModel", "[STEP 4.1] Creating bootstrap user profile")
                                        val bootstrapUser = User(
                                            uid = uid,
                                            fullName = authUser.fullName.ifBlank { email.substringBefore("@") },
                                            email = email,
                                            role = if (email.contains("off", ignoreCase = true)) "OFFICER" else "CUSTOMER",
                                            phoneNumber = authUser.phoneNumber,
                                            kycStatus = KycStatus.VERIFIED, // Assume verified for existing users
                                            avatarUrl = ""
                                        )
                                        android.util.Log.d("LoginViewModel", "[STEP 4.2] Bootstrap user: $bootstrapUser")
                                        when (val createResult = userRepository.createOrUpdateUser(bootstrapUser)) {
                                            is ResultState.Success -> {
                                                android.util.Log.d("LoginViewModel", "[STEP 4.3] Profile created successfully")
                                                setState { copy(isLoading = false, loggedInUser = bootstrapUser, error = null) }
                                            }
                                            is ResultState.Error -> {
                                                android.util.Log.e("LoginViewModel", "[STEP 4.4] Failed to create profile: ${createResult.throwable.message}")
                                                setState { copy(isLoading = false, error = "Không thể tạo thông tin người dùng: ${createResult.throwable.message}") }
                                            }
                                            else -> {
                                                android.util.Log.w("LoginViewModel", "[STEP 4.5] Create profile still loading")
                                            }
                                        }
                                    } ?: run {
                                        android.util.Log.e("LoginViewModel", "[STEP 4.6] Auth user is null, cannot create profile")
                                        setState { copy(isLoading = false, error = result.throwable.message) }
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("LoginViewModel", "[STEP 4.7] Exception creating profile", e)
                                    setState { copy(isLoading = false, error = result.throwable.message) }
                                }
                            } else {
                                setState { copy(isLoading = false, error = result.throwable.message) }
                            }
                        }
                        ResultState.Loading -> {
                            android.util.Log.d("LoginViewModel", "[STEP 5] Profile loading...")
                            setState { copy(isLoading = true, error = null) }
                        }
                        is ResultState.Success -> {
                            android.util.Log.d("LoginViewModel", "[STEP 6] Profile loaded successfully")
                            android.util.Log.d("LoginViewModel", "[STEP 6.1] User: ${result.data}")
                            android.util.Log.d("LoginViewModel", "[STEP 6.2] User role: '${result.data.role}'")
                            setState { copy(isLoading = false, loggedInUser = result.data, error = null) }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "[EXCEPTION] in loadProfile", e)
                setState { copy(isLoading = false, error = e.message ?: "Không thể tải thông tin người dùng") }
            }
        }
    }

    fun nextDestination(): LoginDestination {
        val user = uiState.value.loggedInUser ?: return LoginDestination.Login
        if (user.kycStatus != KycStatus.VERIFIED) return LoginDestination.Kyc
        return if (user.role == "OFFICER") LoginDestination.Officer else LoginDestination.Customer
    }
}

enum class LoginDestination { Login, Customer, Officer, Kyc }

