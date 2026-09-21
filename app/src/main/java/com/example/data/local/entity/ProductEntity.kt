/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produits")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val prix_vente: Double = 0.0,
    val prix_achat: Double = 0.0,
    val compte_source_id: Long? = null,
    val compte_dest_id: Long? = null,
    val actif: Int = 1
)
