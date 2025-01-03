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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.act22.activity.Screen
import com.example.act22.data.model.Crypto
import com.example.act22.viewmodel.PortfolioViewModel
import com.example.act22.viewmodel.TradingViewModel
import com.example.act22.viewmodel.UserPlanViewModel
import com.example.act22.viewmodel.AssetPriceViewModel
import com.example.act22.network.GraphDataPoint
import kotlinx.coroutines.delay
import com.example.act22.ui.components.DrawChart
import com.example.act22.ui.components.ChartPoint
import com.example.act22.ui.components.DrawChartWithTimestamps
import androidx.compose.runtime.DisposableEffect

@Composable
fun UserPortfolio(
    navController: NavController,
    portfolioViewModel: PortfolioViewModel = PortfolioViewModel()
) {
    MainScaffold(navController) {
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
                PortfolioOverview(
                    navController,
                    portfolioViewModel
                )
                PortfolioTabs(
                    navController,
                    portfolioViewModel
                )
            }
        }
    }
}

@Composable
fun PortfolioTabs(
    navController: NavController,
    portfolioViewModel: PortfolioViewModel,
    userPlanViewModel: UserPlanViewModel = viewModel()
) {
    val isPremium by userPlanViewModel.userPlan.collectAsState()

        var selectedTab by remember { mutableIntStateOf(0) }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My assets") }
                )
                if(isPremium){
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("AI Tips") }
                    )
                }
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
    val walletBalance by portfolioViewModel.walletBalance.collectAsState()
    
    // Update prices when the component is first displayed
    LaunchedEffect(Unit) {
        portfolioViewModel.refreshPortfolio()
    }
    
    // Set up periodic updates (every 60 seconds)
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            portfolioViewModel.refreshPortfolio()
        }
    }

    val totalPortfolioValue = portfolio.techStocks.sumOf { it.price * it.quantity } + 
                             portfolio.cryptos.sumOf { it.price * it.quantity }
    val techStockCount = portfolio.techStocks.size
    val cryptoCount = portfolio.cryptos.size

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(bottom = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            Text(
                text = "Total Value: $${String.format("%.2f", totalPortfolioValue)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "Tech Stocks: $techStockCount | Cryptos: $cryptoCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            WalletInformation(walletBalance, navController)
        }
}

@Composable
fun WalletInformation(
    walletBalance: Double,
    navController: NavController
){
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clickable { navController.navigate(Screen.Profile.route) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = "Wallet balance: \$${String.format("%.2f", walletBalance)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Start Trading",
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(15.dp)
        )
    }

}


@Composable
fun PortfolioAssets(
    navController: NavController,
    portfolioViewModel: PortfolioViewModel
) {
    val portfolio by portfolioViewModel.portfolioState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 25.dp),
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
    var tradingMode by remember { mutableStateOf(true) }
    val tradingViewModel: TradingViewModel = viewModel()
    val assetPriceViewModel: AssetPriceViewModel = remember { AssetPriceViewModel() }
    val chartUiState by assetPriceViewModel.chartUiState.collectAsState()

    LaunchedEffect(asset.ID) {
        assetPriceViewModel.fetchAssetInformation(
            id = asset.ID,
            isCrypto = asset is Crypto,
            shouldUpdateGraph = true
        )
    }

    // Cleanup when the composable is disposed
    DisposableEffect(assetPriceViewModel) {
        onDispose {
            assetPriceViewModel.stopGraphUpdates()
        }
    }

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
        Column{
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                when (chartUiState) {
                    is AssetPriceViewModel.ChartUiState.Loading -> {
                        LoadingSpinner()
                    }
                    is AssetPriceViewModel.ChartUiState.Error -> {
                        ErrorMessage("-")
                    }
                    is AssetPriceViewModel.ChartUiState.Success -> {
                        val points = (chartUiState as AssetPriceViewModel.ChartUiState.Success).points
                        if (points.isEmpty()) {
                            ErrorMessage("-")
                        } else {
                            val chartPoints = points.map { ChartPoint(it.timestamp, it.price) }
                            DrawChart(
                                points = chartPoints,
                                lineColor = MaterialTheme.colorScheme.tertiary,
                                pointColor = MaterialTheme.colorScheme.secondary,
                                pointRadius = 2f
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                ) {
                    Button(
                        onClick = {
                            showTradingDialog = true
                            tradingMode = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.height(60.dp)
                    ) {
                        Text("Buy")
                    }
                    Button(
                        onClick = {
                            showTradingDialog = true
                            tradingMode = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.height(60.dp)
                    ) {
                        Text("Sell")
                    }
                }
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
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
                }
            }
        }
    }

    if (showTradingDialog) {
        TradingDialog(
            asset = asset,
            isInPortfolio = true,
            onDismiss = { showTradingDialog = false },
            tradingViewModel = tradingViewModel,
            portfolioViewModel = viewModel(),
            isBuyingMode = tradingMode
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
                .padding(10.dp),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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



