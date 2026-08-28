package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.XBadge
import com.example.ui.components.XBadgeSize
import com.example.ui.components.AdminMasterBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userViewModel: UserViewModel = viewModel()) {
    val context = LocalContext.current
    val profile by userViewModel.profile.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showReferDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of your account?") },
            confirmButton = {
                Button(
                    onClick = { 
                        showLogoutDialog = false
                        userViewModel.logout()
                        navController.navigate("login") { 
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Log Out", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Game Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    label = { Text("Enter your name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (editNameText.isNotBlank()) {
                        userViewModel.updateName(editNameText)
                    }
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReferDialog) {
        ReferAndEarnDialog(
            profile = profile,
            onDismiss = { showReferDialog = false },
            onClaimCode = { code ->
                userViewModel.claimReferralCode(code, bonusCoins = 50) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Black, fontSize = 26.sp, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAFAFA),
                    scrolledContainerColor = Color(0xFFFAFAFA)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            ProfileHeader(
                name = profile?.name ?: "Loading...",
                email = profile?.email ?: "loading...",
                hasXBadge = profile?.hasXBadge == true
            )

            // 👑 [X] Badge Status Showcase Card
            XBadgeStatusCard(profile = profile)
            
            // Refer & Earn Banner Button
            ReferBannerCard(
                referralCode = profile?.referralCode ?: "LOADING...",
                onClick = { showReferDialog = true }
            )

            StatsCard(profile = profile)
            SettingsList(
                isAdmin = profile?.email == "omiq0534@gmail.com",
                onAdminClick = {
                    navController.navigate("admin_dashboard")
                },
                onReferClick = {
                    showReferDialog = true
                },
                onEditProfileClick = {
                    editNameText = profile?.name ?: ""
                    showEditDialog = true
                },
                onSupportClick = {
                    navController.navigate("customer_support")
                },
                onLogoutClick = {
                    showLogoutDialog = true
                }
            )
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ReferBannerCard(referralCode: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFD700).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = "Gift", tint = Color(0xFFFFD700), modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("REFER & EARN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00E676))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("+50 COINS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 9.sp)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Invite friends or claim friend's code!", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFFFFD700))
        }
    }
}

@Composable
fun ReferAndEarnDialog(
    profile: UserProfile?,
    onDismiss: () -> Unit,
    onClaimCode: (String) -> Unit
) {
    val context = LocalContext.current
    var claimCodeInput by remember { mutableStateOf("") }
    val myReferCode = profile?.referralCode?.ifBlank { "GENERATING..." } ?: "REF9999"
    val isAlreadyReferred = !profile?.referredBy.isNullOrBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF10131E),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("REFER & EARN 🎁", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Share your code with friends. Both you and your friend get +50 Free Coins instantly!",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2235))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${profile?.referralCount ?: 0}", color = Color(0xFF00E5FF), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Friends Invited", color = Color(0xFF8E92A4), fontSize = 11.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF2E334D)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${profile?.referralEarnings ?: 0} 🪙", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Total Earned", color = Color(0xFF8E92A4), fontSize = 11.sp)
                    }
                }

                // 1. BHEJNE WALA SECTION (Your Code & Download Link)
                val downloadWebsiteUrl = "https://website-scrim-x-pro.vercel.app/"
                val fullInviteText = "🔥 Play BGMI & Free Fire Esports Tournaments on ScrimX Pro!\n\n📥 Download App: $downloadWebsiteUrl\n🎁 Referral Code: $myReferCode\n\n(Enter this code in Profile to get +50 Free Bonus Coins instantly! 💰)"

                Text("1. YOUR INVITE LINK & REFERRAL CODE", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161A29)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Referral Code Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0D101A))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("MY REFERRAL CODE", color = Color(0xFF8E92A4), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    myReferCode,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    letterSpacing = 2.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Referral Code", myReferCode)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "📋 Code Copied: $myReferCode", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = Color(0xFF00E5FF))
                            }
                        }

                        // App Download Website Link Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0D101A))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("APP DOWNLOAD WEBSITE", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    downloadWebsiteUrl,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Website Link", downloadWebsiteUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "📋 Website Link Copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Link, contentDescription = "Copy Link", tint = Color(0xFFFFD700))
                            }
                        }

                        // Action Buttons: Copy All & Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("ScrimX Pro Invite", fullInviteText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "📋 Full Invite Link & Message Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("COPY LINK", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, fullInviteText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share ScrimX Pro Invite")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SHARE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // 2. CLAIM WALA SECTION (Enter Friend's Code)
                Text("2. CLAIM FRIEND'S REFERRAL CODE", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161A29)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isAlreadyReferred) Color(0xFF00E676).copy(alpha = 0.4f) else Color(0xFF2E334D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (isAlreadyReferred) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Bonus Already Claimed! 🎉", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Referred by: ${profile?.referredBy}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = claimCodeInput,
                                onValueChange = { claimCodeInput = it.uppercase() },
                                label = { Text("Enter Friend's Code", color = Color(0xFF8E92A4)) },
                                placeholder = { Text("e.g. REF1234", color = Color(0xFF555B70)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF2E334D)
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (claimCodeInput.isNotBlank()) {
                                        onClaimCode(claimCodeInput)
                                    } else {
                                        Toast.makeText(context, "Please enter a referral code", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("CLAIM +50 BONUS COINS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ProfileHeader(name: String, email: String, hasXBadge: Boolean = false) {
    val isOwner = email.equals("omiq0534@gmail.com", ignoreCase = true)
    val isAdmin = isOwner || email.equals("admin@tournament.com", ignoreCase = true)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isAdmin) Color(0xFF1E1015) else Color.Black)
                .border(
                    width = if (isAdmin || hasXBadge) 2.5.dp else 0.dp,
                    color = if (isOwner) Color(0xFFFF0055) else if (hasXBadge) Color(0xFFFFD700) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isOwner) Icons.Default.Shield else Icons.Default.Person, 
                contentDescription = "Avatar", 
                tint = if (isOwner) Color(0xFFFFD700) else Color.White, 
                modifier = Modifier.size(36.dp)
            )
            if (isAdmin) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                ) {
                    AdminMasterBadge(isOwner = isOwner, showClickInfo = true)
                }
            } else if (hasXBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                ) {
                    XBadge(size = XBadgeSize.MINI, isAnimated = true)
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                if (isAdmin) {
                    AdminMasterBadge(isOwner = isOwner, showClickInfo = true)
                } else if (hasXBadge) {
                    XBadge(size = XBadgeSize.NORMAL, isAnimated = true, showClickInfo = true)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(email, fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun XBadgeStatusCard(profile: UserProfile?) {
    val hasXBadge = profile?.hasXBadge == true
    val wins = profile?.totalWins ?: 0
    val targetWins = 10
    val progress = (wins.toFloat() / targetWins).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasXBadge) Color(0xFF141208) else Color(0xFF0F172A)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.2.dp,
            color = if (hasXBadge) Color(0xFFFFD700) else Color(0xFF1E293B)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                XBadge(
                    size = XBadgeSize.LARGE,
                    isAnimated = true,
                    showClickInfo = true
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    if (hasXBadge) {
                        Text(
                            "VERIFIED [X] BADGE",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "Official Pro Esports Player / Champion 👑",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            "ROAD TO [X] BADGE",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "Win $wins/$targetWins Tournament Matches to Unlock",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(5.dp)
                                .clip(CircleShape),
                            color = Color(0xFFFFD700),
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (hasXBadge) Color(0xFFFFD700).copy(alpha = 0.15f) else Color(0xFF1E293B))
                    .border(1.dp, if (hasXBadge) Color(0xFFFFD700) else Color(0xFF334155), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (hasXBadge) "PRO ACTIVE" else "$wins/$targetWins WINS",
                    color = if (hasXBadge) Color(0xFFFFD700) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun StatsCard(profile: UserProfile?) {
    val totalMatches = profile?.totalMatches ?: 0
    val totalWins = profile?.totalWins ?: 0
    val totalKills = profile?.totalKills ?: 0

    val winRate = if (totalMatches > 0) {
        String.format(java.util.Locale.US, "%.0f%%", (totalWins.toDouble() / totalMatches) * 100)
    } else "0%"

    val kdRatio = if (totalMatches > 0) {
        String.format(java.util.Locale.US, "%.1f", totalKills.toDouble() / totalMatches)
    } else "0.0"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CAREER BATTLE STATS",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "LIVE VERIFIED",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }

            // 3 Main Stat Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(title = "Matches Played", value = "$totalMatches")
                HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.height(35.dp).width(1.dp))
                StatItem(title = "Total Wins 🏆", value = "$totalWins")
                HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.height(35.dp).width(1.dp))
                StatItem(title = "Total Kills 🎯", value = "$totalKills")
            }

            HorizontalDivider(color = Color(0xFF1E293B))

            // Efficiency Row: Win Rate & K/D Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Win Rate: ", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(winRate, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3366))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Avg K/D: ", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(kdRatio, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
        Spacer(modifier = Modifier.height(2.dp))
        Text(title, color = Color(0xFFB0B0B0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsList(
    isAdmin: Boolean,
    onAdminClick: () -> Unit,
    onReferClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSupportClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
            .padding(12.dp)
    ) {
        if (isAdmin) {
            SettingsRow(icon = Icons.Default.Security, title = "Admin HQ Panel", badge = "Owner", onClick = onAdminClick)
            HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 12.dp))
        }
        SettingsRow(icon = Icons.Default.CardGiftcard, title = "Refer & Earn", badge = "+50 🪙", onClick = onReferClick)
        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 12.dp))
        SettingsRow(icon = Icons.Default.Edit, title = "Edit Profile Name", onClick = onEditProfileClick)
        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 12.dp))
        SettingsRow(icon = Icons.Default.HeadsetMic, title = "Customer Support & FAQs", badge = "24/7", onClick = onSupportClick)
        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 12.dp))
        SettingsRow(icon = Icons.AutoMirrored.Filled.Logout, title = "Log Out", isDestructive = true, onClick = onLogoutClick)
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDestructive) Color(0xFFFFEBEE) else Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = if (isDestructive) Color(0xFFF44336) else Color.Black, 
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 15.sp, 
                color = if (isDestructive) Color(0xFFF44336) else Color.Black
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (badge == "Owner") Color(0xFFFF3366) else Color(0xFFFFD700))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        badge,
                        color = if (badge == "Owner") Color.White else Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.DarkGray)
    }
}

