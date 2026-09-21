/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.OperationEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.security.CryptoManager
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportManager {

    fun exportOperationsCsv(
        context: Context,
        operations: List<OperationEntity>,
        accounts: List<AccountEntity>,
        products: List<ProductEntity>,
        users: List<UserEntity>,
        currency: String
    ): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "FORDANY_Operations_$timeStamp.csv")

            val accountMap = accounts.associateBy { it.id }
            val productMap = products.associateBy { it.id }
            val userMap = users.associateBy { it.id }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRENCH)

            FileWriter(file).use { writer ->
                // En-tête KGC Technologies
                writer.append("# FORDANY MANAGEMENT - Export des opérations\n")
                writer.append("# Ets FORDANY - Développé par KGC Technologies\n")
                writer.append("# Date d'export : ${dateFormat.format(Date())}\n")
                writer.append("ID;Date;Type;Compte;Produit;Quantité;Montant ($currency);Motif;Utilisateur;Statut;Raison Annulation\n")

                for (op in operations) {
                    val dateStr = dateFormat.format(Date(op.date))
                    val compteNom = accountMap[op.compte_id]?.nom ?: "Compte #${op.compte_id}"
                    val produitNom = op.produit_id?.let { productMap[it]?.nom } ?: "-"
                    val userNom = userMap[op.utilisateur_id]?.nom ?: "Utilisateur #${op.utilisateur_id}"
                    val motifClair = CryptoManager.decrypt(op.motif_chiffre).replace(";", ",")
                    val statut = if (op.annulee == 1) "ANNULÉE" else "ACTIVE"
                    val raison = op.raison_annulation?.replace(";", ",") ?: "-"

                    writer.append("${op.id};$dateStr;${op.type};$compteNom;$produitNom;${op.quantite};${op.montant};\"$motifClair\";$userNom;$statut;\"$raison\"\n")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String, chooserTitle: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
