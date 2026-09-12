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
        containerColor = Color(0xFFF9FAFB),
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
                            color = Color(0xFF111827),
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AdminMasterBadge(isOwner = isOwner, showClickInfo = true)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF111827))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9FAFB))
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
                    .background(Color(0xFF111319))
                    .border(1.dp, Color(0xFF262A38), RoundedCornerShape(22.dp))
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
                                color = Color(0xFF9CA3AF),
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
                                .background(if (isLiveStreamActive) Color(0xFF2A151A) else Color(0xFF1E212D))
                                .border(1.dp, if (isLiveStreamActive) Color(0xFFFF003C).copy(alpha = 0.5f) else Color(0xFF333748), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                if (isLiveStreamActive) "🔴 STREAM ON" else "STREAM OFF",
                                color = if (isLiveStreamActive) Color(0xFFFF5252) else Color(0xFF9CA3AF),
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
                Text("STAFF & ACCESS CONTROL", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B5563), letterSpacing = 1.5.sp)
                ClassyAdminActionCard(
                    title = "Staff & Moderator Management",
                    subtitle = "Assign helper admins to create matches & release Room IDs on your behalf",
                    icon = Icons.Default.GroupAdd,
                    iconTint = Color(0xFF8B5CF6),
                    badge = "👑 Owner",
                    onClick = { navController.navigate("admin_staff_management") }
                )
            }

            Text("MATCH OPERATIONS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B5563), letterSpacing = 1.5.sp)

            // 0. Push Notifications & Broadcast Hub
            ClassyAdminActionCard(
                title = "Push Notifications & Broadcast Hub",
                subtitle = "Send Global alerts or Room ID/Pass to joined players + Instant phone test push",
                icon = Icons.Default.Campaign,
                iconTint = Color(0xFFFF0055),
                badge = "📢 Push Alerts",
                onClick = { navController.navigate("admin_broadcast_notifications") }
            )

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
                Text("OWNER SYSTEM CONTROLS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF4B5563), letterSpacing = 1.5.sp)

                // 5. Player Wallets
                ClassyAdminActionCard(
                    title = "Player Wallets & Cashout",
                    subtitle = "Search players by email, add winnings & manual deposit updates",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = Color(0xFF00E676),
                    badge = "🔒 Owner",
                    onClick = { navController.navigate("admin_manage_wallets") }
                )

                // 5b. Mobile Data Recharge Requests
                ClassyAdminActionCard(
                    title = "Mobile Data Recharge Requests",
                    subtitle = "Approve pending Jio & Airtel data booster recharges & copy mobile numbers",
                    icon = Icons.Default.Bolt,
                    iconTint = Color(0xFFFF5252),
                    badge = if (pendingRechargesCount > 0) "⚡ $pendingRechargesCount PENDING" else "⚡ Mobile Top-up",
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

                // 8. Unity Ads Monetization Control
                ClassyAdminActionCard(
                    title = "Unity Ads & Monetization Control",
                    subtitle = "Configure Game ID (6183190), Rewarded Video Coins, Placements & Test Mode",
                    icon = Icons.Default.MonetizationOn,
                    iconTint = Color(0xFFFFD700),
                    badge = "💰 Revenue",
                    onClick = { navController.navigate("admin_unity_ads") }
                )

                // 9. Store Codes & Stock Manager
                ClassyAdminActionCard(
                    title = "Store Codes & Stock Manager",
                    subtitle = "Add real Google Play codes, VIP passes, set item limits & coin prices",
                    icon = Icons.Default.CardGiftcard,
                    iconTint = Color(0xFF00E5FF),
                    badge = "🎁 Rewards",
                    onClick = { navController.navigate("admin_store_codes") }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF111319))
                        .border(1.dp, Color(0xFF262A38), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Logged in as Tournament Moderator. You have match hosting, room ID release, and helpdesk permissions.",
                            color = Color(0xFF9CA3AF),
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
            .background(Color(0xFF1A1D27))
            .border(1.dp, Color(0xFF262A38), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
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
            .background(Color(0xFF111319))
            .border(1.dp, Color(0xFF262A38), RoundedCornerShape(18.dp))
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
                    Text(subtitle, color = Color(0xFF9CA3AF), fontSize = 12.sp, lineHeight = 16.sp)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E212D))
                            .border(1.dp, Color(0xFF2E3348), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(badge, color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
            }
        }
    }
}
