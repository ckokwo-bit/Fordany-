/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.ActivationEntity
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.EnterpriseEntity
import com.example.data.local.entity.OperationEntity
import com.example.data.local.entity.OperationTypeEntity
import com.example.data.local.entity.ParamEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // === ACTIVATION KGC ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivation(activation: ActivationEntity)

    @Query("SELECT * FROM activation WHERE id = 1 LIMIT 1")
    suspend fun getActivation(): ActivationEntity?

    @Query("DELETE FROM activation")
    suspend fun deleteActivation()

    // === ENTREPRISE ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnterprise(enterprise: EnterpriseEntity)

    @Update
    suspend fun updateEnterprise(enterprise: EnterpriseEntity)

    @Query("SELECT * FROM entreprise WHERE id = 1 LIMIT 1")
    fun getEnterprise(): Flow<EnterpriseEntity?>

    @Query("SELECT * FROM entreprise WHERE id = 1 LIMIT 1")
    suspend fun getEnterpriseDirect(): EnterpriseEntity?

    // === TYPES D'OPERATIONS ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperationType(type: OperationTypeEntity): Long

    @Update
    suspend fun updateOperationType(type: OperationTypeEntity)

    @Delete
    suspend fun deleteOperationType(type: OperationTypeEntity)

    @Query("SELECT * FROM types_operation WHERE actif = 1 ORDER BY ordre ASC, nom ASC")
    fun getAllOperationTypes(): Flow<List<OperationTypeEntity>>

    @Query("SELECT * FROM types_operation WHERE actif = 1 ORDER BY ordre ASC, nom ASC")
    suspend fun getOperationTypesList(): List<OperationTypeEntity>

    @Query("SELECT COUNT(*) FROM types_operation")
    suspend fun getOperationTypesCount(): Int

    // === UTILISATEURS ===
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM utilisateurs WHERE login = :login AND actif = 1 LIMIT 1")
    suspend fun getUserByLogin(login: String): UserEntity?

    @Query("SELECT * FROM utilisateurs WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM utilisateurs ORDER BY date_creation DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM utilisateurs")
    suspend fun getUserCount(): Int

    // === COMPTES ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("SELECT * FROM comptes WHERE actif = 1 ORDER BY nom ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM comptes WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM comptes WHERE actif = 1")
    suspend fun getAccountsList(): List<AccountEntity>

    // === PRODUITS ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("SELECT * FROM produits WHERE actif = 1 ORDER BY nom ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM produits WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM produits WHERE actif = 1")
    suspend fun getProductsList(): List<ProductEntity>

    // === OPERATIONS ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(op: OperationEntity): Long

    @Update
    suspend fun updateOperation(op: OperationEntity)

    @Query("SELECT * FROM operations ORDER BY date DESC, id DESC")
    fun getAllOperations(): Flow<List<OperationEntity>>

    @Query("SELECT * FROM operations ORDER BY date DESC, id DESC")
    suspend fun getOperationsList(): List<OperationEntity>

    @Query("SELECT * FROM operations WHERE id = :id LIMIT 1")
    suspend fun getOperationById(id: Long): OperationEntity?

    // === PARAMETRES ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setParam(param: ParamEntity)

    @Query("SELECT * FROM parametres WHERE cle = :cle LIMIT 1")
    suspend fun getParam(cle: String): ParamEntity?

    @Query("SELECT * FROM parametres")
    fun getAllParams(): Flow<List<ParamEntity>>

    // === LOGS AUDIT ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM logs_audit ORDER BY date DESC LIMIT 100")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>
}
