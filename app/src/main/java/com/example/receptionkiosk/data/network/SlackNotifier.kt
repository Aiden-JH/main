package com.example.receptionkiosk.data.network

interface Notifier {
    fun sendVisitMessage(webhookUrl: String, message: String): Result<Unit>
}

class SlackNotifier(
    private val slackClient: SlackClient
) : Notifier {
    override fun sendVisitMessage(webhookUrl: String, message: String): Result<Unit> {
        return slackClient.sendIncomingWebhook(
            webhookUrl = webhookUrl,
            payload = SlackWebhookPayload(text = message)
        )
    }
}
