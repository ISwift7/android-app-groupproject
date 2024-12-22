package com.example.act22.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.act22.activity.MainActivity
import com.example.act22.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val CHANNEL_ID = "price_alerts"
        private const val CHANNEL_NAME = "Price Alerts"
        private const val CHANNEL_DESCRIPTION = "Notifications for asset price alerts"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // Save the new token to Firestore for the current user
        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.email?.let { email ->
            val db = FirebaseFirestore.getInstance()
            db.collection("androidUsers")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        documents.documents[0].reference
                            .update("fcm_token", token)
                    }
                }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Create notification channel for Android O and above
        createNotificationChannel()

        // Get notification data
        val title = message.notification?.title ?: "Price Alert"
        val body = message.notification?.body ?: "A price alert has been triggered"
        
        // Create intent for when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            message.data["symbol"]?.let { symbol ->
                putExtra("symbol", symbol)
                putExtra("is_crypto", message.data["is_crypto"]?.toBoolean() ?: false)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build and show notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure you have this icon
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
} 