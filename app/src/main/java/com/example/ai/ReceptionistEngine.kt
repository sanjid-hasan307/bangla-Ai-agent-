package com.example.ai

import com.example.BuildConfig
import com.example.data.model.Appointment
import com.example.data.model.KnowledgeItem
import com.example.data.model.Organization
import com.example.data.model.ReceptionistConfig
import com.example.data.repository.OrderLookupResult
import com.example.data.repository.ReceptionistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class EngineResponse(
    val replyText: String,
    val executedToolName: String? = null,
    val toolDetails: String? = null,
    val isHandoffTriggered: Boolean = false,
    val isAppointmentCreated: Boolean = false,
    val detectedIntent: String = "INFO_RESOLVED"
)

class ReceptionistEngine(
    private val repository: ReceptionistRepository
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun processCallerMessage(
        userInput: String,
        org: Organization,
        config: ReceptionistConfig,
        callerPhone: String,
        callerName: String,
        conversationHistory: List<Pair<String, String>> // Speaker to text
    ): EngineResponse = withContext(Dispatchers.IO) {
        val query = userInput.trim()

        // 1. Check for explicit Human Handoff or Emergency
        if (isHumanHandoffRequested(query)) {
            val reply = "জি অবশ্যই। আমি আপনাকে আমাদের অন-ডিউটি হিউম্যান এক্সিকিউটিভ (${config.humanHandoffNumber}) এর সাথে সরাসরি সংযোগ করে দিচ্ছি। অনুগ্রহ করে লাইনে থাকুন।"
            return@withContext EngineResponse(
                replyText = reply,
                executedToolName = "HUMAN_HANDOFF",
                toolDetails = "Escalated to ${config.humanHandoffNumber} (${config.escalationReason})",
                isHandoffTriggered = true,
                detectedIntent = "HANDOFF_TO_HUMAN"
            )
        }

        // 2. Check for Order Tracking (E-Commerce)
        val orderIdPattern = Pattern.compile("(?i)(BD-\\d{4}|\\b\\d{4,5}\\b)")
        val orderMatcher = orderIdPattern.matcher(query)
        if (query.contains("অর্ডার") || query.contains("order") || query.contains("পার্সেল") || orderMatcher.find()) {
            val foundOrderId = if (orderMatcher.find(0)) {
                val match = orderMatcher.group(1) ?: "BD-8842"
                if (!match.startsWith("BD-", ignoreCase = true)) "BD-$match" else match
            } else "BD-8842"

            val last4Digits = callerPhone.takeLast(4).filter { it.isDigit() }
            val lookup = repository.verifyAndGetOrder(org.id, foundOrderId, last4Digits)
            return@withContext when (lookup) {
                is OrderLookupResult.Success -> {
                    val order = lookup.order
                    val reply = "আপনার অর্ডার #${order.orderId} বর্তমানে ${order.orderStatus}। কুরিয়ার সার্ভিস: ${order.courierName} (ট্র্যাকিং কোড: ${order.trackingCode})। ডেলিভারি ঠিকানা: ${order.deliveryAddress}। মোট প্রদেয়: ${order.totalAmountBdt.toInt()} টাকা।"
                    EngineResponse(
                        replyText = reply,
                        executedToolName = "SECURE_ORDER_LOOKUP",
                        toolDetails = "Verified Order #${order.orderId} via phone auth",
                        detectedIntent = "ORDER_RESOLVED"
                    )
                }
                is OrderLookupResult.PhoneMismatch -> {
                    val reply = "অর্ডার #${foundOrderId} খুঁজে পাওয়া গেছে, কিন্তু সুরক্ষার স্বার্থে কলার ফোন নম্বরের ভেরিফিকেশন মেলেনি। অনুগ্রহ করে আপনার নিবন্ধিত ফোন নম্বর থেকে কল করুন অথবা অপারেটরের সাথে যোগাযোগ করুন।"
                    EngineResponse(
                        replyText = reply,
                        executedToolName = "AUTH_SECURITY_CHALLENGE",
                        toolDetails = "Failed phone auth check for $foundOrderId",
                        detectedIntent = "AUTH_FAILED"
                    )
                }
                is OrderLookupResult.NotFound -> {
                    val reply = "দুঃখিত, #${foundOrderId} নম্বরের কোনো অর্ডার ডাটাবেজে পাওয়া যায়নি। অনুগ্রহ করে সঠিক অর্ডার আইডি উল্লেখ করুন।"
                    EngineResponse(
                        replyText = reply,
                        executedToolName = "SECURE_ORDER_LOOKUP",
                        toolDetails = "Order $foundOrderId not found",
                        detectedIntent = "ORDER_RESOLVED"
                    )
                }
            }
        }

        // 3. Check for Appointment Booking intent
        if (query.contains("অ্যাপয়েন্টমেন্ট") || query.contains("সিরিয়াল") || query.contains("বুকিং") ||
            query.contains("appointment") || query.contains("book") || query.contains("রিজার্ভেশন")
        ) {
            val doctor = if (query.contains("কার্ডিওলজিস্ট") || query.contains("ফারহানা")) {
                "ডা. ফারহানা আহমেদ (কার্ডিওলজি)"
            } else if (query.contains("মেডিসিন") || query.contains("মনিরুল")) {
                "ডা. মনিরুল ইসলাম (মেডিসিন)"
            } else if (query.contains("দাঁত") || query.contains("নুসরাত")) {
                "ডা. নুসরাত জাহান (দাঁত ও অর্থোডন্টিক্স)"
            } else {
                "স্পেশালিস্ট কনসালটেন্ট"
            }

            val slot = "আগামীকাল সন্ধ্যা ৬:৩০ PM"
            val newAppt = Appointment(
                orgId = org.id,
                customerName = callerName.ifBlank { "সম্মানিত কলার" },
                customerPhone = callerPhone,
                department = if (org.industry.contains("Hospital")) "ওপিডি কনসালটেশন" else "টেবিল রিজার্ভেশন",
                doctorOrStaff = doctor,
                appointmentDate = "২০২৬-১০-০৪",
                timeSlot = slot,
                status = "CONFIRMED"
            )
            val apptId = repository.bookAppointment(newAppt)
            val reply = "আপনার অ্যাপয়েন্টমেন্ট সফলভাবে বুক করা হয়েছে! $doctor এর সাথে $slot এ আপনার সিরিয়াল নিশ্চিত করা হলো (বুকিং #$apptId)। ধন্যবাদ!"
            return@withContext EngineResponse(
                replyText = reply,
                executedToolName = "DATABASE_BOOK_APPOINTMENT",
                toolDetails = "Confirmed booking #$apptId for $callerName with $doctor at $slot",
                isAppointmentCreated = true,
                detectedIntent = "APPOINTMENT_BOOKED"
            )
        }

        // 4. Check Knowledge Base for Grounded Answers
        val activeKnowledge = repository.getActiveKnowledgeDirect(org.id)
        val matchedItem = findBestKnowledgeMatch(query, activeKnowledge)
        if (matchedItem != null) {
            val reply = matchedItem.answerBn
            return@withContext EngineResponse(
                replyText = reply,
                executedToolName = "GROUNDED_KNOWLEDGE_RETRIEVAL",
                toolDetails = "Matched FAQ in '${matchedItem.category}'",
                detectedIntent = "INFO_RESOLVED"
            )
        }

        // 5. Try calling Gemini API if a real API Key is configured
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && !apiKey.startsWith("MY_GEMINI") && apiKey.length > 10) {
            try {
                val geminiReply = callGeminiApi(apiKey, query, org, config, activeKnowledge)
                if (geminiReply.isNotBlank()) {
                    return@withContext EngineResponse(
                        replyText = geminiReply,
                        executedToolName = "GEMINI_3_5_FLASH_INFERENCE",
                        toolDetails = "Grounded dynamic LLM reasoning",
                        detectedIntent = "INFO_RESOLVED"
                    )
                }
            } catch (e: Exception) {
                // Fall back gracefully
            }
        }

        // 6. Intelligent Fallback for the Business
        val fallbackReply = "${org.nameBn} এ যোগাযোগ করার জন্য ধন্যবাদ। আমাদের লোকেশন: ${org.address}। সময়সূচি: ${org.openingHoursBn}। আপনি কি কোনো নির্দিষ্ট তথ্য বা হিউম্যান এজেন্টের সাহায্য চাচ্ছেন?"
        EngineResponse(
            replyText = fallbackReply,
            executedToolName = "BUSINESS_GENERAL_ASSIST",
            toolDetails = "Grounded on business metadata",
            detectedIntent = "INFO_RESOLVED"
        )
    }

    private fun isHumanHandoffRequested(query: String): Boolean {
        val lower = query.lowercase()
        return lower.contains("মানুষ") || lower.contains("অপারেটর") || lower.contains("এজেন্ট") ||
                lower.contains("ইমার্জেন্সি") || lower.contains("জরুরি") || lower.contains("ট্রান্সফার") ||
                lower.contains("human") || lower.contains("operator") || lower.contains("agent") ||
                lower.contains("emergency") || lower.contains("representative")
    }

    private fun findBestKnowledgeMatch(query: String, items: List<KnowledgeItem>): KnowledgeItem? {
        val tokens = query.lowercase().split(" ", "।", ",", "?", "-").filter { it.length > 2 }
        var bestScore = 0
        var bestItem: KnowledgeItem? = null

        for (item in items) {
            var score = 0
            val questionTokens = item.questionBn.lowercase()
            val keywords = item.keywords.lowercase()
            val category = item.category.lowercase()

            for (token in tokens) {
                if (questionTokens.contains(token)) score += 3
                if (keywords.contains(token)) score += 2
                if (category.contains(token)) score += 1
            }

            if (score > bestScore && score >= 2) {
                bestScore = score
                bestItem = item
            }
        }
        return bestItem
    }

    private fun callGeminiApi(
        apiKey: String,
        userQuery: String,
        org: Organization,
        config: ReceptionistConfig,
        knowledge: List<KnowledgeItem>
    ): String {
        val knowledgeContext = knowledge.joinToString("\n") {
            "- Q: ${it.questionBn} | A: ${it.answerBn}"
        }

        val systemPrompt = """
            You are '${config.personaName}', an elite AI Phone Receptionist for '${org.nameBn}' (${org.name}).
            Industry: ${org.industry}
            Address: ${org.address}
            Operating Hours: ${org.openingHoursBn}
            Transfer Number: ${config.humanHandoffNumber}
            
            Verified Business Facts:
            $knowledgeContext
            
            RULES:
            1. Respond naturally in spoken Bangla (or English if the user speaks English).
            2. Keep answers concise, polite, and directly suitable for voice phone calls (1-3 sentences).
            3. NEVER invent fake doctor names, fees, or facts not present above.
            4. If uncertain or emergency, offer transfer to human number ${config.humanHandoffNumber}.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userQuery) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 250)
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return ""

        val respBody = response.body?.string() ?: return ""
        val json = JSONObject(respBody)
        val candidates = json.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        if (parts.length() == 0) return ""

        return parts.getJSONObject(0).optString("text", "").trim()
    }
}
