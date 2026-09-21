/**
 * FORDANY MANAGEMENT - Système de gestion financière et commerciale
 * Conçu pour Ets FORDANY (RDC)
 * Développé par KGC Technologies
 * Tous droits réservés.
 */
package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        ActivationEntity::class,
        EnterpriseEntity::class,
        AccountEntity::class,
        OperationTypeEntity::class,
        ProductEntity::class,
        OperationEntity::class,
        UserEntity::class,
        ParamEntity::class,
        AuditLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fordany_management.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}
