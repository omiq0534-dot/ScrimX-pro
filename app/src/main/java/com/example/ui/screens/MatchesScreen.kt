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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
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
                    color = Color(0xFF111827)
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
                            .background(if (selectedTab == 0) Color.White else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = if (selectedTab == 0) Color.Black else Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "All Scrims (${matches.size})",
                                color = if (selectedTab == 0) Color.Black else Color(0xFF9CA3AF),
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
                            .background(if (selectedTab == 1) Color.White else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (selectedTab == 1) Color.Black else Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "My Matches (${myMatches.size})",
                                color = if (selectedTab == 1) Color.Black else Color(0xFF9CA3AF),
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

    var isCardPressed by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.962f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "myCardScale"
    )
    val cardOffsetY by animateDpAsState(
        targetValue = if (isCardPressed) 5.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "myCardOffsetY"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isCardPressed) 3.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "myCardElevation"
    )

    var isBtnPressed by remember { mutableStateOf(false) }
    val btnScale by animateFloatAsState(
        targetValue = if (isBtnPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "myBtnScale"
    )
    val btnOffsetY by animateDpAsState(
        targetValue = if (isBtnPressed) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "myBtnOffsetY"
    )

    // Outer 3D Base Chassis Container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .offset(y = cardOffsetY)
            .scale(cardScale)
            .shadow(
                elevation = cardElevation,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF00E676).copy(alpha = if (isCardPressed) 0.20f else 0.45f),
                ambientColor = Color(0xFF000000).copy(alpha = 0.85f)
            )
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B232E),
                        Color(0xFF0D1217),
                        Color(0xFF05080A)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(bottom = if (isCardPressed) 1.dp else 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isCardPressed = true
                            tryAwaitRelease()
                            isCardPressed = false
                        },
                        onTap = { onCardClick() }
                    )
                }
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0xF2131A24),
                            Color(0xE80C1017),
                            Color(0xF508140F)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .border(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0x99FFFFFF),
                            Color(0x25FFFFFF),
                            Color(0xFF00E676).copy(alpha = 0.75f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
                    ),
                    width = 1.5.dp,
                    shape = RoundedCornerShape(22.dp)
                )
        ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // 1. Top-Right Corner Black Light / Shadow Glow
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF000000).copy(alpha = 0.96f),
                        Color(0xFF04070A).copy(alpha = 0.88f),
                        Color(0xFF090E14).copy(alpha = 0.55f),
                        Color.Transparent
                    ),
                    center = androidx.compose.ui.geometry.Offset(w * 1.05f, -h * 0.1f),
                    radius = (w.coerceAtLeast(h)) * 1.05f
                ),
                center = androidx.compose.ui.geometry.Offset(w * 1.05f, -h * 0.1f),
                radius = (w.coerceAtLeast(h)) * 1.05f
            )

            // 2. Bottom-Left Corner Green Ambient Glow
            drawCircle(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E676).copy(alpha = 0.28f),
                        Color(0xFF00E676).copy(alpha = 0.14f),
                        Color(0xFF00E676).copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = androidx.compose.ui.geometry.Offset(-w * 0.05f, h * 1.05f),
                    radius = (w.coerceAtLeast(h)) * 0.75f
                ),
                center = androidx.compose.ui.geometry.Offset(-w * 0.05f, h * 1.05f),
                radius = (w.coerceAtLeast(h)) * 0.75f
            )

            // 3. Top Specular Glass Line
            drawLine(
                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.20f),
                        Color.Transparent
                    )
                ),
                start = androidx.compose.ui.geometry.Offset(0f, 1.2f),
                end = androidx.compose.ui.geometry.Offset(w * 0.70f, 1.2f),
                strokeWidth = 2.5f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                                    isLive -> Color(0xFF330C12)
                                    isCompleted -> Color(0xFF141722)
                                    else -> Color(0xFF0D2517)
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    isLive -> Color(0xFFFF5252)
                                    isCompleted -> Color(0xFF8E92A4)
                                    else -> Color(0xFF00E676)
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when {
                                isLive -> "● LIVE NOW"
                                isCompleted -> "FINISHED"
                                else -> "CONFIRMED"
                            },
                            color = when {
                                isLive -> Color(0xFFFF5252)
                                isCompleted -> Color(0xFF8E92A4)
                                else -> Color(0xFF00E676)
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33000000))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(match.map, color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // Slot Number Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D2517))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("SLOT #$slotNumber", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }

            // Title & Time
            Column {
                Text(
                    match.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(match.time, fontSize = 12.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.SemiBold)
                }
            }

            // Player IGN / In-Game UID details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141722))
                    .border(1.dp, Color(0xFF23293A), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("REGISTERED AS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.sp)
                        Text(playerNameOrTeam, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                    if (inGameUid.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("FREE FIRE UID", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.sp)
                            Text(inGameUid, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00E676))
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
                        .background(Color(0xFF0D2517))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(14.dp))
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
                                Text("ROOM ID: ${match.roomId}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("PASSWORD: ${match.roomPass.ifBlank { "None" }}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                .background(Color(0xFF141722), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Room Details", tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1D2B))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Room ID & Password will be released 15 mins before match starts.",
                            color = Color(0xFF8E92A4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action Buttons (Watch Stream or Open Match Details) with 3D press response
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (match.liveUrl.isNotBlank()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(match.liveUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B0B11)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LIVE STREAM", color = Color(0xFFFF5252), fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .scale(btnScale)
                        .shadow(
                            elevation = if (isBtnPressed) 1.dp else 6.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = Color(0xFF00E676).copy(alpha = if (isBtnPressed) 0.2f else 0.45f),
                            ambientColor = Color(0xFF000000).copy(alpha = 0.8f)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isBtnPressed = true
                                    tryAwaitRelease()
                                    isBtnPressed = false
                                },
                                onTap = { onCardClick() }
                            )
                        }
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = if (isBtnPressed) listOf(Color(0xFF041008), Color(0xFF091E11))
                                else listOf(Color(0xFF0E301B), Color(0xFF05120A))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = if (isBtnPressed) listOf(
                                    Color(0xFF00E676).copy(alpha = 0.4f),
                                    Color(0x33FFFFFF)
                                ) else listOf(
                                    Color(0xFF69F0AE),
                                    Color(0xFF00E676),
                                    Color(0x2200E676)
                                )
                            ),
                            width = 1.4.dp,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "VIEW SLOTS & DETAILS",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                }
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
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF141722))
            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(22.dp))
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
                    .background(Color(0xFF1A1D2B))
                    .border(1.dp, Color(0xFF00E676), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                description,
                color = Color(0xFF8E92A4),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
