package com.example.ui.screens
import com.example.ui.theme.AppColors

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

    var broadcastMode by remember { mutableIntStateOf(0) } // 0 = Global All Users, 1 = Room Joined Users Only
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

    LaunchedEffect(Unit) {
        if (db != null) {
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
                            "PUSH NOTIFICATIONS",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White,
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A1D2B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("PUSH & IN-APP BROADCAST", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            Text("Sends high-priority notifications to registered phones", color = Color(0xFF8E92A4), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Tab Selector: Global vs Room Joined Users Only
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Global (All Users)", "Room Players Only").forEachIndexed { index, title ->
                        val isSelected = broadcastMode == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF00E676) else Color.Transparent)
                                .clickable { broadcastMode = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                title,
                                color = if (isSelected) Color.Black else Color(0xFF8E92A4),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 1. GLOBAL NOTIFICATION CARD
            if (broadcastMode == 0) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF141722))
                            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(18.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1A1D2B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("GLOBAL ANNOUNCEMENT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    Text("Delivered to every player with system push notification", color = Color(0xFF8E92A4), fontSize = 11.sp)
                                }
                            }

                            // Category selector
                            Text("CATEGORY", color = Color(0xFF8E92A4), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Announcement", "Tournament", "Cash Prize", "System").forEach { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF00E676),
                                            selectedLabelColor = Color.Black,
                                            containerColor = Color(0xFF1A1D2B),
                                            labelColor = Color(0xFF8E92A4)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = Color(0xFF23293A),
                                            selectedBorderColor = Color(0xFF00E676),
                                            enabled = true,
                                            selected = selectedCategory == cat
                                        )
                                    )
                                }
                            }

                            // Title Field
                            OutlinedTextField(
                                value = globalTitle,
                                onValueChange = { globalTitle = it },
                                label = { Text("Title / Headline", color = Color(0xFF8E92A4)) },
                                placeholder = { Text("e.g. Grand BGMI Finale Tonight", color = Color(0xFF75798E)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E676),
                                    unfocusedBorderColor = Color(0xFF23293A),
                                    focusedContainerColor = Color(0xFF1A1D2B),
                                    unfocusedContainerColor = Color(0xFF1A1D2B)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Message Field
                            OutlinedTextField(
                                value = globalMessage,
                                onValueChange = { globalMessage = it },
                                label = { Text("Message Body", color = Color(0xFF8E92A4)) },
                                placeholder = { Text("Enter broadcast message for all players...", color = Color(0xFF75798E)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E676),
                                    unfocusedBorderColor = Color(0xFF23293A),
                                    focusedContainerColor = Color(0xFF1A1D2B),
                                    unfocusedContainerColor = Color(0xFF1A1D2B)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 4
                            )

                            // Dispatch Button
                            Button(
                                onClick = {
                                    if (globalMessage.isNotBlank()) {
                                        isSending = true
                                        val titleToSend = globalTitle.ifBlank { "ScrimX Announcement" }

                                        NotificationHelper.showGeneralAnnouncementNotification(
                                            context = context,
                                            title = titleToSend,
                                            message = globalMessage
                                        )

                                        val logData = hashMapOf(
                                            "title" to titleToSend,
                                            "message" to globalMessage,
                                            "type" to "GLOBAL",
                                            "category" to selectedCategory,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        db?.collection("broadcast_logs")?.add(logData)
                                        db?.collection("global_notifications")?.add(logData)

                                        Toast.makeText(context, "Global Broadcast Sent", Toast.LENGTH_SHORT).show()
                                        globalTitle = ""
                                        globalMessage = ""
                                        isSending = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isSending && globalMessage.isNotBlank()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SEND GLOBAL BROADCAST", color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // 2. ROOM JOINED USERS ONLY CARD
            if (broadcastMode == 1) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF141722))
                            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(18.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1A1D2B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("ROOM PLAYERS ONLY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                    Text("Send Room ID and Password to registered players", color = Color(0xFF8E92A4), fontSize = 11.sp)
                                }
                            }

                            // Tournament Selector
                            Text("SELECT TOURNAMENT MATCH", color = Color(0xFF8E92A4), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)

                            if (matchesList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1A1D2B))
                                        .padding(12.dp)
                                ) {
                                    Text("No active tournament matches found.", color = Color(0xFFFF5252), fontSize = 12.sp)
                                }
                            } else {
                                matchesList.take(4).forEach { match ->
                                    val isSelected = selectedMatch?.id == match.id
                                    val bookedCount = match.bookedSlots.size
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0xFF0D2517) else Color(0xFF1A1D2B))
                                            .border(
                                                1.dp,
                                                if (isSelected) Color(0xFF00E676) else Color(0xFF23293A),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                selectedMatch = match
                                                roomIdInput = match.roomId
                                                roomPassInput = match.roomPass
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = {
                                                    selectedMatch = match
                                                    roomIdInput = match.roomId
                                                    roomPassInput = match.roomPass
                                                },
                                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00E676))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(match.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(
                                                    "${match.mode} (${match.map}) • $bookedCount/${match.totalSlots} Booked",
                                                    color = Color(0xFF00E676),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Room ID & Pass input fields
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = roomIdInput,
                                    onValueChange = { roomIdInput = it },
                                    label = { Text("Room ID", color = Color(0xFF8E92A4)) },
                                    placeholder = { Text("e.g. 849201", color = Color(0xFF75798E)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00E676),
                                        unfocusedBorderColor = Color(0xFF23293A),
                                        focusedContainerColor = Color(0xFF1A1D2B),
                                        unfocusedContainerColor = Color(0xFF1A1D2B)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = roomPassInput,
                                    onValueChange = { roomPassInput = it },
                                    label = { Text("Password", color = Color(0xFF8E92A4)) },
                                    placeholder = { Text("e.g. 778", color = Color(0xFF75798E)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00E676),
                                        unfocusedBorderColor = Color(0xFF23293A),
                                        focusedContainerColor = Color(0xFF1A1D2B),
                                        unfocusedContainerColor = Color(0xFF1A1D2B)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            // Instructions Field
                            OutlinedTextField(
                                value = customRoomInstructions,
                                onValueChange = { customRoomInstructions = it },
                                label = { Text("Instructions", color = Color(0xFF8E92A4)) },
                                placeholder = { Text("e.g. Join your slot now. Match starts in 10 mins.", color = Color(0xFF75798E)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E676),
                                    unfocusedBorderColor = Color(0xFF23293A),
                                    focusedContainerColor = Color(0xFF1A1D2B),
                                    unfocusedContainerColor = Color(0xFF1A1D2B)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Send Room Button
                            Button(
                                onClick = {
                                    val target = selectedMatch
                                    if (target != null) {
                                        isSending = true

                                        db?.collection("matches")?.document(target.id)?.update(
                                            mapOf(
                                                "roomId" to roomIdInput,
                                                "roomPass" to roomPassInput,
                                                "status" to "Live"
                                            )
                                        )

                                        NotificationHelper.showRoomCredentialsNotification(
                                            context = context,
                                            matchId = target.id,
                                            matchTitle = target.title,
                                            roomId = roomIdInput.ifBlank { "884920" },
                                            roomPass = roomPassInput.ifBlank { "778" },
                                            gameMode = target.mode
                                        )

                                        val logData = hashMapOf(
                                            "title" to "${target.title} - Room Credentials",
                                            "message" to "Room ID: $roomIdInput | Password: $roomPassInput\n${customRoomInstructions.ifBlank { "Join your slot now" }}",
                                            "type" to "ROOM_ONLY",
                                            "targetMode" to target.mode,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        db?.collection("broadcast_logs")?.add(logData)

                                        Toast.makeText(context, "Room Credentials Dispatched", Toast.LENGTH_SHORT).show()
                                        isSending = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isSending && (selectedMatch != null || matchesList.isNotEmpty())
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SEND TO ROOM PLAYERS", color = Color.Black, fontWeight = FontWeight.Black)
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF141722))
                            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            "No broadcasts sent yet.",
                            color = Color(0xFF8E92A4),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(logsList) { log ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF141722))
                            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(log.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (log.type == "ROOM_ONLY") Color(0xFF0D2517) else Color(0xFF1A1D2B))
                                        .border(1.dp, if (log.type == "ROOM_ONLY") Color(0xFF00E676) else Color(0xFF23293A), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        if (log.type == "ROOM_ONLY") "ROOM ONLY" else "GLOBAL",
                                        color = if (log.type == "ROOM_ONLY") Color(0xFF00E676) else Color(0xFF8E92A4),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(log.message, color = Color(0xFF8E92A4), fontSize = 12.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
