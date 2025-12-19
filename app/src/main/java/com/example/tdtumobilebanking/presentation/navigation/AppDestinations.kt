package com.example.tdtumobilebanking.presentation.navigation

object AppDestinations {
    const val LOGIN = "login"
    const val CUSTOMER_HOME = "customer_home"
    const val ACCOUNTS = "accounts"
    const val ACCOUNT_DETAIL = "account_detail/{accountId}"
    const val OFFICER_HOME = "officer_home"
    const val TRANSFER = "transfer"
    const val OTP = "otp"
    const val TRANSFER_RESULT = "transfer_result"
    const val DEPOSIT = "deposit/{accountId}"
    const val WITHDRAW = "withdraw/{accountId}"
    const val DEPOSIT_WITHDRAW_OTP = "deposit_withdraw_otp"
    const val DEPOSIT_WITHDRAW_RESULT = "deposit_withdraw_result"
    const val KYC = "kyc"
    
    fun deposit(accountId: String) = "deposit/$accountId"
    fun withdraw(accountId: String) = "withdraw/$accountId"
    const val BRANCH_MAP = "branch_map"
    const val UTILITIES_GRAPH = "utilities_graph"
    const val UTILITIES = "utilities"
    const val PAY_BILL = "pay_bill"
    const val UTILITY_DETAIL = "utility_detail/{utilityType}"
    const val UTILITY_OTP = "utility_otp"
    const val UTILITY_RESULT = "utility_result"
    const val CUSTOMER_PROFILE = "customer_profile"
    
    // Bill Payment with Stripe
    const val BILL_PAYMENT = "bill_payment"
    const val BILL_PAYMENT_OTP = "bill_payment_otp"
    const val BILL_PAYMENT_RESULT = "bill_payment_result"
    
    fun utilityDetail(utilityType: String) = "utility_detail/$utilityType"
    const val CREATE_ACCOUNT = "create_account"
    const val EDIT_CUSTOMER = "edit_customer/{uid}"
    const val UPDATE_INTEREST_RATE = "update_interest_rate"
    
    fun editCustomer(uid: String) = "edit_customer/$uid"
    fun accountDetail(accountId: String) = "account_detail/$accountId"
}

