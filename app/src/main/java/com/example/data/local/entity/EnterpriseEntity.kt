/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entreprise")
data class EnterpriseEntity(
    @PrimaryKey
    val id: Long = 1,
    val nom: String = "Ets FORDANY",
    val logo_uri: String? = null,
    val devise: String = "USD",
    val couleur_accent: String = "#0284C7", // Bleu sobre professionnel
    val seuil_caisse_basse: Double = 100.0,
    val seuil_stock_bas: Double = 50.0,
    val date_modification: Long = System.currentTimeMillis()
)
