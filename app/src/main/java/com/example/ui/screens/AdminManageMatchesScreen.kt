package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.FirebaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageMatchesScreen(
    navController: NavController,
    matchesViewModel: MatchesViewModel = viewModel()
) {
    val matches by matchesViewModel.matches.collectAsState()
    val db = remember { FirebaseHelper.getFirestore() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val bgColor = Color(0xFF0D0F14)
    val cardBg = Color(0xFF141722)
    val cardBorder = Color(0xFF23293A)
    val neonGreen = Color(0xFF00E676)
    val surfaceSubtle = Color(0xFF1E2330)

    // Edit Room & Match Info Dialog
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMatchForEdit by remember { mutableStateOf<MatchData?>(null) }
    var editMap by remember { mutableStateOf("") }
    var editPrize by remember { mutableStateOf("") }
    var editEntryType by remember { mutableStateOf("PAID") }
    var editPaidEntryFee by remember { mutableStateOf("10") }
    var editRequiredAds by remember { mutableStateOf("1") }
    var roomId by remember { mutableStateOf("") }
    var roomPass by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var liveUrl by remember { mutableStateOf("") }
    var editRules by remember { mutableStateOf("") }

    // View Bookings Dialog
    var showBookingsDialog by remember { mutableStateOf(false) }
    var selectedMatchForBookings by remember { mutableStateOf<MatchData?>(null) }

    // 1. Edit Dialog
    if (showEditDialog && selectedMatchForEdit != null) {
        AlertDialog(
            containerColor = cardBg,
            onDismissRequest = { showEditDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "EDIT MATCH & ROOM",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        selectedMatchForEdit!!.title,
                        color = neonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    ClassyDarkInput(
                        value = editMap,
                        onValueChange = { editMap = it },
                        label = "Map Name",
                        placeholder = "Bermuda / Kalahari / Erangel"
                    )

                    ClassyDarkInput(
                        value = editPrize,
                        onValueChange = { editPrize = it },
                        label = "Prize Pool",
                        placeholder = "e.g. 500 Coins"
                    )

                    Text(
                        "ENTRY TYPE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "FREE" to "Free",
                            "AD" to "Watch Ads",
                            "PAID" to "Paid Coins"
                        ).forEach { (tKey, tLabel) ->
                            val isSel = editEntryType == tKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) neonGreen else surfaceSubtle)
                                    .border(1.dp, if (isSel) neonGreen else cardBorder, RoundedCornerShape(8.dp))
                                    .clickable { editEntryType = tKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    tLabel,
                                    color = if (isSel) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (editEntryType == "AD") {
                        ClassyDarkInput(
                            value = editRequiredAds,
                            onValueChange = { editRequiredAds = it },
                            label = "Required Ads Count",
                            placeholder = "1, 2, 3..."
                        )
                    } else if (editEntryType == "PAID") {
                        ClassyDarkInput(
                            value = editPaidEntryFee,
                            onValueChange = { editPaidEntryFee = it },
                            label = "Entry Fee",
                            placeholder = "10, 20, 50..."
                        )
                    }

                    ClassyDarkInput(
                        value = roomId,
                        onValueChange = { roomId = it },
                        label = "Room ID",
                        placeholder = "e.g. 5892144"
                    )

                    ClassyDarkInput(
                        value = roomPass,
                        onValueChange = { roomPass = it },
                        label = "Room Password",
                        placeholder = "e.g. 1234"
                    )

                    ClassyDarkInput(
                        value = liveUrl,
                        onValueChange = { liveUrl = it },
                        label = "Live Stream URL",
                        placeholder = "https://youtube.com/live/..."
                    )

                    OutlinedTextField(
                        value = editRules,
                        onValueChange = { editRules = it },
                        label = { Text("Match Rules", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("e.g. Desert Eagle Only, No Grenades", color = Color(0xFF64748B)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonGreen,
                            unfocusedBorderColor = cardBorder,
                            focusedContainerColor = surfaceSubtle,
                            unfocusedContainerColor = surfaceSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = neonGreen
                        ),
                        minLines = 2,
                        maxLines = 4
                    )

                    Text(
                        "STATUS",
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Upcoming", "Live", "Completed").forEach { st ->
                            val isSelected = status == st
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) neonGreen else surfaceSubtle)
                                    .border(1.dp, if (isSelected) neonGreen else cardBorder, RoundedCornerShape(8.dp))
                                    .clickable { status = st }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    st,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val computedEntry = when (editEntryType) {
                            "FREE" -> "FREE"
                            "AD" -> {
                                val adsCount = editRequiredAds.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                "Free ($adsCount Ads)"
                            }
                            else -> "₹${editPaidEntryFee.trim().removePrefix("₹").ifBlank { "10" }}"
                        }
                        val computedRequiredAds = if (editEntryType == "AD") (editRequiredAds.toIntOrNull()?.coerceAtLeast(1) ?: 1) else 0

                        scope.launch {
                            db?.collection("matches")?.document(selectedMatchForEdit!!.id)
                                ?.update(
                                    "map", editMap.trim().ifBlank { "Bermuda" },
                                    "prize", editPrize.trim().ifBlank { "₹500" },
                                    "entry", computedEntry,
                                    "entryType", editEntryType,
                                    "requiredAds", computedRequiredAds,
                                    "roomId", roomId.trim(),
                                    "roomPass", roomPass.trim(),
                                    "status", status,
                                    "liveUrl", liveUrl.trim(),
                                    "rules", editRules.trim()
                                )
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // 2. View Bookings & Player List Dialog
    if (showBookingsDialog && selectedMatchForBookings != null) {
        val targetMatch = selectedMatchForBookings!!
        var registeredPlayers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
        var isFetchingPlayers by remember { mutableStateOf(true) }

        LaunchedEffect(targetMatch.id) {
            isFetchingPlayers = true
            try {
                db?.collection("bookings")
                    ?.whereEqualTo("matchId", targetMatch.id)
                    ?.get()
                    ?.addOnSuccessListener { snapshot ->
                        registeredPlayers = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                        isFetchingPlayers = false
                    }
                    ?.addOnFailureListener {
                        registeredPlayers = emptyList()
                        isFetchingPlayers = false
                    }
            } catch (e: Exception) {
                registeredPlayers = emptyList()
                isFetchingPlayers = false
            }
        }

        AlertDialog(
            containerColor = cardBg,
            onDismissRequest = { showBookingsDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Column {
                    Text(
                        "REGISTERED PLAYERS",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${targetMatch.title} • ${targetMatch.bookedSlots.size} Slots Booked",
                        color = neonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                    if (isFetchingPlayers) {
                        CircularProgressIndicator(
                            color = neonGreen,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (registeredPlayers.isEmpty() && targetMatch.bookedSlots.isEmpty()) {
                        Text(
                            "No players have registered for this match yet.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (registeredPlayers.isNotEmpty()) {
                                items(registeredPlayers) { reg ->
                                    val inGameUid = reg["inGameUid"]?.toString() ?: reg["inGameName"]?.toString() ?: "N/A"
                                    val registeredName = reg["userName"]?.toString() ?: reg["inGameName"]?.toString() ?: "Player"
                                    val slotNum = reg["slotNumber"]?.toString() ?: "?"
                                    val accountEmail = reg["userEmail"]?.toString() ?: "No email"

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(surfaceSubtle)
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(neonGreen.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                slotNum,
                                                color = neonGreen,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                registeredName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            if (inGameUid.isNotBlank() && inGameUid != "N/A") {
                                                Text("UID: $inGameUid", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                            }
                                            Text("Email: $accountEmail", color = Color(0xFF64748B), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBookingsDialog = false }) {
                    Text("Close", color = neonGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(neonGreen)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "MANAGE MATCHES & ROOMS",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            if (matches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(cardBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matches to manage. Create one from Command Center.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    }
                }
            }

            items(matches) { match ->
                val isLive = match.status == "Live" || match.status == "Ongoing"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(
                            1.dp,
                            if (isLive) Color(0xFFFF5252).copy(alpha = 0.8f) else cardBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(match.title, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Map: ${match.map} • ${match.time} • ${match.prize}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (match.status) {
                                            "Live", "Ongoing" -> Color(0xFF3B151C)
                                            "Completed" -> Color(0xFF152E20)
                                            else -> Color(0xFF1F2332)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when (match.status) {
                                            "Live", "Ongoing" -> Color(0xFFFF5252)
                                            "Completed" -> neonGreen
                                            else -> Color(0xFF43475C)
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    match.status.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    color = when (match.status) {
                                        "Live", "Ongoing" -> Color(0xFFFF5252)
                                        "Completed" -> neonGreen
                                        else -> Color.White
                                    }
                                )
                            }
                        }

                        // Room Info & Slots Counter
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ROOM ID & PASSWORD", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.sp)
                                val roomInfo = if (match.roomId.isNotBlank()) "ID: ${match.roomId} | Pass: ${match.roomPass}" else "Not Set Yet"
                                Text(roomInfo, color = if (match.roomId.isNotBlank()) neonGreen else Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            // Booked slots badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF141F1A))
                                    .border(1.dp, neonGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMatchForBookings = match
                                        showBookingsDialog = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = neonGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("${match.bookedSlots.size} Booked", color = neonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Live stream preview link badge
                        if (match.liveUrl.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF261218))
                                    .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Match Live Stream Linked", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Test Stream",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(match.liveUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // ignore
                                            }
                                        }
                                )
                            }
                        }

                        // Actions Area
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            // View Bookings Button
                            Button(
                                onClick = {
                                    selectedMatchForBookings = match
                                    showBookingsDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = surfaceSubtle),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PeopleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Players List", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Edit ID/Pass Button
                            Button(
                                onClick = {
                                    selectedMatchForEdit = match
                                    editMap = match.map
                                    editPrize = match.prize
                                    editEntryType = when {
                                        match.entryType.isNotBlank() -> match.entryType
                                        match.entry.contains("Ad", ignoreCase = true) -> "AD"
                                        match.entry.equals("Free", ignoreCase = true) -> "FREE"
                                        else -> "PAID"
                                    }
                                    editPaidEntryFee = match.entry.filter { it.isDigit() }.ifBlank { "10" }
                                    editRequiredAds = (if (match.requiredAds > 0) match.requiredAds else (match.entry.filter { it.isDigit() }.toIntOrNull() ?: 1)).toString()
                                    roomId = match.roomId
                                    roomPass = match.roomPass
                                    status = match.status
                                    liveUrl = match.liveUrl
                                    editRules = match.rules
                                    showEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = surfaceSubtle),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = neonGreen, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit / IDP", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Delete Match Button
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        db?.collection("matches")?.document(match.id)?.delete()
                                    }
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF2B1418), RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}
