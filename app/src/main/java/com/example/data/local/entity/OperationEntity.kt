/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "Vente", "Dépense", "Don", "Achat"
    val produit_id: Long? = null,
    val compte_id: Long,
    val quantite: Double = 1.0,
    val montant: Double = 0.0,
    val motif_chiffre: String = "", // Chiffré en AES-256-GCM
    val date: Long = System.currentTimeMillis(),
    val utilisateur_id: Long,
    val annulee: Int = 0, // 0 = active, 1 = annulée (soft delete)
    val raison_annulation: String? = null,
    val photo_preuve: String? = null, // Chemin local du fichier de preuve photo
    val date_creation: Long = System.currentTimeMillis()
)
