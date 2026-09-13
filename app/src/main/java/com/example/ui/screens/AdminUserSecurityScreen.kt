package com.example.ui.screens
import com.example.ui.theme.AppColors

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
    val hasXBadge: Boolean = false,
    val role: String = "player",
    val isModerator: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserSecurityScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }

    var usersList by remember { mutableStateOf<List<BannedUserData>>(emptyList()) }
    var securityAlerts by remember { mutableStateOf<List<SecurityAlertData>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Users & Bans, 1 = Intrusion Alerts
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Ban Dialog State
    var selectedUserForBan by remember { mutableStateOf<BannedUserData?>(null) }
    var selectedBanType by remember { mutableStateOf("temporary") } // "temporary" or "permanent"
    var tempBanHours by remember { mutableStateOf("24") }
    var banReasonInput by remember { mutableStateOf("Terms violation / security check") }
    var isProcessingBan by remember { mutableStateOf(false) }

    // Edit Stats Dialog State
    var selectedUserForStats by remember { mutableStateOf<BannedUserData?>(null) }
    var editMatchesInput by remember { mutableStateOf("0") }
    var editWinsInput by remember { mutableStateOf("0") }
    var editKillsInput by remember { mutableStateOf("0") }
    var editWalletInput by remember { mutableStateOf("0") }
    var isSavingStats by remember { mutableStateOf(false) }

    // Delete User State
    var userToDelete by remember { mutableStateOf<BannedUserData?>(null) }
    var isDeletingUser by remember { mutableStateOf(false) }

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
                val role = doc.getString("role") ?: "player"
                val isModerator = doc.getBoolean("isModerator") ?: false

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
                    hasXBadge = hasXBadge,
                    role = role,
                    isModerator = isModerator
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
                "banReason" to "Intrusion / Tampering: ${alert.incidentType} from ${alert.deviceModel}",
                "banUntil" to 0L
            )
            db?.collection("users")?.document(alert.uid)?.update(updates)
                ?.addOnSuccessListener {
                    Toast.makeText(context, "User permanently banned (${alert.email.ifBlank { alert.uid }})", Toast.LENGTH_LONG).show()
                    loadUsers()
                }
                ?.addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to ban: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun toggleXBadge(user: BannedUserData) {
        if (db == null) return
        val newStatus = !user.hasXBadge
        db.collection("users").document(user.uid).update("hasXBadge", newStatus)
            .addOnSuccessListener {
                val msg = if (newStatus) "X Badge Granted to ${user.name}" else "X Badge Revoked for ${user.name}"
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
                Toast.makeText(context, "User ${user.name} banned ($banType)", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "User ${user.name} unbanned", Toast.LENGTH_SHORT).show()
            loadUsers()
        }
    }

    fun deleteUserAccount(user: BannedUserData) {
        if (db == null) return
        isDeletingUser = true
        db.collection("users").document(user.uid).delete()
            .addOnSuccessListener {
                isDeletingUser = false
                userToDelete = null
                Toast.makeText(context, "Account deleted: ${user.name} (${user.email})", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                isDeletingUser = false
                Toast.makeText(context, "Error deleting user: ${e.message}", Toast.LENGTH_SHORT).show()
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
                            "USER & SECURITY CONTROL",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0F14))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Tab Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF141722))
                    .border(1.dp, Color(0xFF23293A), RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                listOf(
                    "Users (${usersList.size})",
                    "Security Alerts (${securityAlerts.size})"
                ).forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF00E676) else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title,
                            color = if (isSelected) Color.Black else Color(0xFF8E92A4),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by user email or name...", color = Color(0xFF8E92A4)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E92A4)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF23293A),
                        focusedLabelColor = Color(0xFF00E676),
                        unfocusedLabelColor = Color(0xFF8E92A4),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF141722),
                        unfocusedContainerColor = Color(0xFF141722)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00E676))
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
                                    .background(Color(0xFF141722))
                                    .border(
                                        1.dp,
                                        if (isUserBanned) Color(0xFFFF5252).copy(alpha = 0.6f) else Color(0xFF23293A),
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
                                            Text(
                                                user.email.ifBlank { "UID: ${user.uid.take(10)}..." },
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            if (isUserBanned) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF2A1515))
                                                        .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        if (user.banType == "temporary") "TEMP BAN" else "PERM BAN",
                                                        color = Color(0xFFFF5252),
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF0D2517))
                                                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
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
                                            color = Color(0xFF8E92A4),
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
                                                    .background(Color(0xFF1A1D2B))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("M: ${user.totalMatches}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF1A1D2B))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("W: ${user.totalWins}", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF1A1D2B))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("K: ${user.totalKills}", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF1A1D2B))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("₹${user.realMoney}", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                        // [X] Badge Toggle Button
                                        IconButton(
                                            onClick = { toggleXBadge(user) },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (user.hasXBadge) Color(0xFF00E676) else Color(0xFF1A1D2B))
                                                .border(1.dp, if (user.hasXBadge) Color(0xFF00E676) else Color(0xFF23293A), RoundedCornerShape(8.dp))
                                        ) {
                                            Text(
                                                "X",
                                                color = if (user.hasXBadge) Color.Black else Color(0xFF8E92A4),
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
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
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
                                                    banReasonInput = "Terms violation / security check"
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("BAN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                            }
                                        }

                                        IconButton(
                                            onClick = { userToDelete = user },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2A1515))
                                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Account",
                                                tint = Color(0xFFFF5252),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: Hacker Intrusion Logs
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
                                "No unauthorized tamper attempts detected.",
                                color = Color(0xFF8E92A4),
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
                                    .background(Color(0xFF141722))
                                    .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
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
                                                    .background(Color(0xFFFF5252))
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
                                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF8E92A4))
                                        }
                                    }

                                    Text(
                                        "Target UID / User: ${alert.email.ifBlank { alert.uid }}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )

                                    Text(
                                        "Device: ${alert.deviceModel} (Android ${alert.androidVersion})",
                                        color = Color(0xFF8E92A4),
                                        fontSize = 11.sp
                                    )

                                    Text(
                                        alert.details,
                                        color = Color(0xFFC5C9D8),
                                        fontSize = 11.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(timeFormatted, color = Color(0xFF75798E), fontSize = 10.sp)

                                        Button(
                                            onClick = { banHackerDirectly(alert) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("PERM BAN USER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
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

    // Ban User Dialog
    if (selectedUserForBan != null) {
        val user = selectedUserForBan!!
        AlertDialog(
            onDismissRequest = { selectedUserForBan = null },
            containerColor = Color(0xFF141722),
            title = {
                Text("Ban User: ${user.name}", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Ban Duration & Reason:", color = Color(0xFF8E92A4), fontSize = 12.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedBanType == "temporary",
                            onClick = { selectedBanType = "temporary" },
                            label = { Text("Temporary Ban") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF9800),
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1A1D2B),
                                labelColor = Color(0xFF8E92A4)
                            )
                        )
                        FilterChip(
                            selected = selectedBanType == "permanent",
                            onClick = { selectedBanType = "permanent" },
                            label = { Text("Permanent Ban") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE11D48),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1A1D2B),
                                labelColor = Color(0xFF8E92A4)
                            )
                        )
                    }

                    if (selectedBanType == "temporary") {
                        OutlinedTextField(
                            value = tempBanHours,
                            onValueChange = { tempBanHours = it },
                            label = { Text("Ban Duration (in Hours)", color = Color(0xFF8E92A4)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF23293A)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = banReasonInput,
                        onValueChange = { banReasonInput = it },
                        label = { Text("Ban Reason", color = Color(0xFF8E92A4)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF23293A)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = tempBanHours.toIntOrNull() ?: 24
                        applyBan(user, selectedBanType, hours, banReasonInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    enabled = !isProcessingBan
                ) {
                    Text("CONFIRM BAN", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForBan = null }) {
                    Text("Cancel", color = Color(0xFF8E92A4))
                }
            }
        )
    }

    // Edit Stats Dialog
    if (selectedUserForStats != null) {
        val user = selectedUserForStats!!
        AlertDialog(
            onDismissRequest = { selectedUserForStats = null },
            containerColor = Color(0xFF141722),
            title = {
                Text("Edit Stats: ${user.name}", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editMatchesInput,
                        onValueChange = { editMatchesInput = it },
                        label = { Text("Total Matches", color = Color(0xFF8E92A4)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF23293A)
                        )
                    )
                    OutlinedTextField(
                        value = editWinsInput,
                        onValueChange = { editWinsInput = it },
                        label = { Text("Total Wins", color = Color(0xFF8E92A4)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF23293A)
                        )
                    )
                    OutlinedTextField(
                        value = editKillsInput,
                        onValueChange = { editKillsInput = it },
                        label = { Text("Total Kills", color = Color(0xFF8E92A4)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF23293A)
                        )
                    )
                    OutlinedTextField(
                        value = editWalletInput,
                        onValueChange = { editWalletInput = it },
                        label = { Text("Real Money (₹)", color = Color(0xFF8E92A4)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF23293A)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val m = editMatchesInput.toIntOrNull() ?: user.totalMatches
                        val w = editWinsInput.toIntOrNull() ?: user.totalWins
                        val k = editKillsInput.toIntOrNull() ?: user.totalKills
                        val wall = editWalletInput.toIntOrNull() ?: user.realMoney
                        isSavingStats = true
                        db?.collection("users")?.document(user.uid)?.update(
                            mapOf(
                                "totalMatches" to m,
                                "totalWins" to w,
                                "totalKills" to k,
                                "realMoney" to wall
                            )
                        )?.addOnSuccessListener {
                            isSavingStats = false
                            selectedUserForStats = null
                            Toast.makeText(context, "Stats updated successfully", Toast.LENGTH_SHORT).show()
                            loadUsers()
                        }?.addOnFailureListener { e ->
                            isSavingStats = false
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    enabled = !isSavingStats
                ) {
                    Text("SAVE STATS", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForStats = null }) {
                    Text("Cancel", color = Color(0xFF8E92A4))
                }
            }
        )
    }

    // Delete Account Dialog
    if (userToDelete != null) {
        val user = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            containerColor = Color(0xFF141722),
            title = {
                Text("Delete User Account?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to permanently remove ${user.name} (${user.email})? This action cannot be undone.",
                    color = Color(0xFF8E92A4),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { deleteUserAccount(user) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    enabled = !isDeletingUser
                ) {
                    Text("DELETE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel", color = Color(0xFF8E92A4))
                }
            }
        )
    }
}
