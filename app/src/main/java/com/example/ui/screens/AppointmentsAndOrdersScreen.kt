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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.OrderRecord
import com.example.ui.theme.CallActiveGreen
import com.example.ui.theme.CallEndedRed
import com.example.ui.theme.CallHandoffAmber
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.VioletAccent

@Composable
fun AppointmentsAndOrdersScreen(
    appointments: List<Appointment>,
    orders: List<OrderRecord>,
    onUpdateAppointmentStatus: (id: Long, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Order Verification Sandbox
    var testOrderId by remember { mutableStateOf("BD-8842") }
    var testPhoneLast4 by remember { mutableStateOf("3456") }
    var verificationResult by remember { mutableStateOf<String?>(null) }
    var isAuthSuccess by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("অ্যাপয়েন্টমেন্ট (${appointments.size})", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("অর্ডার ট্র্যাকিং (${orders.size})", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // Appointments Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "নিবন্ধিত শিডিউল ও বুকিং তালিকা",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "এআই রিসেপশনিস্ট কলারের সাথে নিশ্চিত হয়ে ডাটাবেজে কনফ্লিক্ট-ফ্রি অ্যাপয়েন্টমেন্ট শিডিউল করেছে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                if (appointments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("এখনও কোনো বুকিং নেই। টেস্ট কল করে একটি অ্যাপয়েন্টমেন্ট নিন।", color = Slate400)
                        }
                    }
                } else {
                    items(appointments) { appt ->
                        AppointmentCard(
                            appointment = appt,
                            onStatusChange = { newStatus -> onUpdateAppointmentStatus(appt.id, newStatus) }
                        )
                    }
                }
            }
        } else {
            // Orders Tab with Strict Authentication Sandbox
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সিকিউর কলার ভেরিফিকেশন টেস্ট (Auth Sandbox)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = "নীতিমালা অনুযায়ী: কলার শুধু অর্ডার আইডি জানলেই হবে না; মোবাইল নম্বরের শেষ ৪ ডিজিট ভেরিফিকেশন ছাড়া পার্সেল তথ্য প্রকাশ নিষিদ্ধ।",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = testOrderId,
                                    onValueChange = { testOrderId = it },
                                    label = { Text("অর্ডার আইডি") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = testPhoneLast4,
                                    onValueChange = { testPhoneLast4 = it },
                                    label = { Text("ফোনের শেষ ৪ ডিজিট") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val order = orders.find { it.orderId.equals(testOrderId.trim(), ignoreCase = true) }
                                    if (order == null) {
                                        verificationResult = "ত্রুটি: #${testOrderId} নম্বরের অর্ডার ডাটাবেজে নেই।"
                                        isAuthSuccess = false
                                    } else {
                                        val match = order.customerPhone.trim().endsWith(testPhoneLast4.trim())
                                        if (match) {
                                            verificationResult = "যাচাই সফল! কাস্টমার: ${order.customerName}, স্ট্যাটাস: ${order.orderStatus} (${order.courierName})।"
                                            isAuthSuccess = true
                                        } else {
                                            verificationResult = "নিরাপত্তা সতর্কতা: ফোন নম্বর অমিল! সুরক্ষার স্বার্থে পার্সেল তথ্য গোপন রাখা হয়েছে।"
                                            isAuthSuccess = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("নিরাপত্তা যাচাই ও স্ট্যাটাস দেখুন")
                            }

                            if (verificationResult != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isAuthSuccess) CallActiveGreen.copy(alpha = 0.12f) else CallEndedRed.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isAuthSuccess) CallActiveGreen else CallEndedRed
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                ) {
                                    Text(
                                        text = verificationResult!!,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = if (isAuthSuccess) CallActiveGreen else CallEndedRed,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "সিস্টেম অর্ডার রেজিস্ট্রি (Customer Orders)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(orders) { order ->
                    OrderRegistryCard(order = order)
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onStatusChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
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
                            .background(VioletAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = VioletAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = appointment.customerName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = appointment.customerPhone,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (appointment.status) {
                        "CONFIRMED" -> CallActiveGreen.copy(alpha = 0.15f)
                        "PENDING" -> CallHandoffAmber.copy(alpha = 0.15f)
                        else -> CallEndedRed.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when (appointment.status) {
                            "CONFIRMED" -> "নিশ্চিত"
                            "PENDING" -> "অপেক্ষমাণ"
                            else -> "বাতিল"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when (appointment.status) {
                            "CONFIRMED" -> CallActiveGreen
                            "PENDING" -> CallHandoffAmber
                            else -> CallEndedRed
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "ডাক্তার/স্টাফ: ${appointment.doctorOrStaff}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "বিভাগ: ${appointment.department}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    Text(
                        text = "সময়সূচি: ${appointment.appointmentDate} (${appointment.timeSlot})",
                        style = MaterialTheme.typography.bodySmall,
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (appointment.status != "CONFIRMED") {
                    TextButton(onClick = { onStatusChange("CONFIRMED") }) {
                        Text("নিশ্চিত করুন", color = CallActiveGreen)
                    }
                }
                if (appointment.status != "CANCELLED") {
                    TextButton(onClick = { onStatusChange("CANCELLED") }) {
                        Text("বাতিল করুন", color = RoseError)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderRegistryCard(order: OrderRecord) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "অর্ডার #${order.orderId}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldSecondary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = order.orderStatus,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "গ্রাহক: ${order.customerName} (${order.customerPhone.take(3)}****${order.customerPhone.takeLast(4)})",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )

            Text(
                text = "কুরিয়ার: ${order.courierName} • ট্র্যাকিং: ${order.trackingCode}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )

            Text(
                text = "পণ্য: ${order.itemsSummary}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )

            Text(
                text = "ঠিকানা: ${order.deliveryAddress}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "মোট মূল্য: ${order.totalAmountBdt.toInt()} টাকা",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = IndigoPrimary
            )
        }
    }
}
