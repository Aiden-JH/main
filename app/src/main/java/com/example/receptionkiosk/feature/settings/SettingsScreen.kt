package com.example.receptionkiosk.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.receptionkiosk.core.di.ServiceLocator
import com.example.receptionkiosk.data.prefs.SettingsPreferences

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenStatus: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            ServiceLocator.provideRepository(context),
            SettingsPreferences(context)
        )
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = state.webhook,
            onValueChange = viewModel::setWebhook,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Slack Webhook URL") }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::saveSettings, modifier = Modifier.weight(1f)) { Text("Save") }
            Button(onClick = viewModel::testSlack, modifier = Modifier.weight(1f)) { Text("Test Slack") }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Keep screen on", modifier = Modifier.weight(1f))
            Switch(checked = state.keepScreenOn, onCheckedChange = viewModel::setKeepScreenOn)
        }

        OutlinedTextField(
            value = state.idleTimeout,
            onValueChange = viewModel::setIdleTimeout,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Idle timeout (seconds, 0 disables)") }
        )

        Text("Purposes", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.purposes, key = { it.id }) { purpose ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(purpose.label, modifier = Modifier.weight(1f))
                    Switch(checked = purpose.enabled, onCheckedChange = { viewModel.togglePurpose(purpose.id, it) })
                }
            }
            items(state.fields, key = { it.id }) { field ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(field.label)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Enabled")
                        Switch(checked = field.enabled, onCheckedChange = { viewModel.toggleFieldEnabled(field.id, it) })
                        Text("Required")
                        Switch(checked = field.required, onCheckedChange = { viewModel.toggleFieldRequired(field.id, it) })
                    }
                }
            }
        }

        state.message?.let { Text(it) }

        Button(onClick = onOpenStatus, modifier = Modifier.fillMaxWidth()) { Text("Notification Status") }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
