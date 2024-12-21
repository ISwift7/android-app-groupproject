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
import com.google.firebase.auth.FirebaseAuth

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
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid ?: "Not logged in"
    val userEmail = currentUser?.email ?: "No email"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 50.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        UserInfoField("userID: ", userId)
        UserInfoField("email: ", userEmail)
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

    // Format card number (no spacing)
    fun formatCardNumber(input: String): String {
        return input.filter { it.isDigit() }
    }

    // Format expiry date with slash
    fun formatExpiryDate(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }
        return if (digitsOnly.length >= 2) {
            "${digitsOnly.take(2)}/${digitsOnly.drop(2)}"
        } else {
            digitsOnly
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color(0xFF4A148C)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Current Balance: $${String.format("%.2f", balance)}",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF4A148C),
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = {
                    scope.launch {
                        walletViewModel.refreshBalance { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,  // You might want to use a refresh icon instead
                    contentDescription = "Refresh Balance",
                    tint = Color(0xFF4A148C)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0E0E0)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Card Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF4A148C)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Card Number",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A148C)
                    )
                    BasicTextField(
                        value = cardNumber,
                        onValueChange = { 
                            val digitsOnly = it.filter { it.isDigit() }
                            if (digitsOnly.length <= 16) {
                                cardNumber = digitsOnly
                            }
                        },
                        textStyle = TextStyle(
                            color = Color(0xFF4A148C),
                            fontSize = 18.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true
                    )
                    Divider(color = Color(0xFF4A148C), thickness = 1.dp)
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
                            color = Color(0xFF4A148C)
                        )
                        BasicTextField(
                            value = expiryDate,
                            onValueChange = { 
                                val digitsOnly = it.filter { it.isDigit() }
                                if (digitsOnly.length <= 4) {
                                    expiryDate = formatExpiryDate(digitsOnly)
                                }
                            },
                            textStyle = TextStyle(
                                color = Color(0xFF4A148C),
                                fontSize = 18.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                        Divider(color = Color(0xFF4A148C), thickness = 1.dp)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CVV",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4A148C)
                        )
                        BasicTextField(
                            value = cvv,
                            onValueChange = { if (it.length <= 3) cvv = it },
                            textStyle = TextStyle(
                                color = Color(0xFF4A148C),
                                fontSize = 18.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                        Divider(color = Color(0xFF4A148C), thickness = 1.dp)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Top-Up Amount",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4A148C)
            )
            BasicTextField(
                value = topUpAmount,
                onValueChange = { topUpAmount = it },
                textStyle = TextStyle(
                    color = Color(0xFF4A148C),
                    fontSize = 18.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            )
        }

        Button(
            onClick = {
                val amount = topUpAmount.toDoubleOrNull()
                if (amount == null) {
                    Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val cardDigits = cardNumber.filter { it.isDigit() }
                if (cardDigits.length != 16) {
                    Toast.makeText(context, "Please enter a valid card number", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (cvv.length != 3) {
                    Toast.makeText(context, "Please enter a valid CVV", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val expiryDigits = expiryDate.filter { it.isDigit() }
                if (expiryDigits.length != 4) {
                    Toast.makeText(context, "Please enter expiry date in MM/YY format", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val monthStr = expiryDigits.take(2)
                val yearStr = expiryDigits.drop(2)
                val expiryMonth = monthStr.toIntOrNull()
                val expiryYear = yearStr.toIntOrNull()?.let { 2000 + it }

                if (expiryMonth == null || expiryYear == null || 
                    expiryMonth < 1 || expiryMonth > 12) {
                    Toast.makeText(context, "Please enter a valid expiry date", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch {
                    try {
                        walletViewModel.setLoading(true)
                        walletViewModel.createPayment(
                            amount = amount,
                            onSuccess = { clientSecret ->
                                // Clear fields on success
                                topUpAmount = ""
                                cardNumber = ""
                                expiryDate = ""
                                cvv = ""
                                Toast.makeText(context, "Payment successful!", Toast.LENGTH_SHORT).show()
                                walletViewModel.refreshBalance { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Network error: Please check your connection",
                            Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        walletViewModel.setLoading(false)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A148C)
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

@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Text(text = text)
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
