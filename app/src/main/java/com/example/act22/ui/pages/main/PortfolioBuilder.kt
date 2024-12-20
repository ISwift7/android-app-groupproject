package com.example.act22.ui.pages.main

import com.example.act22.data.model.Asset
import android.widget.Toast
import com.example.act22.ui.pages.authentication.DrawLogo

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.act22.activity.Screen
import com.example.act22.viewmodel.PortfolioViewModel
import com.example.act22.data.model.AssetType
import com.example.act22.data.repository.AssetRepositoryFirebaseImpl

@Composable
fun PortfolioBuildingPage(
    navController: NavController,
    portfolioViewModel: PortfolioViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Top
    ) {
        LogoBox()
        InfoBox()
        PortfolioBuilder(
            modifier = Modifier.weight(1f),
            portfolioViewModel = portfolioViewModel
        )
        BigButton(
            prompt = "Save",
            onClick = {navController.navigate(Screen.Portfolio.route)}
        )
    }
}

@Composable
fun LogoBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        DrawLogo(75.dp)
    }
}

@Composable
fun InfoBox() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp)
            .shadow(
                elevation = 100.dp,
                shape = RoundedCornerShape(5.dp),
                ambientColor = MaterialTheme.colorScheme.tertiary,
                spotColor = MaterialTheme.colorScheme.secondary
            )
            .padding(bottom = 15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Build your portfolio",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "You can choose up to 10 technology stocks and 3 crypto assets.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PortfolioBuilder(
    modifier: Modifier,
    portfolioViewModel: PortfolioViewModel
) {
    val listState = rememberLazyListState()
    val assetRepository = remember { AssetRepositoryFirebaseImpl() }
    val stocksState = remember { mutableStateOf<List<Asset>>(emptyList()) }
    val cryptosState = remember { mutableStateOf<List<Asset>>(emptyList()) }

    LaunchedEffect(Unit) {
        stocksState.value = assetRepository.filterAssetsByType(AssetType.STOCK)
        cryptosState.value = assetRepository.filterAssetsByType(AssetType.CRYPTO)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        state = listState
    ) {
        stickyHeader { StickyStockHeader("Tech Stocks") }
        items(
            items = stocksState.value,
            key = { it.ID }
        ) { stock ->
            ToggleAssetCard(
                asset = stock,
                portfolioViewModel = portfolioViewModel
            )
        }

        stickyHeader { StickyStockHeader("Cryptos") }
        items(
            items = cryptosState.value,
            key = { it.ID }
        ) { crypto ->
            ToggleAssetCard(
                asset = crypto,
                portfolioViewModel = portfolioViewModel
            )
        }
    }
}

@Composable
fun StickyStockHeader(title: String){
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(15.dp)
    )
}

@Composable
fun ToggleAssetCard(
    asset: Asset,
    portfolioViewModel: PortfolioViewModel
) {
    val isClicked = remember { mutableStateOf(portfolioViewModel.isAssetInPortfolio(asset)) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clickable {
                portfolioViewModel.toggleAsset(asset) { errorMessage ->
                    if (errorMessage != null) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    isClicked.value = portfolioViewModel.isAssetInPortfolio(asset)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isClicked.value) 
                MaterialTheme.colorScheme.tertiaryContainer 
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssetImage(asset)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isClicked.value) 
                        MaterialTheme.colorScheme.onTertiaryContainer 
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$${String.format("%.2f", asset.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isClicked.value) 
                        MaterialTheme.colorScheme.onTertiaryContainer 
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
