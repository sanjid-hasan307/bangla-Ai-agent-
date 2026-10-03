package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ReceptionistEngine
import com.example.ai.SpeechManager
import com.example.data.database.AppDatabase
import com.example.data.database.DatabaseInitializer
import com.example.data.model.Appointment
import com.example.data.model.AuditLog
import com.example.data.model.CallRecord
import com.example.data.model.CallTranscriptItem
import com.example.data.model.KnowledgeItem
import com.example.data.model.OrderRecord
import com.example.data.model.Organization
import com.example.data.model.ReceptionistConfig
import com.example.data.repository.OrderLookupResult
import com.example.data.repository.ReceptionistRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CallSessionState {
    IDLE,
    RINGING,
    ACTIVE,
    TRANSFERRED,
    ENDED
}

data class ActiveCallUiState(
    val sessionState: CallSessionState = CallSessionState.IDLE,
    val callId: String = "",
    val callerName: String = "মো: রফিকুল ইসলাম",
    val callerNumber: String = "+880 1711-223344",
    val durationSeconds: Int = 0,
    val transcriptList: List<CallTranscriptItem> = emptyList(),
    val activeToolName: String? = null,
    val activeToolDetails: String? = null,
    val isThinking: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isMuted: Boolean = false,
    val handoffNumber: String? = null,
    val endOutcome: String? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ReceptionistViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = ReceptionistRepository(db)
    val speechManager = SpeechManager(application)
    private val engine = ReceptionistEngine(repository)

    private val _selectedOrgId = MutableStateFlow("org_carepoint")
    val selectedOrgId: StateFlow<String> = _selectedOrgId.asStateFlow()

    val organizations: StateFlow<List<Organization>> = repository.allOrganizations.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentOrg: StateFlow<Organization?> = combine(_selectedOrgId, organizations) { id, list ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentConfig: StateFlow<ReceptionistConfig?> = _selectedOrgId.flatMapLatest { id ->
        repository.getConfig(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val knowledgeItems: StateFlow<List<KnowledgeItem>> = _selectedOrgId.flatMapLatest { id ->
        repository.getKnowledgeForOrg(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calls: StateFlow<List<CallRecord>> = _selectedOrgId.flatMapLatest { id ->
        repository.getCallsForOrg(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = _selectedOrgId.flatMapLatest { id ->
        repository.getAppointmentsForOrg(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderRecord>> = _selectedOrgId.flatMapLatest { id ->
        repository.getOrdersForOrg(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = _selectedOrgId.flatMapLatest { id ->
        repository.getAuditLogsForOrg(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Call Simulation
    private val _callState = MutableStateFlow(ActiveCallUiState())
    val callState: StateFlow<ActiveCallUiState> = _callState.asStateFlow()

    private var callTimerJob: Job? = null

    init {
        viewModelScope.launch {
            DatabaseInitializer.seedInitialData(db)
        }
    }

    fun selectOrganization(orgId: String) {
        _selectedOrgId.value = orgId
    }

    fun updateConfig(config: ReceptionistConfig) {
        viewModelScope.launch {
            repository.updateConfig(config)
        }
    }

    fun addKnowledgeItem(category: String, question: String, answerBn: String, keywords: String) {
        val orgId = _selectedOrgId.value
        viewModelScope.launch {
            val item = KnowledgeItem(
                orgId = orgId,
                category = category,
                questionBn = question,
                answerBn = answerBn,
                answerEn = answerBn,
                keywords = keywords
            )
            repository.insertKnowledgeItem(item)
        }
    }

    fun deleteKnowledgeItem(id: Long) {
        val orgId = _selectedOrgId.value
        viewModelScope.launch {
            repository.deleteKnowledgeItem(id, orgId)
        }
    }

    fun startCallSimulation(
        callerName: String = "মো: রফিকুল ইসলাম",
        callerPhone: String = "+880 1711-223344",
        autoAnswer: Boolean = false
    ) {
        val callId = "call_${System.currentTimeMillis()}"
        _callState.value = ActiveCallUiState(
            sessionState = if (autoAnswer) CallSessionState.ACTIVE else CallSessionState.RINGING,
            callId = callId,
            callerName = callerName,
            callerNumber = callerPhone,
            durationSeconds = 0,
            transcriptList = emptyList()
        )

        if (autoAnswer) {
            connectCall()
        }
    }

    fun answerIncomingCall() {
        if (_callState.value.sessionState == CallSessionState.RINGING) {
            connectCall()
        }
    }

    private fun connectCall() {
        _callState.value = _callState.value.copy(
            sessionState = CallSessionState.ACTIVE
        )

        // Start call duration timer
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_callState.value.sessionState == CallSessionState.ACTIVE) {
                delay(1000)
                _callState.value = _callState.value.copy(
                    durationSeconds = _callState.value.durationSeconds + 1
                )
            }
        }

        // Deliver initial greeting from AI receptionist
        val config = currentConfig.value
        val greeting = config?.greetingBn ?: "আসসালামু আলাইকুম! আমি আপনার এআই রিসেপশনিস্ট। কীভাবে সাহায্য করতে পারি?"
        val initialItem = CallTranscriptItem(
            callId = _callState.value.callId,
            speaker = "AI_RECEPTIONIST",
            messageText = greeting,
            timestampOffsetSeconds = 0
        )

        _callState.value = _callState.value.copy(
            transcriptList = listOf(initialItem),
            activeToolName = "TELEPHONY_CALL_ACCEPTED",
            activeToolDetails = "Inbound call connected to '${config?.personaName ?: "Shuchona"}'"
        )

        if (_callState.value.isSpeakerOn && (config?.isAutoTtsEnabled != false)) {
            speechManager.speak(greeting, config?.speechRate ?: 1.0f, config?.speechPitch ?: 1.0f)
        }
    }

    fun sendCallerUtterance(userInput: String) {
        if (_callState.value.sessionState != CallSessionState.ACTIVE || userInput.isBlank()) return

        val callId = _callState.value.callId
        val callerItem = CallTranscriptItem(
            callId = callId,
            speaker = "CALLER",
            messageText = userInput.trim(),
            timestampOffsetSeconds = _callState.value.durationSeconds
        )

        val updatedList = _callState.value.transcriptList + callerItem
        _callState.value = _callState.value.copy(
            transcriptList = updatedList,
            isThinking = true,
            activeToolName = "LISTENING_INTENT_EXTRACTION",
            activeToolDetails = "Parsing Bangla voice input..."
        )

        viewModelScope.launch {
            val org = currentOrg.value ?: return@launch
            val config = currentConfig.value ?: ReceptionistConfig(org.id)

            // Simulate slight network/voice latency
            delay(500)

            val history = updatedList.map { it.speaker to it.messageText }
            val response = engine.processCallerMessage(
                userInput = userInput,
                org = org,
                config = config,
                callerPhone = _callState.value.callerNumber,
                callerName = _callState.value.callerName,
                conversationHistory = history
            )

            val aiItem = CallTranscriptItem(
                callId = callId,
                speaker = if (response.isHandoffTriggered) "SYSTEM_HANDOFF" else "AI_RECEPTIONIST",
                messageText = response.replyText,
                timestampOffsetSeconds = _callState.value.durationSeconds
            )

            _callState.value = _callState.value.copy(
                transcriptList = _callState.value.transcriptList + aiItem,
                isThinking = false,
                activeToolName = response.executedToolName,
                activeToolDetails = response.toolDetails,
                handoffNumber = if (response.isHandoffTriggered) config.humanHandoffNumber else null,
                endOutcome = response.detectedIntent
            )

            if (response.isHandoffTriggered) {
                _callState.value = _callState.value.copy(sessionState = CallSessionState.TRANSFERRED)
                callTimerJob?.cancel()
            }

            if (_callState.value.isSpeakerOn && config.isAutoTtsEnabled) {
                speechManager.speak(response.replyText, config.speechRate, config.speechPitch)
            }
        }
    }

    fun triggerManualHandoff() {
        val config = currentConfig.value ?: return
        val handoffItem = CallTranscriptItem(
            callId = _callState.value.callId,
            speaker = "SYSTEM_HANDOFF",
            messageText = "কলটি হিউম্যান সাপোর্ট হেল্পডেস্কে (${config.humanHandoffNumber}) স্থানান্তর করা হচ্ছে...",
            timestampOffsetSeconds = _callState.value.durationSeconds
        )
        _callState.value = _callState.value.copy(
            sessionState = CallSessionState.TRANSFERRED,
            transcriptList = _callState.value.transcriptList + handoffItem,
            activeToolName = "MANUAL_SUPERVISOR_HANDOFF",
            activeToolDetails = "Operator initiated call transfer to ${config.humanHandoffNumber}",
            handoffNumber = config.humanHandoffNumber,
            endOutcome = "HANDOFF_TO_HUMAN"
        )
        callTimerJob?.cancel()
        speechManager.speak("কলটি হিউম্যান সাপোর্টে স্থানান্তর করা হচ্ছে।", config.speechRate, config.speechPitch)
    }

    fun endCall() {
        val currentState = _callState.value
        callTimerJob?.cancel()
        speechManager.stop()

        val finalOutcome = currentState.endOutcome ?: if (currentState.sessionState == CallSessionState.TRANSFERRED) "HANDOFF_TO_HUMAN" else "AI_HANDLED"
        val callRecord = CallRecord(
            id = currentState.callId,
            orgId = _selectedOrgId.value,
            callerNumber = currentState.callerNumber,
            callerName = currentState.callerName,
            durationSeconds = maxOf(currentState.durationSeconds, 15),
            language = "বাংলা",
            outcome = finalOutcome,
            summaryBn = "কলার '${currentState.callerName}' এর ইনবাউন্ড কল। এআই রিসেপশনিস্ট সফলভাবে কথোপকথন পরিচালনা করেছে। (ফলাফল: $finalOutcome)",
            satisfactionScore = 5
        )

        viewModelScope.launch {
            repository.saveCompletedCall(callRecord, currentState.transcriptList)
        }

        _callState.value = currentState.copy(
            sessionState = CallSessionState.ENDED
        )
    }

    fun resetCallToIdle() {
        speechManager.stop()
        _callState.value = ActiveCallUiState()
    }

    fun toggleSpeaker() {
        _callState.value = _callState.value.copy(
            isSpeakerOn = !_callState.value.isSpeakerOn
        )
        if (!_callState.value.isSpeakerOn) {
            speechManager.stop()
        }
    }

    fun toggleMute() {
        _callState.value = _callState.value.copy(
            isMuted = !_callState.value.isMuted
        )
    }

    fun updateAppointmentStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(id, _selectedOrgId.value, status)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
        callTimerJob?.cancel()
    }
}
