package com.example.tdtumobilebanking.presentation.billpayment

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.tdtumobilebanking.core.common.BaseViewModel
import com.example.tdtumobilebanking.core.common.ResultState
import com.example.tdtumobilebanking.domain.model.Bill
import com.example.tdtumobilebanking.domain.model.BillStatus
import com.example.tdtumobilebanking.domain.repository.AccountRepository
import com.example.tdtumobilebanking.domain.repository.AuthRepository
import com.example.tdtumobilebanking.domain.repository.BillRepository
import com.example.tdtumobilebanking.domain.repository.TransactionRepository
import com.example.tdtumobilebanking.domain.usecase.account.GetAccountsUseCase
import com.example.tdtumobilebanking.domain.usecase.transaction.GenerateOtpUseCase
import com.example.tdtumobilebanking.domain.usecase.utilities.CreateStripePaymentIntentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

data class BillPaymentUiState(
    val billCode: String = "",
    val bill: Bill? = null,
    val isLookingUp: Boolean = false,
    val lookupError: String? = null,
    
    // Account info
    val selectedAccountId: String = "",
    val accountNumber: String = "",
    val currentBalance: Double = 0.0,
    val availableAccounts: List<com.example.tdtumobilebanking.domain.model.Account> = emptyList(),
    val showAccountSelector: Boolean = false,
    
    // OTP
    val generatedOtp: String? = null,
    val enteredOtp: String = "",
    val otpCountdown: Int = 20,
    val otpExpired: Boolean = false,
    
    // Stripe payment
    val isProcessingPayment: Boolean = false,
    val paymentClientSecret: String? = null,
    val paymentError: String? = null,
    
    // Result
    val paymentSuccess: Boolean = false,
    val transactionId: String? = null
)

sealed class BillPaymentEvent {
    data class BillCodeChanged(val code: String) : BillPaymentEvent()
    data object LookupBill : BillPaymentEvent()
    data class SelectAccount(val accountId: String) : BillPaymentEvent()
    data class ShowAccountSelector(val show: Boolean) : BillPaymentEvent()
    data object RequestOtp : BillPaymentEvent()
    data class OtpChanged(val value: String) : BillPaymentEvent()
    data object ConfirmOtp : BillPaymentEvent()
    data object InitiatePayment : BillPaymentEvent()
    data object PaymentCompleted : BillPaymentEvent()
    data class PaymentFailed(val error: String) : BillPaymentEvent()
    data object Reset : BillPaymentEvent()
    data object SeedMockBills : BillPaymentEvent()
}

@HiltViewModel
class BillPaymentViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val authRepository: AuthRepository,
    private val createStripePaymentIntentUseCase: CreateStripePaymentIntentUseCase,
    private val generateOtpUseCase: GenerateOtpUseCase
) : BaseViewModel<BillPaymentUiState>(BillPaymentUiState()) {

    private var otpCountdownJob: Job? = null

    companion object {
        private const val TAG = "BillPaymentVM"
        // Stripe publishable key - in production, this should come from secure config
        const val STRIPE_PUBLISHABLE_KEY = "pk_test_51SfHlZRzThh3el8ilhwOA7w7WruPQspiRhx2Rk3JHmZbN8w91FfLHDfmGnzOZlfDha0x2V10VJpf6Qn1CvLpRh9M00JzYb4JGc"
    }

    init {
        loadUserAccounts()
    }

    private fun loadUserAccounts() {
        viewModelScope.launch {
            try {
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
                                        selectedAccountId = acc.accountId,
                                        accountNumber = acc.accountId,
                                        currentBalance = acc.balance,
                                        availableAccounts = checkingAccounts.ifEmpty { accounts }
                                    )
                                }
                            }
                        }
                        is ResultState.Error -> {
                            Log.e(TAG, "Failed to load accounts: ${accountsResult.throwable.message}")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading accounts", e)
            }
        }
    }

    fun onEvent(event: BillPaymentEvent) {
        when (event) {
            is BillPaymentEvent.BillCodeChanged -> {
                setState { copy(billCode = event.code, lookupError = null, bill = null) }
            }
            BillPaymentEvent.LookupBill -> lookupBill()
            is BillPaymentEvent.SelectAccount -> selectAccount(event.accountId)
            is BillPaymentEvent.ShowAccountSelector -> {
                setState { copy(showAccountSelector = event.show) }
            }
            BillPaymentEvent.RequestOtp -> requestOtp()
            is BillPaymentEvent.OtpChanged -> setState { copy(enteredOtp = event.value) }
            BillPaymentEvent.ConfirmOtp -> confirmOtp()
            BillPaymentEvent.InitiatePayment -> initiatePayment()
            BillPaymentEvent.PaymentCompleted -> handlePaymentSuccess()
            is BillPaymentEvent.PaymentFailed -> {
                setState { copy(isProcessingPayment = false, paymentClientSecret = null, paymentError = event.error) }
            }
            BillPaymentEvent.Reset -> resetState()
            BillPaymentEvent.SeedMockBills -> seedMockBills()
        }
    }

    fun proceedToOtp(): Boolean {
        val current = uiState.value
        val bill = current.bill ?: return false
        
        if (current.currentBalance < bill.amount) {
            setState { copy(lookupError = "Số dư tài khoản không đủ để thanh toán") }
            return false
        }
        
        requestOtp()
        startOtpCountdown()
        return true
    }

    private fun requestOtp() {
        val otp = generateOtpUseCase()
        Log.d(TAG, "Generated OTP: $otp")
        setState {
            copy(
                generatedOtp = otp,
                enteredOtp = "",
                otpExpired = false,
                paymentError = null
            )
        }
    }

    private fun startOtpCountdown() {
        // Cancel previous countdown job if exists
        otpCountdownJob?.cancel()
        
        otpCountdownJob = viewModelScope.launch {
            var remaining = 20
            setState { copy(otpCountdown = remaining, otpExpired = false) }
            while (remaining > 0) {
                delay(1000)
                // Kiểm tra xem payment đã thành công chưa, nếu có thì dừng countdown
                if (uiState.value.paymentSuccess) {
                    Log.d(TAG, "Payment succeeded, stopping OTP countdown")
                    return@launch
                }
                remaining--
                setState { copy(otpCountdown = remaining) }
            }
            // Chỉ set expired nếu payment chưa thành công
            if (!uiState.value.paymentSuccess) {
                setState {
                    copy(
                        otpExpired = true,
                        generatedOtp = null,
                        enteredOtp = "",
                        lookupError = "OTP đã hết hạn. Vui lòng thử lại."
                    )
                }
            }
        }
    }

    fun autoFillOtp() {
        val otp = uiState.value.generatedOtp
        if (otp != null && !uiState.value.otpExpired) {
            setState { copy(enteredOtp = otp) }
        }
    }

    private fun confirmOtp() {
        val current = uiState.value
        if (current.generatedOtp.isNullOrBlank()) {
            setState { copy(lookupError = "Vui lòng yêu cầu OTP trước") }
            return
        }
        if (current.enteredOtp != current.generatedOtp) {
            setState { copy(lookupError = "OTP không đúng") }
            return
        }
        
        // Dừng OTP countdown khi OTP đúng
        otpCountdownJob?.cancel()
        otpCountdownJob = null
        
        // Sau khi OTP đúng, tạo PaymentIntent và chuyển sang Stripe
        initiatePayment()
    }

    private fun lookupBill() {
        val code = uiState.value.billCode.trim().uppercase()
        if (code.isBlank()) {
            setState { copy(lookupError = "Vui lòng nhập mã hóa đơn") }
            return
        }

        setState { copy(isLookingUp = true, lookupError = null, bill = null) }

        viewModelScope.launch {
            when (val result = billRepository.lookupBill(code)) {
                is ResultState.Success -> {
                    val bill = result.data
                    if (bill != null) {
                        if (bill.status == BillStatus.PAID) {
                            setState {
                                copy(
                                    isLookingUp = false,
                                    lookupError = "Hóa đơn này đã được thanh toán",
                                    bill = null
                                )
                            }
                        } else {
                            setState {
                                copy(
                                    isLookingUp = false,
                                    bill = bill,
                                    lookupError = null
                                )
                            }
                        }
                    } else {
                        setState {
                            copy(
                                isLookingUp = false,
                                lookupError = "Không thể đọc thông tin hóa đơn"
                            )
                        }
                    }
                }
                is ResultState.Error -> {
                    setState {
                        copy(
                            isLookingUp = false,
                            lookupError = result.throwable.message ?: "Mã hóa đơn không tồn tại"
                        )
                    }
                }
                else -> {}
            }
        }
    }

    private fun selectAccount(accountId: String) {
        val account = uiState.value.availableAccounts.find { it.accountId == accountId }
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

    private fun initiatePayment() {
        val current = uiState.value
        val bill = current.bill ?: return

        if (current.currentBalance < bill.amount) {
            setState { copy(paymentError = "Số dư tài khoản không đủ để thanh toán") }
            return
        }

        setState { copy(isProcessingPayment = true, paymentError = null, paymentClientSecret = null, lookupError = null) }

        // Gọi backend của bạn để tạo PaymentIntent với Stripe
        viewModelScope.launch {
            try {
                when (val result = createStripePaymentIntentUseCase(bill.amount, bill.billCode)) {
                    is ResultState.Success -> {
                        val clientSecret = result.data
                        setState {
                            copy(
                                isProcessingPayment = false,
                                paymentClientSecret = clientSecret,
                                paymentError = null
                            )
                        }
                        Log.d(TAG, "PaymentIntent created for bill: ${bill.billCode}, amount: ${bill.amount}")
                    }
                    is ResultState.Error -> {
                        setState {
                            copy(
                                isProcessingPayment = false,
                                paymentClientSecret = null,
                                paymentError = result.throwable.message ?: "Không thể khởi tạo thanh toán"
                            )
                        }
                        Log.e(TAG, "Error creating PaymentIntent: ${result.throwable.message}")
                    }
                    ResultState.Loading -> {
                        // không dùng trong trường hợp này
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initiating payment", e)
                setState {
                    copy(
                        isProcessingPayment = false,
                        paymentClientSecret = null,
                        paymentError = "Không thể khởi tạo thanh toán: ${e.message}"
                    )
                }
            }
        }
    }

    fun handlePaymentSuccess() {
        val current = uiState.value
        val bill = current.bill ?: return

        viewModelScope.launch {
            try {
                // 1. Deduct from account balance
                val newBalance = current.currentBalance - bill.amount
                val updateResult = accountRepository.updateBalance(current.selectedAccountId, newBalance)
                
                if (updateResult is ResultState.Error) {
                    setState {
                        copy(
                            isProcessingPayment = false,
                            paymentError = "Không thể trừ tiền từ tài khoản"
                        )
                    }
                    return@launch
                }

                // 2. Mark bill as paid
                val markPaidResult = billRepository.markBillAsPaid(bill.billId)
                if (markPaidResult is ResultState.Error) {
                    Log.e(TAG, "Failed to mark bill as paid, but payment was successful")
                }

                // 3. Create transaction record
                val txnResult = transactionRepository.createUtilityTransaction(
                    accountId = current.selectedAccountId,
                    utilityType = bill.billType,
                    amount = bill.amount,
                    provider = bill.provider,
                    customerCode = bill.customerCode,
                    phoneNumber = "",
                    description = "Thanh toán hóa đơn ${bill.billCode} - ${bill.description}"
                )

                val transactionId = if (txnResult is ResultState.Success) {
                    "TXN_${System.currentTimeMillis()}"
                } else null

                // 4. Update UI state
                // Dừng OTP countdown khi payment thành công
                otpCountdownJob?.cancel()
                otpCountdownJob = null
                setState {
                    copy(
                        isProcessingPayment = false,
                        paymentSuccess = true,
                        transactionId = transactionId,
                        currentBalance = newBalance
                    )
                }

                Log.d(TAG, "Payment completed successfully for bill: ${bill.billCode}")
            } catch (e: Exception) {
                Log.e(TAG, "Error completing payment", e)
                setState {
                    copy(
                        isProcessingPayment = false,
                        paymentError = "Lỗi xử lý thanh toán: ${e.message}"
                    )
                }
            }
        }
    }

    private fun seedMockBills() {
        viewModelScope.launch {
            val result = billRepository.seedMockBills()
            if (result is ResultState.Success) {
                Log.d(TAG, "Mock bills seeded successfully")
            } else if (result is ResultState.Error) {
                Log.e(TAG, "Failed to seed mock bills: ${result.throwable.message}")
            }
        }
    }

    private fun resetState() {
        // Cancel OTP countdown
        otpCountdownJob?.cancel()
        otpCountdownJob = null
        setState {
            BillPaymentUiState(
                selectedAccountId = selectedAccountId,
                accountNumber = accountNumber,
                currentBalance = currentBalance,
                availableAccounts = availableAccounts,
                generatedOtp = null,
                enteredOtp = "",
                otpCountdown = 20,
                otpExpired = false
            )
        }
    }
}

