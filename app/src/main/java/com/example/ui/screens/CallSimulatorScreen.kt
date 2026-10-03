package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PhonePaused
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallTranscriptItem
import com.example.data.model.Organization
import com.example.data.model.ReceptionistConfig
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.theme.CallActiveGreen
import com.example.ui.theme.CallEndedRed
import com.example.ui.theme.CallHandoffAmber
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.ActiveCallUiState
import com.example.ui.viewmodel.CallSessionState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallSimulatorScreen(
    callState: ActiveCallUiState,
    currentOrg: Organization?,
    config: ReceptionistConfig?,
    isSpeaking: Boolean,
    onAnswerCall: () -> Unit,
    onSendUtterance: (String) -> Unit,
    onHandoffClick: () -> Unit,
    onEndCall: () -> Unit,
    onResetCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onStartTestCall: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputUtterance by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(callState.transcriptList.size) {
        if (callState.transcriptList.isNotEmpty()) {
            listState.animateScrollToItem(callState.transcriptList.size - 1)
        }
    }

    when (callState.sessionState) {
        CallSessionState.IDLE -> {
            IdleCallLauncher(
                currentOrg = currentOrg,
                config = config,
                onStartCall = onStartTestCall,
                modifier = modifier
            )
        }
        CallSessionState.RINGING -> {
            RingingCallView(
                callState = callState,
                currentOrg = currentOrg,
                onAnswer = onAnswerCall,
                onDecline = onEndCall,
                modifier = modifier
            )
        }
        CallSessionState.ACTIVE, CallSessionState.TRANSFERRED -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Slate950)
            ) {
                // In-Call Top Status Bar
                InCallHeader(
                    callState = callState,
                    currentOrg = currentOrg,
                    config = config,
                    isSpeaking = isSpeaking
                )

                // Tool Execution Notification Badge
                AnimatedVisibility(visible = callState.activeToolName != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = IndigoDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CallActiveGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "[টুল: ${callState.activeToolName}] ${callState.activeToolDetails ?: ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Live Audio Waveform (Visible during active speaking)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AudioWaveformVisualizer(
                        isSpeaking = isSpeaking || callState.isThinking,
                        activeColor = if (callState.sessionState == CallSessionState.TRANSFERRED) CallHandoffAmber else IndigoPrimary
                    )
                }

                // Transcript Feed
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(callState.transcriptList) { item ->
                        TranscriptBubble(item = item, personaName = config?.personaName ?: "সূচনা")
                    }

                    if (callState.isThinking) {
                        item {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(IndigoPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${config?.personaName ?: "সূচনা"} উত্তর প্রস্তুত করছে...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }

                // Suggested Bangladeshi Caller Utterance Chips
                if (callState.sessionState == CallSessionState.ACTIVE) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate900)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "দ্রুত কলার ভয়েস ইনপুট নির্বাচন করুন:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val suggestions = when {
                                currentOrg?.industry?.contains("Restaurant") == true -> listOf(
                                    "আজকের স্পেশাল মেন্যু কী?",
                                    "৪ জনের একটি টেবিল রিজার্ভেশন দিন",
                                    "হোম ডেলিভারির চার্জ কত?",
                                    "রেস্তোরাঁর অবস্থান কোথায়?"
                                )
                                currentOrg?.industry?.contains("E-Commerce") == true -> listOf(
                                    "আমার অর্ডার #BD-8842 কোথায়?",
                                    "ডেলিভারি চার্জ কত টাকা?",
                                    "অর্ডার বাতিল বা রিটার্ন কীভাবে করব?",
                                    "অপারেটরের সাথে কথা বলতে চাই"
                                )
                                else -> listOf(
                                    "কার্ডিওলজিস্ট ডাক্তার কখন বসেন?",
                                    "ডাক্তারের ভিজিট ফি ও ইকো টেস্টের চার্জ কত?",
                                    "আগামীকাল একটি অ্যাপয়েন্টমেন্ট বুকিং দিন",
                                    "হাসপাতালের সঠিক ঠিকানা কোথায়?",
                                    "ইমার্জেন্সি হিউম্যান ডাক্তারের সাথে কথা বলতে চাই"
                                )
                            }

                            suggestions.forEach { prompt ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Slate800,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSendUtterance(prompt) }
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Custom utterance text input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputUtterance,
                                onValueChange = { inputUtterance = it },
                                placeholder = {
                                    Text("বাংলা বা ইংরেজিতে কলারের প্রশ্ন লিখুন...", color = Slate500, fontSize = 13.sp)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("caller_input_field"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (inputUtterance.isNotBlank()) {
                                        onSendUtterance(inputUtterance)
                                        inputUtterance = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary)
                                    .testTag("send_utterance_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // In-Call Action Control Bar (Mute, Speaker, Handoff, End Call)
                InCallControls(
                    callState = callState,
                    onToggleSpeaker = onToggleSpeaker,
                    onToggleMute = onToggleMute,
                    onHandoffClick = onHandoffClick,
                    onEndCall = onEndCall
                )
            }
        }
        CallSessionState.ENDED -> {
            EndedCallSummaryView(
                callState = callState,
                onRestart = onResetCall,
                modifier = modifier
            )
        }
    }
}

@Composable
fun IdleCallLauncher(
    currentOrg: Organization?,
    config: ReceptionistConfig?,
    onStartCall: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var callerName by remember { mutableStateOf("মো: রফিকুল ইসলাম") }
    var callerPhone by remember { mutableStateOf("+880 1711-223344") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(IndigoPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneInTalk,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "রিয়েল-টাইম কল সিমুলেটর",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${currentOrg?.nameBn ?: "কেয়ারপয়েন্ট হাসপাতাল"} এর ভার্চুয়াল রিসেপশনিস্টে একটি টেস্ট কল ইনিশিয়েট করুন। স্বয়ংক্রিয় ভয়েস, নলেজ গ্রাউন্ডিং ও টুলস পরীক্ষা করুন।",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "কলার তথ্য (Caller Info):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = callerName,
                    onValueChange = { callerName = it },
                    label = { Text("কলারের নাম") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = callerPhone,
                    onValueChange = { callerPhone = it },
                    label = { Text("কলারের মোবাইল নম্বর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = { onStartCall(callerName, callerPhone) },
            colors = ButtonDefaults.buttonColors(containerColor = CallActiveGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("incoming_call_simulate_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ইনবাউন্ড কল সিমুলেট করুন (Ring Inbound)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun RingingCallView(
    callState: ActiveCallUiState,
    currentOrg: Organization?,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = "ইনবাউন্ড কল আসছে...",
                style = MaterialTheme.typography.titleMedium,
                color = CallActiveGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = callState.callerName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = callState.callerNumber,
                style = MaterialTheme.typography.titleSmall,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(30.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Slate900,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "গন্তব্য: ${currentOrg?.nameBn ?: "কেয়ারপয়েন্ট হাসপাতাল"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }

        // Animated Ringing Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(IndigoPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(IndigoPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneInTalk,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Accept / Decline Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(CallEndedRed)
                        .testTag("decline_call_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Decline",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("বাতিল করুন", color = Slate400, style = MaterialTheme.typography.labelSmall)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onAnswer,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(CallActiveGreen)
                        .testTag("answer_call_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Answer",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("রিসিভ করুন", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun InCallHeader(
    callState: ActiveCallUiState,
    currentOrg: Organization?,
    config: ReceptionistConfig?,
    isSpeaking: Boolean
) {
    Surface(
        color = Slate900,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val minutes = callState.durationSeconds / 60
            val seconds = callState.durationSeconds % 60
            val formattedTime = String.format("%02d:%02d", minutes, seconds)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = callState.callerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${callState.callerNumber} • ${currentOrg?.nameBn ?: ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (callState.sessionState == CallSessionState.TRANSFERRED) CallHandoffAmber.copy(alpha = 0.2f) else CallActiveGreen.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (callState.sessionState == CallSessionState.TRANSFERRED) CallHandoffAmber else CallActiveGreen
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (callState.sessionState == CallSessionState.TRANSFERRED) CallHandoffAmber else CallActiveGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (callState.sessionState == CallSessionState.TRANSFERRED) "হিউম্যান ট্রান্সফার" else formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TranscriptBubble(item: CallTranscriptItem, personaName: String) {
    val isCaller = item.speaker == "CALLER"
    val isHandoff = item.speaker == "SYSTEM_HANDOFF"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCaller) Alignment.End else Alignment.Start
    ) {
        Text(
            text = when {
                isCaller -> "কলার"
                isHandoff -> "সিস্টেম এসকেলেশন"
                else -> personaName
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (isCaller) Slate400 else IndigoPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCaller) 16.dp else 4.dp,
                bottomEnd = if (isCaller) 4.dp else 16.dp
            ),
            color = when {
                isHandoff -> CallHandoffAmber.copy(alpha = 0.2f)
                isCaller -> Slate800
                else -> IndigoDark
            },
            border = if (isHandoff) androidx.compose.foundation.BorderStroke(1.dp, CallHandoffAmber) else null,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = item.messageText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun InCallControls(
    callState: ActiveCallUiState,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onHandoffClick: () -> Unit,
    onEndCall: () -> Unit
) {
    Surface(
        color = Slate950,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speaker Button
            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (callState.isSpeakerOn) IndigoPrimary else Slate800)
            ) {
                Icon(
                    imageVector = if (callState.isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                    contentDescription = "Speaker",
                    tint = Color.White
                )
            }

            // Mute Button
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (callState.isMuted) CallEndedRed else Slate800)
            ) {
                Icon(
                    imageVector = if (callState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = Color.White
                )
            }

            // Human Handoff Button
            IconButton(
                onClick = onHandoffClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CallHandoffAmber)
                    .testTag("transfer_human_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Human Handoff",
                    tint = Color.White
                )
            }

            // End Call Button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(CallEndedRed)
                    .testTag("end_call_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun EndedCallSummaryView(
    callState: ActiveCallUiState,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(CallActiveGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = CallActiveGreen,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "কল সমাপ্ত ও সংরক্ষিত",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "কথোপকথন ও অডিও ট্রান্সক্রিপ্ট সফলভাবে স্থানীয় ডাটাবেজে সংরক্ষণ করা হয়েছে।",
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("কলের সময়কাল:", color = Slate500, style = MaterialTheme.typography.bodySmall)
                    Text("${callState.durationSeconds} সেকেন্ড", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ফলাফল (Outcome):", color = Slate500, style = MaterialTheme.typography.bodySmall)
                    Text(callState.endOutcome ?: "AI_HANDLED", fontWeight = FontWeight.Bold, color = IndigoPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("মোট বার্তা আদান-প্রদান:", color = Slate500, style = MaterialTheme.typography.bodySmall)
                    Text("${callState.transcriptList.size} টি ডায়ালগ", fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("reset_call_simulator_btn")
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("নতুন টেস্ট কল শুরু করুন")
        }
    }
}
