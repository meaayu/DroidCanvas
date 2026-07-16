package com.example.ui.state

import com.example.data.CanvasItem
import com.example.data.DroidCanvasRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SelectionStateHolder(
    private val scope: CoroutineScope,
    private val repository: DroidCanvasRepository,
    private val getCanvasItems: () -> List<CanvasItem>,
    private val saveCurrentStateToUndo: () -> Unit,
    private val isLocked: () -> Boolean
) {
    private val _selectedItemId = MutableStateFlow<Int?>(null)
    val selectedItemId: StateFlow<Int?> = _selectedItemId.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    private val _selectedItemIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedItemIds: StateFlow<Set<Int>> = _selectedItemIds.asStateFlow()

    fun toggleMultiSelectMode() {
        val newMode = !_isMultiSelectMode.value
        _isMultiSelectMode.value = newMode
        if (newMode) {
            _selectedItemIds.value = _selectedItemId.value?.let { setOf(it) } ?: emptySet()
            _selectedItemId.value = null
        } else {
            _selectedItemIds.value = emptySet()
        }
    }

    fun clearSelection() {
        _selectedItemId.value = null
        _selectedItemIds.value = emptySet()
    }

    fun selectItem(itemId: Int?) {
        if (isLocked()) {
            _selectedItemId.value = null
            _selectedItemIds.value = emptySet()
        } else {
            if (itemId == null) {
                _selectedItemId.value = null
                _selectedItemIds.value = emptySet()
            } else if (_isMultiSelectMode.value) {
                val current = _selectedItemIds.value
                if (current.contains(itemId)) {
                    _selectedItemIds.value = current - itemId
                } else {
                    _selectedItemIds.value = current + itemId
                }
            } else {
                _selectedItemId.value = itemId
            }
        }
    }

    fun deleteSelectedItems() {
        val idsToDelete = _selectedItemIds.value
        if (idsToDelete.isEmpty()) return
        saveCurrentStateToUndo()
        scope.launch(Dispatchers.IO) {
            val items = getCanvasItems().filter { it.id in idsToDelete }
            items.forEach { item ->
                repository.deleteCanvasItem(item)
            }
            _selectedItemIds.value = emptySet()
        }
    }

    fun togglePinSelectedItems() {
        val idsToPin = _selectedItemIds.value
        if (idsToPin.isEmpty()) return
        saveCurrentStateToUndo()
        scope.launch(Dispatchers.IO) {
            val items = getCanvasItems().filter { it.id in idsToPin }
            val anyUnpinned = items.any { !it.isPinned }
            items.forEach { item ->
                val updated = item.copy(isPinned = anyUnpinned)
                repository.updateCanvasItem(updated)
            }
        }
    }

    fun toggleValuesSelectedItems() {
        val idsToToggle = _selectedItemIds.value
        if (idsToToggle.isEmpty()) return
        saveCurrentStateToUndo()
        scope.launch(Dispatchers.IO) {
            val items = getCanvasItems().filter { it.id in idsToToggle }
            val anyDisabled = items.any { !it.isValuesEnabled }
            items.forEach { item ->
                val updated = item.copy(isValuesEnabled = anyDisabled)
                repository.updateCanvasItem(updated)
            }
        }
    }
}
