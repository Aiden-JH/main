package com.example.receptionkiosk.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.receptionkiosk.core.di.ServiceLocator
import com.example.receptionkiosk.data.network.Notifier
import com.example.receptionkiosk.data.network.SlackNotifier
import com.example.receptionkiosk.data.prefs.SettingsPreferences
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SendNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val repository = ServiceLocator.provideRepository(appContext)
    private val settingsPreferences = SettingsPreferences(appContext)
    private val notifier: Notifier = SlackNotifier(
        com.example.receptionkiosk.data.network.SlackClient(
            OkHttpClient.Builder().build()
        )
    )

    override suspend fun doWork(): Result {
        val visitId = inputData.getLong(WorkerKeys.KEY_VISIT_ID, -1L)
        if (visitId <= 0L) return Result.failure()

        val visit = repository.getVisitById(visitId)
            ?: return Result.failure()

        val webhook = settingsPreferences.getSlackWebhookUrl()
        if (webhook.isBlank()) {
            repository.markVisitFailed(visitId, "Slack webhook is not configured")
            return Result.success()
        }

        val message = buildMessage(visit.purposeLabelSnapshot, visit.payloadJson, visit.createdAtEpochMillis)

        val result = notifier.sendVisitMessage(webhook, message)
        return if (result.isSuccess) {
            repository.markVisitSent(visitId)
            Result.success()
        } else {
            val errorText = result.exceptionOrNull()?.message.orEmpty().ifBlank { "Unknown Slack error" }
            repository.markVisitFailed(visitId, errorText)
            if (errorText.contains("HTTP 4") && !errorText.contains("HTTP 429")) {
                Result.failure()
            } else {
                Result.retry()
            }
        }
    }

    private fun buildMessage(purpose: String, payloadJson: String, createdAtMillis: Long): String {
        val payload = runCatching {
            Json.decodeFromString(
                MapSerializer(String.serializer(), String.serializer()),
                payloadJson
            )
        }.getOrElse { emptyMap() }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val time = formatter.format(Instant.ofEpochMilli(createdAtMillis).atZone(ZoneId.systemDefault()))

        return buildString {
            appendLine("New visitor check-in")
            appendLine("Purpose: $purpose")
            appendLine("Visitor: ${payload["visitorName"].orEmpty()}")
            appendLine("Company: ${payload["company"].orEmpty()}")
            appendLine("Host: ${payload["hostName"].orEmpty()}")
            appendLine("Phone: ${payload["phone"].orEmpty()}")
            appendLine("Note: ${payload["note"].orEmpty()}")
            append("Time: $time")
        }
    }
}
