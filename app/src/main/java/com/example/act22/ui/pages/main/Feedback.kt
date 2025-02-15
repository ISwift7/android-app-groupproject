package com.example.act22.ui.pages.main

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


@Composable
fun FeedbackPage(navController: NavController) {
    MainScaffold(navController) {
        FeedBackColumn(navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedBackColumn(
    navController: NavController,
    userCommunicationViewModel: UserCommunicationViewModel = viewModel()
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
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
                text = "Thank you for your feedback!",
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
            text = "We value your feedback!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Rate Us:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        StarRating(selectedRating) { newRating ->
            selectedRating = newRating
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = feedbackText,
            onValueChange = { feedbackText = it },
            label = { Text("Your Comments") },
            placeholder = { Text("Write your feedback here...") },
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
        BigButton("Submit feedback") {
            userCommunicationViewModel.leaveFeedback(selectedRating, feedbackText){ result, msg ->
                if (result)
                    isSubmitted = true
                else
                    Toast.makeText(context, msg,Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun StarRating(currentRating: Int, onRatingSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { index ->
            val isSelected = index <= currentRating
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star",
                tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingSelected(index) }
            )
        }
    }
}
