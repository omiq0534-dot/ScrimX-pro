package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ads.UnityAdsManager
import com.example.ads.UnityBannerAd
import com.example.security.AppSecurityGuard
import com.unity3d.ads.UnityAds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUnityAdsScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val db = remember { FirebaseHelper.getFirestore() }
    val auth = remember { FirebaseHelper.getAuth() }
    val currentUserEmail = auth?.currentUser?.email?.lowercase() ?: ""
    val isOwner = remember(currentUserEmail) { AppSecurityGuard.isSuperOwner(currentUserEmail) }

    var gameId by remember { mutableStateOf("6183191") }
    var testMode by remember { mutableStateOf(true) }
    var rewardedPlacement by remember { mutableStateOf("Rewarded_Android1") }
    var interstitialPlacement by remember { mutableStateOf("Interstitial_Android2") }
    var bannerPlacement by remember { mutableStateOf("Banner_Android3") }
    var rewardCoinsPerAd by remember { mutableStateOf("15") }
    var adsEnabled by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!isOwner) {
            Toast.makeText(context, "Access Denied: Owner Only", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            return@LaunchedEffect
        }

        db?.collection("settings")?.document("unity_ads")?.get()?.addOnSuccessListener { doc ->
            isLoading = false
            if (doc != null && doc.exists()) {
                gameId = doc.getString("gameId") ?: "6183191"
                testMode = doc.getBoolean("testMode") ?: true
                val r = doc.getString("rewardedPlacement") ?: "Rewarded_Android1"
                val i = doc.getString("interstitialPlacement") ?: "Interstitial_Android2"
                val b = doc.getString("bannerPlacement") ?: "Banner_Android3"
                
                rewardedPlacement = if (r == "rewardedVideo" || r == "Rewarded_Android") "Rewarded_Android1" else r
                interstitialPlacement = if (i == "video" || i == "Interstitial_Android") "Interstitial_Android2" else i
                bannerPlacement = if (b == "banner" || b == "Banner_Android") "Banner_Android3" else b

                rewardCoinsPerAd = (doc.getLong("rewardCoinsPerAd") ?: 15L).toString()
                adsEnabled = doc.getBoolean("adsEnabled") ?: true

                UnityAdsManager.rewardedPlacementId = rewardedPlacement
                UnityAdsManager.interstitialPlacementId = interstitialPlacement
                UnityAdsManager.bannerPlacementId = bannerPlacement
                UnityAdsManager.initialize(context, customGameId = gameId, isTest = testMode, forceReinit = true)
            }
        }?.addOnFailureListener {
            isLoading = false
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
                            "UNITY ADS CONTROL",
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
            // Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                        )
                    )
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UNITY ADS ENGINE", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Text(
                        "Manage in-app ads, rewarded coin payouts & live placement IDs with your Unity Game ID.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00E5FF))
                }
            } else {
                // Live Status Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (UnityAds.isInitialized) Color(0xFF00E676).copy(alpha = 0.12f) else Color(0xFFFF5252).copy(alpha = 0.12f))
                        .border(1.dp, if (UnityAds.isInitialized) Color(0xFF00E676).copy(alpha = 0.4f) else Color(0xFFFF5252).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (UnityAds.isInitialized) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (UnityAds.isInitialized) Color(0xFF00E676) else Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            if (UnityAds.isInitialized) "UNITY ADS SDK ACTIVE" else "UNITY ADS SDK STANDBY",
                            color = if (UnityAds.isInitialized) Color(0xFF00E676) else Color(0xFFFF5252),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        val errorInfo = UnityAdsManager.lastInitErrorMessage
                        Text(
                            if (UnityAds.isInitialized) "SDK Initialized with Game ID: ${UnityAdsManager.gameId}"
                            else if (!errorInfo.isNullOrBlank()) "Status: $errorInfo (Make sure to enter Android Game ID, not iOS)"
                            else "Ready to initialize with Android Game ID",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Game ID Input
                ClassyDarkField(
                    value = gameId,
                    onValueChange = { gameId = it },
                    label = "Unity Game ID (Android)",
                    placeholder = "e.g. 6183190 / 6183191",
                    leadingIcon = Icons.Default.Tag
                )
                Text(
                    "Note: Unity Dashboard me 'Monetization' -> 'Android' ka specific Game ID dalein (iOS Game ID dalne par mismatched platform error aayega).",
                    color = Color(0xFF00E5FF).copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                // Master Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF14161F))
                        .border(1.dp, Color(0xFF262938), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Unity Ads in App", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Toggle ads on/off across all user screens", color = Color(0xFF8E92A4), fontSize = 11.sp)
                    }
                    Switch(
                        checked = adsEnabled,
                        onCheckedChange = { adsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00E676)
                        )
                    )
                }

                // Test Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF14161F))
                        .border(1.dp, Color(0xFF262938), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unity Test Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Turn OFF when publishing real APK to earn actual revenue", color = Color(0xFFFFD700), fontSize = 11.sp)
                    }
                    Switch(
                        checked = testMode,
                        onCheckedChange = { testMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF9800)
                        )
                    )
                }

                // Placement IDs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PLACEMENT IDs", color = Color(0xFF8E92A4), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                rewardedPlacement = "Rewarded_Android1"
                                interstitialPlacement = "Interstitial_Android2"
                                bannerPlacement = "Banner_Android3"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2230)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("My Units", fontSize = 10.sp, color = Color(0xFF00E5FF))
                        }
                        Button(
                            onClick = {
                                rewardedPlacement = "Rewarded_Android"
                                interstitialPlacement = "Interstitial_Android"
                                bannerPlacement = "Banner_Android"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2230)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Standard", fontSize = 10.sp, color = Color(0xFFFFD700))
                        }
                    }
                }

                ClassyDarkField(
                    value = rewardedPlacement,
                    onValueChange = { rewardedPlacement = it },
                    label = "Rewarded Video Placement ID",
                    placeholder = "Rewarded_Android or rewardedVideo",
                    leadingIcon = Icons.Default.PlayCircle
                )

                ClassyDarkField(
                    value = interstitialPlacement,
                    onValueChange = { interstitialPlacement = it },
                    label = "Interstitial Ad Placement ID",
                    placeholder = "Interstitial_Android or video",
                    leadingIcon = Icons.Default.Fullscreen
                )

                ClassyDarkField(
                    value = bannerPlacement,
                    onValueChange = { bannerPlacement = it },
                    label = "Banner Ad Placement ID",
                    placeholder = "Banner_Android or banner",
                    leadingIcon = Icons.Default.ViewStream
                )

                ClassyDarkField(
                    value = rewardCoinsPerAd,
                    onValueChange = { rewardCoinsPerAd = it },
                    label = "Coins Given to User per Video",
                    placeholder = "15",
                    leadingIcon = Icons.Default.Stars
                )

                // Save Button & Force Re-Init
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            isSaving = true
                            statusMessage = null
                            val data = hashMapOf<String, Any>(
                                "gameId" to gameId.trim(),
                                "testMode" to testMode,
                                "rewardedPlacement" to rewardedPlacement.trim(),
                                "interstitialPlacement" to interstitialPlacement.trim(),
                                "bannerPlacement" to bannerPlacement.trim(),
                                "rewardCoinsPerAd" to (rewardCoinsPerAd.toLongOrNull() ?: 15L),
                                "adsEnabled" to adsEnabled,
                                "updatedAt" to System.currentTimeMillis()
                            )

                            db?.collection("settings")?.document("unity_ads")?.set(data)?.addOnSuccessListener {
                                isSaving = false
                                UnityAdsManager.rewardedPlacementId = rewardedPlacement.trim()
                                UnityAdsManager.interstitialPlacementId = interstitialPlacement.trim()
                                UnityAdsManager.bannerPlacementId = bannerPlacement.trim()
                                UnityAdsManager.initialize(
                                    context,
                                    customGameId = gameId.trim(),
                                    isTest = testMode,
                                    forceReinit = true,
                                    onComplete = {
                                        Toast.makeText(context, "Unity Ads Saved & Re-Initialized Successfully!", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailed = { err ->
                                        Toast.makeText(context, "Saved, but Unity Init Notice: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }?.addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(50.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE CONFIG", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Re-connecting Unity SDK...", Toast.LENGTH_SHORT).show()
                            UnityAdsManager.initialize(
                                context,
                                customGameId = gameId.trim(),
                                isTest = testMode,
                                forceReinit = true,
                                onComplete = {
                                    Toast.makeText(context, "Connected! Unity Ads Initialized.", Toast.LENGTH_SHORT).show()
                                },
                                onFailed = { err ->
                                    Toast.makeText(context, "Init Failed: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2230)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00E5FF))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RE-INIT", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ad Testing Arena
                Text("TEST ADS LIVE", color = Color(0xFF8E92A4), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (activity != null) {
                                Toast.makeText(context, "Loading Rewarded Video Ad...", Toast.LENGTH_SHORT).show()
                                UnityAdsManager.showRewardedAd(
                                    activity = activity,
                                    placementId = rewardedPlacement.trim(),
                                    onRewardEarned = {
                                        Toast.makeText(context, "Rewarded Ad Completed! (+Coins credited)", Toast.LENGTH_LONG).show()
                                    },
                                    onAdClosed = {
                                        Toast.makeText(context, "Rewarded Ad Closed", Toast.LENGTH_SHORT).show()
                                    },
                                    onAdFailed = { err ->
                                        Toast.makeText(context, "Ad show failed: $err", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rewarded Ad", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            if (activity != null) {
                                Toast.makeText(context, "Loading Interstitial Ad...", Toast.LENGTH_SHORT).show()
                                UnityAdsManager.showInterstitialAd(
                                    activity = activity,
                                    placementId = interstitialPlacement.trim(),
                                    onAdClosed = {
                                        Toast.makeText(context, "Interstitial Closed", Toast.LENGTH_SHORT).show()
                                    },
                                    onAdFailed = { err ->
                                        Toast.makeText(context, "Interstitial failed: $err", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Interstitial", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Banner Preview
                Text("LIVE BANNER PREVIEW", color = Color(0xFF8E92A4), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF14161F))
                        .border(1.dp, Color(0xFF262938), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    UnityBannerAd(
                        modifier = Modifier.fillMaxWidth(),
                        placementId = bannerPlacement.trim()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ClassyDarkField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color(0xFFC0C4D6), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF64748B), fontSize = 13.sp) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF14161F),
                unfocusedContainerColor = Color(0xFF14161F),
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF262938),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
    }
}
