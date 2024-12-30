package com.example.act22.ui.pages.main

import AssetManagerViewModel
import android.util.Log
import com.example.act22.data.model.Asset
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import com.example.act22.activity.Screen
import com.example.act22.data.model.AssetType
import com.example.act22.data.model.SortingCriteria

@Composable
fun CreateMainPage(
    navController: NavController,
    viewModel: AssetManagerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAllAssets()
    }

    MainScaffold(navController) {
        CustomSearchBar { query -> viewModel.searchAssets(query) }
        TypeSort(
            onFilterStocks = { viewModel.filterAssetsByType(AssetType.STOCK) },
            onFilterCryptos = { viewModel.filterAssetsByType(AssetType.CRYPTO) },
            onFilterAll = { viewModel.filterAssetsByType(AssetType.ALL)}
        )
        when (uiState) {
            is AssetManagerViewModel.UiState.Error -> ErrorMessage((uiState as AssetManagerViewModel.UiState.Error).message)
            is AssetManagerViewModel.UiState.Success ->
                AssetList(
                    navController,
                    (uiState as AssetManagerViewModel.UiState.Success).assets
                )
            else -> LoadingSpinner()
        }
    }
}

@Composable
fun CustomSearchBar(onSearch: (String) -> Unit = {}) {
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .height(95.dp)
            .padding(horizontal = 20.dp)
            .padding(10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onSearch(searchQuery) }
            )

            CompactTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search by ID...",
                onDone = { onSearch(searchQuery) }
            )
        }
    }
}

@Composable
fun CompactTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
                innerTextField()
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun TypeSort(
    onFilterStocks: () -> Unit,
    onFilterCryptos: () -> Unit,
    onFilterAll: () -> Unit
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val selectedButton = remember { mutableStateOf<String>("All") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 24.dp)
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SmallButton(
            title = "All",
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.3f),
            color = if (selectedButton.value == "All") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondary,
            onClick = {
                selectedButton.value = "All"
                onFilterAll()
            }
        )

        SmallButton(
            title = "Stocks",
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.3f),
            color = if (selectedButton.value == "Stocks") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondary,
            onClick = {
                selectedButton.value = "Stocks"
                onFilterStocks()
            }
        )

        SmallButton(
            title = "Crypto",
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.3f),
            color = if (selectedButton.value == "Crypto") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondary,
            onClick = {
                selectedButton.value = "Crypto"
                onFilterCryptos()
            }
        )
    }
}

@Composable
fun LoadingSpinner(){
    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun AssetList(
    navController: NavController,
    assets : List<Asset>
){
    if(assets.isEmpty()){
        ErrorMessage("No asset found!")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            content = {
                items(assets) { asset ->
                    MainAssetCard(navController,asset)
                }
            }
        )
    }
}








