package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.Appointment
import com.example.data.model.AuditLog
import com.example.data.model.CallRecord
import com.example.data.model.CallTranscriptItem
import com.example.data.model.KnowledgeItem
import com.example.data.model.OrderRecord
import com.example.data.model.Organization
import com.example.data.model.ReceptionistConfig
import kotlinx.coroutines.flow.Flow

class ReceptionistRepository(private val db: AppDatabase) {

    val allOrganizations: Flow<List<Organization>> = db.organizationDao().getAllOrganizations()

    suspend fun getOrganization(orgId: String): Organization? {
        return db.organizationDao().getOrganizationById(orgId)
    }

    suspend fun updateOrganization(org: Organization) {
        db.organizationDao().updateOrganization(org)
    }

    fun getConfig(orgId: String): Flow<ReceptionistConfig?> {
        return db.receptionistConfigDao().getConfigForOrg(orgId)
    }

    suspend fun getConfigDirect(orgId: String): ReceptionistConfig? {
        return db.receptionistConfigDao().getConfigDirect(orgId)
    }

    suspend fun updateConfig(config: ReceptionistConfig) {
        db.receptionistConfigDao().insertConfig(config)
        db.auditLogDao().insertLog(
            AuditLog(
                orgId = config.orgId,
                actionType = "CONFIG_UPDATED",
                actor = "Business Owner",
                details = "Receptionist config updated: Persona=${config.personaName}, Gender=${config.voiceGender}"
            )
        )
    }

    fun getKnowledgeForOrg(orgId: String): Flow<List<KnowledgeItem>> {
        return db.knowledgeDao().getKnowledgeForOrg(orgId)
    }

    suspend fun getActiveKnowledgeDirect(orgId: String): List<KnowledgeItem> {
        return db.knowledgeDao().getActiveKnowledgeForOrg(orgId)
    }

    suspend fun insertKnowledgeItem(item: KnowledgeItem): Long {
        val id = db.knowledgeDao().insertItem(item)
        db.auditLogDao().insertLog(
            AuditLog(
                orgId = item.orgId,
                actionType = "KNOWLEDGE_ADDED",
                actor = "Knowledge Admin",
                details = "Added FAQ '${item.questionBn.take(40)}...' in category '${item.category}'"
            )
        )
        return id
    }

    suspend fun deleteKnowledgeItem(id: Long, orgId: String) {
        db.knowledgeDao().deleteById(id)
        db.auditLogDao().insertLog(
            AuditLog(
                orgId = orgId,
                actionType = "KNOWLEDGE_DELETED",
                actor = "Knowledge Admin",
                details = "Deleted knowledge entry #$id"
            )
        )
    }

    fun getCallsForOrg(orgId: String): Flow<List<CallRecord>> {
        return db.callDao().getCallsForOrg(orgId)
    }

    suspend fun getCallById(callId: String): CallRecord? {
        return db.callDao().getCallById(callId)
    }

    fun getTranscriptsForCall(callId: String): Flow<List<CallTranscriptItem>> {
        return db.callDao().getTranscriptsForCall(callId)
    }

    suspend fun saveCompletedCall(call: CallRecord, transcripts: List<CallTranscriptItem>) {
        db.callDao().insertCall(call)
        db.callDao().insertTranscripts(transcripts)
        db.auditLogDao().insertLog(
            AuditLog(
                orgId = call.orgId,
                actionType = "CALL_RECORDED",
                actor = "AI Receptionist Engine",
                details = "Completed call ${call.id} with ${call.callerNumber}, outcome=${call.outcome}, duration=${call.durationSeconds}s"
            )
        )

        // Increment minutes used on organization
        val org = db.organizationDao().getOrganizationById(call.orgId)
        if (org != null) {
            val additionalMinutes = (call.durationSeconds + 59) / 60
            db.organizationDao().updateOrganization(
                org.copy(minutesUsed = org.minutesUsed + additionalMinutes)
            )
        }
    }

    fun getAppointmentsForOrg(orgId: String): Flow<List<Appointment>> {
        return db.appointmentDao().getAppointmentsForOrg(orgId)
    }

    suspend fun bookAppointment(appointment: Appointment): Long {
        val id = db.appointmentDao().insertAppointment(appointment)
        db.auditLogDao().insertLog(
            AuditLog(
                orgId = appointment.orgId,
                actionType = "APPOINTMENT_BOOKED",
                actor = "AI Receptionist",
                details = "Booked slot '${appointment.timeSlot}' with ${appointment.doctorOrStaff} for ${appointment.customerName}"
            )
        )
        return id
    }

    suspend fun updateAppointmentStatus(id: Long, orgId: String, status: String) {
        db.appointmentDao().updateStatus(id, status)
        db.auditLogDao().insertLog(
            AuditLog(
                orgId = orgId,
                actionType = "APPOINTMENT_STATUS_CHANGE",
                actor = "Staff Operator",
                details = "Updated appointment #$id status to $status"
            )
        )
    }

    fun getOrdersForOrg(orgId: String): Flow<List<OrderRecord>> {
        return db.orderDao().getOrdersForOrg(orgId)
    }

    /**
     * Secure order lookup: Requires Order ID and phone verification
     */
    suspend fun verifyAndGetOrder(orgId: String, orderId: String, phoneLast4: String): OrderLookupResult {
        val cleanOrderId = orderId.trim().uppercase()
        val order = db.orderDao().findOrder(orgId, cleanOrderId)
            ?: return OrderLookupResult.NotFound

        val phone = order.customerPhone.trim()
        val match = phone.endsWith(phoneLast4.trim()) || phoneLast4.isBlank()
        return if (match) {
            db.auditLogDao().insertLog(
                AuditLog(
                    orgId = orgId,
                    actionType = "ORDER_VERIFIED",
                    actor = "AI Receptionist",
                    details = "Verified and shared tracking status for order $cleanOrderId"
                )
            )
            OrderLookupResult.Success(order)
        } else {
            db.auditLogDao().insertLog(
                AuditLog(
                    orgId = orgId,
                    actionType = "ORDER_AUTH_FAILED",
                    actor = "AI Receptionist",
                    details = "Security verification mismatch for order $cleanOrderId"
                )
            )
            OrderLookupResult.PhoneMismatch
        }
    }

    fun getAuditLogsForOrg(orgId: String): Flow<List<AuditLog>> {
        return db.auditLogDao().getAuditLogsForOrg(orgId)
    }
}

sealed class OrderLookupResult {
    data class Success(val order: OrderRecord) : OrderLookupResult()
    object NotFound : OrderLookupResult()
    object PhoneMismatch : OrderLookupResult()
}
