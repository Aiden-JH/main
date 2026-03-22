package com.example.receptionkiosk.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.receptionkiosk.data.prefs.SettingsPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminPinUiState(
    val pinInput: String = "",
    val currentPinForChange: String = "",
    val newPin: String = "",
    val message: String? = null
)

class AdminPinViewModel(
    private val settings: SettingsPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminPinUiState())
    val uiState: StateFlow<AdminPinUiState> = _uiState.asStateFlow()

    fun setPinInput(value: String) = _uiState.update { it.copy(pinInput = value.take(8), message = null) }
    fun setCurrentPin(value: String) = _uiState.update { it.copy(currentPinForChange = value.take(8), message = null) }
    fun setNewPin(value: String) = _uiState.update { it.copy(newPin = value.take(8), message = null) }

    fun unlock(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val configuredPin = settings.getAdminPin()
            if (_uiState.value.pinInput == configuredPin) {
                onSuccess()
            } else {
                _uiState.update { it.copy(message = "Invalid PIN") }
            }
        }
    }

    fun changePin() {
        viewModelScope.launch {
            val configuredPin = settings.getAdminPin()
            val state = _uiState.value
            if (state.currentPinForChange != configuredPin) {
                _uiState.update { it.copy(message = "Current PIN is incorrect") }
                return@launch
            }
            if (state.newPin.length < 4) {
                _uiState.update { it.copy(message = "New PIN must be at least 4 digits") }
                return@launch
            }
            settings.setAdminPin(state.newPin)
            _uiState.update { it.copy(message = "PIN changed", currentPinForChange = "", newPin = "") }
        }
    }
}

class AdminPinViewModelFactory(
    private val settings: SettingsPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminPinViewModel(settings) as T
}
