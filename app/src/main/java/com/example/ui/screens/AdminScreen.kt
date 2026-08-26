package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var totalMatches by remember { mutableStateOf(0) }
    var liveMatches by remember { mutableStateOf(0) }
    var totalUsers by remember { mutableStateOf(0) }
    var isLiveStreamActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("matches").addSnapshotListener { snap, _ ->
            if (snap != null) {
                totalMatches = snap.size()
                liveMatches = snap.documents.count { 
                    it.getString("status") == "Live" || it.getString("status") == "Ongoing" 
                }
            }
        }
        db.collection("users").addSnapshotListener { snap, _ ->
            if (snap != null) {
                totalUsers = snap.size()
            }
        }
        db.collection("settings").document("live_stream").addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                isLiveStreamActive = doc.getBoolean("isActive") == true
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0C0D12),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "ADMIN HQ",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Classy Frosted Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF14161F))
                    .border(1.dp, Color(0xFF262938), RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("COMMAND CENTER", color = Color(0xFF8E92A4), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Tournament & Room Engine", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isLiveStreamActive) Color(0xFF2A151A) else Color(0xFF1A1D27))
                                .border(1.dp, if (isLiveStreamActive) Color(0xFFFF003C).copy(alpha = 0.5f) else Color(0xFF333748), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                if (isLiveStreamActive) "🔴 STREAM ON" else "STREAM OFF",
                                color = if (isLiveStreamActive) Color(0xFFFF5252) else Color(0xFF8E92A4),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Metrics Grid (Frosted Glass Sub-Cards)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassStatCard("LIVE GAMES", liveMatches.toString(), Color(0xFFFF5252), Modifier.weight(1f))
                        GlassStatCard("TOTAL MATCHES", totalMatches.toString(), Color.White, Modifier.weight(1f))
                        GlassStatCard("PLAYERS", totalUsers.toString(), Color(0xFFFFD700), Modifier.weight(1f))
                    }
                }
            }

            Text("OPERATIONS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF75798E), letterSpacing = 1.5.sp)

            // 1. Create Match Card
            ClassyAdminActionCard(
                title = "Create New Tournament Match",
                subtitle = "Custom Maps (FF/BGMI), Solo/Duo/Squad slots, Prize Pool & Rules",
                icon = Icons.Default.AddCircleOutline,
                iconTint = Color(0xFFFFD700),
                onClick = { navController.navigate("admin_create_match") }
            )

            // 2. Manage Matches & Rooms (Slot details, ID/Pass & Live URL)
            ClassyAdminActionCard(
                title = "Manage Matches & Bookings",
                subtitle = "View registered teams & player names, release Room ID/Pass",
                icon = Icons.Default.Tune,
                iconTint = Color(0xFFFFFFFF),
                badge = "$totalMatches Total",
                onClick = { navController.navigate("admin_manage_matches") }
            )

            // 3. Live Stream Manager
            ClassyAdminActionCard(
                title = "Global Live Stream Link",
                subtitle = "Configure YouTube / Twitch live stream for all players",
                icon = Icons.Default.LiveTv,
                iconTint = Color(0xFFFF3366),
                badge = if (isLiveStreamActive) "Active" else "Standby",
                onClick = { navController.navigate("admin_live_stream") }
            )

            // 4. Player Wallets
            ClassyAdminActionCard(
                title = "Player Wallets & Cashout",
                subtitle = "Search players by email, add winnings & manual deposit updates",
                icon = Icons.Default.AccountBalanceWallet,
                iconTint = Color(0xFF00E676),
                onClick = { navController.navigate("admin_manage_wallets") }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun GlassStatCard(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F1117))
            .border(1.dp, Color(0xFF222532), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF75798E), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ClassyAdminActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    badge: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF14161F))
            .border(1.dp, Color(0xFF262938), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A1D28))
                        .border(1.dp, Color(0xFF2C3042), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, color = Color(0xFF8E92A4), fontSize = 12.sp, lineHeight = 16.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF202330))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(badge, color = Color(0xFFC0C4D6), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF5A5E72), modifier = Modifier.size(20.dp))
            }
        }
    }
}
