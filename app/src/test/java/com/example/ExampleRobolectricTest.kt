package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.AuthManager
import com.example.security.CryptoManager
import com.example.security.JwtManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertTrue(appName.contains("FORDANY"))
  }

  @Test
  fun `test AES-256-GCM encryption and decryption`() {
    val sensitiveData = "Paiement Airtel Money 50000 CDF"
    val encrypted = CryptoManager.encrypt(sensitiveData)
    assertNotNull(encrypted)
    assertTrue(encrypted.isNotBlank())
    val decrypted = CryptoManager.decrypt(encrypted)
    assertEquals(sensitiveData, decrypted)
  }

  @Test
  fun `test JWT generation and token decoding`() {
    val tokenPair = JwtManager.generateTokenPair(userId = 1L, userName = "Admin Fordany", role = "Admin")
    assertNotNull(tokenPair.accessToken)
    assertNotNull(tokenPair.refreshToken)
    val decoded = JwtManager.validateToken(tokenPair.accessToken)
    assertNotNull(decoded)
    assertEquals(1L, decoded?.userId)
    assertEquals("Admin Fordany", decoded?.userName)
    assertEquals("Admin", decoded?.role)
  }

  @Test
  fun `test password hashing and verification`() = runBlocking {
    val password = "FordanySecure2026!"
    val hash = AuthManager.hashPasswordAsync(password)
    assertNotNull(hash)
    assertTrue(AuthManager.verifyPasswordAsync(password, hash))
    assertFalse(AuthManager.verifyPasswordAsync("wrongpassword", hash))
  }

  @Test
  fun `test rate limiting`() {
    AuthManager.resetFailedAttempts()
    val check1 = AuthManager.checkRateLimit()
    assertTrue(check1.first)

    // Simuler 5 échecs
    repeat(5) {
      AuthManager.recordFailedAttempt()
    }

    val checkBlocked = AuthManager.checkRateLimit()
    assertFalse(checkBlocked.first)
    assertTrue(checkBlocked.second > 0)

    // Reset
    AuthManager.resetFailedAttempts()
    val checkAfterReset = AuthManager.checkRateLimit()
    assertTrue(checkAfterReset.first)
  }
}


