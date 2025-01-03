package com.example.act22.ui.pages.main

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.act22.activity.Screen
import com.example.act22.viewmodel.WalletViewModel
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.act22.activity.LocalPaymentSheetManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Divider
import androidx.compose.material.icons.filled.Refresh
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.LaunchedEffect
import com.example.act22.viewmodel.UserPlanViewModel

@Composable
fun UserProfile(
    navController: NavController
){
    MainScaffold(navController) {
        UserInfo()
        UserWaletAndPlan()
    }
}

@Composable
fun UserInfo(){
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid ?: "Not logged in"
    val userEmail = currentUser?.email ?: "No email"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 50.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        UserInfoField("userID: ", userId)
        UserInfoField("email: ", userEmail)
    }
}

@Composable
fun UserInfoField(
    prompt: String,
    value: String
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
}

@Composable
fun UserWaletAndPlan(){
    var selectedTab by remember { mutableIntStateOf(0) }
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
            0 -> WalletTab()
            1 -> PlanManagementTab(currentPlan = "Lite", onChangePlan = {  })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTab(
    walletViewModel: WalletViewModel = viewModel()
) {
    var topUpAmount by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    val balance by walletViewModel.balance.collectAsState()
    val isLoading by walletViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Automatically load balance when the tab is opened
    LaunchedEffect(Unit) {
        walletViewModel.refreshBalance { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    // Format expiry date with slash
    fun formatExpiryDate(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }

        return when {
            digitsOnly.length <= 2 -> digitsOnly
            else -> "${digitsOnly.take(2)}/${digitsOnly.drop(2)}"
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            BalanceInfo(balance, walletViewModel)

                CardDetails(
                    cardNumber = cardNumber,
                    expiryDate = expiryDate,
                    cvv = cvv,
                    onCardNumberChange = { cardNumber = it },
                    onExpiryDateChange = { expiryDate = it },
                    onCvvChange = { cvv = it },
                    formatExpiryDate = ::formatExpiryDate
                )
            Column(
                modifier = Modifier.padding(bottom = 15.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                TopUpAmountInput(
                    topUpAmount = topUpAmount,
                    onAmountChange = { topUpAmount = it }
                )

                Button(
                    onClick = {
                        walletViewModel.validateAndProcessPayment(
                            amount = topUpAmount,
                            cardNumber = cardNumber,
                            expiryDate = expiryDate,
                            cvv = cvv,
                            onSuccess = {
                                Toast.makeText(context, "Payment successful!", Toast.LENGTH_SHORT).show()
                                topUpAmount = ""
                                cardNumber = ""
                                expiryDate = ""
                                cvv = ""
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Top up",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }

    }
}


@Composable
fun PlanManagementTab(
    currentPlan: String,
    onChangePlan: (String) -> Unit,
    userPlanViewModel: UserPlanViewModel = viewModel()
) {
    val isPremium by userPlanViewModel.userPlan.collectAsState()
    val msg by userPlanViewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(msg) {
        if (msg != null){
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            userPlanViewModel.clearErrorMessage()
        }
    }

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

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {

            PlanCard(
                title = "Lite",
                price = "€0",
                details = listOf(
                    "Basic trading",
                    "No AI features",
                    "No portfolio hints",
                    "Limited support"
                ),
                isSelected = !isPremium,
                onSelect = {
                    userPlanViewModel.changeUserPlan(newPlan = "Lite") },
                modifier = Modifier.weight(1f).padding(8.dp)
            )

            PlanCard(
                title = "Premium",
                price = "€50",
                details = listOf(
                    "AI asset analysis",
                    "AI asset predictions",
                    "AI portfolio hints",
                    "Priority support"
                ),
                isSelected = isPremium,
                onSelect = {
                    userPlanViewModel.changeUserPlan(newPlan = "Premium") },
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
        Row(){
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = price,
                    style = MaterialTheme.typography.displayLarge,
                )
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ){
                Text(
                    text = "Available Features:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Column (
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ){
                    details.forEach { detail ->
                        Text(
                            text = "- $detail",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BalanceInfo(
    balance: Double,
    walletViewModel: WalletViewModel
){
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Current Balance: $${String.format("%.2f", balance)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = {
                    walletViewModel.refreshBalance { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Balance",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun CardDetails(
    cardNumber: String,
    expiryDate: String,
    cvv: String,
    onCardNumberChange: (String) -> Unit,
    onExpiryDateChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    formatExpiryDate: (String) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Card Details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Card Number",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Column{
                    BasicTextField(
                        value = cardNumber,
                        onValueChange = {
                            val digitsOnly = it.filter { char -> char.isDigit() }
                            if (digitsOnly.length <= 16) {
                                onCardNumberChange(digitsOnly)
                            }
                        },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 18.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true
                    )
                    Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                }

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Expiry (MM/YY)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    BasicTextField(
                        value = expiryDate,
                        onValueChange = {
                            val digitsOnly = it.filter { char -> char.isDigit() }
                            if (digitsOnly.length <= 4) {
                                onExpiryDateChange(formatExpiryDate(digitsOnly))
                            }
                        },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CVV",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    BasicTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 3) onCvvChange(it) },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                }
            }
        }
    }
}


@Composable
fun TopUpAmountInput(
    topUpAmount: String,
    onAmountChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Top-Up Amount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        BasicTextField(
            value = topUpAmount,
            onValueChange = onAmountChange,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 18.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .padding(16.dp)
        )
    }
}

