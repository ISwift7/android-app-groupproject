package com.example.act22.ui.pages.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    portfolioViewModel: PortfolioViewModel = viewModel(),
    isBuyingMode: Boolean = true //true for buy, false for sell
) {
    var quantity by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Refresh portfolio when dialog opens
    LaunchedEffect(Unit) {
        portfolioViewModel.refreshPortfolio()
    }

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
    fun validateAndTrade(isBuying: Boolean) {
        val qty = quantity.toDoubleOrNull() ?: 0.0
        if (qty <= 0) {
            errorMessage = "Please enter a valid quantity"
            return
        }
        if (!isBuying && qty > ownedQuantity) {
            errorMessage = "Cannot sell more than owned quantity (${String.format("%.4f", ownedQuantity)})"
            return
        }

        scope.launch {
            if (!isBuying) {
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
        modifier = Modifier.fillMaxWidth(),
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface ,
        title = {
            Text(
                text = if (isBuyingMode) "Buy ${asset.name}" else "Sell ${asset.name}",
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                if (isInPortfolio) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp))
                                .padding(5.dp)
                            ,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Currently Owned",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "${String.format("%.4f", ownedQuantity)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                }
                Text(
                    text = "Current Price: $${String.format("%.2f", asset.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(5.dp)
                )
                TextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it.filter { char -> char.isDigit() || char == '.' }
                        errorMessage = null
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    isError = errorMessage != null,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                quantity.toDoubleOrNull()?.let { qty ->
                    Text(
                        text = "Total: $${String.format("%.2f", qty * asset.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top=10.dp)
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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (isBuyingMode){
                    Button(
                        onClick = { validateAndTrade(true) },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Buy")
                    }
                }
                if (isInPortfolio && !isBuyingMode) {
                    Button(
                        onClick = { validateAndTrade(false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Sell")
                    }
                }
            }
        }
    )
}