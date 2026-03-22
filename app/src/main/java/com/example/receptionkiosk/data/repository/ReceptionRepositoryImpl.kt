package com.example.receptionkiosk.data.repository

import com.example.receptionkiosk.data.local.dao.FieldSchemaDao
import com.example.receptionkiosk.data.local.dao.PurposeDao
import com.example.receptionkiosk.data.local.dao.VisitDao
import com.example.receptionkiosk.data.local.entity.FieldSchemaEntity
import com.example.receptionkiosk.data.local.entity.PurposeEntity
import com.example.receptionkiosk.data.local.entity.VisitEntity
import com.example.receptionkiosk.data.local.seed.SeedData
import com.example.receptionkiosk.data.prefs.SettingsPreferences
import com.example.receptionkiosk.domain.model.VisitStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ReceptionRepositoryImpl(
    private val purposeDao: PurposeDao,
    private val fieldSchemaDao: FieldSchemaDao,
    private val visitDao: VisitDao,
    private val settingsPreferences: SettingsPreferences
) : ReceptionRepository {

    override fun observePurposes(): Flow<List<PurposeEntity>> = purposeDao.observeAll()
    override fun observeEnabledPurposes(): Flow<List<PurposeEntity>> = purposeDao.observeEnabled()
    override fun observeFieldSchemas(): Flow<List<FieldSchemaEntity>> = fieldSchemaDao.observeAll()
    override fun observeEnabledFieldSchemas(): Flow<List<FieldSchemaEntity>> = fieldSchemaDao.observeEnabled()
    override fun observeVisits(): Flow<List<VisitEntity>> = visitDao.observeAll()
    override fun observeFailedVisits(): Flow<List<VisitEntity>> = visitDao.observeByStatus(VisitStatus.FAILED)

    override suspend fun getPurposeById(purposeId: Long): PurposeEntity? = purposeDao.getById(purposeId)
    override suspend fun getVisitById(visitId: Long): VisitEntity? = visitDao.getById(visitId)
    override suspend fun createVisit(visit: VisitEntity): Long = visitDao.insert(visit)

    override suspend fun markVisitSent(visitId: Long) {
        visitDao.updateSent(visitId, VisitStatus.SENT, System.currentTimeMillis())
    }

    override suspend fun markVisitFailed(visitId: Long, error: String) {
        visitDao.updateFailed(visitId, VisitStatus.FAILED, error.take(300))
    }

    override suspend fun setPurposeEnabled(purposeId: Long, enabled: Boolean) {
        purposeDao.setEnabled(purposeId, enabled)
    }

    override suspend fun setFieldEnabled(fieldId: Long, enabled: Boolean) {
        fieldSchemaDao.setEnabled(fieldId, enabled)
    }

    override suspend fun setFieldRequired(fieldId: Long, required: Boolean) {
        fieldSchemaDao.setRequired(fieldId, required)
    }

    override suspend fun seedDefaultsIfNeeded() {
        val alreadySeeded = settingsPreferences.isSeedDone.first()
        if (alreadySeeded) return

        if (purposeDao.count() == 0) purposeDao.insertAll(SeedData.defaultPurposes())
        if (fieldSchemaDao.count() == 0) fieldSchemaDao.insertAll(SeedData.defaultFields())
        settingsPreferences.setSeedDone(true)
    }
}
