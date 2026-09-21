/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    // Les 5 écrans principaux
    data object Login : Screen("login", "Connexion", Icons.Default.Lock)
    data object Dashboard : Screen("dashboard", "Tableau de bord", Icons.Default.Dashboard)
    data object NewOperation : Screen("new_operation", "Nouvelle opération", Icons.Default.AddCircle)
    data object History : Screen("history", "Historique", Icons.Default.History)
    data object Settings : Screen("settings", "Paramètres", Icons.Default.Settings)

    // Écrans du tiroir latéral (Drawer)
    data object Accounts : Screen("accounts", "Gestion des Comptes", Icons.Default.ManageAccounts)
    data object Products : Screen("products", "Gestion des Produits", Icons.Default.Inventory)
    data object Analytics : Screen("analytics", "Analyses & Statistiques", Icons.Default.Analytics)

    companion object {
        // Les 4 écrans de la barre de navigation inférieure principale
        val bottomNavScreens: List<Screen> by lazy {
            listOf(Dashboard, NewOperation, History, Settings)
        }
    }
}
