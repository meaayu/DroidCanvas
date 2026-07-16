package com.example.ui.state

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.example.ui.StrokeOptimizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class DrawingStroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<Offset>,
    val color: Int,
    val strokeWidth: Float
)

class DrawingStateHolder(
    private val scope: CoroutineScope,
    private val context: Context,
    private val getCurrentBoardId: () -> Int?,
    private val updateUndoRedoAvailability: () -> Unit
) {
    private val TAG = "DrawingState"

    private var _isDrawModeEnabled = mutableStateOf(false)
    var isDrawModeEnabled: Boolean
        get() = _isDrawModeEnabled.value
        set(value) {
            _isDrawModeEnabled.value = value
            updateUndoRedoAvailability()
        }

    private var _isEraserModeEnabled = mutableStateOf(false)
    var isEraserModeEnabled: Boolean
        get() = _isEraserModeEnabled.value
        set(value) {
            _isEraserModeEnabled.value = value
        }

    private var _drawingColor = mutableStateOf(0xFFF44336.toInt()) // Red by default
    var drawingColor: Int
        get() = _drawingColor.value
        set(value) {
            _drawingColor.value = value
        }

    private var _drawingWidth = mutableStateOf(8f)
    var drawingWidth: Float
        get() = _drawingWidth.value
        set(value) {
            _drawingWidth.value = value
        }

    private val _drawingStrokes = MutableStateFlow<List<DrawingStroke>>(emptyList())
    val drawingStrokes: StateFlow<List<DrawingStroke>> = _drawingStrokes.asStateFlow()

    var activeStroke by mutableStateOf<DrawingStroke?>(null)

    val drawingUndoStack = mutableListOf<List<DrawingStroke>>()
    val drawingRedoStack = mutableListOf<List<DrawingStroke>>()

    fun startNewStroke(startPoint: Offset) {
        val newStroke = DrawingStroke(
            points = listOf(startPoint),
            color = drawingColor,
            strokeWidth = drawingWidth
        )
        activeStroke = newStroke
    }

    fun appendPointToActiveStroke(point: Offset) {
        val current = activeStroke ?: return
        activeStroke = current.copy(points = current.points + point)
    }

    fun finishActiveStroke() {
        var stroke = activeStroke ?: return
        if (stroke.points.size > 2) {
            val xs = stroke.points.map { it.x }.toFloatArray()
            val ys = stroke.points.map { it.y }.toFloatArray()
            
            val optimized = StrokeOptimizer.optimizeStroke(xs, ys, 0.8f, 2)
            val optimizedXs = optimized[0]
            val optimizedYs = optimized[1]
            
            val optimizedPoints = mutableListOf<Offset>()
            for (i in optimizedXs.indices) {
                optimizedPoints.add(Offset(optimizedXs[i], optimizedYs[i]))
            }
            stroke = stroke.copy(points = optimizedPoints)
        }
        if (stroke.points.isNotEmpty()) {
            saveDrawingStateToUndo()
            val updated = _drawingStrokes.value + stroke
            _drawingStrokes.value = updated
            val boardId = getCurrentBoardId()
            if (boardId != null) {
                saveDrawingStrokes(boardId, updated)
            }
        }
        activeStroke = null
    }

    fun eraseStrokeAt(point: Offset, threshold: Float = 30f) {
        val updated = _drawingStrokes.value.filter { stroke ->
            val xs = stroke.points.map { it.x }.toFloatArray()
            val ys = stroke.points.map { it.y }.toFloatArray()
            !StrokeOptimizer.isStrokeIntersectingPoint(xs, ys, point.x, point.y, threshold)
        }
        if (updated.size != _drawingStrokes.value.size) {
            saveDrawingStateToUndo()
            _drawingStrokes.value = updated
            val boardId = getCurrentBoardId() ?: return
            saveDrawingStrokes(boardId, updated)
        }
    }

    fun clearDrawingStrokes() {
        if (_drawingStrokes.value.isNotEmpty()) {
            saveDrawingStateToUndo()
            _drawingStrokes.value = emptyList()
            val boardId = getCurrentBoardId() ?: return
            saveDrawingStrokes(boardId, emptyList())
        }
    }

    fun setStrokes(strokes: List<DrawingStroke>) {
        _drawingStrokes.value = strokes
    }

    fun saveDrawingStateToUndo() {
        val currentStrokes = _drawingStrokes.value.map { it.copy() }
        drawingUndoStack.add(currentStrokes)
        if (drawingUndoStack.size > 50) {
            drawingUndoStack.removeAt(0)
        }
        drawingRedoStack.clear()
        updateUndoRedoAvailability()
    }

    fun undoDrawing(): Boolean {
        val boardId = getCurrentBoardId() ?: return false
        if (drawingUndoStack.isEmpty()) return false
        
        val previousState = drawingUndoStack.removeAt(drawingUndoStack.size - 1)
        val currentState = _drawingStrokes.value.map { it.copy() }
        drawingRedoStack.add(currentState)
        
        _drawingStrokes.value = previousState
        saveDrawingStrokes(boardId, previousState)
        updateUndoRedoAvailability()
        return true
    }

    fun redoDrawing(): Boolean {
        val boardId = getCurrentBoardId() ?: return false
        if (drawingRedoStack.isEmpty()) return false
        
        val nextState = drawingRedoStack.removeAt(drawingRedoStack.size - 1)
        val currentState = _drawingStrokes.value.map { it.copy() }
        drawingUndoStack.add(currentState)
        
        _drawingStrokes.value = nextState
        saveDrawingStrokes(boardId, nextState)
        updateUndoRedoAvailability()
        return true
    }

    fun saveDrawingStrokes(boardId: Int, strokes: List<DrawingStroke>) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "drawing_strokes_$boardId.txt")
                val content = strokes.joinToString("\n") { stroke ->
                    val pointsStr = stroke.points.joinToString(";") { "${it.x},${it.y}" }
                    "${stroke.id}|${stroke.color}|${stroke.strokeWidth}|$pointsStr"
                }
                file.writeText(content)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save drawing strokes", e)
            }
        }
    }

    fun loadDrawingStrokes(boardId: Int): List<DrawingStroke> {
        return try {
            val file = File(context.filesDir, "drawing_strokes_$boardId.txt")
            if (!file.exists()) return emptyList()
            val lines = file.readLines()
            lines.mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size < 4) return@mapNotNull null
                val id = parts[0]
                val color = parts[1].toIntOrNull() ?: 0xFFF44336.toInt()
                val strokeWidth = parts[2].toFloatOrNull() ?: 8f
                val pointsStr = parts[3]
                val points = if (pointsStr.isEmpty()) {
                    emptyList()
                } else {
                    pointsStr.split(";").mapNotNull { p ->
                        val coords = p.split(",")
                        if (coords.size == 2) {
                            val x = coords[0].toFloatOrNull()
                            val y = coords[1].toFloatOrNull()
                            if (x != null && y != null) Offset(x, y) else null
                        } else null
                    }
                }
                DrawingStroke(id, points, color, strokeWidth)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load drawing strokes", e)
            emptyList()
        }
    }

    fun clearStacks() {
        drawingUndoStack.clear()
        drawingRedoStack.clear()
    }
}
