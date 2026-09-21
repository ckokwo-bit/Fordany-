/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.security

import android.util.Base64
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Gestionnaire de Jetons JWT HS256 (expiration 8 heures) & Refresh Tokens
 * Développé par KGC Technologies
 */
object JwtManager {

    private const val HMAC_ALGORITHM = "HmacSHA256"
    private const val SECRET = "KGC_TECHNOLOGIES_FORDANY_MANAGEMENT_SECRET_KEY_2026_DRC_RDC"
    const val EXPIRATION_MILLIS = 8 * 60 * 60 * 1000L // 8 heures

    data class TokenClaims(
        val userId: Long,
        val userName: String,
        val role: String,
        val issuedAt: Long,
        val expiresAt: Long
    )

    data class AuthTokenPair(
        val accessToken: String,
        val refreshToken: String,
        val expiresAt: Long
    )

    /**
     * Génère un jeton JWT HS256 valide pour 8 heures
     */
    fun generateTokenPair(userId: Long, userName: String, role: String): AuthTokenPair {
        val now = System.currentTimeMillis()
        val exp = now + EXPIRATION_MILLIS

        val headerJson = JSONObject().apply {
            put("alg", "HS256")
            put("typ", "JWT")
        }

        val payloadJson = JSONObject().apply {
            put("sub", userId)
            put("name", userName)
            put("role", role)
            put("iat", now)
            put("exp", exp)
        }

        val headerEncoded = base64UrlEncode(headerJson.toString().toByteArray(Charsets.UTF_8))
        val payloadEncoded = base64UrlEncode(payloadJson.toString().toByteArray(Charsets.UTF_8))
        val signatureInput = "$headerEncoded.$payloadEncoded"

        val signature = signHmacSha256(signatureInput)
        val accessToken = "$signatureInput.$signature"

        // Refresh token aléatoire sécurisé
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)
        val refreshToken = Base64.encodeToString(randomBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

        return AuthTokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = exp
        )
    }

    /**
     * Valide un jeton JWT HS256 et extrait les revendications (claims)
     */
    fun validateToken(token: String): TokenClaims? {
        try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val headerEncoded = parts[0]
            val payloadEncoded = parts[1]
            val receivedSignature = parts[2]

            val expectedSignature = signHmacSha256("$headerEncoded.$payloadEncoded")
            if (receivedSignature != expectedSignature) return null

            val payloadJson = JSONObject(String(base64UrlDecode(payloadEncoded), Charsets.UTF_8))
            val exp = payloadJson.getLong("exp")

            if (System.currentTimeMillis() > exp) {
                return null // Token expiré (après 8h)
            }

            return TokenClaims(
                userId = payloadJson.getLong("sub"),
                userName = payloadJson.getString("name"),
                role = payloadJson.getString("role"),
                issuedAt = payloadJson.getLong("iat"),
                expiresAt = exp
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun signHmacSha256(data: String): String {
        val key = SecretKeySpec(SECRET.toByteArray(Charsets.UTF_8), HMAC_ALGORITHM)
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(key)
        val signatureBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return base64UrlEncode(signatureBytes)
    }

    private fun base64UrlEncode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    private fun base64UrlDecode(str: String): ByteArray {
        return Base64.decode(str, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}
