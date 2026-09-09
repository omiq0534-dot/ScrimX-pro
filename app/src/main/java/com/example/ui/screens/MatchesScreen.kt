package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.example.ui.theme.AppColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.ui.components.PremiumMatchCard
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MatchesScreen(
    navController: NavController,
    viewModel: MatchesViewModel = viewModel()
) {
    val matches by viewModel.matches.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val currentUserEmail = auth.currentUser?.email ?: ""

    var selectedTab by remember { mutableStateOf(0) } // 0 = All Scrims, 1 = My Joined Matches

    val myMatches = remember(matches, currentUserId, currentUserEmail) {
        if (currentUserId.isBlank() && currentUserEmail.isBlank()) emptyList()
        else matches.filter { match ->
            match.bookedSlots.values.any { slotOwner ->
                (currentUserId.isNotBlank() && slotOwner == currentUserId) ||
                (currentUserEmail.isNotBlank() && slotOwner.equals(currentUserEmail, ignoreCase = true))
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAFA))
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
            ) {
                Text(
                    "Tournaments & Scrims",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.ScreenBackground
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E212D))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 1: All Tournaments
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == 0) Color(0xFF33384D) else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = if (selectedTab == 0) AppColors.TextPrimary else Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "All Scrims (${matches.size})",
                                color = if (selectedTab == 0) AppColors.TextPrimary else Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Tab 2: My Joined Matches
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == 1) Color(0xFF33384D) else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (selectedTab == 1) Color(0xFFFFD700) else Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "My Matches (${myMatches.size})",
                                color = if (selectedTab == 1) AppColors.TextPrimary else Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            if (selectedTab == 0) {
                // ALL SCRIMS
                if (matches.isEmpty()) {
                    item {
                        EmptyMatchesCard(
                            icon = Icons.Default.SportsEsports,
                            title = "No Upcoming Matches",
                            description = "New Free Fire Custom Tournaments will appear here shortly. Stay tuned!"
                        )
                    }
                } else {
                    items(matches) { match ->
                        PremiumMatchCard(
                            title = match.title,
                            time = match.time,
                            prize = match.prize,
                            entry = match.entry,
                            badge = match.badge,
                            status = match.status,
                            map = match.map,
                            slotsBooked = match.bookedSlots.size,
                            totalSlots = if (match.totalSlots > 0) match.totalSlots else 12,
                            liveUrl = match.liveUrl,
                            onClick = { navController.navigate("match_details/${Uri.encode(match.id)}") }
                        )
                    }
                }
            } else {
                // MY JOINED MATCHES
                if (myMatches.isEmpty()) {
                    item {
                        EmptyMatchesCard(
                            icon = Icons.Default.EventBusy,
                            title = "You haven't joined any match yet!",
                            description = "Browse the 'All Scrims' tab, book your slot with Coins/Cash, and get Room ID & Password here."
                        )
                    }
                } else {
                    items(myMatches) { match ->
                        // Find user's booked slot number
                        val userSlotEntry = match.bookedSlots.entries.find { 
                            (currentUserId.isNotBlank() && it.value == currentUserId) ||
                            (currentUserEmail.isNotBlank() && it.value.equals(currentUserEmail, ignoreCase = true))
                        }
                        val slotNumber = userSlotEntry?.key ?: "?"
                        val playerNameOrTeam = match.slotNames[slotNumber] ?: "Registered Player"
                        val inGameUid = match.slotUids[slotNumber] ?: ""

                        MyJoinedMatchCard(
                            match = match,
                            slotNumber = slotNumber,
                            playerNameOrTeam = playerNameOrTeam,
                            inGameUid = inGameUid,
                            onCardClick = { navController.navigate("match_details/${Uri.encode(match.id)}") }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun MyJoinedMatchCard(
    match: MatchData,
    slotNumber: String,
    playerNameOrTeam: String,
    inGameUid: String,
    onCardClick: () -> Unit
) {
    val context = LocalContext.current
    val isLive = match.status.equals("Live", ignoreCase = true) || match.status.equals("Ongoing", ignoreCase = true)
    val isCompleted = match.status.equals("Completed", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x26000000),
                ambientColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.TextPrimary)
            .border(1.2.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
            .clickable { onCardClick() }
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isLive -> Color(0xFFFF003C)
                                    isCompleted -> Color(0xFF00C853)
                                    else -> AppColors.ScreenBackground
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when {
                                isLive -> "● LIVE NOW"
                                isCompleted -> "FINISHED"
                                else -> "CONFIRMED"
                            },
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F1F5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(match.map, color = Color(0xFF424250), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Slot Number Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFF9C4))
                        .border(1.dp, Color(0xFFFBC02D), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("SLOT #$slotNumber", color = Color(0xFF7F6000), fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }

            // Title & Time
            Column {
                Text(
                    match.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = AppColors.ScreenBackground
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(match.time, fontSize = 12.sp, color = Color(0xFF616161), fontWeight = FontWeight.SemiBold)
                }
            }

            // Player IGN / In-Game UID details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8F9FA))
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("REGISTERED AS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF888899))
                        Text(playerNameOrTeam, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.ScreenBackground)
                    }
                    if (inGameUid.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("FREE FIRE UID", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF888899))
                            Text(inGameUid, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.ScreenBackground)
                        }
                    }
                }
            }

            // Room ID & Pass Container
            if (match.roomId.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF14161F))
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00E676)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ROOM ID: ${match.roomId}", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("PASSWORD: ${match.roomPass.ifBlank { "None" }}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Room Details", "Room ID: ${match.roomId} | Password: ${match.roomPass}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Room ID & Password Copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF262938), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Room Details", tint = AppColors.TextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF8E1))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF57C00), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Room ID & Password will be released 15 mins before match starts.",
                            color = Color(0xFFE65100),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action Buttons (Watch Stream or Open Match Details)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (match.liveUrl.isNotBlank()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(match.liveUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AppColors.TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LIVE STREAM", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onCardClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.ScreenBackground),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text("VIEW SLOTS & DETAILS", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyMatchesCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.TextPrimary)
            .border(1.2.dp, Color(0xFFE5E5EA), RoundedCornerShape(24.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F2F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AppColors.ScreenBackground, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = AppColors.ScreenBackground,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                description,
                color = Color(0xFF757575),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
