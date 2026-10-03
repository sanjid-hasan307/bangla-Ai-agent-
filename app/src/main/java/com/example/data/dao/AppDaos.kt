package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Appointment
import com.example.data.model.AuditLog
import com.example.data.model.CallRecord
import com.example.data.model.CallTranscriptItem
import com.example.data.model.KnowledgeItem
import com.example.data.model.OrderRecord
import com.example.data.model.Organization
import com.example.data.model.ReceptionistConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {
    @Query("SELECT * FROM organizations")
    fun getAllOrganizations(): Flow<List<Organization>>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    suspend fun getOrganizationById(id: String): Organization?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizations(orgs: List<Organization>)

    @Update
    suspend fun updateOrganization(org: Organization)
}

@Dao
interface ReceptionistConfigDao {
    @Query("SELECT * FROM receptionist_configs WHERE orgId = :orgId LIMIT 1")
    fun getConfigForOrg(orgId: String): Flow<ReceptionistConfig?>

    @Query("SELECT * FROM receptionist_configs WHERE orgId = :orgId LIMIT 1")
    suspend fun getConfigDirect(orgId: String): ReceptionistConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: ReceptionistConfig)
}

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_items WHERE orgId = :orgId ORDER BY id DESC")
    fun getKnowledgeForOrg(orgId: String): Flow<List<KnowledgeItem>>

    @Query("SELECT * FROM knowledge_items WHERE orgId = :orgId AND isActive = 1")
    suspend fun getActiveKnowledgeForOrg(orgId: String): List<KnowledgeItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: KnowledgeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<KnowledgeItem>)

    @Query("DELETE FROM knowledge_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface CallDao {
    @Query("SELECT * FROM call_records WHERE orgId = :orgId ORDER BY timestamp DESC")
    fun getCallsForOrg(orgId: String): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_records WHERE id = :callId LIMIT 1")
    suspend fun getCallById(callId: String): CallRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(calls: List<CallRecord>)

    @Query("SELECT * FROM call_transcripts WHERE callId = :callId ORDER BY id ASC")
    fun getTranscriptsForCall(callId: String): Flow<List<CallTranscriptItem>>

    @Query("SELECT * FROM call_transcripts WHERE callId = :callId ORDER BY id ASC")
    suspend fun getTranscriptsDirect(callId: String): List<CallTranscriptItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscripts(transcripts: List<CallTranscriptItem>)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE orgId = :orgId ORDER BY createdAt DESC")
    fun getAppointmentsForOrg(orgId: String): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<Appointment>)

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE orgId = :orgId")
    fun getOrdersForOrg(orgId: String): Flow<List<OrderRecord>>

    @Query("SELECT * FROM orders WHERE orgId = :orgId AND orderId = :orderId LIMIT 1")
    suspend fun findOrder(orgId: String, orderId: String): OrderRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderRecord>)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs WHERE orgId = :orgId ORDER BY timestamp DESC LIMIT 50")
    fun getAuditLogsForOrg(orgId: String): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog)
}
