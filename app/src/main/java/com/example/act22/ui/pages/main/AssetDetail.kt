package com.example.act22.ui.pages.main

import com.example.act22.viewmodel.AIViewModel
import com.example.act22.data.model.Asset
import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.act22.data.model.Crypto
import com.example.act22.data.model.TechStock
import com.example.act22.viewmodel.AssetPriceViewModel
import com.example.act22.viewmodel.PortfolioViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.round
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.act22.ui.components.TradingDialog
import com.example.act22.viewmodel.TradingViewModel
import com.example.act22.network.GraphDataPoint
import com.example.act22.viewmodel.PriceAlertViewModel
import com.example.act22.data.model.PriceAlert

@Composable
fun AssetDetails(
    navController: NavController,
    assetPriceViewModel: AssetPriceViewModel = viewModel(),
    portfolioViewModel: PortfolioViewModel = viewModel(),
    aiViewModel: AIViewModel = viewModel(),
    assetId: String
) {
    val assetUiState by assetPriceViewModel.assetUiState.collectAsState()
    val chartUiState by assetPriceViewModel.chartUiState.collectAsState()

    // Initial fetch of asset info
    LaunchedEffect(assetId) {
        assetPriceViewModel.fetchAssetInformation(
            id = assetId,
            isCrypto = false,
            shouldUpdateGraph = false  // Don't start graph updates yet
        )
    }

    // Update isCrypto and start graph updates when we get the asset info
    LaunchedEffect(assetUiState) {
        if (assetUiState is AssetPriceViewModel.AssetUiState.Success) {
            val asset = (assetUiState as AssetPriceViewModel.AssetUiState.Success).asset
            // Start continuous graph updates
            assetPriceViewModel.startGraphUpdates(assetId, asset is Crypto)
        }
    }

    // Clean up graph updates when leaving the screen
    DisposableEffect(assetId) {
        onDispose {
            assetPriceViewModel.stopGraphUpdates()
        }
    }

    MainScaffold(navController) {
        when (assetUiState) {
            is AssetPriceViewModel.AssetUiState.Loading -> LoadingSpinner()
            is AssetPriceViewModel.AssetUiState.Error -> ErrorMessage((assetUiState as AssetPriceViewModel.AssetUiState.Error).message)
            is AssetPriceViewModel.AssetUiState.Success -> {
                val asset = (assetUiState as AssetPriceViewModel.AssetUiState.Success).asset
                AssetDetailsColumn(
                    portfolioViewModel,
                    aiViewModel,
                    asset,
                    chartUiState,
                    navController
                )
            }
        }
    }
}

@Composable
fun AssetDetailsColumn(
    portfolioViewModel: PortfolioViewModel,
    aiViewModel: AIViewModel,
    asset: Asset,
    chartUiState: AssetPriceViewModel.ChartUiState,
    navController: NavController
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 90.dp, bottom = 80.dp)
        ) {
            AssetChart(chartUiState)
            AssetDetailsTabs(
                aiViewModel,
                asset,
                true
            ) //todo dynamic
        }

        AssetHeader(asset, Modifier.align(Alignment.TopCenter))
        AssetActions(portfolioViewModel, asset, Modifier.align(Alignment.BottomCenter), navController)
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun AssetHeader(
    asset: Asset,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 40.dp, vertical = 20.dp)
            .padding(top = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = asset.ID,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary
        )

        VerticalDivider(
            modifier = Modifier.height(35.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )

        Text(
            text = "$${String.format("%.2f", asset.price)}",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onPrimary
        )

    }
}

@Composable
fun AssetActions(
    portfolioViewModel: PortfolioViewModel,
    asset: Asset,
    modifier: Modifier,
    navController: NavController
) {
    var showTradingDialog by remember { mutableStateOf(false) }
    val tradingViewModel: TradingViewModel = viewModel()
    val isInPortfolio = portfolioViewModel.isAssetInPortfolio(asset)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(top = 5.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = if (isInPortfolio) Arrangement.SpaceBetween else Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        Button(
            onClick = { showTradingDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(if (isInPortfolio) "Buy More" else "Buy")
        }
        if (isInPortfolio) {
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { navController.navigate("portfolio") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Go to Portfolio to Sell")
            }
        }
    }

    if (showTradingDialog) {
        TradingDialog(
            asset = asset,
            isInPortfolio = false, // Always show buy dialog
            onDismiss = { showTradingDialog = false },
            tradingViewModel = tradingViewModel,
            portfolioViewModel = portfolioViewModel
        )
    }
}

@Composable
fun AssetChart(
    chartUiState: AssetPriceViewModel.ChartUiState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        when (chartUiState) {
            is AssetPriceViewModel.ChartUiState.Loading -> {
                StockChartPlaceholder(height = 300.dp)
            }
            is AssetPriceViewModel.ChartUiState.Error -> {
                StockChartPlaceholder(height = 300.dp)
            }
            is AssetPriceViewModel.ChartUiState.Success -> {
                StockChartPlaceholder(
                    height = 300.dp,
                    dataPoints = (chartUiState as AssetPriceViewModel.ChartUiState.Success).points
                )
            }
        }
    }
}

@Composable
fun DrawChart(
    pricePoints: List<Double>,
    lineColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    pointColor: Color = MaterialTheme.colorScheme.onBackground,
    pointRadius: Float = 6f
) {
    val pricePointsFloat = pricePoints.map { it.toFloat() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (pricePointsFloat.isNotEmpty()) {
                val spacing = canvasWidth / (pricePointsFloat.size - 1)

                val minPrice = pricePointsFloat.minOrNull() ?: 0f
                val maxPrice = pricePointsFloat.maxOrNull() ?: 0f
                val priceRange = maxPrice - minPrice

                val path = Path().apply {
                    pricePointsFloat.forEachIndexed { index, price ->
                        val x = index * spacing
                        val y = canvasHeight - ((price - minPrice) / priceRange * canvasHeight)
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 5f)
                )

                pricePointsFloat.forEachIndexed { index, price ->
                    val x = index * spacing
                    val y = canvasHeight - ((price - minPrice) / priceRange * canvasHeight)
                    drawCircle(
                        color = pointColor,
                        radius = pointRadius,
                        center = Offset(x, y)
                    )
                }
            }
        }

        if (pricePoints.isEmpty()) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AssetDetailsTabs(
    aiViewModel: AIViewModel,
    asset: Asset,
    isPremiumUser: Boolean
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = if (isPremiumUser) {
        listOf("Details & Analytics", "AI Predictions", "Price alerts")
    } else {
        listOf("Details")
    }

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.height(40.dp),
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, style = MaterialTheme.typography.bodySmall) }
                )
            }
        }

        when (tabs[selectedTabIndex]) {
            "Details & Analytics" -> AssetAnalyticsTabs(aiViewModel, asset)
            "AI Predictions" -> AssetPredictions(aiViewModel, asset)
            "Price alerts" -> AssetPriceAlerts(asset = asset, viewModel = viewModel())
        }
    }
}

@Composable
fun AssetAnalyticsTabs(
    aiViewModel: AIViewModel,
    asset: Asset
){
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Details", "Analytics")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                modifier = Modifier.height(40.dp),
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(title, style = MaterialTheme.typography.bodySmall) }
            )
        }
    }

    when (tabs[selectedTabIndex]) {
        "Details" -> AssetBasicDetails(asset)
        "Analytics" -> AssetAnalytics(aiViewModel, asset)
    }

}

@Composable
fun AssetBasicDetails(
    asset: Asset
) {
    Box(
        Modifier.fillMaxWidth(),
        Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(10.dp),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .defaultMinSize(minWidth = 170.dp)
                    .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AssetImage(asset)
                AssetBasicDetailsText("Name : ${asset.name}")
                AssetBasicDetailsText("Price : $${String.format("%.2f", asset.price)}")
                when (asset) {
                    is TechStock -> {
                        AssetBasicDetailsText("Low : $${String.format("%.2f", asset.low)}")
                        AssetBasicDetailsText("High : $${String.format("%.2f", asset.high)}")
                        AssetBasicDetailsText("Open : $${String.format("%.2f", asset.open)}")
                        AssetBasicDetailsText("Previous Close : $${String.format("%.2f", asset.previous_close)}")
                    }
                    is Crypto -> {
                        AssetBasicDetailsText("Low : $${String.format("%.2f", asset.low)}")
                        AssetBasicDetailsText("High : $${String.format("%.2f", asset.high)}")
                        AssetBasicDetailsText("Previous Close : $${String.format("%.2f", asset.previous_close)}")
                    }

                    is Crypto -> TODO()
                    is TechStock -> TODO()
                }
            }
        }
    }
}

@Composable
fun AssetBasicDetailsText(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.None),
        modifier = Modifier.padding(5.dp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun AssetAnalytics(
    aiViewModel: AIViewModel,
    asset: Asset
) {
    val analysisState by aiViewModel.analysisState.collectAsState()

    DisposableEffect(asset.ID) {
        aiViewModel.fetchAssetAnalysis(asset.ID)
        onDispose {
            aiViewModel.resetAnalysisState()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minWidth = 170.dp)
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Curve AI predicts that...",
                style = MaterialTheme.typography.titleSmall.copy(textDecoration = TextDecoration.None),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            when (analysisState) {
                is AIViewModel.UiState.Error -> ErrorMessage((analysisState as AIViewModel.UiState.Error).message)
                is AIViewModel.UiState.Success -> {
                    val aiAnalysisText = (analysisState as AIViewModel.UiState.Success).data[0]
                    TypingTextAnimation(
                        fullText = aiAnalysisText,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
                else -> LoadingSpinner()
            }
        }
    }
}


@Composable
fun TypingTextAnimation(
    fullText: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.None),
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Justify,
    modifier: Modifier = Modifier,
    typingSpeed: Long = 25L,
    onComplete: () -> Unit = {}
) {
    var displayedText by remember { mutableStateOf("") }
    val job = rememberCoroutineScope()

    LaunchedEffect(fullText) {
        job.launch {
            displayedText = ""
            for (i in fullText.indices) {
                displayedText = fullText.substring(0, i + 1)
                delay(typingSpeed)
            }
            onComplete()
        }
    }

    DisposableEffect(fullText) {
        onDispose { job.cancel() }
    }

    Text(
        text = displayedText,
        style = style.copy(textDecoration = TextDecoration.None),
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}


@Composable
fun AssetPredictions(
    aiViewModel: AIViewModel,
    asset: Asset
) {
    val predictionState by aiViewModel.predictionState.collectAsState()

    DisposableEffect(asset.ID) {
        aiViewModel.fetchAssetPricePrediction(asset.ID)
        onDispose {
            aiViewModel.resetPredictionState()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minWidth = 170.dp)
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Curve AI Price Prediction",
                style = MaterialTheme.typography.titleSmall.copy(textDecoration = TextDecoration.None),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            when (predictionState) {
                is AIViewModel.UiState.Error -> ErrorMessage((predictionState as AIViewModel.UiState.Error).message)
                is AIViewModel.UiState.Success -> {
                    val predictions = (predictionState as AIViewModel.UiState.Success).data
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        predictions.forEach { prediction ->
                            TypingTextAnimation(
                                fullText = prediction,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                }
                else -> LoadingSpinner()
            }
        }
    }
}


@Composable
fun AssetPriceAlerts(
    asset: Asset,
    viewModel: PriceAlertViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val priceAlerts by viewModel.priceAlerts.collectAsState()
    val error by viewModel.error.collectAsState()

    // Fetch alerts when the component is first displayed
    LaunchedEffect(asset.ID) {
        viewModel.fetchPriceAlertsForAsset(asset)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Receive notification\nwhen price reaches a set price point",
            style = MaterialTheme.typography.titleSmall.copy(textDecoration = TextDecoration.None),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "You can set up to 3 price alerts",
            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.None),
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Saved price alerts",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                PriceAlertTable(
                    priceAlerts = priceAlerts,
                    onDeleteAlert = { alert -> viewModel.deletePriceAlert(alert, asset) }
                )
            }

            if (priceAlerts.size < 3) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Set alert",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .clip(RoundedCornerShape(25))
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(5.dp)
                        .clickable { showDialog = true },
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    if (showDialog) {
        AddPriceAlertDialog(
            asset = asset,
            onDismiss = { showDialog = false },
            onConfirm = { price, alertType ->
                viewModel.addPriceAlert(asset, price, alertType)
                showDialog = false
            }
        )
    }

    error?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun PriceAlertTable(
    priceAlerts: List<PriceAlert>,
    onDeleteAlert: (PriceAlert) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Type",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Target",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Status",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(48.dp)) // Space for delete button
        }

        // Alert rows
        priceAlerts.forEach { alert ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.alert_type.capitalize(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$${String.format("%.2f", alert.target_price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = alert.status.capitalize(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onDeleteAlert(alert) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Alert",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Empty state
        if (priceAlerts.isEmpty()) {
            Text(
                text = "No price alerts set",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AddPriceAlertDialog(
    asset: Asset,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var enteredPrice by remember { mutableStateOf("") }
    var selectedAlertType by remember { mutableStateOf("above") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Add Price Alert",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Current price: $${String.format("%.2f", asset.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Alert type selection
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Text(
                        "Alert type",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Above price option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedAlertType = "above" }
                                .background(
                                    if (selectedAlertType == "above")
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAlertType == "above",
                                onClick = { selectedAlertType = "above" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Above price",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedAlertType == "above")
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Below price option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedAlertType = "below" }
                                .background(
                                    if (selectedAlertType == "below")
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAlertType == "below",
                                onClick = { selectedAlertType = "below" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Below price",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedAlertType == "below")
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Price input
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = enteredPrice,
                        onValueChange = { enteredPrice = it },
                        label = { Text("Target Price") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        prefix = { Text("$", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    enteredPrice.toDoubleOrNull()?.let { price ->
                        onConfirm(price, selectedAlertType)
                    }
                },
                enabled = enteredPrice.toDoubleOrNull() != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                Text(
                    "Add Alert",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    )
}

@Composable
fun StockChartPlaceholder(
    height: Dp,
    dataPoints: List<GraphDataPoint> = emptyList()
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        if (dataPoints.isEmpty()) {
            CircularProgressIndicator()
        } else {
            // Draw the chart with the data points
            val pricePoints = dataPoints.map { it.price }
            DrawChart(pricePoints = pricePoints)
        }
    }
}
