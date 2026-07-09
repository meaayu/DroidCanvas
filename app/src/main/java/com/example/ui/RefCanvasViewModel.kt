package com.example.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Board
import com.example.data.CanvasItem
import com.example.data.ImageStorageHelper
import com.example.data.RefCanvasRepository
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
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class RefCanvasViewModel(
    private val repository: RefCanvasRepository,
    context: Context
) : ViewModel() {
    private val TAG = "RefCanvasViewModel"
    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences("canvas_view_prefs", Context.MODE_PRIVATE)

    // List of all boards
    val boards: StateFlow<List<Board>> = repository.allBoards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current active board ID
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
    val canvasItems: StateFlow<List<CanvasItem>> = _currentBoardId
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

    // Undo/Redo Stacks
    private val undoStack = mutableListOf<List<CanvasItem>>()
    private val redoStack = mutableListOf<List<CanvasItem>>()

    // Expose undo/redo availability state to the UI
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private fun updateUndoRedoAvailability() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
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
        val boardId = _currentBoardId.value ?: return
        if (undoStack.isEmpty()) return
        
        val previousState = undoStack.removeAt(undoStack.size - 1)
        val currentState = canvasItems.value.map { it.copy() }
        redoStack.add(currentState)
        
        updateUndoRedoAvailability()
        restoreSnapshot(previousState)
    }

    fun redo() {
        val boardId = _currentBoardId.value ?: return
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

    // Canvas view states (panned and zoomed) with debounced database persistence
    private var _canvasScale = mutableStateOf(1f)
    var canvasScale: Float
        get() = _canvasScale.value
        set(value) {
            if (_canvasScale.value != value) {
                _canvasScale.value = value
                schedulePersistCanvasState()
            }
        }

    private var _canvasTranslateX = mutableStateOf(0f)
    var canvasTranslateX: Float
        get() = _canvasTranslateX.value
        set(value) {
            if (_canvasTranslateX.value != value) {
                _canvasTranslateX.value = value
                schedulePersistCanvasState()
            }
        }

    private var _canvasTranslateY = mutableStateOf(0f)
    var canvasTranslateY: Float
        get() = _canvasTranslateY.value
        set(value) {
            if (_canvasTranslateY.value != value) {
                _canvasTranslateY.value = value
                schedulePersistCanvasState()
            }
        }

    private var persistJob: kotlinx.coroutines.Job? = null

    private fun schedulePersistCanvasState() {
        val boardId = _currentBoardId.value ?: return
        persistJob?.cancel()
        persistJob = viewModelScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(1000)
            prefs.edit()
                .putFloat("board_scale_$boardId", canvasScale)
                .putFloat("board_trans_x_$boardId", canvasTranslateX)
                .putFloat("board_trans_y_$boardId", canvasTranslateY)
                .apply()
        }
    }

    // Selected item id
    private val _selectedItemId = MutableStateFlow<Int?>(null)
    val selectedItemId: StateFlow<Int?> = _selectedItemId.asStateFlow()

    // Multiple selection support
    private val _selectedItemIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedItemIds: StateFlow<Set<Int>> = _selectedItemIds.asStateFlow()

    var lastItemTapTime = 0L

    fun toggleItemSelection(itemId: Int) {
        if (isLocked) return
        val current = _selectedItemIds.value
        val updated = if (current.contains(itemId)) {
            current - itemId
        } else {
            current + itemId
        }
        _selectedItemIds.value = updated
        _selectedItemId.value = updated.lastOrNull()
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
        _selectedItemId.value = null
    }

    fun selectAllItems() {
        if (isLocked) return
        val allIds = canvasItems.value.map { it.id }.toSet()
        _selectedItemIds.value = allIds
        _selectedItemId.value = allIds.lastOrNull()
    }

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

    private var _gridStyle by mutableStateOf(prefs.getString("grid_style", "dots") ?: "dots")
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

    private fun setCurrentBoardId(boardId: Int?) {
        persistJob?.cancel() // Cancel any pending saves when switching boards
        _loadedBoardId.value = null // Reset loaded ID immediately to trigger isLoaded = false during transition
        _currentBoardId.value = boardId
        if (boardId != null) {
            _canvasScale.value = prefs.getFloat("board_scale_$boardId", 1f)
            _canvasTranslateX.value = prefs.getFloat("board_trans_x_$boardId", 0f)
            _canvasTranslateY.value = prefs.getFloat("board_trans_y_$boardId", 0f)
        } else {
            _canvasScale.value = 1f
            _canvasTranslateX.value = 0f
            _canvasTranslateY.value = 0f
        }
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoAvailability()
    }

    init {
        // Initialize with default board if none exists
        viewModelScope.launch {
            // 1. Clean up duplicate "Main Board"s from previous default board creation bug
            val initialBoards = repository.allBoards.first()
            val mainBoards = initialBoards.filter { it.name == "Main Board" }
            if (mainBoards.size > 1) {
                // Keep the first one, delete the rest to fix the database for existing users
                val keepBoard = mainBoards.first()
                val toDelete = mainBoards.drop(1)
                for (b in toDelete) {
                    repository.deleteBoard(b)
                }
            }

            // 2. Refresh board state after clean up and load initial board
            val cleanedBoards = repository.allBoards.first()
            val initialBoardId = if (cleanedBoards.isEmpty()) {
                val defaultBoardId = repository.insertBoard(Board(name = "Main Board"))
                defaultBoardId.toInt()
            } else {
                val otherBoardsExist = cleanedBoards.any { it.name.trim().lowercase() != "main board" }
                if (otherBoardsExist) {
                    cleanedBoards.first { it.name.trim().lowercase() != "main board" }.id
                } else {
                    cleanedBoards.first().id
                }
            }

            setCurrentBoardId(initialBoardId)

            // Pre-load items to verify load completion state
            try {
                repository.getItemsForBoard(initialBoardId).first()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial canvas items", e)
            }

            _isInitialLoadComplete.value = true

            // Keep current board ID valid if boards change or current one is deleted
            boards.collect { boardList ->
                if (boardList.isNotEmpty()) {
                    val otherBoardsExist = boardList.any { it.name.trim().lowercase() != "main board" }
                    val visibleBoards = if (otherBoardsExist) {
                        boardList.filter { it.name.trim().lowercase() != "main board" }
                    } else {
                        boardList
                    }
                    val currentId = _currentBoardId.value
                    if (currentId == null || !visibleBoards.any { it.id == currentId }) {
                        setCurrentBoardId(visibleBoards.first().id)
                    }
                } else {
                    setCurrentBoardId(null)
                }
            }
        }
    }

    fun selectBoard(boardId: Int) {
        setCurrentBoardId(boardId)
        _selectedItemId.value = null
        _selectedItemIds.value = emptySet()
    }

    fun createBoard(name: String) {
        viewModelScope.launch {
            val newId = repository.insertBoard(Board(name = name))
            setCurrentBoardId(newId.toInt())
        }
    }

    fun updateBoardBackground(board: Board, backgroundType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = board.copy(backgroundType = backgroundType)
            repository.updateBoard(updated)
        }
    }

    fun renameBoard(board: Board, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = board.copy(name = newName)
            repository.updateBoard(updated)
        }
    }

    fun deleteBoard(board: Board) {
        viewModelScope.launch {
            // Delete all canvas items and files first using .first() to avoid blocking flow
            val items = repository.getItemsForBoard(board.id).first()
            items.forEach { item ->
                ImageStorageHelper.deleteImageFiles(item.fullPath, item.thumbPath)
            }
            repository.deleteBoard(board)
            if (_currentBoardId.value == board.id) {
                val remaining = boards.value.filter { it.id != board.id }
                if (remaining.isNotEmpty()) {
                    selectBoard(remaining.first().id)
                } else {
                    setCurrentBoardId(null)
                }
            }
        }
    }

    fun selectItem(itemId: Int?) {
        if (isLocked) {
            _selectedItemId.value = null
            _selectedItemIds.value = emptySet()
        } else {
            _selectedItemId.value = itemId
            _selectedItemIds.value = if (itemId != null) setOf(itemId) else emptySet()
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
                    val nativeWidthDp = item.width / density
                    val nativeHeightDp = item.height / density
                    
                    val maxBound = 320f
                    val maxDimension = maxOf(nativeWidthDp, nativeHeightDp)
                    val scaleFactor = if (maxDimension > maxBound) {
                        maxBound / maxDimension
                    } else {
                        1f
                    }
                    
                    val displayWidthPx = nativeWidthDp * scaleFactor * density
                    val displayHeightPx = nativeHeightDp * scaleFactor * density
                    
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
            } else { // "STRIP" (Horizontal film-strip layout)
                // First pass: scale every item so its visual height is exactly targetDimensionPx
                // keeping its aspect ratio intact.
                val processedItems = sortedItems.map { item ->
                    val nativeWidthDp = item.width / density
                    val nativeHeightDp = item.height / density
                    
                    val maxBound = 320f
                    val maxDimension = maxOf(nativeWidthDp, nativeHeightDp)
                    val scaleFactor = if (maxDimension > maxBound) {
                        maxBound / maxDimension
                    } else {
                        1f
                    }
                    
                    val displayWidthPx = nativeWidthDp * scaleFactor * density
                    val displayHeightPx = nativeHeightDp * scaleFactor * density
                    
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
            }
        }
    }



    /**
     * Imports selected image Uris and adds them to the canvas.
     * Places them at the current viewport center.
     */
    fun addImages(context: Context, uris: List<Uri>, viewportWidthPx: Float = 1000f, viewportHeightPx: Float = 1000f) {
        val boardId = _currentBoardId.value ?: return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
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
            if (_selectedItemId.value == item.id) {
                _selectedItemId.value = null
            }
            val currentSet = _selectedItemIds.value
            if (currentSet.contains(item.id)) {
                _selectedItemIds.value = currentSet - item.id
            }
        }
    }

    fun duplicateSelectedItems() {
        val idsToDuplicate = _selectedItemIds.value
        if (idsToDuplicate.isEmpty()) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentItems = canvasItems.value
                val itemsToDuplicate = currentItems.filter { it.id in idsToDuplicate }
                if (itemsToDuplicate.isEmpty()) return@launch
                val firstItem = itemsToDuplicate.first()
                var maxZ = repository.getMaxZIndex(firstItem.boardId) ?: 0
                val newSelectedIds = mutableSetOf<Int>()
                
                itemsToDuplicate.forEach { item ->
                    val id = UUID.randomUUID().toString()
                    val oldFull = File(item.fullPath)
                    val oldThumb = File(item.thumbPath)
                    val parentFile = oldFull.parentFile
                    if (parentFile != null) {
                        val newFull = File(parentFile, "ref_full_$id.jpg")
                        val newThumb = File(parentFile, "ref_thumb_$id.jpg")
                        if (oldFull.exists()) oldFull.copyTo(newFull)
                        if (oldThumb.exists()) oldThumb.copyTo(newThumb)
                        
                        maxZ += 1
                        val duplicated = item.copy(
                            id = 0,
                            fullPath = newFull.absolutePath,
                            thumbPath = newThumb.absolutePath,
                            posX = item.posX + 40f,
                            posY = item.posY + 40f,
                            zIndex = maxZ
                        )
                        val insertedId = repository.insertCanvasItem(duplicated)
                        newSelectedIds.add(insertedId.toInt())
                    }
                }
                
                // Update selection to newly duplicated items
                _selectedItemIds.value = newSelectedIds
                _selectedItemId.value = newSelectedIds.lastOrNull()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to duplicate selected items", e)
            }
        }
    }

    fun bringSelectedToFront() {
        val ids = _selectedItemIds.value
        if (ids.isEmpty()) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val currentItems = canvasItems.value
            val selectedItems = currentItems.filter { it.id in ids }.sortedBy { it.zIndex }
            if (selectedItems.isEmpty()) return@launch
            
            var maxZ = repository.getMaxZIndex(selectedItems.first().boardId) ?: 0
            selectedItems.forEach { item ->
                maxZ += 1
                repository.updateCanvasItem(item.copy(zIndex = maxZ))
            }
        }
    }

    fun sendSelectedToBack() {
        val ids = _selectedItemIds.value
        if (ids.isEmpty()) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val currentItems = canvasItems.value
            val selectedItems = currentItems.filter { it.id in ids }.sortedByDescending { it.zIndex }
            if (selectedItems.isEmpty()) return@launch
            
            var minZ = currentItems.minOfOrNull { it.zIndex } ?: 0
            selectedItems.forEach { item ->
                minZ -= 1
                repository.updateCanvasItem(item.copy(zIndex = minZ))
            }
        }
    }

    fun deleteSelectedItems() {
        val idsToDelete = _selectedItemIds.value
        if (idsToDelete.isEmpty()) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val currentItems = canvasItems.value
            val itemsToDelete = currentItems.filter { it.id in idsToDelete }
            itemsToDelete.forEach { item ->
                repository.deleteCanvasItem(item)
            }
            clearSelection()
        }
    }

    fun togglePinSelectedItems() {
        val idsToPin = _selectedItemIds.value
        if (idsToPin.isEmpty()) return
        saveCurrentStateToUndo()
        viewModelScope.launch(Dispatchers.IO) {
            val currentItems = canvasItems.value
            val selectedItems = currentItems.filter { it.id in idsToPin }
            if (selectedItems.isEmpty()) return@launch
            val targetPinState = !selectedItems.first().isPinned
            selectedItems.forEach { item ->
                repository.updateCanvasItem(item.copy(isPinned = targetPinState))
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
            val nativeWidthDp = item.width / density
            val nativeHeightDp = item.height / density
            val maxDimensionDp = maxOf(nativeWidthDp, nativeHeightDp)
            val scaleFactor = if (maxDimensionDp > 320f) 320f / maxDimensionDp else 1f
            
            val itemWidthPx = item.width * scaleFactor * item.scale
            val itemHeightPx = item.height * scaleFactor * item.scale
            
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

    fun centerOnItem(item: CanvasItem, viewportWidth: Float, viewportHeight: Float, density: Float) {
        val nativeWidthDp = item.width / density
        val nativeHeightDp = item.height / density
        val maxDimensionDp = maxOf(nativeWidthDp, nativeHeightDp)
        val scaleFactor = if (maxDimensionDp > 320f) 320f / maxDimensionDp else 1f
        
        val itemWidthPx = item.width * scaleFactor * item.scale
        val itemHeightPx = item.height * scaleFactor * item.scale
        
        val itemCenterX = item.posX + itemWidthPx / 2f
        val itemCenterY = item.posY + itemHeightPx / 2f
        
        val viewportCenterX = viewportWidth / 2f
        val viewportCenterY = viewportHeight / 2f
        
        canvasTranslateX = viewportCenterX - itemCenterX * canvasScale
        canvasTranslateY = viewportCenterY - itemCenterY * canvasScale
        selectItem(item.id)
    }

    fun handleCanvasTap(tapX: Float, tapY: Float, density: Float) {
        if (isLocked) return
        
        val canvasX = (tapX - canvasTranslateX) / canvasScale
        val canvasY = (tapY - canvasTranslateY) / canvasScale
        
        val items = canvasItems.value
        
        val overlappingItems = items.filter { item ->
            val nativeWidthDp = item.width / density
            val nativeHeightDp = item.height / density
            val maxDimensionDp = maxOf(nativeWidthDp, nativeHeightDp)
            val scaleFactor = if (maxDimensionDp > 320f) 320f / maxDimensionDp else 1f
            
            val itemWidthPx = item.width * scaleFactor * item.scale
            val itemHeightPx = item.height * scaleFactor * item.scale
            
            canvasX >= item.posX && canvasX <= item.posX + itemWidthPx &&
            canvasY >= item.posY && canvasY <= item.posY + itemHeightPx
        }.sortedByDescending { it.zIndex }
        
        if (overlappingItems.isEmpty()) {
            selectItem(null)
            return
        }
        
        val currentSelectedId = _selectedItemId.value
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
        
        val nativeWidthDp = item.width / density
        val nativeHeightDp = item.height / density
        val maxDimensionDp = maxOf(nativeWidthDp, nativeHeightDp)
        val scaleFactor = if (maxDimensionDp > 320f) 320f / maxDimensionDp else 1f
        
        val itemWidthPx = item.width * scaleFactor * item.scale
        val itemHeightPx = item.height * scaleFactor * item.scale
        
        val itemCenterX = item.posX + itemWidthPx / 2f
        val itemCenterY = item.posY + itemHeightPx / 2f
        
        val viewportCenterX = viewportWidth / 2f
        val viewportCenterY = viewportHeight / 2f
        
        canvasScale = targetScale
        canvasTranslateX = viewportCenterX - itemCenterX * targetScale
        canvasTranslateY = viewportCenterY - itemCenterY * targetScale
        
        selectItem(item.id)
    }
}

class RefCanvasViewModelFactory(
    private val repository: RefCanvasRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RefCanvasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RefCanvasViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
