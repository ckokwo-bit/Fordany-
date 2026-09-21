/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.security

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.example.data.local.dao.AppDao
import com.example.data.local.entity.ActivationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.security.MessageDigest

/**
 * Système d'activation matériel KGC Technologies.
 * Protège l'application contre les copies illégales d'APK en liant la licence
 * à l'identifiant matériel unique (Device ID) et en chiffrant l'empreinte avec AES-256-GCM.
 */
object KgcActivationManager {

    // Code secret d'activation requis
    const val SECRET_ACTIVATION_CODE = "kokwokgc.2010"

    // Empreinte BCrypt de référence pour "kokwokgc.2010"
    private const val KGC_CODE_HASH = "\$2a\$10\$v17l8bZtX9E1pIq888d17.Lp9c5rL0B2B29K2D.K9vJpLq9W0Z1.8"

    private const val LICENSE_FILENAME = "kgc_license.dat"

    /**
     * Récupère l'empreinte matérielle unique du téléphone (Device ID).
     */
    @SuppressLint("HardwareIds")
    fun getDeviceFingerprint(context: Context): String {
        return try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "kgc_default_id"

            val rawSignature = "${androidId}_${Build.MANUFACTURER}_${Build.MODEL}_KGC_SECURITY"
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(rawSignature.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        } catch (e: Exception) {
            "KGC-DEV-8A9F4C2E"
        }
    }

    /**
     * Vérifie si le code d'activation fourni correspond au code secret KGC (BCrypt).
     */
    fun verifyActivationCode(inputCode: String): Boolean {
        val trimmed = inputCode.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed == SECRET_ACTIVATION_CODE) return true
        return try {
            BCrypt.checkpw(trimmed, KGC_CODE_HASH)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vérifie si l'application est activée et valide sur cet appareil physique.
     * Si l'APK a été copié sur un autre appareil, l'empreinte ne correspondra pas
     * et l'accès sera immédiatement bloqué ("Application non activée").
     */
    suspend fun checkLicenseValidity(context: Context, dao: AppDao): Boolean = withContext(Dispatchers.IO) {
        try {
            val currentDeviceId = getDeviceFingerprint(context)

            // 1. Vérification dans la base SQLite
            val dbActivation = dao.getActivation() ?: return@withContext false
            if (dbActivation.device_id != currentDeviceId) {
                return@withContext false
            }

            // 2. Déchiffrement AES-256-GCM du payload de licence
            val decryptedPayload = CryptoManager.decrypt(dbActivation.licence_chiffree)
            if (!decryptedPayload.contains(currentDeviceId) || !decryptedPayload.contains("KGC_ACTIVATED")) {
                return@withContext false
            }

            // 3. Vérification du fichier physique de licence
            val licenseFile = File(context.filesDir, LICENSE_FILENAME)
            if (!licenseFile.exists()) {
                // Régénération du fichier physique si présent en base
                licenseFile.writeText(dbActivation.licence_chiffree)
            } else {
                val fileContent = licenseFile.readText()
                val decryptedFile = CryptoManager.decrypt(fileContent)
                if (!decryptedFile.contains(currentDeviceId)) {
                    return@withContext false
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Active l'application sur le téléphone avec le code secret.
     * Chiffre la licence avec AES-256-GCM et la persiste.
     */
    suspend fun activateApp(context: Context, dao: AppDao, code: String): Boolean = withContext(Dispatchers.IO) {
        if (!verifyActivationCode(code)) {
            return@withContext false
        }

        try {
            val currentDeviceId = getDeviceFingerprint(context)
            val codeHash = AuthManager.hashPassword(SECRET_ACTIVATION_CODE)
            val timestamp = System.currentTimeMillis()

            // Payload sensible chiffré en AES-256-GCM
            val licensePayload = "KGC_ACTIVATED::$currentDeviceId::$timestamp::FORDANY_V1"
            val encryptedLicense = CryptoManager.encrypt(licensePayload)

            // Enregistrement en base de données
            dao.insertActivation(
                ActivationEntity(
                    id = 1,
                    device_id = currentDeviceId,
                    code_hash = codeHash,
                    date_activation = timestamp,
                    licence_chiffree = encryptedLicense,
                    statut = "ACTIVE"
                )
            )

            // Enregistrement dans le fichier de licence local
            val licenseFile = File(context.filesDir, LICENSE_FILENAME)
            licenseFile.writeText(encryptedLicense)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
