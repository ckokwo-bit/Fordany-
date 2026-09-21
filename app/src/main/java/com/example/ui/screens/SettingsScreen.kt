/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.components.KgcFooter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentCompanyName: String,
    currentCurrency: String,
    currentSeuilCaisse: Double,
    currentObjectif: Double,
    currentUser: UserEntity?,
    users: List<UserEntity>,
    onSaveSettings: (nom: String, devise: String, seuil: Double, objectif: Double) -> Unit,
    onCreateUser: (nom: String, login: String, mdp: String, role: String) -> Unit,
    onToggleUserStatus: (UserEntity) -> Unit,
    onUpdatePassword: (oldPass: String, newPass: String) -> Unit,
    onExportEncryptedDb: (Context) -> Unit
) {
    val context = LocalContext.current
    val isAdmin = currentUser?.role == "Admin"

    var nomEntreprise by remember(currentCompanyName) { mutableStateOf(currentCompanyName) }
    var devise by remember(currentCurrency) { mutableStateOf(currentCurrency) }
    var seuilCaisseText by remember(currentSeuilCaisse) { mutableStateOf(currentSeuilCaisse.toString()) }
    var objectifText by remember(currentObjectif) { mutableStateOf(currentObjectif.toString()) }

    // Mot de passe
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var passwordChangeError by remember { mutableStateOf<String?>(null) }
    var showPasswordForm by remember { mutableStateOf(false) }

    // Nouvel utilisateur
    var showAddUserForm by remember { mutableStateOf(false) }
    var newUserName by remember { mutableStateOf("") }
    var newUserLogin by remember { mutableStateOf("") }
    var newUserPassword by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf("Opérateur") }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Paramètres & Sécurité",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                )
                Text(
                    text = "Configuration de l'entreprise, gestion des utilisateurs et sauvegarde",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                )
            }
        }

        // 1. Paramètres Généraux
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF00B4D8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configuration Entreprise", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }

                    OutlinedTextField(
                        value = nomEntreprise,
                        onValueChange = { nomEntreprise = it },
                        label = { Text("Nom de l'entreprise") },
                        enabled = isAdmin,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = devise,
                        onValueChange = { devise = it },
                        label = { Text("Devise (ex: FC, USD)") },
                        enabled = isAdmin,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = seuilCaisseText,
                            onValueChange = { seuilCaisseText = it },
                            label = { Text("Seuil alerte caisse") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = isAdmin,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = objectifText,
                            onValueChange = { objectifText = it },
                            label = { Text("Objectif jour bénéfice") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = isAdmin,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (isAdmin) {
                        Button(
                            onClick = {
                                val s = seuilCaisseText.toDoubleOrNull() ?: 50000.0
                                val obj = objectifText.toDoubleOrNull() ?: 100000.0
                                onSaveSettings(nomEntreprise, devise, s, obj)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ENREGISTRER LES PARAMÈTRES", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Text(
                            text = "Seul un Administrateur peut modifier ces paramètres.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                }
            }
        }

        // Modification du mot de passe de l'utilisateur connecté
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFE11D48))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Sécurité & Mot de passe", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(
                                    "Modifier votre mot de passe de connexion",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                showPasswordForm = !showPasswordForm
                                passwordChangeError = null
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (showPasswordForm) "Fermer" else "Modifier", fontSize = 12.sp)
                        }
                    }

                    AnimatedVisibility(visible = showPasswordForm) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            passwordChangeError?.let { err ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = err,
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9F1239))
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it },
                                label = { Text("Ancien mot de passe") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text("Nouveau mot de passe") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = confirmNewPassword,
                                onValueChange = { confirmNewPassword = it },
                                label = { Text("Confirmer le nouveau mot de passe") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (oldPassword.isBlank()) {
                                        passwordChangeError = "Veuillez entrer l'ancien mot de passe."
                                    } else if (newPassword.length < 4) {
                                        passwordChangeError = "Le nouveau mot de passe doit contenir au moins 4 caractères."
                                    } else if (newPassword != confirmNewPassword) {
                                        passwordChangeError = "Les nouveaux mots de passe ne correspondent pas."
                                    } else {
                                        passwordChangeError = null
                                        onUpdatePassword(oldPassword, newPassword)
                                        oldPassword = ""
                                        newPassword = ""
                                        confirmNewPassword = ""
                                        showPasswordForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("VALIDER LE NOUVEAU MOT DE PASSE", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // 2. Gestion des Utilisateurs (Admin Only)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = Color(0xFF06D6A0))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gestion des Utilisateurs", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }

                        if (isAdmin) {
                            OutlinedButton(
                                onClick = { showAddUserForm = !showAddUserForm },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (showAddUserForm) "Fermer" else "Ajouter", fontSize = 12.sp)
                            }
                        }
                    }

                    // Formulaire d'ajout d'utilisateur
                    AnimatedVisibility(visible = showAddUserForm && isAdmin) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Créer un nouvel utilisateur", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            OutlinedTextField(
                                value = newUserName,
                                onValueChange = { newUserName = it },
                                label = { Text("Nom complet") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newUserLogin,
                                onValueChange = { newUserLogin = it },
                                label = { Text("Identifiant (Login)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newUserPassword,
                                onValueChange = { newUserPassword = it },
                                label = { Text("Mot de passe") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            ExposedDropdownMenuBox(
                                expanded = roleDropdownExpanded,
                                onExpandedChange = { roleDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = newUserRole,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Rôle d'accès") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = roleDropdownExpanded,
                                    onDismissRequest = { roleDropdownExpanded = false }
                                ) {
                                    listOf("Admin", "Opérateur", "Lecteur").forEach { roleOption ->
                                        DropdownMenuItem(
                                            text = { Text(roleOption) },
                                            onClick = {
                                                newUserRole = roleOption
                                                roleDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (newUserName.isNotBlank() && newUserLogin.isNotBlank() && newUserPassword.isNotBlank()) {
                                        onCreateUser(newUserName, newUserLogin, newUserPassword, newUserRole)
                                        newUserName = ""
                                        newUserLogin = ""
                                        newUserPassword = ""
                                        showAddUserForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("VALIDER LA CRÉATION", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Liste des utilisateurs
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        users.forEach { u ->
                            val isSelf = u.id == currentUser?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = u.nom + if (isSelf) " (Vous)" else "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Login: ${u.login} • Rôle: ${u.role}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                    )
                                }

                                if (isAdmin && !isSelf) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (u.actif == 1) "Actif" else "Désactivé",
                                            fontSize = 11.sp,
                                            color = if (u.actif == 1) Color(0xFF16A34A) else Color(0xFFDC2626)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Switch(
                                            checked = u.actif == 1,
                                            onCheckedChange = { onToggleUserStatus(u) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Sauvegarde de la Base de Données Chiffrée (AES-256-GCM)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF7209B7))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sauvegarde Chiffrée AES-256-GCM", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    }

                    Text(
                        text = "Exportez l'intégralité de la base de données locale (comptes, produits, historique) sous format chiffré de niveau militaire pour mise en sécurité hors ligne.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                    )

                    Button(
                        onClick = { onExportEncryptedDb(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7209B7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_db_encrypted_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EXPORTER LA BASE CHIFFRÉE (.KGC)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // 4. Page À propos avec KGC Technologies
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A192F)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFF00B4D8), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "FORDANY MANAGEMENT",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "Version 1.0.0 Release Pro",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF90E0EF))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Conçu sur mesure pour Ets FORDANY (RDC) pour la gestion d'opérations télécoms (forfaits data, transferts de monnaie électronique, cartes SIM Airtel, Orange, Vodacom).",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1)),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF132F54), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DÉVELOPPÉ PAR KGC TECHNOLOGIES",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFF06D6A0),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ingénierie logicielle de pointe, solutions financières & haute sécurité",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8)),
                                textAlign = TextAlign.Center
                            )
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
