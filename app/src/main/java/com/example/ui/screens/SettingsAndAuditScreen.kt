package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.AuditLog
import com.example.data.model.Organization
import com.example.data.model.ReceptionistConfig
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsAndAuditScreen(
    currentOrg: Organization?,
    config: ReceptionistConfig?,
    auditLogs: List<AuditLog>,
    onUpdateConfig: (ReceptionistConfig) -> Unit,
    onTestVoice: (text: String, rate: Float, pitch: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var personaName by remember(config) { mutableStateOf(config?.personaName ?: "সূচনা (Shuchona)") }
    var voiceGender by remember(config) { mutableStateOf(config?.voiceGender ?: "Female") }
    var greetingBn by remember(config) { mutableStateOf(config?.greetingBn ?: "") }
    var humanHandoffNumber by remember(config) { mutableStateOf(config?.humanHandoffNumber ?: "+8801711998877") }
    var escalationReason by remember(config) { mutableStateOf(config?.escalationReason ?: "") }
    var speechRate by remember(config) { mutableFloatStateOf(config?.speechRate ?: 1.0f) }
    var speechPitch by remember(config) { mutableFloatStateOf(config?.speechPitch ?: 1.0f) }
    var isAutoTtsEnabled by remember(config) { mutableStateOf(config?.isAutoTtsEnabled ?: true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Telephony Quotas & Plan
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "টেলিফোনি সাবস্ক্রিপশন ও মিনিট কোটা",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "প্ল্যান: ${currentOrg?.planName ?: "Growth Pro"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IndigoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CallActiveGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "সক্রিয় সাবস্ক্রিপশন",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CallActiveGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val used = currentOrg?.minutesUsed ?: 142
                    val limit = currentOrg?.minutesLimit ?: 1000
                    val progress = (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = IndigoPrimary,
                        trackColor = Slate200,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ব্যবহৃত: $used মিনিট",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                        Text(
                            text = "মোট বরাদ্দ: $limit মিনিট (${((1f - progress) * 100).toInt()}% অবশিষ্ট)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }
            }
        }

        // Section 2: AI Voice Persona & Speech Engine
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "এআই ভয়েস পার্সোনা কনফিগারেশন",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = personaName,
                        onValueChange = { personaName = it },
                        label = { Text("রিসেপশনিস্টের নাম (Persona Name)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = greetingBn,
                        onValueChange = { greetingBn = it },
                        label = { Text("স্বাগত সম্ভাষণ (Greeting Message in Bangla)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "কথা বলার গতি (Speech Rate): ${String.format("%.1fx", speechRate)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate600
                    )
                    Slider(
                        value = speechRate,
                        onValueChange = { speechRate = it },
                        valueRange = 0.8f..1.4f,
                        colors = SliderDefaults.colors(thumbColor = IndigoPrimary, activeTrackColor = IndigoPrimary)
                    )

                    Text(
                        text = "ভয়েস পিচ (Speech Pitch): ${String.format("%.1f", speechPitch)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate600
                    )
                    Slider(
                        value = speechPitch,
                        onValueChange = { speechPitch = it },
                        valueRange = 0.8f..1.3f,
                        colors = SliderDefaults.colors(thumbColor = IndigoPrimary, activeTrackColor = IndigoPrimary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "স্বয়ংক্রিয় অডিও কথা বলা (Auto Speak via TTS)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = isAutoTtsEnabled,
                            onCheckedChange = { isAutoTtsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onTestVoice(greetingBn, speechRate, speechPitch)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ভয়েস টেস্ট")
                        }

                        Button(
                            onClick = {
                                if (config != null) {
                                    val updated = config.copy(
                                        personaName = personaName,
                                        voiceGender = voiceGender,
                                        greetingBn = greetingBn,
                                        speechRate = speechRate,
                                        speechPitch = speechPitch,
                                        isAutoTtsEnabled = isAutoTtsEnabled,
                                        humanHandoffNumber = humanHandoffNumber,
                                        escalationReason = escalationReason
                                    )
                                    onUpdateConfig(updated)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_config_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }

        // Section 3: Human Escalation & Fallback
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = CallHandoffAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "হিউম্যান এসকেলেশন ও ফলব্যাক গেটওয়ে",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = humanHandoffNumber,
                        onValueChange = { humanHandoffNumber = it },
                        label = { Text("হিউম্যান হেল্পডেস্ক ফোন নম্বর") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = escalationReason,
                        onValueChange = { escalationReason = it },
                        label = { Text("এসকেলেশন নীতিমালা বা কারণ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 4: Security & Immutable Audit Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "সিকিউরিটি ও অপরিবর্তনীয় অডিট ট্রেইল (Audit Logs)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "প্রতিটি কনফিগারেশন পরিবর্তন, কলার ডেটা অ্যাক্সেস ও কল ট্রান্সফারের টাইমস্ট্যাম্পযুক্ত ইতিহাস।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }
        }

        items(auditLogs) { log ->
            AuditLogRowItem(log = log)
        }
    }
}

@Composable
fun AuditLogRowItem(log: AuditLog) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(log.timestamp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(IndigoContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.actionType,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = IndigoPrimary
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }

                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = "সম্পাদনকারী: ${log.actor}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
