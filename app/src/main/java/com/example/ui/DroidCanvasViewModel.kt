package com.example.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Board
import com.example.data.CanvasItem
import com.example.data.ImageStorageHelper
import com.example.data.DroidCanvasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.roundToInt

import com.example.ui.state.*

typealias DrawingStroke = com.example.ui.state.DrawingStroke

data class ValuesResult(
    val processedBitmap: Bitmap,
    val histogram: IntArray,
    val distribution: List<Float>
)

class DroidCanvasViewModel(
    private val repository: DroidCanvasRepository,
    context: Context
) : ViewModel() {
    private val TAG = "DroidCanvasViewModel"
    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences("canvas_view_prefs", Context.MODE_PRIVATE)

    // NEW state holders
    val transformState = CanvasTransformStateHolder(
        scope = viewModelScope,
        prefs = prefs,
        getCurrentBoardId = { _currentBoardId.value }
    )

    val drawingState = DrawingStateHolder(
        scope = viewModelScope,
        context = appContext,
        getCurrentBoardId = { _currentBoardId.value },
        updateUndoRedoAvailability = { updateUndoRedoAvailability() }
    )

    val selectionState = SelectionStateHolder(
        scope = viewModelScope,
        repository = repository,
        getCanvasItems = { canvasItems.value },
        saveCurrentStateToUndo = { saveCurrentStateToUndo() },
        isLocked = { isLocked }
    )

    val boardState = BoardStateHolder(
        scope = viewModelScope,
        repository = repository,
        onBoardSwitched = { boardId ->
            _loadedBoardId.value = null // Reset loaded ID immediately to trigger isLoaded = false during transition
            _currentBoardId.value = boardId
            _itemOverrides.value = emptyMap()
            if (boardId != null) {
                transformState.resetTransform(
                    scale = prefs.getFloat("board_scale_$boardId", 1f),
                    translateX = prefs.getFloat("board_trans_x_$boardId", 0f),
                    translateY = prefs.getFloat("board_trans_y_$boardId", 0f)
                )
                viewModelScope.launch(Dispatchers.IO) {
                    val strokes = drawingState.loadDrawingStrokes(boardId)
                    withContext(Dispatchers.Main) {
                        drawingState.setStrokes(strokes)
                    }
                }
            } else {
                transformState.resetTransform()
                drawingState.setStrokes(emptyList())
            }
            undoStack.clear()
            redoStack.clear()
            drawingState.clearStacks()
            updateUndoRedoAvailability()
        }
    )

    // Delegate properties for Board State
    val boards: StateFlow<List<Board>> get() = boardState.boards
    private val _currentBoardId = MutableStateFlow<Int?>(null)
    val currentBoardId: StateFlow<Int?> = _currentBoardId.asStateFlow()

    private val _isInitialLoadComplete = MutableStateFlow(false)
    val isInitialLoadComplete: StateFlow<Boolean> = _isInitialLoadComplete.asStateFlow()

    private val _loadedBoardId = MutableStateFlow<Int?>(null)
    val loadedBoardId: StateFlow<Int?> = _loadedBoardId.asStateFlow()

    val isLoaded: StateFlow<Boolean> = combine(_currentBoardId, _loadedBoardId) { current, loaded ->
        current != null && current == loaded
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Canvas Items for the current board
    private val dbCanvasItems: StateFlow<List<CanvasItem>> = _currentBoardId
        .flatMapLatest { boardId ->
            if (boardId != null) {
                repository.getItemsForBoard(boardId).onEach {
                    _loadedBoardId.value = boardId
                }
            } else {
                _loadedBoardId.value = null
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _itemOverrides = MutableStateFlow<Map<Int, CanvasItem>>(emptyMap())

    val canvasItems: StateFlow<List<CanvasItem>> = combine(dbCanvasItems, _itemOverrides) { dbItems, overrides ->
        dbItems.map { item -> overrides[item.id] ?: item }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _valuesResults = MutableStateFlow<Map<Int, ValuesResult>>(emptyMap())
    val valuesResults: StateFlow<Map<Int, ValuesResult>> = _valuesResults.asStateFlow()

    private val processingJobs = java.util.concurrent.ConcurrentHashMap<Int, Job>()
    private val lastProcessedConfigs = java.util.concurrent.ConcurrentHashMap<Int, String>()
    private val updateJobs = java.util.concurrent.ConcurrentHashMap<Int, Job>()
    private val decodedBitmapCache = android.util.LruCache<String, Bitmap>(5)

    // Undo/Redo Stacks
    private val undoStack = mutableListOf<List<CanvasItem>>()
    private val redoStack = mutableListOf<List<CanvasItem>>()

    // Expose undo/redo availability state to the UI
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private fun updateUndoRedoAvailability() {
        if (drawingState.isDrawModeEnabled) {
            _canUndo.value = drawingState.drawingUndoStack.isNotEmpty()
            _canRedo.value = drawingState.drawingRedoStack.isNotEmpty()
        } else {
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = redoStack.isNotEmpty()
        }
    }

    fun saveCurrentStateToUndo() {
        val currentItems = canvasItems.value.map { it.copy() }
        undoStack.add(currentItems)
        if (undoStack.size > 50) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        updateUndoRedoAvailability()
    }

    fun undo() {
        if (drawingState.isDrawModeEnabled) {
            drawingState.undoDrawing()
            return
        }
        if (undoStack.isEmpty()) return
        
        val previousState = undoStack.removeAt(undoStack.size - 1)
        val currentState = canvasItems.value.map { it.copy() }
        redoStack.add(currentState)
        
        updateUndoRedoAvailability()
        restoreSnapshot(previousState)
    }

    fun redo() {
        if (drawingState.isDrawModeEnabled) {
            drawingState.redoDrawing()
            return
        }
        if (redoStack.isEmpty()) return
        
        val nextState = redoStack.removeAt(redoStack.size - 1)
        val currentState = canvasItems.value.map { it.copy() }
        undoStack.add(currentState)
        
        updateUndoRedoAvailability()
        restoreSnapshot(nextState)
    }

    private fun restoreSnapshot(targetState: List<CanvasItem>) {
        val boardId = _currentBoardId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentItems = repository.getItemsForBoard(boardId).first()
                
                // 1. Delete items that exist in current database but NOT in targetState
                val targetIds = targetState.map { it.id }.toSet()
                val itemsToDelete = currentItems.filter { it.id !in targetIds }
                itemsToDelete.forEach {
                    repository.deleteCanvasItem(it)
                }
                
                // 2. Insert or update items
                targetState.forEach { targetItem ->
                    val existing = currentItems.find { it.id == targetItem.id }
                    if (existing == null) {
                        repository.insertCanvasItem(targetItem)
                    } else if (existing != targetItem) {
                        repository.updateCanvasItem(targetItem)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore snapshot", e)
            }
        }
    }

    // Delegate properties for Canvas Transform State
    val isCenteringAnimated: Boolean get() = transformState.isCenteringAnimated
    var canvasScale: Float
        get() = transformState.canvasScale
        set(value) { transformState.canvasScale = value }
    var canvasTranslateX: Float
        get() = transformState.canvasTranslateX
        set(value) { transformState.canvasTranslateX = value }
    var canvasTranslateY: Float
        get() = transformState.canvasTranslateY
        set(value) { transformState.canvasTranslateY = value }

    fun cancelCentering() = transformState.cancelCentering()
    fun centerOnItem(item: CanvasItem, viewportWidth: Float, viewportHeight: Float, density: Float) =
        transformState.centerOnItem(item, viewportWidth, viewportHeight, density) { selectItem(it) }

    // Delegate properties for Selection State
    val selectedItemId: StateFlow<Int?> get() = selectionState.selectedItemId
    val isMultiSelectMode: StateFlow<Boolean> get() = selectionState.isMultiSelectMode
    val selectedItemIds: StateFlow<Set<Int>> get() = selectionState.selectedItemIds

    fun selectItem(itemId: Int?) = selectionState.selectItem(itemId)
    fun toggleMultiSelectMode() = selectionState.toggleMultiSelectMode()
    var lastItemTapTime = 0L
    fun clearSelection() = selectionState.clearSelection()
    fun deleteSelectedItems() = selectionState.deleteSelectedItems()
    fun togglePinSelectedItems() = selectionState.togglePinSelectedItems()
    fun toggleValuesSelectedItems() = selectionState.toggleValuesSelectedItems()

    // Delegate properties and methods for Board State
    fun createBoard(name: String) = boardState.createBoard(name)
    fun selectBoard(boardId: Int) = boardState.selectBoard(boardId)
    fun deleteBoard(board: Board) = boardState.deleteBoard(board)
    fun renameBoard(board: Board, newName: String) = boardState.renameBoard(board, newName)
    fun setCurrentBoardId(boardId: Int?) = boardState.setCurrentBoardIdDirectly(boardId)

    // Delegate properties for Drawing State
    var isDrawModeEnabled: Boolean
        get() = drawingState.isDrawModeEnabled
        set(value) {
            drawingState.isDrawModeEnabled = value
            if (value) {
                selectionState.clearSelection()
            }
            updateUndoRedoAvailability()
        }
    var isEraserModeEnabled: Boolean
        get() = drawingState.isEraserModeEnabled
        set(value) { drawingState.isEraserModeEnabled = value }
    var drawingColor: Int
        get() = drawingState.drawingColor
        set(value) { drawingState.drawingColor = value }
    var drawingWidth: Float
        get() = drawingState.drawingWidth
        set(value) { drawingState.drawingWidth = value }
    val drawingStrokes: StateFlow<List<DrawingStroke>> get() = drawingState.drawingStrokes
    var activeStroke: DrawingStroke?
        get() = drawingState.activeStroke
        set(value) { drawingState.activeStroke = value }

    fun startNewStroke(startPoint: Offset) = drawingState.startNewStroke(startPoint)
    fun appendPointToActiveStroke(point: Offset) = drawingState.appendPointToActiveStroke(point)
    fun finishActiveStroke() = drawingState.finishActiveStroke()
    fun eraseStrokeAt(point: Offset, threshold: Float = 30f) = drawingState.eraseStrokeAt(point, threshold)
    fun clearDrawingStrokes() = drawingState.clearDrawingStrokes()

    var isLocked by mutableStateOf(false)
        private set

    fun toggleLock() {
        isLocked = !isLocked
    }

    private var _isSnapToGrid by mutableStateOf(prefs.getBoolean("is_snap_to_grid", false))
    var isSnapToGrid: Boolean
        get() = _isSnapToGrid
        set(value) {
            _isSnapToGrid = value
            prefs.edit().putBoolean("is_snap_to_grid", value).apply()
        }

    private var _snapStepSize by mutableStateOf(prefs.getFloat("snap_step_size", 40f))
    var snapStepSize: Float
        get() = _snapStepSize
        set(value) {
            _snapStepSize = value
            prefs.edit().putFloat("snap_step_size", value).apply()
        }

    private var _arrangeSpacing by mutableStateOf(prefs.getFloat("arrange_spacing", 60f))
    var arrangeSpacing: Float
        get() = _arrangeSpacing
        set(value) {
            _arrangeSpacing = value
            prefs.edit().putFloat("arrange_spacing", value).apply()
        }

    private var _gridStyle by mutableStateOf(prefs.getString("grid_style", "none") ?: "none")
    var gridStyle: String
        get() = _gridStyle
        set(value) {
            _gridStyle = value
            prefs.edit().putString("grid_style", value).apply()
        }

    private var _isHapticEnabled by mutableStateOf(prefs.getBoolean("is_haptic_enabled", true))
    var isHapticEnabled: Boolean
        get() = _isHapticEnabled
        set(value) {
            _isHapticEnabled = value
            prefs.edit().putBoolean("is_haptic_enabled", value).apply()
        }

    private var _doubleTapZoomTarget by mutableStateOf(prefs.getFloat("double_tap_zoom_target", 2.3f))
    var doubleTapZoomTarget: Float
        get() = _doubleTapZoomTarget
        set(value) {
            _doubleTapZoomTarget = value
            prefs.edit().putFloat("double_tap_zoom_target", value).apply()
        }

    private var _themeMode by mutableStateOf(prefs.getString("theme_mode", "system") ?: "system")
    var themeMode: String
        get() = _themeMode
        set(value) {
            _themeMode = value
            prefs.edit().putString("theme_mode", value).apply()
        }

    private var _isDynamicColorEnabled by mutableStateOf(prefs.getBoolean("dynamic_color_enabled", true))
    var isDynamicColorEnabled: Boolean
        get() = _isDynamicColorEnabled
        set(value) {
            _isDynamicColorEnabled = value
            prefs.edit().putBoolean("dynamic_color_enabled", value).apply()
        }

    private var _isPitchBlackEnabled by mutableStateOf(prefs.getBoolean("pitch_black_enabled", false))
    var isPitchBlackEnabled: Boolean
        get() = _isPitchBlackEnabled
        set(value) {
            _isPitchBlackEnabled = value
            prefs.edit().putBoolean("pitch_black_enabled", value).apply()
        }

    fun clearCurrentBoard() {
        val boardId = _currentBoardId.value ?: return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.getItemsForBoard(boardId).first()
            items.forEach { item ->
                // Comment out immediate file deletion so that undo works perfectly
                // ImageStorageHelper.deleteImageFiles(item.fullPath, item.thumbPath)
                repository.deleteCanvasItem(item)
            }
        }
    }

    init {
        // Initialize with default board if none exists
        viewModelScope.launch {
            var currentBoards = repository.allBoards.first()
            if (currentBoards.isEmpty()) {
                repository.insertBoard(Board(name = "Default Workspace"))
                currentBoards = repository.allBoards.first()
            }

            val initialBoardId = currentBoards.firstOrNull()?.id
            boardState.setCurrentBoardIdDirectly(initialBoardId)

            if (initialBoardId != null) {
                // Pre-load items to verify load completion state
                try {
                    repository.getItemsForBoard(initialBoardId).first()
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading initial canvas items", e)
                }
            }

            _isInitialLoadComplete.value = true

            // Keep current board ID valid if boards change or current one is deleted
            boards.collect { boardList ->
                if (boardList.isNotEmpty()) {
                    val currentId = _currentBoardId.value
                    if (currentId == null || !boardList.any { it.id == currentId }) {
                        boardState.setCurrentBoardIdDirectly(boardList.first().id)
                    }
                } else {
                    boardState.setCurrentBoardIdDirectly(null)
                }
            }
        }

        viewModelScope.launch {
            canvasItems.collect { items ->
                val currentIds = items.map { it.id }.toSet()
                _valuesResults.update { current ->
                    current.filterKeys { id -> id in currentIds && (items.firstOrNull { it.id == id }?.isValuesEnabled == true) }
                }
                
                processingJobs.keys.retainAll(currentIds)
                lastProcessedConfigs.keys.retainAll(currentIds)
                updateJobs.keys.retainAll(currentIds)

                for (item in items) {
                    if (item.isValuesEnabled) {
                        scheduleValuesProcessing(item)
                    } else {
                        lastProcessedConfigs.remove(item.id)
                    }
                }
            }
        }
    }



    fun autoArrangeGrid(layoutType: String = "GRID", density: Float = 2.75f) {
        val boardId = _currentBoardId.value ?: return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val items = canvasItems.value
            val unpinnedItems = items.filter { !it.isPinned }
            if (unpinnedItems.isEmpty()) return@launch
            
            val sortedItems = unpinnedItems.sortedBy { it.id }
            val spacing = arrangeSpacing
            
            // Standard target dimension in pixels/canvas units to make all images uniform in size
            val targetDimensionPx = 260f * density // approximately 260dp uniform size
            
            if (layoutType == "GRID") {
                val count = sortedItems.size
                // Smart column count calculation
                val cols = when {
                    count <= 2 -> count
                    count <= 4 -> 2
                    count <= 9 -> 3
                    else -> 4
                }
                
                // First pass: scale every item so its visual height is exactly targetDimensionPx
                // keeping its aspect ratio intact.
                val processedItems = sortedItems.map { item ->
                    val displayWidthPx = item.width
                    val displayHeightPx = item.height
                    
                    // We want visualHeightPx to be exactly targetDimensionPx.
                    // Since visualHeightPx = displayHeightPx * scale, we solve for scale:
                    val targetScale = if (displayHeightPx > 0f) targetDimensionPx / displayHeightPx else 1f
                    
                    val visualWidthPx = displayWidthPx * targetScale
                    
                    Triple(item, targetScale, visualWidthPx)
                }
                
                // Group processed items into rows
                val rows = processedItems.chunked(cols)
                
                // Calculate cumulative height
                var currentTopY = 0f
                
                rows.forEach { rowItems ->
                    var currentLeftX = 0f
                    
                    rowItems.forEach { (item, targetScale, visualWidthPx) ->
                        // Target visual coordinates
                        val posX = currentLeftX
                        val posY = currentTopY
                        
                        val updated = item.copy(
                            posX = posX,
                            posY = posY,
                            scale = targetScale,
                            rotation = 0f // reset rotation for precise grid alignment
                        )
                        repository.updateCanvasItem(updated)
                        
                        // Advance horizontal position for the next item in the same row
                        currentLeftX += visualWidthPx + spacing
                    }
                    
                    // Move currentTopY to the next row, since all items in this row have exactly targetDimensionPx height
                    currentTopY += targetDimensionPx + spacing
                }
            } else if (layoutType == "STRIP") { // "STRIP" (Horizontal film-strip layout)
                // First pass: scale every item so its visual height is exactly targetDimensionPx
                // keeping its aspect ratio intact.
                val processedItems = sortedItems.map { item ->
                    val displayWidthPx = item.width
                    val displayHeightPx = item.height
                    
                    // We want visualHeightPx to be exactly targetDimensionPx.
                    // Since visualHeightPx = displayHeightPx * scale, we solve for scale:
                    val targetScale = if (displayHeightPx > 0f) targetDimensionPx / displayHeightPx else 1f
                    
                    val visualWidthPx = displayWidthPx * targetScale
                    
                    Triple(item, targetScale, visualWidthPx)
                }
                
                var currentX = 0f
                processedItems.forEach { (item, targetScale, visualWidthPx) ->
                    val posX = currentX
                    val posY = 0f // perfect horizontal baseline alignment
 
                    val updated = item.copy(
                        posX = posX,
                        posY = posY,
                        scale = targetScale,
                        rotation = 0f // reset rotation
                    )
                    repository.updateCanvasItem(updated)
                    
                    // Move currentX to the next item, ensuring the horizontal gap is exactly equal to spacing
                    currentX += visualWidthPx + spacing
                }
            } else if (layoutType == "COLUMN") { // "COLUMN" (Vertical single-column layout)
                // Scale every item so its visual width is exactly targetDimensionPx
                val processedItems = sortedItems.map { item ->
                    val displayWidthPx = item.width
                    val displayHeightPx = item.height
                    
                    // Scale so that visualWidthPx matches targetDimensionPx:
                    val targetScale = if (displayWidthPx > 0f) targetDimensionPx / displayWidthPx else 1f
                    val visualHeightPx = displayHeightPx * targetScale
                    
                    Triple(item, targetScale, visualHeightPx)
                }
                
                var currentY = 0f
                processedItems.forEach { (item, targetScale, visualHeightPx) ->
                    val posX = 0f
                    val posY = currentY
                    
                    val updated = item.copy(
                        posX = posX,
                        posY = posY,
                        scale = targetScale,
                        rotation = 0f
                    )
                    repository.updateCanvasItem(updated)
                    
                    currentY += visualHeightPx + spacing
                }
            } else if (layoutType == "RADIAL") { // "RADIAL" (Circular distribution)
                // Scale items uniformly by height
                val processedItems = sortedItems.map { item ->
                    val displayWidthPx = item.width
                    val displayHeightPx = item.height
                    
                    val targetScale = if (displayHeightPx > 0f) targetDimensionPx / displayHeightPx else 1f
                    
                    Pair(item, targetScale)
                }
                
                // Calculate dynamic radius to prevent overlaps with many images
                val radius = 350f * density + (sortedItems.size * 25f * density)
                val totalItems = processedItems.size
                
                processedItems.forEachIndexed { index, (item, targetScale) ->
                    val angle = (2 * Math.PI * index) / totalItems
                    val posX = (radius * Math.cos(angle)).toFloat()
                    val posY = (radius * Math.sin(angle)).toFloat()
                    
                    val updated = item.copy(
                        posX = posX,
                        posY = posY,
                        scale = targetScale,
                        rotation = 0f
                    )
                    repository.updateCanvasItem(updated)
                }
            } else if (layoutType == "SPREAD") { // "SPREAD" (Beautiful overlapping Moodboard Spread)
                // Scale items uniformly by height
                val processedItems = sortedItems.map { item ->
                    val displayWidthPx = item.width
                    val displayHeightPx = item.height
                    
                    val targetScale = if (displayHeightPx > 0f) targetDimensionPx / displayHeightPx else 1f
                    val visualWidthPx = displayWidthPx * targetScale
                    
                    Triple(item, targetScale, visualWidthPx)
                }
                
                val goldenAngle = 137.5 * Math.PI / 180.0
                val step = 160f * density // distance between spiral steps
                
                processedItems.forEachIndexed { index, (item, targetScale, visualWidthPx) ->
                    // Beautiful Fermat spiral distribution
                    val r = step * Math.sqrt((index + 1).toDouble())
                    val theta = index * goldenAngle
                    val posX = (r * Math.cos(theta)).toFloat() - (visualWidthPx / 2)
                    val posY = (r * Math.sin(theta)).toFloat() - (targetDimensionPx / 2)
                    
                    // Deterministic pseudo-random rotation to look organic yet consistent
                    val rotation = ((index * 17) % 31 - 15).toFloat() // range: -15 to +15 degrees
                    
                    val updated = item.copy(
                        posX = posX,
                        posY = posY,
                        scale = targetScale,
                        rotation = rotation
                    )
                    repository.updateCanvasItem(updated)
                }
            }
        }
    }



    /**
     * Imports selected image Uris and adds them to the canvas.
     * Places them at the current viewport center.
     */
    fun addImages(context: Context, uris: List<Uri>, viewportWidthPx: Float = 1000f, viewportHeightPx: Float = 1000f) {
        viewModelScope.launch(Dispatchers.IO) {
            var boardId = _currentBoardId.value
            if (boardId == null) {
                // Automatically create a board if none exists
                val newBoardId = repository.insertBoard(Board(name = "Default Workspace"))
                boardId = newBoardId.toInt()
                withContext(Dispatchers.Main) {
                    setCurrentBoardId(boardId)
                }
            }
            saveCurrentStateToUndo()
            // Calculate center of screen in canvas space
            // screen_center = (canvas_center - translation) / scale
            val centerXPx = (viewportWidthPx / 2f - canvasTranslateX) / canvasScale
            val centerYPx = (viewportHeightPx / 2f - canvasTranslateY) / canvasScale

            var indexOffset = 0f
            uris.forEach { uri ->
                val imported = ImageStorageHelper.importImage(context, uri)
                if (imported != null) {
                    val maxZ = repository.getMaxZIndex(boardId) ?: 0
                    
                    // We stagger the multiple images slightly so they don't cover each other completely
                    val posX = centerXPx - (imported.width * 0.25f) + indexOffset
                    val posY = centerYPx - (imported.height * 0.25f) + indexOffset
                    
                    val newItem = CanvasItem(
                        boardId = boardId,
                        fullPath = imported.fullPath,
                        thumbPath = imported.thumbPath,
                        posX = posX,
                        posY = posY,
                        width = imported.width,
                        height = imported.height,
                        scale = 0.5f, // Start at 50% scale so it's not too massive on first drop
                        rotation = 0f,
                        zIndex = maxZ + 1
                    )
                    repository.insertCanvasItem(newItem)
                    indexOffset += 60f // Stagger placement of next image
                }
            }
        }
    }

    fun updateItemPosition(item: CanvasItem, deltaX: Float, deltaY: Float) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            // Delta is in canvas space already
            var targetX = item.posX + deltaX
            var targetY = item.posY + deltaY
            if (isSnapToGrid) {
                targetX = kotlin.math.round(targetX / snapStepSize) * snapStepSize
                targetY = kotlin.math.round(targetY / snapStepSize) * snapStepSize
            }
            val updated = item.copy(
                posX = targetX,
                posY = targetY
            )
            repository.updateCanvasItem(updated)
        }
    }

    fun updateItemAbsolutePosition(item: CanvasItem, posX: Float, posY: Float) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            var targetX = posX
            var targetY = posY
            if (isSnapToGrid) {
                targetX = kotlin.math.round(targetX / snapStepSize) * snapStepSize
                targetY = kotlin.math.round(targetY / snapStepSize) * snapStepSize
            }
            val updated = item.copy(
                posX = targetX,
                posY = targetY
            )
            repository.updateCanvasItem(updated)
        }
    }

    fun updateItemScaleAndRotation(item: CanvasItem, scaleMultiplier: Float, rotationDelta: Float) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(
                scale = (item.scale * scaleMultiplier).coerceIn(0.05f, 10f),
                rotation = (item.rotation + rotationDelta) % 360f
            )
            repository.updateCanvasItem(updated)
        }
    }

    fun updateItemScaleAndPositionDirectly(item: CanvasItem, newScale: Float, posX: Float, posY: Float) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            var targetX = posX
            var targetY = posY
            if (isSnapToGrid) {
                targetX = kotlin.math.round(targetX / snapStepSize) * snapStepSize
                targetY = kotlin.math.round(targetY / snapStepSize) * snapStepSize
            }
            val updated = item.copy(
                scale = newScale.coerceIn(0.05f, 10f),
                posX = targetX,
                posY = targetY
            )
            repository.updateCanvasItem(updated)
        }
    }

    fun updateItemAbsoluteScale(item: CanvasItem, targetScale: Float) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(scale = targetScale.coerceIn(0.05f, 10f))
            repository.updateCanvasItem(updated)
        }
    }

    fun updateItemScaleInMemory(item: CanvasItem, targetScale: Float) {
        if (item.isPinned) return
        val updated = item.copy(scale = targetScale.coerceIn(0.05f, 10f))
        _itemOverrides.update { current -> current + (item.id to updated) }
    }

    fun commitItemScale(item: CanvasItem, targetScale: Float) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(scale = targetScale.coerceIn(0.05f, 10f))
            repository.updateCanvasItem(updated)
            _itemOverrides.update { current -> current - item.id }
        }
    }

    fun updateItemAbsoluteRotation(item: CanvasItem, targetRotation: Float) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(rotation = (targetRotation % 360f + 360f) % 360f)
            repository.updateCanvasItem(updated)
        }
    }

    fun updateItemRotationInMemory(item: CanvasItem, targetRotation: Float) {
        if (item.isPinned) return
        val updated = item.copy(rotation = (targetRotation % 360f + 360f) % 360f)
        _itemOverrides.update { current -> current + (item.id to updated) }
    }

    fun commitItemRotation(item: CanvasItem, targetRotation: Float) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(rotation = (targetRotation % 360f + 360f) % 360f)
            repository.updateCanvasItem(updated)
            _itemOverrides.update { current -> current - item.id }
        }
    }

    fun updateSimplicityInMemory(item: CanvasItem, simplicity: Int) {
        val updated = item.copy(simplicity = simplicity.coerceIn(0, 100))
        _itemOverrides.update { current -> current + (item.id to updated) }
        scheduleValuesProcessing(updated)
    }

    fun commitSimplicity(item: CanvasItem, simplicity: Int) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(simplicity = simplicity.coerceIn(0, 100))
            repository.updateCanvasItem(updated)
            _itemOverrides.update { current -> current - item.id }
        }
    }

    fun updateStopsInMemory(item: CanvasItem, stops: List<ValueStop>) {
        val sorted = stops.sortedBy { it.position }
        val json = ValueProcessor.serializeStops(sorted)
        val updated = item.copy(stopsJson = json, stopsCount = sorted.size)
        _itemOverrides.update { current -> current + (item.id to updated) }
        scheduleValuesProcessing(updated)
    }

    fun commitStops(item: CanvasItem, stops: List<ValueStop>) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val sorted = stops.sortedBy { it.position }
            val json = ValueProcessor.serializeStops(sorted)
            val updated = item.copy(stopsJson = json, stopsCount = sorted.size)
            repository.updateCanvasItem(updated)
            _itemOverrides.update { current -> current - item.id }
        }
    }

    fun togglePinItem(item: CanvasItem) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(isPinned = !item.isPinned)
            repository.updateCanvasItem(updated)
        }
    }

    fun toggleFlipHorizontal(item: CanvasItem) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(flipHorizontal = !item.flipHorizontal)
            repository.updateCanvasItem(updated)
        }
    }

    fun toggleFlipVertical(item: CanvasItem) {
        if (item.isPinned) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(flipVertical = !item.flipVertical)
            repository.updateCanvasItem(updated)
        }
    }

    fun toggleValuesEnabled(item: CanvasItem) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(isValuesEnabled = !item.isValuesEnabled)
            repository.updateCanvasItem(updated)
        }
    }

    fun updateSimplicity(item: CanvasItem, simplicity: Int) {
        val itemId = item.id
        updateJobs[itemId]?.cancel()
        updateJobs[itemId] = viewModelScope.launch(Dispatchers.IO) {
            delay(50)
            val updated = item.copy(simplicity = simplicity.coerceIn(0, 100))
            repository.updateCanvasItem(updated)
        }
    }

    fun updateStopsCount(item: CanvasItem, count: Int) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val newCount = count.coerceIn(2, 8)
            val defaultStops = ValueProcessor.generateDefaultStops(newCount)
            val json = ValueProcessor.serializeStops(defaultStops)
            val updated = item.copy(stopsCount = newCount, stopsJson = json)
            repository.updateCanvasItem(updated)
        }
    }

    fun updateStops(item: CanvasItem, stops: List<ValueStop>) {
        val itemId = item.id
        updateJobs[itemId]?.cancel()
        updateJobs[itemId] = viewModelScope.launch(Dispatchers.IO) {
            delay(50)
            val sorted = stops.sortedBy { it.position }
            val json = ValueProcessor.serializeStops(sorted)
            val updated = item.copy(stopsJson = json, stopsCount = sorted.size)
            repository.updateCanvasItem(updated)
        }
    }

    fun scheduleValuesProcessing(item: CanvasItem) {
        val itemId = item.id
        val path = if (File(item.thumbPath).exists()) item.thumbPath else item.fullPath
        if (path.isEmpty() || !File(path).exists()) {
            return
        }

        val configSignature = "${path}:${item.simplicity}:${item.stopsCount}:${item.stopsJson}"
        if (lastProcessedConfigs[itemId] == configSignature) {
            return
        }

        processingJobs[itemId]?.cancel()

        val job = viewModelScope.launch(Dispatchers.Default) {
            delay(150)

            try {
                lastProcessedConfigs[itemId] = configSignature
                var bitmap = decodedBitmapCache.get(path)
                if (bitmap == null) {
                    bitmap = BitmapFactory.decodeFile(path)
                    if (bitmap != null) {
                        decodedBitmapCache.put(path, bitmap)
                    }
                }
                if (bitmap == null) return@launch
                
                val stops = if (item.stopsJson.isEmpty()) {
                    ValueProcessor.generateDefaultStops(item.stopsCount)
                } else {
                    ValueProcessor.parseStops(item.stopsJson)
                }

                val result = ValueProcessor.processImage(
                    source = bitmap,
                    simplicity = item.simplicity,
                    stops = stops
                )

                _valuesResults.update { current ->
                    current + (itemId to ValuesResult(
                        processedBitmap = result.processedBitmap,
                        histogram = result.histogram,
                        distribution = result.distribution
                    ))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing values for item $itemId", e)
            }
        }
        processingJobs[itemId] = job
    }

    fun bringToFront(item: CanvasItem) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val maxZ = repository.getMaxZIndex(item.boardId) ?: 0
            if (item.zIndex < maxZ) {
                val updated = item.copy(zIndex = maxZ + 1)
                repository.updateCanvasItem(updated)
            }
        }
    }

    fun sendToBack(item: CanvasItem) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            // Find current minimum z-index
            val boardItems = canvasItems.value
            val minZ = boardItems.minOfOrNull { it.zIndex } ?: 0
            if (item.zIndex > minZ) {
                val updated = item.copy(zIndex = minZ - 1)
                repository.updateCanvasItem(updated)
            }
        }
    }

    fun duplicateItem(item: CanvasItem) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Create duplicate files
                val id = UUID.randomUUID().toString()
                val oldFull = File(item.fullPath)
                val oldThumb = File(item.thumbPath)
                
                val context = oldFull.parentFile ?: return@launch
                val newFull = File(context, "ref_full_$id.jpg")
                val newThumb = File(context, "ref_thumb_$id.jpg")
  
                if (oldFull.exists()) oldFull.copyTo(newFull)
                if (oldThumb.exists()) oldThumb.copyTo(newThumb)
 
                // 2. Insert new DB item
                val maxZ = repository.getMaxZIndex(item.boardId) ?: 0
                val duplicated = item.copy(
                    id = 0, // autoGenerate
                    fullPath = newFull.absolutePath,
                    thumbPath = newThumb.absolutePath,
                    posX = item.posX + 40f, // Offset slightly
                    posY = item.posY + 40f,
                    zIndex = maxZ + 1
                )
                repository.insertCanvasItem(duplicated)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to duplicate item", e)
            }
        }
    }

    fun deleteItem(item: CanvasItem) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Remove DB entry
            repository.deleteCanvasItem(item)
            // 2. Comment out physical file deletion to allow full restoration via Undo
            // ImageStorageHelper.deleteImageFiles(item.fullPath, item.thumbPath)
            if (selectedItemId.value == item.id) {
                clearSelection()
            }
        }
    }

    fun resetZoom() {
        canvasScale = 1f
        canvasTranslateX = 0f
        canvasTranslateY = 0f
    }

    fun fitToContent(viewportWidth: Float, viewportHeight: Float, density: Float) {
        val items = canvasItems.value
        if (items.isEmpty()) {
            canvasScale = 1f
            canvasTranslateX = 0f
            canvasTranslateY = 0f
            return
        }

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        items.forEach { item ->
            val itemWidthPx = item.width * item.scale
            val itemHeightPx = item.height * item.scale
            
            val left = item.posX
            val top = item.posY
            val right = item.posX + itemWidthPx
            val bottom = item.posY + itemHeightPx
            
            if (left < minX) minX = left
            if (top < minY) minY = top
            if (right > maxX) maxX = right
            if (bottom > maxY) maxY = bottom
        }

        val contentWidth = maxX - minX
        val contentHeight = maxY - minY
        
        val padding = 100f
        val availableWidth = (viewportWidth - padding * 2).coerceAtLeast(100f)
        val availableHeight = (viewportHeight - padding * 2).coerceAtLeast(100f)
        
        val scaleX = availableWidth / contentWidth
        val scaleY = availableHeight / contentHeight
        val newScale = minOf(scaleX, scaleY).coerceIn(0.01f, 3.0f)
        
        val contentCenterX = minX + contentWidth / 2f
        val contentCenterY = minY + contentHeight / 2f
        
        val viewportCenterX = viewportWidth / 2f
        val viewportCenterY = viewportHeight / 2f
        
        canvasScale = newScale
        canvasTranslateX = viewportCenterX - contentCenterX * newScale
        canvasTranslateY = viewportCenterY - contentCenterY * newScale
    }



    fun handleCanvasTap(tapX: Float, tapY: Float, density: Float) {
        if (isLocked) return
        
        val canvasX = (tapX - canvasTranslateX) / canvasScale
        val canvasY = (tapY - canvasTranslateY) / canvasScale
        
        val items = canvasItems.value
        
        val overlappingItems = items.filter { item ->
            val itemWidthPx = item.width * item.scale
            val itemHeightPx = item.height * item.scale
            
            canvasX >= item.posX && canvasX <= item.posX + itemWidthPx &&
            canvasY >= item.posY && canvasY <= item.posY + itemHeightPx
        }.sortedByDescending { it.zIndex }
        
        if (overlappingItems.isEmpty()) {
            selectItem(null)
            return
        }
        
        val currentSelectedId = selectedItemId.value
        val currentIndex = overlappingItems.indexOfFirst { it.id == currentSelectedId }
        
        if (currentIndex != -1 && overlappingItems.size > 1) {
            val nextIndex = (currentIndex + 1) % overlappingItems.size
            selectItem(overlappingItems[nextIndex].id)
        } else {
            selectItem(overlappingItems.first().id)
        }
    }

    fun toggleZoomOnItem(item: CanvasItem, viewportWidth: Float, viewportHeight: Float, density: Float) {
        val distToZoomOut = kotlin.math.abs(canvasScale - 0.36f)
        val distToZoomIn = kotlin.math.abs(canvasScale - doubleTapZoomTarget)
        val targetScale = if (distToZoomIn < distToZoomOut) {
            0.36f
        } else {
            doubleTapZoomTarget
        }
        
        val itemWidthPx = item.width * item.scale
        val itemHeightPx = item.height * item.scale
        
        val itemCenterX = item.posX + itemWidthPx / 2f
        val itemCenterY = item.posY + itemHeightPx / 2f
        
        val viewportCenterX = viewportWidth / 2f
        val viewportCenterY = viewportHeight / 2f
        
        canvasScale = targetScale
        canvasTranslateX = viewportCenterX - itemCenterX * targetScale
        canvasTranslateY = viewportCenterY - itemCenterY * targetScale
        
        selectItem(item.id)
    }

    fun cropCanvasItem(
        context: Context,
        item: CanvasItem,
        cropLeft: Float,
        cropTop: Float,
        cropRight: Float,
        cropBottom: Float
    ) {
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val originalFilePath = item.fullPath
                val originalFile = File(originalFilePath)
                if (!originalFile.exists()) {
                    Log.e("DroidCanvasViewModel", "Original image file does not exist: $originalFilePath")
                    return@launch
                }

                // 1. Get EXIF rotation of the original image
                var rotationDegrees = 0
                try {
                    val exifInterface = ExifInterface(originalFilePath)
                    val orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    rotationDegrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                } catch (e: Exception) {
                    Log.e("DroidCanvasViewModel", "Error reading EXIF", e)
                }

                // 2. Load the original bitmap
                val originalBitmap = BitmapFactory.decodeFile(originalFilePath)
                if (originalBitmap == null) {
                    Log.e("DroidCanvasViewModel", "Failed to decode original bitmap")
                    return@launch
                }

                // 3. Rotate bitmap if needed to match displayed orientation
                val rotatedBitmap = if (rotationDegrees != 0) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                    val rotated = Bitmap.createBitmap(
                        originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
                    )
                    if (rotated != originalBitmap) {
                        originalBitmap.recycle()
                    }
                    rotated
                } else {
                    originalBitmap
                }

                // 4. Calculate crop coordinates on the rotated (display-oriented) bitmap
                val x = (cropLeft * rotatedBitmap.width).roundToInt().coerceIn(0, rotatedBitmap.width - 1)
                val y = (cropTop * rotatedBitmap.height).roundToInt().coerceIn(0, rotatedBitmap.height - 1)
                val width = ((cropRight - cropLeft) * rotatedBitmap.width).roundToInt().coerceIn(1, rotatedBitmap.width - x)
                val height = ((cropBottom - cropTop) * rotatedBitmap.height).roundToInt().coerceIn(1, rotatedBitmap.height - y)

                // 5. Crop the bitmap
                val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, x, y, width, height)

                // 6. Generate new files and paths to prevent caching issues and keep original intact in undo stack
                val id = UUID.randomUUID().toString()
                val extension = "jpg"
                val newFullFile = File(context.filesDir, "ref_full_$id.$extension")
                val newThumbFile = File(context.filesDir, "ref_thumb_$id.jpg")

                FileOutputStream(newFullFile).use { out ->
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }

                // Save thumbnail
                val maxDim = maxOf(width, height)
                val scale = if (maxDim > 400) 400f / maxDim else 1f
                val tW = (width * scale).roundToInt().coerceAtLeast(1)
                val tH = (height * scale).roundToInt().coerceAtLeast(1)
                val thumbBitmap = Bitmap.createScaledBitmap(croppedBitmap, tW, tH, true)
                FileOutputStream(newThumbFile).use { out ->
                    thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                // 7. Calculate new visual offset/position of the cropped image in canvas space
                val visualCropLeftPx = cropLeft * item.width * item.scale
                val visualCropTopPx = cropTop * item.height * item.scale

                // Account for rotation of the offset vector if the canvas item is rotated!
                val rotatedOffset = CanvasMathHelper.rotateDragAmount(visualCropLeftPx, visualCropTopPx, item.rotation)

                val newPosX = item.posX + rotatedOffset.posX
                val newPosY = item.posY + rotatedOffset.posY

                val newScale = item.scale

                // 8. Update database
                val updated = item.copy(
                    fullPath = newFullFile.absolutePath,
                    thumbPath = newThumbFile.absolutePath,
                    width = width.toFloat(),
                    height = height.toFloat(),
                    posX = newPosX,
                    posY = newPosY,
                    scale = newScale
                )
                repository.updateCanvasItem(updated)

                // Clean up bitmaps
                croppedBitmap.recycle()
                if (rotatedBitmap != originalBitmap) {
                    rotatedBitmap.recycle()
                }
                originalBitmap.recycle()
                thumbBitmap.recycle()
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Image cropped successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DroidCanvasViewModel", "Error cropping image", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error cropping image: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displayDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }
}

class DroidCanvasViewModelFactory(
    private val repository: DroidCanvasRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DroidCanvasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DroidCanvasViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
