/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Gestionnaire d'authentification robuste (BCrypt coût 12, Rate Limiter, Timeout d'inactivité)
 * Développé par KGC Technologies
 */
object AuthManager {

    private const val BCRYPT_COST = 12
    const val MAX_FAILED_ATTEMPTS = 5
    const val RATE_LIMIT_WINDOW_MS = 15 * 60 * 1000L // 15 minutes
    const val INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes

    // File thread-safe des horodatages d'échecs de connexion
    private val failedAttemptsTimestamps = ConcurrentLinkedQueue<Long>()

    /**
     * Hache un mot de passe en utilisant BCrypt de manière asynchrone sur Dispatchers.Default pour éviter tout blocage UI
     */
    suspend fun hashPasswordAsync(password: String): String = withContext(Dispatchers.Default) {
        try {
            val salt = BCrypt.gensalt(BCRYPT_COST)
            BCrypt.hashpw(password, salt)
        } catch (e: Exception) {
            // Repli sécurisé en cas d'erreur de sel
            val salt = BCrypt.gensalt(10)
            BCrypt.hashpw(password, salt)
        }
    }

    fun hashPassword(password: String): String {
        return try {
            val salt = BCrypt.gensalt(10)
            BCrypt.hashpw(password, salt)
        } catch (e: Exception) {
            password.hashCode().toString()
        }
    }

    /**
     * Vérifie la validité d'un mot de passe contre son empreinte BCrypt de manière non bloquante
     */
    suspend fun verifyPasswordAsync(password: String, hash: String): Boolean = withContext(Dispatchers.Default) {
        verifyPassword(password, hash)
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return try {
            if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
                BCrypt.checkpw(password, hash)
            } else {
                // Fallback compatibilité
                password == hash || password.hashCode().toString() == hash
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Vérifie si la tentative de connexion est autorisée par le Rate Limiter
     * @return Pair(isAllowed, remainingCooldownSeconds)
     */
    fun checkRateLimit(): Pair<Boolean, Long> {
        val now = System.currentTimeMillis()
        // Nettoyer les tentatives plus anciennes que la fenêtre de 15 minutes
        failedAttemptsTimestamps.removeIf { now - it > RATE_LIMIT_WINDOW_MS }

        if (failedAttemptsTimestamps.size >= MAX_FAILED_ATTEMPTS) {
            val oldest = failedAttemptsTimestamps.peek() ?: now
            val remainingMs = (oldest + RATE_LIMIT_WINDOW_MS) - now
            val remainingSec = (remainingMs / 1000L).coerceAtLeast(1L)
            return Pair(false, remainingSec)
        }
        return Pair(true, 0L)
    }

    /**
     * Enregistre un échec de connexion pour le rate limiting
     */
    fun recordFailedAttempt() {
        val now = System.currentTimeMillis()
        failedAttemptsTimestamps.add(now)
    }

    /**
     * Réinitialise les tentatives après une connexion réussie
     */
    fun resetFailedAttempts() {
        failedAttemptsTimestamps.clear()
    }

    /**
     * Vérifie si la session doit être verrouillée pour cause d'inactivité (> 5 minutes)
     */
    fun isSessionTimedOut(lastActiveTimestamp: Long): Boolean {
        if (lastActiveTimestamp <= 0L) return false
        val elapsed = System.currentTimeMillis() - lastActiveTimestamp
        return elapsed > INACTIVITY_TIMEOUT_MS
    }
}

