package com.example.receptionkiosk.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class NotificationWorkScheduler(private val context: Context) {
    fun enqueueSendVisit(visitId: Long) {
        val request = OneTimeWorkRequestBuilder<SendNotificationWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .setInputData(Data.Builder().putLong(WorkerKeys.KEY_VISIT_ID, visitId).build())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WorkerKeys.WORK_SEND_VISIT_PREFIX + visitId,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
