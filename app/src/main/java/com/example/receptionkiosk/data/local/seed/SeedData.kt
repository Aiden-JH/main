package com.example.receptionkiosk.data.local.seed

import com.example.receptionkiosk.data.local.entity.FieldSchemaEntity
import com.example.receptionkiosk.data.local.entity.PurposeEntity

object SeedData {
    fun defaultPurposes(): List<PurposeEntity> = listOf(
        PurposeEntity(label = "Interview", enabled = true, sortOrder = 0),
        PurposeEntity(label = "Delivery", enabled = true, sortOrder = 1),
        PurposeEntity(label = "Guest", enabled = true, sortOrder = 2),
        PurposeEntity(label = "Other", enabled = true, sortOrder = 3)
    )

    fun defaultFields(): List<FieldSchemaEntity> = listOf(
        FieldSchemaEntity(key = "visitorName", label = "Visitor Name", required = true, inputType = "TEXT", sortOrder = 0),
        FieldSchemaEntity(key = "company", label = "Company", required = false, inputType = "TEXT", sortOrder = 1),
        FieldSchemaEntity(key = "hostName", label = "Host Name", required = true, inputType = "TEXT", sortOrder = 2),
        FieldSchemaEntity(key = "phone", label = "Phone", required = false, inputType = "PHONE", sortOrder = 3),
        FieldSchemaEntity(key = "note", label = "Note", required = false, inputType = "MULTILINE", sortOrder = 4)
    )
}
