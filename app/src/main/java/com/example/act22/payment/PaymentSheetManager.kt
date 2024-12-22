package com.example.act22.payment

import androidx.activity.ComponentActivity
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

class PaymentSheetManager(private val activity: ComponentActivity) {
    private val stripe = Stripe(
        activity.applicationContext,
        "pk_test_51OPwQhHuhy8PxLxbxmOKHZGnpUNgFXMJBKxGyHXgKhEbwwKxEPtUAh8Ry8PoQDqyXHtxNvHI8iZQEtWLYhLvlHSq00yPrEPwbf"
    )

    var onPaymentCompleted: ((String, Double) -> Unit)? = null
    var onPaymentCanceled: (() -> Unit)? = null
    var onPaymentFailed: ((Throwable) -> Unit)? = null

    init {
        PaymentConfiguration.init(
            activity.applicationContext,
            "pk_test_51OPwQhHuhy8PxLxbxmOKHZGnpUNgFXMJBKxGyHXgKhEbwwKxEPtUAh8Ry8PoQDqyXHtxNvHI8iZQEtWLYhLvlHSq00yPrEPwbf"
        )
    }

    suspend fun processPayment(
        paymentIntentClientSecret: String,
        amount: Double,
        cardNumber: String,
        expiryMonth: Int,
        expiryYear: Int,
        cvc: String
    ) {
        try {
            withContext(Dispatchers.IO) {
                try {
                    withTimeout(30000) { // 30 second timeout
                        val paymentMethodParams = PaymentMethodCreateParams.create(
                            PaymentMethodCreateParams.Card.Builder()
                                .setNumber(cardNumber)
                                .setExpiryMonth(expiryMonth)
                                .setExpiryYear(expiryYear)
                                .setCvc(cvc)
                                .build()
                        )

                        val confirmParams = ConfirmPaymentIntentParams
                            .createWithPaymentMethodCreateParams(
                                paymentMethodCreateParams = paymentMethodParams,
                                clientSecret = paymentIntentClientSecret
                            )

                        try {
                            stripe.confirmPayment(activity, confirmParams)
                            withContext(Dispatchers.Main) {
                                Log.d("PaymentSheetManager", "Payment successful")
                                onPaymentCompleted?.invoke(paymentIntentClientSecret, amount)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.e("PaymentSheetManager", "Payment failed: ${e.message}")
                                onPaymentFailed?.invoke(e)
                            }
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    withContext(Dispatchers.Main) {
                        Log.e("PaymentSheetManager", "Payment timed out")
                        onPaymentFailed?.invoke(Exception("Payment timed out. Please try again."))
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("PaymentSheetManager", "Error processing payment: ${e.message}")
                onPaymentFailed?.invoke(e)
            }
        }
    }
} 