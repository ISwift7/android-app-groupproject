package com.example.act22

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.common.GoogleApiAvailability

class ACT22Application : Application() {
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            
            // Initialize Firestore
            val db = FirebaseFirestore.getInstance()
            
            // Enable Firestore logging in debug mode
            FirebaseFirestore.setLoggingEnabled(true)
            
            Log.d("ACT22Application", "Firebase initialized successfully")
            
            // Check Google Play Services availability
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(this)
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                Log.e("ACT22Application", "Google Play Services not available: $resultCode")
                // Let the user know they need to update Google Play Services
                if (availability.isUserResolvableError(resultCode)) {
                    Log.w("ACT22Application", "Google Play Services error is resolvable")
                }
            } else {
                Log.d("ACT22Application", "Google Play Services available")
            }
        } catch (e: Exception) {
            Log.e("ACT22Application", "Error initializing Firebase", e)
        }
    }
} 