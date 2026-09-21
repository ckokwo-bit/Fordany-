/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.OperationEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.security.CryptoManager
import com.example.ui.components.KgcFooter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    currency: String,
    operations: List<OperationEntity>,
    accounts: List<AccountEntity>,
    products: List<ProductEntity>,
    users: List<UserEntity>,
    currentUser: UserEntity?,
    onCancelOperation: (opId: Long, raison: String) -> Unit,
    onExportCsv: (Context) -> Unit
) {
    val context = LocalContext.current
    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.FRENCH).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
    }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH) }

    val accountMap = remember(accounts) { accounts.associateBy { it.id } }
    val productMap = remember(products) { products.associateBy { it.id } }
    val userMap = remember(users) { users.associateBy { it.id } }

    // Filtres
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("Tous") }
    var selectedAccountIdFilter by remember { mutableStateOf<Long?>(null) }
    var showOnlyActive by remember { mutableStateOf(false) }

    // Pagination
    val pageSize = 15
    var currentPage by remember { mutableStateOf(0) }

    // Dialogue d'annulation (Soft delete)
    var operationToCancel by remember { mutableStateOf<OperationEntity?>(null) }
    var cancelReasonText by remember { mutableStateOf("") }
    var previewPhotoPath by remember { mutableStateOf<String?>(null) }

    // Filtrage des opérations
    val filteredOps = remember(operations, searchQuery, selectedTypeFilter, selectedAccountIdFilter, showOnlyActive) {
        operations.filter { op ->
            val matchType = if (selectedTypeFilter == "Tous") true else op.type == selectedTypeFilter
            val matchAccount = if (selectedAccountIdFilter == null) true else op.compte_id == selectedAccountIdFilter
            val matchActive = if (showOnlyActive) op.annulee == 0 else true

            val motifDecrypted = CryptoManager.decrypt(op.motif_chiffre)
            val productName = op.produit_id?.let { productMap[it]?.nom } ?: ""
            val matchQuery = if (searchQuery.isBlank()) true else {
                motifDecrypted.contains(searchQuery, ignoreCase = true) ||
                        productName.contains(searchQuery, ignoreCase = true) ||
                        op.montant.toString().contains(searchQuery)
            }

            matchType && matchAccount && matchActive && matchQuery
        }
    }

    val totalPages = (filteredOps.size + pageSize - 1) / pageSize.coerceAtLeast(1)
    val paginatedOps = remember(filteredOps, currentPage) {
        val start = currentPage * pageSize
        filteredOps.drop(start).take(pageSize)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // En-tête & Barre d'action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Historique & Traçabilité",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                )
                Text(
                    text = "${filteredOps.size} opération(s) trouvée(s)",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )
            }

            // Bouton Export CSV
            Button(
                onClick = { onExportCsv(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("export_csv_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export CSV", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }
        }

        // Zone des Filtres
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Recherche
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        currentPage = 0
                    },
                    placeholder = { Text("Rechercher motif, produit, montant...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filtres rapides de type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Tous", "Vente", "Dépense", "Don", "Achat").forEach { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = {
                                selectedTypeFilter = type
                                currentPage = 0
                            },
                            label = { Text(type, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Liste des opérations paginée
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (paginatedOps.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Aucune opération ne correspond aux critères.", color = Color(0xFF64748B))
                        }
                    }
                }
            } else {
                items(paginatedOps, key = { it.id }) { op ->
                    val compteNom = accountMap[op.compte_id]?.nom ?: "Compte #${op.compte_id}"
                    val prodNom = op.produit_id?.let { productMap[it]?.nom }
                    val userNom = userMap[op.utilisateur_id]?.nom ?: "Utilisateur #${op.utilisateur_id}"
                    val motifClair = CryptoManager.decrypt(op.motif_chiffre)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (op.annulee == 1) Color(0xFFF1F5F9) else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (op.annulee == 1) 0.dp else 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Badge type
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                when (op.type) {
                                                    "Vente" -> Color(0xFFDCFCE7)
                                                    "Dépense" -> Color(0xFFFEE2E2)
                                                    "Don" -> Color(0xFFFEF3C7)
                                                    else -> Color(0xFFE0F2FE)
                                                },
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = op.type,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = when (op.type) {
                                                "Vente" -> Color(0xFF16A34A)
                                                "Dépense" -> Color(0xFFDC2626)
                                                "Don" -> Color(0xFFD97706)
                                                else -> Color(0xFF0284C7)
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "#${op.id} • ${dateFormat.format(Date(op.date))}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                    )
                                }

                                Text(
                                    text = "${if (op.type == "Vente") "+" else "-"}${numberFormat.format(op.montant)} $currency",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (op.annulee == 1) Color.Gray else if (op.type == "Vente") Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Compte : $compteNom" + (if (prodNom != null) " | Produit : $prodNom (x${op.quantite})" else ""),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    )
                                    if (motifClair.isNotBlank()) {
                                        Text(
                                            text = "Motif : $motifClair",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                        )
                                    }
                                    Text(
                                        text = "Enregistré par : $userNom",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                    )
                                }

                                // Statut & Annulation
                                if (op.annulee == 1) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFFE4E6), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ANNULÉE",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFBE123C),
                                                fontSize = 10.sp
                                            )
                                        }
                                        op.raison_annulation?.let { raison ->
                                            Text(
                                                text = raison,
                                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFBE123C), fontSize = 10.sp)
                                            )
                                        }
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (op.photo_preuve != null) {
                                            IconButton(
                                                onClick = { previewPhotoPath = op.photo_preuve },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoCamera,
                                                    contentDescription = "Voir la preuve photo",
                                                    tint = Color(0xFF00B4D8),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }

                                        // Bouton Annuler (Soft delete)
                                        if (currentUser != null && currentUser.role != "Lecteur") {
                                            OutlinedButton(
                                                onClick = {
                                                    operationToCancel = op
                                                    cancelReasonText = ""
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF476F))
                                            ) {
                                                Text("Annuler", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                KgcFooter()
            }
        }

        // Pagination en bas
        if (totalPages > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (currentPage > 0) currentPage-- },
                    enabled = currentPage > 0
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                    Text("Précédent")
                }

                Text(
                    text = "Page ${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedButton(
                    onClick = { if (currentPage < totalPages - 1) currentPage++ },
                    enabled = currentPage < totalPages - 1
                ) {
                    Text("Suivant")
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }

    // Dialogue de confirmation d'annulation (Soft Delete avec trace)
    if (operationToCancel != null) {
        AlertDialog(
            onDismissRequest = { operationToCancel = null },
            title = { Text("Annuler l'opération #${operationToCancel?.id} ?") },
            text = {
                Column {
                    Text(
                        text = "Conformément à la règle de gestion, l'opération ne sera jamais supprimée physiquement mais archivée avec son motif pour traçabilité comptable.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cancelReasonText,
                        onValueChange = { cancelReasonText = it },
                        label = { Text("Raison de l'annulation *") },
                        placeholder = { Text("Ex: Erreur de saisie client, doublon...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val op = operationToCancel
                        if (op != null && cancelReasonText.isNotBlank()) {
                            onCancelOperation(op.id, cancelReasonText)
                            operationToCancel = null
                        }
                    },
                    enabled = cancelReasonText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF476F))
                ) {
                    Text("Confirmer l'annulation", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { operationToCancel = null }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Modal de prévisualisation de la preuve photo
    if (previewPhotoPath != null) {
        val bitmap = remember(previewPhotoPath) {
            try {
                BitmapFactory.decodeFile(previewPhotoPath)
            } catch (e: Exception) {
                null
            }
        }
        AlertDialog(
            onDismissRequest = { previewPhotoPath = null },
            title = {
                Text(
                    "Preuve Photo / Reçu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Preuve photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("Impossible de charger l'image de la preuve.", color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { previewPhotoPath = null }) {
                    Text("Fermer", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
