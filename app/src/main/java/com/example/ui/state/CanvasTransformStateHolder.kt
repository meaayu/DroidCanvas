package com.example.ui.state

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.CanvasItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CanvasTransformStateHolder(
    private val scope: CoroutineScope,
    private val prefs: SharedPreferences,
    private val getCurrentBoardId: () -> Int?
) {
    private val TAG = "CanvasTransform"

    var isCenteringAnimated by mutableStateOf(false)
        private set

    private var _canvasScale = mutableStateOf(1f)
    var canvasScale: Float
        get() = _canvasScale.value
        set(value) {
            if (_canvasScale.value != value) {
                if (!isCenteringAnimated) {
                    cancelCentering()
                }
                _canvasScale.value = value
                schedulePersistCanvasState()
            }
        }

    private var _canvasTranslateX = mutableStateOf(0f)
    var canvasTranslateX: Float
        get() = _canvasTranslateX.value
        set(value) {
            if (_canvasTranslateX.value != value) {
                if (!isCenteringAnimated) {
                    cancelCentering()
                }
                _canvasTranslateX.value = value
                schedulePersistCanvasState()
            }
        }

    private var _canvasTranslateY = mutableStateOf(0f)
    var canvasTranslateY: Float
        get() = _canvasTranslateY.value
        set(value) {
            if (_canvasTranslateY.value != value) {
                if (!isCenteringAnimated) {
                    cancelCentering()
                }
                _canvasTranslateY.value = value
                schedulePersistCanvasState()
            }
        }

    private var persistJob: Job? = null
    private var centerJob: Job? = null

    fun resetTransform(scale: Float = 1f, translateX: Float = 0f, translateY: Float = 0f) {
        persistJob?.cancel()
        _canvasScale.value = scale
        _canvasTranslateX.value = translateX
        _canvasTranslateY.value = translateY
    }

    private fun schedulePersistCanvasState() {
        val boardId = getCurrentBoardId() ?: return
        persistJob?.cancel()
        persistJob = scope.launch(Dispatchers.IO) {
            delay(2000) // Debounce SharedPreferences writes by 2000ms
            prefs.edit()
                .putFloat("board_scale_$boardId", canvasScale)
                .putFloat("board_trans_x_$boardId", canvasTranslateX)
                .putFloat("board_trans_y_$boardId", canvasTranslateY)
                .apply()
        }
    }

    fun cancelCentering() {
        centerJob?.cancel()
        centerJob = null
    }

    fun centerOnItem(
        item: CanvasItem,
        viewportWidth: Float,
        viewportHeight: Float,
        density: Float,
        onSelect: (Int) -> Unit
    ) {
        val itemWidthPx = item.width * item.scale
        val itemHeightPx = item.height * item.scale
        
        val itemCenterX = item.posX + itemWidthPx / 2f
        val itemCenterY = item.posY + itemHeightPx / 2f
        
        val viewportCenterX = viewportWidth / 2f
        val viewportCenterY = viewportHeight / 2f
        
        val targetTranslateX = viewportCenterX - itemCenterX * canvasScale
        val targetTranslateY = viewportCenterY - itemCenterY * canvasScale
        
        onSelect(item.id)

        centerJob?.cancel()
        centerJob = scope.launch(Dispatchers.Main) {
            isCenteringAnimated = true
            try {
                val startX = canvasTranslateX
                val startY = canvasTranslateY
                val durationMs = 350f
                val startTime = System.currentTimeMillis()
                while (true) {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed >= durationMs) {
                        canvasTranslateX = targetTranslateX
                        canvasTranslateY = targetTranslateY
                        break
                    }
                    val fraction = elapsed / durationMs
                    // Cubic ease-out interpolation
                    val t = 1f - fraction
                    val easeOut = 1f - t * t * t
                    canvasTranslateX = startX + (targetTranslateX - startX) * easeOut
                    canvasTranslateY = startY + (targetTranslateY - startY) * easeOut
                    delay(16)
                }
            } finally {
                isCenteringAnimated = false
            }
        }
    }
}
