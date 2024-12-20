package com.example.act22.ui.pages.authentication

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.act22.activity.Screen
import com.example.act22.ui.pages.main.BasicButton
import com.example.act22.viewmodel.AuthenticationViewModel
import com.radusalagean.infobarcompose.InfoBarMessage

@Composable
fun RegistrationSuccess(
    navController: NavController,
    authenticationViewModel: AuthenticationViewModel
){
    AuthPage(
        logoHeight = 100.dp,
        content = {
            RegistrationSuccessColumn(navController)
        }
    )
}

@Composable
fun RegistrationSuccessColumn(
    navController: NavController
){
    Spacer(modifier = Modifier.height(50.dp))
    Text(
        text = "Registration successful!",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(10.dp)
    )
    Text(
        text = "Your registration was successful, but we need you to confirm your email first!",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(10.dp)
    )
    AuthButton(
        prompt = "Continue",
        onClick = {
            navController.navigate(Screen.SignInPage.route)
        }
    )
    Spacer(modifier = Modifier.height(10.dp))
}
