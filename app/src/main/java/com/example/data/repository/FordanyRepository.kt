/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.data.repository

import com.example.data.local.dao.AppDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.ActivationEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.EnterpriseEntity
import com.example.data.local.entity.OperationEntity
import com.example.data.local.entity.OperationTypeEntity
import com.example.data.local.entity.ParamEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

data class AccountWithBalance(
    val account: AccountEntity,
    val currentBalance: Double,
    val totalEntries: Double,
    val totalSorties: Double
)

data class DashboardSummary(
    val totalCaisse: Double,
    val accountsWithBalance: List<AccountWithBalance>,
    val beneficeJour: Double,
    val beneficeSemaine: Double,
    val beneficeMois: Double,
    val beneficeSemainePrecedente: Double,
    val progressionPercentSemaine: Double,
    val isProgressing: Boolean,
    val syntheseTexte: String,
    val alertCaisseBasse: Boolean,
    val alertStockEpuise: List<String>,
    val seuilCaisse: Double,
    val objectifJournalier: Double,
    val dernieresOperations: List<OperationEntity>
)

data class DayChartPoint(
    val dayLabel: String,
    val timestamp: Long = 0L,
    val benefice: Double = 0.0,
    val volumeVentes: Double = 0.0
)

data class TopProductStats(
    val productName: String,
    val totalQuantite: Double,
    val totalMontant: Double,
    val totalBenefice: Double
)

class FordanyRepository(val dao: AppDao) {

    // === ACTIVATION ===
    suspend fun getActivation(): ActivationEntity? = dao.getActivation()
    suspend fun insertActivation(activation: ActivationEntity) = dao.insertActivation(activation)

    // === ENTREPRISE ===
    val enterprise: Flow<EnterpriseEntity?> = dao.getEnterprise()
    suspend fun getEnterpriseDirect(): EnterpriseEntity? = dao.getEnterpriseDirect()
    suspend fun updateEnterprise(enterprise: EnterpriseEntity) = dao.updateEnterprise(enterprise)
    suspend fun insertEnterprise(enterprise: EnterpriseEntity) = dao.insertEnterprise(enterprise)

    // === TYPES D'OPERATIONS ===
    val allOperationTypes: Flow<List<OperationTypeEntity>> = dao.getAllOperationTypes()
    suspend fun getOperationTypesList(): List<OperationTypeEntity> = dao.getOperationTypesList()
    suspend fun insertOperationType(type: OperationTypeEntity): Long = dao.insertOperationType(type)
    suspend fun updateOperationType(type: OperationTypeEntity) = dao.updateOperationType(type)
    suspend fun deleteOperationType(type: OperationTypeEntity) = dao.deleteOperationType(type)

    // === UTILISATEURS ===
    val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    suspend fun getUserCount(): Int = dao.getUserCount()
    suspend fun getUserByLogin(login: String): UserEntity? = dao.getUserByLogin(login)
    suspend fun getUserById(id: Long): UserEntity? = dao.getUserById(id)
    suspend fun insertUser(user: UserEntity): Long = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    // === COMPTES ===
    val allAccounts: Flow<List<AccountEntity>> = dao.getAllAccounts()
    suspend fun insertAccount(account: AccountEntity): Long = dao.insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = dao.updateAccount(account)
    suspend fun getAccountById(id: Long): AccountEntity? = dao.getAccountById(id)

    // === PRODUITS ===
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    suspend fun insertProduct(product: ProductEntity): Long = dao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)
    suspend fun getProductById(id: Long): ProductEntity? = dao.getProductById(id)

    // === OPERATIONS ===
    val allOperations: Flow<List<OperationEntity>> = dao.getAllOperations()
    suspend fun insertOperation(op: OperationEntity): Long = dao.insertOperation(op)
    suspend fun updateOperation(op: OperationEntity) = dao.updateOperation(op)
    suspend fun getOperationById(id: Long): OperationEntity? = dao.getOperationById(id)

    // === PARAMETRES ===
    val allParams: Flow<List<ParamEntity>> = dao.getAllParams()
    suspend fun getParam(cle: String): String? = dao.getParam(cle)?.valeur
    suspend fun setParam(cle: String, valeur: String) = dao.setParam(ParamEntity(cle, valeur))

    // === LOGS AUDIT ===
    val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
    suspend fun logAction(userId: Long?, action: String, details: String, ip: String = "127.0.0.1") {
        dao.insertAuditLog(
            AuditLogEntity(
                utilisateur_id = userId,
                action = action,
                details = details,
                ip = ip,
                date = System.currentTimeMillis()
            )
        )
    }

    /**
     * RÈGLE D'OR : Calcul dynamique et rigoureux des soldes de tous les comptes.
     * Le solde n'est JAMAIS stocké en dur.
     * Solde = solde_initial + Σentrées − Σsorties
     */
    fun getAccountsWithBalanceFlow(): Flow<List<AccountWithBalance>> {
        return combine(dao.getAllAccounts(), dao.getAllOperations(), dao.getAllProducts()) { accounts, operations, products ->
            computeAccountsWithBalance(accounts, operations, products)
        }
    }

    fun computeAccountsWithBalance(
        accounts: List<AccountEntity>,
        operations: List<OperationEntity>,
        products: List<ProductEntity>
    ): List<AccountWithBalance> {
        val productMap = products.associateBy { it.id }
        val activeOps = operations.filter { it.annulee == 0 }

        return accounts.map { account ->
            var entries = 0.0
            var sorties = 0.0

            for (op in activeOps) {
                val prod = op.produit_id?.let { productMap[it] }

                // 1. Mouvements directs sur le compte principal de l'opération
                if (op.compte_id == account.id) {
                    when (op.type) {
                        "Vente" -> {
                            if (account.type == "ARGENT") {
                                entries += op.montant
                            } else {
                                sorties += op.quantite
                            }
                        }
                        "Achat" -> {
                            if (account.type == "ARGENT") {
                                sorties += op.montant
                            } else {
                                entries += op.quantite
                            }
                        }
                        "Dépense", "Don" -> {
                            if (account.type == "ARGENT") {
                                sorties += op.montant
                            } else {
                                sorties += op.quantite
                            }
                        }
                    }
                }

                // 2. Mouvements secondaires liés aux produits (stocks unités)
                if (prod != null) {
                    // Pour un Achat de produit : le stock arrive dans compte_dest_id
                    if (op.type == "Achat" && prod.compte_dest_id == account.id && op.compte_id != account.id) {
                        entries += op.quantite
                    }
                    // Pour une Vente de produit : le stock sort de compte_source_id
                    if (op.type == "Vente" && prod.compte_source_id == account.id && op.compte_id != account.id) {
                        sorties += op.quantite
                    }
                }
            }

            val currentBalance = account.solde_initial + entries - sorties
            AccountWithBalance(
                account = account,
                currentBalance = currentBalance,
                totalEntries = entries,
                totalSorties = sorties
            )
        }
    }

    /**
     * Calcule le bénéfice net sur un intervalle de temps donné [startMs, endMs].
     * Bénéfice = Σ(marge sur ventes) - Σ(dépenses) - Σ(dons)
     */
    fun computeBeneficePeriod(
        operations: List<OperationEntity>,
        products: List<ProductEntity>,
        startMs: Long,
        endMs: Long
    ): Double {
        val productMap = products.associateBy { it.id }
        val opsInPeriod = operations.filter { it.annulee == 0 && it.date in startMs..endMs }

        var totalMargeVentes = 0.0
        var totalDepenses = 0.0
        var totalDons = 0.0

        for (op in opsInPeriod) {
            when (op.type) {
                "Vente" -> {
                    val prod = op.produit_id?.let { productMap[it] }
                    val coutAchat = if (prod != null) prod.prix_achat * op.quantite else 0.0
                    val marge = op.montant - coutAchat
                    totalMargeVentes += marge
                }
                "Dépense" -> totalDepenses += op.montant
                "Don" -> totalDons += op.montant
                "Achat" -> {
                    // L'achat de stock est une immobilisation, son coût est déduit lors de la vente.
                }
            }
        }

        return totalMargeVentes - totalDepenses - totalDons
    }

    /**
     * Génère la liste des 7 derniers jours pour les graphiques du tableau de bord
     */
    fun computeLast7DaysChart(
        operations: List<OperationEntity>,
        products: List<ProductEntity>,
        currency: String
    ): List<DayChartPoint> {
        val result = mutableListOf<DayChartPoint>()
        val cal = Calendar.getInstance()
        val dayFormat = java.text.SimpleDateFormat("EEE dd", java.util.Locale.FRENCH)

        for (i in 6 downTo 0) {
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            val benefice = computeBeneficePeriod(operations, products, startOfDay, endOfDay)
            val volumeVentes = operations.filter { it.annulee == 0 && it.type == "Vente" && it.date in startOfDay..endOfDay }
                .sumOf { it.montant }

            result.add(
                DayChartPoint(
                    dayLabel = dayFormat.format(java.util.Date(startOfDay)).replaceFirstChar { it.uppercase() },
                    timestamp = startOfDay,
                    benefice = benefice,
                    volumeVentes = volumeVentes
                )
            )
        }
        return result
    }

    /**
     * Calcule le Top des produits vendus
     */
    fun computeTopProducts(
        operations: List<OperationEntity>,
        products: List<ProductEntity>
    ): List<TopProductStats> {
        val productMap = products.associateBy { it.id }
        val activeSales = operations.filter { it.annulee == 0 && it.type == "Vente" }

        val statsByProduct = mutableMapOf<String, Triple<Double, Double, Double>>() // Quantite, Montant, Benefice

        for (op in activeSales) {
            val prod = op.produit_id?.let { productMap[it] }
            val name = prod?.nom ?: "Vente diverse"
            val coutAchat = if (prod != null) prod.prix_achat * op.quantite else 0.0
            val benefice = op.montant - coutAchat

            val current = statsByProduct.getOrDefault(name, Triple(0.0, 0.0, 0.0))
            statsByProduct[name] = Triple(
                current.first + op.quantite,
                current.second + op.montant,
                current.third + benefice
            )
        }

        return statsByProduct.map { (name, stats) ->
            TopProductStats(
                productName = name,
                totalQuantite = stats.first,
                totalMontant = stats.second,
                totalBenefice = stats.third
            )
        }.sortedByDescending { it.totalBenefice }
    }

    /**
     * Initialise les données système de base :
     * - Compte administrateur initial
     * - Entité entreprise par défaut
     * - Modèles de types d'opérations suggérés (libres, modifiables et supprimables par le client)
     * Aucune donnée métier ou compte n'est codé en dur : le client configure tout librement.
     */
    suspend fun seedInitialDataIfNeeded() {
        val existingAdmin = dao.getUserByLogin("admin")
        if (existingAdmin == null) {
            val adminPasswordHash = com.example.security.AuthManager.hashPassword("fordany.2026")
            val adminId = dao.insertUser(
                UserEntity(
                    nom = "Administrateur",
                    login = "admin",
                    mdp_hash = adminPasswordHash,
                    role = "Admin",
                    actif = 1
                )
            )
            logAction(adminId, "CREATION_SYSTEME", "Compte administrateur initial configuré (mdp: fordany.2026)")
        } else if (com.example.security.AuthManager.verifyPassword("admin123", existingAdmin.mdp_hash)) {
            val newHash = com.example.security.AuthManager.hashPassword("fordany.2026")
            dao.updateUser(existingAdmin.copy(mdp_hash = newHash))
        }

        // Configuration Entreprise par défaut
        if (dao.getEnterpriseDirect() == null) {
            dao.insertEnterprise(
                EnterpriseEntity(
                    id = 1,
                    nom = "Ets FORDANY",
                    devise = "USD",
                    couleur_accent = "#0284C7",
                    seuil_caisse_basse = 100.0,
                    seuil_stock_bas = 50.0
                )
            )
        }

        // Modèles suggérés non-imposés de types d'opérations (modifiables/supprimables)
        if (dao.getOperationTypesCount() == 0) {
            dao.insertOperationType(OperationTypeEntity(code = "VENTE", nom = "Vente", sens = "ENTREE", ordre = 1))
            dao.insertOperationType(OperationTypeEntity(code = "DEPENSE", nom = "Dépense", sens = "SORTIE", ordre = 2))
            dao.insertOperationType(OperationTypeEntity(code = "REVENU", nom = "Revenu", sens = "ENTREE", ordre = 3))
            dao.insertOperationType(OperationTypeEntity(code = "DON", nom = "Don", sens = "ENTREE", ordre = 4))
            dao.insertOperationType(OperationTypeEntity(code = "ACHAT", nom = "Achat", sens = "SORTIE", ordre = 5))
            dao.insertOperationType(OperationTypeEntity(code = "TRANSFERT", nom = "Transfert interne", sens = "NEUTRE", ordre = 6))
        }

        if (dao.getParam("company_name") == null) {
            dao.setParam(ParamEntity("company_name", "Ets FORDANY"))
        }
        if (dao.getParam("currency") == null) {
            dao.setParam(ParamEntity("currency", "USD"))
        }
        if (dao.getParam("seuil_caisse") == null) {
            dao.setParam(ParamEntity("seuil_caisse", "100"))
        }
        if (dao.getParam("objectif_journalier") == null) {
            dao.setParam(ParamEntity("objectif_journalier", "500"))
        }
    }
}
