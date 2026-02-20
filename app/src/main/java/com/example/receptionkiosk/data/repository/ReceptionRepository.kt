package com.example.receptionkiosk.data.repository

import com.example.receptionkiosk.data.local.entity.FieldSchemaEntity
import com.example.receptionkiosk.data.local.entity.PurposeEntity
import com.example.receptionkiosk.data.local.entity.VisitEntity
import kotlinx.coroutines.flow.Flow

interface ReceptionRepository {
    fun observePurposes(): Flow<List<PurposeEntity>>
    fun observeEnabledPurposes(): Flow<List<PurposeEntity>>
    fun observeFieldSchemas(): Flow<List<FieldSchemaEntity>>
    fun observeEnabledFieldSchemas(): Flow<List<FieldSchemaEntity>>
    fun observeVisits(): Flow<List<VisitEntity>>
    fun observeFailedVisits(): Flow<List<VisitEntity>>

    suspend fun getPurposeById(purposeId: Long): PurposeEntity?
    suspend fun getVisitById(visitId: Long): VisitEntity?
    suspend fun createVisit(visit: VisitEntity): Long
    suspend fun markVisitSent(visitId: Long)
    suspend fun markVisitFailed(visitId: Long, error: String)
    suspend fun setPurposeEnabled(purposeId: Long, enabled: Boolean)
    suspend fun setFieldEnabled(fieldId: Long, enabled: Boolean)
    suspend fun setFieldRequired(fieldId: Long, required: Boolean)
    suspend fun seedDefaultsIfNeeded()
}
