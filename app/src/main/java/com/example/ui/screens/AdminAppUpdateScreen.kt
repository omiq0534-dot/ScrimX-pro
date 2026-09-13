package com.example.ui.screens
import com.example.ui.theme.AppColors

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

    // APK Version Update State (Current App is Build 5, Version 1.4.0)
    var latestVersionCode by remember { mutableStateOf("4") }
    var latestVersionName by remember { mutableStateOf("1.4.0") }
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
    var newUserRealMoneyBonus by remember { mutableStateOf("5") }
    var newUserAppMoneyBonus by remember { mutableStateOf("50") }
    var adminUpiId by remember { mutableStateOf("admin@upi") }
    var supportWhatsapp by remember { mutableStateOf("+919876543210") }
    var supportTelegram by remember { mutableStateOf("https://t.me/tournament_support") }

    // New Live Patch & Remote Switch System State
    var patchTag by remember { mutableStateOf("v1.4.0-LIVE") }
    var patchNotes by remember { mutableStateOf("All server systems operational & low-ping matchmaking active.") }
    var isLivePatchActive by remember { mutableStateOf(true) }
    var isRegistrationEnabled by remember { mutableStateOf(true) }
    var isWithdrawalsEnabled by remember { mutableStateOf(true) }
    var isSpinWheelEnabled by remember { mutableStateOf(true) }
    var isWatchAdsEnabled by remember { mutableStateOf(true) }
    var cacheBustTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    fun applyDoc(doc: com.google.firebase.firestore.DocumentSnapshot) {
        val rawCode = doc.get("latestVersionCode") ?: doc.get("versionCode") ?: doc.get("version_code") ?: doc.get("build")
        latestVersionCode = when (rawCode) {
            is Number -> rawCode.toInt().toString()
            is String -> rawCode.trim().ifBlank { "4" }
            else -> "4"
        }

        val rawName = doc.getString("latestVersionName") ?: doc.getString("versionName") ?: doc.getString("version")
        latestVersionName = rawName?.trim()?.ifBlank { "1.4.0" } ?: "1.4.0"

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
        newUserRealMoneyBonus = doc.getLong("newUserRealMoneyBonus")?.toString() ?: "5"
        newUserAppMoneyBonus = doc.getLong("newUserAppMoneyBonus")?.toString() ?: "50"
        adminUpiId = doc.getString("adminUpiId") ?: "admin@upi"
        supportWhatsapp = doc.getString("supportWhatsapp") ?: "+919876543210"
        supportTelegram = doc.getString("supportTelegram") ?: "https://t.me/tournament_support"

        patchTag = doc.getString("patchTag") ?: "v1.4.0-LIVE"
        patchNotes = doc.getString("patchNotes") ?: patchNotes
        isLivePatchActive = doc.getBoolean("isLivePatchActive") ?: true
        isRegistrationEnabled = doc.getBoolean("isRegistrationEnabled") ?: true
        isWithdrawalsEnabled = doc.getBoolean("isWithdrawalsEnabled") ?: true
        isSpinWheelEnabled = doc.getBoolean("isSpinWheelEnabled") ?: true
        isWatchAdsEnabled = doc.getBoolean("isWatchAdsEnabled") ?: true
        cacheBustTimestamp = doc.getLong("cacheBustTimestamp") ?: System.currentTimeMillis()
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
        val targetCode = latestVersionCode.toIntOrNull() ?: 4
        val targetName = latestVersionName.trim().ifBlank { "1.4.0" }

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
            "newUserRealMoneyBonus" to (newUserRealMoneyBonus.toIntOrNull() ?: 5),
            "newUserAppMoneyBonus" to (newUserAppMoneyBonus.toIntOrNull() ?: 50),
            "adminUpiId" to adminUpiId.trim(),
            "supportWhatsapp" to supportWhatsapp.trim(),
            "supportTelegram" to supportTelegram.trim(),
            "patchTag" to patchTag.trim(),
            "patchNotes" to patchNotes.trim(),
            "isLivePatchActive" to isLivePatchActive,
            "isRegistrationEnabled" to isRegistrationEnabled,
            "isWithdrawalsEnabled" to isWithdrawalsEnabled,
            "isSpinWheelEnabled" to isSpinWheelEnabled,
            "isWatchAdsEnabled" to isWatchAdsEnabled,
            "cacheBustTimestamp" to cacheBustTimestamp,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0F14))
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
                CircularProgressIndicator(color = Color(0xFF00E676))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // VIP Admin Safety Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF141F1A), Color(0xFF0F141A))
                            )
                        )
                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF00E676))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "ADMIN VIP PROTECTION ACTIVE",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Maintenance mode and Force Update never lock the Admin. You retain 100% control.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Diagnostics & Version Status Card
                val serverCodeInt = latestVersionCode.toIntOrNull() ?: 3
                val currentAppCode = remember(context) { AppControlViewModel.getInstalledVersionCode(context) }
                val currentAppName = remember(context) { AppControlViewModel.getInstalledVersionName(context) }
                val isOutdatedActive = serverCodeInt > currentAppCode

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOutdatedActive) Color(0xFF241418) else Color(0xFF141D18)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isOutdatedActive) Color(0xFFFF5252).copy(alpha = 0.5f) else Color(0xFF00E676).copy(alpha = 0.4f)
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
                                    "Installed App Version",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
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
                                    "Firebase Live Setting",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    "$latestVersionName (Build $latestVersionCode)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF23293A))

                        Text(
                            text = when {
                                serverCodeInt >= currentAppCode ->
                                    "TARGET BUILD: $serverCodeInt ($latestVersionName). Purane users ko update prompt dikhega. Jo user naya build install kar chuke hain unhe koi popup nahi aayega."
                                else ->
                                    "Update prompts are currently turned off. No user will see update popups."
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF00E676)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    latestVersionCode = currentAppCode.toString()
                                    latestVersionName = currentAppName
                                    isForceUpdate = false
                                    saveConfiguration()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Push Update: Build $currentAppCode",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2330)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Turn Off Updates",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }
                    }
                }

                // CARD 1: APK VERSION & RELEASE BUILDS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdminCardHeader(
                            icon = Icons.Default.SystemUpdate,
                            iconTint = Color(0xFF00E676),
                            title = "1. APK VERSION & RELEASE BUILDS",
                            subtitle = "Target version numbers & direct APK download link for players"
                        )

                        HorizontalDivider(color = Color(0xFF23293A))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = latestVersionCode,
                                onValueChange = { latestVersionCode = it },
                                label = { Text("Version Code (e.g. 4)") },
                                placeholder = { Text("4") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                            OutlinedTextField(
                                value = latestVersionName,
                                onValueChange = { latestVersionName = it },
                                label = { Text("Version Name") },
                                placeholder = { Text("1.4.0") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                        }

                        OutlinedTextField(
                            value = apkDownloadUrl,
                            onValueChange = { apkDownloadUrl = it },
                            label = { Text("APK Download / Website Link") },
                            placeholder = { Text("https://website-scrim-x-pro.vercel.app/") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = adminTextFieldColors(),
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF64748B)) },
                            trailingIcon = {
                                if (apkDownloadUrl.isNotBlank()) {
                                    IconButton(onClick = {
                                        try {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("APK Link", apkDownloadUrl.trim()))
                                            Toast.makeText(context, "APK Link Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {}
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = Color(0xFF00E676))
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
                    }
                }

                // CARD 2: UPDATE POLICY & ENFORCEMENT
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdminCardHeader(
                            icon = Icons.Default.Security,
                            iconTint = Color(0xFFFF5252),
                            title = "2. UPDATE POLICY & ENFORCEMENT",
                            subtitle = "Manage mandatory blockers, skip permissions and cheat protection"
                        )

                        HorizontalDivider(color = Color(0xFF23293A))

                        AdminSwitchRow(
                            title = if (isForceUpdate) "Force Update (Mandatory)" else "Optional Update Mode",
                            subtitle = if (isForceUpdate) "Users CANNOT enter app without downloading update." else "Standard non-blocking update dialog.",
                            checked = isForceUpdate,
                            onCheckedChange = { isForceUpdate = it },
                            activeColor = Color(0xFFFF3366),
                            activeContainerColor = Color(0xFF2E1014)
                        )

                        AdminSwitchRow(
                            title = "Show 'Later / Skip' Button",
                            subtitle = if (allowLaterButton) "Allowed: Users see 'Later' button and can skip update." else "Hidden: Users get NO skip button.",
                            checked = allowLaterButton,
                            onCheckedChange = { allowLaterButton = it },
                            activeColor = Color(0xFF00E676),
                            activeContainerColor = Color(0xFF0D2818)
                        )

                        AdminSwitchRow(
                            title = "Auto-Ban Outdated APK Bypassers",
                            subtitle = if (autoBanOutdatedUsers) "Active: Anyone bypassing update with old APK gets auto temporary suspension." else "Disabled: Just shows update blocker without ban.",
                            checked = autoBanOutdatedUsers,
                            onCheckedChange = { autoBanOutdatedUsers = it },
                            activeColor = Color(0xFF00E676),
                            activeContainerColor = Color(0xFF0D2818)
                        )
                    }
                }

                // CARD 3: LIVE PATCH & HOTFIX SYSTEM
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdminCardHeader(
                            icon = Icons.Default.Bolt,
                            iconTint = Color(0xFF00E676),
                            title = "3. LIVE PATCH & HOTFIX SYSTEM",
                            subtitle = "Instant over-the-air hotfix deployment with 0 seconds download required"
                        )

                        HorizontalDivider(color = Color(0xFF23293A))

                        OutlinedTextField(
                            value = patchTag,
                            onValueChange = { patchTag = it },
                            label = { Text("Live Patch Tag (e.g. v1.4.0-HOTFIX-P1)") },
                            placeholder = { Text("v1.4.0-LIVE") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = adminTextFieldColors(),
                            leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null, tint = Color(0xFF00E676)) }
                        )

                        OutlinedTextField(
                            value = patchNotes,
                            onValueChange = { patchNotes = it },
                            label = { Text("Hotfix Notes / Live Status") },
                            placeholder = { Text("All server systems operational & matchmaking active.") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = adminTextFieldColors()
                        )

                        AdminSwitchRow(
                            title = "Show In-Game Live Patch Status Banner",
                            subtitle = if (isLivePatchActive) "Active: Displays green live patch status badge on player screen." else "Hidden: Patch banner is hidden.",
                            checked = isLivePatchActive,
                            onCheckedChange = { isLivePatchActive = it },
                            activeColor = Color(0xFF00E676),
                            activeContainerColor = Color(0xFF0D2818)
                        )

                        Button(
                            onClick = {
                                cacheBustTimestamp = System.currentTimeMillis()
                                Toast.makeText(context, "Cache-Bust timestamp refreshed! Click SAVE to broadcast.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2230))
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FORCE RE-SYNC ALL CLIENT CACHES", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }

                // CARD 4: REMOTE FEATURE TOGGLES
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdminCardHeader(
                            icon = Icons.Default.ToggleOn,
                            iconTint = Color(0xFF00E676),
                            title = "4. REMOTE FEATURE TOGGLES",
                            subtitle = "Instantly enable or disable individual app features without restarting"
                        )

                        HorizontalDivider(color = Color(0xFF23293A))

                        AdminSwitchRow(
                            title = "Tournament Registrations",
                            subtitle = if (isRegistrationEnabled) "Active: Players can join Free Fire & BGMI matches." else "Paused: Registrations temporarily locked.",
                            checked = isRegistrationEnabled,
                            onCheckedChange = { isRegistrationEnabled = it },
                            activeColor = Color(0xFF00E676),
                            activeContainerColor = Color(0xFF0D2818)
                        )

                        AdminSwitchRow(
                            title = "Real Cash Withdrawals",
                            subtitle = if (isWithdrawalsEnabled) "Active: Players can submit UPI withdrawal requests." else "Paused: Cashouts temporarily held.",
                            checked = isWithdrawalsEnabled,
                            onCheckedChange = { isWithdrawalsEnabled = it },
                            activeColor = Color(0xFF00E676),
                            activeContainerColor = Color(0xFF0D2818)
                        )

                        AdminSwitchRow(
                            title = "Lucky Spin Wheel",
                            subtitle = if (isSpinWheelEnabled) "Active: Daily spin wheel is open for all players." else "Paused: Spin wheel temporarily disabled.",
                            checked = isSpinWheelEnabled,
                            onCheckedChange = { isSpinWheelEnabled = it },
                            activeColor = Color(0xFF00E676),
                            activeContainerColor = Color(0xFF0D2818)
                        )

                        AdminSwitchRow(
                            title = "Watch & Earn Video Ads",
                            subtitle = if (isWatchAdsEnabled) "Active: Players earn coins for watching rewarded ads." else "Paused: Video ads reward is paused.",
                            checked = isWatchAdsEnabled,
                            onCheckedChange = { isWatchAdsEnabled = it },
                            activeColor = Color(0xFF00E676),
                            activeContainerColor = Color(0xFF0D2818)
                        )
                    }
                }

                // CARD 5: SERVER MAINTENANCE LOCK
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdminCardHeader(
                            icon = Icons.Default.Build,
                            iconTint = Color(0xFFFF9800),
                            title = "5. SERVER MAINTENANCE LOCK",
                            subtitle = "Full-screen maintenance screen displayed to all regular players"
                        )

                        HorizontalDivider(color = Color(0xFF23293A))

                        AdminSwitchRow(
                            title = if (isMaintenanceMode) "Server Maintenance ON" else "Server Live (Normal)",
                            subtitle = if (isMaintenanceMode) "All regular players are locked on the maintenance screen." else "App running smoothly for everyone.",
                            checked = isMaintenanceMode,
                            onCheckedChange = { isMaintenanceMode = it },
                            activeColor = Color(0xFFFF9800),
                            activeContainerColor = Color(0xFF331D08)
                        )

                        if (isMaintenanceMode) {
                            OutlinedTextField(
                                value = maintenanceMessage,
                                onValueChange = { maintenanceMessage = it },
                                label = { Text("Maintenance Message") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                colors = adminTextFieldColors()
                            )
                        }
                    }
                }

                // CARD 6: LIVE ANNOUNCEMENT TICKER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdminCardHeader(
                            icon = Icons.Default.Campaign,
                            iconTint = Color(0xFF00E676),
                            title = "6. LIVE ANNOUNCEMENT TICKER",
                            subtitle = "Broadcast high-priority announcement on top of the Home Screen"
                        )

                        HorizontalDivider(color = Color(0xFF23293A))

                        AdminSwitchRow(
                            title = "Live Announcement Ticker",
                            subtitle = if (isAnnouncementActive) "Active: Displays scrolling alert bar on Home Screen." else "Hidden: Alert bar is turned off.",
                            checked = isAnnouncementActive,
                            onCheckedChange = { isAnnouncementActive = it },
                            activeColor = Color(0xFF00E676),
                            activeContainerColor = Color(0xFF0D2818)
                        )

                        if (isAnnouncementActive) {
                            OutlinedTextField(
                                value = announcementNotice,
                                onValueChange = { announcementNotice = it },
                                label = { Text("Notice Text (Shown to players)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                colors = adminTextFieldColors()
                            )
                        }
                    }
                }

                // CARD 7: SIGNUP BONUSES & REWARD ECONOMY
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdminCardHeader(
                            icon = Icons.Default.MonetizationOn,
                            iconTint = Color(0xFF00E676),
                            title = "7. SIGNUP BONUSES & ECONOMY",
                            subtitle = "New player joining bonus (Real Cash & Coins), spin jackpot, and ad rewards"
                        )

                        HorizontalDivider(color = Color(0xFF23293A))

                        Text(
                            "NEW USER SIGNUP / JOINING BONUS",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color(0xFF00E676),
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = newUserRealMoneyBonus,
                                onValueChange = { newUserRealMoneyBonus = it },
                                label = { Text("Joining Real Cash (₹)") },
                                placeholder = { Text("5") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                            OutlinedTextField(
                                value = newUserAppMoneyBonus,
                                onValueChange = { newUserAppMoneyBonus = it },
                                label = { Text("Joining App Coins") },
                                placeholder = { Text("50") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = adminTextFieldColors()
                            )
                        }

                        Text(
                            "Default ₹5 Real Cash and 50 Coins configured for new registrations.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 15.sp
                        )

                        HorizontalDivider(color = Color(0xFF23293A))

                        Text(
                            "IN-GAME EARNING & JACKPOT",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )

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
                    }
                }

                // CARD 8: TOURNAMENT UPI & SUPPORT CONTACTS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AdminCardHeader(
                            icon = Icons.Default.AccountBalance,
                            iconTint = Color(0xFF00E676),
                            title = "8. TOURNAMENT UPI & SUPPORT",
                            subtitle = "Official deposit payment receiver and player support links"
                        )

                        HorizontalDivider(color = Color(0xFF23293A))

                        OutlinedTextField(
                            value = adminUpiId,
                            onValueChange = { adminUpiId = it },
                            label = { Text("Admin UPI ID (Player QR & Deposits)") },
                            placeholder = { Text("yourupi@oksbi") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = adminTextFieldColors(),
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF00E676)) },
                            trailingIcon = {
                                if (adminUpiId.isNotBlank()) {
                                    IconButton(onClick = {
                                        try {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Admin UPI ID", adminUpiId.trim()))
                                            Toast.makeText(context, "UPI ID Copied!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {}
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy UPI", tint = Color(0xFF00E676))
                                    }
                                }
                            }
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

                // SAVE & PUSH BUTTON
                Button(
                    onClick = { saveConfiguration() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("SAVING...", color = Color.Black, fontWeight = FontWeight.Black)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE & PUSH LIVE UPDATE TO ALL PLAYERS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF00E676),
    unfocusedBorderColor = Color(0xFF23293A),
    focusedLabelColor = Color(0xFF00E676),
    unfocusedLabelColor = Color(0xFF64748B),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color(0xFF00E676),
    focusedContainerColor = Color(0xFF0D0F14),
    unfocusedContainerColor = Color(0xFF0D0F14)
)

@Composable
private fun AdminCardHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.15f))
                .border(1.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun AdminSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeColor: Color = Color(0xFF00E676),
    activeContainerColor: Color = Color(0xFF0D2818)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) activeContainerColor else Color(0xFF10131B))
            .border(
                1.dp,
                if (checked) activeColor.copy(alpha = 0.5f) else Color(0xFF23293A),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (checked) activeColor else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
            )
            Text(
                subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 10.5.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = activeColor
            )
        )
    }
}
