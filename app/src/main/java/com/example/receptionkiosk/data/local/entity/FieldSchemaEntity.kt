package com.example.receptionkiosk.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "field_schemas",
    indices = [Index(value = ["key"], unique = true)]
)
data class FieldSchemaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val label: String,
    val enabled: Boolean = true,
    val required: Boolean = false,
    val inputType: String = "TEXT",
    val sortOrder: Int = 0
)
