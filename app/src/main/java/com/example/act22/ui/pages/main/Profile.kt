package com.example.act22.ui.pages.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.act22.activity.Screen
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Prompt

@Composable
fun UserProfile(
    navController: NavController
){
    MainScaffold(navController) {
        UserInfo(navController)
        UserWaletAndPlan()
    }
}

@Composable
fun UserInfo(
    navController: NavController
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 50.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        UserInfoField("userID: ", "230300")
        UserInfoField("name: ", "Milana Zhuhaievych")
        UserInfoField("company: ", "SnapOn")
        UserInfoField("email: ", "milana.zhug@gmail.com", {navController.navigate(Screen.MainPage.route)})
        UserInfoField("password: ", "********", {navController.navigate(Screen.MainPage.route)})
    }
}

@Composable
fun UserInfoField(
    prompt: String,
    value: String,
    onClick: (() -> Unit)? = null
){
   Row(
       modifier = Modifier
           .fillMaxWidth()
           .height(55.dp)
           .padding(5.dp)
           .background(
               Brush.horizontalGradient(
                   listOf(
                       MaterialTheme.colorScheme.primaryContainer,
                       MaterialTheme.colorScheme.primary
                   )
               )
           )
           .padding(10.dp),
       horizontalArrangement = Arrangement.Start,
       verticalAlignment = Alignment.CenterVertically
   ) {
       Row(
           Modifier
               .fillMaxHeight()
               .weight(1f)
       ){
           Text(
               text = prompt,
               color = MaterialTheme.colorScheme.onSecondaryContainer,
               style = MaterialTheme.typography.titleSmall
           )
           Text(
               text = value,
               color = MaterialTheme.colorScheme.onPrimary,
               style = MaterialTheme.typography.titleSmall
           )
       }
       if (onClick != null){
           Icon(
               imageVector = Icons.Outlined.Edit,
               contentDescription = "Change",
               tint = MaterialTheme.colorScheme.onPrimary,
               modifier = Modifier.clickable { onClick() }
           )
       }
   }
}

@Composable
fun UserWaletAndPlan(){
    var selectedTab by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Wallet", style = MaterialTheme.typography.bodySmall) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Plan Management", style = MaterialTheme.typography.bodySmall) }
            )
        }

        when (selectedTab) {
            0 -> WalletTab(currentBalance = 20.5){number, name, cvv, amount -> }
            1 -> PlanManagementTab(currentPlan = "Lite", onChangePlan = {  })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTab(currentBalance: Double, onTopUp: (String, String, String, Double) -> Unit) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var topUpAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Current Balance: $${String.format("%.2f", currentBalance)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

        StyledCardField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            label = "Card Number",
            placeholder = "1234 5678 9012 3456"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledCardField(
                value = expiryDate,
                onValueChange = { expiryDate = it },
                label = "Expiry Date",
                placeholder = "MM/YY",
                modifier = Modifier.weight(1f)
            )
            StyledCardField(
                value = cvv,
                onValueChange = { cvv = it },
                label = "CVV",
                placeholder = "123",
                modifier = Modifier.weight(1f)
            )
        }
        StyledCardField(
            value = topUpAmount,
            onValueChange = { topUpAmount = it },
            label = "Top-Up Amount",
            placeholder = "Enter amount"
        )

        BigButton("Top up") {onTopUp(cardNumber, cardNumber, cvv, topUpAmount.toDouble()) }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledCardField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}




@Composable
fun PlanManagementTab(currentPlan: String, onChangePlan: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Choose your plan:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            PlanCard(
                title = "Lite",
                price = "€0",
                details = listOf(
                    "Basic access to the app",
                    "No AI features",
                    "Limited support"
                ),
                isSelected = currentPlan == "Lite",
                onSelect = { onChangePlan("Lite") },
                modifier = Modifier.weight(1f).padding(8.dp)
            )

            PlanCard(
                title = "Premium",
                price = "€50",
                details = listOf(
                    "Access to all features",
                    "AI recommendations",
                    "Priority support"
                ),
                isSelected = currentPlan == "Premium",
                onSelect = { onChangePlan("Premium") },
                modifier = Modifier.weight(1f).padding(8.dp)
            )
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    details: List<String>,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = price,
                style = MaterialTheme.typography.displayMedium,
            )
            details.forEach { detail ->
                Text(
                    text = "- $detail",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = onSelect,
                enabled = !isSelected,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondary,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = if (isSelected) "Selected" else "Choose Plan",
                )
            }
        }
    }
}
