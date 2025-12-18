package com.example.tdtumobilebanking.presentation.transactions

import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.repository.AuthRepository
import com.example.tdtumobilebanking.domain.usecase.account.GetAccountsUseCase
import com.example.tdtumobilebanking.domain.usecase.transaction.DepositMoneyUseCase
import com.example.tdtumobilebanking.domain.usecase.transaction.GenerateOtpUseCase
import com.example.tdtumobilebanking.domain.usecase.transaction.WithdrawMoneyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DepositWithdrawType {
    DEPOSIT, // Nạp tiền
    WITHDRAW // Rút tiền
}

data class DepositWithdrawUiState(
    val type: DepositWithdrawType = DepositWithdrawType.DEPOSIT,
    val accountId: String = "",
    val accountNumber: String = "",
    val currentBalance: Double = 0.0,
    val amount: String = "",
    val description: String = "",
    val generatedOtp: String? = null,
    val enteredOtp: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val otpCountdown: Int = 20,
    val otpExpired: Boolean = false,
    val availableAccounts: List<com.example.tdtumobilebanking.domain.model.Account> = emptyList(),
    val showAccountSelector: Boolean = false
)

sealed class DepositWithdrawEvent {
    data class AmountChanged(val value: String) : DepositWithdrawEvent()
    data class DescriptionChanged(val value: String) : DepositWithdrawEvent()
    data class OtpChanged(val value: String) : DepositWithdrawEvent()
    data object RequestOtp : DepositWithdrawEvent()
    data object Confirm : DepositWithdrawEvent()
    data object Reset : DepositWithdrawEvent()
    data object ShowAccountSelector : DepositWithdrawEvent()
    data object HideAccountSelector : DepositWithdrawEvent()
    data class SelectAccount(val accountId: String) : DepositWithdrawEvent()
}

@HiltViewModel
class DepositWithdrawViewModel @Inject constructor(
    private val depositMoneyUseCase: DepositMoneyUseCase,
    private val withdrawMoneyUseCase: WithdrawMoneyUseCase,
    private val generateOtpUseCase: GenerateOtpUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val authRepository: AuthRepository
) : BaseViewModel<DepositWithdrawUiState>(DepositWithdrawUiState()) {

    fun initialize(type: DepositWithdrawType, accountId: String? = null) {
        viewModelScope.launch {
            try {
                android.util.Log.d("DepositWithdrawVM", "=== initialize() called: type=$type, accountId=$accountId ===")
                val currentUser = authRepository.currentUser().first()
                if (currentUser == null) {
                    android.util.Log.e("DepositWithdrawVM", "No current user found")
                    setState { copy(error = "Không tìm thấy người dùng") }
                    return@launch
                }
                
                android.util.Log.d("DepositWithdrawVM", "Current user UID: ${currentUser.uid}")
                
                // Use first() with condition to skip Loading states
                // This will wait until we get Success or Error
                val accountsResult = try {
                    android.util.Log.d("DepositWithdrawVM", "Waiting for accounts result (skipping Loading states)...")
                    getAccountsUseCase(currentUser.uid).first { result ->
                        val isFinal = result is ResultState.Success || result is ResultState.Error
                        if (!isFinal && result is ResultState.Loading) {
                            android.util.Log.d("DepositWithdrawVM", "Skipping Loading state, waiting for final result...")
                            setState { copy(isLoading = true) }
                        }
                        isFinal
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    android.util.Log.e("DepositWithdrawVM", "Timeout waiting for accounts", e)
                    setState { copy(error = "Timeout khi tải tài khoản", isLoading = false) }
                    return@launch
                } catch (e: Exception) {
                    android.util.Log.e("DepositWithdrawVM", "Error getting accounts", e)
                    setState { copy(error = "Lỗi khi tải tài khoản: ${e.message}", isLoading = false) }
                    return@launch
                }
                
                android.util.Log.d("DepositWithdrawVM", "Final accounts result: ${accountsResult.javaClass.simpleName}")
                
                when (accountsResult) {
                    is ResultState.Success -> {
                        val accounts = accountsResult.data ?: emptyList()
                        android.util.Log.d("DepositWithdrawVM", "Found ${accounts.size} total accounts")
                        accounts.forEachIndexed { index, acc ->
                            android.util.Log.d("DepositWithdrawVM", "  Account[$index]: id=${acc.accountId}, balance=${acc.balance}, type=${acc.accountType.name}, owner=${acc.ownerId}")
                        }
                        
                        // Filter checking accounts
                        val checkingAccounts = accounts.filter { it.accountType.name == "CHECKING" }
                        android.util.Log.d("DepositWithdrawVM", "Found ${checkingAccounts.size} checking accounts")
                        checkingAccounts.forEachIndexed { index, acc ->
                            android.util.Log.d("DepositWithdrawVM", "  Checking[$index]: id=${acc.accountId}, balance=${acc.balance}")
                        }
                        
                        // Store all available accounts (prefer checking, fallback to all)
                        val availableAccounts = checkingAccounts.ifEmpty { accounts }
                        android.util.Log.d("DepositWithdrawVM", "Storing ${availableAccounts.size} available accounts")
                        setState { copy(availableAccounts = availableAccounts) }
                        
                        // Select account: use provided accountId if valid, otherwise use first checking, otherwise first any
                        val selectedAccount = when {
                            !accountId.isNullOrBlank() -> {
                                android.util.Log.d("DepositWithdrawVM", "Looking for account with id: $accountId")
                                accounts.find { it.accountId == accountId }?.also {
                                    android.util.Log.d("DepositWithdrawVM", "Found account by id: ${it.accountId}")
                                }
                            }
                            checkingAccounts.isNotEmpty() -> {
                                android.util.Log.d("DepositWithdrawVM", "Using first checking account: ${checkingAccounts.first().accountId}")
                                checkingAccounts.first()
                            }
                            accounts.isNotEmpty() -> {
                                android.util.Log.d("DepositWithdrawVM", "Using first available account: ${accounts.first().accountId}")
                                accounts.first()
                            }
                            else -> null
                        }

                        if (selectedAccount != null) {
                            android.util.Log.d("DepositWithdrawVM", "=== Setting state: accountId=${selectedAccount.accountId}, accountNumber=${selectedAccount.accountId}, balance=${selectedAccount.balance} ===")
                            setState {
                                copy(
                                    type = type,
                                    accountId = selectedAccount.accountId,
                                    accountNumber = selectedAccount.accountId,
                                    currentBalance = selectedAccount.balance,
                                    description = if (type == DepositWithdrawType.DEPOSIT) "Nạp tiền" else "Rút tiền",
                                    showAccountSelector = false,
                                    error = null, // Clear any previous errors
                                    isLoading = false // Ensure loading is false after successful load
                                )
                            }
                            android.util.Log.d("DepositWithdrawVM", "State updated successfully, isLoading=false")
                        } else {
                            android.util.Log.w("DepositWithdrawVM", "No account found to select")
                            setState { copy(error = "Không tìm thấy tài khoản", isLoading = false) }
                        }
                    }
                    is ResultState.Error -> {
                        android.util.Log.e("DepositWithdrawVM", "Error loading accounts: ${accountsResult.throwable.message}", accountsResult.throwable)
                        setState { copy(error = "Không thể tải thông tin tài khoản: ${accountsResult.throwable.message}") }
                    }
                    ResultState.Loading -> {
                        android.util.Log.d("DepositWithdrawVM", "Accounts still loading...")
                        setState { copy(isLoading = true) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DepositWithdrawVM", "Exception in initialize", e)
                setState { copy(error = "Lỗi: ${e.message}") }
            }
        }
    }

    fun onEvent(event: DepositWithdrawEvent) {
        when (event) {
            is DepositWithdrawEvent.AmountChanged -> setState { copy(amount = event.value) }
            is DepositWithdrawEvent.DescriptionChanged -> setState { copy(description = event.value) }
            is DepositWithdrawEvent.OtpChanged -> setState { copy(enteredOtp = event.value) }
            DepositWithdrawEvent.RequestOtp -> generateOtp()
            DepositWithdrawEvent.Confirm -> confirmTransaction()
            DepositWithdrawEvent.Reset -> resetForNewTransaction()
            DepositWithdrawEvent.ShowAccountSelector -> {
                android.util.Log.d("DepositWithdrawVM", "ShowAccountSelector event, availableAccounts=${uiState.value.availableAccounts.size}")
                setState { copy(showAccountSelector = true) }
                android.util.Log.d("DepositWithdrawVM", "After setting showAccountSelector=true")
            }
            DepositWithdrawEvent.HideAccountSelector -> {
                android.util.Log.d("DepositWithdrawVM", "HideAccountSelector event")
                setState { copy(showAccountSelector = false) }
            }
            is DepositWithdrawEvent.SelectAccount -> {
                android.util.Log.d("DepositWithdrawVM", "SelectAccount event: accountId=${event.accountId}, availableAccounts=${uiState.value.availableAccounts.size}")
                val account = uiState.value.availableAccounts.find { it.accountId == event.accountId }
                account?.let { acc ->
                    android.util.Log.d("DepositWithdrawVM", "Account found: ${acc.accountId}, balance=${acc.balance}")
                    setState {
                        copy(
                            accountId = acc.accountId,
                            accountNumber = acc.accountId,
                            currentBalance = acc.balance,
                            showAccountSelector = false
                        )
                    }
                } ?: android.util.Log.w("DepositWithdrawVM", "Account not found: ${event.accountId}")
            }
        }
    }

    fun proceedToOtp(): Boolean {
        val current = uiState.value
        val amount = current.amount.replace(",", "").toDoubleOrNull()
        if (amount == null || amount <= 0) {
            setState { copy(error = "Số tiền không hợp lệ") }
            return false
        }
        if (current.type == DepositWithdrawType.WITHDRAW && amount > current.currentBalance) {
            setState { copy(error = "Số dư không đủ") }
            return false
        }
        generateOtp()
        startOtpCountdown()
        return true
    }

    private fun generateOtp() {
        val otp = generateOtpUseCase()
        android.util.Log.d("DepositWithdrawVM", "Generated OTP: $otp")
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
        val amount = current.amount.replace(",", "").toDoubleOrNull()
        if (amount == null || amount <= 0) {
            setState { copy(error = "Số tiền không hợp lệ") }
            return
        }
        if (current.type == DepositWithdrawType.WITHDRAW && amount > current.currentBalance) {
            setState { copy(error = "Số dư không đủ") }
            return
        }

        setState { copy(isLoading = true, error = null, success = false) }
        viewModelScope.launch {
            android.util.Log.d("DepositWithdrawVM", "Confirming transaction: type=${current.type}, accountId=${current.accountId}, amount=$amount")
            val result = if (current.type == DepositWithdrawType.DEPOSIT) {
                depositMoneyUseCase(
                    accountId = current.accountId,
                    amount = amount,
                    description = current.description.ifBlank { "Nạp tiền" }
                )
            } else {
                withdrawMoneyUseCase(
                    accountId = current.accountId,
                    amount = amount,
                    description = current.description.ifBlank { "Rút tiền" }
                )
            }
            when (result) {
                is ResultState.Error -> {
                    android.util.Log.e("DepositWithdrawVM", "Transaction error: ${result.throwable.message}", result.throwable)
                    setState {
                        copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Có lỗi xảy ra"
                        )
                    }
                }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success -> {
                    android.util.Log.d("DepositWithdrawVM", "Transaction successful, setting success=true")
                    setState { copy(isLoading = false, success = true) }
                }
            }
        }
    }

    private fun resetForNewTransaction() {
        setState {
            copy(
                amount = "",
                description = if (type == DepositWithdrawType.DEPOSIT) "Nạp tiền" else "Rút tiền",
                generatedOtp = null,
                enteredOtp = "",
                error = null,
                success = false,
                otpCountdown = 20,
                otpExpired = false
            )
        }
    }
}

