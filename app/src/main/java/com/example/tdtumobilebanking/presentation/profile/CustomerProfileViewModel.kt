package com.example.tdtumobilebanking.presentation.profile

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.User
import com.example.tdtumobilebanking.domain.repository.AuthRepository
import com.example.tdtumobilebanking.domain.repository.UserRepository
import com.example.tdtumobilebanking.domain.usecase.user.UpdateUserUseCase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class CustomerProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val user: User? = null,
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val avatarUrl: String = "",
    val isUploadingAvatar: Boolean = false
)

@HiltViewModel
class CustomerProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val updateUserUseCase: UpdateUserUseCase,
    private val firebaseStorage: FirebaseStorage
) : BaseViewModel<CustomerProfileUiState>(CustomerProfileUiState()) {

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        android.util.Log.d("CustomerProfileViewModel", "========== [START] loadUserProfile() ==========")
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                android.util.Log.d("CustomerProfileViewModel", "[STEP 1] Getting current user from authRepository...")
                val currentUser = authRepository.currentUser().first()
                android.util.Log.d("CustomerProfileViewModel", "[STEP 2] Current user received - UID: '${currentUser?.uid}', Email: '${currentUser?.email}'")
                
                currentUser?.let { user ->
                    android.util.Log.d("CustomerProfileViewModel", "[STEP 3] User exists, loading profile for UID: '${user.uid}'")
                    
                    // Use collect with catch to handle Flow properly
                    var finalResult: ResultState<User>? = null
                    try {
                        android.util.Log.d("CustomerProfileViewModel", "[STEP 4] Starting getUserProfile flow collection...")
                        userRepository.getUserProfile(user.uid)
                            .catch { e ->
                                android.util.Log.e("CustomerProfileViewModel", "[STEP 4.1] Flow catch block triggered")
                                android.util.Log.e("CustomerProfileViewModel", "[STEP 4.2] Exception type: ${e::class.simpleName}")
                                android.util.Log.e("CustomerProfileViewModel", "[STEP 4.3] Exception message: ${e.message}")
                                // Check if it's a cancellation exception (AbortFlowException is internal)
                                if (e is kotlinx.coroutines.CancellationException) {
                                    android.util.Log.d("CustomerProfileViewModel", "[STEP 4.4] CancellationException (normal flow completion), ignoring")
                                    throw e // Re-throw cancellation exceptions
                                } else {
                                    android.util.Log.e("CustomerProfileViewModel", "[STEP 4.5] Real exception, emitting Error state")
                                    android.util.Log.e("CustomerProfileViewModel", "[STEP 4.6] Exception stack trace", e)
                                    finalResult = ResultState.Error(e)
                                }
                            }
                            .collect { state ->
                                android.util.Log.d("CustomerProfileViewModel", "[STEP 5] Received state in collect")
                                android.util.Log.d("CustomerProfileViewModel", "[STEP 5.1] State type: ${state::class.simpleName}")
                                
                                when (state) {
                                    is ResultState.Loading -> {
                                        android.util.Log.d("CustomerProfileViewModel", "[STEP 5.2] State is Loading, keeping loading state")
                                        // Keep loading, continue collecting
                                    }
                                    is ResultState.Success -> {
                                        android.util.Log.d("CustomerProfileViewModel", "[STEP 5.3] State is Success!")
                                        android.util.Log.d("CustomerProfileViewModel", "[STEP 5.4] User data object: ${state.data}")
                                        android.util.Log.d("CustomerProfileViewModel", "[STEP 5.5] FullName: '${state.data?.fullName}'")
                                        android.util.Log.d("CustomerProfileViewModel", "[STEP 5.6] Email: '${state.data?.email}'")
                                        android.util.Log.d("CustomerProfileViewModel", "[STEP 5.7] Phone: '${state.data?.phoneNumber}'")
                                        android.util.Log.d("CustomerProfileViewModel", "[STEP 5.8] UID: '${state.data?.uid}'")
                                        finalResult = state
                                    }
                                    is ResultState.Error -> {
                                        android.util.Log.e("CustomerProfileViewModel", "[STEP 5.9] State is Error!")
                                        android.util.Log.e("CustomerProfileViewModel", "[STEP 5.10] Error message: ${state.throwable.message}")
                                        android.util.Log.e("CustomerProfileViewModel", "[STEP 5.11] Error type: ${state.throwable::class.simpleName}")
                                        android.util.Log.e("CustomerProfileViewModel", "[STEP 5.12] Error stack trace", state.throwable)
                                        finalResult = state
                                    }
                                }
                            }
                        android.util.Log.d("CustomerProfileViewModel", "[STEP 6] Flow collection completed normally")
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        android.util.Log.d("CustomerProfileViewModel", "[STEP 6.1] CancellationException caught (normal flow completion)")
                        android.util.Log.d("CustomerProfileViewModel", "[STEP 6.2] CancellationException message: ${e.message}")
                        // This is expected when flow completes, ignore it
                    } catch (e: Exception) {
                        android.util.Log.e("CustomerProfileViewModel", "[STEP 6.3] Exception in flow collection")
                        android.util.Log.e("CustomerProfileViewModel", "[STEP 6.4] Exception type: ${e::class.simpleName}")
                        android.util.Log.e("CustomerProfileViewModel", "[STEP 6.5] Exception message: ${e.message}")
                        android.util.Log.e("CustomerProfileViewModel", "[STEP 6.6] Exception stack trace", e)
                        finalResult = ResultState.Error(e)
                    }
                    
                    android.util.Log.d("CustomerProfileViewModel", "[STEP 7] Processing final result")
                    android.util.Log.d("CustomerProfileViewModel", "[STEP 7.1] finalResult type: ${if (finalResult != null) finalResult::class.simpleName else "null"}")
                    
                    when (val result = finalResult) {
                        is ResultState.Success -> {
                            android.util.Log.d("CustomerProfileViewModel", "[STEP 8] Processing Success result")
                            val profile = result.data
                            android.util.Log.d("CustomerProfileViewModel", "[STEP 8.1] Profile object: $profile")
                            android.util.Log.d("CustomerProfileViewModel", "[STEP 8.2] Setting state with profile data")
                            setState {
                                copy(
                                    isLoading = false,
                                    user = profile,
                                    fullName = profile?.fullName ?: "",
                                    email = profile?.email ?: user.email ?: "",
                                    phoneNumber = profile?.phoneNumber ?: user.phoneNumber ?: "",
                                    avatarUrl = profile?.avatarUrl ?: ""
                                )
                            }
                            android.util.Log.d("CustomerProfileViewModel", "[STEP 8.3] State updated successfully")
                            android.util.Log.d("CustomerProfileViewModel", "[STEP 8.4] Final state values:")
                            android.util.Log.d("CustomerProfileViewModel", "[STEP 8.5]   - fullName: '${uiState.value.fullName}'")
                            android.util.Log.d("CustomerProfileViewModel", "[STEP 8.6]   - email: '${uiState.value.email}'")
                            android.util.Log.d("CustomerProfileViewModel", "[STEP 8.7]   - phoneNumber: '${uiState.value.phoneNumber}'")
                        }
                        is ResultState.Error -> {
                            android.util.Log.e("CustomerProfileViewModel", "[STEP 9] Processing Error result")
                            android.util.Log.e("CustomerProfileViewModel", "[STEP 9.1] Error message: ${result.throwable.message}")
                            setState {
                                copy(
                                    isLoading = false,
                                    error = result.throwable.message ?: "Không thể tải thông tin người dùng"
                                )
                            }
                        }
                        null -> {
                            android.util.Log.w("CustomerProfileViewModel", "[STEP 10] No result received (null)")
                            setState {
                                copy(
                                    isLoading = false,
                                    error = "Không thể tải thông tin người dùng"
                                )
                            }
                        }
                        else -> {
                            android.util.Log.w("CustomerProfileViewModel", "[STEP 11] Unexpected result type: ${result::class.simpleName}")
                            setState {
                                copy(
                                    isLoading = false,
                                    error = "Không thể tải thông tin người dùng"
                                )
                            }
                        }
                    }
                    android.util.Log.d("CustomerProfileViewModel", "========== [END] loadUserProfile() completed ==========")
                } ?: run {
                    android.util.Log.w("CustomerProfileViewModel", "[STEP 12] Current user is null")
                    setState { copy(isLoading = false, error = "User not logged in") }
                }
            } catch (e: Exception) {
                android.util.Log.e("CustomerProfileViewModel", "========== [EXCEPTION] in loadUserProfile ==========")
                android.util.Log.e("CustomerProfileViewModel", "[EXCEPTION] Type: ${e::class.simpleName}")
                android.util.Log.e("CustomerProfileViewModel", "[EXCEPTION] Message: ${e.message}")
                android.util.Log.e("CustomerProfileViewModel", "[EXCEPTION] Stack trace", e)
                e.printStackTrace()
                setState { copy(isLoading = false, error = e.message ?: "Lỗi không xác định") }
            }
        }
    }

    fun onEvent(event: CustomerProfileEvent) {
        when (event) {
            is CustomerProfileEvent.FullNameChanged -> setState { copy(fullName = event.value) }
            is CustomerProfileEvent.EmailChanged -> setState { copy(email = event.value) }
            is CustomerProfileEvent.PhoneNumberChanged -> setState { copy(phoneNumber = event.value) }
            is CustomerProfileEvent.AvatarUriSelected -> uploadAvatar(event.uri)
            CustomerProfileEvent.Save -> saveProfile()
        }
    }
    
    private fun uploadAvatar(uri: Uri) {
        val currentUser = uiState.value.user ?: return
        setState { copy(isUploadingAvatar = true, error = null) }
        
        viewModelScope.launch {
            try {
                val fileName = "avatars/${currentUser.uid}/${UUID.randomUUID()}.jpg"
                val storageRef = firebaseStorage.reference.child(fileName)
                
                val uploadTask = storageRef.putFile(uri).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()
                
                // Update user with new avatar URL
                val updatedUser = currentUser.copy(avatarUrl = downloadUrl)
                when (val result = updateUserUseCase(updatedUser)) {
                    is ResultState.Success<*> -> {
                        setState {
                            copy(
                                isUploadingAvatar = false,
                                avatarUrl = downloadUrl,
                                user = updatedUser
                            )
                        }
                    }
                    is ResultState.Error -> {
                        setState {
                            copy(
                                isUploadingAvatar = false,
                                error = result.throwable.message ?: "Lỗi khi cập nhật avatar"
                            )
                        }
                    }
                    else -> {
                        setState { copy(isUploadingAvatar = false) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CustomerProfileViewModel", "Error uploading avatar", e)
                setState {
                    copy(
                        isUploadingAvatar = false,
                        error = e.message ?: "Lỗi khi tải lên avatar"
                    )
                }
            }
        }
    }

    private fun saveProfile() {
        val current = uiState.value
        val currentUser = current.user ?: return
        
        if (current.fullName.isBlank()) {
            setState { copy(error = "Tên không được để trống") }
            return
        }

        setState { copy(isLoading = true, error = null, success = false) }
        viewModelScope.launch {
            val updatedUser = currentUser.copy(
                fullName = current.fullName.trim(),
                email = current.email.trim(),
                phoneNumber = current.phoneNumber.trim(),
                avatarUrl = current.avatarUrl
            )
            when (val result = updateUserUseCase(updatedUser)) {
                is ResultState.Error -> {
                    setState {
                        copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Lỗi khi cập nhật thông tin"
                        )
                    }
                }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success<*> -> {
                    setState {
                        copy(
                            isLoading = false,
                            success = true,
                            user = updatedUser
                        )
                    }
                }
            }
        }
    }
}

