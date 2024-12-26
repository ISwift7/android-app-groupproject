package com.example.act22.ui.pages.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.act22.activity.Screen
import com.example.act22.viewmodel.UserCommunicationViewModel

@Composable
fun SupportEmail(navController: NavController) {
    MainScaffold(navController) {
        EmailForm(navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailForm(
    navController: NavController,
    userCommunicationViewModel: UserCommunicationViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailTitle by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    if (isSubmitted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Thank you for your for contacting us!",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            BigButton("To main menu") {
                navController.navigate(Screen.MainPage.route)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "We are sorry\nyou run into an error",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Please fill up this form.\nWe will contact you soon.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Your email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        TextField(
            value = emailTitle,
            onValueChange = { emailTitle = it },
            label = { Text("Error title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        TextField(
            value = emailText,
            onValueChange = { emailText = it },
            label = { Text("Error desciption") },
            placeholder = { Text("Write more about your error...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.surface,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSecondaryContainer,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current
        BigButton("Send the form") {
            userCommunicationViewModel.requestSupport(email, emailTitle, emailText){ result, msg ->
                if (result)
                    isSubmitted = true
                else
                    Toast.makeText(context, msg,Toast.LENGTH_SHORT).show()
            }
        }
    }
}


