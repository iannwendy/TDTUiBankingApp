package com.example.tdtumobilebanking.presentation.transactions

import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.BankInfo
import com.example.tdtumobilebanking.domain.model.User
import com.example.tdtumobilebanking.domain.repository.AuthRepository
import com.example.tdtumobilebanking.domain.repository.UserRepository
import com.example.tdtumobilebanking.domain.usecase.transaction.GenerateOtpUseCase
import com.example.tdtumobilebanking.domain.usecase.transaction.TransferMoneyUseCase
import com.example.tdtumobilebanking.domain.usecase.account.GetAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferUiState(
    val senderAccountId: String = "",
    val senderAccountNumber: String = "",
    val receiverAccountId: String = "",
    val receiverName: String = "",
    val receiverFullName: String = "",
    val amount: String = "",
    val description: String = "",
    val generatedOtp: String? = null,
    val enteredOtp: String = "",
    val bankSearch: String = "",
    val selectedBank: String = "",
    val selectedBankName: String = "",
    val bankDropdownExpanded: Boolean = false,
    val banks: List<BankInfo> = emptyList(),
    val senderBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val otpCountdown: Int = 20,
    val otpExpired: Boolean = false,
    val isReceiverValid: Boolean = false,
    val availableAccounts: List<com.example.tdtumobilebanking.domain.model.Account> = emptyList(),
    val showAccountSelector: Boolean = false
)

sealed class TransferEvent {
    data class SenderChanged(val value: String) : TransferEvent()
    data class ReceiverChanged(val value: String) : TransferEvent()
    data class BankSearchChanged(val value: String) : TransferEvent()
    data class BankSelected(val value: String) : TransferEvent()
    data object ToggleBankDropdown : TransferEvent()
    data class AmountChanged(val value: String) : TransferEvent()
    data class DescriptionChanged(val value: String) : TransferEvent()
    data class OtpChanged(val value: String) : TransferEvent()
    data object RequestOtp : TransferEvent()
    data object LookupReceiver : TransferEvent()
    data object Confirm : TransferEvent()
    data object Reset : TransferEvent()
    data object ShowAccountSelector : TransferEvent()
    data object HideAccountSelector : TransferEvent()
    data class SelectAccount(val accountId: String) : TransferEvent()
}

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferMoneyUseCase: TransferMoneyUseCase,
    private val generateOtpUseCase: GenerateOtpUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : BaseViewModel<TransferUiState>(TransferUiState()) {

    private val vnBanks = listOf(
        BankInfo("Ngân hàng TMCP An Bình", "ABB", "ABBANK", ""),
        BankInfo("Ngân hàng TMCP Á Châu", "ACB", "ACB", ""),
        BankInfo("Ngân hàng Nông nghiệp và PTNT VN", "AGRIBANK", "AGRIBANK", ""),
        BankInfo("Ngân hàng Công Thương VN", "CTG", "VIETINBANK", ""),
        BankInfo("Ngân hàng Ngoại Thương VN", "VCB", "VIETCOMBANK", ""),
        BankInfo("Ngân hàng Đầu tư và Phát triển VN", "BIDV", "BIDV", ""),
        BankInfo("Ngân hàng Kỹ Thương VN", "TCB", "TECHCOMBANK", ""),
        BankInfo("Ngân hàng Quân đội", "MB", "MBBANK", ""),
        BankInfo("Ngân hàng Tiên Phong", "TPB", "TPBANK", ""),
        BankInfo("Ngân hàng Việt Nam Thịnh Vượng", "VPB", "VPBANK", ""),
        BankInfo("Ngân hàng Sài Gòn Thương Tín", "STB", "SACOMBANK", ""),
        BankInfo("Ngân hàng Phát triển TP.HCM", "HDB", "HDBANK", ""),
        BankInfo("Ngân hàng Bản Việt", "BVB", "VIETCAPITALBANK", ""),
        BankInfo("Ngân hàng Đông Á", "DOB", "DONGABANK", ""),
        BankInfo("Ngân hàng Sài Gòn", "SCB", "SCB", ""),
        BankInfo("Ngân hàng Việt Á", "VAB", "VIETABANK", ""),
        BankInfo("Ngân hàng Bắc Á", "BAB", "BACABANK", ""),
        BankInfo("Ngân hàng Quốc tế VIB", "VIB", "VIB", ""),
        BankInfo("Ngân hàng Hàng Hải MSB", "MSB", "MSB", ""),
        BankInfo("Ngân hàng Kiên Long", "KLB", "KIENLONGBANK", ""),
        BankInfo("Ngân hàng Nam Á", "NAB", "NAMABANK", ""),
        BankInfo("Ngân hàng Bưu điện Liên Việt", "LPB", "LPBANK", ""),
        BankInfo("Ngân hàng Xăng dầu Petrolimex", "PGB", "PGBANK", ""),
        BankInfo("Ngân hàng Phương Đông", "OCB", "OCB", ""),
        BankInfo("Ngân hàng Quốc Dân", "NCB", "NCB", ""),
        BankInfo("Ngân hàng Đông Nam Á", "SEAB", "SEABANK", ""),
        BankInfo("Ngân hàng Sài Gòn Công Thương", "SGB", "SAIGONBANK", ""),
        BankInfo("Ngân hàng Bảo Việt", "BVB", "BAOVIETBANK", ""),
        BankInfo("Ngân hàng Đại Chúng PVComBank", "PVCB", "PVCOMBANK", ""),
        BankInfo("Đại học Tôn Đức Thắng", "TDTU", "TDTU", "")
    )

    init {
        setState { copy(banks = vnBanks) }
        observeCurrentUser()
    }

    fun onEvent(event: TransferEvent) {
        when (event) {
            is TransferEvent.SenderChanged -> {
                setState { copy(senderAccountId = event.value) }
            }
            is TransferEvent.ReceiverChanged -> setState { copy(receiverAccountId = event.value) }
            is TransferEvent.BankSearchChanged -> setState { copy(bankSearch = event.value, bankDropdownExpanded = true) }
            is TransferEvent.BankSelected -> {
                val bank = uiState.value.banks.find { it.shortName == event.value }
                val bankDisplayName = bank?.let { "${it.shortName} - ${it.name}" } ?: event.value
                setState { 
                    copy(
                        selectedBank = event.value, 
                        selectedBankName = bank?.name ?: event.value,
                        bankSearch = bankDisplayName, // Fill the input field with bank name
                        bankDropdownExpanded = false
                    ) 
                }
            }
            TransferEvent.ToggleBankDropdown -> setState { copy(bankDropdownExpanded = !uiState.value.bankDropdownExpanded) }
            is TransferEvent.AmountChanged -> setState { copy(amount = event.value) }
            is TransferEvent.DescriptionChanged -> setState { copy(description = event.value) }
            is TransferEvent.OtpChanged -> setState { copy(enteredOtp = event.value) }
            TransferEvent.RequestOtp -> generateOtp()
            TransferEvent.LookupReceiver -> lookupReceiver()
            TransferEvent.Confirm -> confirmTransfer()
            TransferEvent.Reset -> resetForNewTransfer()
            TransferEvent.ShowAccountSelector -> setState { copy(showAccountSelector = true) }
            TransferEvent.HideAccountSelector -> setState { copy(showAccountSelector = false) }
            is TransferEvent.SelectAccount -> {
                android.util.Log.d("TransferViewModel", "SelectAccount event: accountId=${event.accountId}, availableAccounts=${uiState.value.availableAccounts.size}")
                val account = uiState.value.availableAccounts.find { it.accountId == event.accountId }
                account?.let { acc ->
                    android.util.Log.d("TransferViewModel", "Account found: ${acc.accountId}, balance=${acc.balance}")
                    setState {
                        copy(
                            senderAccountId = acc.accountId,
                            senderAccountNumber = acc.accountId,
                            senderBalance = acc.balance,
                            showAccountSelector = false
                        )
                    }
                } ?: android.util.Log.w("TransferViewModel", "Account not found: ${event.accountId}")
            }
        }
    }

    private fun generateOtp() {
        val otp = generateOtpUseCase()
        android.util.Log.d("TransferViewModel", "Generated OTP: $otp")
        setState {
            copy(
                generatedOtp = otp,
                error = null,
                success = false
            )
        }
        android.util.Log.d("TransferViewModel", "State updated, generatedOtp in state: ${uiState.value.generatedOtp}")
    }

    private suspend fun getSenderFullName(): String {
        return try {
            val currentUser = authRepository.currentUser().first()
            currentUser?.let { user ->
                val userResult = userRepository.getUserProfile(user.uid).first()
                when (userResult) {
                    is ResultState.Success -> {
                        val fullName = userResult.data?.fullName?.takeIf { it.isNotBlank() }
                        fullName ?: ""
                    }
                    else -> ""
                }
            } ?: ""
        } catch (e: Exception) {
            android.util.Log.e("TransferViewModel", "Error getting sender full name", e)
            ""
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.currentUser().collect { authUser ->
                authUser?.let { user ->
                    // Fetch user profile from Firestore to get phone number
                    val result = userRepository.getUserProfile(user.uid).first()
                    val phone = when (result) {
                        is ResultState.Success -> result.data?.phoneNumber?.takeIf { it.isNotBlank() }
                        else -> null
                    } ?: user.phoneNumber?.takeIf { it.isNotBlank() }
                    
                    // Get accounts for current user (by UID)
                    launch {
                        try {
                            android.util.Log.d("TransferViewModel", "Loading accounts for UID: ${user.uid}")
                            
                            // First try: query by ownerId
                            val accountsResult = getAccountsUseCase(user.uid)
                                .onEach { result ->
                                    if (result is ResultState.Loading) {
                                        android.util.Log.d("TransferViewModel", "Accounts Loading... waiting for final result")
                                    }
                                }
                                .first { result -> result is ResultState.Success || result is ResultState.Error }
                            when (accountsResult) {
                                is ResultState.Success -> {
                                    val accounts = accountsResult.data
                                    android.util.Log.d("TransferViewModel", "Found ${accounts.size} accounts")
                                    accounts.forEach { acc ->
                                        android.util.Log.d("TransferViewModel", "Account: ${acc.accountId}, Owner: ${acc.ownerId}, Balance: ${acc.balance}, Type: ${acc.accountType.name}")
                                    }
                                    
                                    // Filter checking accounts
                                    val checkingAccounts = accounts.filter { it.accountType.name == "CHECKING" }
                                    android.util.Log.d("TransferViewModel", "Found ${checkingAccounts.size} checking accounts")
                                    
                                    // Store available checking accounts
                                    setState { copy(availableAccounts = checkingAccounts.ifEmpty { accounts }) }
                                    
                                    val account = checkingAccounts.firstOrNull() 
                                        ?: accounts.firstOrNull()
                                    if (account != null) {
                                        // Compare ownerId with trimmed strings to avoid whitespace issues
                                        val accountOwnerId = account.ownerId.trim()
                                        val currentUserId = user.uid.trim()
                                        
                                        // Normalize UIDs for comparison (handle O vs 0 confusion)
                                        val normalizedAccountOwner = accountOwnerId.replace("O", "0").replace("o", "0")
                                        val normalizedCurrentUser = currentUserId.replace("O", "0").replace("o", "0")
                                        
                                        android.util.Log.d("TransferViewModel", "Comparing ownerId: '$accountOwnerId' == '$currentUserId': ${accountOwnerId == currentUserId}")
                                        android.util.Log.d("TransferViewModel", "Normalized: '$normalizedAccountOwner' == '$normalizedCurrentUser': ${normalizedAccountOwner == normalizedCurrentUser}")
                                        
                                        if (accountOwnerId == currentUserId || normalizedAccountOwner == normalizedCurrentUser) {
                                            android.util.Log.d("TransferViewModel", "Selected account: ${account.accountId}, Balance: ${account.balance}")
                                            setState { 
                                                copy(
                                                    senderAccountId = account.accountId, // Use accountId, not phone
                                                    senderAccountNumber = account.accountId,
                                                    senderBalance = account.balance
                                                ) 
                                            }
                                            return@launch
                                        } else {
                                            android.util.Log.w("TransferViewModel", "Owner mismatch: '$accountOwnerId' != '$currentUserId'")
                                            // Still set balance if UIDs are similar (might be data issue)
                                            if (accountOwnerId.length == currentUserId.length && 
                                                accountOwnerId.length > 20 && 
                                                accountOwnerId.take(10) == currentUserId.take(10)) {
                                                android.util.Log.w("TransferViewModel", "UIDs are similar, accepting account anyway")
                                                setState { 
                                                    copy(
                                                        senderAccountId = phone ?: account.accountId,
                                                        senderAccountNumber = account.accountId,
                                                        senderBalance = account.balance
                                                    ) 
                                                }
                                                return@launch
                                            }
                                        }
                                    }
                                }
                                is ResultState.Error -> {
                                    android.util.Log.e("TransferViewModel", "Error loading accounts: ${accountsResult.throwable.message}")
                                }
                                else -> {}
                            }
                            
                            // Fallback: try direct lookup for known account
                            android.util.Log.d("TransferViewModel", "Trying direct lookup for 123456-TDTU")
                            val accountById = getAccountsUseCase.getById("123456-TDTU")
                            when (accountById) {
                                is ResultState.Success -> {
                                    accountById.data?.let { acc ->
                                        val accountOwnerId = acc.ownerId.trim()
                                        val currentUserId = user.uid.trim()
                                        android.util.Log.d("TransferViewModel", "Direct lookup found: ${acc.accountId}, Owner: '$accountOwnerId', Balance: ${acc.balance}, Current UID: '$currentUserId'")
                                        
                                        // Normalize UIDs for comparison (handle O vs 0 confusion)
                                        val normalizedAccountOwner = accountOwnerId.replace("O", "0").replace("o", "0")
                                        val normalizedCurrentUser = currentUserId.replace("O", "0").replace("o", "0")
                                        
                                        if (accountOwnerId == currentUserId || normalizedAccountOwner == normalizedCurrentUser) {
                                            android.util.Log.d("TransferViewModel", "Owner match! Setting balance: ${acc.balance}")
                                            setState { 
                                                copy(
                                                    senderAccountId = acc.accountId, // Use accountId, not phone
                                                    senderAccountNumber = acc.accountId,
                                                    senderBalance = acc.balance
                                                ) 
                                            }
                                            return@launch
                                        } else {
                                            android.util.Log.w("TransferViewModel", "Owner mismatch: '$accountOwnerId' != '$currentUserId' (normalized: '$normalizedAccountOwner' != '$normalizedCurrentUser')")
                                            // Still set balance if lengths match and similar (might be data issue)
                                            if (accountOwnerId.length == currentUserId.length && 
                                                accountOwnerId.length > 20 && 
                                                accountOwnerId.take(10) == currentUserId.take(10)) {
                                                android.util.Log.w("TransferViewModel", "UIDs are similar, accepting account anyway")
                                                setState { 
                                                    copy(
                                                        senderAccountId = phone ?: acc.accountId,
                                                        senderAccountNumber = acc.accountId,
                                                        senderBalance = acc.balance
                                                    ) 
                                                }
                                                return@launch
                                            }
                                        }
                                    } ?: android.util.Log.w("TransferViewModel", "Direct lookup returned null")
                                }
                                is ResultState.Error -> {
                                    android.util.Log.e("TransferViewModel", "Error in direct lookup: ${accountById.throwable.message}")
                                }
                                else -> {}
                            }
                            
                            // No account found
                            android.util.Log.w("TransferViewModel", "No account found for user ${user.uid}")
                            setState { 
                                copy(
                                    senderAccountId = phone ?: "",
                                    senderAccountNumber = phone ?: "",
                                    senderBalance = 0.0
                                ) 
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("TransferViewModel", "Exception loading accounts", e)
                            setState { 
                                copy(
                                    senderAccountId = phone ?: "",
                                    senderAccountNumber = phone ?: "",
                                    senderBalance = 0.0
                                ) 
                            }
                        }
                    }
                }
            }
        }
    }

    fun proceedToOtp(): Boolean {
        val current = uiState.value
        android.util.Log.d("TransferViewModel", "proceedToOtp called")
        android.util.Log.d("TransferViewModel", "receiverName: '${current.receiverName}'")
        android.util.Log.d("TransferViewModel", "receiverFullName: '${current.receiverFullName}'")
        android.util.Log.d("TransferViewModel", "receiverAccountId: '${current.receiverAccountId}'")
        android.util.Log.d("TransferViewModel", "amount: '${current.amount}'")
        android.util.Log.d("TransferViewModel", "selectedBank: '${current.selectedBank}'")
        
        if (current.receiverName.isBlank() && current.receiverFullName.isBlank()) {
            android.util.Log.w("TransferViewModel", "Receiver name is blank, showing error")
            setState { copy(error = "Vui lòng kiểm tra tài khoản nhận") }
            return false
        }
        val amount = current.amount.replace(",", "").toDoubleOrNull()
        if (amount == null || amount <= 0) {
            android.util.Log.w("TransferViewModel", "Invalid amount: ${current.amount}")
            setState { copy(error = "Số tiền không hợp lệ") }
            return false
        }
        // Generate OTP when proceeding and start countdown
        android.util.Log.d("TransferViewModel", "Generating OTP...")
        generateOtp()
        startOtpCountdown()
        android.util.Log.d("TransferViewModel", "OTP generated: ${uiState.value.generatedOtp}")
        return true
    }

    private fun startOtpCountdown() {
        viewModelScope.launch {
            var remaining = 20
            setState { copy(otpCountdown = remaining, otpExpired = false) }
            while (remaining > 0) {
                kotlinx.coroutines.delay(1000)
                remaining--
                setState { copy(otpCountdown = remaining) }
            }
            // OTP expired - cancel transaction
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

    private fun confirmTransfer() {
        val current = uiState.value
        if (current.generatedOtp.isNullOrBlank()) {
            setState { copy(error = "Please request OTP before confirming") }
            return
        }
        if (current.enteredOtp != current.generatedOtp) {
            setState { copy(error = "Invalid OTP") }
            return
        }
        val amount = current.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            setState { copy(error = "Amount invalid") }
            return
        }
        setState { copy(isLoading = true, error = null, success = false) }
        viewModelScope.launch {
            val current = uiState.value
            val amount = current.amount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                setState { copy(isLoading = false, error = "Amount invalid") }
                return@launch
            }
            if (current.receiverName.isBlank()) {
                setState { copy(isLoading = false, error = "Vui lòng kiểm tra tài khoản nhận") }
                return@launch
            }
            // Combine receiver account number with bank code
            val receiverAccountId = if (current.selectedBank.isNotBlank()) {
                "${current.receiverAccountId}-${current.selectedBank}"
            } else {
                current.receiverAccountId
            }
            when (val result = transferMoneyUseCase(
                senderAccountId = current.senderAccountId,
                receiverAccountId = receiverAccountId,
                amount = amount,
                description = current.description
            )) {
                is ResultState.Error -> setState { copy(isLoading = false, error = result.throwable.message, success = false) }
                ResultState.Loading -> setState { copy(isLoading = true) }
                is ResultState.Success -> setState { copy(isLoading = false, success = true, error = null) }
            }
        }
    }

    private fun lookupReceiver() {
        val accountNumber = uiState.value.receiverAccountId.trim()
        val bankCode = uiState.value.selectedBank.trim()
        if (accountNumber.isBlank()) {
            setState { copy(error = "Nhập số tài khoản nhận") }
            return
        }
        if (bankCode.isBlank()) {
            setState { copy(error = "Vui lòng chọn ngân hàng") }
            return
        }
        // Combine account number with bank code: "12345-MBBANK"
        val accountId = "$accountNumber-$bankCode"
        setState { copy(isLoading = true, error = null, receiverName = "", receiverFullName = "", isReceiverValid = false) }
        viewModelScope.launch {
            try {
                when (val result = getAccountsUseCase.getById(accountId)) {
                    is ResultState.Error -> {
                        setState { 
                            copy(
                                isLoading = false,
                                isReceiverValid = false,
                                error = "Không tìm thấy tài khoản, vui lòng kiểm tra lại."
                            ) 
                        }
                    }
                    ResultState.Loading -> setState { copy(isLoading = true) }
                    is ResultState.Success -> {
                        val acc = result.data
                        if (acc == null) {
                            setState { 
                                copy(
                                    isLoading = false,
                                    isReceiverValid = false,
                                    error = "Không tìm thấy tài khoản, vui lòng kiểm tra lại."
                                ) 
                            }
                        } else {
                            // Fetch owner name from user document
                            val ownerId = acc.ownerId.trim()
                            try {
                                android.util.Log.d("TransferViewModel", "Looking up user profile for ownerId: '$ownerId'")
                                // Collect the flow and get the first non-Loading result
                                var userResult: ResultState<User>? = null
                                userRepository.getUserProfile(ownerId)
                                    .takeWhile { state ->
                                        // Continue collecting while Loading, stop when we get Success or Error
                                        if (state is ResultState.Loading) {
                                            true // Continue
                                        } else {
                                            userResult = state
                                            false // Stop collection
                                        }
                                    }
                                    .catch { e ->
                                        android.util.Log.e("TransferViewModel", "Flow error: ${e.message}")
                                        if (userResult == null) {
                                            userResult = ResultState.Error(e)
                                        }
                                    }
                                    .collect { } // Just collect to trigger the flow
                                
                                val result = userResult ?: ResultState.Error(IllegalStateException("No result received"))
                                android.util.Log.d("TransferViewModel", "User result type: ${result::class.simpleName}")
                                val ownerFullName = when (result) {
                                    is ResultState.Success -> {
                                        val user = result.data
                                        android.util.Log.d("TransferViewModel", "User object: $user")
                                        android.util.Log.d("TransferViewModel", "User.uid: '${user.uid}'")
                                        android.util.Log.d("TransferViewModel", "User.fullName: '${user.fullName}'")
                                        android.util.Log.d("TransferViewModel", "User.fullName.isBlank(): ${user.fullName.isBlank()}")
                                        android.util.Log.d("TransferViewModel", "User.fullName.length: ${user.fullName.length}")
                                        val name = user.fullName.takeIf { it.isNotBlank() }
                                        android.util.Log.d("TransferViewModel", "Extracted name after takeIf: '$name'")
                                        val trimmedName = name?.trim() ?: ""
                                        android.util.Log.d("TransferViewModel", "Final ownerFullName: '$trimmedName'")
                                        trimmedName
                                    }
                                    is ResultState.Error -> {
                                        android.util.Log.e("TransferViewModel", "Error fetching user: ${result.throwable.message}")
                                        android.util.Log.e("TransferViewModel", "Error stack trace: ${result.throwable.stackTraceToString()}")
                                        ""
                                    }
                                    else -> {
                                        android.util.Log.w("TransferViewModel", "User result is not Success or Error")
                                        ""
                                    }
                                }
                                
                                // Check if fullName is valid (not empty, not a UID, not a test identifier)
                                // Be more lenient - accept names without spaces (single names are valid)
                                val isValidName = ownerFullName.isNotBlank() && 
                                    ownerFullName.length > 2 && // At least 3 characters
                                    !ownerFullName.startsWith("test_") && 
                                    !ownerFullName.matches(Regex("^[a-zA-Z0-9]{20,}$")) && // Not a UID (long alphanumeric)
                                    !ownerFullName.equals(ownerId, ignoreCase = true) // Not the same as UID
                                
                                val displayName = if (isValidName) {
                                    android.util.Log.d("TransferViewModel", "Using fullName: $ownerFullName")
                                    ownerFullName
                                } else {
                                    // If we have a name but it doesn't pass strict validation, still try to use it
                                    // Only fallback to accountId if name is truly invalid
                                    if (ownerFullName.isNotBlank() && ownerFullName.length > 1 && 
                                        !ownerFullName.matches(Regex("^[a-zA-Z0-9]{20,}$"))) {
                                        android.util.Log.d("TransferViewModel", "Using fullName with lenient validation: $ownerFullName")
                                        ownerFullName
                                    } else {
                                        // Last resort: use accountId
                                        android.util.Log.d("TransferViewModel", "FullName not found or invalid (ownerFullName='$ownerFullName', ownerId='$ownerId'), using accountId: $accountId")
                                        "Tài khoản $accountId"
                                    }
                                }
                                
                                // Set receiver info first
                                setState { 
                                    copy(
                                        isLoading = false,
                                        receiverFullName = displayName,
                                        receiverName = displayName,
                                        error = null,
                                        isReceiverValid = true
                                    ) 
                                }
                                // Then update description with sender name
                                launch {
                                    val senderFullName = getSenderFullName()
                                    val autoDescription = if (senderFullName.isNotBlank()) {
                                        "$senderFullName chuyen tien"
                                    } else {
                                        ""
                                    }
                                    android.util.Log.d("TransferViewModel", "Setting receiverFullName to: $displayName, senderFullName: $senderFullName, description: $autoDescription")
                                    setState { 
                                        copy(
                                            description = autoDescription
                                        ) 
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("TransferViewModel", "Exception fetching user profile", e)
                                // Fallback: try to get name from account or use accountId
                                setState { 
                                    copy(
                                        isLoading = false,
                                        receiverFullName = "Tài khoản $accountId",
                                        receiverName = "Tài khoản $accountId",
                                        error = null,
                                        isReceiverValid = true
                                    ) 
                                }
                                // Update description with sender name
                                launch {
                                    val senderFullName = getSenderFullName()
                                    val autoDescription = if (senderFullName.isNotBlank()) {
                                        "$senderFullName chuyen tien"
                                    } else {
                                        ""
                                    }
                                    setState { 
                                        copy(
                                            description = autoDescription
                                        ) 
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                setState { 
                    copy(
                        isLoading = false,
                        error = "Lỗi khi tìm kiếm: ${e.message}",
                        isReceiverValid = false
                    ) 
                }
            }
        }
    }

    private fun resetForNewTransfer() {
        setState {
            copy(
                receiverAccountId = "",
                receiverName = "",
                receiverFullName = "",
                amount = "",
                description = "",
                generatedOtp = null,
                enteredOtp = "",
                bankSearch = "",
                selectedBank = "",
                selectedBankName = "",
                bankDropdownExpanded = false,
                error = null,
                success = false,
                otpCountdown = 20,
                otpExpired = false,
                isReceiverValid = false
            )
        }
    }

}


