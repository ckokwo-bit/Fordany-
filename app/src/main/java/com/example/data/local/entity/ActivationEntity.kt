/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activation")
data class ActivationEntity(
    @PrimaryKey
    val id: Long = 1,
    val device_id: String,
    val code_hash: String,
    val date_activation: Long = System.currentTimeMillis(),
    val licence_chiffree: String,
    val statut: String = "ACTIVE"
)
