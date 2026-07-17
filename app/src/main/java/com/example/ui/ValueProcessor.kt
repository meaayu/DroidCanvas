package com.example.ui

import android.graphics.Bitmap
import android.graphics.Color

data class ValueStop(val position: Float, val color: Int)

data class ValueProcessingResult(
    val processedBitmap: Bitmap,
    val histogram: IntArray,
    val distribution: List<Float>
)

object ValueProcessor {

    fun generateDefaultStops(n: Int): List<ValueStop> {
        val stops = mutableListOf<ValueStop>()
        val count = n.coerceIn(2, 8)
        for (i in 0 until count) {
            val position = (i + 1) / count.toFloat()
            val brightness = if (count > 1) {
                (i / (count - 1).toFloat() * 255f).toInt().coerceIn(0, 255)
            } else {
                255
            }
            val color = Color.rgb(brightness, brightness, brightness)
            stops.add(ValueStop(position, color))
        }
        return stops
    }

    fun parseStops(json: String): List<ValueStop> {
        if (json.isEmpty()) return emptyList()
        return json.split(";").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                val pos = parts[0].toFloatOrNull() ?: return@mapNotNull null
                val col = parts[1].toIntOrNull() ?: return@mapNotNull null
                ValueStop(pos, col)
            } else null
        }.sortedBy { it.position }
    }

    fun serializeStops(stops: List<ValueStop>): String {
        return stops.joinToString(";") { "${it.position}:${it.color}" }
    }

    fun processImage(
        source: Bitmap,
        simplicity: Int,
        stops: List<ValueStop>
    ): ValueProcessingResult {
        val sortedStops = stops.sortedBy { it.position }
        if (sortedStops.isEmpty()) {
            return ValueProcessingResult(
                processedBitmap = source,
                histogram = IntArray(256),
                distribution = emptyList()
            )
        }

        val stopPositions = sortedStops.map { it.position }.toFloatArray()
        val stopColors = sortedStops.map { it.color }.toIntArray()

        // Call our multi-threaded Java processor
        val javaResult = ImageValueProcessor.processImage(
            source,
            simplicity,
            stopPositions,
            stopColors
        )

        return ValueProcessingResult(
            processedBitmap = javaResult.processedBitmap,
            histogram = javaResult.histogram,
            distribution = javaResult.distribution.toList()
        )
    }
}
