package com.example.receptionkiosk.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visits",
    indices = [Index(value = ["status", "createdAtEpochMillis"])]
)
data class VisitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purposeId: Long,
    val purposeLabelSnapshot: String,
    val payloadJson: String,
    val status: String,
    val createdAtEpochMillis: Long,
    val lastError: String? = null,
    val sentAtEpochMillis: Long? = null,
    val retryCount: Int = 0
)
