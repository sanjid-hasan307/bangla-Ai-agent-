package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CallRecord
import com.example.data.model.CallTranscriptItem
import com.example.data.repository.ReceptionistRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CallActiveGreen
import com.example.ui.theme.CallHandoffAmber
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletAccent

@Composable
fun CallHistoryScreen(
    calls: List<CallRecord>,
    repository: ReceptionistRepository,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedCallForDetail by remember { mutableStateOf<CallRecord?>(null) }

    val filteredCalls = calls.filter { call ->
        val matchesSearch = call.callerName.contains(searchQuery, ignoreCase = true) ||
                call.callerNumber.contains(searchQuery, ignoreCase = true) ||
                call.summaryBn.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "APPOINTMENT" -> call.outcome == "APPOINTMENT_BOOKED"
            "HANDOFF" -> call.outcome == "HANDOFF_TO_HUMAN"
            "AI" -> call.outcome == "AI_HANDLED"
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("কলারের নাম, নাম্বার বা সামারি খুঁজুন...", color = Slate400) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Slate500)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("call_search_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                "ALL" to "সকল কল (${calls.size})",
                "APPOINTMENT" to "অ্যাপয়েন্টমেন্ট বুকিং",
                "AI" to "এআই সল্যুশন",
                "HANDOFF" to "হিউম্যান ট্রান্সফার"
            )
            items(filters) { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoContainer,
                        selectedLabelColor = OnIndigoContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Call list
        if (filteredCalls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "কোনো সংরক্ষিত কল রেকর্ড পাওয়া যায়নি।",
                    color = Slate400,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCalls) { call ->
                    CallHistoryCard(
                        call = call,
                        onClick = { selectedCallForDetail = call }
                    )
                }
            }
        }
    }

    // Call Details Dialog
    if (selectedCallForDetail != null) {
        CallDetailDialog(
            call = selectedCallForDetail!!,
            repository = repository,
            onDismiss = { selectedCallForDetail = null }
        )
    }
}

@Composable
fun CallHistoryCard(
    call: CallRecord,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("call_card_${call.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when (call.outcome) {
                                    "APPOINTMENT_BOOKED" -> VioletAccent.copy(alpha = 0.15f)
                                    "HANDOFF_TO_HUMAN" -> CallHandoffAmber.copy(alpha = 0.15f)
                                    else -> CallActiveGreen.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (call.outcome) {
                                "APPOINTMENT_BOOKED" -> Icons.Default.CalendarMonth
                                "HANDOFF_TO_HUMAN" -> Icons.Default.SupportAgent
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = when (call.outcome) {
                                "APPOINTMENT_BOOKED" -> VioletAccent
                                "HANDOFF_TO_HUMAN" -> CallHandoffAmber
                                else -> CallActiveGreen
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = call.callerName.ifBlank { call.callerNumber },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = call.callerNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${call.durationSeconds} সে.",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = call.summaryBn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(call.satisfactionScore) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = when (call.outcome) {
                        "APPOINTMENT_BOOKED" -> "অ্যাপয়েন্টমেন্ট সম্পন্ন"
                        "HANDOFF_TO_HUMAN" -> "হিউম্যান ট্রান্সফার"
                        else -> "এআই মীমাংসিত"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = when (call.outcome) {
                        "APPOINTMENT_BOOKED" -> VioletAccent
                        "HANDOFF_TO_HUMAN" -> CallHandoffAmber
                        else -> CallActiveGreen
                    }
                )
            }
        }
    }
}

@Composable
fun CallDetailDialog(
    call: CallRecord,
    repository: ReceptionistRepository,
    onDismiss: () -> Unit
) {
    val transcripts by repository.getTranscriptsForCall(call.id).collectAsState(initial = emptyList())
    var isPlayingSimulatedAudio by remember { mutableStateOf(false) }
    var playProgress by remember { mutableFloatStateOf(0.35f) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = call.callerName.ifBlank { "সম্মানিত কলার" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${call.callerNumber} • ${call.durationSeconds} সেকেন্ড",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Audio Playback Scrubber Simulator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isPlayingSimulatedAudio = !isPlayingSimulatedAudio },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary)
                        ) {
                            Icon(
                                imageVector = if (isPlayingSimulatedAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Slider(
                            value = playProgress,
                            onValueChange = { playProgress = it },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = IndigoPrimary,
                                activeTrackColor = IndigoPrimary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "01:14",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500
                        )
                    }
                }

                Text(
                    text = "কথোপকথনের লিখিত ট্রান্সক্রিপ্ট (Transcript):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (transcripts.isEmpty()) {
                        item {
                            Text(
                                text = "এই কলের বিস্তারিত রেকর্ড সংরক্ষিত রয়েছে।",
                                color = Slate400,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        items(transcripts) { transcript ->
                            val isCaller = transcript.speaker == "CALLER"
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isCaller) Alignment.End else Alignment.Start
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isCaller) Slate200 else IndigoContainer,
                                    modifier = Modifier.widthIn(max = 260.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (isCaller) "কলার" else "এআই রিসেপশনিস্ট",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isCaller) Slate700 else OnIndigoContainer
                                        )
                                        Text(
                                            text = transcript.messageText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isCaller) Slate900 else OnIndigoContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("বন্ধ করুন")
                    }
                }
            }
        }
    }
}
