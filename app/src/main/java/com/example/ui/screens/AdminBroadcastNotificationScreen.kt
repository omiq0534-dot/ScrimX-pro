package com.example.ui.screens
import com.example.ui.theme.AppColors

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.utils.NotificationHelper
import com.google.firebase.firestore.Query

data class BroadcastLog(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "GLOBAL", // GLOBAL or ROOM_ONLY
    val targetMode: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBroadcastNotificationScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }

    var broadcastMode by remember { mutableStateOf(0) } // 0 = Global All Users, 1 = Room Joined Users Only
    var isSending by remember { mutableStateOf(false) }

    // Global Notification State
    var globalTitle by remember { mutableStateOf("") }
    var globalMessage by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Announcement") }

    // Room Joined Users State
    var matchesList by remember { mutableStateOf<List<MatchData>>(emptyList()) }
    var selectedMatch by remember { mutableStateOf<MatchData?>(null) }
    var roomIdInput by remember { mutableStateOf("") }
    var roomPassInput by remember { mutableStateOf("") }
    var customRoomInstructions by remember { mutableStateOf("") }

    // Broadcast Logs State
    var logsList by remember { mutableStateOf<List<BroadcastLog>>(emptyList()) }

    // Load matches and broadcast history
    LaunchedEffect(Unit) {
        if (db != null) {
            // Load Matches
            db.collection("matches").addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val list = snap.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(MatchData::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    matchesList = list
                    if (selectedMatch == null && list.isNotEmpty()) {
                        selectedMatch = list.first()
                        roomIdInput = list.first().roomId
                        roomPassInput = list.first().roomPass
                    }
                }
            }

            // Load Notification Broadcast Logs
            db.collection("broadcast_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snap, _ ->
                    if (snap != null) {
                        logsList = snap.documents.map { doc ->
                            BroadcastLog(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                type = doc.getString("type") ?: "GLOBAL",
                                targetMode = doc.getString("targetMode") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            )
                        }
                    }
                }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0C0D14),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "PUSH NOTIFICATION HUB",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF0055).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ADMIN LIVE", color = Color(0xFFFF0055), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF10121A))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Pill
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF151824),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.3f), Color.Transparent))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Instant Push & In-App Broadcast", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Sends high-priority notification to phones & writes to live alerts", color = Color(0xFF8E92A4), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Tab Selector: Global vs Room Joined Users Only
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF131520))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Global Broadcast Tab
                    Button(
                        onClick = { broadcastMode = 0 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (broadcastMode == 0) Color(0xFFFFD700) else Color.Transparent,
                            contentColor = if (broadcastMode == 0) Color.Black else Color(0xFF8E92A4)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Global All Users", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }

                    // Room Users Only Tab
                    Button(
                        onClick = { broadcastMode = 1 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (broadcastMode == 1) Color(0xFFFF0055) else Color.Transparent,
                            contentColor = if (broadcastMode == 1) Color.White else Color(0xFF8E92A4)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Room Users Only", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }

            // 1. GLOBAL NOTIFICATION CARD
            if (broadcastMode == 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141622)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.verticalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.5f), Color.Transparent))
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("GLOBAL ANNOUNCEMENT", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    Text("Delivered to every player with system push notification", color = Color(0xFF8E92A4), fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Category selector
                            Text("CATEGORY", color = Color(0xFF8E92A4), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Announcement", "Tournament", "Cash Prize", "System").forEach { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFFFD700),
                                            selectedLabelColor = Color.Black,
                                            containerColor = Color(0xFF1D2130),
                                            labelColor = Color(0xFFC0C4D6)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Title Field
                            OutlinedTextField(
                                value = globalTitle,
                                onValueChange = { globalTitle = it },
                                label = { Text("Title / Headline", color = Color(0xFF8E92A4)) },
                                placeholder = { Text("e.g. ⚡ ₹10,000 Grand BGMI Finale Tonight!", color = Color(0xFF475569)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF2C3044)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Message Field
                            OutlinedTextField(
                                value = globalMessage,
                                onValueChange = { globalMessage = it },
                                label = { Text("Message Body", color = Color(0xFF8E92A4)) },
                                placeholder = { Text("Enter detailed broadcast message for all players...", color = Color(0xFF475569)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF2C3044)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Dispatch Button
                            Button(
                                onClick = {
                                    if (globalMessage.isNotBlank()) {
                                        isSending = true
                                        val titleToSend = globalTitle.ifBlank { "📢 ScrimX Global Announcement" }
                                        
                                        // 1. Trigger local system push on admin's phone immediately
                                        NotificationHelper.showGeneralAnnouncementNotification(
                                            context = context,
                                            title = titleToSend,
                                            message = globalMessage
                                        )

                                        // 2. Write to Firestore for all users
                                        val logData = hashMapOf(
                                            "title" to titleToSend,
                                            "message" to globalMessage,
                                            "type" to "GLOBAL",
                                            "category" to selectedCategory,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        db?.collection("broadcast_logs")?.add(logData)
                                        db?.collection("global_notifications")?.add(logData)

                                        Toast.makeText(context, "✅ Global Broadcast Sent + Phone Push Triggered!", Toast.LENGTH_LONG).show()
                                        globalTitle = ""
                                        globalMessage = ""
                                        isSending = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isSending && globalMessage.isNotBlank()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SEND GLOBAL BROADCAST + PUSH ALERT", color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // 2. ROOM JOINED USERS ONLY CARD
            if (broadcastMode == 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141622)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.verticalGradient(listOf(Color(0xFFFF0055).copy(alpha = 0.5f), Color.Transparent))
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF0055).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFFF0055), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("ROOM JOINED USERS ONLY", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    Text("Send Room ID, Password & Alerts only to registered players", color = Color(0xFF8E92A4), fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tournament Selector
                            Text("SELECT TOURNAMENT MATCH", color = Color(0xFF8E92A4), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (matchesList.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E2130),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("No active tournament matches found. Please create a match first.", color = Color(0xFFFF5252), fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                                }
                            } else {
                                matchesList.take(4).forEach { match ->
                                    val isSelected = selectedMatch?.id == match.id
                                    val bookedCount = match.bookedSlots.size
                                    Surface(
                                        onClick = {
                                            selectedMatch = match
                                            roomIdInput = match.roomId
                                            roomPassInput = match.roomPass
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(0xFFFF0055).copy(alpha = 0.15f) else Color(0xFF1B1E2B),
                                        border = CardDefaults.outlinedCardBorder().copy(
                                            brush = Brush.linearGradient(
                                                if (isSelected) listOf(Color(0xFFFF0055), Color(0xFFFFD700))
                                                else listOf(Color(0xFF2C3044), Color.Transparent)
                                            )
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedMatch = match
                                                    roomIdInput = match.roomId
                                                    roomPassInput = match.roomPass
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF0055))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(match.title, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(
                                                    "${match.mode} (${match.map}) • $bookedCount/${match.totalSlots} Slots Booked",
                                                    color = Color(0xFF00E676),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Room ID & Pass input fields
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = roomIdInput,
                                    onValueChange = { roomIdInput = it },
                                    label = { Text("Room ID", color = Color(0xFF8E92A4)) },
                                    placeholder = { Text("e.g. 849201", color = Color(0xFF475569)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFF0055),
                                        unfocusedBorderColor = Color(0xFF2C3044)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = roomPassInput,
                                    onValueChange = { roomPassInput = it },
                                    label = { Text("Password", color = Color(0xFF8E92A4)) },
                                    placeholder = { Text("e.g. 778", color = Color(0xFF475569)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFFF0055),
                                        unfocusedBorderColor = Color(0xFF2C3044)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Instructions Field
                            OutlinedTextField(
                                value = customRoomInstructions,
                                onValueChange = { customRoomInstructions = it },
                                label = { Text("Slot/Starting Alert", color = Color(0xFF8E92A4)) },
                                placeholder = { Text("e.g. Join your allocated slot immediately! Match starts in 10 mins.", color = Color(0xFF475569)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFF0055),
                                    unfocusedBorderColor = Color(0xFF2C3044)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Send Room Button
                            Button(
                                onClick = {
                                    val target = selectedMatch
                                    if (target != null) {
                                        isSending = true

                                        // 1. Update Match Room in Firestore
                                        db?.collection("matches")?.document(target.id)?.update(
                                            mapOf(
                                                "roomId" to roomIdInput,
                                                "roomPass" to roomPassInput,
                                                "status" to "Live"
                                            )
                                        )

                                        // 2. Trigger System Notification on Admin's device to test
                                        NotificationHelper.showRoomCredentialsNotification(
                                            context = context,
                                            matchId = target.id,
                                            matchTitle = target.title,
                                            roomId = roomIdInput.ifBlank { "884920" },
                                            roomPass = roomPassInput.ifBlank { "778" },
                                            gameMode = target.mode
                                        )

                                        // 3. Save to broadcast log
                                        val logData = hashMapOf(
                                            "title" to "🔑 ${target.title} - Room Credentials",
                                            "message" to "Room ID: $roomIdInput | Password: $roomPassInput\n${customRoomInstructions.ifBlank { "Join your slot now!" }}",
                                            "type" to "ROOM_ONLY",
                                            "targetMode" to target.mode,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        db?.collection("broadcast_logs")?.add(logData)

                                        Toast.makeText(context, "✅ Room Credentials Dispatched to Joined Players + Push Alert Triggered!", Toast.LENGTH_LONG).show()
                                        isSending = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isSending && (selectedMatch != null || matchesList.isNotEmpty())
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SEND TO ROOM JOINED PLAYERS ONLY", color = AppColors.TextPrimary, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // 3. SENT BROADCAST LOGS
            item {
                Text("RECENT BROADCAST LOGS", color = Color(0xFF8E92A4), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }

            if (logsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141622)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "No broadcasts sent yet. Use the controls above to send your first alert!",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(logsList) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141622)),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF2C3044), Color.Transparent))
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(log.title, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (log.type == "ROOM_ONLY") Color(0xFFFF0055).copy(alpha = 0.2f) else Color(0xFFFFD700).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        if (log.type == "ROOM_ONLY") "ROOM ONLY" else "GLOBAL",
                                        color = if (log.type == "ROOM_ONLY") Color(0xFFFF0055) else Color(0xFFFFD700),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.message, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
