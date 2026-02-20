package com.example.receptionkiosk.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.receptionkiosk.data.local.entity.PurposeEntity
import com.example.receptionkiosk.data.repository.ReceptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val purposes: List<PurposeEntity> = emptyList()
)

class HomeViewModel(
    private val repository: ReceptionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeEnabledPurposes().collect { purposes ->
                _uiState.update { it.copy(purposes = purposes) }
            }
        }
    }
}

class HomeViewModelFactory(
    private val repository: ReceptionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}
