package com.example.ui.composables

import com.example.data.CanvasItem

object CanvasViewportCalculator {
    fun isItemVisible(
        item: CanvasItem,
        scale: Float,
        translateX: Float,
        translateY: Float,
        viewportWidth: Float,
        viewportHeight: Float,
        margin: Float = 500f // margin in canvas pixels
    ): Boolean {
        if (viewportWidth <= 0f || viewportHeight <= 0f) return true // default to visible if viewport is not measured yet

        val minX = -translateX / scale - margin
        val maxX = (viewportWidth - translateX) / scale + margin
        val minY = -translateY / scale - margin
        val maxY = (viewportHeight - translateY) / scale + margin

        val itemWidth = item.width * item.scale
        val itemHeight = item.height * item.scale

        val itemLeft = item.posX
        val itemRight = item.posX + itemWidth
        val itemTop = item.posY
        val itemBottom = item.posY + itemHeight

        return itemRight >= minX && itemLeft <= maxX && itemBottom >= minY && itemTop <= maxY
    }
}
