package com.example.tdtumobilebanking.presentation.officer

import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.AccountType
import com.example.tdtumobilebanking.domain.usecase.account.UpdateInterestRateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdateInterestRateUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val accountType: AccountType = AccountType.SAVING,
    val interestRate: String = "",
    val updatedAccountsCount: Int = 0
)

sealed class UpdateInterestRateEvent {
    data class AccountTypeChanged(val accountType: AccountType) : UpdateInterestRateEvent()
    data class InterestRateChanged(val value: String) : UpdateInterestRateEvent()
    data object UpdateInterestRate : UpdateInterestRateEvent()
    data object ResetSuccess : UpdateInterestRateEvent()
}

@HiltViewModel
class UpdateInterestRateViewModel @Inject constructor(
    private val updateInterestRateUseCase: UpdateInterestRateUseCase
) : BaseViewModel<UpdateInterestRateUiState>(UpdateInterestRateUiState()) {

    fun onEvent(event: UpdateInterestRateEvent) {
        when (event) {
            is UpdateInterestRateEvent.AccountTypeChanged -> {
                setState { copy(accountType = event.accountType) }
            }
            is UpdateInterestRateEvent.InterestRateChanged -> {
                setState { copy(interestRate = event.value) }
            }
            UpdateInterestRateEvent.UpdateInterestRate -> {
                updateInterestRate()
            }
            UpdateInterestRateEvent.ResetSuccess -> {
                setState { copy(success = false, updatedAccountsCount = 0) }
            }
        }
    }

    private fun updateInterestRate() {
        val rateValue = uiState.value.interestRate.trim()
        if (rateValue.isBlank()) {
            setState { copy(error = "Vui lòng nhập lãi suất") }
            return
        }

        val rate = rateValue.toDoubleOrNull()
        if (rate == null) {
            setState { copy(error = "Lãi suất không hợp lệ") }
            return
        }

        if (rate < 0 || rate > 100) {
            setState { copy(error = "Lãi suất phải từ 0% đến 100%") }
            return
        }

        setState { copy(isLoading = true, error = null, success = false) }
        viewModelScope.launch {
            when (val result = updateInterestRateUseCase(uiState.value.accountType, rate)) {
                is ResultState.Success -> {
                    setState {
                        copy(
                            isLoading = false,
                            success = true,
                            updatedAccountsCount = result.data,
                            error = null
                        )
                    }
                }
                is ResultState.Error -> {
                    setState {
                        copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Có lỗi xảy ra khi cập nhật lãi suất"
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

