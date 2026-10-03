package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppHeader
import com.example.ui.components.OrgSelectionBottomSheet
import com.example.ui.screens.AppointmentsAndOrdersScreen
import com.example.ui.screens.CallHistoryScreen
import com.example.ui.screens.CallSimulatorScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.KnowledgeBaseScreen
import com.example.ui.screens.SettingsAndAuditScreen
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.Slate400
import com.example.ui.viewmodel.CallSessionState
import com.example.ui.viewmodel.ReceptionistViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

enum class NavTab(val title: String, val icon: ImageVector, val testTag: String) {
    DASHBOARD("ওভারভিউ", Icons.Default.Dashboard, "nav_dashboard"),
    SIMULATOR("কল স্টুডিও", Icons.Default.PhoneInTalk, "nav_simulator"),
    HISTORY("কল রেকর্ড", Icons.Default.History, "nav_history"),
    KNOWLEDGE("নলেজ", Icons.AutoMirrored.Filled.MenuBook, "nav_knowledge"),
    OPERATIONS("অপারেশন", Icons.Default.CalendarMonth, "nav_operations"),
    SETTINGS("সেটিংস", Icons.Default.Settings, "nav_settings")
}

@Composable
fun MainAppContent(
    viewModel: ReceptionistViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showOrgSheet by remember { mutableStateOf(false) }

    val currentOrg by viewModel.currentOrg.collectAsState()
    val organizations by viewModel.organizations.collectAsState()
    val selectedOrgId by viewModel.selectedOrgId.collectAsState()
    val config by viewModel.currentConfig.collectAsState()
    val calls by viewModel.calls.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val knowledgeItems by viewModel.knowledgeItems.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val callState by viewModel.callState.collectAsState()
    val isSpeaking by viewModel.speechManager.isSpeaking.collectAsState()

    // Handle back button to return to Dashboard if in secondary tab
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Only show top header when not in active full-screen call
            if (callState.sessionState == CallSessionState.IDLE || selectedTab != 1) {
                AppHeader(
                    currentOrg = currentOrg,
                    onSwitchOrgClick = { showOrgSheet = true }
                )
            }
        },
        bottomBar = {
            // Hide bottom bar when inside active voice call screen to maximize phone screen experience
            if (callState.sessionState != CallSessionState.RINGING && callState.sessionState != CallSessionState.ACTIVE) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    NavTab.values().forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(imageVector = tab.icon, contentDescription = tab.title)
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = IndigoPrimary,
                                indicatorColor = IndigoContainer,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    currentOrg = currentOrg,
                    config = config,
                    calls = calls,
                    appointmentCount = appointments.size,
                    onStartTestCall = { name, phone ->
                        viewModel.startCallSimulation(callerName = name, callerPhone = phone, autoAnswer = false)
                        selectedTab = 1
                    },
                    onNavigateToCalls = { selectedTab = 2 },
                    onNavigateToSettings = { selectedTab = 5 }
                )
                1 -> CallSimulatorScreen(
                    callState = callState,
                    currentOrg = currentOrg,
                    config = config,
                    isSpeaking = isSpeaking,
                    onAnswerCall = { viewModel.answerIncomingCall() },
                    onSendUtterance = { viewModel.sendCallerUtterance(it) },
                    onHandoffClick = { viewModel.triggerManualHandoff() },
                    onEndCall = { viewModel.endCall() },
                    onResetCall = { viewModel.resetCallToIdle() },
                    onToggleSpeaker = { viewModel.toggleSpeaker() },
                    onToggleMute = { viewModel.toggleMute() },
                    onStartTestCall = { name, phone ->
                        viewModel.startCallSimulation(callerName = name, callerPhone = phone, autoAnswer = false)
                    }
                )
                2 -> CallHistoryScreen(
                    calls = calls,
                    repository = viewModel.repository
                )
                3 -> KnowledgeBaseScreen(
                    items = knowledgeItems,
                    onAddKnowledge = { cat, q, a, kw -> viewModel.addKnowledgeItem(cat, q, a, kw) },
                    onDeleteKnowledge = { id -> viewModel.deleteKnowledgeItem(id) }
                )
                4 -> AppointmentsAndOrdersScreen(
                    appointments = appointments,
                    orders = orders,
                    onUpdateAppointmentStatus = { id, status -> viewModel.updateAppointmentStatus(id, status) }
                )
                5 -> SettingsAndAuditScreen(
                    currentOrg = currentOrg,
                    config = config,
                    auditLogs = auditLogs,
                    onUpdateConfig = { viewModel.updateConfig(it) },
                    onTestVoice = { text, rate, pitch ->
                        viewModel.speechManager.speak(text, rate, pitch)
                    }
                )
            }
        }
    }

    if (showOrgSheet) {
        OrgSelectionBottomSheet(
            organizations = organizations,
            selectedOrgId = selectedOrgId,
            onSelectOrg = { orgId -> viewModel.selectOrganization(orgId) },
            onDismiss = { showOrgSheet = false }
        )
    }
}
