package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.FirebaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppUpdateScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }

    // APK Version Update State (Current App is Build 3, Version 1.2.1)
    var latestVersionCode by remember { mutableStateOf("3") }
    var latestVersionName by remember { mutableStateOf("1.2.1") }
    var apkDownloadUrl by remember { mutableStateOf("https://website-scrim-x-pro.vercel.app/") }
    var whatsNewText by remember { mutableStateOf("• Bug fixes & performance improvements\n• Enhanced tournament room speed\n• New instant cashout options") }
    var isForceUpdate by remember { mutableStateOf(false) }
    var allowLaterButton by remember { mutableStateOf(false) }
    var autoBanOutdatedUsers by remember { mutableStateOf(false) }

    // Live Patch & Remote Config State
    var isMaintenanceMode by remember { mutableStateOf(false) }
    var maintenanceMessage by remember { mutableStateOf("Server is currently undergoing scheduled maintenance. We'll be back shortly!") }
    var isAnnouncementActive by remember { mutableStateOf(true) }
    var announcementNotice by remember { mutableStateOf("🔥 Mega Free Fire & BGMI Tournament at 8 PM! Register slots now!") }
    var spinJackpot by remember { mutableStateOf("200") }
    var dailyFreeSpins by remember { mutableStateOf("2") }
    var watchVideoCoins by remember { mutableStateOf("10") }
    var dailyRewardCoins by remember { mutableStateOf("15") }
    var adminUpiId by remember { mutableStateOf("admin@upi") }
    var supportWhatsapp by remember { mutableStateOf("+919876543210") }
    var supportTelegram by remember { mutableStateOf("https://t.me/tournament_support") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    fun applyDoc(doc: com.google.firebase.firestore.DocumentSnapshot) {
        val rawCode = doc.get("latestVersionCode") ?: doc.get("versionCode") ?: doc.get("version_code") ?: doc.get("build")
        latestVersionCode = when (rawCode) {
            is Number -> rawCode.toInt().toString()
            is String -> rawCode.trim().ifBlank { "3" }
            else -> "3"
        }

        val rawName = doc.getString("latestVersionName") ?: doc.getString("versionName") ?: doc.getString("version")
        latestVersionName = rawName?.trim()?.ifBlank { "1.2.1" } ?: "1.2.1"

        apkDownloadUrl = doc.getString("apkDownloadUrl") ?: ""
        whatsNewText = doc.getString("whatsNew") ?: whatsNewText
        isForceUpdate = doc.getBoolean("isForceUpdate") ?: false
        allowLaterButton = doc.getBoolean("allowLaterButton") ?: false
        autoBanOutdatedUsers = doc.getBoolean("autoBanOutdatedUsers") ?: false

        isMaintenanceMode = doc.getBoolean("isMaintenanceMode") ?: false
        maintenanceMessage = doc.getString("maintenanceMessage") ?: maintenanceMessage
        isAnnouncementActive = doc.getBoolean("isAnnouncementActive") ?: true
        announcementNotice = doc.getString("announcementNotice") ?: announcementNotice
        spinJackpot = doc.getLong("spinJackpot")?.toString() ?: "200"
        dailyFreeSpins = doc.getLong("dailyFreeSpins")?.toString() ?: "2"
        watchVideoCoins = doc.getLong("watchVideoCoins")?.toString() ?: "10"
        dailyRewardCoins = doc.getLong("dailyRewardCoins")?.toString() ?: "15"
        adminUpiId = doc.getString("adminUpiId") ?: "admin@upi"
        supportWhatsapp = doc.getString("supportWhatsapp") ?: "+919876543210"
        supportTelegram = doc.getString("supportTelegram") ?: "https://t.me/tournament_support"
    }

    // Load initial settings with robust type parsing
    LaunchedEffect(Unit) {
        if (db != null) {
            db.collection("system_config").document("app_control").get()
                .addOnSuccessListener { doc ->
                    val targetDoc = if (doc != null && doc.exists()) doc else null
                    if (targetDoc != null) {
                        applyDoc(targetDoc)
                        isLoading = false
                    } else {
                        // Check settings/app_control as fallback
                        db.collection("settings").document("app_control").get()
                            .addOnSuccessListener { fallbackDoc ->
                                if (fallbackDoc != null && fallbackDoc.exists()) {
                                    applyDoc(fallbackDoc)
                                }
                                isLoading = false
                            }
                            .addOnFailureListener { isLoading = false }
                    }
                }
                .addOnFailureListener {
                    // Try fallback on failure too
                    db.collection("settings").document("app_control").get()
                        .addOnSuccessListener { fallbackDoc ->
                            if (fallbackDoc != null && fallbackDoc.exists()) {
                                applyDoc(fallbackDoc)
                            }
                            isLoading = false
                        }
                        .addOnFailureListener { isLoading = false }
                }
        } else {
            isLoading = false
        }
    }

    fun saveConfiguration() {
        if (db == null) {
            Toast.makeText(context, "Database connection unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        isSaving = true
        val targetCode = latestVersionCode.toIntOrNull() ?: 3
        val targetName = latestVersionName.trim().ifBlank { "1.2.1" }

        val data = hashMapOf<String, Any>(
            "latestVersionCode" to targetCode,
            "versionCode" to targetCode,
            "latestVersionName" to targetName,
            "versionName" to targetName,
            "apkDownloadUrl" to apkDownloadUrl.trim(),
            "whatsNew" to whatsNewText.trim(),
            "isForceUpdate" to isForceUpdate,
            "allowLaterButton" to allowLaterButton,
            "autoBanOutdatedUsers" to autoBanOutdatedUsers,
            "isMaintenanceMode" to isMaintenanceMode,
            "maintenanceMessage" to maintenanceMessage.trim(),
            "isAnnouncementActive" to isAnnouncementActive,
            "announcementNotice" to announcementNotice.trim(),
            "spinJackpot" to (spinJackpot.toIntOrNull() ?: 200),
            "dailyFreeSpins" to (dailyFreeSpins.toIntOrNull() ?: 2),
            "watchVideoCoins" to (watchVideoCoins.toIntOrNull() ?: 10),
            "dailyRewardCoins" to (dailyRewardCoins.toIntOrNull() ?: 15),
            "adminUpiId" to adminUpiId.trim(),
            "supportWhatsapp" to supportWhatsapp.trim(),
            "supportTelegram" to supportTelegram.trim(),
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        // Write to both paths for complete compatibility across rules and console
        db.collection("system_config").document("app_control").set(data)
        db.collection("settings").document("app_control").set(data)
            .addOnSuccessListener {
                isSaving = false
                Toast.makeText(context, "✅ Settings & Live Patch Updated Successfully!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                isSaving = false
                Toast.makeText(context, "⚠️ Saved with note: ${e.message}", Toast.LENGTH_SHORT).show()
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
                                .background(Color(0xFF00E5FF))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "APP UPDATE & LIVE PATCH",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00E5FF))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // VIP Admin Safety Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E3A8A).copy(alpha = 0.4f), Color(0xFF0F172A))
                            )
                        )
                        .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF60A5FA))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "ADMIN VIP PROTECTION ACTIVE",
                                color = Color(0xFF93C5FD),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Maintenance mode and Force Update never lock the Admin. You retain 100% control.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // ==========================================
                // SECTION 1: 📦 FULL APK UPDATE CONTROLLER
                // ==========================================
                Text(
                    "📦 1. FULL APK UPDATE CONTROLLER",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )

                // Diagnostics & Version Status Card
                val serverCodeInt = latestVersionCode.toIntOrNull() ?: 3
                val currentAppCode = remember(context) { AppControlViewModel.getInstalledVersionCode(context) }
                val currentAppName = remember(context) { AppControlViewModel.getInstalledVersionName(context) }
                val isOutdatedActive = serverCodeInt > currentAppCode

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOutdatedActive) Color(0xFF2E1218) else Color(0xFF0E2218)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isOutdatedActive) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFF00E676).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "📱 Installed App Version",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8E92A4)
                                )
                                Text(
                                    "$currentAppName (Build $currentAppCode)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "🔥 Firebase Live Setting",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8E92A4)
                                )
                                Text(
                                    "$latestVersionName (Build $latestVersionCode)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        Text(
                            text = when {
                                serverCodeInt > currentAppCode ->
                                    "⚠️ UPDATE ACTIVE FOR BUILD $currentAppCode: Firebase build ($serverCodeInt) is HIGHER than app ($currentAppCode). This causes all Build $currentAppCode users to receive an update dialog! Click '⚡ Sync to Build $currentAppCode' below to stop this prompt immediately."
                                serverCodeInt == currentAppCode ->
                                    "✅ PERFECT SYNC: Current App is Build $currentAppCode. Old users on Build 1 & 2 WILL see update prompt to get $currentAppName. Build $currentAppCode users will NOT see update prompt."
                                else ->
                                    "ℹ️ Firebase build code is $serverCodeInt (lower than current app build $currentAppCode). Update prompts are completely inactive for all users."
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = if (isOutdatedActive) Color(0xFFFF8A80) else Color(0xFFB9F6CA)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    latestVersionCode = currentAppCode.toString()
                                    latestVersionName = currentAppName
                                    isForceUpdate = false
                                    saveConfiguration()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "⚡ Sync Build $currentAppCode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    latestVersionCode = "1"
                                    latestVersionName = "1.0.0"
                                    isForceUpdate = false
                                    saveConfiguration()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262C3A)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "🛑 Stop Prompts (1)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    latestVersionCode = (currentAppCode + 1).toString()
                                    latestVersionName = "1.2.2"
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "🚀 Build ${currentAppCode + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF14161F))
                        .border(1.dp, Color(0xFF262938), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = latestVersionCode,
                                onValueChange = { latestVersionCode = it },
                                label = { Text("Version Code (e.g. 3)") },
                                placeholder = { Text("3") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                            OutlinedTextField(
                                value = latestVersionName,
                                onValueChange = { latestVersionName = it },
                                label = { Text("Version Name") },
                                placeholder = { Text("1.2.1") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                        }

                        OutlinedTextField(
                            value = apkDownloadUrl,
                            onValueChange = { apkDownloadUrl = it },
                            label = { Text("APK Download / Website Link") },
                            placeholder = { Text("https://yourwebsite.com/app.apk or Drive link") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = adminTextFieldColors(),
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color.Gray) },
                            trailingIcon = {
                                if (apkDownloadUrl.isNotBlank()) {
                                    IconButton(onClick = {
                                        try {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("APK Link", apkDownloadUrl.trim()))
                                            Toast.makeText(context, "📋 APK Link Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {}
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = Color(0xFF00E5FF))
                                    }
                                }
                            }
                        )

                        OutlinedTextField(
                            value = whatsNewText,
                            onValueChange = { whatsNewText = it },
                            label = { Text("What's New (Changelog for players)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = adminTextFieldColors()
                        )

                        // Force Update Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isForceUpdate) Color(0xFF2E1014) else Color(0xFF191B24))
                                .border(
                                    1.dp,
                                    if (isForceUpdate) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFF2B2E3D),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (isForceUpdate) "🚨 Force Update (Mandatory)" else "Optional Update Mode",
                                    color = if (isForceUpdate) Color(0xFFFF5252) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    if (isForceUpdate) "Users CANNOT enter app without downloading update." else "Standard update prompt.",
                                    color = Color(0xFF8E92A4),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isForceUpdate,
                                onCheckedChange = { isForceUpdate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFF3366)
                                )
                            )
                        }

                        // Allow Later Button Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (allowLaterButton) Color(0xFF0D2818) else Color(0xFF191B24))
                                .border(
                                    1.dp,
                                    if (allowLaterButton) Color(0xFF00E676).copy(alpha = 0.5f) else Color(0xFF2B2E3D),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Show 'Later / Skip' Button",
                                    color = if (allowLaterButton) Color(0xFF00E676) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    if (allowLaterButton) "Allowed: Users see 'Later' button and can skip." else "Hidden: Users get NO skip button unless permitted by you.",
                                    color = Color(0xFF8E92A4),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = allowLaterButton,
                                onCheckedChange = { allowLaterButton = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00E676)
                                )
                            )
                        }

                        // Auto-Ban Outdated APK Users Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (autoBanOutdatedUsers) Color(0xFF3B0764).copy(alpha = 0.4f) else Color(0xFF191B24))
                                .border(
                                    1.dp,
                                    if (autoBanOutdatedUsers) Color(0xFFA855F7).copy(alpha = 0.6f) else Color(0xFF2B2E3D),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "⚡ Auto-Ban Outdated APK Bypassers",
                                    color = if (autoBanOutdatedUsers) Color(0xFFD8B4FE) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    if (autoBanOutdatedUsers) "Enabled: Anyone attempting to bypass update with old APK gets auto temporary ban (24h)." else "Disabled: Just shows update blocker without ban.",
                                    color = Color(0xFF8E92A4),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = autoBanOutdatedUsers,
                                onCheckedChange = { autoBanOutdatedUsers = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFA855F7)
                                )
                            )
                        }
                    }
                }

                // ==========================================
                // SECTION 2: ⚡ LIVE PATCH & INSTANT CONFIG
                // ==========================================
                Text(
                    "⚡ 2. LIVE PATCH & INSTANT CONFIG (0s DOWNLOAD)",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF14161F))
                        .border(1.dp, Color(0xFF262938), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Maintenance Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMaintenanceMode) Color(0xFF331D08) else Color(0xFF191B24))
                                .border(
                                    1.dp,
                                    if (isMaintenanceMode) Color(0xFFFF9800).copy(alpha = 0.5f) else Color(0xFF2B2E3D),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (isMaintenanceMode) "🚧 Server Maintenance ON" else "Server Live (Normal)",
                                    color = if (isMaintenanceMode) Color(0xFFFFB74D) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    if (isMaintenanceMode) "All regular users see maintenance screen." else "App running smoothly for everyone.",
                                    color = Color(0xFF8E92A4),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isMaintenanceMode,
                                onCheckedChange = { isMaintenanceMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFF9800)
                                )
                            )
                        }

                        if (isMaintenanceMode) {
                            OutlinedTextField(
                                value = maintenanceMessage,
                                onValueChange = { maintenanceMessage = it },
                                label = { Text("Maintenance Message") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = adminTextFieldColors()
                            )
                        }

                        Divider(color = Color(0xFF262938))

                        // Live Marquee Announcement
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "📢 Live Announcement Ticker",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Shows moving red alert bar on Home Screen",
                                    color = Color(0xFF8E92A4),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isAnnouncementActive,
                                onCheckedChange = { isAnnouncementActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00E676)
                                )
                            )
                        }

                        if (isAnnouncementActive) {
                            OutlinedTextField(
                                value = announcementNotice,
                                onValueChange = { announcementNotice = it },
                                label = { Text("Notice Text") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = adminTextFieldColors()
                            )
                        }

                        Divider(color = Color(0xFF262938))

                        // Game Economy & Coin Rewards Patch
                        Text("🎮 Game Economy & Rewards Patch", color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = spinJackpot,
                                onValueChange = { spinJackpot = it },
                                label = { Text("Spin Jackpot (Coins)") },
                                placeholder = { Text("200") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                            OutlinedTextField(
                                value = dailyFreeSpins,
                                onValueChange = { dailyFreeSpins = it },
                                label = { Text("Daily Spins Count") },
                                placeholder = { Text("2") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = watchVideoCoins,
                                onValueChange = { watchVideoCoins = it },
                                label = { Text("Video Ad Reward (Coins)") },
                                placeholder = { Text("10") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                            OutlinedTextField(
                                value = dailyRewardCoins,
                                onValueChange = { dailyRewardCoins = it },
                                label = { Text("Daily Check-in (Coins)") },
                                placeholder = { Text("15") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                        }

                        Divider(color = Color(0xFF262938))

                        // Admin UPI & Support
                        Text("💳 Admin UPI & Support Links", color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        OutlinedTextField(
                            value = adminUpiId,
                            onValueChange = { adminUpiId = it },
                            label = { Text("Admin UPI ID for deposits") },
                            placeholder = { Text("yourupi@oksbi") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = adminTextFieldColors(),
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.Gray) }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = supportWhatsapp,
                                onValueChange = { supportWhatsapp = it },
                                label = { Text("WhatsApp Support") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                            OutlinedTextField(
                                value = supportTelegram,
                                onValueChange = { supportTelegram = it },
                                label = { Text("Telegram Link") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                        }
                    }
                }

                // ==========================================
                // SAVE & PUSH BUTTON
                // ==========================================
                Button(
                    onClick = { saveConfiguration() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("SAVING & PUSHING...", color = Color.Black, fontWeight = FontWeight.Black)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE & PUSH LIVE UPDATE TO ALL USERS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00E5FF),
    unfocusedBorderColor = Color(0xFF2C3042),
    focusedLabelColor = Color(0xFF00E5FF),
    unfocusedLabelColor = Color(0xFF8E92A4),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF00E5FF),
    focusedContainerColor = Color(0xFF0E1017),
    unfocusedContainerColor = Color(0xFF0E1017)
)
