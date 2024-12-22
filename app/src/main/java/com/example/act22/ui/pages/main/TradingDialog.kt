package com.example.act22.ui.pages.main

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.act22.data.model.Asset
import com.example.act22.viewmodel.PortfolioViewModel
import com.example.act22.viewmodel.TradingViewModel
import kotlinx.coroutines.launch

@Composable
fun TradingDialog(
    asset: Asset,
    isInPortfolio: Boolean,
    onDismiss: () -> Unit,
    tradingViewModel: TradingViewModel,
    portfolioViewModel: PortfolioViewModel = viewModel()
) {
    var quantity by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Refresh portfolio when dialog opens
    LaunchedEffect(Unit) {
        portfolioViewModel.refreshPortfolio()
    }
    
    // Collect portfolio state
    val portfolioState by portfolioViewModel.portfolioState.collectAsState()
    val ownedQuantity = remember(portfolioState) {
        portfolioViewModel.getAssetQuantity(asset.ID)
    }

    // Handle trading completion
    fun handleTradingCompletion(success: Boolean, message: String) {
        if (success) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // Refresh portfolio data
            scope.launch {
                portfolioViewModel.refreshPortfolio()
            }
            onDismiss()
        } else {
            errorMessage = message
        }
    }

    // Validate quantity before trading
    fun validateAndTrade(isSelling: Boolean) {
        val qty = quantity.toDoubleOrNull() ?: 0.0
        if (qty <= 0) {
            errorMessage = "Please enter a valid quantity"
            return
        }
        if (isSelling && qty > ownedQuantity) {
            errorMessage = "Cannot sell more than owned quantity (${String.format("%.4f", ownedQuantity)})"
            return
        }
        
        scope.launch {
            if (isSelling) {
                portfolioViewModel.sellAsset(asset, qty) { success, message ->
                    handleTradingCompletion(success, message)
                }
            } else {
                portfolioViewModel.buyAsset(asset, qty) { success, message ->
                    handleTradingCompletion(success, message)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isInPortfolio) "Trade ${asset.name}" else "Buy ${asset.name}",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                if (isInPortfolio) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Currently Owned",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.4f", ownedQuantity),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = "Current Price: $${String.format("%.2f", asset.price)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        quantity = it.filter { char -> char.isDigit() || char == '.' }
                        errorMessage = null // Clear error when user types
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    isError = errorMessage != null
                )
                quantity.toDoubleOrNull()?.let { qty ->
                    Text(
                        text = "Total: $${String.format("%.2f", qty * asset.price)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isInPortfolio) Arrangement.SpaceEvenly else Arrangement.End
            ) {
                if (isInPortfolio) {
                    Button(
                        onClick = { validateAndTrade(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Sell")
                    }
                }
                Button(
                    onClick = { validateAndTrade(false) }
                ) {
                    Text(if (isInPortfolio) "Buy More" else "Buy")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 