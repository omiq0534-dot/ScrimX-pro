package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    matchId: String,
    navController: NavController,
    viewModel: MatchesViewModel = viewModel()
) {
    val context = LocalContext.current
    val matchState by viewModel.currentMatch.collectAsState()
    
    var selectedSlot by remember { mutableStateOf<Int?>(null) }
    var isBooking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog state for Player/Team details
    var showBookingDialog by remember { mutableStateOf(false) }
    var inputPlayerOrTeamName by remember { mutableStateOf("") }
    var inputInGameUid by remember { mutableStateOf("") }
    
    LaunchedEffect(matchId) {
        viewModel.listenToMatchDetails(matchId)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMatchListener()
        }
    }
    
    val match = matchState
    
    if (match == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Black)
        }
        return
    }

    val bookedSlots = match.bookedSlots
    val slotNames = match.slotNames
    val slotUids = match.slotUids

    val matchMode = when {
        match.mode.isNotBlank() -> match.mode
        match.title.contains("Solo", true) -> "Solo"
        match.title.contains("Duo", true) -> "Duo"
        else -> "Squad"
    }

    val totalSlotsCount = if (match.totalSlots > 0) {
        match.totalSlots
    } else {
        when (matchMode) {
            "Solo" -> 48
            "Duo" -> 24
            else -> 12
        }
    }

    // Classy Booking Confirmation Dialog
    if (showBookingDialog && selectedSlot != null) {
        val isSquad = matchMode.equals("Squad", ignoreCase = true)
        val nameLabel = if (isSquad) "Team Name" else "Player In-Game Name (IGN)"
        val namePlaceholder = if (isSquad) "e.g. Total Gaming / GodLike" else "e.g. ProSniper_99"

        AlertDialog(
            containerColor = Color(0xFF14151B),
            onDismissRequest = { if (!isBooking) showBookingDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "CONFIRM SLOT $selectedSlot",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Match: ${match.title} (${match.map})",
                        color = Color(0xFFAAAAAA),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = inputPlayerOrTeamName,
                        onValueChange = { inputPlayerOrTeamName = it },
                        label = { Text(nameLabel, color = Color(0xFF9E9EA8)) },
                        placeholder = { Text(namePlaceholder, color = Color(0xFF555566)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF2E313D),
                            focusedContainerColor = Color(0xFF0C0D11),
                            unfocusedContainerColor = Color(0xFF0C0D11),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputInGameUid,
                        onValueChange = { inputInGameUid = it },
                        label = { Text("Game In-Game UID", color = Color(0xFF9E9EA8)) },
                        placeholder = { Text("e.g. 192847291", color = Color(0xFF555566)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF2E313D),
                            focusedContainerColor = Color(0xFF0C0D11),
                            unfocusedContainerColor = Color(0xFF0C0D11),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1F222C))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Entry Deductible", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                            Text(match.entry, color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val slotToBook = selectedSlot ?: return@Button
                        if (inputPlayerOrTeamName.isBlank()) {
                            Toast.makeText(context, "Please enter ${if (isSquad) "Team Name" else "Player Name"}", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isBooking = true
                        errorMessage = null
                        viewModel.bookSlot(
                            matchId = match.id,
                            slotNumber = slotToBook,
                            playerNameOrTeam = inputPlayerOrTeamName.trim(),
                            inGameUid = inputInGameUid.trim(),
                            onSuccess = {
                                isBooking = false
                                showBookingDialog = false
                                selectedSlot = null
                                inputPlayerOrTeamName = ""
                                inputInGameUid = ""
                                Toast.makeText(context, "Slot $slotToBook Booked Successfully!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                isBooking = false
                                errorMessage = err
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isBooking && inputPlayerOrTeamName.isNotBlank()
                ) {
                    if (isBooking) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                    } else {
                        Text("CONFIRM & PAY", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isBooking) showBookingDialog = false }) {
                    Text("Cancel", color = Color(0xFF9E9EA8))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Match Details", fontWeight = FontWeight.Black, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Button(
                    onClick = {
                        if (selectedSlot != null) {
                            showBookingDialog = true
                        }
                    },
                    enabled = selectedSlot != null && !isBooking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        disabledContainerColor = Color(0xFFE5E5EA),
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF8E8E93)
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isBooking) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else if (selectedSlot != null) {
                        Text("BOOK SLOT $selectedSlot • ${match.entry}", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 0.5.sp)
                    } else {
                        Text("SELECT A SLOT ABOVE TO BOOK", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Title and basic info
            Spacer(modifier = Modifier.height(8.dp))
            Text(match.title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.Black, lineHeight = 32.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(match.map)
                InfoChip(matchMode)
                InfoChip(if (match.badge.isNotBlank()) match.badge else "ESPORTS")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Prize and Entry
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailBox("Prize Pool", match.prize, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                DetailBox("Entry Fee", match.entry, Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Time & Room ID Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF7F7FA))
                    .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Match Timing: ${match.time}", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                    }
                    
                    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    
                    val hasBooked = bookedSlots.values.any { it == currentUserEmail || it == currentUserId }
                    
                    if (hasBooked && match.roomId.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("ROOM CREDENTIALS UNLOCKED", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Room ID: ${match.roomId}", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                                if (match.roomPass.isNotEmpty()) {
                                    Text("Password: ${match.roomPass}", fontWeight = FontWeight.Black, color = Color(0xFF00E5FF), fontSize = 15.sp)
                                }
                            }
                        }
                    } else if (hasBooked) {
                        Text(
                            "You are registered! Room ID & Password will appear here 15 mins before match starts.",
                            color = Color(0xFF2E7D32),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            "Book a slot below to unlock Room ID & Password when released by admin.",
                            color = Color(0xFF666677),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            // Watch Live stream button if live
            if (match.status == "Live" || match.status == "Ongoing" || match.liveUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        val url = if (match.liveUrl.isNotBlank()) {
                            match.liveUrl
                        } else {
                            "https://www.youtube.com/results?search_query=${Uri.encode(match.title + " Live")}"
                        }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open stream", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF003C), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Watch Live", modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WATCH LIVE STREAM", fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Slots Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SELECT YOUR SLOT", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 0.5.sp)
                Text("${bookedSlots.size}/$totalSlotsCount Booked", fontSize = 12.sp, color = Color(0xFF666677), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Slots Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val slots = (1..totalSlotsCount).map { i ->
                    val isBooked = bookedSlots.containsKey(i.toString())
                    val bookedName = slotNames[i.toString()] ?: if (isBooked) "Booked Player" else null
                    val inGameId = slotUids[i.toString()]
                    MatchSlot(
                        number = i,
                        isBooked = isBooked,
                        teamName = bookedName,
                        inGameUid = inGameId
                    )
                }

                slots.forEach { slot ->
                    val isSelected = selectedSlot == slot.number
                    SlotCard(slot = slot, isSelected = isSelected) {
                        if (!slot.isBooked) selectedSlot = slot.number
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Rules
            Text("TOURNAMENT RULES", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "1. Emulators & Hacks are strictly prohibited (Instant Ban).\n" +
                "2. Join only the designated Slot Number you booked.\n" +
                "3. Room ID & Password must not be shared with external players.\n" +
                "4. Screenshot of final result must be kept for verification.",
                color = Color(0xFF444455),
                fontSize = 13.sp,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

data class MatchSlot(
    val number: Int,
    val isBooked: Boolean,
    val teamName: String? = null,
    val inGameUid: String? = null
)

@Composable
fun SlotCard(slot: MatchSlot, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = when {
        isSelected -> Color.Black
        slot.isBooked -> Color(0xFFF2F2F7)
        else -> Color(0xFFFFFFFF)
    }
    
    val borderColor = when {
        isSelected -> Color.Black
        slot.isBooked -> Color(0xFFE5E5EA)
        else -> Color(0xFFD1D1D6)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = !slot.isBooked) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFFFFD700) else if (slot.isBooked) Color(0xFFD1D1D6) else Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${slot.number}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = if (isSelected) Color.Black else if (slot.isBooked) Color(0xFF666677) else Color.White
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                if (slot.isBooked) {
                    Text(
                        slot.teamName ?: "Reserved",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color(0xFF1C1C1E)
                    )
                    if (!slot.inGameUid.isNullOrBlank() && slot.inGameUid != "N/A") {
                        Text(
                            "UID: ${slot.inGameUid}",
                            fontSize = 11.sp,
                            color = Color(0xFF8E8E93),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        "Slot ${slot.number}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isSelected) Color.White else Color.Black
                    )
                    Text(
                        "Available",
                        fontSize = 11.sp,
                        color = if (isSelected) Color(0xFFCCCCCC) else Color(0xFF8E8E93)
                    )
                }
            }
        }
        
        if (slot.isBooked) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE8F5E9))
                    .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("BOOKED", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        } else {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFD700))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("SELECTED", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Text("TAP TO SELECT", color = Color(0xFF8E8E93), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1C1C1E))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
    }
}

@Composable
fun DetailBox(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(16.dp))
            .background(Color(0xFFF7F7FA))
            .padding(16.dp)
    ) {
        Text(title.uppercase(), fontSize = 10.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black)
    }
}
