package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_events")
data class AuditEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val auditId: String,       // Custom UUID code like AUD-20260610-abcd
    val eventType: String,     // KEY_GEN, BIOMETRIC_SUCCESS, NFC_READ, NFC_EMULATE, SYNC_SUCCESS
    val contractId: String?,
    val status: String,        // SUCCESS, FAILED, ERROR
    val timestamp: Long = System.currentTimeMillis(),
    val details: String,       // Action JSON details / readable message
    val signatureHash: String  // Cryptographic digest of this record to prevent database tampering
)
