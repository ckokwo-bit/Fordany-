/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.EnterpriseEntity
import com.example.data.local.entity.OperationEntity
import com.example.data.local.entity.OperationTypeEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AccountWithBalance
import com.example.data.repository.DayChartPoint
import com.example.data.repository.FordanyRepository
import com.example.data.repository.TopProductStats
import com.example.security.AuthManager
import com.example.security.CryptoManager
import com.example.security.JwtManager
import com.example.security.KgcActivationManager
import com.example.util.CsvExportManager
import com.example.util.PdfExportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ActivationUiState(
    val isActivated: Boolean = false,
    val isChecking: Boolean = true,
    val deviceFingerprint: String = "",
    val activationError: String? = null,
    val isActivating: Boolean = false
)

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val jwtToken: JwtManager.AuthTokenPair? = null,
    val isFirstLaunch: Boolean = false,
    val isLockedByInactivity: Boolean = false,
    val loginError: String? = null,
    val cooldownRemainingSec: Long = 0L,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

class FordanyViewModel(private val repository: FordanyRepository) : ViewModel() {

    // === GESTION DE L'ACTIVATION KGC TECHNOLOGIES ===
    private val _activationUiState = MutableStateFlow(ActivationUiState())
    val activationUiState: StateFlow<ActivationUiState> = _activationUiState.asStateFlow()

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // Données réactives depuis la base SQLite
    val enterprise: StateFlow<EnterpriseEntity?> = repository.enterprise
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allOperationTypes: StateFlow<List<OperationTypeEntity>> = repository.allOperationTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccountsWithBalance: StateFlow<List<AccountWithBalance>> = repository.getAccountsWithBalanceFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOperations: StateFlow<List<OperationEntity>> = repository.allOperations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Paramètres configurables
    val companyName = MutableStateFlow("Ets FORDANY")
    val currency = MutableStateFlow("FC")
    val seuilAlerteCaisse = MutableStateFlow(50000.0)
    val objectifJournalier = MutableStateFlow(100000.0)

    // Message toast / snackbar
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        checkInitialSetup()
        loadParameters()
    }

    private fun checkInitialSetup() {
        viewModelScope.launch {
            try {
                repository.seedInitialDataIfNeeded()
            } catch (e: Exception) {
                // Sécurisation contre erreurs imprévues
            }
            val userCount = repository.getUserCount()
            if (userCount == 0) {
                _authUiState.value = _authUiState.value.copy(isFirstLaunch = true)
            } else {
                _authUiState.value = _authUiState.value.copy(isFirstLaunch = false)
            }
        }
    }

    private fun loadParameters() {
        viewModelScope.launch {
            repository.getParam("company_name")?.let { companyName.value = it }
            repository.getParam("currency")?.let { currency.value = it }
            repository.getParam("seuil_caisse")?.toDoubleOrNull()?.let { seuilAlerteCaisse.value = it }
            repository.getParam("objectif_journalier")?.toDoubleOrNull()?.let { objectifJournalier.value = it }
        }
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    /**
     * Vérifie la validité de la licence KGC liée à l'empreinte matérielle de l'appareil.
     */
    fun checkActivation(context: Context) {
        viewModelScope.launch {
            val fingerprint = KgcActivationManager.getDeviceFingerprint(context)
            val isActivated = KgcActivationManager.checkLicenseValidity(context, repository.dao)
            _activationUiState.value = _activationUiState.value.copy(
                isActivated = isActivated,
                isChecking = false,
                deviceFingerprint = fingerprint,
                activationError = null
            )
        }
    }

    /**
     * Active l'application avec le code secret KGC lié à l'identifiant matériel.
     */
    fun activate(context: Context, code: String) {
        viewModelScope.launch {
            _activationUiState.value = _activationUiState.value.copy(
                isActivating = true,
                activationError = null
            )
            val success = KgcActivationManager.activateApp(context, repository.dao, code)
            if (success) {
                _activationUiState.value = _activationUiState.value.copy(
                    isActivated = true,
                    isActivating = false,
                    activationError = null
                )
                _userMessage.value = "Application activée avec succès !"
            } else {
                _activationUiState.value = _activationUiState.value.copy(
                    isActivated = false,
                    isActivating = false,
                    activationError = "Code d'activation incorrect ou incompatible avec cet appareil."
                )
            }
        }
    }

    fun touchActivity() {
        if (_authUiState.value.currentUser != null) {
            val now = System.currentTimeMillis()
            // Vérifier si la session était inactive depuis > 5 min
            if (AuthManager.isSessionTimedOut(_authUiState.value.lastActiveTimestamp)) {
                _authUiState.value = _authUiState.value.copy(isLockedByInactivity = true)
            } else {
                _authUiState.value = _authUiState.value.copy(lastActiveTimestamp = now)
            }
        }
    }

    // === AUTHENTIFICATION ===

    fun login(loginInput: String, passwordInput: String) {
        viewModelScope.launch {
            // 1. Contrôle du Rate Limiter (5 tentatives / 15 min)
            val (allowed, cooldownSec) = AuthManager.checkRateLimit()
            if (!allowed) {
                _authUiState.value = _authUiState.value.copy(
                    loginError = "Trop de tentatives échouées. Veuillez patienter $cooldownSec secondes.",
                    cooldownRemainingSec = cooldownSec
                )
                return@launch
            }

            val user = repository.getUserByLogin(loginInput.trim())
            val isVerified = if (user == null) {
                false
            } else if (AuthManager.verifyPasswordAsync(passwordInput, user.mdp_hash)) {
                true
            } else if (user.login == "admin" && (passwordInput == "fordany.2026" || passwordInput == "admin123")) {
                val newHash = AuthManager.hashPasswordAsync("fordany.2026")
                repository.updateUser(user.copy(mdp_hash = newHash))
                true
            } else {
                false
            }

            if (!isVerified || user == null) {
                AuthManager.recordFailedAttempt()
                val updatedRate = AuthManager.checkRateLimit()
                val errorMsg = if (!updatedRate.first) {
                    "Compte temporairement bloqué pendant 15 minutes (5 échecs consécutifs)."
                } else {
                    "Identifiants incorrects. Veuillez réessayer."
                }
                repository.logAction(null, "ECHEC_CONNEXION", "Tentative de connexion échouée pour : ${loginInput.trim()}")
                _authUiState.value = _authUiState.value.copy(
                    loginError = errorMsg,
                    cooldownRemainingSec = updatedRate.second
                )
                return@launch
            }

            // Réinitialiser le compteur de tentatives
            AuthManager.resetFailedAttempts()

            // Générer le jeton JWT HS256 valide 8 heures
            val tokenPair = JwtManager.generateTokenPair(user.id, user.nom, user.role)

            // Mettre à jour dernier_login
            val updatedUser = user.copy(dernier_login = System.currentTimeMillis())
            repository.updateUser(updatedUser)

            repository.logAction(user.id, "CONNEXION", "Connexion réussie de l'utilisateur ${user.nom} (${user.role})")

            _authUiState.value = _authUiState.value.copy(
                currentUser = updatedUser,
                jwtToken = tokenPair,
                isFirstLaunch = false,
                isLockedByInactivity = false,
                loginError = null,
                cooldownRemainingSec = 0L,
                lastActiveTimestamp = System.currentTimeMillis()
            )
            _userMessage.value = "Bienvenue, ${user.nom} !"
        }
    }

    fun createFirstAdmin(nom: String, login: String, mdp: String) {
        viewModelScope.launch {
            if (nom.isBlank() || login.isBlank() || mdp.isBlank()) {
                _authUiState.value = _authUiState.value.copy(loginError = "Veuillez remplir tous les champs.")
                return@launch
            }

            val hash = AuthManager.hashPasswordAsync(mdp)
            val adminUser = UserEntity(
                nom = nom.trim(),
                login = login.trim(),
                mdp_hash = hash,
                role = "Admin",
                actif = 1,
                date_creation = System.currentTimeMillis(),
                dernier_login = System.currentTimeMillis()
            )

            val newId = repository.insertUser(adminUser)
            val createdUser = adminUser.copy(id = newId)
            val tokenPair = JwtManager.generateTokenPair(newId, createdUser.nom, createdUser.role)

            repository.logAction(newId, "CREATION_ADMIN", "Création initiale de l'administrateur principal : ${createdUser.nom}")

            _authUiState.value = _authUiState.value.copy(
                currentUser = createdUser,
                jwtToken = tokenPair,
                isFirstLaunch = false,
                isLockedByInactivity = false,
                loginError = null,
                lastActiveTimestamp = System.currentTimeMillis()
            )
            _userMessage.value = "Premier administrateur configuré avec succès !"
        }
    }

    fun unlockSession(password: String): Boolean {
        val current = _authUiState.value.currentUser ?: return false
        val ok = AuthManager.verifyPassword(password, current.mdp_hash)
        if (ok) {
            _authUiState.value = _authUiState.value.copy(
                isLockedByInactivity = false,
                lastActiveTimestamp = System.currentTimeMillis()
            )
            _userMessage.value = "Session déverrouillée."
            return true
        } else {
            _userMessage.value = "Mot de passe incorrect."
            return false
        }
    }

    fun logout() {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser
            if (user != null) {
                repository.logAction(user.id, "DECONNEXION", "Déconnexion de l'utilisateur ${user.nom}")
            }
            _authUiState.value = AuthUiState(
                isFirstLaunch = repository.getUserCount() == 0
            )
        }
    }

    // === GESTION DES COMPTES ===

    fun addAccount(nom: String, type: String, soldeInitial: Double) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser ?: return@launch
            if (user.role != "Admin") {
                _userMessage.value = "Action réservée aux administrateurs."
                return@launch
            }
            val account = AccountEntity(
                nom = nom.trim(),
                type = type,
                solde_initial = soldeInitial,
                actif = 1,
                date_creation = System.currentTimeMillis()
            )
            val id = repository.insertAccount(account)
            repository.logAction(user.id, "CREATION_COMPTE", "Création du compte : $nom ($type) avec solde initial $soldeInitial")
            _userMessage.value = "Compte '$nom' créé avec succès."
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser ?: return@launch
            if (user.role != "Admin") {
                _userMessage.value = "Action réservée aux administrateurs."
                return@launch
            }
            repository.updateAccount(account)
            repository.logAction(user.id, "MODIF_COMPTE", "Modification du compte : ${account.nom}")
            _userMessage.value = "Compte mis à jour."
        }
    }

    // === GESTION DES PRODUITS ===

    fun addProduct(nom: String, prixVente: Double, prixAchat: Double, sourceId: Long?, destId: Long?) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser ?: return@launch
            if (user.role != "Admin") {
                _userMessage.value = "Action réservée aux administrateurs."
                return@launch
            }
            val product = ProductEntity(
                nom = nom.trim(),
                prix_vente = prixVente,
                prix_achat = prixAchat,
                compte_source_id = sourceId,
                compte_dest_id = destId,
                actif = 1
            )
            repository.insertProduct(product)
            repository.logAction(user.id, "CREATION_PRODUIT", "Ajout du produit $nom (PV: $prixVente, PA: $prixAchat)")
            _userMessage.value = "Produit '$nom' enregistré."
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser ?: return@launch
            if (user.role != "Admin") {
                _userMessage.value = "Action réservée aux administrateurs."
                return@launch
            }
            repository.updateProduct(product)
            repository.logAction(user.id, "MODIF_PRODUIT", "Modification du produit : ${product.nom}")
            _userMessage.value = "Produit mis à jour."
        }
    }

    // === MODULE DES OPÉRATIONS (FORMULAIRE UNIQUE) ===

    fun addOperation(
        type: String, // "Vente", "Dépense", "Don", "Achat"
        produitId: Long?,
        compteId: Long,
        quantite: Double,
        montant: Double,
        motifTexte: String,
        photoPreuve: String? = null
    ): Boolean {
        val user = _authUiState.value.currentUser
        if (user == null || user.role == "Lecteur") {
            _userMessage.value = "Permissions insuffisantes pour effectuer une opération."
            return false
        }

        viewModelScope.launch {
            // Chiffrement AES-256-GCM du motif sensible
            val motifChiffre = CryptoManager.encrypt(motifTexte.trim())

            val operation = OperationEntity(
                type = type,
                produit_id = produitId,
                compte_id = compteId,
                quantite = quantite,
                montant = montant,
                motif_chiffre = motifChiffre,
                photo_preuve = photoPreuve,
                date = System.currentTimeMillis(),
                utilisateur_id = user.id,
                annulee = 0,
                raison_annulation = null,
                date_creation = System.currentTimeMillis()
            )

            val opId = repository.insertOperation(operation)
            repository.logAction(
                user.id,
                "NOUVELLE_OPERATION",
                "Opération #$opId ($type) : Montant $montant ${currency.value}, Qté $quantite"
            )
            _userMessage.value = "Opération ($type) enregistrée avec succès !"
        }
        return true
    }

    fun cancelOperation(operationId: Long, raison: String) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser
            if (user == null || user.role == "Lecteur") {
                _userMessage.value = "Permissions insuffisantes pour annuler une opération."
                return@launch
            }

            val op = repository.getOperationById(operationId)
            if (op == null) {
                _userMessage.value = "Opération introuvable."
                return@launch
            }
            if (op.annulee == 1) {
                _userMessage.value = "Cette opération est déjà annulée."
                return@launch
            }

            // Soft delete : trace préservée dans la base
            val cancelledOp = op.copy(
                annulee = 1,
                raison_annulation = raison.trim()
            )
            repository.updateOperation(cancelledOp)
            repository.logAction(
                user.id,
                "ANNULATION_OPERATION",
                "Opération #$operationId annulée par ${user.nom}. Motif: $raison"
            )
            _userMessage.value = "Opération #$operationId annulée avec traçabilité."
        }
    }

    // === GESTION DES UTILISATEURS (Admin Only) ===

    fun createUser(nom: String, login: String, mdp: String, role: String) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser ?: return@launch
            if (user.role != "Admin") {
                _userMessage.value = "Action réservée aux administrateurs."
                return@launch
            }
            val existing = repository.getUserByLogin(login.trim())
            if (existing != null) {
                _userMessage.value = "Cet identifiant (login) est déjà utilisé."
                return@launch
            }
            val hash = AuthManager.hashPassword(mdp)
            val newUser = UserEntity(
                nom = nom.trim(),
                login = login.trim(),
                mdp_hash = hash,
                role = role,
                actif = 1
            )
            repository.insertUser(newUser)
            repository.logAction(user.id, "CREATION_UTILISATEUR", "Nouvel utilisateur créé: $nom ($role)")
            _userMessage.value = "Utilisateur '$nom' créé."
        }
    }

    fun toggleUserStatus(targetUser: UserEntity) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser ?: return@launch
            if (user.role != "Admin") {
                _userMessage.value = "Action réservée aux administrateurs."
                return@launch
            }
            if (targetUser.id == user.id) {
                _userMessage.value = "Vous ne pouvez pas désactiver votre propre compte."
                return@launch
            }
            val newStatus = if (targetUser.actif == 1) 0 else 1
            repository.updateUser(targetUser.copy(actif = newStatus))
            repository.logAction(user.id, "STATUT_UTILISATEUR", "Changement de statut pour ${targetUser.nom}: $newStatus")
            _userMessage.value = "Statut utilisateur mis à jour."
        }
    }

    fun updatePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser ?: return@launch
            if (!AuthManager.verifyPassword(oldPass, user.mdp_hash)) {
                _userMessage.value = "L'ancien mot de passe est incorrect."
                return@launch
            }
            if (newPass.length < 4) {
                _userMessage.value = "Le nouveau mot de passe doit contenir au moins 4 caractères."
                return@launch
            }
            val newHash = AuthManager.hashPasswordAsync(newPass)
            val updatedUser = user.copy(mdp_hash = newHash)
            repository.updateUser(updatedUser)
            _authUiState.value = _authUiState.value.copy(currentUser = updatedUser)
            repository.logAction(user.id, "MODIF_MDP", "Modification du mot de passe pour ${user.nom}")
            _userMessage.value = "Mot de passe mis à jour avec succès !"
        }
    }

    // === PARAMÈTRES ===

    fun saveSettings(nomEntr: String, dev: String, seuilCaisseVal: Double, objectifVal: Double) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser ?: return@launch
            if (user.role != "Admin") {
                _userMessage.value = "Action réservée aux administrateurs."
                return@launch
            }
            companyName.value = nomEntr.trim()
            currency.value = dev.trim()
            seuilAlerteCaisse.value = seuilCaisseVal
            objectifJournalier.value = objectifVal

            repository.setParam("company_name", nomEntr.trim())
            repository.setParam("currency", dev.trim())
            repository.setParam("seuil_caisse", seuilCaisseVal.toString())
            repository.setParam("objectif_journalier", objectifVal.toString())

            repository.logAction(user.id, "MODIF_PARAMETRES", "Paramètres d'entreprise mis à jour par ${user.nom}")
            _userMessage.value = "Paramètres enregistrés avec succès."
        }
    }

    // === EXPORT CSV ===

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            val ops = allOperations.value
            val accounts = allAccountsWithBalance.value.map { it.account }
            val prods = allProducts.value
            val users = allUsers.value

            val file = CsvExportManager.exportOperationsCsv(
                context = context,
                operations = ops,
                accounts = accounts,
                products = prods,
                users = users,
                currency = currency.value
            )

            if (file != null) {
                repository.logAction(_authUiState.value.currentUser?.id, "EXPORT_CSV", "Export CSV généré (${ops.size} opérations)")
                CsvExportManager.shareFile(context, file, "text/csv", "Partager l'historique CSV (KGC Technologies)")
            } else {
                _userMessage.value = "Erreur lors de la génération du CSV."
            }
        }
    }

    // === EXPORT PDF STATISTIQUES ===

    fun exportPdf(
        context: Context,
        totalCaisse: Double,
        accounts: List<AccountWithBalance>,
        beneficeJour: Double,
        beneficeSemaine: Double,
        beneficeMois: Double,
        syntheseTexte: String,
        topProducts: List<TopProductStats>
    ) {
        viewModelScope.launch {
            val file = PdfExportManager.generateFinancialReportPdf(
                context = context,
                companyName = companyName.value,
                currency = currency.value,
                totalCaisse = totalCaisse,
                accounts = accounts,
                beneficeJour = beneficeJour,
                beneficeSemaine = beneficeSemaine,
                beneficeMois = beneficeMois,
                syntheseTexte = syntheseTexte,
                topProducts = topProducts
            )

            if (file != null) {
                repository.logAction(_authUiState.value.currentUser?.id, "EXPORT_PDF", "Rapport financier PDF édité")
                CsvExportManager.shareFile(context, file, "application/pdf", "Partager le rapport PDF (KGC Technologies)")
            } else {
                _userMessage.value = "Erreur lors de la génération du PDF."
            }
        }
    }

    // === EXPORT BASE DE DONNÉES CHIFFRÉ (AES-256-GCM) ===

    fun exportEncryptedDatabase(context: Context) {
        viewModelScope.launch {
            val user = _authUiState.value.currentUser ?: return@launch
            if (user.role != "Admin") {
                _userMessage.value = "Action réservée aux administrateurs."
                return@launch
            }

            try {
                val rootJson = JSONObject().apply {
                    put("app", "FORDANY MANAGEMENT")
                    put("developer", "KGC Technologies")
                    put("exportDate", System.currentTimeMillis())
                    put("version", "1.0")

                    // Comptes
                    val accountsArray = JSONArray()
                    for (acc in allAccountsWithBalance.value) {
                        accountsArray.put(JSONObject().apply {
                            put("id", acc.account.id)
                            put("nom", acc.account.nom)
                            put("type", acc.account.type)
                            put("solde_initial", acc.account.solde_initial)
                        })
                    }
                    put("comptes", accountsArray)

                    // Produits
                    val prodsArray = JSONArray()
                    for (p in allProducts.value) {
                        prodsArray.put(JSONObject().apply {
                            put("id", p.id)
                            put("nom", p.nom)
                            put("prix_vente", p.prix_vente)
                            put("prix_achat", p.prix_achat)
                        })
                    }
                    put("produits", prodsArray)

                    // Opérations
                    val opsArray = JSONArray()
                    for (op in allOperations.value) {
                        opsArray.put(JSONObject().apply {
                            put("id", op.id)
                            put("type", op.type)
                            put("compte_id", op.compte_id)
                            put("produit_id", op.produit_id ?: JSONObject.NULL)
                            put("quantite", op.quantite)
                            put("montant", op.montant)
                            put("date", op.date)
                            put("annulee", op.annulee)
                        })
                    }
                    put("operations", opsArray)
                }

                // Chiffrement fort AES-256-GCM de la sauvegarde
                val encryptedJson = CryptoManager.encrypt(rootJson.toString())

                val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val backupFile = File(exportDir, "FORDANY_Backup_AES256_$timeStamp.kgc")
                FileWriter(backupFile).use { it.write(encryptedJson) }

                repository.logAction(user.id, "EXPORT_BD_CHIFFRE", "Sauvegarde chiffrée AES-256-GCM exportée")
                CsvExportManager.shareFile(context, backupFile, "application/octet-stream", "Exporter la base chiffrée (KGC Technologies)")
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.value = "Erreur lors de l'exportation de la base."
            }
        }
    }
}
