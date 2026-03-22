package com.example.receptionkiosk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.receptionkiosk.data.local.entity.VisitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VisitEntity): Long

    @Query("SELECT * FROM visits ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE id = :visitId LIMIT 1")
    suspend fun getById(visitId: Long): VisitEntity?

    @Query("SELECT * FROM visits WHERE status = :status ORDER BY createdAtEpochMillis DESC")
    fun observeByStatus(status: String): Flow<List<VisitEntity>>

    @Query("UPDATE visits SET status = :status, sentAtEpochMillis = :sentAt WHERE id = :visitId")
    suspend fun updateSent(visitId: Long, status: String, sentAt: Long)

    @Query("UPDATE visits SET status = :status, lastError = :error, retryCount = retryCount + 1 WHERE id = :visitId")
    suspend fun updateFailed(visitId: Long, status: String, error: String)
}
