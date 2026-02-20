package com.example.receptionkiosk.data.network

import kotlinx.serialization.Serializable

@Serializable
data class SlackWebhookPayload(
    val text: String
)
