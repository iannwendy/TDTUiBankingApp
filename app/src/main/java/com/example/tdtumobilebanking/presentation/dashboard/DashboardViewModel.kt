package com.example.tdtumobilebanking.presentation.dashboard

import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.Account
import com.example.tdtumobilebanking.domain.model.User
import com.example.tdtumobilebanking.domain.model.Transaction
import com.example.tdtumobilebanking.domain.repository.UserRepository
import com.example.tdtumobilebanking.domain.repository.AuthRepository
import com.example.tdtumobilebanking.domain.repository.TransactionRepository
import com.example.tdtumobilebanking.domain.usecase.account.CalculateMortgagePaymentUseCase
import com.example.tdtumobilebanking.domain.usecase.account.CalculateSavingMonthlyProfitUseCase
import com.example.tdtumobilebanking.domain.usecase.account.GetAccountsUseCase
import com.example.tdtumobilebanking.domain.usecase.account.ImportSampleAccountsUseCase
import com.example.tdtumobilebanking.domain.usecase.account.ImportAccountsFromUriUseCase
import com.example.tdtumobilebanking.domain.usecase.utilities.ImportBillsFromCsvUseCase
import com.example.tdtumobilebanking.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val accounts: List<Account> = emptyList(),
    val customers: List<User> = emptyList(),
    val monthlyProfitPreview: Map<String, Double> = emptyMap(),
    val mortgagePreview: Map<String, Double> = emptyMap(),
    val user: User? = null,
    val transactions: List<Transaction> = emptyList(),
    val isTransactionsLoading: Boolean = false,
    val selectedAccountId: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val calculateSavingMonthlyProfitUseCase: CalculateSavingMonthlyProfitUseCase,
    private val calculateMortgagePaymentUseCase: CalculateMortgagePaymentUseCase,
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val logoutUseCase: LogoutUseCase,
    private val importSampleAccountsUseCase: ImportSampleAccountsUseCase,
    private val importAccountsFromUriUseCase: ImportAccountsFromUriUseCase,
    private val importBillsFromCsvUseCase: ImportBillsFromCsvUseCase
) : BaseViewModel<DashboardUiState>(DashboardUiState()) {

    fun loadForCurrentUser(ownerId: String = "") {
        Log.d("DashboardVM", "loadForCurrentUser() start, ownerId param='$ownerId'")
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val currentUid = ownerId.ifBlank { authRepository.currentUser().first()?.uid.orEmpty() }
            Log.d("DashboardVM", "Resolved currentUid='$currentUid'")
            if (currentUid.isBlank()) {
                Log.e("DashboardVM", "currentUid blank, abort")
                setState { copy(isLoading = false, error = "Không tìm thấy thông tin người dùng") }
                return@launch
            }

            val profileResult = userRepository.getUserProfile(currentUid).first { it !is ResultState.Loading }
            Log.d("DashboardVM", "Profile result type=${profileResult::class.simpleName}")
            val profile = when (profileResult) {
                is ResultState.Success -> profileResult.data
                is ResultState.Error -> {
                    Log.e("DashboardVM", "Profile error: ${profileResult.throwable.message}")
                    setState { copy(isLoading = false, error = profileResult.throwable.message) }
                    return@launch
                }
                else -> null
            }

            when (val accountResult = getAccountsUseCase(currentUid).first { it !is ResultState.Loading }) {
                is ResultState.Error -> {
                    Log.e("DashboardVM", "Accounts error: ${accountResult.throwable.message}")
                    setState { copy(isLoading = false, error = accountResult.throwable.message) }
                }
                    is ResultState.Success -> {
                    val accounts = accountResult.data
                    Log.d("DashboardVM", "Accounts loaded: ${accounts.size}")
                    val primaryAccountId = accounts.firstOrNull()?.accountId
                        setState {
                            copy(
                                isLoading = false,
                                accounts = accounts,
                            user = profile,
                            selectedAccountId = primaryAccountId,
                                monthlyProfitPreview = accounts.associate { it.accountId to calculateSavingMonthlyProfitUseCase(it) },
                                mortgagePreview = accounts.associate { it.accountId to calculateMortgagePaymentUseCase(it) }
                            )
                        }
                    primaryAccountId?.let {
                        Log.d("DashboardVM", "Loading transactions for primaryAccountId=$it")
                        loadTransactionsForAccount(it)
                    }
                }
                ResultState.Loading -> setState { copy(isLoading = true) }
            }
        }
    }

    fun loadTransactionsForAccount(accountId: String) {
        viewModelScope.launch {
            Log.d("DashboardVM", "loadTransactionsForAccount($accountId) start")
            setState { copy(isTransactionsLoading = true, selectedAccountId = accountId) }
            when (val result = transactionRepository.getTransactionsForAccount(accountId).first { it !is ResultState.Loading }) {
                is ResultState.Success -> {
                    Log.d("DashboardVM", "Transactions loaded count=${result.data.size} for account=$accountId")
                    setState { copy(isTransactionsLoading = false, transactions = result.data) }
                }
                is ResultState.Error -> {
                    Log.e("DashboardVM", "Transactions error: ${result.throwable.message}")
                    setState { copy(isTransactionsLoading = false, error = result.throwable.message) }
                }
                else -> {
                    Log.w("DashboardVM", "Transactions unexpected state for account=$accountId")
                    setState { copy(isTransactionsLoading = false) }
                }
            }
        }
    }

    fun loadAllCustomers() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            userRepository.getAllCustomers().collectLatest { result ->
                when (result) {
                    is ResultState.Error -> setState { copy(isLoading = false, error = result.throwable.message) }
                    ResultState.Loading -> setState { copy(isLoading = true) }
                    is ResultState.Success -> setState { copy(isLoading = false, customers = result.data) }
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = logoutUseCase()) {
                is ResultState.Error -> setState { copy(isLoading = false, error = result.throwable.message) }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success -> {
                    setState { copy(isLoading = false) }
                    onLoggedOut()
                }
            }
        }
    }

    fun importSampleAccounts() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = importSampleAccountsUseCase()) {
                is ResultState.Error -> setState { copy(isLoading = false, error = result.throwable.message) }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success -> setState { copy(isLoading = false) }
            }
        }
    }

    fun importAccountsFromUri(uri: android.net.Uri) {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = importAccountsFromUriUseCase(uri)) {
                is ResultState.Error -> setState { copy(isLoading = false, error = result.throwable.message) }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success -> setState { copy(isLoading = false) }
            }
        }
    }

    fun importBillsFromCsv() {
        setState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = importBillsFromCsvUseCase()) {
                is ResultState.Error -> setState { copy(isLoading = false, error = result.throwable.message) }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success -> setState { copy(isLoading = false) }
            }
        }
    }
}

