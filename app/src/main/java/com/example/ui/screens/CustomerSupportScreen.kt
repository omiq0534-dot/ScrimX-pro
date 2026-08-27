package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.FirebaseHelper
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class UserSupportTicket(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val category: String = "General",
    val subject: String = "",
    val message: String = "",
    val status: String = "Pending",
    val adminReply: String = "",
    val timestamp: Long = 0L,
    val answeredAt: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSupportScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }
    val auth = remember { FirebaseHelper.getAuth() }
    val currentUser = auth?.currentUser

    var activeTab by remember { mutableIntStateOf(0) } // 0: Live Channels & FAQ, 1: Ask Question / My Tickets

    // Dynamic support channels
    var supportEmail by remember { mutableStateOf("omiq0534@gmail.com") }
    var supportWhatsApp by remember { mutableStateOf("+91 9876543210") }
    var supportTelegram by remember { mutableStateOf("https://t.me/scrimx_official") }
    var supportDiscord by remember { mutableStateOf("https://discord.gg/scrimx") }
    var supportWorkingHours by remember { mutableStateOf("10:00 AM - 11:00 PM IST (Daily)") }

    // Dynamic FAQs
    var customFaqs by remember { mutableStateOf<List<CustomFaqItem>>(emptyList()) }

    // Ask Query Form state
    var selectedCategory by remember { mutableStateOf("Match & Room ID") }
    var querySubject by remember { mutableStateOf("") }
    var queryMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // My Tickets
    var myTickets by remember { mutableStateOf<List<UserSupportTicket>>(emptyList()) }

    val categories = listOf("Match & Room ID", "Deposit & Withdrawal", "Account & Login", "Cheat Report", "Other")

    LaunchedEffect(Unit) {
        if (db != null) {
            // Load dynamic support channels
            db.collection("settings").document("support").addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    doc.getString("email")?.let { if (it.isNotBlank()) supportEmail = it }
                    doc.getString("whatsapp")?.let { if (it.isNotBlank()) supportWhatsApp = it }
                    doc.getString("telegram")?.let { if (it.isNotBlank()) supportTelegram = it }
                    doc.getString("discord")?.let { if (it.isNotBlank()) supportDiscord = it }
                    doc.getString("hours")?.let { if (it.isNotBlank()) supportWorkingHours = it }
                }
            }

            // Load dynamic FAQs
            db.collection("faqs").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snap, _ ->
                if (snap != null) {
                    customFaqs = snap.documents.map { doc ->
                        CustomFaqItem(
                            id = doc.id,
                            question = doc.getString("question") ?: "",
                            answer = doc.getString("answer") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }
                }
            }

            // Load user's tickets
            if (currentUser != null) {
                db.collection("support_tickets")
                    .whereEqualTo("userId", currentUser.uid)
                    .addSnapshotListener { snap, _ ->
                        if (snap != null) {
                            myTickets = snap.documents.map { doc ->
                                UserSupportTicket(
                                    id = doc.id,
                                    userId = doc.getString("userId") ?: "",
                                    userName = doc.getString("userName") ?: "",
                                    userEmail = doc.getString("userEmail") ?: "",
                                    category = doc.getString("category") ?: "General",
                                    subject = doc.getString("subject") ?: "",
                                    message = doc.getString("message") ?: "",
                                    status = doc.getString("status") ?: "Pending",
                                    adminReply = doc.getString("adminReply") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: 0L,
                                    answeredAt = doc.getLong("answeredAt") ?: 0L
                                )
                            }.sortedByDescending { it.timestamp }
                        }
                    }
            }
        }
    }

    val myPendingCount = myTickets.count { it.status.equals("Pending", ignoreCase = true) }
    val myAnsweredCount = myTickets.count { it.status.equals("Answered", ignoreCase = true) }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Help & Support",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAFAFA)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs: Contact & FAQs vs Ask Question / My Tickets
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = Color.Black,
                divider = { HorizontalDivider(color = Color(0xFFE2E8F0)) }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Contact & FAQs", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ask Query / My Tickets", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (myTickets.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (myAnsweredCount > 0) Color(0xFF00E676) else Color(0xFFFF9800))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "${myTickets.size}",
                                        color = if (myAnsweredCount > 0) Color.Black else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                )
            }

            when (activeTab) {
                0 -> {
                    // TAB 1: Live Channels & FAQs
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Hero Support Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                    )
                                )
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(22.dp))
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFFD700).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Headphones,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "SCRIMX 24/7 HELPDESK",
                                            color = Color(0xFFFFD700),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            "How can we help you?",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 17.sp
                                        )
                                    }
                                }

                                Text(
                                    "Need instant help with Match Room details, Coin deposit/withdrawal, or rules? Tap below or raise a ticket!",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF00E676).copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00E676))
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Text(
                                        "Working Hours: $supportWorkingHours",
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Quick Button to Ask Question
                        Button(
                            onClick = { activeTab = 1 },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFFFFD700))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ASK A QUESTION / RAISE TICKET", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
                        }

                        Text(
                            "OFFICIAL CONTACT CHANNELS",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        // 1. Email Support
                        SupportActionCard(
                            icon = Icons.Default.Email,
                            iconBg = Color(0xFF3B82F6),
                            title = "Official Support Email",
                            value = supportEmail,
                            actionLabel = "Send Mail",
                            onAction = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$supportEmail")
                                        putExtra(Intent.EXTRA_SUBJECT, "SCRIMX Support Request")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Support Email", supportEmail))
                                    Toast.makeText(context, "Email copied: $supportEmail", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 2. WhatsApp Support
                        SupportActionCard(
                            icon = Icons.Default.Chat,
                            iconBg = Color(0xFF25D366),
                            title = "WhatsApp Direct Chat",
                            value = supportWhatsApp,
                            actionLabel = "Chat Now",
                            onAction = {
                                val cleanPhone = supportWhatsApp.replace("[^0-9]".toRegex(), "")
                                val url = if (cleanPhone.isNotBlank()) "https://wa.me/$cleanPhone" else "https://wa.me/"
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("WhatsApp Number", supportWhatsApp))
                                    Toast.makeText(context, "WhatsApp copied: $supportWhatsApp", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 3. Telegram Channel & Group
                        SupportActionCard(
                            icon = Icons.Default.Send,
                            iconBg = Color(0xFF229ED9),
                            title = "Telegram Official Channel",
                            value = "Join for Room ID & Match Alerts",
                            actionLabel = "Join Group",
                            onAction = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(supportTelegram)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // 4. Discord Community
                        SupportActionCard(
                            icon = Icons.Default.SportsEsports,
                            iconBg = Color(0xFF5865F2),
                            title = "Discord Community Server",
                            value = "Voice channels, scrim finding & chats",
                            actionLabel = "Join Discord",
                            onAction = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(supportDiscord)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // FAQs Section
                        Text(
                            "FREQUENTLY ASKED QUESTIONS",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        if (customFaqs.isNotEmpty()) {
                            customFaqs.forEach { faq ->
                                FaqCard(question = faq.question, answer = faq.answer)
                            }
                        } else {
                            FaqCard(
                                question = "When will I get my Room ID & Password?",
                                answer = "Room ID and Password are automatically updated on the Match Details screen 15 minutes before the match start time."
                            )
                            FaqCard(
                                question = "How long do Coin Withdrawals take?",
                                answer = "Instant to 24 hours. The admin verifies the payment proof and credits UPI/Bank account within the same day."
                            )
                            FaqCard(
                                question = "Are emulators or hacks allowed?",
                                answer = "No! Strict anti-cheat policy. Anyone using emulators, config files, or cheats is permanently banned without refunds."
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                1 -> {
                    // TAB 2: Ask Question / My Tickets
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Form Card: Ask a Question
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "SUBMIT SUPPORT QUERY",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )

                                Text(
                                    "Our admin team responds directly to your query right here in the app.",
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp
                                )

                                // Category chips
                                Text("Select Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categories.take(3).forEach { cat ->
                                        FilterChip(
                                            selected = selectedCategory == cat,
                                            onClick = { selectedCategory = cat },
                                            label = { Text(cat, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF0F172A),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categories.drop(3).forEach { cat ->
                                        FilterChip(
                                            selected = selectedCategory == cat,
                                            onClick = { selectedCategory = cat },
                                            label = { Text(cat, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF0F172A),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = querySubject,
                                    onValueChange = { querySubject = it },
                                    label = { Text("Subject (e.g., Room ID not received / Coin deduction)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0F172A),
                                        unfocusedBorderColor = Color(0xFFCBD5E1)
                                    )
                                )

                                OutlinedTextField(
                                    value = queryMessage,
                                    onValueChange = { queryMessage = it },
                                    label = { Text("Detailed Question / Problem Description") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF0F172A),
                                        unfocusedBorderColor = Color(0xFFCBD5E1)
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (querySubject.isNotBlank() && queryMessage.isNotBlank()) {
                                            if (db != null && currentUser != null) {
                                                isSubmitting = true
                                                val ticketData = mapOf(
                                                    "userId" to currentUser.uid,
                                                    "userName" to (currentUser.displayName ?: "Player"),
                                                    "userEmail" to (currentUser.email ?: "No Email"),
                                                    "category" to selectedCategory,
                                                    "subject" to querySubject.trim(),
                                                    "message" to queryMessage.trim(),
                                                    "status" to "Pending",
                                                    "adminReply" to "",
                                                    "timestamp" to System.currentTimeMillis(),
                                                    "answeredAt" to 0L
                                                )

                                                db.collection("support_tickets").add(ticketData)
                                                    .addOnSuccessListener {
                                                        isSubmitting = false
                                                        querySubject = ""
                                                        queryMessage = ""
                                                        Toast.makeText(context, "Query submitted! Admin will answer shortly.", Toast.LENGTH_LONG).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        isSubmitting = false
                                                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                            } else {
                                                Toast.makeText(context, "Please login first to submit a query", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Please fill subject and message", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = !isSubmitting && querySubject.isNotBlank() && queryMessage.isNotBlank(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("SUBMIT QUERY TO ADMIN", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // My Queries / Tickets History
                        Text(
                            "MY QUERIES & ADMIN ANSWERS (${myTickets.size})",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        if (myTickets.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFCBD5E1),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("You haven't asked any questions yet.", color = Color(0xFF64748B), fontSize = 13.sp)
                                }
                            }
                        } else {
                            myTickets.forEach { ticket ->
                                UserTicketCard(ticket = ticket)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun UserTicketCard(ticket: UserSupportTicket) {
    val isPending = ticket.status.equals("Pending", ignoreCase = true)

    val formattedDate = remember(ticket.timestamp) {
        if (ticket.timestamp > 0) {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            sdf.format(Date(ticket.timestamp))
        } else "Just now"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isPending) Color(0xFFFFE082) else Color(0xFF00E676).copy(alpha = 0.5f),
                    RoundedCornerShape(18.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        ticket.category.uppercase(),
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPending) Color(0xFFFFF3E0) else Color(0xFFE8F5E9))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (isPending) "⏳ PENDING" else "✓ ANSWERED",
                        color = if (isPending) Color(0xFFE65100) else Color(0xFF2E7D32),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                ticket.subject,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color.Black
            )

            Text(
                ticket.message,
                color = Color(0xFF475569),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Text(
                "Asked on: $formattedDate",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            // If Answered, render highlight answer box
            if (!isPending && ticket.adminReply.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "ADMIN OFFICIAL ANSWER:",
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            ticket.adminReply,
                            color = Color(0xFF14532D),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupportActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    title: String,
    value: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconBg, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Text(value, color = Color(0xFF64748B), fontSize = 12.sp)
            }
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(actionLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun FaqCard(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.DarkGray
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    answer,
                    color = Color(0xFF475569),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
