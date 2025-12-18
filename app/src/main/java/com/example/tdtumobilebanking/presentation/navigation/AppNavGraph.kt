package com.example.tdtumobilebanking.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import com.example.tdtumobilebanking.presentation.auth.LoginScreen
import com.example.tdtumobilebanking.presentation.auth.LoginViewModel
import com.example.tdtumobilebanking.presentation.branches.BranchMapScreen
import com.example.tdtumobilebanking.presentation.branches.BranchViewModel
import com.example.tdtumobilebanking.presentation.dashboard.CustomerDashboardScreen
import com.example.tdtumobilebanking.presentation.dashboard.DashboardViewModel
import com.example.tdtumobilebanking.presentation.dashboard.OfficerDashboardScreen
import com.example.tdtumobilebanking.presentation.dashboard.AccountDetailScreen
import com.example.tdtumobilebanking.presentation.dashboard.AccountsListScreen
import com.example.tdtumobilebanking.presentation.kyc.KycScreen
import com.example.tdtumobilebanking.presentation.transactions.TransferScreen
import com.example.tdtumobilebanking.presentation.transactions.TransferViewModel
import com.example.tdtumobilebanking.presentation.transactions.TransferEvent
import com.example.tdtumobilebanking.presentation.transactions.OtpScreen
import com.example.tdtumobilebanking.presentation.transactions.TransferResultScreen
import com.example.tdtumobilebanking.presentation.transactions.DepositWithdrawScreen
import com.example.tdtumobilebanking.presentation.transactions.DepositWithdrawViewModel
import com.example.tdtumobilebanking.presentation.transactions.DepositWithdrawEvent
import com.example.tdtumobilebanking.presentation.transactions.DepositWithdrawType
import com.example.tdtumobilebanking.presentation.transactions.DepositWithdrawOtpScreen
import com.example.tdtumobilebanking.presentation.transactions.DepositWithdrawResultScreen
import com.example.tdtumobilebanking.presentation.profile.CustomerProfileScreen
import com.example.tdtumobilebanking.presentation.utilities.UtilitiesScreen
import com.example.tdtumobilebanking.presentation.utilities.UtilityDetailScreen
import com.example.tdtumobilebanking.presentation.utilities.UtilityOtpScreen
import com.example.tdtumobilebanking.presentation.utilities.UtilityResultScreen
import com.example.tdtumobilebanking.presentation.utilities.UtilitiesViewModel
import com.example.tdtumobilebanking.presentation.utilities.UtilitiesEvent
import com.example.tdtumobilebanking.presentation.billpayment.BillPaymentScreen
import com.example.tdtumobilebanking.presentation.billpayment.BillPaymentResultScreen
import com.example.tdtumobilebanking.presentation.billpayment.BillPaymentViewModel
import com.example.tdtumobilebanking.presentation.billpayment.BillPaymentEvent
import com.example.tdtumobilebanking.presentation.profile.CustomerProfileViewModel
import com.example.tdtumobilebanking.presentation.profile.CustomerProfileEvent
import com.example.tdtumobilebanking.presentation.officer.CreateAccountScreen
import com.example.tdtumobilebanking.presentation.officer.CreateAccountViewModel
import com.example.tdtumobilebanking.presentation.officer.CreateAccountEvent
import com.example.tdtumobilebanking.presentation.officer.EditCustomerScreen
import com.example.tdtumobilebanking.presentation.officer.EditCustomerViewModel
import com.example.tdtumobilebanking.presentation.officer.EditCustomerEvent
import com.example.tdtumobilebanking.presentation.officer.UpdateInterestRateScreen
import com.example.tdtumobilebanking.presentation.officer.UpdateInterestRateViewModel

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN
    ) {
        composable(AppDestinations.LOGIN) {
            val vm: LoginViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value
            LoginScreen(
                state = state,
                onEvent = vm::onEvent,
                nextDestination = vm::nextDestination,
                onNavigateCustomer = { navController.navigate(AppDestinations.CUSTOMER_HOME) },
                onNavigateOfficer = { navController.navigate(AppDestinations.OFFICER_HOME) },
                onNavigateKyc = { navController.navigate(AppDestinations.KYC) }
            )
        }
        composable(AppDestinations.CUSTOMER_HOME) {
            val vm: DashboardViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value
            val primaryAccount = state.accounts.firstOrNull()
            LaunchedEffect(Unit) { vm.loadForCurrentUser() }
            CustomerDashboardScreen(
                state = state,
                onRefresh = vm::loadForCurrentUser,
                onAccountSelected = { accountId ->
                    vm.loadTransactionsForAccount(accountId)
                    navController.navigate(AppDestinations.accountDetail(accountId))
                },
                onTransferClick = { navController.navigate(AppDestinations.TRANSFER) },
                // Nút Thanh toán trên dashboard -> đi thẳng vào trang thanh toán Stripe
                onPayBillClick = {
                    navController.navigate(AppDestinations.BILL_PAYMENT)
                },
                onMapClick = { navController.navigate(AppDestinations.BRANCH_MAP) },
                onProfileClick = { navController.navigate(AppDestinations.CUSTOMER_PROFILE) },
                onHomeClick = { /* already on home, no-op */ },
                onAccountsClick = { navController.navigate(AppDestinations.ACCOUNTS) },
                onUtilitiesClick = { navController.navigate(AppDestinations.UTILITIES_GRAPH) },
                onProfileNavClick = { navController.navigate(AppDestinations.CUSTOMER_PROFILE) },
                onDepositClick = { accountId ->
                    navController.navigate(AppDestinations.deposit(accountId ?: primaryAccount?.accountId ?: ""))
                },
                onWithdrawClick = { accountId ->
                    navController.navigate(AppDestinations.withdraw(accountId ?: primaryAccount?.accountId ?: ""))
                },
                onLogout = {
                    vm.logout {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(
            route = AppDestinations.ACCOUNT_DETAIL,
            arguments = listOf(
                navArgument("accountId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId").orEmpty()
            android.util.Log.d("NavGraph", "ACCOUNT_DETAIL for accountId=$accountId")

            // dùng chung DashboardViewModel với CUSTOMER_HOME để có sẵn accounts
            val parentEntry = navController.getBackStackEntry(AppDestinations.CUSTOMER_HOME)
            val vm: DashboardViewModel = hiltViewModel(parentEntry)
            val state = vm.uiState.collectAsStateWithLifecycle().value

            val account = state.accounts.firstOrNull { it.accountId == accountId }
            if (account != null) {
                android.util.Log.d(
                    "NavGraph",
                    "AccountDetailScreen bound to account=${account.accountId}, balance=${account.balance}"
                )
                AccountDetailScreen(
                    account = account,
                    monthlyProfit = state.monthlyProfitPreview[accountId],
                    mortgagePayment = state.mortgagePreview[accountId],
                    onBack = { navController.popBackStack() }
                )
            } else {
                android.util.Log.w("NavGraph", "AccountDetailScreen: accountId=$accountId not found in state")
            }
        }
        composable(AppDestinations.ACCOUNTS) {
            android.util.Log.d("NavGraph", "ACCOUNTS screen opened")
            // dùng chung DashboardViewModel với CUSTOMER_HOME để có sẵn danh sách accounts
            val parentEntry = navController.getBackStackEntry(AppDestinations.CUSTOMER_HOME)
            val vm: DashboardViewModel = hiltViewModel(parentEntry)
            val state = vm.uiState.collectAsStateWithLifecycle().value
            
            LaunchedEffect(Unit) {
                if (state.accounts.isEmpty()) {
                    android.util.Log.d("NavGraph", "ACCOUNTS: accounts empty -> reload")
                    vm.loadForCurrentUser()
                } else {
                    android.util.Log.d("NavGraph", "ACCOUNTS: accounts size=${state.accounts.size}")
                }
            }

            AccountsListScreen(
                state = state,
                onAccountClick = { accountId: String ->
                    android.util.Log.d("NavGraph", "ACCOUNTS: click accountId=$accountId")
                    vm.loadTransactionsForAccount(accountId)
                    navController.navigate(AppDestinations.accountDetail(accountId))
                },
                onBack = { navController.popBackStack() },
                onHomeClick = { navController.navigate(AppDestinations.CUSTOMER_HOME) },
                onAccountsClick = { /* already on accounts */ },
                onUtilitiesClick = { navController.navigate(AppDestinations.UTILITIES_GRAPH) },
                onProfileClick = { navController.navigate(AppDestinations.CUSTOMER_PROFILE) }
            )
        }
        composable(AppDestinations.OFFICER_HOME) {
            val vm: DashboardViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value
            LaunchedEffect(Unit) {
                vm.loadAllCustomers()
                vm.loadForCurrentUser()
            }
            OfficerDashboardScreen(
                state = state,
                onRefresh = vm::loadAllCustomers,
                onCreateAccountClick = { navController.navigate(AppDestinations.CREATE_ACCOUNT) },
                onEditCustomerClick = { uid -> navController.navigate(AppDestinations.editCustomer(uid)) },
                onProfileClick = { uid -> navController.navigate(AppDestinations.editCustomer(uid)) },
                onUpdateInterestRateClick = { navController.navigate(AppDestinations.UPDATE_INTEREST_RATE) },
                onImportBillsClick = { vm.importBillsFromCsv() },
                onLogout = {
                    vm.logout {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(AppDestinations.TRANSFER) {
            val vm: TransferViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value
            TransferScreen(
                state = state,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() },
                onProceedToOtp = {
                    if (vm.proceedToOtp()) {
                        navController.navigate(AppDestinations.OTP)
                    }
                }
            )
        }
        composable(AppDestinations.OTP) {
            // Sử dụng cùng ViewModel instance với TRANSFER screen để giữ state
            val transferEntry = navController.getBackStackEntry(AppDestinations.TRANSFER)
            val vm: TransferViewModel = hiltViewModel(transferEntry)
            val state = vm.uiState.collectAsStateWithLifecycle().value
            
            // Auto-generate OTP if not already generated
            LaunchedEffect(Unit) {
                Log.d("AppNavGraph", "OTP Screen LaunchedEffect")
                Log.d("AppNavGraph", "Current state - generatedOtp: ${state.generatedOtp}, otpExpired: ${state.otpExpired}")
                Log.d("AppNavGraph", "receiverName: '${state.receiverName}', receiverFullName: '${state.receiverFullName}'")
                Log.d("AppNavGraph", "receiverAccountId: '${state.receiverAccountId}', amount: '${state.amount}'")
                // Không cần gọi proceedToOtp() nữa vì đã được gọi ở TRANSFER screen
                // Chỉ cần đảm bảo OTP đã được tạo
                if (state.generatedOtp == null && !state.otpExpired) {
                    Log.w("AppNavGraph", "OTP is null, this should not happen if navigation is correct")
                } else {
                    Log.d("AppNavGraph", "OTP status - generated: ${state.generatedOtp != null}, expired: ${state.otpExpired}")
                }
            }
            
            // Navigate back if OTP expired (with delay to show error message)
            LaunchedEffect(state.otpExpired) {
                if (state.otpExpired) {
                    // Wait 2 seconds to show error message before navigating back
                    delay(2000)
                    navController.popBackStack()
                }
            }
            
            // Navigate to result screen when success flag turns true
            LaunchedEffect(state.success) {
                if (state.success) {
                    navController.navigate(AppDestinations.TRANSFER_RESULT)
                }
            }
            
            OtpScreen(
                state = state,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() },
                onConfirm = {
                    vm.onEvent(TransferEvent.Confirm)
                },
                onAutoFillOtp = { vm.autoFillOtp() }
            )
        }
        composable(AppDestinations.TRANSFER_RESULT) {
            // Get ViewModel from TRANSFER entry (which should still be in backstack when navigating from OTP)
            // Use remember to cache the result and avoid recomposition issues
            val transferEntry = remember {
                try {
                    navController.getBackStackEntry(AppDestinations.TRANSFER)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            val vm: TransferViewModel = if (transferEntry != null) {
                hiltViewModel(transferEntry)
            } else {
                Log.w("AppNavGraph", "TRANSFER entry not found, using new ViewModel")
                hiltViewModel()
            }
            val state = vm.uiState.collectAsStateWithLifecycle().value

            TransferResultScreen(
                state = state,
                onNewTransfer = {
                    vm.onEvent(TransferEvent.Reset)
                    navController.navigate(AppDestinations.TRANSFER) {
                        popUpTo(AppDestinations.TRANSFER) { inclusive = true }
                    }
                },
                onBackHome = {
                    vm.onEvent(TransferEvent.Reset)
                    // Navigate to CUSTOMER_HOME and clear backstack
                    navController.navigate(AppDestinations.CUSTOMER_HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = AppDestinations.DEPOSIT,
            arguments = listOf(
                navArgument("accountId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            android.util.Log.d("AppNavGraph", "DEPOSIT screen: accountId=$accountId")
            val vm: DepositWithdrawViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value

            LaunchedEffect(Unit) {
                android.util.Log.d("AppNavGraph", "DEPOSIT: Initializing with accountId=$accountId")
                vm.initialize(DepositWithdrawType.DEPOSIT, accountId?.takeIf { it.isNotBlank() })
            }
            
            LaunchedEffect(state.accountId, state.accountNumber, state.currentBalance) {
                android.util.Log.d("AppNavGraph", "DEPOSIT state updated: accountId=${state.accountId}, accountNumber=${state.accountNumber}, balance=${state.currentBalance}, availableAccounts=${state.availableAccounts.size}")
            }

            DepositWithdrawScreen(
                state = state,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() },
                onProceedToOtp = {
                    if (vm.proceedToOtp()) {
                        navController.navigate(AppDestinations.DEPOSIT_WITHDRAW_OTP)
                    }
                }
            )
        }
        composable(
            route = AppDestinations.WITHDRAW,
            arguments = listOf(
                navArgument("accountId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")
            android.util.Log.d("AppNavGraph", "WITHDRAW screen: accountId=$accountId")
            val vm: DepositWithdrawViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value

            LaunchedEffect(Unit) {
                android.util.Log.d("AppNavGraph", "WITHDRAW: Initializing with accountId=$accountId")
                vm.initialize(DepositWithdrawType.WITHDRAW, accountId?.takeIf { it.isNotBlank() })
            }
            
            LaunchedEffect(state.accountId, state.accountNumber, state.currentBalance) {
                android.util.Log.d("AppNavGraph", "WITHDRAW state updated: accountId=${state.accountId}, accountNumber=${state.accountNumber}, balance=${state.currentBalance}, availableAccounts=${state.availableAccounts.size}")
            }

            DepositWithdrawScreen(
                state = state,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() },
                onProceedToOtp = {
                    if (vm.proceedToOtp()) {
                        navController.navigate(AppDestinations.DEPOSIT_WITHDRAW_OTP)
                    }
                }
            )
        }
        composable(AppDestinations.DEPOSIT_WITHDRAW_OTP) {
            val depositEntry = remember {
                try {
                    navController.getBackStackEntry(AppDestinations.DEPOSIT)
                } catch (e: IllegalArgumentException) {
                    try {
                        navController.getBackStackEntry(AppDestinations.WITHDRAW)
                    } catch (e2: IllegalArgumentException) {
                        null
                    }
                }
            }
            val vm: DepositWithdrawViewModel = if (depositEntry != null) {
                hiltViewModel(depositEntry)
            } else {
                hiltViewModel()
            }
            val state = vm.uiState.collectAsStateWithLifecycle().value

            LaunchedEffect(state.success) {
                if (state.success) {
                    navController.navigate(AppDestinations.DEPOSIT_WITHDRAW_RESULT)
                }
            }

            DepositWithdrawOtpScreen(
                state = state,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() },
                onConfirm = {
                    vm.onEvent(DepositWithdrawEvent.Confirm)
                },
                onAutoFillOtp = { vm.autoFillOtp() }
            )
        }
        composable(AppDestinations.DEPOSIT_WITHDRAW_RESULT) {
            val depositEntry = remember {
                try {
                    navController.getBackStackEntry(AppDestinations.DEPOSIT)
                } catch (e: IllegalArgumentException) {
                    try {
                        navController.getBackStackEntry(AppDestinations.WITHDRAW)
                    } catch (e2: IllegalArgumentException) {
                        null
                    }
                }
            }
            val vm: DepositWithdrawViewModel = if (depositEntry != null) {
                hiltViewModel(depositEntry)
            } else {
                hiltViewModel()
            }
            val state = vm.uiState.collectAsStateWithLifecycle().value

            DepositWithdrawResultScreen(
                state = state,
                onNewTransaction = {
                    vm.onEvent(DepositWithdrawEvent.Reset)
                    val route = if (state.type == DepositWithdrawType.DEPOSIT) {
                        AppDestinations.deposit(state.accountId)
                    } else {
                        AppDestinations.withdraw(state.accountId)
                    }
                    navController.navigate(route) {
                        popUpTo(route) { inclusive = true }
                    }
                },
                onBackHome = {
                    vm.onEvent(DepositWithdrawEvent.Reset)
                    navController.navigate(AppDestinations.CUSTOMER_HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(AppDestinations.KYC) {
            KycScreen(onCompleted = { navController.popBackStack() })
        }
        composable(AppDestinations.BRANCH_MAP) {
            val vm: BranchViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value
            BranchMapScreen(state = state, onRefresh = vm::loadBranches)
        }
        composable(AppDestinations.CUSTOMER_PROFILE) {
            val vm: CustomerProfileViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value
            CustomerProfileScreen(
                state = state,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() },
                onHomeClick = { navController.navigate(AppDestinations.CUSTOMER_HOME) },
                onAccountsClick = { navController.navigate(AppDestinations.ACCOUNTS) },
                onUtilitiesClick = { navController.navigate(AppDestinations.UTILITIES_GRAPH) },
                onProfileClick = { /* already on profile */ }
            )
        }
        composable(AppDestinations.UPDATE_INTEREST_RATE) {
            val vm: UpdateInterestRateViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value

            UpdateInterestRateScreen(
                state = state,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.CREATE_ACCOUNT) {
            val vm: CreateAccountViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value
            CreateAccountScreen(
                state = state,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = AppDestinations.EDIT_CUSTOMER,
            arguments = listOf(
                navArgument("uid") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            val vm: EditCustomerViewModel = hiltViewModel()
            val state = vm.uiState.collectAsStateWithLifecycle().value
            
            androidx.compose.runtime.LaunchedEffect(uid) {
                if (uid.isNotBlank()) {
                    vm.onEvent(EditCustomerEvent.LoadCustomer(uid))
                }
            }
            
            EditCustomerScreen(
                state = state,
                onEvent = vm::onEvent,
                onBack = { navController.popBackStack() },
                customerUid = uid
            )
        }
        navigation(
            startDestination = AppDestinations.UTILITIES,
            route = AppDestinations.UTILITIES_GRAPH
        ) {
            composable(AppDestinations.UTILITIES) {
                UtilitiesScreen(
                    onBack = { navController.popBackStack() },
                    onUtilitySelected = { utilityType ->
                        navController.navigate(AppDestinations.utilityDetail(utilityType))
                    },
                    onBillPaymentClick = {
                        navController.navigate(AppDestinations.BILL_PAYMENT)
                    },
                    onHomeClick = { navController.navigate(AppDestinations.CUSTOMER_HOME) },
                    onAccountsClick = { navController.navigate(AppDestinations.ACCOUNTS) },
                    onUtilitiesClick = { /* already on utilities */ },
                    onProfileClick = { navController.navigate(AppDestinations.CUSTOMER_PROFILE) }
                )
            }
            composable(AppDestinations.PAY_BILL) {
                // Redirect to new Bill Payment with Stripe
                navController.navigate(AppDestinations.BILL_PAYMENT) {
                    popUpTo(AppDestinations.PAY_BILL) { inclusive = true }
                }
            }
            
            // Bill Payment with Stripe
            composable(AppDestinations.BILL_PAYMENT) {
                val vm: BillPaymentViewModel = hiltViewModel()
                val state by vm.uiState.collectAsStateWithLifecycle()
                
                LaunchedEffect(state.paymentSuccess) {
                    if (state.paymentSuccess) {
                        navController.navigate(AppDestinations.BILL_PAYMENT_RESULT) {
                            popUpTo(AppDestinations.BILL_PAYMENT) { inclusive = true }
                        }
                    }
                }
                
                BillPaymentScreen(
                    state = state,
                    onEvent = vm::onEvent,
                    onBack = { navController.popBackStack() },
                    onPayWithStripe = {
                        vm.onEvent(BillPaymentEvent.InitiatePayment)
                    }
                )
            }
            
            composable(AppDestinations.BILL_PAYMENT_RESULT) {
                val billEntry = remember {
                    try {
                        navController.getBackStackEntry(AppDestinations.BILL_PAYMENT)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                val vm: BillPaymentViewModel = if (billEntry != null) {
                    hiltViewModel(billEntry)
                } else {
                    hiltViewModel()
                }
                val state by vm.uiState.collectAsStateWithLifecycle()
                
                BillPaymentResultScreen(
                    state = state,
                    onNewPayment = {
                        vm.onEvent(BillPaymentEvent.Reset)
                        navController.navigate(AppDestinations.BILL_PAYMENT) {
                            popUpTo(AppDestinations.UTILITIES_GRAPH) { inclusive = false }
                        }
                    },
                    onBackHome = {
                        vm.onEvent(BillPaymentEvent.Reset)
                        navController.navigate(AppDestinations.CUSTOMER_HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = AppDestinations.UTILITY_DETAIL,
                arguments = listOf(
                    navArgument("utilityType") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val utilityType = backStackEntry.arguments?.getString("utilityType") ?: ""
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestinations.UTILITIES_GRAPH)
                }
                val vm: UtilitiesViewModel = hiltViewModel(parentEntry)
                val state by vm.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(utilityType) {
                    if (utilityType.isNotBlank()) {
                        vm.initialize(utilityType)
                    }
                }

                UtilityDetailScreen(
                    utilityType = utilityType,
                    state = state,
                    onEvent = vm::onEvent,
                    onBack = { navController.popBackStack() },
                    onSubmit = {
                        vm.onEvent(UtilitiesEvent.Submit)
                        navController.navigate(AppDestinations.UTILITY_OTP)
                    }
                )
            }
            composable(AppDestinations.UTILITY_OTP) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestinations.UTILITIES_GRAPH)
                }
                val vm: UtilitiesViewModel = hiltViewModel(parentEntry)
                val state by vm.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.success) {
                    if (state.success) {
                        navController.navigate(AppDestinations.UTILITY_RESULT) {
                            popUpTo(AppDestinations.UTILITY_OTP) { inclusive = true }
                        }
                    }
                }

                UtilityOtpScreen(
                    state = state,
                    onEvent = vm::onEvent,
                    onBack = { navController.popBackStack() },
                    onConfirm = { vm.onEvent(UtilitiesEvent.Confirm) },
                    onAutoFillOtp = { vm.autoFillOtp() }
                )
            }
            composable(AppDestinations.UTILITY_RESULT) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppDestinations.UTILITIES_GRAPH)
                }
                val vm: UtilitiesViewModel = hiltViewModel(parentEntry)
                val state by vm.uiState.collectAsStateWithLifecycle()

                UtilityResultScreen(
                    state = state,
                    onNewTransaction = {
                        vm.onEvent(UtilitiesEvent.Reset)
                        navController.navigate(AppDestinations.UTILITIES) {
                            popUpTo(AppDestinations.UTILITIES_GRAPH) { inclusive = false }
                        }
                    },
                    onBackHome = {
                        vm.onEvent(UtilitiesEvent.Reset)
                        navController.navigate(AppDestinations.CUSTOMER_HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

