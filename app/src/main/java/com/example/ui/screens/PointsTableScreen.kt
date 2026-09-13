package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.FirebaseHelper
import com.example.utils.TournamentTimeHelper
import com.example.utils.rememberResultCountdown
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class TeamRankEntry(
    val rank: Int,
    val teamName: String,
    val kills: String,
    val prize: String,
    val points: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsTableScreen(
    matchId: String,
    navController: NavController,
    matchesViewModel: MatchesViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentMatch by matchesViewModel.currentMatch.collectAsState()
    val auth = FirebaseHelper.getAuth()
    val currentUserEmail = auth?.currentUser?.email ?: ""
    val currentUserUid = auth?.currentUser?.uid ?: ""

    var fullScreenImage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(matchId) {
        matchesViewModel.listenToMatchDetails(matchId)
    }

    DisposableEffect(Unit) {
        onDispose {
            matchesViewModel.clearMatchListener()
        }
    }

    if (currentMatch == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0D14)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF00E676))
        }
        return
    }

    val match = currentMatch!!
    val isJoined = remember(match, currentUserEmail, currentUserUid) {
        match.bookedSlots.values.any { it.equals(currentUserEmail, ignoreCase = true) || it == currentUserUid }
    }

    val isResultReady = TournamentTimeHelper.isResultReady(
        match.resultTime,
        match.isResultDeclared,
        match.status
    )
    val isPublicAllowed = TournamentTimeHelper.isPublicAccessAllowed(
        match.resultTime,
        match.isResultDeclared,
        match.resultPublicDelayMinutes,
        isJoined,
        match.status
    )

    val resultCountdown = rememberResultCountdown(match.resultTime, match.isResultDeclared)

    // Parse structured standings
    val standingsList = remember(match.pointsTableRanks) {
        parseStandings(match.pointsTableRanks)
    }

    Scaffold(
        containerColor = Color(0xFF080B11),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isResultReady) Color(0xFF00E676) else Color(0xFFFFB300))
                            )
                            Text(
                                "OFFICIAL SCORECARD",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            match.title,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            shareMatchResult(context, match, standingsList)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF00E676))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF080B11))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            // 1. Tournament Overview Header Card
            item {
                TournamentHeaderCard(match = match, isResultReady = isResultReady, resultCountdown = resultCountdown)
            }

            // 2. VIP Priority Access Banner
            item {
                AccessStatusBanner(isJoined = isJoined, isPublicAllowed = isPublicAllowed, match = match)
            }

            // 3. Quick Action Bar (Download & Share)
            item {
                ScorecardActionButtons(
                    match = match,
                    standings = standingsList,
                    onDownload = {
                        downloadScorecardSummary(context, match, standingsList)
                    },
                    onShare = {
                        shareMatchResult(context, match, standingsList)
                    }
                )
            }

            // 4. Admin Uploaded Scorecard Screenshot (if available)
            if (match.pointsTableImageUrl.isNotBlank()) {
                item {
                    Text(
                        "OFFICIAL MATCH SCREENSHOT",
                        color = Color(0xFF8E92A4),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF101520))
                            .border(1.2.dp, Color(0x3300E676), RoundedCornerShape(18.dp))
                            .clickable { fullScreenImage = match.pointsTableImageUrl }
                    ) {
                        AsyncImage(
                            model = match.pointsTableImageUrl,
                            contentDescription = "Match Scorecard Screenshot",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp, max = 280.dp),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xCC000000))
                                .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text("TAP TO EXPAND", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5. Team Standings Matrix
            item {
                Text(
                    "LEADERBOARD & PRIZE BREAKDOWN",
                    color = Color(0xFF8E92A4),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            }

            if (standingsList.isEmpty() && match.pointsTableImageUrl.isBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF121722))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                if (isResultReady) "Scorecard is being compiled by Admin." else "Result declaration time: ${match.resultTime.ifBlank { "Soon" }}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Standings and kill points will appear here shortly.",
                                color = Color(0xFF8E92A4),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(standingsList) { index, entry ->
                    TeamStandingRowCard(entry = entry, isCurrentUserTeam = false)
                }
            }

            // 6. Admin MVP / Custom Notes
            if (match.pointsTableNotes.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0E1A14), Color(0xFF09120D))
                                )
                            )
                            .border(1.dp, Color(0x4000E676), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                Text("ADMIN MATCH SUMMARY & MVP", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                match.pointsTableNotes,
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Screenshot Modal
    fullScreenImage?.let { imgUrl ->
        Dialog(onDismissRequest = { fullScreenImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xE6000000))
                    .clickable { fullScreenImage = null },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Expanded Scorecard",
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { fullScreenImage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937))
                    ) {
                        Text("CLOSE PREVIEW", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TournamentHeaderCard(
    match: MatchData,
    isResultReady: Boolean,
    resultCountdown: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF00E676).copy(alpha = 0.35f))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF141F1A), Color(0xFF0D1418))
                )
            )
            .border(
                1.3.dp,
                Brush.verticalGradient(
                    listOf(Color(0xFF80FFC0), Color(0xFF00E676), Color(0x3300E676))
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${match.map.uppercase()} • ${match.mode.uppercase()}",
                        color = Color(0xFF00E676),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(15.dp))
                    Text(
                        if (match.prize.startsWith("₹")) match.prize else "₹${match.prize}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(match.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Match Time
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                    Text("Match: ${match.time}", color = Color(0xFFCBD5E1), fontSize = 11.5.sp)
                }

                // Result Time
                if (match.resultTime.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(13.dp))
                        Text(
                            if (resultCountdown != null) "Declaring in $resultCountdown" else "Result: ${match.resultTime}",
                            color = Color(0xFF00E676),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessStatusBanner(
    isJoined: Boolean,
    isPublicAllowed: Boolean,
    match: MatchData
) {
    if (isJoined) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF062211))
                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676).copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("👑 PRIORITY ACCESS VERIFIED", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 11.sp)
                    Text("You joined this tournament. Instant scorecard & winnings claim are unlocked.", color = Color(0xFFCBD5E1), fontSize = 10.5.sp)
                }
            }
        }
    } else if (!isPublicAllowed) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF241C0A))
                .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                Column {
                    Text("🔒 VIP EARLY ACCESS ACTIVE", color = Color(0xFFFFB300), fontWeight = FontWeight.Black, fontSize = 11.sp)
                    Text("Joined players are reviewing standings. Full public leaderboard opens in ${match.resultPublicDelayMinutes}m.", color = Color(0xFFE2E8F0), fontSize = 10.5.sp)
                }
            }
        }
    }
}

@Composable
private fun ScorecardActionButtons(
    match: MatchData,
    standings: List<TeamRankEntry>,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Download Button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F3B20), Color(0xFF06180D))
                    )
                )
                .border(
                    1.2.dp,
                    Brush.verticalGradient(
                        listOf(Color(0xFF80FFC0), Color(0xFF00E676))
                    ),
                    RoundedCornerShape(14.dp)
                )
                .clickable { onDownload() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                Text("DOWNLOAD SCORECARD", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 11.5.sp)
            }
        }

        // Share Button
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E2838), Color(0xFF111722))
                    )
                )
                .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(14.dp))
                .clickable { onShare() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("SHARE STANDINGS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.5.sp)
            }
        }
    }
}

@Composable
private fun TeamStandingRowCard(entry: TeamRankEntry, isCurrentUserTeam: Boolean) {
    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color(0xFF94A3B8)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (entry.rank == 1) {
                    Brush.verticalGradient(listOf(Color(0xFF1D2618), Color(0xFF10170F)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF141924), Color(0xFF0C1018)))
                }
            )
            .border(
                1.dp,
                if (entry.rank == 1) Color(0x66FFD700) else Color(0x22FFFFFF),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank Badge & Team Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(rankColor.copy(alpha = 0.18f))
                        .border(1.dp, rankColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${entry.rank}",
                        color = rankColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            entry.teamName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                        if (entry.rank == 1) {
                            Text("👑 BOOYAH", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text(
                        entry.kills,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.5.sp
                    )
                }
            }

            // Prize Won (if any)
            if (entry.prize.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        entry.prize,
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Parsing helper to convert multiline text formatted standings into structured list
 * Format: "1 | Team Name | 12 Kills | ₹250"
 */
private fun parseStandings(rawText: String): List<TeamRankEntry> {
    if (rawText.isBlank()) return emptyList()

    val lines = rawText.lines().filter { it.isNotBlank() }
    val result = mutableListOf<TeamRankEntry>()

    lines.forEachIndexed { index, line ->
        val parts = line.split("|").map { it.trim() }
        if (parts.size >= 2) {
            val rank = parts[0].replace("#", "").trim().toIntOrNull() ?: (index + 1)
            val team = parts[1]
            val kills = if (parts.size > 2) parts[2] else ""
            val prize = if (parts.size > 3) parts[3] else ""
            result.add(TeamRankEntry(rank = rank, teamName = team, kills = kills, prize = prize))
        } else {
            // fallback plain line
            result.add(TeamRankEntry(rank = index + 1, teamName = line, kills = "", prize = ""))
        }
    }
    return result
}

/**
 * Download & Save Scorecard summary as text/file
 */
private fun downloadScorecardSummary(
    context: Context,
    match: MatchData,
    standings: List<TeamRankEntry>
) {
    try {
        val fileName = "Scorecard_${match.title.replace(" ", "_")}_${System.currentTimeMillis()}.txt"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("   OFFICIAL ESPORTS TOURNAMENT SCORECARD\n")
        sb.append("=========================================\n")
        sb.append("Tournament : ${match.title}\n")
        sb.append("Map        : ${match.map}\n")
        sb.append("Mode       : ${match.mode}\n")
        sb.append("Time       : ${match.time}\n")
        sb.append("Prize Pool : ${match.prize}\n\n")
        sb.append("--- STANDINGS & LEADERBOARD ---\n")
        if (standings.isNotEmpty()) {
            standings.forEach { s ->
                sb.append("#${s.rank} | ${s.teamName} | ${s.kills} | Prize: ${s.prize}\n")
            }
        } else {
            sb.append("Admin Screenshot uploaded in app.\n")
        }
        if (match.pointsTableNotes.isNotBlank()) {
            sb.append("\nAdmin Notes: ${match.pointsTableNotes}\n")
        }
        sb.append("=========================================\n")
        sb.append("Generated by Free Fire Scrims App\n")

        FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }

        Toast.makeText(context, "📥 Scorecard saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Scorecard summary generated in app.", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Share Match Scorecard via Android Share Sheet
 */
private fun shareMatchResult(
    context: Context,
    match: MatchData,
    standings: List<TeamRankEntry>
) {
    val sb = StringBuilder()
    sb.append("🏆 *OFFICIAL SCORECARD - ${match.title}*\n")
    sb.append("📍 Map: ${match.map} | Mode: ${match.mode}\n")
    sb.append("💰 Prize Pool: ${match.prize}\n\n")

    if (standings.isNotEmpty()) {
        sb.append("🔥 *TOP STANDINGS:*\n")
        standings.take(5).forEach { s ->
            val crown = if (s.rank == 1) "👑 " else ""
            sb.append("$crown#${s.rank} ${s.teamName} (${s.kills}) - ${s.prize}\n")
        }
    }

    if (match.pointsTableNotes.isNotBlank()) {
        sb.append("\n✨ ${match.pointsTableNotes}\n")
    }

    sb.append("\n📲 Play Daily Scrims & Win Real Cash! Download our Free Fire Esports App now!")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Tournament Scorecard: ${match.title}")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share Scorecard"))
}
