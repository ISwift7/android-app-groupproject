package com.example.act22.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class AuthenticationViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private fun validateCredentials(email: String, password: String): String? {
        if (email.isEmpty() || password.isEmpty()) {
            return "All fields must be filled in."
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "The email address format is invalid."
        }
        return null
    }

    private fun getErrorMessage(exception: Exception?): String {
        return when {
            exception is FirebaseAuthInvalidUserException -> "This email is not registered. Please sign up first."
            exception is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
            exception is FirebaseAuthUserCollisionException -> "This email is already registered."
            exception?.message?.contains("PASSWORD_DOES_NOT_MEET_REQUIREMENTS") == true ->
                "Password needs to be 8 characters long, include upper- and lowercase letters."
            else -> exception?.message ?: "An unknown error occurred. Please try again."
        }
    }

    fun signUpUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        validateCredentials(email, password)?.let {
            onResult(false, it)
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            val user = auth.currentUser
                            user?.let {
                                it.sendEmailVerification()
                                    .addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            onResult(true, "Registration successful! Please check your email to verify your account.")
                                        } else {
                                            onResult(false, "Failed to send verification email. Please try again.")
                                        }
                                    }
                            } ?: onResult(false, "User registration failed. Please try again.")
                        } else {
                            onResult(false, "Automatic sign-in failed. Please try logging in manually.")
                        }
                    }
                } else {
                    val errorMessage = getErrorMessage(task.exception)
                    Log.e("SignUP", "${task.exception}")
                    onResult(false, errorMessage)
                }
            }
    }

    fun signInUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        validateCredentials(email, password)?.let {
            onResult(false, it)
            return
        }
        //TODO check whether email is confirmed

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        Log.d("SignIn", "Successfully signed in: ${user.email}")
                        onResult(true, null)
                    } else {
                        auth.signOut()
                        Log.e("SignIn", "Email not verified. Please verify your email.")
                        onResult(false, "Please verify your email before signing in.")
                    }
                } else {
                    val errorMessage = getErrorMessage(task.exception)
                    onResult(false, errorMessage)
                }
            }
    }

    fun passwordRecovery(email: String, onResult: (Boolean, String?) -> Unit) {
        validateCredentials(email, "password")?.let {
            onResult(false, it)
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Password reset email sent. Please check your inbox.")
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidUserException -> "No account found with this email address."
                        else -> task.exception?.message ?: "Failed to send password reset email. Please try again."
                    }
                    onResult(false, errorMessage)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun checkIfUserIsLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}