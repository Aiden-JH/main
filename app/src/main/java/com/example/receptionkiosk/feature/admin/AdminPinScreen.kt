package com.example.receptionkiosk.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.receptionkiosk.data.prefs.SettingsPreferences

@Composable
fun AdminPinScreen(
    onBack: () -> Unit,
    onPinSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AdminPinViewModel = viewModel(
        factory = AdminPinViewModelFactory(SettingsPreferences(context))
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Admin PIN", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = state.pinInput,
            onValueChange = viewModel::setPinInput,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )
        Button(onClick = { viewModel.unlock(onPinSuccess) }, modifier = Modifier.fillMaxWidth()) { Text("Unlock") }

        Text("Change PIN", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = state.currentPinForChange,
            onValueChange = viewModel::setCurrentPin,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Current PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )
        OutlinedTextField(
            value = state.newPin,
            onValueChange = viewModel::setNewPin,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New PIN") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
        )
        Button(onClick = viewModel::changePin, modifier = Modifier.fillMaxWidth()) { Text("Save PIN") }

        state.message?.let { Text(it) }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
