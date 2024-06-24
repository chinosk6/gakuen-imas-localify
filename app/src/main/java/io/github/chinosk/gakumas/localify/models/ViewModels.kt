package io.github.chinosk.gakumas.localify.models

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider


class CollapsibleBoxViewModel(initiallyExpanded: Boolean = false) : ViewModel() {
    var expanded by mutableStateOf(initiallyExpanded)
}

class CollapsibleBoxViewModelFactory(private val initiallyExpanded: Boolean) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollapsibleBoxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CollapsibleBoxViewModel(initiallyExpanded) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}