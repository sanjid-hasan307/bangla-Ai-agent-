package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KnowledgeItem
import com.example.ui.theme.CallActiveGreen
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.OnIndigoContainer
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(
    items: List<KnowledgeItem>,
    onAddKnowledge: (category: String, question: String, answerBn: String, keywords: String) -> Unit,
    onDeleteKnowledge: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    // Interactive Grounding Sandbox state
    var testQuery by remember { mutableStateOf("") }
    var testResultAnswer by remember { mutableStateOf<String?>(null) }
    var testMatchedCategory by remember { mutableStateOf<String?>(null) }

    val categories = listOf("ALL" to "সকল বিষয়") + items.map { it.category }.distinct().map { it to it }

    val filteredItems = items.filter { item ->
        val matchesCategory = selectedCategory == "ALL" || item.category == selectedCategory
        val matchesSearch = item.questionBn.contains(searchQuery, ignoreCase = true) ||
                item.answerBn.contains(searchQuery, ignoreCase = true) ||
                item.keywords.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info & Description
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ব্যবসায়িক জ্ঞানভাণ্ডার (Knowledge Base)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ভেরিফায়েড তথ্যের উপর ভিত্তি করে এআই রিসেপশনিস্ট কলারদের প্রশ্নের নির্ভুল উত্তর দেবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }

            // Interactive Grounding Sandbox
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoContainer.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "গ্রাউন্ডিং ভেরিফিকেশন টেস্ট (Grounding Sandbox)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OnIndigoContainer
                                )
                            )
                        }
                        Text(
                            text = "যেকোনো সম্ভাব্য কলারের প্রশ্ন টাইপ করে দেখুন জ্ঞানভাণ্ডার থেকে সঠিক তথ্য গ্রাউন্ড হচ্ছে কিনা:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = testQuery,
                                onValueChange = { testQuery = it },
                                placeholder = { Text("যেমন: ভিজিট ফি কত? বা ডাক্তার কখন বসেন?", color = Slate400, fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("grounding_test_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (testQuery.isNotBlank()) {
                                        val match = items.find { item ->
                                            val q = testQuery.lowercase()
                                            item.questionBn.lowercase().contains(q) ||
                                                    item.keywords.lowercase().contains(q) ||
                                                    q.split(" ").any { item.keywords.lowercase().contains(it) && it.length > 2 }
                                        }
                                        if (match != null) {
                                            testResultAnswer = match.answerBn
                                            testMatchedCategory = match.category
                                        } else {
                                            testResultAnswer = "কোনো সরাসরি মিল পাওয়া যায়নি। সাধারণ ব্যবসায়িক তথ্য বা অপারেটর ট্রান্সফার প্রয়োগ হবে।"
                                            testMatchedCategory = "ডিফল্ট ফলব্যাক"
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("যাচাই")
                            }
                        }

                        if (testResultAnswer != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "ম্যাচ ক্যাটাগরি: $testMatchedCategory",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = IndigoPrimary
                                    )
                                    Text(
                                        text = testResultAnswer!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("প্রশ্ন বা কীওয়ার্ড খুঁজুন...", color = Slate400) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate500)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Category Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { selectedCategory = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoContainer,
                                selectedLabelColor = OnIndigoContainer
                            )
                        )
                    }
                }
            }

            // Items list
            items(filteredItems) { item ->
                KnowledgeItemCard(
                    item = item,
                    onDelete = { onDeleteKnowledge(item.id) }
                )
            }
        }

        // FAB to add new knowledge
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = IndigoPrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_knowledge_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add FAQ")
        }
    }

    // Add Knowledge Bottom Sheet
    if (showAddDialog) {
        AddKnowledgeBottomSheet(
            onDismiss = { showAddDialog = false },
            onSave = { cat, q, a, kw ->
                onAddKnowledge(cat, q, a, kw)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun KnowledgeItemCard(
    item: KnowledgeItem,
    onDelete: () -> Unit
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IndigoContainer
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnIndigoContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = RoseError,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.questionBn,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.answerBn,
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "কীওয়ার্ড: ${item.keywords}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKnowledgeBottomSheet(
    onDismiss: () -> Unit,
    onSave: (category: String, question: String, answerBn: String, keywords: String) -> Unit
) {
    var category by remember { mutableStateOf("ডাক্তারদের সময়সূচি") }
    var question by remember { mutableStateOf("") }
    var answerBn by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "নতুন জ্ঞান বা প্রশ্নোত্তর যুক্ত করুন",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("ক্যাটাগরি বা বিভাগ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("প্রশ্ন (বাংলায়)") },
                placeholder = { Text("যেমন: অর্থোপেডিক ডাক্তার কবে বসেন?") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = answerBn,
                onValueChange = { answerBn = it },
                label = { Text("ভেরিফায়েড উত্তর (স্পোকেন বাংলায়)") },
                placeholder = { Text("যেমন: ডা. জামিল প্রতি শনি ও সোম সন্ধ্যা ৬টায় রোগী দেখেন।") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = keywords,
                onValueChange = { keywords = it },
                label = { Text("অনুসন্ধান কীওয়ার্ড (স্পেস বা কমা দিয়ে)") },
                placeholder = { Text("যেমন: অর্থোপেডিক হাড় ব্যাথা ডাক্তার") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (question.isNotBlank() && answerBn.isNotBlank()) {
                        onSave(category, question, answerBn, keywords)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_knowledge_btn")
            ) {
                Text("সংরক্ষণ ও প্রকাশ করুন")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
