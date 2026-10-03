package com.example.data.database

import com.example.data.model.Appointment
import com.example.data.model.AuditLog
import com.example.data.model.CallRecord
import com.example.data.model.CallTranscriptItem
import com.example.data.model.KnowledgeItem
import com.example.data.model.OrderRecord
import com.example.data.model.Organization
import com.example.data.model.ReceptionistConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun seedInitialData(database: AppDatabase) = withContext(Dispatchers.IO) {
        val orgDao = database.organizationDao()
        val configDao = database.receptionistConfigDao()
        val knowledgeDao = database.knowledgeDao()
        val callDao = database.callDao()
        val appointmentDao = database.appointmentDao()
        val orderDao = database.orderDao()
        val auditDao = database.auditLogDao()

        // Check if already seeded
        val existing = orgDao.getOrganizationById("org_carepoint")
        if (existing != null) return@withContext

        // 1. Organizations
        val orgs = listOf(
            Organization(
                id = "org_carepoint",
                name = "CarePoint Hospital & Diagnostic",
                nameBn = "কেয়ারপয়েন্ট হাসপাতাল ও ডায়াগনস্টিক",
                industry = "Healthcare & Hospital",
                phoneNumber = "+880 9612-445566",
                address = "রোড #৭, বাড়ি #১৪, ধানমন্ডি, ঢাকা-১২০৫",
                openingHoursBn = "২৪ ঘণ্টা ইমার্জেন্সি খোলা, ওপিডি কনসালটেশন সকাল ৯:০০ - রাত ১০:০০",
                isOpenToday = true,
                planName = "Enterprise Telephony",
                minutesUsed = 485,
                minutesLimit = 2500
            ),
            Organization(
                id = "org_kacchi",
                name = "Dhakaiya Kacchi Dine",
                nameBn = "ঢাকাইয়া কাচ্চি ডাইন",
                industry = "Food & Restaurant",
                phoneNumber = "+880 9612-778899",
                address = "বনানী ১১ নম্বর রোড, ব্লক ডি, ঢাকা-১২১৩",
                openingHoursBn = "প্রতিদিন দুপুর ১২:০০ - রাত ১১:৩০",
                isOpenToday = true,
                planName = "Growth Pro",
                minutesUsed = 192,
                minutesLimit = 1000
            ),
            Organization(
                id = "org_deshiwear",
                name = "DeshiWear Lifestyle E-Commerce",
                nameBn = "দেশিওয়্যার লাইফস্টাইল",
                industry = "E-Commerce & Retail",
                phoneNumber = "+880 9612-112233",
                address = "লেভেল ৪, যমুনা ফিউচার পার্ক, প্রগতি সরণি, ঢাকা",
                openingHoursBn = "কাস্টমার সাপোর্ট প্রতিদিন সকাল ১০:০০ - রাত ৮:০০",
                isOpenToday = true,
                planName = "Growth Pro",
                minutesUsed = 310,
                minutesLimit = 1500
            ),
            Organization(
                id = "org_shikho",
                name = "Apex IT Academy",
                nameBn = "অ্যাপেক্স আইটি একাডেমি",
                industry = "Education & Training",
                phoneNumber = "+880 9612-556677",
                address = "উত্তরা সেক্টর ৭, রবীন্দ্র সরণি, ঢাকা",
                openingHoursBn = "শনিবার থেকে বৃহস্পতিবার সকাল ১০:০০ - সন্ধ্যা ৭:০০",
                isOpenToday = true,
                planName = "Starter",
                minutesUsed = 64,
                minutesLimit = 500
            )
        )
        orgDao.insertOrganizations(orgs)

        // 2. Receptionist Configs
        configDao.insertConfig(
            ReceptionistConfig(
                orgId = "org_carepoint",
                personaName = "Shuchona (সূচনা)",
                voiceGender = "Female",
                languageMode = "Bilingual (Bangla & English)",
                greetingBn = "আসসালামু আলাইকুম! কেয়ারপয়েন্ট হাসপাতালে স্বাগতম। আমি সূচনা, আপনার এআই রিসেপশনিস্ট। কীভাবে সাহায্য করতে পারি?",
                greetingEn = "Welcome to CarePoint Hospital. I am Shuchona, your AI receptionist. How may I assist you today?",
                humanHandoffNumber = "+8801711998877",
                escalationReason = "জটিল মেডিক্যাল ইমার্জেন্সি বা সিনিয়র ডক্টরের সাথে সরাসরি কথা বলা",
                speechRate = 1.0f,
                speechPitch = 1.05f
            )
        )
        configDao.insertConfig(
            ReceptionistConfig(
                orgId = "org_kacchi",
                personaName = "Tanvir (তানভীর)",
                voiceGender = "Male",
                languageMode = "Bangla Preferred",
                greetingBn = "আসসালামু আলাইকুম! ঢাকাইয়া কাচ্চি ডাইনে স্বাগতম। টেবিল বুকিং বা হোম ডেলিভারির জন্য আমি সাহায্য করতে পারি।",
                greetingEn = "Welcome to Dhakaiya Kacchi Dine. How can I help you with table reservations or delivery orders?",
                humanHandoffNumber = "+8801812334455",
                escalationReason = "পার্টি/ক্যাটারিং বড় ইভেন্ট বুকিং",
                speechRate = 1.05f,
                speechPitch = 0.95f
            )
        )
        configDao.insertConfig(
            ReceptionistConfig(
                orgId = "org_deshiwear",
                personaName = "Ayesha (আয়েশা)",
                voiceGender = "Female",
                languageMode = "Bilingual (Bangla & English)",
                greetingBn = "দেশিওয়্যার কাস্টমার কেয়ারে স্বাগতম! আপনার অর্ডার ট্র্যাকিং অথবা নতুন অর্ডারের জন্য আমি সহায়তা করছি।",
                greetingEn = "Welcome to DeshiWear Customer Support! I can assist you with tracking your parcel or returns.",
                humanHandoffNumber = "+8801919887766",
                escalationReason = "পণ্য নষ্ট বা রিফান্ড ডিসপিউট",
                speechRate = 1.0f,
                speechPitch = 1.0f
            )
        )

        // 3. Knowledge Base
        val carepointKnowledge = listOf(
            KnowledgeItem(
                orgId = "org_carepoint",
                category = "ডাক্তারদের সময়সূচি",
                questionBn = "কার্ডিওলজিস্ট ও মেডিসিন বিশেষজ্ঞ ডাক্তার কখন বসেন?",
                answerBn = "কার্ডিওলজিস্ট ডা. ফারহানা আহমেদ রবি, মঙ্গল ও বৃহস্পতিবার বিকাল ৫:০০ থেকে রাত ৯:০০ টা পর্যন্ত রোগী দেখেন। মেডিসিন বিশেষজ্ঞ ডা. মনিরুল ইসলাম প্রতিদিন সকাল ১০:০০ থেকে দুপুর ২:০০ টা পর্যন্ত চেম্বার করেন।",
                answerEn = "Cardiologist Dr. Farhana Ahmed visits Sun, Tue, Thu from 5:00 PM to 9:00 PM. Medicine specialist Dr. Monirul Islam is available daily 10:00 AM to 2:00 PM.",
                keywords = "ডাক্তার সময়সূচি কার্ডিওলজিস্ট মেডিসিন চেম্বার doctor schedule"
            ),
            KnowledgeItem(
                orgId = "org_carepoint",
                category = "ফি ও টেস্টের মূল্য",
                questionBn = "ডাক্তারের ভিজিট ফি এবং টেস্টের চার্জ কত?",
                answerBn = "বিশেষজ্ঞ ডাক্তারের কনসালটেশন ফি ১,২০০ টাকা এবং ফলোআপ ৭০০ টাকা। ইকোকার্ডিওগ্রাম টেস্ট ৩,৫০০ টাকা, রক্তের সিবিসি টেস্ট ৬০০ টাকা এবং ডিজিটাল এক্স-রে ৮০০ টাকা।",
                answerEn = "Specialist consultation fee is 1,200 BDT (Follow-up 700 BDT). Echocardiogram is 3,500 BDT, CBC blood test is 600 BDT, and Digital X-Ray is 800 BDT.",
                keywords = "ভিজিট ফি টেস্ট খরচ চার্জ মূল্য ইকো সিবিসি এক্সরে fee cost price"
            ),
            KnowledgeItem(
                orgId = "org_carepoint",
                category = "ঠিকানা ও যাতায়াত",
                questionBn = "কেয়ারপয়েন্ট হাসপাতালের সঠিক ঠিকানা এবং অবস্থান কোথায়?",
                answerBn = "কেয়ারপয়েন্ট হাসপাতাল ধানমন্ডি ৭ নম্বর রোড, বাড়ি ১৪ (আনোয়ার খান মডার্ন হাসপাতালের ঠিক বিপরীতে অবস্থিত)। নিজস্ব কার পার্কিং এবং অ্যাম্বুলেন্স স্ট্যান্ড রয়েছে।",
                answerEn = "CarePoint Hospital is located at House 14, Road 7, Dhanmondi, Dhaka-1205 (opposite Anwar Khan Modern). Parking and ambulance bay available.",
                keywords = "ঠিকানা লোকেশন কোথায় ধানমন্ডি পার্কিং address location"
            ),
            KnowledgeItem(
                orgId = "org_carepoint",
                category = "অ্যাপয়েন্টমেন্ট গ্রহণ",
                questionBn = "রোগীর জন্য নতুন অ্যাপয়েন্টমেন্ট কীভাবে বুক করব?",
                answerBn = "আপনি আমাকে রোগীর পুরো নাম, মোবাইল নম্বর এবং পছন্দসই তারিখ জানালে আমি এখনই সিস্টেমে বুকিং নিশ্চিত করে দিচ্ছি।",
                answerEn = "Just provide the patient's full name, phone number, and preferred date/slot, and I will book the appointment immediately.",
                keywords = "অ্যাপয়েন্টমেন্ট বুকিং সিরিয়াল appointment booking serial"
            )
        )
        knowledgeDao.insertItems(carepointKnowledge)

        // Knowledge for E-Commerce
        val deshiwearKnowledge = listOf(
            KnowledgeItem(
                orgId = "org_deshiwear",
                category = "অর্ডার ও ডেলিভারি",
                questionBn = "আমার অর্ডারের বর্তমান অবস্থা কীভাবে জানব?",
                answerBn = "আপনার ৪ বা ৫ সংখ্যার অর্ডার আইডি (যেমন BD-8842) এবং সিকিউরিটি ভেরিফিকেশনের জন্য আপনার মোবাইল নম্বরের শেষ ৪ ডিজিট বলুন।",
                answerEn = "Please provide your Order ID (e.g. BD-8842) and the last 4 digits of your phone number for verification.",
                keywords = "অর্ডার ট্র্যাকিং পার্সেল ডেলিভারি order tracking parcel"
            ),
            KnowledgeItem(
                orgId = "org_deshiwear",
                category = "ডেলিভারি চার্জ ও সময়",
                questionBn = "ঢাকা ও ঢাকার বাইরে ডেলিভারি চার্জ কত এবং কতদিন সময় লাগে?",
                answerBn = "ঢাকার ভিতরে ডেলিভারি চার্জ ৬০ টাকা (২৪ থেকে ৪৮ ঘণ্টার মধ্যে ডেলিভারি)। ঢাকার বাইরে সমগ্র বাংলাদেশে ডেলিভারি চার্জ ১২০ টাকা (২ থেকে ৩ কর্মদিবস সময় লাগে)। ক্যাশ অন ডেলিভারি সুবিধা আছে।",
                answerEn = "Inside Dhaka delivery fee is 60 BDT (24-48 hrs). Outside Dhaka across Bangladesh is 120 BDT (2-3 business days). Cash on Delivery available.",
                keywords = "ডেলিভারি চার্জ সময় খরচ ক্যাশ অন ডেলিভারি delivery charge time"
            )
        )
        knowledgeDao.insertItems(deshiwearKnowledge)

        // 4. Sample Orders (with phone verification logic!)
        val orders = listOf(
            OrderRecord(
                orderId = "BD-8842",
                orgId = "org_deshiwear",
                customerPhone = "01712343456",
                customerName = "তানজিল আহমেদ",
                orderStatus = "ডেলিভারির পথে (Out for Delivery)",
                courierName = "পাঠাও কুরিয়ার (Pathao Logistics)",
                trackingCode = "PTH-DH-99214",
                itemsSummary = "প্রিমিয়াম সুতি পাঞ্জাবি (Navy Blue, Size L) x ১",
                totalAmountBdt = 1850.0,
                deliveryAddress = "মিরপুর ১০, ব্লক সি, রোড ৪, বাসা ১২"
            ),
            OrderRecord(
                orderId = "BD-9105",
                orgId = "org_deshiwear",
                customerPhone = "01844556677",
                customerName = "ফারহানা করিম",
                orderStatus = "কুরিয়ারে হস্তান্তর সম্পন্ন (In Transit)",
                courierName = "স্টেডফাস্ট কুরিয়ার",
                trackingCode = "SFC-CTG-1048",
                itemsSummary = "জামদানি এম্ব্রয়েডারি শাড়ি (Maroon) x ১",
                totalAmountBdt = 4200.0,
                deliveryAddress = "জিইসি মোড়, নাসিরাবাদ, চট্টগ্রাম"
            )
        )
        orderDao.insertOrders(orders)

        // 5. Sample Appointments
        val appointments = listOf(
            Appointment(
                orgId = "org_carepoint",
                customerName = "মো: রফিকুল ইসলাম",
                customerPhone = "01711223344",
                department = "কার্ডিওলজি (হৃদরোগ বিভাগ)",
                doctorOrStaff = "ডা. ফারহানা আহমেদ",
                appointmentDate = "আগামীকাল (Sunday)",
                timeSlot = "সন্ধ্যা ৬:৩০ PM",
                status = "CONFIRMED"
            ),
            Appointment(
                orgId = "org_carepoint",
                customerName = "সাবরিনা চৌধুরী",
                customerPhone = "01988776655",
                department = "মেডিসিন বিশেষজ্ঞ",
                doctorOrStaff = "ডা. মনিরুল ইসলাম",
                appointmentDate = "সোমবার (Monday)",
                timeSlot = "সকাল ১১:০০ AM",
                status = "CONFIRMED"
            ),
            Appointment(
                orgId = "org_carepoint",
                customerName = "কামরুল হাসান",
                customerPhone = "01822334411",
                department = "দাঁত ও অর্থোডন্টিক্স",
                doctorOrStaff = "ডা. নুসরাত জাহান",
                appointmentDate = "মঙ্গলবার (Tuesday)",
                timeSlot = "বিকাল ৪:০০ PM",
                status = "PENDING"
            )
        )
        appointmentDao.insertAppointments(appointments)

        // 6. Sample Past Calls & Transcripts
        val pastCalls = listOf(
            CallRecord(
                id = "call_01",
                orgId = "org_carepoint",
                callerNumber = "+880 1711-223344",
                callerName = "রফিকুল ইসলাম",
                durationSeconds = 142,
                language = "বাংলা",
                outcome = "APPOINTMENT_BOOKED",
                summaryBn = "রোগী কার্ডিওলজিস্ট ডা. ফারহানা আহমেদের চেম্বার শিডিউল জানতে চান এবং আগামীকাল সন্ধ্যা ৬:৩০ মিনিটে সফলভাবে অ্যাপয়েন্টমেন্ট বুকিং সম্পন্ন করেন।",
                satisfactionScore = 5
            ),
            CallRecord(
                id = "call_02",
                orgId = "org_carepoint",
                callerNumber = "+880 1912-889900",
                callerName = "রহিম চৌধুরী",
                durationSeconds = 98,
                language = "বাংলা",
                outcome = "AI_HANDLED",
                summaryBn = "ইকোকার্ডিওগ্রাম টেস্টের খরচ এবং শুক্রবার ওপিডি টেস্ট খোলা আছে কিনা জেনে সন্তুষ্ট হয়ে কল সমাপ্ত করেন।",
                satisfactionScore = 5
            ),
            CallRecord(
                id = "call_03",
                orgId = "org_carepoint",
                callerNumber = "+880 1823-774411",
                callerName = "সুমাইয়া আক্তার",
                durationSeconds = 175,
                language = "বাংলিশ / Code-switching",
                outcome = "HANDOFF_TO_HUMAN",
                summaryBn = "রোগীর তীব্র বুকে ব্যথা ও ইমার্জেন্সি অ্যাম্বুলেন্স সাপোর্টের প্রয়োজন হওয়ায় স্বয়ংক্রিয়ভাবে অন-ডিউটি সিনিয়র মেডিক্যাল অফিসারের কাছে কল ট্রান্সফার করা হয়।",
                satisfactionScore = 4
            )
        )
        callDao.insertCalls(pastCalls)

        // Transcripts for call_01
        val call01Transcripts = listOf(
            CallTranscriptItem(callId = "call_01", speaker = "AI_RECEPTIONIST", messageText = "আসসালামু আলাইকুম! কেয়ারপয়েন্ট হাসপাতালে স্বাগতম। আমি সূচনা, আপনার এআই রিসেপশনিস্ট। কীভাবে সাহায্য করতে পারি?"),
            CallTranscriptItem(callId = "call_01", speaker = "CALLER", messageText = "আপনাদের হার্টের বিশেষজ্ঞ ডা. ফারহানা আহমেদ কি আগামীকাল বসবেন? উনার ভিজিট ফি কত?"),
            CallTranscriptItem(callId = "call_01", speaker = "AI_RECEPTIONIST", messageText = "জি, কার্ডিওলজিস্ট ডা. ফারহানা আহমেদ আগামীকাল বিকাল ৫:০০ থেকে রাত ৯:০০ টা পর্যন্ত চেম্বার করবেন। উনার কনসালটেশন ফি ১,২০০ টাকা। আপনি কি উনার সাথে অ্যাপয়েন্টমেন্ট নিতে চাচ্ছেন?"),
            CallTranscriptItem(callId = "call_01", speaker = "CALLER", messageText = "হ্যাঁ, সন্ধ্যা সাড়ে ৬টায় একটি সিরিয়াল দিন। নাম মো: রফিকুল ইসলাম।"),
            CallTranscriptItem(callId = "call_01", speaker = "AI_RECEPTIONIST", messageText = "ধন্যবাদ জনাব রফিকুল ইসলাম। আগামীকাল সন্ধ্যা ৬:৩০ মিনিটে ডা. ফারহানা আহমেদের কাছে আপনার অ্যাপয়েন্টমেন্ট বুক করা হয়েছে। আপনার ফোনে কনফার্মেশন এসএমএস পাঠানো হবে। আর কোনো তথ্য কি জানতে চান?"),
            CallTranscriptItem(callId = "call_01", speaker = "CALLER", messageText = "না ধন্যবাদ, এটাই জানার ছিল। ভালো থাকবেন।"),
            CallTranscriptItem(callId = "call_01", speaker = "AI_RECEPTIONIST", messageText = "কেয়ারপয়েন্ট হাসপাতালে কল করার জন্য ধন্যবাদ। আপনার সুস্থতা কামনা করছি। শুভ সন্ধ্যা!")
        )
        callDao.insertTranscripts(call01Transcripts)

        // 7. Audit Logs
        val auditLogs = listOf(
            AuditLog(
                orgId = "org_carepoint",
                actionType = "RECEPTIONIST_INIT",
                actor = "System (Admin)",
                details = "AI Receptionist 'Shuchona' activated with verified bilingual medical knowledge."
            ),
            AuditLog(
                orgId = "org_carepoint",
                actionType = "KNOWLEDGE_PUBLISHED",
                actor = "Dr. Farhana Admin",
                details = "Updated Cardiology consultation schedule (5:00 PM - 9:00 PM)."
            ),
            AuditLog(
                orgId = "org_carepoint",
                actionType = "APPOINTMENT_CREATED",
                actor = "AI Receptionist Engine",
                details = "Created confirmed booking #1 for Rafiqul Islam with Dr. Farhana Ahmed."
            ),
            AuditLog(
                orgId = "org_carepoint",
                actionType = "EMERGENCY_HANDOFF",
                actor = "Handoff Gateway",
                details = "Transferred incoming call from 01823774411 to Emergency Officer +8801711998877."
            )
        )
        for (log in auditLogs) {
            auditDao.insertLog(log)
        }
    }
}
