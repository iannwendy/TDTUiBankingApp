package com.example.tdtumobilebanking

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tdtumobilebanking.ui.theme.TDTUMobileBankingTheme
import com.example.tdtumobilebanking.presentation.navigation.AppNavGraph
import com.stripe.android.PaymentConfiguration
import dagger.hilt.android.AndroidEntryPoint
import com.example.tdtumobilebanking.presentation.billpayment.StripePaymentManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        // Stripe Publishable Key
        private const val STRIPE_PUBLISHABLE_KEY = "pk_test_51SfHlZRzThh3el8ilhwOA7w7WruPQspiRhx2Rk3JHmZbN8w91FfLHDfmGnzOZlfDha0x2V10VJpf6Qn1CvLpRh9M00JzYb4JGc"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Stripe
        initializeStripe()
        // Initialize PaymentSheet manager (phải gọi trước khi Activity RESUMED)
        StripePaymentManager.init(this)
        
        enableEdgeToEdge()
        setContent {
            TDTUMobileBankingTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph()
                }
            }
        }
    }
    
    private fun initializeStripe() {
        try {
            PaymentConfiguration.init(
                applicationContext,
                STRIPE_PUBLISHABLE_KEY
            )
            Log.d(TAG, "Stripe initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Stripe", e)
        }
    }
}