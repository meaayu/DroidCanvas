package com.example.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.CanvasItem

@Composable
fun CanvasItemLayer(
    canvasItems: List<CanvasItem>,
    canvasScale: Float,
    canvasTranslateX: Float,
    canvasTranslateY: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    isMultiSelectMode: Boolean,
    selectedItemId: Int?,
    selectedItemIds: Set<Int>,
    modifier: Modifier = Modifier,
    renderItem: @Composable (CanvasItem, Boolean) -> Unit
) {
    // Viewport-based culling filter
    val visibleItems = remember(canvasItems, canvasScale, canvasTranslateX, canvasTranslateY, viewportWidth, viewportHeight) {
        canvasItems.filter { item ->
            CanvasViewportCalculator.isItemVisible(
                item = item,
                scale = canvasScale,
                translateX = canvasTranslateX,
                translateY = canvasTranslateY,
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        visibleItems.forEach { item ->
            val isSelected = if (isMultiSelectMode) {
                selectedItemIds.contains(item.id)
            } else {
                selectedItemId == item.id
            }
            renderItem(item, isSelected)
        }
    }
}
