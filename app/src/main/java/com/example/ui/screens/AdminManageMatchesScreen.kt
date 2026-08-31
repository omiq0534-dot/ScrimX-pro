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
import com.google.firebase.firestore.FirebaseFirestore
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
            containerColor = Color(0xFF14161F),
            onDismissRequest = { showEditDialog = false },
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
                    Text(selectedMatchForEdit!!.title, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    ClassyDarkInput(
                        value = editMap,
                        onValueChange = { editMap = it },
                        label = "Map Name (Custom)",
                        placeholder = "Bermuda / Kalahari / Erangel"
                    )

                    ClassyDarkInput(
                        value = editPrize,
                        onValueChange = { editPrize = it },
                        label = "Prize Pool",
                        placeholder = "e.g. ₹500"
                    )

                    Text("ENTRY TYPE & REQUIREMENTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF75798E))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "FREE" to "🆓 Free",
                            "AD" to "🎬 Watch Ad",
                            "PAID" to "💵 Paid"
                        ).forEach { (tKey, tLabel) ->
                            val isSel = editEntryType == tKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF222738) else Color(0xFF0C0D12))
                                    .border(1.dp, if (isSel) Color(0xFFFFD700) else Color(0xFF262938), RoundedCornerShape(8.dp))
                                    .clickable { editEntryType = tKey }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    tLabel,
                                    color = if (isSel) Color(0xFFFFD700) else Color.White,
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
                            label = "Number of Ads to Watch",
                            placeholder = "1, 2, 3..."
                        )
                    } else if (editEntryType == "PAID") {
                        ClassyDarkInput(
                            value = editPaidEntryFee,
                            onValueChange = { editPaidEntryFee = it },
                            label = "Entry Fee (Coins / ₹)",
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
                        label = "Match Live Stream URL",
                        placeholder = "https://youtube.com/live/..."
                    )

                    OutlinedTextField(
                        value = editRules,
                        onValueChange = { editRules = it },
                        label = { Text("Match Custom Rules", color = Color(0xFF75798E)) },
                        placeholder = { Text("e.g. Desert Eagle Only, 13 Rounds, No Grenades...", color = Color(0xFF3E4254)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF262938),
                            focusedContainerColor = Color(0xFF0C0D12),
                            unfocusedContainerColor = Color(0xFF0C0D12),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFFFD700)
                        ),
                        minLines = 2,
                        maxLines = 4
                    )

                    Text("Match Status", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF75798E))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Upcoming", "Live", "Completed").forEach { st ->
                            val isSelected = status == st
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF222636) else Color(0xFF0C0D12))
                                    .border(1.dp, if (isSelected) Color.White else Color(0xFF262938), RoundedCornerShape(8.dp))
                                    .clickable { status = st }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    st,
                                    color = if (isSelected) Color.White else Color(0xFF75798E),
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
                                "🎬 Free ($adsCount Ad${if (adsCount > 1) "s" else ""})"
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color(0xFF75798E))
                }
            }
        )
    }

    // 2. View Bookings & Player List Dialog
    if (showBookingsDialog && selectedMatchForBookings != null) {
        val targetMatch = selectedMatchForBookings!!
        val isSquad = targetMatch.mode.equals("Squad", ignoreCase = true) || targetMatch.title.contains("Squad", true)
        val bookedMap = targetMatch.bookedSlots
        val namesMap = targetMatch.slotNames
        val uidsMap = targetMatch.slotUids

        AlertDialog(
            containerColor = Color(0xFF14161F),
            onDismissRequest = { showBookingsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "REGISTERED PLAYERS & TEAMS",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF222636))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${bookedMap.size} Booked", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "${targetMatch.title} • Map: ${targetMatch.map}",
                        color = Color(0xFF8E92A4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (bookedMap.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C0D12))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No slots booked yet for this match.", color = Color(0xFF75798E), fontSize = 13.sp)
                        }
                    } else {
                        val sortedSlotKeys = bookedMap.keys.mapNotNull { it.toIntOrNull() }.sorted()
                        sortedSlotKeys.forEach { slotNum ->
                            val key = slotNum.toString()
                            val registeredName = namesMap[key] ?: "Player $slotNum"
                            val inGameUid = uidsMap[key] ?: "N/A"
                            val accountEmail = bookedMap[key] ?: ""

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0C0D12))
                                    .border(1.dp, Color(0xFF262938), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF202330)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("$slotNum", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 13.sp)
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
                                                Text("UID: $inGameUid", color = Color(0xFF8E92A4), fontSize = 11.sp)
                                            }
                                            Text("Email: $accountEmail", color = Color(0xFF5A5E72), fontSize = 10.sp)
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
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0C0D12),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MANAGE MATCHES & ROOMS",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0C0D12))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            if (matches.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF14161F))
                            .border(1.dp, Color(0xFF262938), RoundedCornerShape(18.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matches to manage. Create one from Command Center!", color = Color(0xFF75798E), fontSize = 13.sp)
                    }
                }
            }

            items(matches) { match ->
                val isLive = match.status == "Live" || match.status == "Ongoing"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF14161F))
                        .border(
                            1.dp,
                            if (isLive) Color(0xFFFF5252).copy(alpha = 0.6f) else Color(0xFF262938),
                            RoundedCornerShape(18.dp)
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
                                Text(match.title, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Map: ${match.map} • ${match.time} • ${match.prize}",
                                    color = Color(0xFF8E92A4),
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
                                            "Completed" -> Color(0xFF00E676)
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
                                        "Completed" -> Color(0xFF00E676)
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
                                .background(Color(0xFF0C0D12))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ROOM ID & PASSWORD", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF75798E), letterSpacing = 1.sp)
                                val roomInfo = if (match.roomId.isNotBlank()) "ID: ${match.roomId} | Pass: ${match.roomPass}" else "Not Set Yet"
                                Text(roomInfo, color = if (match.roomId.isNotBlank()) Color(0xFFFFD700) else Color(0xFF5A5E72), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            // Booked slots clickable badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1A1D28))
                                    .border(1.dp, Color(0xFF2C3042), RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedMatchForBookings = match
                                        showBookingsDialog = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("${match.bookedSlots.size} Booked", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Live stream preview link badge
                        if (match.liveUrl.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1F1216))
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E212D)),
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262A3B)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
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
