package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AppointmentDao
import com.example.data.dao.AuditLogDao
import com.example.data.dao.CallDao
import com.example.data.dao.KnowledgeDao
import com.example.data.dao.OrderDao
import com.example.data.dao.OrganizationDao
import com.example.data.dao.ReceptionistConfigDao
import com.example.data.model.Appointment
import com.example.data.model.AuditLog
import com.example.data.model.CallRecord
import com.example.data.model.CallTranscriptItem
import com.example.data.model.KnowledgeItem
import com.example.data.model.OrderRecord
import com.example.data.model.Organization
import com.example.data.model.ReceptionistConfig

@Database(
    entities = [
        Organization::class,
        ReceptionistConfig::class,
        KnowledgeItem::class,
        CallRecord::class,
        CallTranscriptItem::class,
        Appointment::class,
        OrderRecord::class,
        AuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun organizationDao(): OrganizationDao
    abstract fun receptionistConfigDao(): ReceptionistConfigDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun callDao(): CallDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun orderDao(): OrderDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bangla_receptionist_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
