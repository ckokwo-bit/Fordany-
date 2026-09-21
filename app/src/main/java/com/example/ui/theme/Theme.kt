/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FordanyColorScheme = lightColorScheme(
    primary = FordanyCyan,
    onPrimary = Color.White,
    primaryContainer = FordanyNavyLight,
    onPrimaryContainer = Color.White,
    secondary = FordanyEmerald,
    onSecondary = Color.White,
    tertiary = FordanyAmber,
    onTertiary = FordanyNavy,
    background = FordanyBg,
    onBackground = FordanyTextPrimary,
    surface = FordanySurface,
    onSurface = FordanyTextPrimary,
    error = FordanyRose,
    onError = Color.White
)

@Composable
fun FordanyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FordanyColorScheme,
        typography = Typography,
        content = content
    )
}
