package com.example.tdtumobilebanking.presentation.utilities

import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.repository.AuthRepository
import com.example.tdtumobilebanking.domain.repository.TransactionRepository
import com.example.tdtumobilebanking.domain.usecase.account.GetAccountsUseCase
import com.example.tdtumobilebanking.domain.usecase.transaction.GenerateOtpUseCase
import com.example.tdtumobilebanking.domain.usecase.utilities.CreateUtilityTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UtilitiesUiState(
    val selectedAccountId: String = "",
    val accountNumber: String = "",
    val currentBalance: Double = 0.0,
    val utilityType: String = "",
    val amount: String = "",
    val provider: String = "",
    val customerCode: String = "",
    val phoneNumber: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val availableAccounts: List<com.example.tdtumobilebanking.domain.model.Account> = emptyList(),
    val showAccountSelector: Boolean = false,
    val generatedOtp: String? = null,
    val enteredOtp: String = "",
    val otpCountdown: Int = 20,
    val otpExpired: Boolean = false
)

sealed class UtilitiesEvent {
    data class SelectAccount(val accountId: String) : UtilitiesEvent()
    data class ShowAccountSelector(val show: Boolean) : UtilitiesEvent()
    data class AmountChanged(val value: String) : UtilitiesEvent()
    data class ProviderChanged(val value: String) : UtilitiesEvent()
    data class CustomerCodeChanged(val value: String) : UtilitiesEvent()
    data class PhoneNumberChanged(val value: String) : UtilitiesEvent()
    data class DescriptionChanged(val value: String) : UtilitiesEvent()
    data class OtpChanged(val value: String) : UtilitiesEvent()
    data object RequestOtp : UtilitiesEvent()
    data object Submit : UtilitiesEvent()
    data object Confirm : UtilitiesEvent()
    data object Reset : UtilitiesEvent()
}

@HiltViewModel
class UtilitiesViewModel @Inject constructor(
    private val createUtilityTransactionUseCase: CreateUtilityTransactionUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val generateOtpUseCase: GenerateOtpUseCase,
    private val authRepository: AuthRepository
) : BaseViewModel<UtilitiesUiState>(UtilitiesUiState()) {

    fun initialize(utilityType: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("UtilitiesVM", "Initializing for utilityType=$utilityType")
                val currentUser = authRepository.currentUser().first()
                currentUser?.let { user ->
                    val accountsResult = getAccountsUseCase(user.uid).first { 
                        it is ResultState.Success || it is ResultState.Error 
                    }
                    when (accountsResult) {
                        is ResultState.Success -> {
                            val accounts = accountsResult.data ?: emptyList()
                            val checkingAccounts = accounts.filter { 
                                it.accountType.name == "CHECKING" 
                            }
                            val account = checkingAccounts.firstOrNull() ?: accounts.firstOrNull()
                            
                            account?.let { acc ->
                                setState {
                                    copy(
                                        utilityType = utilityType,
                                        selectedAccountId = acc.accountId,
                                        accountNumber = acc.accountId,
                                        currentBalance = acc.balance,
                                        availableAccounts = checkingAccounts.ifEmpty { accounts },
                                        isLoading = false
                                    )
                                }
                            }
                        }
                        is ResultState.Error -> {
                            setState { 
                                copy(
                                    error = "Không thể tải thông tin tài khoản",
                                    isLoading = false
                                ) 
                            }
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("UtilitiesVM", "Exception in initialize", e)
                setState { copy(error = "Lỗi: ${e.message}", isLoading = false) }
            }
        }
    }

    fun onEvent(event: UtilitiesEvent) {
        when (event) {
            is UtilitiesEvent.SelectAccount -> {
                val account = uiState.value.availableAccounts.find { 
                    it.accountId == event.accountId 
                }
                account?.let { acc ->
                    setState {
                        copy(
                            selectedAccountId = acc.accountId,
                            accountNumber = acc.accountId,
                            currentBalance = acc.balance,
                            showAccountSelector = false
                        )
                    }
                }
            }
            is UtilitiesEvent.ShowAccountSelector -> {
                setState { copy(showAccountSelector = event.show) }
            }
            is UtilitiesEvent.AmountChanged -> setState { copy(amount = event.value) }
            is UtilitiesEvent.ProviderChanged -> setState { copy(provider = event.value) }
            is UtilitiesEvent.CustomerCodeChanged -> setState { copy(customerCode = event.value) }
            is UtilitiesEvent.PhoneNumberChanged -> setState { copy(phoneNumber = event.value) }
            is UtilitiesEvent.DescriptionChanged -> setState { copy(description = event.value) }
            is UtilitiesEvent.OtpChanged -> setState { copy(enteredOtp = event.value) }
            UtilitiesEvent.RequestOtp -> {
                generateOtp()
                startOtpCountdown()
            }
            UtilitiesEvent.Submit -> {
                // Navigate to OTP screen - generate OTP when submitting
                generateOtp()
                startOtpCountdown()
            }
            UtilitiesEvent.Confirm -> confirmTransaction()
            UtilitiesEvent.Reset -> resetState()
        }
    }
    
    private fun generateOtp() {
        val otp = generateOtpUseCase()
        android.util.Log.d("UtilitiesVM", "Generated OTP: $otp")
        setState {
            copy(
                generatedOtp = otp,
                error = null,
                success = false
            )
        }
    }
    
    private fun startOtpCountdown() {
        viewModelScope.launch {
            var remaining = 20
            setState { copy(otpCountdown = remaining, otpExpired = false) }
            while (remaining > 0) {
                delay(1000)
                remaining--
                setState { copy(otpCountdown = remaining) }
            }
            setState {
                copy(
                    otpExpired = true,
                    generatedOtp = null,
                    enteredOtp = "",
                    error = "OTP đã hết hạn. Vui lòng thử lại."
                )
            }
        }
    }
    
    fun autoFillOtp() {
        val otp = uiState.value.generatedOtp
        if (otp != null && !uiState.value.otpExpired) {
            setState { copy(enteredOtp = otp) }
        }
    }
    
    private fun confirmTransaction() {
        val current = uiState.value
        if (current.generatedOtp.isNullOrBlank()) {
            setState { copy(error = "Vui lòng yêu cầu OTP trước") }
            return
        }
        if (current.enteredOtp != current.generatedOtp) {
            setState { copy(error = "OTP không đúng") }
            return
        }
        
        proceedWithTransaction()
    }
    
    private fun proceedWithTransaction() {
        val current = uiState.value
        val amount = current.amount.replace(",", "").toDoubleOrNull()
        
        if (amount == null || amount <= 0) {
            setState { copy(error = "Số tiền không hợp lệ") }
            return
        }
        
        if (amount > current.currentBalance) {
            setState { copy(error = "Số dư không đủ") }
            return
        }

        setState { copy(isLoading = true, error = null, success = false) }
        viewModelScope.launch {
            val result = createUtilityTransactionUseCase(
                accountId = current.selectedAccountId,
                utilityType = current.utilityType,
                amount = amount,
                provider = current.provider,
                customerCode = current.customerCode,
                phoneNumber = current.phoneNumber,
                description = current.description.ifBlank { 
                    getDefaultDescription(current.utilityType) 
                }
            )
            
            when (result) {
                is ResultState.Error -> {
                    setState {
                        copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Có lỗi xảy ra"
                        )
                    }
                }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success -> {
                    setState { copy(isLoading = false, success = true) }
                }
            }
        }
    }


    private fun getDefaultDescription(utilityType: String): String {
        return when (utilityType) {
            "ELECTRIC_BILL" -> "Thanh toán hóa đơn điện"
            "WATER_BILL" -> "Thanh toán hóa đơn nước"
            "PHONE_TOPUP" -> "Nạp tiền điện thoại"
            "FLIGHT_TICKET" -> "Mua vé máy bay"
            "MOVIE_TICKET" -> "Mua vé xem phim"
            "HOTEL_BOOKING" -> "Đặt phòng khách sạn"
            "E_COMMERCE" -> "Thanh toán thương mại điện tử"
            else -> "Thanh toán tiện ích"
        }
    }

    private fun resetState() {
        setState {
            copy(
                amount = "",
                provider = "",
                customerCode = "",
                phoneNumber = "",
                description = "",
                error = null,
                success = false,
                generatedOtp = null,
                enteredOtp = "",
                otpCountdown = 20,
                otpExpired = false
            )
        }
    }
}

