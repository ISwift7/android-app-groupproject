package com.example.act22.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class UserCommunicationViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun leaveFeedback(rating: Int, comment: String, onResult: (Boolean, String?) -> Unit){
        val userId = auth.currentUser?.uid

        if(userId == null){
            onResult(false, "Please sign in to submit feedback")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val review = hashMapOf(
                "userId" to userId,
                "rating" to rating,
                "comment" to comment,
                "timestamp" to Date()
            )
            db.collection("userReviews")
                .add(review)
                .addOnSuccessListener {
                    onResult(true, null)
                }
                .addOnFailureListener {
                    onResult(false, "Failed to submit feedback. Try again later.")
                }

        }
    }

    fun requestSupport(email:String, title:String, problem:String, onResult: (Boolean, String?) -> Unit){
        val userId = auth.currentUser?.uid

        if(userId == null){
            onResult(false, "Please sign in to submit feedback")
            return
        }

        if (email.isEmpty() || title.isEmpty() || problem.isEmpty()) {
            onResult(false, "Please fill all fields. ")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult(false, "The email address format is invalid.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val support = hashMapOf(
                "platform" to "Android App",
                "userId" to userId,
                "email" to email,
                "title" to title,
                "problem" to problem,
                "timestamp" to Date()
            )

            db.collection("supportRequests")
                .add(support)
                .addOnSuccessListener {
                    onResult(true, null)
                }
                .addOnFailureListener {
                    onResult(false, "Failed to submit feedback. Try again later.")
                }
        }
    }
}