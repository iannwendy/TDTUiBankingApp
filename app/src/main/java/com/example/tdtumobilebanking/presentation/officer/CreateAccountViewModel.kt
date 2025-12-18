package com.example.tdtumobilebanking.presentation.officer

import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.AccountType
import com.example.tdtumobilebanking.domain.repository.UserRepository
import com.example.tdtumobilebanking.domain.usecase.account.CreateAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateAccountUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val ownerId: String = "",
    val accountId: String = "",
    val accountType: AccountType = AccountType.CHECKING,
    val balance: String = "",
    val currency: String = "VND",
    val interestRate: String = "",
    val termMonth: String = "",
    val customers: List<com.example.tdtumobilebanking.domain.model.User> = emptyList(),
    val selectedCustomer: com.example.tdtumobilebanking.domain.model.User? = null,
    val customerSearchQuery: String = "",
    val isCustomerDropdownExpanded: Boolean = false
)

sealed class CreateAccountEvent {
    data class OwnerIdChanged(val value: String) : CreateAccountEvent()
    data class AccountIdChanged(val value: String) : CreateAccountEvent()
    data class AccountTypeChanged(val value: AccountType) : CreateAccountEvent()
    data class BalanceChanged(val value: String) : CreateAccountEvent()
    data class CurrencyChanged(val value: String) : CreateAccountEvent()
    data class InterestRateChanged(val value: String) : CreateAccountEvent()
    data class TermMonthChanged(val value: String) : CreateAccountEvent()
    data class CustomerSelected(val customer: com.example.tdtumobilebanking.domain.model.User) : CreateAccountEvent()
    data class CustomerSearchQueryChanged(val query: String) : CreateAccountEvent()
    data class CustomerDropdownExpandedChanged(val expanded: Boolean) : CreateAccountEvent()
    data object LoadCustomers : CreateAccountEvent()
    data object CreateAccount : CreateAccountEvent()
}

@HiltViewModel
class CreateAccountViewModel @Inject constructor(
    private val createAccountUseCase: CreateAccountUseCase,
    private val userRepository: UserRepository
) : BaseViewModel<CreateAccountUiState>(CreateAccountUiState()) {

    fun onEvent(event: CreateAccountEvent) {
        when (event) {
            is CreateAccountEvent.OwnerIdChanged -> setState { copy(ownerId = event.value) }
            is CreateAccountEvent.AccountIdChanged -> setState { copy(accountId = event.value) }
            is CreateAccountEvent.AccountTypeChanged -> setState { copy(accountType = event.value) }
            is CreateAccountEvent.BalanceChanged -> setState { copy(balance = event.value) }
            is CreateAccountEvent.CurrencyChanged -> setState { copy(currency = event.value) }
            is CreateAccountEvent.InterestRateChanged -> setState { copy(interestRate = event.value) }
            is CreateAccountEvent.TermMonthChanged -> setState { copy(termMonth = event.value) }
            is CreateAccountEvent.CustomerSelected -> {
                setState {
                    copy(
                        selectedCustomer = event.customer,
                        ownerId = event.customer.uid,
                        customerSearchQuery = "${event.customer.fullName} - ${event.customer.email}",
                        isCustomerDropdownExpanded = false
                    )
                }
            }
            is CreateAccountEvent.CustomerSearchQueryChanged -> {
                setState { 
                    copy(
                        customerSearchQuery = event.query,
                        isCustomerDropdownExpanded = event.query.isNotEmpty()
                    )
                }
            }
            is CreateAccountEvent.CustomerDropdownExpandedChanged -> {
                setState { copy(isCustomerDropdownExpanded = event.expanded) }
            }
            CreateAccountEvent.LoadCustomers -> loadCustomers()
            CreateAccountEvent.CreateAccount -> createAccount()
        }
    }

    private fun loadCustomers() {
        android.util.Log.d("CreateAccountViewModel", "========== [START] loadCustomers() ==========")
        
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                android.util.Log.d("CreateAccountViewModel", "[STEP 1] Starting getAllCustomers flow collection...")
                
                userRepository.getAllCustomers()
                    .catch { e ->
                        android.util.Log.e("CreateAccountViewModel", "[STEP 2] Flow catch block triggered")
                        android.util.Log.e("CreateAccountViewModel", "[STEP 2.1] Exception type: ${e::class.simpleName}")
                        android.util.Log.e("CreateAccountViewModel", "[STEP 2.2] Exception message: ${e.message}")
                        
                        // Check if it's a cancellation exception
                        if (e is kotlinx.coroutines.CancellationException) {
                            android.util.Log.d("CreateAccountViewModel", "[STEP 2.3] CancellationException (normal flow completion), ignoring")
                            throw e // Re-throw cancellation exceptions
                        } else {
                            android.util.Log.e("CreateAccountViewModel", "[STEP 2.4] Real exception, emitting Error state")
                            emit(ResultState.Error(e))
                        }
                    }
                    .collect { state ->
                        android.util.Log.d("CreateAccountViewModel", "[STEP 3] Received state in collect")
                        android.util.Log.d("CreateAccountViewModel", "[STEP 3.1] State type: ${state::class.simpleName}")
                        
                        when (state) {
                            is ResultState.Loading -> {
                                android.util.Log.d("CreateAccountViewModel", "[STEP 3.2] State is Loading, keeping loading state")
                                // Keep loading, continue collecting
                            }
                            is ResultState.Success -> {
                                android.util.Log.d("CreateAccountViewModel", "[STEP 3.3] State is Success!")
                                android.util.Log.d("CreateAccountViewModel", "[STEP 3.4] Customers count: ${state.data.size}")
                                setState {
                                    copy(
                                        isLoading = false,
                                        customers = state.data
                                    )
                                }
                                android.util.Log.d("CreateAccountViewModel", "[STEP 3.5] State updated successfully")
                            }
                            is ResultState.Error -> {
                                android.util.Log.e("CreateAccountViewModel", "[STEP 3.6] State is Error!")
                                android.util.Log.e("CreateAccountViewModel", "[STEP 3.7] Error message: ${state.throwable.message}")
                                setState {
                                    copy(
                                        isLoading = false,
                                        error = state.throwable.message ?: "Không thể tải danh sách khách hàng"
                                    )
                                }
                            }
                        }
                    }
                
                android.util.Log.d("CreateAccountViewModel", "[STEP 4] Flow collection completed")
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("CreateAccountViewModel", "[STEP 4.1] CancellationException caught (normal flow completion)")
                // This is expected when flow completes, ignore it
            } catch (e: Exception) {
                android.util.Log.e("CreateAccountViewModel", "[STEP 4.2] Exception in flow collection")
                android.util.Log.e("CreateAccountViewModel", "[STEP 4.3] Exception: ${e.message}", e)
                setState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Không thể tải danh sách khách hàng"
                    )
                }
            }
        }
    }

    private fun createAccount() {
        val current = uiState.value
        
        if (current.ownerId.isBlank()) {
            setState { copy(error = "Vui lòng chọn khách hàng") }
            return
        }
        if (current.accountId.isBlank()) {
            setState { copy(error = "Vui lòng nhập số tài khoản") }
            return
        }
        
        val balance = current.balance.replace(",", "").toDoubleOrNull() ?: 0.0
        
        setState { copy(isLoading = true, error = null, success = false) }
        viewModelScope.launch {
            val account = com.example.tdtumobilebanking.domain.model.Account(
                accountId = current.accountId.trim(),
                ownerId = current.ownerId.trim(),
                accountType = current.accountType,
                balance = balance,
                currency = current.currency,
                interestRate = current.interestRate.toDoubleOrNull(),
                termMonth = current.termMonth.toIntOrNull(),
                termMonths = current.termMonth.toIntOrNull()
            )
            
            when (val result = createAccountUseCase(account)) {
                is ResultState.Error -> {
                    setState {
                        copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Lỗi khi tạo tài khoản"
                        )
                    }
                }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success<*> -> {
                    setState {
                        copy(
                            isLoading = false,
                            success = true,
                            accountId = "",
                            balance = "",
                            interestRate = "",
                            termMonth = ""
                        )
                    }
                }
            }
        }
    }
}

