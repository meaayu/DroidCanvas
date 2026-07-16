package com.example.ui.state

import com.example.data.Board
import com.example.data.DroidCanvasRepository
import com.example.data.ImageStorageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BoardStateHolder(
    private val scope: CoroutineScope,
    private val repository: DroidCanvasRepository,
    private val onBoardSwitched: (Int?) -> Unit
) {
    val boards: StateFlow<List<Board>> = repository.allBoards
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentBoardId = MutableStateFlow<Int?>(null)
    val currentBoardId: StateFlow<Int?> = _currentBoardId.asStateFlow()

    fun setCurrentBoardIdDirectly(boardId: Int?) {
        _currentBoardId.value = boardId
        onBoardSwitched(boardId)
    }

    fun createBoard(name: String) {
        scope.launch {
            val newId = repository.insertBoard(Board(name = name))
            setCurrentBoardIdDirectly(newId.toInt())
        }
    }

    fun selectBoard(boardId: Int) {
        setCurrentBoardIdDirectly(boardId)
    }

    fun updateBoardBackground(board: Board, backgroundType: String) {
        scope.launch(Dispatchers.IO) {
            val updated = board.copy(backgroundType = backgroundType)
            repository.updateBoard(updated)
        }
    }

    fun renameBoard(board: Board, newName: String) {
        scope.launch(Dispatchers.IO) {
            val updated = board.copy(name = newName)
            repository.updateBoard(updated)
        }
    }

    fun deleteBoard(board: Board) {
        scope.launch {
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
                    setCurrentBoardIdDirectly(null)
                }
            }
        }
    }
}
