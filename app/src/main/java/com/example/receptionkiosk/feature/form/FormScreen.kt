package com.example.receptionkiosk.feature.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.receptionkiosk.core.di.ServiceLocator
import com.example.receptionkiosk.worker.NotificationWorkScheduler

@Composable
fun FormScreen(
    purposeId: Long,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FormViewModel = viewModel(
        factory = FormViewModelFactory(
            purposeId = purposeId,
            repository = ServiceLocator.provideRepository(context)
        )
    )
    val scheduler = NotificationWorkScheduler(context)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = state.purpose?.label ?: "Visitor Form",
            style = MaterialTheme.typography.headlineMedium
        )

        state.fields.forEach { field ->
            val value = state.values[field.key].orEmpty()
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = { viewModel.onFieldChanged(field.key, it) },
                label = { Text(if (field.required) "${field.label} *" else field.label) },
                isError = state.errors.containsKey(field.key),
                supportingText = {
                    state.errors[field.key]?.let { Text(it) }
                },
                minLines = if (field.inputType == "MULTILINE") 3 else 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (field.inputType == "PHONE") KeyboardType.Phone else KeyboardType.Text
                )
            )
        }

        Button(
            onClick = {
                viewModel.submit(
                    onSaved = { visitId -> scheduler.enqueueSendVisit(visitId) },
                    onSuccess = onSuccess
                )
            },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSubmitting) "Submitting..." else "Submit")
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
