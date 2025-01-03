package com.example.act22.viewmodel

import android.app.Activity
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.R
import com.example.act22.service.BackendService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val backendService = BackendService()

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

        Log.d("SignUp", "Starting sign up process for email: $email")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SignUp", "User created successfully in Firebase")
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            Log.d("SignUp", "User signed in after creation")
                            val user = auth.currentUser
                            user?.let { firebaseUser ->
                                firebaseUser.getIdToken(true)
                                    .addOnCompleteListener { tokenTask ->
                                        if (tokenTask.isSuccessful) {
                                            val idToken = tokenTask.result.token
                                            Log.d("SignUp", "Got ID token, creating user in backend")
                                            viewModelScope.launch {
                                                try {
                                                    backendService.createUser(email, idToken!!)
                                                        .onSuccess {
                                                            Log.d("EmailVerification", "About to send verification email to: $email")
                                                            firebaseUser.sendEmailVerification()
                                                                .addOnCompleteListener { verificationTask ->
                                                                    if (verificationTask.isSuccessful) {
                                                                        Log.d("EmailVerification", "Verification email sent successfully")
                                                                        onResult(true, "Registration successful! Please check your email to verify your account.")
                                                                    } else {
                                                                        val exception = verificationTask.exception
                                                                        Log.e("EmailVerification", "Failed to send verification email", exception)
                                                                        val errorMessage = when {
                                                                            exception?.message?.contains("too-many-requests") == true ->
                                                                                "Too many verification emails sent. Please try again later."
                                                                            exception?.message?.contains("invalid-email") == true ->
                                                                                "Invalid email address format."
                                                                            else -> "Failed to send verification email: ${exception?.message ?: "Unknown error"}"
                                                                        }
                                                                        onResult(false, errorMessage)
                                                                    }
                                                                }
                                                        }
                                                        .onFailure { e ->
                                                            Log.e("SignUp", "Failed to create user in backend", e)
                                                            onResult(false, "Failed to create user in backend: ${e.message}")
                                                        }
                                                } catch (e: Exception) {
                                                    Log.e("SignUp", "Exception during user creation", e)
                                                    onResult(false, "Failed to create user: ${e.message}")
                                                }
                                            }
                                        } else {
                                            Log.e("SignUp", "Failed to get ID token", tokenTask.exception)
                                            onResult(false, "Failed to get authentication token")
                                        }
                                    }
                            } ?: run {
                                Log.e("SignUp", "User is null after successful sign in")
                                onResult(false, "User registration failed. Please try again.")
                            }
                        } else {
                            Log.e("SignUp", "Failed to sign in after creation", signInTask.exception)
                            onResult(false, "Automatic sign-in failed. Please try logging in manually.")
                        }
                    }
                } else {
                    val errorMessage = getErrorMessage(task.exception)
                    Log.e("SignUp", "Failed to create user", task.exception)
                    onResult(false, errorMessage)
                }
            }
    }

    fun signInUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        validateCredentials(email, password)?.let {
            onResult(false, it)
            return
        }

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

    fun getGoogleSignInClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun handleGoogleAuthentication(
        idToken: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                user?.let { firebaseUser ->
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true // Check if the user is new
                    val email = firebaseUser.email ?: "No Email"

                    if (isNewUser) {
                        // If the user is new, register them in the backend
                        viewModelScope.launch {
                            try {
                                backendService.createUser(email, idToken).onSuccess {
                                    onResult(true, "Registration successful!")
                                }.onFailure { backendError ->
                                    onResult(false, "Failed to register user in backend: ${backendError.message}")
                                }
                            } catch (e: Exception) {
                                println(e)
                                onResult(false, "Failed to register user: ${e.message}")
                            }
                        }
                    } else {
                        // If the user is existing, sign them in
                        onResult(true, "Sign-in successful!")
                    }
                } ?: onResult(false, "Google sign-in failed. Please try again.")
            } else {
                val errorMessage = task.exception?.message ?: "Unknown error occurred."
                onResult(false, errorMessage)
            }
        }
    }
}