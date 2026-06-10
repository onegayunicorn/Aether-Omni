package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "escrow_contracts")
data class EscrowContract(
    @PrimaryKey
    val contractId: String,
    val title: String,
    val amountAud: Double,
    val userRole: String, // "Buyer", "Seller", "Fiduciary Agent"
    val status: String,   // "PENDING", "ACTIVE", "RELEASE_AUTHORIZED", "COMPLETED"
    val jurisdiction: String = "Queensland, Australia",
    val complianceNotice: String = "Queensland Privacy Act 2009 - Section 33 Compliant",
    val escrowTokenHash: String, // 64-character SHA-256 tag validation hash
    val createdTimestamp: Long = System.currentTimeMillis()
) : Serializable
