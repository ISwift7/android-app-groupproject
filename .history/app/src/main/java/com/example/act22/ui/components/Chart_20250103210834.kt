package com.example.act22.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

data class ChartPoint(
    val timestamp: String,
    val price: Double
)

@Composable
fun DrawChart(
    points: List<ChartPoint>,
    lineColor: Color,
    pointColor: Color,
    pointRadius: Float = 3f
) {
    if (points.isEmpty()) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val padding = 16f

        // Find min and max values for scaling
        val minPrice = points.minOf { it.price }
        val maxPrice = points.maxOf { it.price }
        val priceRange = maxPrice - minPrice

        // Calculate points
        val scaledPoints = points.mapIndexed { index, point ->
            val x = padding + (index * (width - 2 * padding) / (points.size - 1))
            val y = height - padding - ((point.price - minPrice) / priceRange) * (height - 2 * padding)
            Offset(x, y)
        }

        // Draw line connecting points
        val path = Path().apply {
            moveTo(scaledPoints.first().x, scaledPoints.first().y)
            for (i in 1 until scaledPoints.size) {
                lineTo(scaledPoints[i].x, scaledPoints[i].y)
            }
        }
        drawPath(path, lineColor, style = Stroke(width = 2f))

        // Draw points
        scaledPoints.forEach { point ->
            drawCircle(
                color = pointColor,
                radius = pointRadius,
                center = point
            )
        }
    }
}

@Composable
fun DrawChartWithTimestamps(
    points: List<ChartPoint>,
    lineColor: Color,
    pointColor: Color,
    pointRadius: Float = 3f
) {
    if (points.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val padding = 32f // Increased padding to accommodate timestamps

        // Find min and max values for scaling
        val minPrice = points.minOf { it.price }
        val maxPrice = points.maxOf { it.price }
        val priceRange = maxPrice - minPrice

        // Calculate points
        val scaledPoints = points.mapIndexed { index, point ->
            val x = padding + (index * (width - 2 * padding) / (points.size - 1))
            val y = height - padding - ((point.price - minPrice) / priceRange) * (height - 2 * padding)
            Offset(x, y)
        }

        // Draw line connecting points
        val path = Path().apply {
            moveTo(scaledPoints.first().x, scaledPoints.first().y)
            for (i in 1 until scaledPoints.size) {
                lineTo(scaledPoints[i].x, scaledPoints[i].y)
            }
        }
        drawPath(path, lineColor, style = Stroke(width = 2f))

        // Draw points and timestamps
        points.forEachIndexed { index, point ->
            val scaledPoint = scaledPoints[index]
            
            // Draw point
            drawCircle(
                color = pointColor,
                radius = pointRadius,
                center = scaledPoint
            )

            // Draw timestamp for first, middle, and last points
            if (index == 0 || index == points.size - 1 || index == points.size / 2) {
                try {
                    val timestamp = point.timestamp.toLongOrNull()?.let {
                        dateFormat.format(it * 1000L) // Convert Unix timestamp to date
                    } ?: point.timestamp
                    
                    drawText(
                        textMeasurer = textMeasurer,
                        text = timestamp,
                        topLeft = Offset(scaledPoint.x - 20f, height - padding + 5f),
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = lineColor
                        )
                    )
                } catch (e: Exception) {
                    // Handle timestamp parsing error silently
                }
            }
        }
    }
} 