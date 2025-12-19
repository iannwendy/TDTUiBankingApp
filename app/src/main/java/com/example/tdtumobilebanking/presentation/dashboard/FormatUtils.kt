package com.example.tdtumobilebanking.presentation.dashboard

import java.text.NumberFormat
import java.util.Locale

internal fun formatBalance(value: Double, currency: String): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return "${formatter.format(value)} $currency"
}

