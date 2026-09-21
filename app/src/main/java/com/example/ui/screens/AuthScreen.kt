/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AuthUiState
import com.example.ui.components.KgcFooter

/**
 * Écran d'authentification complète :
 * 1. Premier lancement : Configuration du premier Administrateur
 * 2. Connexion sécurisée standard (BCrypt, Rate Limiting, JWT)
 * 3. Verrouillage automatique suite à inactivité (> 5 min)
 */
@Composable
fun AuthScreen(
    authState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onCreateFirstAdmin: (String, String, String) -> Unit,
    onUnlock: (String) -> Boolean,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A192F), Color(0xFF0D223F), Color(0xFF132F54))
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo officiel de l'entreprise ETS FORDANY
            Image(
                painter = painterResource(id = R.drawable.img_fordany_logo),
                contentDescription = "Logo ETS FORDANY",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ETS FORDANY",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
            )

            Text(
                text = "by kgc technologies",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF90E0EF),
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sélection de la vue selon l'état
            when {
                authState.isLockedByInactivity -> {
                    InactivityLockCard(
                        userName = authState.currentUser?.nom ?: "Utilisateur",
                        onUnlock = onUnlock,
                        onLogout = onLogout
                    )
                }
                authState.isFirstLaunch -> {
                    FirstAdminCreationCard(
                        errorMessage = authState.loginError,
                        onCreateAdmin = onCreateFirstAdmin
                    )
                }
                else -> {
                    LoginCard(
                        errorMessage = authState.loginError,
                        cooldownRemainingSec = authState.cooldownRemainingSec,
                        onLogin = onLogin
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mention discrète obligatoire KGC Technologies
            KgcFooter()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Carte de connexion standard
 */
@Composable
fun LoginCard(
    errorMessage: String?,
    cooldownRemainingSec: Long,
    onLogin: (String, String) -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Authentification Sécurisée",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )
            Text(
                text = "Connectez-vous pour accéder à la gestion",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
            )

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF9F1239),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Identifiant / Login") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_input"),
                enabled = cooldownRemainingSec == 0L
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Afficher/Masquer le mot de passe"
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input"),
                enabled = cooldownRemainingSec == 0L
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onLogin(login, password) },
                enabled = login.isNotBlank() && password.isNotBlank() && cooldownRemainingSec == 0L,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
            ) {
                Text(
                    text = if (cooldownRemainingSec > 0L) "Bloqué ($cooldownRemainingSec s)" else "SE CONNECTER",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sécurité renforcée • Chiffrement AES-256-GCM",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Carte de création du premier administrateur (au tout premier lancement)
 */
@Composable
fun FirstAdminCreationCard(
    errorMessage: String?,
    onCreateAdmin: (String, String, String) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configuration Initiale",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )
            Text(
                text = "Créez le compte Administrateur Principal",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
            )

            Spacer(modifier = Modifier.height(16.dp))

            val displayedError = errorMessage ?: localError
            AnimatedVisibility(visible = displayedError != null) {
                displayedError?.let {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9F1239))
                        )
                    }
                }
            }

            OutlinedTextField(
                value = nom,
                onValueChange = { nom = it },
                label = { Text("Nom complet du Responsable") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Identifiant (Login de connexion)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_login_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_password_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmer le mot de passe") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_password_confirm_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        localError = "Les mots de passe ne correspondent pas."
                    } else if (password.length < 4) {
                        localError = "Le mot de passe doit contenir au moins 4 caractères."
                    } else {
                        localError = null
                        onCreateAdmin(nom, login, password)
                    }
                },
                enabled = nom.isNotBlank() && login.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("admin_create_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06D6A0))
            ) {
                Text(
                    text = "CRÉER L'ADMINISTRATEUR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

/**
 * Carte de verrouillage pour cause d'inactivité (5 minutes)
 */
@Composable
fun InactivityLockCard(
    userName: String,
    onUnlock: (String) -> Boolean,
    onLogout: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var unlockError by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFFFFB703),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Session Verrouillée",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )
            Text(
                text = "Verrouillage automatique après 5 minutes d'inactivité pour sécuriser votre caisse.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Utilisateur : $userName",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0077B6)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = unlockError) {
                Text(
                    text = "Mot de passe incorrect.",
                    color = Color(0xFFEF476F),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    unlockError = false
                },
                label = { Text("Mot de passe pour déverrouiller") },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("unlock_password_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val success = onUnlock(password)
                    if (!success) unlockError = true
                },
                enabled = password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("unlock_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B4D8))
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DÉVERROUILLER", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Se déconnecter", color = Color(0xFF64748B))
            }
        }
    }
}
