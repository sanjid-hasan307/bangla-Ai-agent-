package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CallRecord
import com.example.data.model.Organization
import com.example.data.model.ReceptionistConfig
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CallActiveGreen
import com.example.ui.theme.CallHandoffAmber
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.VioletAccent

@Composable
fun DashboardScreen(
    currentOrg: Organization?,
    config: ReceptionistConfig?,
    calls: List<CallRecord>,
    appointmentCount: Int,
    onStartTestCall: (callerName: String, callerPhone: String) -> Unit,
    onNavigateToCalls: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.receptionist_hero_banner),
                            contentDescription = "AI Receptionist Hero",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Color.Transparent, Slate900.copy(alpha = 0.9f))
                                    )
                                )
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "বাংলারেসেপশনিস্ট কন্টাক্ট হাব",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = IndigoPrimary
                            ) {
                                Text(
                                    text = currentOrg?.planName ?: "Growth Pro",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "২৪/৭ স্বয়ংক্রিয় বাংলা ও ইংরেজি ভয়েস কল হ্যান্ডলার। ডাক্তার শিডিউল, অ্যাপয়েন্টমেন্ট বুকিং, পার্সেল ট্র্যাকিং ও স্মার্ট হিউম্যান এসকেলেশন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onStartTestCall("মো: রফিকুল ইসলাম", "+880 1711-223344")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("simulate_call_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneInTalk,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("টেস্ট কল করুন", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onNavigateToSettings,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate600),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ভয়েস পার্সোনা")
                            }
                        }
                    }
                }
            }
        }

        // Metrics Grid (2x2)
        item {
            Text(
                text = "আজকের অপারেশনাল স্ট্যাটাস",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "মোট ইনবাউন্ড কল",
                    value = "${calls.size + 14}",
                    subtitle = "গত ২৪ ঘণ্টায় +১২%",
                    icon = Icons.Default.PhoneInTalk,
                    accentColor = IndigoPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "এআই সল্যুশন রেট",
                    value = "৮৮.৪%",
                    subtitle = "হিউম্যান ট্রান্সফার ১১.৬%",
                    icon = Icons.Default.CheckCircle,
                    accentColor = CallActiveGreen,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "নিশ্চিত অ্যাপয়েন্টমেন্ট",
                    value = "$appointmentCount টি",
                    subtitle = "অটোমেটেড ডাটাবেজ বুকিং",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = VioletAccent,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "টেলিফোনি মিনিট",
                    value = "${currentOrg?.minutesUsed ?: 142} মি.",
                    subtitle = "কোটা: ${currentOrg?.minutesLimit ?: 1000} মিনিট",
                    icon = Icons.Default.HourglassBottom,
                    accentColor = EmeraldSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Receptionist Config Overview
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(IndigoContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "সক্রিয় ভয়েস পার্সোনা",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                                Text(
                                    text = config?.personaName ?: "সূচনা (Shuchona)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = config?.languageMode ?: "দ্বিভাষিক (Bangla & English)",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Slate600
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "স্বাগত অভিবাদন বার্তা (Greeting):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Slate500
                    )
                    Text(
                        text = "\"${config?.greetingBn ?: "আসসালামু আলাইকুম! কেয়ারপয়েন্ট হাসপাতালে স্বাগতম। কীভাবে সাহায্য করতে পারি?"}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = CallHandoffAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ইমার্জেন্সি হিউম্যান এসকেলেশন: ${config?.humanHandoffNumber ?: "+8801711998877"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }
            }
        }

        // Recent Calls Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সাম্প্রতিক কাস্টমার কলসমূহ",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "সবগুলো দেখুন",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    ),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        items(calls.take(4)) { call ->
            RecentCallRowItem(call = call)
        }
    }
}

@Composable
fun RecentCallRowItem(call: CallRecord) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tint) = when (call.outcome) {
                "APPOINTMENT_BOOKED" -> Icons.Default.CalendarMonth to VioletAccent
                "HANDOFF_TO_HUMAN" -> Icons.Default.SupportAgent to CallHandoffAmber
                else -> Icons.Default.CheckCircle to CallActiveGreen
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = call.callerName.ifBlank { call.callerNumber },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${call.durationSeconds} সে.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
                Text(
                    text = call.summaryBn,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
