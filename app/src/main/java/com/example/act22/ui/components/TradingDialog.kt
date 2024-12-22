package com.example.act22.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.act22.data.model.Asset
import com.example.act22.data.model.Crypto
import com.example.act22.viewmodel.TradingState
import com.example.act22.viewmodel.TradingViewModel

@Composable
fun TradingDialog(
    asset: Asset,
    isInPortfolio: Boolean,
    onDismiss: () -> Unit,
    tradingViewModel: TradingViewModel
) {
    var quantity by remember { mutableStateOf("") }
    val tradingState by tradingViewModel.tradingState.collectAsState()
    val currentPrice = asset.price

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isInPortfolio) "Sell ${asset.name}" else "Buy ${asset.name}",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Current Price: $${String.format("%.2f", currentPrice)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Total: $${String.format("%.2f", quantity.toDoubleOrNull()?.times(currentPrice) ?: 0.0)}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (tradingState) {
                    is TradingState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is TradingState.Success -> {
                        Text(
                            text = (tradingState as TradingState.Success).message,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LaunchedEffect(Unit) {
                            onDismiss()
                        }
                    }
                    is TradingState.Error -> {
                        Text(
                            text = (tradingState as TradingState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        if (isInPortfolio) {
                            Button(
                                onClick = {
                                    val quantityValue = quantity.toDoubleOrNull()
                                    if (quantityValue != null && quantityValue > 0) {
                                        tradingViewModel.sellAsset(
                                            asset.ID,
                                            asset is Crypto,
                                            quantityValue,
                                            currentPrice
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sell")
                            }
                        } else {
                            Button(
                                onClick = {
                                    val quantityValue = quantity.toDoubleOrNull()
                                    if (quantityValue != null && quantityValue > 0) {
                                        tradingViewModel.buyAsset(
                                            asset.ID,
                                            asset is Crypto,
                                            quantityValue,
                                            currentPrice
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Buy")
                            }
                        }
                    }
                }
            }
        }
    }
} 