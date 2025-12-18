package com.example.tdtumobilebanking.presentation.officer

import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.User
import com.example.tdtumobilebanking.domain.repository.UserRepository
import com.example.tdtumobilebanking.domain.usecase.user.UpdateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditCustomerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val customer: User? = null,
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = ""
)

sealed class EditCustomerEvent {
    data class LoadCustomer(val uid: String) : EditCustomerEvent()
    data class FullNameChanged(val value: String) : EditCustomerEvent()
    data class EmailChanged(val value: String) : EditCustomerEvent()
    data class PhoneNumberChanged(val value: String) : EditCustomerEvent()
    data object Save : EditCustomerEvent()
}

@HiltViewModel
class EditCustomerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val updateUserUseCase: UpdateUserUseCase
) : BaseViewModel<EditCustomerUiState>(EditCustomerUiState()) {

    fun onEvent(event: EditCustomerEvent) {
        when (event) {
            is EditCustomerEvent.LoadCustomer -> loadCustomer(event.uid)
            is EditCustomerEvent.FullNameChanged -> setState { copy(fullName = event.value) }
            is EditCustomerEvent.EmailChanged -> setState { copy(email = event.value) }
            is EditCustomerEvent.PhoneNumberChanged -> setState { copy(phoneNumber = event.value) }
            EditCustomerEvent.Save -> saveCustomer()
        }
    }

    private fun loadCustomer(uid: String) {
        android.util.Log.d("EditCustomerViewModel", "========== [START] loadCustomer() ==========")
        android.util.Log.d("EditCustomerViewModel", "[STEP 1] Loading customer with UID: '$uid'")
        
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                android.util.Log.d("EditCustomerViewModel", "[STEP 2] Starting getUserProfile flow collection...")
                
                userRepository.getUserProfile(uid)
                    .catch { e ->
                        android.util.Log.e("EditCustomerViewModel", "[STEP 3] Flow catch block triggered")
                        android.util.Log.e("EditCustomerViewModel", "[STEP 3.1] Exception type: ${e::class.simpleName}")
                        android.util.Log.e("EditCustomerViewModel", "[STEP 3.2] Exception message: ${e.message}")
                        
                        // Check if it's a cancellation exception (AbortFlowException is internal)
                        if (e is kotlinx.coroutines.CancellationException) {
                            android.util.Log.d("EditCustomerViewModel", "[STEP 3.3] CancellationException (normal flow completion), ignoring")
                            throw e // Re-throw cancellation exceptions
                        } else {
                            android.util.Log.e("EditCustomerViewModel", "[STEP 3.4] Real exception, emitting Error state")
                            emit(ResultState.Error(e))
                        }
                    }
                    .collect { state ->
                        android.util.Log.d("EditCustomerViewModel", "[STEP 4] Received state in collect")
                        android.util.Log.d("EditCustomerViewModel", "[STEP 4.1] State type: ${state::class.simpleName}")
                        
                        when (state) {
                            is ResultState.Loading -> {
                                android.util.Log.d("EditCustomerViewModel", "[STEP 4.2] State is Loading, keeping loading state")
                                // Keep loading, continue collecting
                            }
                            is ResultState.Success -> {
                                android.util.Log.d("EditCustomerViewModel", "[STEP 4.3] State is Success!")
                                android.util.Log.d("EditCustomerViewModel", "[STEP 4.4] Customer data: ${state.data}")
                                val customer = state.data
                                setState {
                                    copy(
                                        isLoading = false,
                                        customer = customer,
                                        fullName = customer?.fullName ?: "",
                                        email = customer?.email ?: "",
                                        phoneNumber = customer?.phoneNumber ?: ""
                                    )
                                }
                                android.util.Log.d("EditCustomerViewModel", "[STEP 4.5] State updated successfully")
                            }
                            is ResultState.Error -> {
                                android.util.Log.e("EditCustomerViewModel", "[STEP 4.6] State is Error!")
                                android.util.Log.e("EditCustomerViewModel", "[STEP 4.7] Error message: ${state.throwable.message}")
                                setState {
                                    copy(
                                        isLoading = false,
                                        error = state.throwable.message ?: "Không thể tải thông tin khách hàng"
                                    )
                                }
                            }
                        }
                    }
                
                android.util.Log.d("EditCustomerViewModel", "[STEP 5] Flow collection completed")
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("EditCustomerViewModel", "[STEP 5.1] CancellationException caught (normal flow completion)")
                // This is expected when flow completes, ignore it
            } catch (e: Exception) {
                android.util.Log.e("EditCustomerViewModel", "[STEP 5.2] Exception in flow collection")
                android.util.Log.e("EditCustomerViewModel", "[STEP 5.3] Exception: ${e.message}", e)
                setState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Không thể tải thông tin khách hàng"
                    )
                }
            }
        }
    }

    private fun saveCustomer() {
        val current = uiState.value
        val customer = current.customer ?: return

        if (current.fullName.isBlank()) {
            setState { copy(error = "Tên không được để trống") }
            return
        }

        setState { copy(isLoading = true, error = null, success = false) }
        viewModelScope.launch {
            val updatedCustomer = customer.copy(
                fullName = current.fullName.trim(),
                email = current.email.trim(),
                phoneNumber = current.phoneNumber.trim()
            )
            when (val result = updateUserUseCase(updatedCustomer)) {
                is ResultState.Error -> {
                    setState {
                        copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Lỗi khi cập nhật thông tin"
                        )
                    }
                }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            success = true,
                            customer = updatedCustomer
                        )
                    }
                }
            }
        }
    }
}

