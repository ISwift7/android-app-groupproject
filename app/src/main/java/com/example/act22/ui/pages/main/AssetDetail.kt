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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.act22.viewmodel.AssetPriceViewModel
import com.example.act22.viewmodel.PortfolioViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.round

@Composable
fun AssetDetails(
    navController: NavController,
    assetPriceViewModel: AssetPriceViewModel,
    portfolioViewModel: PortfolioViewModel,
    aiViewModel: AIViewModel,
    assetId: String
) {
    val assetUiState by assetPriceViewModel.assetUiState.collectAsState()
    val chartUiState by assetPriceViewModel.chartUiState.collectAsState()

    LaunchedEffect(assetId) {
        assetPriceViewModel.fetchAssetInformation(assetId)
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
                    chartUiState
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
    chartUiState: AssetPriceViewModel.ChartUiState
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
        AssetActions(portfolioViewModel, asset, Modifier.align(Alignment.BottomCenter))
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
    modifier: Modifier
){
    var isInPortfolio = false;

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(top = 5.dp)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Top
    ){
        if (isInPortfolio) {
            SmallButton(
                title = "Buy",
                onClick = {},
                modifier = Modifier.weight(0.5f),
                color = Color(0xFF058A00)
            )

            SmallButton(
                title = "Sell",
                onClick = {},
                modifier = Modifier.weight(0.5f),
                color = Color(0xFFD30000)
            )
        } else {
            SmallButton(
                title = "Add to portfolio",
                onClick = {},
                modifier = Modifier
                    .weight(1f)
            )
        }
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
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 20.dp)
            .padding(end = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        when (chartUiState) {
            is AssetPriceViewModel.ChartUiState.Loading -> LoadingSpinner()
            is AssetPriceViewModel.ChartUiState.Error -> ErrorMessage(
                (chartUiState as AssetPriceViewModel.ChartUiState.Error).message
            )
            is AssetPriceViewModel.ChartUiState.Success -> {
                DrawChart((chartUiState as AssetPriceViewModel.ChartUiState.Success).pricePoints)
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

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.onSecondary // Custom indicator color
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) }
                )
            }
        }

        when (tabs[selectedTabIndex]) {
            "Details" -> AssetBasicDetails(asset)
            "Details & Analytics" -> AssetAnalyticsTabs(aiViewModel, asset)
            "AI Predictions" -> AssetPredictions(aiViewModel, asset)
            "Price alerts" -> AssetPriceAlerts(asset)
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
                AssetBasicDetailsText("Low : ${String.format("%.2f", asset.price * 0.5)}")
                AssetBasicDetailsText("High : ${String.format("%.2f", asset.price * 1.7)}")
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
        style = MaterialTheme.typography.bodyMedium,
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
                text = "Curve AI analyzed that...",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
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
    style: TextStyle = MaterialTheme.typography.bodyMedium,
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
        style = style,
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
                text = "Curve AI predicts that...",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            when (predictionState) {
                is AIViewModel.UiState.Error -> ErrorMessage((predictionState as AIViewModel.UiState.Error).message)
                is AIViewModel.UiState.Success -> {
                    val aiPredictionText = (predictionState as AIViewModel.UiState.Success).data[0]
                    val aiPredictionExplanation = (predictionState as AIViewModel.UiState.Success).data[1]

                    var isComplete by remember { mutableStateOf(false) }
                    TypingTextAnimation(
                        fullText = aiPredictionText,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        onComplete = {isComplete = true}
                    )

                    if(isComplete){
                        TypingTextAnimation(
                            fullText = aiPredictionExplanation,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
                else -> LoadingSpinner()
            }
        }
    }
}


@Composable
fun AssetPriceAlerts(
    asset: Asset
) {
    var showDialog by remember { mutableStateOf(false) }
    var priceAlerts by remember { mutableStateOf(listOf<PriceAlert>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Receive notification\nwhen price reaches a set price point",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "You can set up to 3 price alerts",
            style = MaterialTheme.typography.bodySmall,
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
                PriceAlertTable(priceAlerts = priceAlerts)
            }

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

    if (showDialog) {
        AddPriceAlertDialog(
            onDismiss = { showDialog = false },
            onConfirm = { price ->
                priceAlerts = priceAlerts + PriceAlert(date = "Today", price = price)
                showDialog = false
            }
        )
    }
}


@Composable
fun PriceAlertTable(
    priceAlerts: List<PriceAlert>
) {
    val maxRows = 3

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("No.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            Text("Date", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2.5f))
            Text("Price", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
            Spacer(Modifier.weight(0.5f))
        }

        repeat(maxRows) { index ->
            val alert = priceAlerts.getOrNull(index)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (alert != null) "${index + 1}" else "-",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(0.8f)
                )
                Text(
                    text = alert?.date ?: "--/--/--",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(2.5f)
                )
                Text(
                    text = alert?.price?.toString() ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(2f)
                )
                if (alert != null) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Alert",
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(25.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.error)
                            .padding(5.dp)

                            .clickable { /* Handle delete action */ },
                        tint = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Spacer(Modifier.weight(0.5f))
                }
            }
        }
    }
}


@Composable
fun AddPriceAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var enteredPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text("Add Price Alert", style = MaterialTheme.typography.titleSmall)
        },
        text = {
            Column {
                Text("Enter the price for the alert:", color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = enteredPrice,
                    onValueChange = { enteredPrice = it },
                    label = { Text("Price") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = enteredPrice.toDoubleOrNull()
                    if (price != null) {
                        onConfirm(price)
                    }
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}


// Sample Data Class for PriceAlert
data class PriceAlert(val date: String, val price: Double)
