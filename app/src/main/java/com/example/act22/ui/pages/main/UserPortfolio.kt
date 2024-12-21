package com.example.act22.ui.pages.main

import com.example.act22.data.model.Asset
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.act22.activity.Screen
import com.example.act22.viewmodel.PortfolioViewModel
import com.example.act22.viewmodel.TradingViewModel
import kotlinx.coroutines.delay

@Composable
fun UserPortfolio(
    navController: NavController,
    portfolioViewModel: PortfolioViewModel = PortfolioViewModel()
) {
    MainScaffold(navController) {
        val portfolio by portfolioViewModel.portfolioState.collectAsState()
        val walletBalance by portfolioViewModel.walletBalance.collectAsState()
        
        // Update portfolio data when the component is first displayed
        LaunchedEffect(Unit) {
            portfolioViewModel.refreshPortfolio()
        }
        
        if (portfolioViewModel.isPortfolioEmpty()) {
            EmptyPage(
                text = "Your portfolio is empty",
                buttonText = "Start Trading",
                onClick = { navController.navigate(Screen.MainPage.route) }
            )
        } else {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Display wallet balance
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Wallet Balance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", walletBalance)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                PortfolioOverview(
                    navController,
                    portfolioViewModel
                )
                PortfolioTabs(navController, portfolioViewModel)
            }
        }
    }
}

@Composable
fun PortfolioTabs(
    navController: NavController,
    portfolioViewModel: PortfolioViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("My assets") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("AI Tips") }
            )
        }

            when (selectedTab) {
                0 -> PortfolioAssets(navController, portfolioViewModel)
                1 -> AITab()
            }

    }
}


@SuppressLint("DefaultLocale")
@Composable
fun PortfolioOverview(
    navController: NavController,
    portfolioViewModel: PortfolioViewModel
) {
    val portfolio by portfolioViewModel.portfolioState.collectAsState()
    
    // Update prices when the component is first displayed
    LaunchedEffect(Unit) {
        portfolioViewModel.refreshPortfolio()
    }
    
    // Set up periodic updates (every 30 seconds)
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000) // 30 seconds
            portfolioViewModel.refreshPortfolio()
        }
    }

    val totalPortfolioValue = portfolio.techStocks.sumOf { it.price * it.quantity } + 
                             portfolio.cryptos.sumOf { it.price * it.quantity }
    val techStockCount = portfolio.techStocks.size
    val cryptoCount = portfolio.cryptos.size

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display total portfolio value
            Text(
                text = "Total Value: $${String.format("%.2f", totalPortfolioValue)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Display count of stocks and cryptos
            Text(
                text = "Tech Stocks: $techStockCount | Cryptos: $cryptoCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 20.dp),
            contentAlignment = Alignment.BottomEnd
        ){
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Start Trading",
                modifier = Modifier
                    .size(35.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(5.dp)
                    .clickable { navController.navigate(Screen.MainPage.route) },
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


@Composable
fun PortfolioAssets(
    navController: NavController,
    portfolioViewModel: PortfolioViewModel
) {
    val portfolio by portfolioViewModel.portfolioState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Stocks",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        portfolio.techStocks.forEach { stock ->
            PortfolioAssetCard(navController, stock)
        }
        
        if (portfolio.cryptos.isNotEmpty()) {
            Text(
                text = "Cryptocurrencies",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            portfolio.cryptos.forEach { crypto ->
                PortfolioAssetCard(navController, crypto)
            }
        }
    }
}

@Composable
fun PortfolioAssetCard(
    navController: NavController,
    asset: Asset
){
    var showTradingDialog by remember { mutableStateOf(false) }
    val tradingViewModel: TradingViewModel = viewModel()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { navController.navigate(Screen.AssetDetails.createRoute(asset.ID)) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        StockChartPlaceholder(100.dp)
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = asset.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Quantity: ${String.format("%.4f", asset.quantity)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Price: $${String.format("%.2f", asset.price)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Total Value: $${String.format("%.2f", asset.price * asset.quantity)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Trading buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { showTradingDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Buy More")
                }
                Button(
                    onClick = { showTradingDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sell")
                }
            }
        }
    }

    if (showTradingDialog) {
        TradingDialog(
            asset = asset,
            isInPortfolio = true,
            onDismiss = { showTradingDialog = false },
            tradingViewModel = tradingViewModel
        )
    }
}

@Composable
fun AITab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AI Tips and Predictions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Coming soon...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}



