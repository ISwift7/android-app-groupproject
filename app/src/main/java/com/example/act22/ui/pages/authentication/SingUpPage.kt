package com.example.act22.ui.pages.authentication

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.act22.activity.Screen
import com.example.act22.viewmodel.AuthenticationViewModel
import com.radusalagean.infobarcompose.InfoBar
import com.radusalagean.infobarcompose.InfoBarMessage


@Composable
fun SignUpPage(
    navController: NavController,
    authenticationViewModel: AuthenticationViewModel
) {
    AuthPage(
        logoHeight = 100.dp,
        content = {
            SignUpColumn(
                navController,
                authenticationViewModel
            )
        }
    )
}

@Composable
fun SignUpColumn(
    navController: NavController,
    authenticationViewModel: AuthenticationViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Spacer(modifier = Modifier.height(10.dp))

    AuthTextField("Email", value = email, onValueChange = { email = it })
    AuthTextField("Password", value = password, onValueChange = { password = it }, isPassword = true)

    AuthButton("Sign up") {
        authenticationViewModel.signUpUser(email, password) {success, errorMessages ->
            if (success) {
                navController.navigate(Screen.RegistrationSuccess.route)
            } else {
                Toast.makeText(
                    context,
                    errorMessages ?: "An unknown error occurred.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LinkToOtherPage("Already have an account?", "Sign in.", { navController.navigate(Screen.SignInPage.route) })

    OrDevider()

    AuthGoogleButton("Sign up", {})
}


