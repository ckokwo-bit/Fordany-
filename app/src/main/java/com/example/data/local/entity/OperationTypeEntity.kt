/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "types_operation")
data class OperationTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String, // ex: "VENTE", "DEPENSE", "REVENU", "DON", "ACHAT", "TRANSFERT"
    val nom: String,
    val sens: String, // "ENTREE", "SORTIE", "NEUTRE"
    val actif: Int = 1,
    val ordre: Int = 0
)
