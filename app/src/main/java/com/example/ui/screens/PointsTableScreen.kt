package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val booyahs: Int = 0,
    val kills: String,
    val killPoints: Int = 0,
    val placePoints: Int = 0,
    val totalPoints: Int = 0,
    val prize: String = ""
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
    var isDownloading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                    isDownloading = isDownloading,
                    onDownload = {
                        if (!isDownloading) {
                            isDownloading = true
                            scope.launch {
                                try {
                                    exportOrDownloadScorecardImage(
                                        context = context,
                                        match = match,
                                        standings = standingsList
                                    )
                                } finally {
                                    isDownloading = false
                                }
                            }
                        }
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

            // 5. Team Standings Matrix & Interactive Table Box
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(15.dp))
                        Text(
                            "OFFICIAL POINTS TABLE & STATS",
                            color = Color(0xFF8E92A4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Text(
                        "PTS = KILL + PLACE",
                        color = Color(0xFF00E676),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
            } else if (standingsList.isNotEmpty()) {
                // Interactive Zoomable & Movable Table Container (Bounded Area)
                item {
                    InteractivePointsTableContainer(standings = standingsList)
                }

                // Individual Team Cards View
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
                    Text("PRIORITY ACCESS VERIFIED", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 11.sp)
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
                    Text("VIP EARLY ACCESS ACTIVE", color = Color(0xFFFFB300), fontWeight = FontWeight.Black, fontSize = 11.sp)
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
    isDownloading: Boolean = false,
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
                .clickable(enabled = !isDownloading) { onDownload() }
                .padding(horizontal = 8.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        color = Color(0xFF00E676),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAVING...", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 11.sp)
                } else {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            "DOWNLOAD",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            lineHeight = 12.sp
                        )
                        Text(
                            "SCORECARD",
                            color = Color(0xFF80FFC0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            lineHeight = 10.sp
                        )
                    }
                }
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
                .padding(horizontal = 8.dp, vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "SHARE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        lineHeight = 12.sp
                    )
                    Text(
                        "STANDINGS",
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        lineHeight = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * Interactive Points Table Viewport Box (Move / Pan & Zoom with 1 finger or pinch, horizontal scroll, dedicated BOOYAH column)
 */
@Composable
private fun InteractivePointsTableContainer(standings: List<TeamRankEntry>) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D121D))
            .border(1.2.dp, Color(0x4400E676), RoundedCornerShape(16.dp))
    ) {
        // Top Toolbar: Controls and Drag Guide
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131A29))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag / Pan hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Swipe,
                    contentDescription = "Swipe / Pan",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "SWIPE LEFT-RIGHT • DRAG TO MOVE",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Quick Zoom Actions (+, -, Reset)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Zoom Out Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .clickable {
                            scale = (scale - 0.2f).coerceAtLeast(0.75f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(14.dp))
                }

                // Scale Display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${(scale * 100).toInt()}%",
                        color = Color(0xFF00E676),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Zoom In Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .clickable {
                            scale = (scale + 0.2f).coerceAtMost(2.5f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(14.dp))
                }

                // Reset Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .clickable {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset View", tint = Color(0xFFCBD5E1), modifier = Modifier.size(15.dp))
                }
            }
        }

        // Dedicated Bounded Viewport Box (Allows smooth 1-finger drag, pan, and pinch zoom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .clipToBounds()
                .background(Color(0xFF090D15))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.75f, 2.5f)
                        val maxOffsetX = 450f * scale
                        val maxOffsetY = 600f * scale
                        offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                        offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                    }
                }
        ) {
            // Transformable & Horizontally Scrollable Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offsetX
                        translationY = offsetY
                    }
                    .horizontalScroll(horizontalScrollState)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.width(620.dp)
                ) {
                    // Table Columns Header with dedicated BOOYAH Column
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF182234))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RANK", color = Color(0xFF94A3B8), fontSize = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(46.dp))
                        Text("TEAM / PLAYER", color = Color(0xFF94A3B8), fontSize = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(170.dp))
                        Text("BOOYAH", color = Color(0xFFFFD700), fontSize = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(76.dp), textAlign = TextAlign.Center)
                        Text("KILLS", color = Color(0xFF94A3B8), fontSize = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(58.dp), textAlign = TextAlign.Center)
                        Text("PLACE", color = Color(0xFF94A3B8), fontSize = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(58.dp), textAlign = TextAlign.Center)
                        Text("TOTAL", color = Color(0xFF00E676), fontSize = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(68.dp), textAlign = TextAlign.Center)
                        Text("PRIZE", color = Color(0xFFFFD700), fontSize = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(76.dp), textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Table Rows
                    standings.forEachIndexed { idx, item ->
                        val hasBooyah = item.booyahs > 0
                        val rowBg = if (item.rank == 1) {
                            Color(0xFF1A2616)
                        } else if (idx % 2 == 0) {
                            Color(0xFF111723)
                        } else {
                            Color(0xFF0D121C)
                        }

                        val rankColor = when (item.rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> Color(0xFF94A3B8)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(rowBg)
                                .border(
                                    0.6.dp,
                                    if (item.rank == 1) Color(0x66FFD700) else Color(0x15FFFFFF),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Rank Badge
                            Box(
                                modifier = Modifier.width(46.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "#${item.rank}",
                                    color = rankColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.5.sp
                                )
                            }

                            // 2. Team Name
                            Text(
                                text = item.teamName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(170.dp)
                            )

                            // 3. Dedicated BOOYAH Count Column
                            Box(
                                modifier = Modifier.width(76.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasBooyah) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFFD700).copy(alpha = 0.22f))
                                            .border(0.8.dp, Color(0xFFFFD700), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (item.booyahs > 1) "${item.booyahs} BOOYAH" else "1 BOOYAH",
                                            color = Color(0xFFFFD700),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                } else {
                                    Text(
                                        "0",
                                        color = Color(0xFF64748B),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 4. Kill Points
                            Text(
                                text = "${item.killPoints}",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(58.dp),
                                textAlign = TextAlign.Center
                            )

                            // 5. Place Points
                            Text(
                                text = "${item.placePoints}",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(58.dp),
                                textAlign = TextAlign.Center
                            )

                            // 6. Total Points (Highlighted)
                            Box(
                                modifier = Modifier.width(68.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF00E676).copy(alpha = 0.18f))
                                        .border(0.5.dp, Color(0x6600E676), RoundedCornerShape(5.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                                ) {
                                    Text(
                                        text = "${item.totalPoints}",
                                        color = Color(0xFF00E676),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            // 7. Prize
                            Text(
                                text = item.prize.ifBlank { "-" },
                                color = if (item.prize.isNotBlank()) Color(0xFFFFD700) else Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.width(76.dp),
                                textAlign = TextAlign.End,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
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
                    Brush.verticalGradient(listOf(Color(0xFF1D2618), Color(0xFF0E170F)))
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: Rank Badge + Team Name + Booyah Tag + Prize (on the right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Rank Badge
                    Box(
                        modifier = Modifier
                            .size(26.dp)
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

                    Text(
                        text = entry.teamName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (entry.booyahs > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                                .border(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                if (entry.booyahs > 1) "${entry.booyahs} BOOYAH" else "1 BOOYAH",
                                color = Color(0xFFFFD700),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Prize Won (if any)
                if (entry.prize.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00E676).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = entry.prize,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Row 2: Stats Breakdown Pills (Booyahs, Kills, Place Points, Total Points)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Booyah Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (entry.booyahs > 0) Color(0xFFFFD700).copy(alpha = 0.18f) else Color(0xFF1E2638))
                        .border(0.5.dp, if (entry.booyahs > 0) Color(0xFFFFD700).copy(alpha = 0.5f) else Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "BOOYAH: ${entry.booyahs}",
                        color = if (entry.booyahs > 0) Color(0xFFFFD700) else Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Kills Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E2638))
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "KILLS: ${entry.killPoints}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Place Pts Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E2638))
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PLACE: ${entry.placePoints}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Total Points Pill (Highlighted Neon)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E676).copy(alpha = 0.25f), Color(0xFF00B0FF).copy(alpha = 0.25f))
                            )
                        )
                        .border(0.8.dp, Color(0xFF00E676).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "TOTAL: ${entry.totalPoints} PTS",
                        color = Color(0xFF00E676),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

/**
 * Standard Placement Points in Esports Tournament Rules
 */
private fun getDefaultPlacePoints(rank: Int): Int {
    return when (rank) {
        1 -> 12
        2 -> 9
        3 -> 8
        4 -> 7
        5 -> 6
        6 -> 5
        7 -> 4
        8 -> 3
        9 -> 2
        10 -> 1
        else -> 0
    }
}

/**
 * Parsing helper to convert multiline text formatted standings into structured list with Booyahs, Kills, Place Points, and Total Points
 * Formats supported:
 * - "1 | Team Name | 14 Kills | ₹300" (Auto 1 Booyah if Rank 1)
 * - "1 | Team Name | 2 Booyah | 14 Kills | 26 Pts | ₹300"
 * - "1 | Team Name | 1 | 14 | 12 | 26 | ₹300" (Rank | Team | Booyah | Kills | Place | Total | Prize)
 * - "1 | Team Name | 14 | 12 | 26 | ₹300"
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
            val defaultPlace = getDefaultPlacePoints(rank)
            val defaultBooyah = if (rank == 1) 1 else 0

            when (parts.size) {
                2 -> {
                    result.add(
                        TeamRankEntry(
                            rank = rank,
                            teamName = team,
                            booyahs = defaultBooyah,
                            kills = "0 Kills",
                            killPoints = 0,
                            placePoints = defaultPlace,
                            totalPoints = defaultPlace,
                            prize = ""
                        )
                    )
                }
                3 -> {
                    val p2 = parts[2]
                    val isPrize = p2.startsWith("₹") || p2.startsWith("Rs", ignoreCase = true)
                    val killsStr = if (!isPrize) p2 else "0 Kills"
                    val prizeStr = if (isPrize) p2 else ""
                    val killsNum = killsStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                    val total = defaultPlace + killsNum
                    result.add(
                        TeamRankEntry(
                            rank = rank,
                            teamName = team,
                            booyahs = defaultBooyah,
                            kills = if (killsStr.contains("kill", ignoreCase = true)) killsStr else "$killsNum Kills",
                            killPoints = killsNum,
                            placePoints = defaultPlace,
                            totalPoints = total,
                            prize = prizeStr
                        )
                    )
                }
                4 -> {
                    val p2 = parts[2]
                    val p3 = parts[3]
                    val isP2Booyah = p2.contains("booyah", ignoreCase = true) || p2.contains("wwcd", ignoreCase = true)
                    val booyahNum = if (isP2Booyah) {
                        p2.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 1
                    } else defaultBooyah

                    val killsStr = if (isP2Booyah) p3 else p2
                    val killsNum = killsStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                    val isPrize = p3.startsWith("₹") || p3.startsWith("Rs", ignoreCase = true) || (!isP2Booyah && p3.contains("0") && !p3.contains("pt", ignoreCase = true))
                    val prizeStr = if (isPrize && !isP2Booyah) p3 else ""
                    val explicitPts = if (!isPrize && !isP2Booyah) p3.replace("[^0-9]".toRegex(), "").toIntOrNull() else null
                    val total = explicitPts ?: (defaultPlace + killsNum)
                    val placePts = if (explicitPts != null) (explicitPts - killsNum).coerceAtLeast(0) else defaultPlace

                    result.add(
                        TeamRankEntry(
                            rank = rank,
                            teamName = team,
                            booyahs = booyahNum,
                            kills = if (killsStr.contains("kill", ignoreCase = true)) killsStr else "$killsNum Kills",
                            killPoints = killsNum,
                            placePoints = placePts,
                            totalPoints = total,
                            prize = prizeStr
                        )
                    )
                }
                5 -> {
                    // Check if parts[2] is booyah count (e.g., "1 | Team Toxic | 2 Booyah | 14 Kills | ₹300")
                    val p2 = parts[2]
                    val p3 = parts[3]
                    val p4 = parts[4]
                    val isP2Booyah = p2.contains("booyah", ignoreCase = true) || p2.contains("wwcd", ignoreCase = true)

                    val booyahNum: Int
                    val killsStr: String
                    val killsNum: Int
                    val explicitPts: Int
                    val prizeStr: String

                    if (isP2Booyah) {
                        booyahNum = p2.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 1
                        killsStr = p3
                        killsNum = killsStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                        prizeStr = p4
                        explicitPts = defaultPlace + killsNum
                    } else {
                        booyahNum = defaultBooyah
                        killsStr = p2
                        killsNum = killsStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                        val ptsOrPlace = p3.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: (defaultPlace + killsNum)
                        explicitPts = ptsOrPlace
                        prizeStr = p4
                    }
                    val placePts = (explicitPts - killsNum).coerceAtLeast(0)

                    result.add(
                        TeamRankEntry(
                            rank = rank,
                            teamName = team,
                            booyahs = booyahNum,
                            kills = if (killsStr.contains("kill", ignoreCase = true)) killsStr else "$killsNum Kills",
                            killPoints = killsNum,
                            placePoints = placePts,
                            totalPoints = explicitPts,
                            prize = prizeStr
                        )
                    )
                }
                6 -> {
                    // Format: 1 | Team Toxic | 2 Booyah | 14 Kills | 26 Pts | ₹300
                    // OR: 1 | Team Toxic | 2 | 14 | 12 | ₹300
                    val p2 = parts[2]
                    val isP2Booyah = p2.contains("booyah", ignoreCase = true) || p2.contains("wwcd", ignoreCase = true)
                    val booyahNum = if (isP2Booyah) {
                        p2.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 1
                    } else {
                        // Could be: Rank | Team | BooyahCount | Kills | Place | Total
                        p2.toIntOrNull() ?: defaultBooyah
                    }

                    val killsStr = parts[3]
                    val killsNum = killsStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                    val p4 = parts[4]
                    val placePts = p4.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: defaultPlace
                    val totalPts = placePts + killsNum
                    val prizeStr = parts[5]

                    result.add(
                        TeamRankEntry(
                            rank = rank,
                            teamName = team,
                            booyahs = booyahNum,
                            kills = if (killsStr.contains("kill", ignoreCase = true)) killsStr else "$killsNum Kills",
                            killPoints = killsNum,
                            placePoints = placePts,
                            totalPoints = totalPts,
                            prize = prizeStr
                        )
                    )
                }
                else -> {
                    // 7 parts or more: Rank | Team | Booyahs | Kills | Place | Total | Prize
                    val booyahNum = parts[2].replace("[^0-9]".toRegex(), "").toIntOrNull() ?: defaultBooyah
                    val killsStr = parts[3]
                    val killsNum = killsStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                    val placePts = parts[4].replace("[^0-9]".toRegex(), "").toIntOrNull() ?: defaultPlace
                    val totalPts = parts[5].replace("[^0-9]".toRegex(), "").toIntOrNull() ?: (placePts + killsNum)
                    val prizeStr = parts[6]

                    result.add(
                        TeamRankEntry(
                            rank = rank,
                            teamName = team,
                            booyahs = booyahNum,
                            kills = if (killsStr.contains("kill", ignoreCase = true)) killsStr else "$killsNum Kills",
                            killPoints = killsNum,
                            placePoints = placePts,
                            totalPoints = totalPts,
                            prize = prizeStr
                        )
                    )
                }
            }
        } else {
            result.add(
                TeamRankEntry(
                    rank = index + 1,
                    teamName = line,
                    booyahs = if (index == 0) 1 else 0,
                    kills = "",
                    killPoints = 0,
                    placePoints = getDefaultPlacePoints(index + 1),
                    totalPoints = getDefaultPlacePoints(index + 1),
                    prize = ""
                )
            )
        }
    }
    return result
}

/**
 * Generate a high-resolution PNG Scorecard image and save directly to Gallery / Downloads
 */
private suspend fun exportOrDownloadScorecardImage(
    context: Context,
    match: MatchData,
    standings: List<TeamRankEntry>
) {
    withContext(Dispatchers.IO) {
        try {
            val bitmap = createScorecardBitmap(context, match, standings)
            val fileName = "Scorecard_${match.title.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${System.currentTimeMillis()}.png"
            var savedUri: Uri? = null

            // 1. Try saving to MediaStore (Pictures / Gallery)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Tournaments")
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    savedUri = uri
                }
            }

            // 2. Fallback to app external files or Downloads directory
            if (savedUri == null) {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                try {
                    savedUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } catch (e: Exception) {
                    savedUri = Uri.fromFile(file)
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Scorecard saved to Gallery & Downloads!", Toast.LENGTH_LONG).show()

                // Also provide instant share / open option
                savedUri?.let { uri ->
                    try {
                        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "image/png")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(viewIntent, "Open Scorecard Image"))
                    } catch (e: Exception) {
                        // View app not found, already saved
                    }
                }
            }
        } catch (e: Exception) {
            // Text fallback if graphic engine fails on older device
            withContext(Dispatchers.Main) {
                downloadScorecardSummary(context, match, standings)
            }
        }
    }
}

/**
 * Creates a high-definition esports scorecard banner bitmap (1080px wide)
 */
private fun createScorecardBitmap(
    context: Context,
    match: MatchData,
    standings: List<TeamRankEntry>
): Bitmap {
    val width = 1080
    val rows = if (standings.isNotEmpty()) standings.take(12).size else 1
    val headerHeight = 420
    val rowHeight = 90
    val footerHeight = 220
    val height = headerHeight + (rows * rowHeight) + footerHeight

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Dark Premium Background
    val bgPaint = Paint().apply {
        color = AndroidColor.parseColor("#090D14")
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Header Card / Top Gradient Box
    val headerBgPaint = Paint().apply {
        color = AndroidColor.parseColor("#0F1E16")
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), 340f, headerBgPaint)

    // Neon Accent Line
    val neonLinePaint = Paint().apply {
        color = AndroidColor.parseColor("#00E676")
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    canvas.drawLine(0f, 340f, width.toFloat(), 340f, neonLinePaint)

    // Header Title
    val titlePaint = Paint().apply {
        color = AndroidColor.WHITE
        textSize = 48f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText("OFFICIAL ESPORTS SCORECARD", 60f, 100f, titlePaint)

    // Tournament Name
    val tourneyPaint = Paint().apply {
        color = AndroidColor.parseColor("#00E676")
        textSize = 40f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText(match.title, 60f, 175f, tourneyPaint)

    // Meta (Map, Mode, Prize Pool)
    val metaPaint = Paint().apply {
        color = AndroidColor.parseColor("#CBD5E1")
        textSize = 28f
        isAntiAlias = true
    }
    canvas.drawText("MAP: ${match.map.uppercase()}  |  MODE: ${match.mode.uppercase()}  |  PRIZE: ${match.prize}", 60f, 240f, metaPaint)
    canvas.drawText("MATCH TIME: ${match.time}", 60f, 290f, metaPaint)

    // Table Header
    val tableHeaderBg = Paint().apply {
        color = AndroidColor.parseColor("#151C28")
        style = Paint.Style.FILL
    }
    canvas.drawRect(40f, 370f, (width - 40).toFloat(), 430f, tableHeaderBg)

    val thPaint = Paint().apply {
        color = AndroidColor.parseColor("#94A3B8")
        textSize = 21f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val thBooyahPaint = Paint().apply {
        color = AndroidColor.parseColor("#FFD700")
        textSize = 21f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText("RANK", 60f, 410f, thPaint)
    canvas.drawText("TEAM / PLAYER", 155f, 410f, thPaint)
    canvas.drawText("BOOYAH", 490f, 410f, thBooyahPaint)
    canvas.drawText("KILL PTS", 615f, 410f, thPaint)
    canvas.drawText("PLACE", 740f, 410f, thPaint)
    canvas.drawText("TOTAL", 845f, 410f, thPaint)
    canvas.drawText("PRIZE", 950f, 410f, thPaint)

    // Rows
    var curY = 460f
    val rankTextPaint = Paint().apply {
        textSize = 25f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val teamPaint = Paint().apply {
        color = AndroidColor.WHITE
        textSize = 25f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val booyahTextPaint = Paint().apply {
        color = AndroidColor.parseColor("#FFD700")
        textSize = 23f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val statPaint = Paint().apply {
        color = AndroidColor.parseColor("#CBD5E1")
        textSize = 23f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val totalPtsPaint = Paint().apply {
        color = AndroidColor.parseColor("#00E676")
        textSize = 25f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val prizePaint = Paint().apply {
        color = AndroidColor.parseColor("#FFD700")
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    if (standings.isNotEmpty()) {
        standings.take(12).forEach { entry ->
            val rowBg = Paint().apply {
                color = when (entry.rank) {
                    1 -> AndroidColor.parseColor("#1D2B1E")
                    2 -> AndroidColor.parseColor("#1A202C")
                    3 -> AndroidColor.parseColor("#1F1C18")
                    else -> AndroidColor.parseColor("#0F141E")
                }
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(40f, curY - 35f, (width - 40).toFloat(), curY + 40f, 12f, 12f, rowBg)

            rankTextPaint.color = when (entry.rank) {
                1 -> AndroidColor.parseColor("#FFD700")
                2 -> AndroidColor.parseColor("#C0C0C0")
                3 -> AndroidColor.parseColor("#CD7F32")
                else -> AndroidColor.WHITE
            }

            val rankPrefix = if (entry.rank == 1) "#1" else "#${entry.rank}"
            canvas.drawText(rankPrefix, 60f, curY + 15f, rankTextPaint)
            canvas.drawText(entry.teamName.take(16), 155f, curY + 15f, teamPaint)
            
            // Booyah count column
            val booyahStr = if (entry.booyahs > 0) "${entry.booyahs}" else "0"
            booyahTextPaint.color = if (entry.booyahs > 0) AndroidColor.parseColor("#FFD700") else AndroidColor.parseColor("#64748B")
            canvas.drawText(booyahStr, 515f, curY + 15f, booyahTextPaint)

            canvas.drawText("${entry.killPoints}", 635f, curY + 15f, statPaint)
            canvas.drawText("${entry.placePoints}", 755f, curY + 15f, statPaint)
            canvas.drawText("${entry.totalPoints}", 855f, curY + 15f, totalPtsPaint)
            canvas.drawText(entry.prize.ifBlank { "-" }, 950f, curY + 15f, prizePaint)

            curY += rowHeight
        }
    } else {
        teamPaint.color = AndroidColor.parseColor("#94A3B8")
        canvas.drawText("Scorecard results compiled in-app.", 220f, curY + 15f, teamPaint)
        curY += rowHeight
    }

    // Footer
    val footerPaint = Paint().apply {
        color = AndroidColor.parseColor("#64748B")
        textSize = 22f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Verified by Free Fire Tournaments & Esports Platform", (width / 2).toFloat(), curY + 60f, footerPaint)
    canvas.drawText("Play Daily Scrims • Win Real Cash Prizes", (width / 2).toFloat(), curY + 100f, footerPaint)

    return bitmap
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
        sb.append("====================================================\n")
        sb.append("      OFFICIAL ESPORTS TOURNAMENT SCORECARD\n")
        sb.append("====================================================\n")
        sb.append("Tournament : ${match.title}\n")
        sb.append("Map        : ${match.map}\n")
        sb.append("Mode       : ${match.mode}\n")
        sb.append("Time       : ${match.time}\n")
        sb.append("Prize Pool : ${match.prize}\n\n")
        sb.append("--- STANDINGS & POINTS LEADERBOARD ---\n")
        sb.append("RANK | TEAM NAME | BOOYAH | KILLS | PLACE PTS | TOTAL PTS | PRIZE\n")
        if (standings.isNotEmpty()) {
            standings.forEach { s ->
                val winTag = if (s.booyahs > 0) "[BOOYAH x${s.booyahs}] " else ""
                sb.append("$winTag#${s.rank} | ${s.teamName} | ${s.booyahs} Booyah | ${s.killPoints} Kills | ${s.placePoints} Place | ${s.totalPoints} Total | ${s.prize.ifBlank { "-" }}\n")
            }
        } else {
            sb.append("Admin Screenshot uploaded in app.\n")
        }
        if (match.pointsTableNotes.isNotBlank()) {
            sb.append("\nAdmin Notes: ${match.pointsTableNotes}\n")
        }
        sb.append("====================================================\n")
        sb.append("Generated by Free Fire Scrims App\n")

        FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }

        Toast.makeText(context, "Scorecard saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
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
    sb.append("*OFFICIAL SCORECARD - ${match.title}*\n")
    sb.append("Map: ${match.map} | Mode: ${match.mode}\n")
    sb.append("Prize Pool: ${match.prize}\n\n")

    if (standings.isNotEmpty()) {
        sb.append("*TOP STANDINGS & POINTS:*\n")
        standings.take(5).forEach { s ->
            val winTag = if (s.rank == 1) "[BOOYAH] " else ""
            sb.append("$winTag#${s.rank} ${s.teamName} -> ${s.killPoints} Kills | ${s.placePoints} Place | *${s.totalPoints} PTS* | ${s.prize.ifBlank { "Top 5" }}\n")
        }
    }

    if (match.pointsTableNotes.isNotBlank()) {
        sb.append("\nNotes: ${match.pointsTableNotes}\n")
    }

    sb.append("\nPlay Daily Scrims & Win Real Cash! Join now in Free Fire Esports App.")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Tournament Scorecard: ${match.title}")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share Scorecard"))
}
