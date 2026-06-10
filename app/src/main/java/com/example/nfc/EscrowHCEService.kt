package com.example.nfc

import android.content.Intent
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.example.database.AppDatabase
import com.example.database.AuditEvent
import com.example.security.KnoxSecurity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class EscrowHCEService : HostApduService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private const val TAG = "EscrowHCEService"
        
        // Allowed SELECT APDU command: 00 A4 04 00 07 F0 01 02 03 04 05 06
        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 0x07.toByte(),
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )

        // READ / RETRIEVE APDU command: 80 10 00 00 00 (proprietary APDU)
        private val GET_DATA_APDU = byteArrayOf(
            0x80.toByte(), 0x10.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
        )

        // Global state of what is mounted currently in HCE
        @Volatile
        var mountedContractId: String? = null
        
        @Volatile
        var mountedTokenHash: String? = null

        @Volatile
        var mountedTitle: String = "No Active Contract"
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return byteArrayOf(0x6F.toByte(), 0x00.toByte()) // File not found / general error
        }

        Log.i(TAG, "Command received: ${bytesToHex(commandApdu)}")

        // Handle SELECT APDU
        if (commandApdu.contentEquals(SELECT_APDU)) {
            Log.i(TAG, "Aether-Omni SELECT AID processed successfully.")
            logAudit("NFC_EMULATE_SELECT", "SUCCESS", "Aether Applet Selected. Ready to transmit.")
            // Return 9000 (Success status word)
            return byteArrayOf(0x90.toByte(), 0x00.toByte())
        }

        // Handle GET DATA APDU (reads the current mounted contract + signature)
        if (commandApdu.size >= 5 && commandApdu[0] == 0x80.toByte() && commandApdu[1] == 0x10.toByte()) {
            val contractId = mountedContractId
            val tokenHash = mountedTokenHash

            if (contractId == null || tokenHash == null) {
                Log.w(TAG, "NFC read requested but no active contract is mounted in HCE.")
                logAudit("NFC_EMULATE_READ", "FAILED", "Read rejected: No contract mounted.")
                return byteArrayOf(0x6A.toByte(), 0x88.toByte()) // Referenced data not found
            }

            // 1. Generate validation transaction payload
            val timestamp = System.currentTimeMillis()
            val securityInfo = KnoxSecurity.getSecurityLevel()
            val rawPayload = "$contractId:$tokenHash:$timestamp:${securityInfo.level}"

            // 2. Sign transaction with secure hardware keystore
            val signature = KnoxSecurity.signPayload(rawPayload)
            
            // 3. Package response string
            // Format: "CONTRACT=<id>;HASH=<hash>;TIME=<time>;SIG=<sig>"
            val responseString = "CONTRACT=$contractId;HASH=$tokenHash;TIME=$timestamp;SIG=$signature"
            val responseBytes = responseString.toByteArray(Charsets.UTF_8)

            Log.i(TAG, "Transmitting signed escrow validation token over NFC Dep: $responseString")
            logAudit("NFC_EMULATE_READ", "SUCCESS", "NFC client emulated escrow release signed.")

            // Append 9000 status (successful)
            val out = ByteArray(responseBytes.size + 2)
            System.arraycopy(responseBytes, 0, out, 0, responseBytes.size)
            out[out.size - 2] = 0x90.toByte()
            out[out.size - 1] = 0x00.toByte()
            return out
        }

        // Unknown command APDU
        Log.w(TAG, "ISO-7816 Command mismatch.")
        return byteArrayOf(0x6D.toByte(), 0x00.toByte()) // Instruction code not supported
    }

    override fun onDeactivated(reason: Int) {
        Log.i(TAG, "NFC Session Deactivated. Reason: $reason")
        logAudit("NFC_EMULATE_DISCONNECT", "SUCCESS", "Card Emulation channel disconnected: Status code $reason")
    }

    private fun logAudit(eventType: String, status: String, message: String) {
        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val auditId = "AUD-${System.currentTimeMillis() / 1000}-${UUID.randomUUID().toString().take(4)}"
                
                // Chain signature
                val auditPayload = "$auditId:$eventType:$status:$message"
                val signatureHash = KnoxSecurity.signPayload(auditPayload)

                db.auditDao().insertAudit(
                    AuditEvent(
                        auditId = auditId,
                        eventType = eventType,
                        contractId = mountedContractId,
                        status = status,
                        details = message,
                        signatureHash = signatureHash
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "HCE failed to write audit trail", e)
            }
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }
}
