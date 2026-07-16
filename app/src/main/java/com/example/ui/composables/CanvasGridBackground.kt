package com.example.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun CanvasGridBackground(
    canvasScale: Float,
    canvasTranslateX: Float,
    canvasTranslateY: Float,
    gridStyle: String,
    modifier: Modifier = Modifier,
    slateBackgroundColor: Color = MaterialTheme.colorScheme.background,
    slateGridDotColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(slateBackgroundColor)
            .drawBehind {
                if (gridStyle == "none") return@drawBehind

                val gridSize = 45.dp.toPx()
                val scale = canvasScale
                val transX = canvasTranslateX
                val transY = canvasTranslateY

                val width = size.width
                val height = size.height

                val startX = -transX / scale
                val endX = (width - transX) / scale
                val startY = -transY / scale
                val endY = (height - transY) / scale

                val startGridX = startX - (startX % gridSize) - gridSize
                val startGridY = startY - (startY % gridSize) - gridSize

                val stepMultiplier = when {
                    scale < 0.22f -> 6
                    scale < 0.45f -> 3
                    scale < 0.75f -> 2
                    else -> 1
                }
                val effectiveGridSize = gridSize * stepMultiplier

                val dotColor = slateGridDotColor

                var currX = startGridX
                if (gridStyle == "dots") {
                    while (currX < endX + effectiveGridSize) {
                        val screenX = currX * scale + transX
                        var currY = startGridY
                        while (currY < endY + effectiveGridSize) {
                            val screenY = currY * scale + transY
                            drawCircle(
                                color = dotColor,
                                radius = (1.8f * scale).coerceIn(1.2f, 4.0f),
                                center = Offset(screenX, screenY)
                            )
                            currY += effectiveGridSize
                        }
                        currX += effectiveGridSize
                    }
                } else if (gridStyle == "lines") {
                    // Draw vertical lines
                    while (currX < endX + effectiveGridSize) {
                        val screenX = currX * scale + transX
                        drawLine(
                            color = dotColor.copy(alpha = dotColor.alpha * 0.45f),
                            start = Offset(screenX, 0f),
                            end = Offset(screenX, height),
                            strokeWidth = (1.2f * scale).coerceIn(0.8f, 2.5f)
                        )
                        currX += effectiveGridSize
                    }
                    // Draw horizontal lines
                    var currY = startGridY
                    while (currY < endY + effectiveGridSize) {
                        val screenY = currY * scale + transY
                        drawLine(
                            color = dotColor.copy(alpha = dotColor.alpha * 0.45f),
                            start = Offset(0f, screenY),
                            end = Offset(width, screenY),
                            strokeWidth = (1.2f * scale).coerceIn(0.8f, 2.5f)
                        )
                        currY += effectiveGridSize
                    }
                } else if (gridStyle == "graph") {
                    // Draw horizontal & vertical lines with major lines every 5 subdivisions
                    while (currX < endX + effectiveGridSize) {
                        val screenX = currX * scale + transX
                        val index = (currX / gridSize).roundToInt()
                        val isMajor = index % 5 == 0
                        val alphaFactor = if (isMajor) 0.8f else 0.25f
                        val strokeWidthFactor = if (isMajor) 2.0f else 1.0f
                        drawLine(
                            color = dotColor.copy(alpha = dotColor.alpha * alphaFactor * 0.45f),
                            start = Offset(screenX, 0f),
                            end = Offset(screenX, height),
                            strokeWidth = (strokeWidthFactor * 1.2f * scale).coerceIn(0.8f, 3.5f)
                        )
                        currX += effectiveGridSize
                    }
                    var currY = startGridY
                    while (currY < endY + effectiveGridSize) {
                        val screenY = currY * scale + transY
                        val index = (currY / gridSize).roundToInt()
                        val isMajor = index % 5 == 0
                        val alphaFactor = if (isMajor) 0.8f else 0.25f
                        val strokeWidthFactor = if (isMajor) 2.0f else 1.0f
                        drawLine(
                            color = dotColor.copy(alpha = dotColor.alpha * alphaFactor * 0.45f),
                            start = Offset(0f, screenY),
                            end = Offset(width, screenY),
                            strokeWidth = (strokeWidthFactor * 1.2f * scale).coerceIn(0.8f, 3.5f)
                        )
                        currY += effectiveGridSize
                    }
                }
            }
    )
}
