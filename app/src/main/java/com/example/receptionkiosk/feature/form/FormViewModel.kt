package com.example.receptionkiosk.feature.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.receptionkiosk.data.local.entity.FieldSchemaEntity
import com.example.receptionkiosk.data.local.entity.PurposeEntity
import com.example.receptionkiosk.data.local.entity.VisitEntity
import com.example.receptionkiosk.data.repository.ReceptionRepository
import com.example.receptionkiosk.domain.model.VisitStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

data class FormUiState(
    val purpose: PurposeEntity? = null,
    val fields: List<FieldSchemaEntity> = emptyList(),
    val values: Map<String, String> = emptyMap(),
    val errors: Map<String, String> = emptyMap(),
    val isSubmitting: Boolean = false,
    val submittedVisitId: Long? = null
)

class FormViewModel(
    private val purposeId: Long,
    private val repository: ReceptionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FormUiState())
    val uiState: StateFlow<FormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(purpose = repository.getPurposeById(purposeId)) }
        }
        viewModelScope.launch {
            repository.observeEnabledFieldSchemas().collect { schemas ->
                _uiState.update { state ->
                    val defaults = schemas.associate { field -> field.key to (state.values[field.key] ?: "") }
                    state.copy(fields = schemas, values = defaults)
                }
            }
        }
    }

    fun onFieldChanged(key: String, value: String) {
        _uiState.update { state ->
            state.copy(
                values = state.values + (key to value),
                errors = state.errors - key
            )
        }
    }

    fun submit(onSaved: (Long) -> Unit, onSuccess: () -> Unit) {
        val state = _uiState.value
        val validationErrors = mutableMapOf<String, String>()
        state.fields.forEach { field ->
            if (field.required && state.values[field.key].orEmpty().trim().isEmpty()) {
                validationErrors[field.key] = "${field.label} is required"
            }
        }
        if (validationErrors.isNotEmpty()) {
            _uiState.update { it.copy(errors = validationErrors) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val payload = state.values.mapValues { it.value.trim() }
            val payloadJson = Json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                payload
            )
            val visitId = repository.createVisit(
                VisitEntity(
                    purposeId = purposeId,
                    purposeLabelSnapshot = state.purpose?.label ?: "Unknown",
                    payloadJson = payloadJson,
                    status = VisitStatus.PENDING,
                    createdAtEpochMillis = System.currentTimeMillis()
                )
            )
            onSaved(visitId)
            _uiState.update {
                it.copy(isSubmitting = false, submittedVisitId = visitId)
            }
            onSuccess()
        }
    }
}

class FormViewModelFactory(
    private val purposeId: Long,
    private val repository: ReceptionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FormViewModel(purposeId, repository) as T
    }
}
