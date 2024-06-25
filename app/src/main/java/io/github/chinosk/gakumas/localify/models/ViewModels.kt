package io.github.chinosk.gakumas.localify.models

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

open class CollapsibleBoxViewModel(initiallyBreastExpanded: Boolean = false) : ViewModel() {
    open var expanded by mutableStateOf(initiallyBreastExpanded)
}

class BreastCollapsibleBoxViewModel(initiallyBreastExpanded: Boolean = false) : CollapsibleBoxViewModel(initiallyBreastExpanded) {
    override var expanded by mutableStateOf(initiallyBreastExpanded)
}

class ResourceCollapsibleBoxViewModel(initiallyBreastExpanded: Boolean = false) : CollapsibleBoxViewModel(initiallyBreastExpanded) {
    override var expanded by mutableStateOf(initiallyBreastExpanded)
}

class BreastCollapsibleBoxViewModelFactory(private val initiallyExpanded: Boolean) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BreastCollapsibleBoxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BreastCollapsibleBoxViewModel(initiallyExpanded) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ResourceCollapsibleBoxViewModelFactory(private val initiallyExpanded: Boolean) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResourceCollapsibleBoxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResourceCollapsibleBoxViewModel(initiallyExpanded) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class ProgramConfigViewModelFactory(private val initialValue: ProgramConfig,
                                    private val localResourceVersion: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgramConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProgramConfigViewModel(initialValue, localResourceVersion) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProgramConfigViewModel(initValue: ProgramConfig, initLocalResourceVersion: String) : ViewModel() {
    val configState = MutableStateFlow(initValue)
    val config: StateFlow<ProgramConfig> = configState.asStateFlow()

    val downloadProgressState = MutableStateFlow(-1f)
    val downloadProgress: StateFlow<Float> = downloadProgressState.asStateFlow()

    val downloadAbleState = MutableStateFlow(true)
    val downloadAble: StateFlow<Boolean> = downloadAbleState.asStateFlow()

    val localResourceVersionState = MutableStateFlow(initLocalResourceVersion)
    val localResourceVersion: StateFlow<String> = localResourceVersionState.asStateFlow()

    val errorStringState = MutableStateFlow("")
    val errorString: StateFlow<String> = errorStringState.asStateFlow()
}
