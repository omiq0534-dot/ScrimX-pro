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

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.FirebaseHelper
import com.example.ui.components.AdminMasterBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }
    val auth = remember { FirebaseHelper.getAuth() }
    val currentUserEmail = auth?.currentUser?.email?.lowercase() ?: ""
    val isOwner = currentUserEmail == "omiq0534@gmail.com"
    var isModerator by remember { mutableStateOf(!isOwner) }

    var totalMatches by remember { mutableStateOf(0) }
    var liveMatches by remember { mutableStateOf(0) }
    var totalUsers by remember { mutableStateOf(0) }
    var isLiveStreamActive by remember { mutableStateOf(false) }
    var pendingQueriesCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        if (db != null) {
            val uid = auth?.currentUser?.uid
            if (uid != null) {
                db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val role = doc.getString("role") ?: "player"
                        val isMod = doc.getBoolean("isModerator") ?: false
                        isModerator = isMod || role == "moderator"
                    }
                }
            }

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
            db.collection("support_tickets").addSnapshotListener { snap, _ ->
                if (snap != null) {
                    pendingQueriesCount = snap.documents.count {
                        it.getString("status") == "Pending"
                    }
                }
            }
            db.collection("settings").document("live_stream").addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    isLiveStreamActive = doc.getBoolean("isActive") == true
                }
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
                                .background(if (isOwner) Color(0xFFFF0055) else Color(0xFF8B5CF6))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            if (isOwner) "👑 OWNER HQ" else "🛡️ MODERATOR HQ",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AdminMasterBadge(isOwner = isOwner, showClickInfo = true)
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
                            Text(
                                if (isOwner) "SUPREME COMMAND CENTER" else "STAFF & MODERATOR PORTAL",
                                color = Color(0xFF8E92A4),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                if (isOwner) "Tournament & System Engine" else "Tournament Match Host Engine",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
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

            // OWNER-EXCLUSIVE: STAFF MANAGEMENT BANNER
            if (isOwner) {
                Text("STAFF & ACCESS CONTROL", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF75798E), letterSpacing = 1.5.sp)
                ClassyAdminActionCard(
                    title = "Staff & Moderator Management",
                    subtitle = "Assign helper admins to create matches & release Room IDs on your behalf",
                    icon = Icons.Default.GroupAdd,
                    iconTint = Color(0xFF8B5CF6),
                    badge = "👑 Owner",
                    onClick = { navController.navigate("admin_staff_management") }
                )
            }

            Text("MATCH OPERATIONS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF75798E), letterSpacing = 1.5.sp)

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
                title = "Manage Matches & Rooms",
                subtitle = "View registered teams, release Room ID/Pass before match start",
                icon = Icons.Default.Tune,
                iconTint = Color(0xFFFFFFFF),
                badge = "$totalMatches Total",
                onClick = { navController.navigate("admin_manage_matches") }
            )

            // 3. Customer Helpdesk & User Queries
            ClassyAdminActionCard(
                title = "Helpdesk & User Queries",
                subtitle = "Read user questions, send official answers, setup contact channels & FAQs",
                icon = Icons.Default.HeadsetMic,
                iconTint = Color(0xFFFFD700),
                badge = if (pendingQueriesCount > 0) "$pendingQueriesCount New" else "Live",
                onClick = { navController.navigate("admin_support") }
            )

            // 4. Live Stream Manager
            ClassyAdminActionCard(
                title = "Global Live Stream Link",
                subtitle = "Configure YouTube / Twitch live stream for all players",
                icon = Icons.Default.LiveTv,
                iconTint = Color(0xFFFF3366),
                badge = if (isLiveStreamActive) "Active" else "Standby",
                onClick = { navController.navigate("admin_live_stream") }
            )

            // OWNER-EXCLUSIVE POWER TOOLS
            if (isOwner) {
                Text("OWNER SYSTEM CONTROLS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF75798E), letterSpacing = 1.5.sp)

                // 5. Player Wallets
                ClassyAdminActionCard(
                    title = "Player Wallets & Cashout",
                    subtitle = "Search players by email, add winnings & manual deposit updates",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = Color(0xFF00E676),
                    badge = "🔒 Owner",
                    onClick = { navController.navigate("admin_manage_wallets") }
                )

                // 6. App Update & Live Patch Control
                ClassyAdminActionCard(
                    title = "App Updates & Live Patch",
                    subtitle = "Push new APK versions, maintenance mode, live notices & rewards patch",
                    icon = Icons.Default.SystemUpdateAlt,
                    iconTint = Color(0xFF00E5FF),
                    badge = "🔒 Owner",
                    onClick = { navController.navigate("admin_app_update") }
                )

                // 7. User Security & Ban System
                ClassyAdminActionCard(
                    title = "User Security & Ban Radar",
                    subtitle = "Auto-detect cheats, ban violators, 24h suspensions & unban players",
                    icon = Icons.Default.Gavel,
                    iconTint = Color(0xFFFF3366),
                    badge = "🔒 Owner",
                    onClick = { navController.navigate("admin_user_security") }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131520))
                        .border(1.dp, Color(0xFF262938), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Logged in as Tournament Moderator. You have match hosting, room ID release, and helpdesk permissions.",
                            color = Color(0xFF8E92A4),
                            fontSize = 11.sp
                        )
                    }
                }
            }

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
