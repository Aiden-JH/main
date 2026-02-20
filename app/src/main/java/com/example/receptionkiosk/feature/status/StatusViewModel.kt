package com.example.receptionkiosk.feature.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.receptionkiosk.data.local.entity.VisitEntity
import com.example.receptionkiosk.data.repository.ReceptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatusUiState(
    val failedVisits: List<VisitEntity> = emptyList()
)

class StatusViewModel(
    private val repository: ReceptionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeFailedVisits().collect { failed ->
                _uiState.update { it.copy(failedVisits = failed) }
            }
        }
    }
}

class StatusViewModelFactory(
    private val repository: ReceptionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatusViewModel(repository) as T
    }
}
