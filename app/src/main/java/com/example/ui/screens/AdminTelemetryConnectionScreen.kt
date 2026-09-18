package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard
import com.example.utils.TelemetrySyncEngine
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTelemetryConnectionScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseHelper.getFirestore() }
    val auth = remember { FirebaseHelper.getAuth() }

    val currentUserEmail = auth?.currentUser?.email?.lowercase() ?: ""
    val isOwner = remember(currentUserEmail) { AppSecurityGuard.isSuperOwner(currentUserEmail) }

    // Telemetry Auth State
    var connectionKey by remember { mutableStateOf("SX-TEL-INITIALIZING...") }
    var isTelemetryEnabled by remember { mutableStateOf(true) }
    var syncIntervalSec by remember { mutableStateOf(45) }
    var keyStatus by remember { mutableStateOf("ACTIVE") }
    var keyGeneratedAt by remember { mutableStateOf("Recently") }

    // Telemetry Client Handshake State (from /telemetry_connection/status)
    var clientStatus by remember { mutableStateOf("STANDBY") }
    var lastPingTime by remember { mutableStateOf("Waiting for connection...") }
    var clientDevice by remember { mutableStateOf("None") }
    var clientName by remember { mutableStateOf("ScrimX Telemetry App") }
    var totalReadsCount by remember { mutableStateOf(0L) }
    var isClientOnline by remember { mutableStateOf(false) }

    // Live Metrics Preview
    var liveUsers by remember { mutableStateOf(0) }
    var liveOnline by remember { mutableStateOf(0) }
    var liveMatches by remember { mutableStateOf(0) }
    var liveRevenue by remember { mutableStateOf(0.0) }
    var isSyncingNow by remember { mutableStateOf(false) }

    // Dialogs
    var showRegenerateDialog by remember { mutableStateOf(false) }
    var showRevokeDialog by remember { mutableStateOf(false) }

    // Real-time Firestore Listeners
    LaunchedEffect(Unit) {
        if (db == null) return@LaunchedEffect

        // 1. Listen for Auth & Secret Key Config
        db.collection("system_config").document("telemetry_auth")
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    connectionKey = doc.getString("apiKey") ?: "SX-TEL-98421-SECURE"
                    isTelemetryEnabled = doc.getBoolean("isEnabled") ?: true
                    syncIntervalSec = (doc.getLong("syncIntervalSeconds") ?: 45L).toInt()
                    keyStatus = doc.getString("status") ?: "ACTIVE"
                    val timestamp = doc.getLong("generatedTimestamp") ?: 0L
                    if (timestamp > 0L) {
                        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        keyGeneratedAt = sdf.format(Date(timestamp))
                    }
                } else {
                    // Create default initial key if not present
                    val defaultKey = "SX-TEL-${Random.nextInt(10000, 99999)}-SECURE"
                    val initialData = hashMapOf<String, Any>(
                        "apiKey" to defaultKey,
                        "isEnabled" to true,
                        "syncIntervalSeconds" to 45,
                        "status" to "ACTIVE",
                        "generatedTimestamp" to System.currentTimeMillis(),
                        "generatedBy" to currentUserEmail,
                        "createdDate" to FieldValue.serverTimestamp()
                    )
                    db.collection("system_config").document("telemetry_auth")
                        .set(initialData, SetOptions.merge())
                }
            }

        // 2. Listen for ScrimX Telemetry App Handshake & Heartbeat
        db.collection("telemetry_connection").document("status")
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val rawStatus = doc.getString("status") ?: "IDLE"
                    clientDevice = doc.getString("deviceInfo") ?: "Android Monitor"
                    clientName = doc.getString("clientName") ?: "ScrimX Telemetry App"
                    totalReadsCount = doc.getLong("totalReadsCount") ?: 0L
                    val lastPing = doc.getLong("lastPingTimestamp") ?: 0L

                    val diff = System.currentTimeMillis() - lastPing
                    if (lastPing > 0L && diff < 90_000L) { // active within 90s
                        isClientOnline = true
                        clientStatus = "ONLINE"
                        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                        lastPingTime = "Active (${sdf.format(Date(lastPing))})"
                    } else if (lastPing > 0L) {
                        isClientOnline = false
                        clientStatus = "IDLE / SLEEP"
                        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        lastPingTime = "Last seen: ${sdf.format(Date(lastPing))}"
                    } else {
                        isClientOnline = false
                        clientStatus = rawStatus
                        lastPingTime = "No pings received yet"
                    }
                }
            }

        // 3. Listen for Telemetry Live Data Snapshot
        db.collection("telemetry").document("overview")
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    liveUsers = (doc.getLong("totalUsers") ?: 0L).toInt()
                    liveOnline = (doc.getLong("onlineUsers") ?: 0L).toInt()
                    liveMatches = (doc.getLong("activeMatches") ?: 0L).toInt()
                }
            }

        db.collection("financials").document("overview")
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    liveRevenue = doc.getDouble("grossRevenue") ?: (doc.getLong("grossRevenue")?.toDouble() ?: 0.0)
                }
            }
    }

    Scaffold(
        containerColor = Color(0xFF0D0F14),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Telemetry App Bridge",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            "Link ScrimX Telemetry App via Secret Key",
                            fontSize = 11.sp,
                            color = Color(0xFF00E676)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isSyncingNow = true
                                TelemetrySyncEngine.performSync()
                                Toast.makeText(context, "Telemetry Broadcast Synced!", Toast.LENGTH_SHORT).show()
                                isSyncingNow = false
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Force Sync",
                            tint = if (isSyncingNow) Color(0xFF00E676) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF12151D))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LIVE CONNECTION STATUS BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141722))
                    .border(
                        1.dp,
                        if (isClientOnline) Color(0xFF00E676).copy(alpha = 0.6f) else Color(0xFF222838),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isClientOnline) Color(0xFF00E676)
                                        else if (keyStatus == "REVOKED" || !isTelemetryEnabled) Color(0xFFFF5252)
                                        else Color(0xFFFFB300)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isClientOnline) "TELEMETRY APP LINKED"
                                else if (!isTelemetryEnabled) "BROADCAST DISABLED"
                                else if (keyStatus == "REVOKED") "KEY REVOKED"
                                else "AWAITING CONNECTION",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = if (isClientOnline) Color(0xFF00E676)
                                else if (!isTelemetryEnabled || keyStatus == "REVOKED") Color(0xFFFF5252)
                                else Color(0xFFFFB300)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1C2230))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (isClientOnline) "LIVE HANDSHAKE" else "IDLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isClientOnline) Color(0xFF00E676) else Color(0xFF94A3B8)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF222838))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TARGET CLIENT", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(clientName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("LAST ACTIVITY", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(lastPingTime, fontSize = 12.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // 2. SECRET CONNECTION KEY CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141722))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFF00E676).copy(alpha = 0.3f), Color(0xFF222838))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SECRET CONNECTION KEY",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            "Status: $keyStatus",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (keyStatus == "ACTIVE") Color(0xFF00E676) else Color(0xFFFF5252)
                        )
                    }

                    // Key Display Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B0D12))
                            .border(1.dp, Color(0xFF222838), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = connectionKey,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (keyStatus == "ACTIVE") Color(0xFF00E676) else Color(0xFF94A3B8)
                            )

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("ScrimX Telemetry Key", connectionKey)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Key Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy Key",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Text(
                        "Generated: $keyGeneratedAt • Enter this key into the ScrimX Telemetry App to authenticate data stream.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )

                    // Action Buttons (Generate New Key / Revoke)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showRegenerateDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Key", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showRevokeDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Revoke Key", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. MASTER BROADCAST CONTROLS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141722))
                    .border(1.dp, Color(0xFF222838), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "MASTER BROADCAST SETTINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.sp
                    )

                    // Toggle Telemetry Live Stream
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Live Telemetry Broadcast", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Push users, matches & revenue metrics automatically", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Switch(
                            checked = isTelemetryEnabled,
                            onCheckedChange = { isChecked ->
                                isTelemetryEnabled = isChecked
                                db?.collection("system_config")?.document("telemetry_auth")?.update(
                                    mapOf(
                                        "isEnabled" to isChecked,
                                        "status" to (if (isChecked) "ACTIVE" else "DISABLED")
                                    )
                                )
                                Toast.makeText(context, if (isChecked) "Broadcast Enabled" else "Broadcast Paused", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFF00E676),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF2A3142)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF222838))

                    // Sync Interval selector
                    Text("Auto-Sync Interval", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val intervals = listOf(30 to "30s", 45 to "45s", 60 to "1m", 300 to "5m")
                        intervals.forEach { (sec, label) ->
                            val isSelected = syncIntervalSec == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF00E676) else Color(0xFF1A1F2C))
                                    .border(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF2A3142), RoundedCornerShape(8.dp))
                                    .clickable {
                                        syncIntervalSec = sec
                                        db?.collection("system_config")?.document("telemetry_auth")?.update("syncIntervalSeconds", sec)
                                        Toast.makeText(context, "Sync Interval: $label", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. LIVE METRICS PREVIEW (WHAT TELEMETRY APP SEES)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141722))
                    .border(1.dp, Color(0xFF222838), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "LIVE DATA FEED PREVIEW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )

                        Text("Firestore: /telemetry", fontSize = 10.sp, color = Color(0xFF00E676), fontFamily = FontFamily.Monospace)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreviewStatBox(title = "USERS", value = "$liveUsers", modifier = Modifier.weight(1f))
                        PreviewStatBox(title = "ONLINE", value = "$liveOnline", modifier = Modifier.weight(1f), valueColor = Color(0xFF00E676))
                        PreviewStatBox(title = "MATCHES", value = "$liveMatches", modifier = Modifier.weight(1f))
                        PreviewStatBox(title = "REVENUE", value = "₹${liveRevenue.toInt()}", modifier = Modifier.weight(1f), valueColor = Color(0xFFFFD700))
                    }
                }
            }

            // 5. STEP-BY-STEP INSTRUCTIONS CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF10131B))
                    .border(1.dp, Color(0xFF1E2536), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "HOW TO CONNECT 'SCRIMX TELEMETRY' APP",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    InstructionStepItem(
                        number = "1",
                        title = "Copy Secret Key",
                        desc = "Copy the active key above ($connectionKey) from this ScrimX app."
                    )
                    InstructionStepItem(
                        number = "2",
                        title = "Open ScrimX Telemetry App",
                        desc = "Open the ScrimX Telemetry monitoring app on your device."
                    )
                    InstructionStepItem(
                        number = "3",
                        title = "Paste Key & Connect",
                        desc = "Enter this key into the Telemetry app settings. It will immediately authenticate and show live analytics."
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog: Generate New Key
    if (showRegenerateDialog) {
        AlertDialog(
            onDismissRequest = { showRegenerateDialog = false },
            title = { Text("Generate New Key?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Generating a new key will require you to enter the new key into the ScrimX Telemetry App. Continue?",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newKey = "SX-TEL-${Random.nextInt(10000, 99999)}-${Random.nextInt(100, 999)}"
                        val updatePayload = mapOf(
                            "apiKey" to newKey,
                            "status" to "ACTIVE",
                            "generatedTimestamp" to System.currentTimeMillis(),
                            "generatedBy" to currentUserEmail
                        )
                        db?.collection("system_config")?.document("telemetry_auth")?.set(updatePayload, SetOptions.merge())
                        showRegenerateDialog = false
                        Toast.makeText(context, "New Key Generated!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("Generate", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegenerateDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF181C26)
        )
    }

    // Dialog: Revoke Key
    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            title = { Text("Revoke Access Key?", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will immediately cut off data streaming to ScrimX Telemetry App until a new key is authorized.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        db?.collection("system_config")?.document("telemetry_auth")?.update(
                            mapOf(
                                "status" to "REVOKED",
                                "isEnabled" to false
                            )
                        )
                        showRevokeDialog = false
                        Toast.makeText(context, "Key Revoked! Stream stopped.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Revoke Now", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF181C26)
        )
    }
}

@Composable
fun PreviewStatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0D0F14))
            .border(1.dp, Color(0xFF222838), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun InstructionStepItem(number: String, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF00E676).copy(alpha = 0.15f))
                .border(1.dp, Color(0xFF00E676), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, fontSize = 11.5.sp, color = Color(0xFF94A3B8), lineHeight = 15.sp)
        }
    }
}
