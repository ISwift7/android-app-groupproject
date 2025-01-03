package com.example.act22.ui.pages.main

import com.example.act22.data.model.Asset
import com.example.act22.data.model.Crypto
import com.example.act22.data.model.TechStock
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.act22.R
import kotlin.random.Random
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun AssetImage(asset: Asset) {
    Box(
        modifier = Modifier
            .size(75.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = asset.iconUrl,
            contentDescription = "${asset.name} logo",
            placeholder = painterResource(R.drawable.logo_bright),
            error = painterResource(R.drawable.logo_dark),
            onError = { Log.e("AssetImage", "Error loading image for ${asset.name}: ${asset.iconUrl}") },
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.onPrimary)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50.dp)
                )
                .padding(5.dp)
        )
    }
}

@Composable
fun BasicButton(
    string: String,
    onClickAction: () -> Unit,
    color: Color = MaterialTheme.colorScheme.tertiary,
    textColor: Color = MaterialTheme.colorScheme.onTertiary
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClickAction,
            modifier = Modifier
                .padding(20.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(24.dp)),
            colors = ButtonDefaults.buttonColors(color)
        ) {
            Text(
                text = string,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
fun BigButton(prompt: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(90.dp)
            .fillMaxWidth()
            .padding(20.dp)
            .clip(RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        shape = RoundedCornerShape(30.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@Composable
fun SmallButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSecondary
) {
    Button(
        modifier = modifier.padding(horizontal = 5.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun EmptyPage(
    text: String,
    buttonText: String,
    onClick: () -> Unit
){
    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        BasicButton(buttonText, onClick)
    }
}

@Composable
fun StockChartPlaceholder(
    height: Dp = 300.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
fun VerticalTabItem(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ErrorMessage(
    text: String
){
    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
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


