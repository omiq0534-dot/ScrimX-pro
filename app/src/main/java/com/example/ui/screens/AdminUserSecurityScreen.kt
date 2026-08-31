package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.ui.components.XBadge
import com.example.ui.components.XBadgeSize

data class SecurityAlertData(
    val id: String = "",
    val uid: String = "",
    val email: String = "",
    val incidentType: String = "",
    val details: String = "",
    val timestamp: Long = 0L,
    val deviceModel: String = "",
    val androidVersion: Int = 0,
    val status: String = "CRITICAL_SUSPECT"
)

data class BannedUserData(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val isBanned: Boolean = false,
    val banType: String = "none", // "none", "temporary", "permanent"
    val banReason: String = "",
    val banUntil: Long = 0L, // timestamp in ms for temporary ban
    val totalMatches: Int = 0,
    val totalWins: Int = 0,
    val totalKills: Int = 0,
    val realMoney: Int = 0,
    val hasXBadge: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserSecurityScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }

    var usersList by remember { mutableStateOf<List<BannedUserData>>(emptyList()) }
    var securityAlerts by remember { mutableStateOf<List<SecurityAlertData>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Users & Bans, 1 = Hacker Intrusion Alerts
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Ban Dialog State
    var selectedUserForBan by remember { mutableStateOf<BannedUserData?>(null) }
    var selectedBanType by remember { mutableStateOf("temporary") } // "temporary" or "permanent"
    var tempBanHours by remember { mutableStateOf("24") }
    var banReasonInput by remember { mutableStateOf("Using unauthorized outdated APK / Terms violation") }
    var isProcessingBan by remember { mutableStateOf(false) }

    // Edit Stats Dialog State
    var selectedUserForStats by remember { mutableStateOf<BannedUserData?>(null) }
    var editMatchesInput by remember { mutableStateOf("0") }
    var editWinsInput by remember { mutableStateOf("0") }
    var editKillsInput by remember { mutableStateOf("0") }
    var editWalletInput by remember { mutableStateOf("0") }
    var isSavingStats by remember { mutableStateOf(false) }

    fun loadUsers() {
        isLoading = true
        db?.collection("users")?.get()?.addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { doc ->
                val uid = doc.id
                val email = doc.getString("email") ?: ""
                val name = doc.getString("name") ?: "Player"
                val isBanned = doc.getBoolean("isBanned") ?: false
                val banType = doc.getString("banType") ?: if (isBanned) "permanent" else "none"
                val banReason = doc.getString("banReason") ?: ""
                val banUntil = doc.getLong("banUntil") ?: 0L
                val totalMatches = doc.getLong("totalMatches")?.toInt() ?: 0
                val totalWins = doc.getLong("totalWins")?.toInt() ?: 0
                val totalKills = doc.getLong("totalKills")?.toInt() ?: 0
                val realMoney = doc.getLong("realMoney")?.toInt() ?: 0
                val hasXBadge = doc.getBoolean("hasXBadge") ?: false

                BannedUserData(
                    uid = uid,
                    email = email,
                    name = name,
                    isBanned = isBanned,
                    banType = banType,
                    banReason = banReason,
                    banUntil = banUntil,
                    totalMatches = totalMatches,
                    totalWins = totalWins,
                    totalKills = totalKills,
                    realMoney = realMoney,
                    hasXBadge = hasXBadge
                )
            }
            usersList = list
            isLoading = false
        }?.addOnFailureListener {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadUsers()
        db?.collection("security_alerts")
            ?.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            ?.addSnapshotListener { snap, _ ->
                if (snap != null) {
                    securityAlerts = snap.documents.mapNotNull { doc ->
                        val id = doc.id
                        val uid = doc.getString("uid") ?: ""
                        val email = doc.getString("email") ?: ""
                        val incidentType = doc.getString("incidentType") ?: "SECURITY_ALERT"
                        val details = doc.getString("details") ?: ""
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        val deviceModel = doc.getString("deviceModel") ?: "Unknown Device"
                        val androidVersion = doc.getLong("androidVersion")?.toInt() ?: 0
                        val status = doc.getString("status") ?: "CRITICAL_SUSPECT"
                        SecurityAlertData(
                            id = id,
                            uid = uid,
                            email = email,
                            incidentType = incidentType,
                            details = details,
                            timestamp = timestamp,
                            deviceModel = deviceModel,
                            androidVersion = androidVersion,
                            status = status
                        )
                    }
                }
            }
    }

    fun dismissAlert(alertId: String) {
        db?.collection("security_alerts")?.document(alertId)?.delete()
            ?.addOnSuccessListener {
                Toast.makeText(context, "Security Alert Cleared", Toast.LENGTH_SHORT).show()
            }
    }

    fun banHackerDirectly(alert: SecurityAlertData) {
        if (alert.uid.isBlank() && alert.email.isBlank()) {
            Toast.makeText(context, "Cannot ban: No UID/Email present", Toast.LENGTH_SHORT).show()
            return
        }
        if (alert.uid.isNotBlank()) {
            val updates = hashMapOf<String, Any>(
                "isBanned" to true,
                "banType" to "permanent",
                "banReason" to "Hardware Intrusion / Tampering: ${alert.incidentType} from ${alert.deviceModel}",
                "banUntil" to 0L
            )
            db?.collection("users")?.document(alert.uid)?.update(updates)
                ?.addOnSuccessListener {
                    Toast.makeText(context, "🔨 Hacker permanently banned! (${alert.email.ifBlank { alert.uid }})", Toast.LENGTH_LONG).show()
                    loadUsers()
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to ban: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun triggerTestAlertSimulation() {
        val auth = FirebaseHelper.getAuth()
        AppSecurityGuard.logSecurityIncident(
            db = db,
            auth = auth,
            incidentType = "TEST_SIMULATED_BREACH",
            details = "Manual intrusion test simulated by Owner from Admin Console"
        )
        Toast.makeText(context, "🧪 Test Hacker Intrusion alert generated! Check the Alerts tab.", Toast.LENGTH_LONG).show()
    }

    fun toggleXBadge(user: BannedUserData) {
        if (db == null) return
        val newStatus = !user.hasXBadge
        db.collection("users").document(user.uid).update("hasXBadge", newStatus)
            .addOnSuccessListener {
                val msg = if (newStatus) "👑 [X] Badge Granted to ${user.name}!" else "❌ [X] Badge Revoked for ${user.name}"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun applyBan(user: BannedUserData, banType: String, hours: Int, reason: String) {
        if (db == null) return
        isProcessingBan = true
        val banUntilTimestamp = if (banType == "temporary") {
            System.currentTimeMillis() + (hours * 3600 * 1000L)
        } else {
            0L
        }

        val updates = hashMapOf<String, Any>(
            "isBanned" to true,
            "banType" to banType,
            "banReason" to reason.trim(),
            "banUntil" to banUntilTimestamp
        )

        db.collection("users").document(user.uid).update(updates)
            .addOnSuccessListener {
                isProcessingBan = false
                selectedUserForBan = null
                Toast.makeText(context, "User ${user.name} banned successfully ($banType)", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                isProcessingBan = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun unbanUser(user: BannedUserData) {
        if (db == null) return
        db.collection("users").document(user.uid).update(
            mapOf(
                "isBanned" to false,
                "banType" to "none",
                "banReason" to "",
                "banUntil" to 0L
            )
        ).addOnSuccessListener {
            Toast.makeText(context, "User ${user.name} unbanned!", Toast.LENGTH_SHORT).show()
            loadUsers()
        }
    }

    val filteredList = remember(usersList, searchQuery) {
        if (searchQuery.isBlank()) usersList
        else usersList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.uid.contains(searchQuery, ignoreCase = true)
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
                                .background(Color(0xFFFF3366))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "SECURITY & USER BAN CONTROL",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { loadUsers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
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
                .padding(horizontal = 16.dp)
        ) {
            // Security Info Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF3B0764).copy(alpha = 0.5f), Color(0xFF1E1B4B))
                        )
                    )
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("ANTI-CHEAT & SECURITY ENFORCER", color = Color(0xFFDDD6FE), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        Text("Live hacker detection, device fingerprinting, and 1-click ban enforcer.", color = Color(0xFFC4B5FD), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF14161F),
                contentColor = Color(0xFFFF3366),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFFF3366)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("USERS (${usersList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    },
                    selectedContentColor = Color(0xFFFF3366),
                    unselectedContentColor = Color(0xFF8E92A4)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = if (securityAlerts.isNotEmpty()) Color(0xFFFF0055) else Color(0xFF8E92A4), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "🚨 HACKER LOGS (${securityAlerts.size})",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (selectedTab == 1) Color(0xFFFF0055) else if (securityAlerts.isNotEmpty()) Color(0xFFFF5252) else Color(0xFF8E92A4)
                            )
                        }
                    },
                    selectedContentColor = Color(0xFFFF0055),
                    unselectedContentColor = Color(0xFF8E92A4)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by user Gmail or name...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E92A4)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF3366),
                    unfocusedBorderColor = Color(0xFF262938),
                    focusedLabelColor = Color(0xFFFF3366),
                    unfocusedLabelColor = Color(0xFF8E92A4),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF14161F),
                    unfocusedContainerColor = Color(0xFF14161F)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF3366))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(filteredList, key = { it.uid }) { user ->
                        val isUserBanned = user.isBanned && (user.banType != "temporary" || user.banUntil > System.currentTimeMillis())
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isUserBanned) Color(0xFF260D12) else Color(0xFF14161F))
                                .border(
                                    1.dp,
                                    if (isUserBanned) Color(0xFFFF3366).copy(alpha = 0.6f) else Color(0xFF262938),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (user.hasXBadge) {
                                            XBadge(size = XBadgeSize.MINI, isAnimated = false, showClickInfo = true)
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        // Primary Identifier: GMAIL
                                        Text(
                                            user.email.ifBlank { "User UID: ${user.uid.take(10)}..." },
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (isUserBanned) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFFF0055))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    if (user.banType == "temporary") "TEMP BAN" else "PERMANENT BAN",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    "ACTIVE",
                                                    color = Color(0xFF00E676),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        "Player Name: ${user.name.ifBlank { "Player" }}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    // User Live Stats Row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF202638))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("⚔️ M: ${user.totalMatches}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF202638))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("🏆 W: ${user.totalWins}", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF202638))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("🎯 K: ${user.totalKills}", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF202638))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("💰 ₹${user.realMoney}", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (isUserBanned && user.banReason.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Reason: ${user.banReason}",
                                            color = Color(0xFFFF8A80),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 👑 [X] Badge Toggle Button
                                    IconButton(
                                        onClick = { toggleXBadge(user) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (user.hasXBadge) Color(0xFFFFD700) else Color(0xFF1E2230))
                                            .border(1.dp, if (user.hasXBadge) Color(0xFFFF9100) else Color(0xFF374151), RoundedCornerShape(8.dp))
                                    ) {
                                        Text(
                                            "X",
                                            color = if (user.hasXBadge) Color.Black else Color(0xFF94A3B8),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            selectedUserForStats = user
                                            editMatchesInput = user.totalMatches.toString()
                                            editWinsInput = user.totalWins.toString()
                                            editKillsInput = user.totalKills.toString()
                                            editWalletInput = user.realMoney.toString()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("STATS", fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    }

                                    if (isUserBanned) {
                                        Button(
                                            onClick = { unbanUser(user) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("UNBAN", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                selectedUserForBan = user
                                                selectedBanType = "temporary"
                                                tempBanHours = "24"
                                                banReasonInput = "Using unauthorized outdated APK / Terms violation"
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("BAN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: Hacker Intrusion Logs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "LIVE INTRUSION SENSORS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )

                    Button(
                        onClick = { triggerTestAlertSimulation() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🧪 Test Simulator", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (securityAlerts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("SYSTEM CLEAN & SECURE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "No unauthorized tamper attempts detected. Press 'Test Simulator' above to simulate a live hacker detection!",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(securityAlerts, key = { it.id }) { alert ->
                            val timeFormatted = remember(alert.timestamp) {
                                try {
                                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))
                                } catch (e: Exception) {
                                    "Just now"
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF230C14))
                                    .border(1.dp, Color(0xFFFF0055).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFF0055))
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                alert.incidentType,
                                                color = Color(0xFFFF5252),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = { dismissAlert(alert.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF8E92A4), modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    // Device & Hardware Info
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF14161F))
                                            .padding(10.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "Device: ${alert.deviceModel.ifBlank { "Unknown Android Device" }} (API ${alert.androidVersion})",
                                                    color = Color(0xFFFFD700),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                "📧 Email: ${alert.email.ifBlank { "Not Logged In / Fake DEX" }}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            Text(
                                                "🔑 UID: ${alert.uid}",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )

                                            Text(
                                                "🕒 Time: $timeFormatted",
                                                color = Color(0xFF64748B),
                                                fontSize = 10.sp
                                            )

                                            if (alert.details.isNotBlank()) {
                                                Text(
                                                    "📝 Details: ${alert.details}",
                                                    color = Color(0xFFFF8A80),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    // Quick Ban Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { banHackerDirectly(alert) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("🔨 1-Click Ban Hacker", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Ban Action Dialog
    if (selectedUserForBan != null) {
        val target = selectedUserForBan!!
        AlertDialog(
            onDismissRequest = { if (!isProcessingBan) selectedUserForBan = null },
            containerColor = Color(0xFF181B26),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFF3366))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "BAN GMAIL: ${target.email.ifBlank { target.uid.take(10) }}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            "Current Name: ${target.name}",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Select Ban Duration & Type:", color = Color(0xFFC4C8D8), fontSize = 12.sp)

                    // Ban Type Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedBanType = "temporary" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedBanType == "temporary") Color(0xFFFF9800).copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedBanType == "temporary") Color(0xFFFF9800) else Color(0xFF33384C)
                            )
                        ) {
                            Text("TEMPORARY", color = if (selectedBanType == "temporary") Color(0xFFFFB74D) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { selectedBanType = "permanent" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedBanType == "permanent") Color(0xFFFF0055).copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedBanType == "permanent") Color(0xFFFF0055) else Color(0xFF33384C)
                            )
                        ) {
                            Text("PERMANENT", color = if (selectedBanType == "permanent") Color(0xFFFF5252) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    if (selectedBanType == "temporary") {
                        OutlinedTextField(
                            value = tempBanHours,
                            onValueChange = { tempBanHours = it },
                            label = { Text("Ban Duration (in Hours)") },
                            placeholder = { Text("24") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9800),
                                unfocusedBorderColor = Color(0xFF33384C),
                                focusedLabelColor = Color(0xFFFF9800),
                                unfocusedLabelColor = Color(0xFF8E92A4),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = banReasonInput,
                        onValueChange = { banReasonInput = it },
                        label = { Text("Ban Reason (Shown to player)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF3366),
                            unfocusedBorderColor = Color(0xFF33384C),
                            focusedLabelColor = Color(0xFFFF3366),
                            unfocusedLabelColor = Color(0xFF8E92A4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = tempBanHours.toIntOrNull() ?: 24
                        applyBan(target, selectedBanType, hours, banReasonInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedBanType == "permanent") Color(0xFFFF0055) else Color(0xFFFF9800)),
                    enabled = !isProcessingBan
                ) {
                    Text("CONFIRM BAN", color = Color.White, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForBan = null }, enabled = !isProcessingBan) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }

    // Edit Player Stats & Wallet Dialog
    if (selectedUserForStats != null) {
        val target = selectedUserForStats!!
        AlertDialog(
            onDismissRequest = { if (!isSavingStats) selectedUserForStats = null },
            containerColor = Color(0xFF181B26),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "MANAGE STATS & WALLET",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Text(
                            target.email.ifBlank { target.name },
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Adjust verified stats & match records:", color = Color(0xFFC4C8D8), fontSize = 12.sp)

                    OutlinedTextField(
                        value = editMatchesInput,
                        onValueChange = { editMatchesInput = it },
                        label = { Text("Total Matches Played") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF33384C),
                            focusedLabelColor = Color(0xFFFFD700),
                            unfocusedLabelColor = Color(0xFF8E92A4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = editWinsInput,
                        onValueChange = { editWinsInput = it },
                        label = { Text("Total Wins (Booyahs / Chicken)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF33384C),
                            focusedLabelColor = Color(0xFFFFD700),
                            unfocusedLabelColor = Color(0xFF8E92A4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = editKillsInput,
                        onValueChange = { editKillsInput = it },
                        label = { Text("Total Kills 🎯") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF3366),
                            unfocusedBorderColor = Color(0xFF33384C),
                            focusedLabelColor = Color(0xFFFF3366),
                            unfocusedLabelColor = Color(0xFF8E92A4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = editWalletInput,
                        onValueChange = { editWalletInput = it },
                        label = { Text("Real Money Balance (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF33384C),
                            focusedLabelColor = Color(0xFF00E676),
                            unfocusedLabelColor = Color(0xFF8E92A4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (db == null) return@Button
                        isSavingStats = true
                        val matchesVal = editMatchesInput.toIntOrNull() ?: 0
                        val winsVal = editWinsInput.toIntOrNull() ?: 0
                        val killsVal = editKillsInput.toIntOrNull() ?: 0
                        val walletVal = editWalletInput.toIntOrNull() ?: 0

                        val updates = hashMapOf<String, Any>(
                            "totalMatches" to matchesVal,
                            "totalWins" to winsVal,
                            "totalKills" to killsVal,
                            "realMoney" to walletVal
                        )

                        db.collection("users").document(target.uid).update(updates)
                            .addOnSuccessListener {
                                isSavingStats = false
                                selectedUserForStats = null
                                Toast.makeText(context, "Stats updated successfully!", Toast.LENGTH_SHORT).show()
                                loadUsers()
                            }
                            .addOnFailureListener { e ->
                                isSavingStats = false
                                Toast.makeText(context, "Error updating stats: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    enabled = !isSavingStats
                ) {
                    Text("SAVE STATS", color = Color.Black, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForStats = null }, enabled = !isSavingStats) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }
}
