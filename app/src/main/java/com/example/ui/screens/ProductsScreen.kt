/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.components.KgcFooter
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    currency: String,
    products: List<ProductEntity>,
    accounts: List<AccountEntity>,
    currentUser: UserEntity?,
    onAddProduct: (nom: String, prixVente: Double, prixAchat: Double, sourceId: Long?, destId: Long?) -> Unit,
    onUpdateProduct: (ProductEntity) -> Unit
) {
    val isAdmin = currentUser?.role == "Admin"
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    var nomInput by remember { mutableStateOf("") }
    var prixVenteInput by remember { mutableStateOf("") }
    var prixAchatInput by remember { mutableStateOf("") }
    var sourceCompteId by remember { mutableStateOf<Long?>(null) }
    var destCompteId by remember { mutableStateOf<Long?>(null) }

    val accountMap = remember(accounts) { accounts.associateBy { it.id } }
    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale.FRENCH).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
    }

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        nomInput = ""
                        prixVenteInput = ""
                        prixAchatInput = ""
                        sourceCompteId = null
                        destCompteId = null
                        showDialog = true
                    },
                    containerColor = Color(0xFF06D6A0),
                    modifier = Modifier.testTag("add_product_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter un produit", tint = Color.White)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Catalogue des Produits & Forfaits",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Forfaits data, SIM, Unités Flash - Prix de vente, prix d'achat et marges",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }
            }

            if (products.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aucun produit dans le catalogue. Cliquez sur le bouton vert '+' pour ajouter un produit (ex: Forfait 1Go, SIM Airtel).",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                        )
                    }
                }
            } else {
                items(products, key = { it.id }) { prod ->
                    val marge = prod.prix_vente - prod.prix_achat

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFFDCFCE7), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF16A34A))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = prod.nom,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        )
                                        Text(
                                            text = "ID: #${prod.id}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                        )
                                    }
                                }

                                if (isAdmin) {
                                    IconButton(
                                        onClick = {
                                            editingProduct = prod
                                            nomInput = prod.nom
                                            prixVenteInput = prod.prix_vente.toString()
                                            prixAchatInput = prod.prix_achat.toString()
                                            sourceCompteId = prod.compte_source_id
                                            destCompteId = prod.compte_dest_id
                                            showDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Color(0xFF64748B))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Prix de Vente", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B)))
                                    Text(
                                        "${numberFormat.format(prod.prix_vente)} $currency",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }

                                Column {
                                    Text("Prix d'Achat", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B)))
                                    Text(
                                        "${numberFormat.format(prod.prix_achat)} $currency",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Marge Unitaire", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF06D6A0)))
                                    Text(
                                        "+${numberFormat.format(marge)} $currency",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF16A34A)
                                    )
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
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(if (editingProduct == null) "Ajouter un Produit" else "Modifier le Produit")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nomInput,
                        onValueChange = { nomInput = it },
                        label = { Text("Nom du produit (ex: Forfait Airtel 1 Go)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = prixVenteInput,
                        onValueChange = { prixVenteInput = it },
                        label = { Text("Prix de vente ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = prixAchatInput,
                        onValueChange = { prixAchatInput = it },
                        label = { Text("Prix d'achat ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pv = prixVenteInput.toDoubleOrNull() ?: 0.0
                        val pa = prixAchatInput.toDoubleOrNull() ?: 0.0
                        if (nomInput.isNotBlank()) {
                            val current = editingProduct
                            if (current == null) {
                                onAddProduct(nomInput, pv, pa, sourceCompteId, destCompteId)
                            } else {
                                onUpdateProduct(
                                    current.copy(
                                        nom = nomInput.trim(),
                                        prix_vente = pv,
                                        prix_achat = pa,
                                        compte_source_id = sourceCompteId,
                                        compte_dest_id = destCompteId
                                    )
                                )
                            }
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0))
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
