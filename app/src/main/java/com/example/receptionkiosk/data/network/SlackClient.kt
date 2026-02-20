package com.example.receptionkiosk.data.network

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SlackClient(
    private val httpClient: OkHttpClient,
    private val json: Json = Json
) {
    fun sendIncomingWebhook(webhookUrl: String, payload: SlackWebhookPayload): Result<Unit> {
        return try {
            val body = json.encodeToString(payload)
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(webhookUrl)
                .post(body)
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val code = response.code
                    val message = response.body?.string().orEmpty().take(200)
                    Result.failure(IOException("Slack HTTP $code: $message"))
                }
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}
