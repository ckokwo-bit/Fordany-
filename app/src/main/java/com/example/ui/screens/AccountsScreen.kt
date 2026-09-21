/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SimCard
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
import androidx.compose.material3.OutlinedButton
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
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AccountWithBalance
import com.example.ui.components.KgcFooter
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    currency: String,
    accounts: List<AccountWithBalance>,
    currentUser: UserEntity?,
    onAddAccount: (nom: String, type: String, soldeInitial: Double) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit
) {
    val isAdmin = currentUser?.role == "Admin"
    var showDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }

    var nomInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("ARGENT") }
    var soldeInitialInput by remember { mutableStateOf("0") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

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
                        editingAccount = null
                        nomInput = ""
                        typeInput = "ARGENT"
                        soldeInitialInput = "0"
                        showDialog = true
                    },
                    containerColor = Color(0xFF00B4D8),
                    modifier = Modifier.testTag("add_account_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter un compte", tint = Color.White)
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
                        text = "Gestion des Comptes",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Text(
                        text = "Règle d'or : solde recalculé dynamiquement = solde_initial + Σentrées − Σsorties",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }
            }

            if (accounts.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Aucun compte. Cliquez sur '+' pour en créer un (ex: Caisse Principale, Airtel Money, Stock Airtel).",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                        )
                    }
                }
            } else {
                items(accounts, key = { it.account.id }) { acc ->
                    val isArgent = acc.account.type == "ARGENT"
                    val unitLabel = if (isArgent) currency else "Unités"

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
                                            .size(42.dp)
                                            .background(
                                                if (isArgent) Color(0xFFE0F2FE) else Color(0xFFFEF3C7),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isArgent) Icons.Default.AccountBalance else Icons.Default.SimCard,
                                            contentDescription = null,
                                            tint = if (isArgent) Color(0xFF0284C7) else Color(0xFFD97706)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = acc.account.nom,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isArgent) Color(0xFFBAE6FD) else Color(0xFFFDE68A),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "TYPE : ${acc.account.type}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isArgent) Color(0xFF0369A1) else Color(0xFFB45309)
                                            )
                                        }
                                    }
                                }

                                if (isAdmin) {
                                    IconButton(
                                        onClick = {
                                            editingAccount = acc.account
                                            nomInput = acc.account.nom
                                            typeInput = acc.account.type
                                            soldeInitialInput = acc.account.solde_initial.toString()
                                            showDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Color(0xFF64748B))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Solde Initial", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B)))
                                    Text(
                                        "${numberFormat.format(acc.account.solde_initial)} $unitLabel",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                Column {
                                    Text("Entrées (+)", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF16A34A)))
                                    Text(
                                        "+${numberFormat.format(acc.totalEntries)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF16A34A)
                                    )
                                }

                                Column {
                                    Text("Sorties (-)", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFDC2626)))
                                    Text(
                                        "-${numberFormat.format(acc.totalSorties)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Solde Recalculé", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF0077B6)))
                                    Text(
                                        "${numberFormat.format(acc.currentBalance)} $unitLabel",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (acc.currentBalance >= 0) Color(0xFF0F766E) else Color(0xFFBE123C)
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

    // Dialogue d'ajout / modification de compte
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(if (editingAccount == null) "Ajouter un Compte" else "Modifier le Compte")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nomInput,
                        onValueChange = { nomInput = it },
                        label = { Text("Nom du compte (ex: Airtel Money)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = typeInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type de compte") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("ARGENT (Espèces, Mobile Money)") },
                                onClick = {
                                    typeInput = "ARGENT"
                                    typeDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("UNITE (Stock Forfaits, Unités Flash)") },
                                onClick = {
                                    typeInput = "UNITE"
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = soldeInitialInput,
                        onValueChange = { soldeInitialInput = it },
                        label = { Text("Solde initial de départ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val soldeInit = soldeInitialInput.toDoubleOrNull() ?: 0.0
                        if (nomInput.isNotBlank()) {
                            val current = editingAccount
                            if (current == null) {
                                onAddAccount(nomInput, typeInput, soldeInit)
                            } else {
                                onUpdateAccount(
                                    current.copy(
                                        nom = nomInput.trim(),
                                        type = typeInput,
                                        solde_initial = soldeInit
                                    )
                                )
                            }
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
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
