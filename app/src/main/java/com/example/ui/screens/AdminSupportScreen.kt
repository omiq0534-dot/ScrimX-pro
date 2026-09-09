package com.example.ui.screens
import com.example.ui.theme.AppColors

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.FirebaseHelper
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class AdminSupportTicket(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Gamer",
    val userEmail: String = "",
    val userPhone: String = "",
    val category: String = "General",
    val subject: String = "",
    val message: String = "",
    val status: String = "Pending",
    val adminReply: String = "",
    val timestamp: Long = 0L,
    val answeredAt: Long = 0L
)

data class CustomFaqItem(
    val id: String = "",
    val question: String = "",
    val answer: String = "",
    val timestamp: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupportScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: User Queries, 1: Contact Channels, 2: FAQ Manager
    var queryFilter by remember { mutableStateOf("All") } // "All", "Pending", "Answered"

    var tickets by remember { mutableStateOf<List<AdminSupportTicket>>(emptyList()) }
    var faqs by remember { mutableStateOf<List<CustomFaqItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Settings fields
    var supportEmail by remember { mutableStateOf("omiq0534@gmail.com") }
    var supportWhatsApp by remember { mutableStateOf("+91 9876543210") }
    var supportTelegram by remember { mutableStateOf("https://t.me/scrimx_official") }
    var supportDiscord by remember { mutableStateOf("https://discord.gg/scrimx") }
    var supportWorkingHours by remember { mutableStateOf("10:00 AM - 11:00 PM IST (Daily)") }
    var isSavingSettings by remember { mutableStateOf(false) }

    // Add FAQ Dialog
    var showAddFaqDialog by remember { mutableStateOf(false) }
    var newFaqQuestion by remember { mutableStateOf("") }
    var newFaqAnswer by remember { mutableStateOf("") }

    // Listen to real-time Support Tickets
    LaunchedEffect(Unit) {
        if (db != null) {
            db.collection("support_tickets")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snap, _ ->
                    if (snap != null) {
                        tickets = snap.documents.map { doc ->
                            AdminSupportTicket(
                                id = doc.id,
                                userId = doc.getString("userId") ?: "",
                                userName = doc.getString("userName") ?: "Player",
                                userEmail = doc.getString("userEmail") ?: "No Email",
                                userPhone = doc.getString("userPhone") ?: "",
                                category = doc.getString("category") ?: "General",
                                subject = doc.getString("subject") ?: "Support Query",
                                message = doc.getString("message") ?: "",
                                status = doc.getString("status") ?: "Pending",
                                adminReply = doc.getString("adminReply") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0L,
                                answeredAt = doc.getLong("answeredAt") ?: 0L
                            )
                        }
                    }
                    isLoading = false
                }

            // Listen to support contact channels settings
            db.collection("settings").document("support").addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    doc.getString("email")?.let { supportEmail = it }
                    doc.getString("whatsapp")?.let { supportWhatsApp = it }
                    doc.getString("telegram")?.let { supportTelegram = it }
                    doc.getString("discord")?.let { supportDiscord = it }
                    doc.getString("hours")?.let { supportWorkingHours = it }
                }
            }

            // Listen to FAQs
            db.collection("faqs").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snap, _ ->
                if (snap != null) {
                    faqs = snap.documents.map { doc ->
                        CustomFaqItem(
                            id = doc.id,
                            question = doc.getString("question") ?: "",
                            answer = doc.getString("answer") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                }
            }
        } else {
            isLoading = false
        }
    }

    val pendingCount = tickets.count { it.status.equals("Pending", ignoreCase = true) }

    val filteredTickets = remember(tickets, queryFilter) {
        when (queryFilter) {
            "Pending" -> tickets.filter { it.status.equals("Pending", ignoreCase = true) }
            "Answered" -> tickets.filter { it.status.equals("Answered", ignoreCase = true) }
            else -> tickets
        }
    }

    Scaffold(
        containerColor = Color(0xFF0C0D12),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "CUSTOMER HELPDESK",
                            fontWeight = FontWeight.Black,
                            color = AppColors.TextPrimary,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (pendingCount > 0) {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF3366).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFFF3366), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                "$pendingCount Pending",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0C0D12))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector (User Queries / Contact Settings / FAQs)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF14161F),
                contentColor = Color.White,
                divider = { HorizontalDivider(color = Color(0xFF262938)) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("User Queries", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (pendingCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF3366)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$pendingCount", color = AppColors.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Channels Setup", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("FAQ Manager", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
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
                                    selectedContainerColor = Color(0xFF262938),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF14161F),
                                    labelColor = Color(0xFF8E92A4)
                                )
                            )
                            FilterChip(
                                selected = queryFilter == "Pending",
                                onClick = { queryFilter = "Pending" },
                                label = { Text("Pending ($pendingCount)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF332014),
                                    selectedLabelColor = Color(0xFFFF9800),
                                    containerColor = Color(0xFF14161F),
                                    labelColor = Color(0xFF8E92A4)
                                )
                            )
                            FilterChip(
                                selected = queryFilter == "Answered",
                                onClick = { queryFilter = "Answered" },
                                label = { Text("Answered (${tickets.size - pendingCount})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF143320),
                                    selectedLabelColor = Color(0xFF00E676),
                                    containerColor = Color(0xFF14161F),
                                    labelColor = Color(0xFF8E92A4)
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
                                        tint = Color(0xFF474C65),
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
                                        Toast.makeText(context, "Answer sent to user!", Toast.LENGTH_SHORT).show()
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
                            "These details are shown dynamically to all users in the SCRIMX Support screen.",
                            color = Color(0xFFC5C9D8),
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = supportEmail,
                            onValueChange = { supportEmail = it },
                            label = { Text("Official Support Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF3B82F6)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFF262938),
                                focusedContainerColor = Color(0xFF14161F),
                                unfocusedContainerColor = Color(0xFF14161F)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = supportWhatsApp,
                            onValueChange = { supportWhatsApp = it },
                            label = { Text("WhatsApp Contact Number / Chat") },
                            leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF25D366),
                                unfocusedBorderColor = Color(0xFF262938),
                                focusedContainerColor = Color(0xFF14161F),
                                unfocusedContainerColor = Color(0xFF14161F)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = supportTelegram,
                            onValueChange = { supportTelegram = it },
                            label = { Text("Telegram Group / Alerts Channel Link") },
                            leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF229ED9)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF229ED9),
                                unfocusedBorderColor = Color(0xFF262938),
                                focusedContainerColor = Color(0xFF14161F),
                                unfocusedContainerColor = Color(0xFF14161F)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = supportDiscord,
                            onValueChange = { supportDiscord = it },
                            label = { Text("Discord Community Server Link") },
                            leadingIcon = { Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF5865F2)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF5865F2),
                                unfocusedBorderColor = Color(0xFF262938),
                                focusedContainerColor = Color(0xFF14161F),
                                unfocusedContainerColor = Color(0xFF14161F)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = supportWorkingHours,
                            onValueChange = { supportWorkingHours = it },
                            label = { Text("Support Working Hours (e.g., 10 AM - 11 PM)") },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFFFD700)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF262938),
                                focusedContainerColor = Color(0xFF14161F),
                                unfocusedContainerColor = Color(0xFF14161F)
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
                                            Toast.makeText(context, "Support channels updated successfully!", Toast.LENGTH_SHORT).show()
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
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
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14161F))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFF262938), RoundedCornerShape(14.dp))
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
                                                    color = AppColors.TextPrimary,
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
                                                color = Color(0xFF94A3B8),
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
            containerColor = Color(0xFF14161F),
            title = { Text("Add New FAQ", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFaqQuestion,
                        onValueChange = { newFaqQuestion = it },
                        label = { Text("Question") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF333748)
                        )
                    )
                    OutlinedTextField(
                        value = newFaqAnswer,
                        onValueChange = { newFaqAnswer = it },
                        label = { Text("Answer") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF333748)
                        )
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
                            Toast.makeText(context, "FAQ Added Live!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Add FAQ", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFaqDialog = false }) {
                    Text("Cancel", color = Color.LightGray)
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
            .background(Color(0xFF14161F))
            .border(
                1.dp,
                if (isPending) Color(0xFFFF9800).copy(alpha = 0.5f) else Color(0xFF262938),
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
                            .background(Color(0xFF262938)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            ticket.userName.take(1).uppercase(),
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(ticket.userName, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        if (isPending) "⏳ PENDING" else "✓ ANSWERED",
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
                        .background(Color(0xFF222534))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        ticket.category.uppercase(),
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(formattedDate, color = Color(0xFF75798E), fontSize = 11.sp)
            }

            Text(
                ticket.subject,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary,
                fontSize = 15.sp
            )

            // User Question Body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1117))
                    .border(1.dp, Color(0xFF222532), RoundedCornerShape(12.dp))
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
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showReplyBox = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ticket.adminReply, color = AppColors.TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }

            // Reply input box
            if (showReplyBox) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Write your reply / solution here...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF333748),
                        focusedContainerColor = Color(0xFF0F1117),
                        unfocusedContainerColor = Color(0xFF0F1117)
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
                            Text("Cancel", color = Color.LightGray)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
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
