/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utilisateurs")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val login: String,
    val mdp_hash: String,
    val role: String, // "Admin", "Opérateur", "Lecteur"
    val actif: Int = 1,
    val date_creation: Long = System.currentTimeMillis(),
    val dernier_login: Long? = null
)
