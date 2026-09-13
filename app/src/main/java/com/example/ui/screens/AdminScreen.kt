package com.example.ui.screens
import com.example.ui.theme.AppColors

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
import com.example.security.AppSecurityGuard
import com.example.ui.components.AdminMasterBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }
    val auth = remember { FirebaseHelper.getAuth() }
    val currentUserEmail = auth?.currentUser?.email?.lowercase() ?: ""
    val isOwner = remember(currentUserEmail) { AppSecurityGuard.isSuperOwner(currentUserEmail) }
    var isModerator by remember { mutableStateOf(false) }

    var totalMatches by remember { mutableStateOf(0) }
    var liveMatches by remember { mutableStateOf(0) }
    var totalUsers by remember { mutableStateOf(0) }
    var isLiveStreamActive by remember { mutableStateOf(false) }
    var pendingQueriesCount by remember { mutableStateOf(0) }
    var pendingRechargesCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        if (db != null) {
            val uid = auth?.currentUser?.uid
            if (uid != null) {
                db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val role = doc.getString("role") ?: "player"
                        val isMod = doc.getBoolean("isModerator") ?: false
                        val verifiedMod = isMod || role.equals("moderator", ignoreCase = true)
                        isModerator = verifiedMod

                        // If user is neither owner nor moderator, kick them out immediately and log alert!
                        if (!isOwner && !verifiedMod) {
                            AppSecurityGuard.logSecurityIncident(
                                db = db,
                                auth = auth,
                                incidentType = "UNAUTHORIZED_ADMIN_PANEL_BREACH",
                                details = "User attempted to open Admin Screen with email: $currentUserEmail"
                            )
                            Toast.makeText(context, "Access Denied: Security Violation Logged!", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                    } else if (!isOwner) {
                        navController.popBackStack()
                    }
                }
            } else if (!isOwner) {
                navController.popBackStack()
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
            db.collection("transactions")
                .whereEqualTo("type", "DATA_RECHARGE")
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener { snap, _ ->
                    if (snap != null) {
                        pendingRechargesCount = snap.size()
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
                            if (isOwner) "OWNER CONSOLE" else "MODERATOR CONSOLE",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 16.sp,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0F14))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sleek Obsidian + Neon Green Glass Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF161A24),
                                Color(0xFF0F121A)
                            )
                        )
                    )
                    .border(
                        1.2.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00E676).copy(alpha = 0.6f),
                                Color(0xFF202636)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
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
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                if (isOwner) "Tournament & System Engine" else "Tournament Match Host Engine",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLiveStreamActive) Color(0xFF2A151A) else Color(0xFF151922))
                                .border(1.dp, if (isLiveStreamActive) Color(0xFFFF003C).copy(alpha = 0.5f) else Color(0xFF00E676).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                if (isLiveStreamActive) "STREAM ACTIVE" else "STREAM STANDBY",
                                color = if (isLiveStreamActive) Color(0xFFFF5252) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Metrics Grid (Clean Dark Cyber Style)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassStatCard("LIVE GAMES", liveMatches.toString(), Color(0xFFFF5252), Modifier.weight(1f))
                        GlassStatCard("TOTAL MATCHES", totalMatches.toString(), Color.White, Modifier.weight(1f))
                        GlassStatCard("PLAYERS", totalUsers.toString(), Color(0xFF00E676), Modifier.weight(1f))
                    }
                }
            }

            // OWNER-EXCLUSIVE: STAFF MANAGEMENT BANNER
            if (isOwner) {
                Text("STAFF & ACCESS CONTROL", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.5.sp)
                ClassyAdminActionCard(
                    title = "Staff & Moderator Management",
                    subtitle = "Assign helper admins to create matches & release Room IDs on your behalf",
                    icon = Icons.Default.GroupAdd,
                    iconTint = Color(0xFF00E676),
                    badge = "Owner",
                    onClick = { navController.navigate("admin_staff_management") }
                )
            }

            Text("MATCH OPERATIONS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.5.sp)

            // 0. Push Notifications & Broadcast Hub
            ClassyAdminActionCard(
                title = "Broadcast & Push Hub",
                subtitle = "Send global alerts or Room ID/Pass directly to joined player phones",
                icon = Icons.Default.Campaign,
                iconTint = Color(0xFF00E676),
                badge = "Push Alerts",
                onClick = { navController.navigate("admin_broadcast_notifications") }
            )

            // 1. Create Match Card
            ClassyAdminActionCard(
                title = "Create New Tournament Match",
                subtitle = "Custom Maps (FF/BGMI), Solo/Duo/Squad slots, Prize Pool & Rules",
                icon = Icons.Default.AddCircleOutline,
                iconTint = Color(0xFF00E676),
                onClick = { navController.navigate("admin_create_match") }
            )

            // 2. Manage Matches & Rooms (Slot details, ID/Pass & Live URL)
            ClassyAdminActionCard(
                title = "Manage Matches & Rooms",
                subtitle = "View registered teams, release Room ID/Pass before match start",
                icon = Icons.Default.Tune,
                iconTint = Color(0xFF00E676),
                badge = "$totalMatches Total",
                onClick = { navController.navigate("admin_manage_matches") }
            )

            // 3. Customer Helpdesk & User Queries
            ClassyAdminActionCard(
                title = "Helpdesk & User Queries",
                subtitle = "Read user questions, send official answers, setup contact channels & FAQs",
                icon = Icons.Default.HeadsetMic,
                iconTint = Color(0xFF00E676),
                badge = if (pendingQueriesCount > 0) "$pendingQueriesCount New" else "Live",
                onClick = { navController.navigate("admin_support") }
            )

            // 4. Live Stream Manager
            ClassyAdminActionCard(
                title = "Global Live Stream Link",
                subtitle = "Configure YouTube / Twitch live stream for all players",
                icon = Icons.Default.LiveTv,
                iconTint = Color(0xFF00E676),
                badge = if (isLiveStreamActive) "Active" else "Standby",
                onClick = { navController.navigate("admin_live_stream") }
            )

            // OWNER-EXCLUSIVE POWER TOOLS
            if (isOwner) {
                Text("OWNER SYSTEM CONTROLS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF64748B), letterSpacing = 1.5.sp)

                // 5. Player Wallets
                ClassyAdminActionCard(
                    title = "Player Wallets & Cashout",
                    subtitle = "Search players by email, approve deposits & manage winnings",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = Color(0xFF00E676),
                    badge = "Owner",
                    onClick = { navController.navigate("admin_manage_wallets") }
                )

                // 5b. Mobile Data Recharge Requests
                ClassyAdminActionCard(
                    title = "Mobile Data Recharge Requests",
                    subtitle = "Approve pending Jio & Airtel data booster recharges & copy mobile numbers",
                    icon = Icons.Default.Bolt,
                    iconTint = Color(0xFF00E676),
                    badge = if (pendingRechargesCount > 0) "$pendingRechargesCount PENDING" else "Mobile Top-up",
                    onClick = { navController.navigate("admin_manage_wallets") }
                )

                // 6. App Update & Live Patch Control
                ClassyAdminActionCard(
                    title = "App Updates & Live Patch",
                    subtitle = "Push new APK versions, maintenance mode, live notices & rewards patch",
                    icon = Icons.Default.SystemUpdateAlt,
                    iconTint = Color(0xFF00E676),
                    badge = "Owner",
                    onClick = { navController.navigate("admin_app_update") }
                )

                // 7. User Security & Ban System
                ClassyAdminActionCard(
                    title = "User Security & Ban Radar",
                    subtitle = "Auto-detect violations, suspend accounts & manage security whitelist",
                    icon = Icons.Default.Gavel,
                    iconTint = Color(0xFF00E676),
                    badge = "Owner",
                    onClick = { navController.navigate("admin_user_security") }
                )

                // 8. Unity Ads Monetization Control
                ClassyAdminActionCard(
                    title = "Unity Ads & Monetization Control",
                    subtitle = "Configure Game ID (6183190), Rewarded Video Coins, Placements & Test Mode",
                    icon = Icons.Default.MonetizationOn,
                    iconTint = Color(0xFF00E676),
                    badge = "Revenue",
                    onClick = { navController.navigate("admin_unity_ads") }
                )

                // 9. Store Codes & Stock Manager
                ClassyAdminActionCard(
                    title = "Store Codes & Stock Manager",
                    subtitle = "Add Google Play redeem codes, VIP passes, set item limits & coin prices",
                    icon = Icons.Default.CardGiftcard,
                    iconTint = Color(0xFF00E676),
                    badge = "Rewards",
                    onClick = { navController.navigate("admin_store_codes") }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141720))
                        .border(1.dp, Color(0xFF222838), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        "Logged in as Tournament Moderator. You have match hosting, room ID release, and helpdesk permissions.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
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
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141722))
            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = valueColor, fontSize = 17.sp, fontWeight = FontWeight.Black)
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141722))
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00E676).copy(alpha = 0.25f),
                        Color(0xFF222838)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF00E676).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, color = Color(0xFF94A3B8), fontSize = 11.5.sp, lineHeight = 15.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1C2230))
                            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(badge, color = Color(0xFF00E676), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
            }
        }
    }
}
