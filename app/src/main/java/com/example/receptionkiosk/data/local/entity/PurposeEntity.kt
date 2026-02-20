package com.example.receptionkiosk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purposes")
data class PurposeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val enabled: Boolean = true,
    val sortOrder: Int = 0
)
