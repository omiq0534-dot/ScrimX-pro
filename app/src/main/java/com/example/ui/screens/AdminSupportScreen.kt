package com.example.ui.screens
import com.example.ui.theme.AppColors

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.FirebaseHelper

data class AdminSupportTicket(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "Player",
    val category: String = "General",
    val subject: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val status: String = "Pending", // "Pending" or "Answered"
    val adminReply: String = "",
    val answeredAt: Long = 0L
)

data class AdminFaqItem(
    val id: String = "",
    val question: String = "",
    val answer: String = "",
    val timestamp: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportScreen(navController: NavController) {
    val db = remember { FirebaseHelper.getFirestore() }
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: User Queries, 1: Live Channels Setup, 2: App FAQs
    var tickets by remember { mutableStateOf<List<AdminSupportTicket>>(emptyList()) }
    var faqs by remember { mutableStateOf<List<AdminFaqItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Live Support Channels State
    var supportEmail by remember { mutableStateOf("support@scrimx.com") }
    var supportWhatsApp by remember { mutableStateOf("+91 98765 43210") }
    var supportTelegram by remember { mutableStateOf("https://t.me/scrimx_official") }
    var supportDiscord by remember { mutableStateOf("https://discord.gg/scrimx") }
    var supportWorkingHours by remember { mutableStateOf("10:00 AM - 11:00 PM (Daily)") }
    var isSavingSettings by remember { mutableStateOf(false) }

    // Add FAQ Dialog State
    var showAddFaqDialog by remember { mutableStateOf(false) }
    var newFaqQuestion by remember { mutableStateOf("") }
    var newFaqAnswer by remember { mutableStateOf("") }

    // Query Filter: All, Pending, Answered
    var queryFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) {
        if (db != null) {
            // Realtime Queries Listener
            db.collection("support_tickets")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        tickets = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(AdminSupportTicket::class.java)?.copy(id = doc.id)
                        }.sortedByDescending { it.timestamp }
                    }
                    isLoading = false
                }

            // Realtime FAQs Listener
            db.collection("faqs")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null) {
                        faqs = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(AdminFaqItem::class.java)?.copy(id = doc.id)
                        }.sortedByDescending { it.timestamp }
                    }
                }

            // Fetch Current Support Channels
            db.collection("settings").document("support")
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        supportEmail = doc.getString("email") ?: supportEmail
                        supportWhatsApp = doc.getString("whatsapp") ?: supportWhatsApp
                        supportTelegram = doc.getString("telegram") ?: supportTelegram
                        supportDiscord = doc.getString("discord") ?: supportDiscord
                        supportWorkingHours = doc.getString("hours") ?: supportWorkingHours
                    }
                }
        } else {
            isLoading = false
        }
    }

    val pendingCount = tickets.count { it.status.equals("Pending", ignoreCase = true) }
    val filteredTickets = when (queryFilter) {
        "Pending" -> tickets.filter { it.status.equals("Pending", ignoreCase = true) }
        "Answered" -> tickets.filter { it.status.equals("Answered", ignoreCase = true) }
        else -> tickets
    }

    Scaffold(
        containerColor = Color(0xFF0D0F14),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "SUPPORT & HELP DESK",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0F14))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF141722))
                    .border(1.dp, Color(0xFF23293A), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                listOf(
                    "Tickets (${pendingCount})",
                    "Live Channels",
                    "App FAQs (${faqs.size})"
                ).forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF00E676) else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title,
                            color = if (isSelected) Color.Black else Color(0xFF8E92A4),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // USER QUERIES TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = queryFilter == "All",
                                onClick = { queryFilter = "All" },
                                label = { Text("All (${tickets.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E676),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF141722),
                                    labelColor = Color(0xFF8E92A4)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color(0xFF23293A),
                                    selectedBorderColor = Color(0xFF00E676),
                                    enabled = true,
                                    selected = queryFilter == "All"
                                )
                            )
                            FilterChip(
                                selected = queryFilter == "Pending",
                                onClick = { queryFilter = "Pending" },
                                label = { Text("Pending ($pendingCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF9800),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF141722),
                                    labelColor = Color(0xFF8E92A4)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color(0xFF23293A),
                                    selectedBorderColor = Color(0xFFFF9800),
                                    enabled = true,
                                    selected = queryFilter == "Pending"
                                )
                            )
                            FilterChip(
                                selected = queryFilter == "Answered",
                                onClick = { queryFilter = "Answered" },
                                label = { Text("Answered (${tickets.size - pendingCount})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E676),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF141722),
                                    labelColor = Color(0xFF8E92A4)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color(0xFF23293A),
                                    selectedBorderColor = Color(0xFF00E676),
                                    enabled = true,
                                    selected = queryFilter == "Answered"
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (filteredTickets.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.MarkChatRead,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "No queries found in this filter",
                                        color = Color(0xFF8E92A4),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(filteredTickets, key = { it.id }) { ticket ->
                                    AdminTicketCard(ticket = ticket, onReplySuccess = {
                                        Toast.makeText(context, "Answer sent to user", Toast.LENGTH_SHORT).show()
                                    })
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // CONTACT CHANNELS SETUP TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "LIVE SUPPORT CHANNELS",
                            color = Color(0xFF8E92A4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Text(
                            "These details are shown dynamically to all users in the support screen.",
                            color = Color(0xFFC5C9D8),
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = supportEmail,
                            onValueChange = { supportEmail = it },
                            label = { Text("Official Support Email", color = Color(0xFF8E92A4)) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF00E676)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF23293A),
                                focusedContainerColor = Color(0xFF141722),
                                unfocusedContainerColor = Color(0xFF141722)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = supportWhatsApp,
                            onValueChange = { supportWhatsApp = it },
                            label = { Text("WhatsApp Contact Number / Chat", color = Color(0xFF8E92A4)) },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF00E676)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF23293A),
                                focusedContainerColor = Color(0xFF141722),
                                unfocusedContainerColor = Color(0xFF141722)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = supportTelegram,
                            onValueChange = { supportTelegram = it },
                            label = { Text("Telegram Group / Channel Link", color = Color(0xFF8E92A4)) },
                            leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF00E676)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF23293A),
                                focusedContainerColor = Color(0xFF141722),
                                unfocusedContainerColor = Color(0xFF141722)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = supportDiscord,
                            onValueChange = { supportDiscord = it },
                            label = { Text("Discord Community Server Link", color = Color(0xFF8E92A4)) },
                            leadingIcon = { Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF00E676)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF23293A),
                                focusedContainerColor = Color(0xFF141722),
                                unfocusedContainerColor = Color(0xFF141722)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = supportWorkingHours,
                            onValueChange = { supportWorkingHours = it },
                            label = { Text("Support Working Hours (e.g. 10 AM - 11 PM)", color = Color(0xFF8E92A4)) },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF00E676)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF23293A),
                                focusedContainerColor = Color(0xFF141722),
                                unfocusedContainerColor = Color(0xFF141722)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (db != null) {
                                    isSavingSettings = true
                                    val data = mapOf(
                                        "email" to supportEmail,
                                        "whatsapp" to supportWhatsApp,
                                        "telegram" to supportTelegram,
                                        "discord" to supportDiscord,
                                        "hours" to supportWorkingHours,
                                        "updatedAt" to System.currentTimeMillis()
                                    )
                                    db.collection("settings").document("support")
                                        .set(data)
                                        .addOnSuccessListener {
                                            isSavingSettings = false
                                            Toast.makeText(context, "Support channels updated", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isSavingSettings = false
                                            Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            enabled = !isSavingSettings,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                        ) {
                            if (isSavingSettings) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SAVE LIVE CHANNELS", color = Color.Black, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // FAQ MANAGER TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "DYNAMIC APP FAQS (${faqs.size})",
                                color = Color(0xFF8E92A4),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Button(
                                onClick = { showAddFaqDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add FAQ", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (faqs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No custom FAQs added yet. Default FAQs will be shown in app.",
                                    color = Color(0xFF8E92A4),
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(faqs, key = { it.id }) { faq ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141722))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFF23293A), RoundedCornerShape(14.dp))
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Q: ${faq.question}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(onClick = {
                                                    db?.collection("faqs")?.document(faq.id)?.delete()
                                                    Toast.makeText(context, "FAQ removed", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                "A: ${faq.answer}",
                                                color = Color(0xFF8E92A4),
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add FAQ Dialog
    if (showAddFaqDialog) {
        AlertDialog(
            onDismissRequest = { showAddFaqDialog = false },
            containerColor = Color(0xFF141722),
            title = { Text("Add New FAQ", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFaqQuestion,
                        onValueChange = { newFaqQuestion = it },
                        label = { Text("Question", color = Color(0xFF8E92A4)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF23293A),
                            focusedContainerColor = Color(0xFF1A1D2B),
                            unfocusedContainerColor = Color(0xFF1A1D2B)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newFaqAnswer,
                        onValueChange = { newFaqAnswer = it },
                        label = { Text("Answer", color = Color(0xFF8E92A4)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF23293A),
                            focusedContainerColor = Color(0xFF1A1D2B),
                            unfocusedContainerColor = Color(0xFF1A1D2B)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFaqQuestion.isNotBlank() && newFaqAnswer.isNotBlank()) {
                            db?.collection("faqs")?.add(
                                mapOf(
                                    "question" to newFaqQuestion.trim(),
                                    "answer" to newFaqAnswer.trim(),
                                    "timestamp" to System.currentTimeMillis()
                                )
                            )
                            newFaqQuestion = ""
                            newFaqAnswer = ""
                            showAddFaqDialog = false
                            Toast.makeText(context, "FAQ Added", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("Add FAQ", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFaqDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E92A4))
                }
            }
        )
    }
}

@Composable
fun AdminTicketCard(
    ticket: AdminSupportTicket,
    onReplySuccess: () -> Unit
) {
    val db = remember { FirebaseHelper.getFirestore() }
    var replyText by remember { mutableStateOf(ticket.adminReply) }
    var isReplying by remember { mutableStateOf(false) }
    var showReplyBox by remember { mutableStateOf(ticket.status.equals("Pending", ignoreCase = true)) }

    val formattedDate = remember(ticket.timestamp) {
        if (ticket.timestamp > 0) {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            sdf.format(Date(ticket.timestamp))
        } else "Just now"
    }

    val isPending = ticket.status.equals("Pending", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF141722))
            .border(
                1.dp,
                if (isPending) Color(0xFFFF9800).copy(alpha = 0.5f) else Color(0xFF23293A),
                RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: User Name + Category + Status Badge
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
                            .background(Color(0xFF1A1D2B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            ticket.userName.take(1).uppercase(),
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(ticket.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(ticket.userEmail, color = Color(0xFF8E92A4), fontSize = 11.sp)
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPending) Color(0xFFFF9800).copy(alpha = 0.15f) else Color(0xFF00E676).copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (isPending) Color(0xFFFF9800) else Color(0xFF00E676),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (isPending) "PENDING" else "ANSWERED",
                        color = if (isPending) Color(0xFFFF9800) else Color(0xFF00E676),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }

            // Subject & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A1D2B))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        ticket.category.uppercase(),
                        color = Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(formattedDate, color = Color(0xFF75798E), fontSize = 11.sp)
            }

            Text(
                ticket.subject,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 15.sp
            )

            // User Question Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1D2B))
                    .border(1.dp, Color(0xFF23293A), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("USER QUERY / MESSAGE:", color = Color(0xFF75798E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ticket.message, color = Color(0xFFE2E8F0), fontSize = 13.sp, lineHeight = 18.sp)
                }
            }

            // If Answered, display the Answer card
            if (!isPending && ticket.adminReply.isNotBlank() && !showReplyBox) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C2417))
                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ADMIN ANSWER (SENT):", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text(
                                "Edit Answer",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showReplyBox = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ticket.adminReply, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }

            // Reply input box
            if (showReplyBox) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Write your reply / solution here...", color = Color(0xFF8E92A4)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF23293A),
                        focusedContainerColor = Color(0xFF1A1D2B),
                        unfocusedContainerColor = Color(0xFF1A1D2B)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isPending) {
                        TextButton(onClick = { showReplyBox = false }) {
                            Text("Cancel", color = Color(0xFF8E92A4))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            if (replyText.isNotBlank() && db != null) {
                                isReplying = true
                                val updateData = mapOf(
                                    "adminReply" to replyText.trim(),
                                    "status" to "Answered",
                                    "answeredAt" to System.currentTimeMillis()
                                )
                                db.collection("support_tickets").document(ticket.id)
                                    .update(updateData)
                                    .addOnSuccessListener {
                                        isReplying = false
                                        showReplyBox = false
                                        onReplySuccess()
                                    }
                                    .addOnFailureListener {
                                        isReplying = false
                                    }
                            }
                        },
                        enabled = !isReplying && replyText.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        if (isReplying) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SEND ANSWER", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
