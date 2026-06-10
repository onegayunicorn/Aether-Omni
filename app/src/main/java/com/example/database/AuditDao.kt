package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_events ORDER BY timestamp DESC")
    fun getAllAuditsFlow(): Flow<List<AuditEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: AuditEvent)

    @Query("DELETE FROM audit_events")
    suspend fun clearAudits()
}
