package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "organizations")
data class Organization(
    @PrimaryKey val id: String,
    val name: String,
    val nameBn: String,
    val industry: String, // Healthcare, Restaurant, E-Commerce, Education
    val phoneNumber: String,
    val address: String,
    val openingHoursBn: String,
    val isOpenToday: Boolean = true,
    val planName: String = "Growth Pro",
    val minutesUsed: Int = 142,
    val minutesLimit: Int = 1000
)

@Entity(tableName = "receptionist_configs")
data class ReceptionistConfig(
    @PrimaryKey val orgId: String,
    val personaName: String = "Shuchona (সূচনা)",
    val voiceGender: String = "Female",
    val languageMode: String = "Bilingual (Bangla & English)",
    val greetingBn: String = "আসসালামু আলাইকুম! কেয়ারপয়েন্ট স্পেশালাইজড হাসপাতালে আপনাকে স্বাগতম। আমি আপনার এআই রিসেপশনিস্ট সূচনা। আমি কীভাবে আপনাকে সাহায্য করতে পারি?",
    val greetingEn: String = "Welcome to CarePoint Specialized Hospital. I am Shuchona, your AI receptionist. How may I assist you today?",
    val humanHandoffNumber: String = "+8801712345678",
    val escalationReason: String = "ডাক্তারের জটিল চিকিৎসা পরামর্শ অথবা ইমার্জেন্সি সাপোর্ট",
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val isAutoTtsEnabled: Boolean = true,
    val isHandoffEnabled: Boolean = true
)

@Entity(tableName = "knowledge_items")
data class KnowledgeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orgId: String,
    val category: String, // "ডাক্তারদের সময়সূচি", "ফি ও টেস্টের মূল্য", "ঠিকানা ও যাতায়াত", "অর্ডার ও ডেলিভারি", "সাধারণ জিজ্ঞাসা"
    val questionBn: String,
    val answerBn: String,
    val answerEn: String,
    val keywords: String,
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_records")
data class CallRecord(
    @PrimaryKey val id: String,
    val orgId: String,
    val callerNumber: String,
    val callerName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val language: String, // "বাংলা", "English", "বাংলিশ / Code-switching"
    val outcome: String, // "AI_HANDLED", "APPOINTMENT_BOOKED", "ORDER_RESOLVED", "HANDOFF_TO_HUMAN"
    val summaryBn: String,
    val satisfactionScore: Int = 5, // 1 to 5
    val hasRecording: Boolean = true
)

@Entity(tableName = "call_transcripts")
data class CallTranscriptItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callId: String,
    val speaker: String, // "CALLER", "AI_RECEPTIONIST", "SYSTEM_HANDOFF"
    val messageText: String,
    val timestampOffsetSeconds: Int = 0
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orgId: String,
    val customerName: String,
    val customerPhone: String,
    val department: String, // "কার্ডিওলজি", "মেডিসিন", "দাঁতের ডাক্তার", "টেবিল রিজার্ভেশন"
    val doctorOrStaff: String,
    val appointmentDate: String, // "২০২৬-১০-০৪" or "Tomorrow 10:00 AM"
    val timeSlot: String,
    val status: String = "CONFIRMED", // "CONFIRMED", "PENDING", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderRecord(
    @PrimaryKey val orderId: String, // e.g. "BD-8842"
    val orgId: String,
    val customerPhone: String,
    val customerName: String,
    val orderStatus: String, // "ডেলিভারির পথে (Out for Delivery)", "প্রসেসিং হচ্ছে", "ডেলিভারি সম্পন্ন"
    val courierName: String, // "পাঠাও কুরিয়ার (Pathao)", "রেডএক্স", "স্টেডফাস্ট"
    val trackingCode: String,
    val itemsSummary: String,
    val totalAmountBdt: Double,
    val deliveryAddress: String
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orgId: String,
    val actionType: String, // "RECEPTIONIST_UPDATE", "KNOWLEDGE_ADD", "CALL_RECORDED", "HANDOFF_TRIGGERED"
    val actor: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
