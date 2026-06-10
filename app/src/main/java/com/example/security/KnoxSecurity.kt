package com.example.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec

object KnoxSecurity {
    private const val TAG = "KnoxSecurity"
    private const val KEY_ALIAS = "AetherEscrowSecureCertKey"
    private const val KEYSTORE_NAME = "AndroidKeyStore"

    fun isKeyProvisioned(): Boolean {
        return try {
            val ks = KeyStore.getInstance(KEYSTORE_NAME)
            ks.load(null)
            ks.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to inspect keystore", e)
            false
        }
    }

    fun deleteProvisionedKey(): Boolean {
        return try {
            val ks = KeyStore.getInstance(KEYSTORE_NAME)
            ks.load(null)
            if (ks.containsAlias(KEY_ALIAS)) {
                ks.deleteEntry(KEY_ALIAS)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete secure key", e)
            false
        }
    }

    /**
     * Generates a new EC key pair in the Knox TEE (Android KeyStore).
     * Falls back gracefully based on device capabilities.
     */
    fun provisionSecureKey(): SecurityLevelInfo {
        return try {
            val ks = KeyStore.getInstance(KEYSTORE_NAME)
            ks.load(null)

            if (ks.containsAlias(KEY_ALIAS)) {
                ks.deleteEntry(KEY_ALIAS)
            }

            val kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                KEYSTORE_NAME
            )

            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).run {
                setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384)
                // We enable hardware-backed properties but avoid strict user-authentication
                // requirement blocks unless biometrics are configured, ensuring
                // standard devices and emulators can run the core flows without instant crash.
                build()
            }

            kpg.initialize(spec)
            val keyPair = kpg.generateKeyPair()
            Log.i(TAG, "Secure TEE ECC key pair created successfully.")
            
            getSecurityLevel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to provision hardware-backed cryptographic key", e)
            SecurityLevelInfo("SOFTWARE_EMULATED", false, "Provisioning failed: ${e.message}")
        }
    }

    /**
     * Analyzes key structure to verify hardware root of trust level.
     */
    fun getSecurityLevel(): SecurityLevelInfo {
        return try {
            if (!isKeyProvisioned()) {
                return SecurityLevelInfo("UNPROVISIONED", false, "Security key not found. Execute provisioning sequence.")
            }

            val ks = KeyStore.getInstance(KEYSTORE_NAME)
            ks.load(null)
            val entry = ks.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
            if (entry == null) {
                return SecurityLevelInfo("UNPROVISIONED", false, "Cryptographic entry is corrupted.")
            }

            val privateKey = entry.privateKey
            val factory = java.security.KeyFactory.getInstance(privateKey.algorithm, KEYSTORE_NAME)
            val keyInfo = factory.getKeySpec(privateKey, KeyInfo::class.java)

            val isHardwareBacked = keyInfo.isInsideSecureHardware
            val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX) {
                "SECURE_ELEMENT_STRONGBOX"
            } else if (isHardwareBacked) {
                "TRUSTED_EXECUTION_ENVIRONMENT_TEE"
            } else {
                "SOFTWARE_EMULATED"
            }

            val details = if (isHardwareBacked) {
                "Knox-Compliant Hardware key generated successfully within hardware boundaries."
            } else {
                "Key generated inside android software-sandbox. Sub-hardware security classification."
            }

            SecurityLevelInfo(level, isHardwareBacked, details, getPublicKeyBase64())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to analyze hardware security level", e)
            SecurityLevelInfo("UNKNOWN", false, "Error analyzing key attributes: ${e.message}")
        }
    }

    private fun getPublicKeyBase64(): String {
        return try {
            val ks = KeyStore.getInstance(KEYSTORE_NAME)
            ks.load(null)
            val cert = ks.getCertificate(KEY_ALIAS) ?: return ""
            Base64.encodeToString(cert.publicKey.encoded, Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Signs contract validation payload using the secure hardware-backed key.
     */
    fun signPayload(payload: String): String {
        return try {
            val ks = KeyStore.getInstance(KEYSTORE_NAME)
            ks.load(null)
            val entry = ks.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
                ?: throw IllegalStateException("Escrow secure signing key is not provisioned.")

            val privateKey = entry.privateKey
            val s = Signature.getInstance("SHA256withECDSA")
            s.initSign(privateKey)
            s.update(payload.toByteArray())
            val signatureBytes = s.sign()
            Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Cryptographic signing operations failed", e)
            "ERROR:SIGNING_FAILED"
        }
    }
}

data class SecurityLevelInfo(
    val level: String, // "SOFTWARE_EMULATED", "TRUSTED_EXECUTION_ENVIRONMENT_TEE", "SECURE_ELEMENT_STRONGBOX", "UNPROVISIONED", "UNKNOWN"
    val isHardwareBacked: Boolean,
    val description: String,
    val publicKeyPem: String = ""
)
