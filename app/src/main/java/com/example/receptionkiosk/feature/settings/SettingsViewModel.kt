package com.example.receptionkiosk.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.receptionkiosk.data.local.entity.FieldSchemaEntity
import com.example.receptionkiosk.data.local.entity.PurposeEntity
import com.example.receptionkiosk.data.network.SlackClient
import com.example.receptionkiosk.data.network.SlackWebhookPayload
import com.example.receptionkiosk.data.prefs.SettingsPreferences
import com.example.receptionkiosk.data.repository.ReceptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

data class SettingsUiState(
    val webhook: String = "",
    val keepScreenOn: Boolean = true,
    val idleTimeout: String = "60",
    val purposes: List<PurposeEntity> = emptyList(),
    val fields: List<FieldSchemaEntity> = emptyList(),
    val message: String? = null
)

class SettingsViewModel(
    private val repository: ReceptionRepository,
    private val settings: SettingsPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { settings.slackWebhookUrl.collect { _uiState.update { s -> s.copy(webhook = it, message = null) } } }
        viewModelScope.launch { settings.keepScreenOn.collect { _uiState.update { s -> s.copy(keepScreenOn = it) } } }
        viewModelScope.launch { settings.idleTimeoutSeconds.collect { _uiState.update { s -> s.copy(idleTimeout = it.toString()) } } }
        viewModelScope.launch { repository.observePurposes().collect { _uiState.update { s -> s.copy(purposes = it) } } }
        viewModelScope.launch { repository.observeFieldSchemas().collect { _uiState.update { s -> s.copy(fields = it) } } }
    }

    fun setWebhook(value: String) = _uiState.update { it.copy(webhook = value, message = null) }
    fun setIdleTimeout(value: String) = _uiState.update { it.copy(idleTimeout = value.filter(Char::isDigit).take(4), message = null) }

    fun saveSettings() {
        viewModelScope.launch {
            settings.setSlackWebhookUrl(_uiState.value.webhook)
            settings.setIdleTimeoutSeconds(_uiState.value.idleTimeout.toIntOrNull() ?: 60)
            _uiState.update { it.copy(message = "Settings saved") }
        }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { settings.setKeepScreenOn(enabled) }
    }

    fun togglePurpose(id: Long, enabled: Boolean) {
        viewModelScope.launch { repository.setPurposeEnabled(id, enabled) }
    }

    fun toggleFieldEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch { repository.setFieldEnabled(id, enabled) }
    }

    fun toggleFieldRequired(id: Long, required: Boolean) {
        viewModelScope.launch { repository.setFieldRequired(id, required) }
    }

    fun testSlack() {
        viewModelScope.launch {
            val url = _uiState.value.webhook.trim()
            if (url.isBlank()) {
                _uiState.update { it.copy(message = "Webhook URL is empty") }
                return@launch
            }
            val result = SlackClient(OkHttpClient()).sendIncomingWebhook(
                url,
                SlackWebhookPayload("Reception kiosk test message")
            )
            _uiState.update {
                it.copy(message = if (result.isSuccess) "Slack test sent" else "Slack test failed")
            }
        }
    }
}

class SettingsViewModelFactory(
    private val repository: ReceptionRepository,
    private val settings: SettingsPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SettingsViewModel(repository, settings) as T
}
