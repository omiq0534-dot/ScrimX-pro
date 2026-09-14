package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
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

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.theme.AppColors
import com.example.ui.theme.glassCard
import com.example.ui.theme.glassBackground
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.example.ads.UnityAdsManager
import com.example.security.AppSecurityGuard
import com.example.ui.components.AdminMasterBadge
import com.example.ui.components.XBadge
import com.example.ui.components.XBadgeSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    matchId: String,
    navController: NavController,
    viewModel: MatchesViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val matchState by viewModel.currentMatch.collectAsState()
    val profile by userViewModel.profile.collectAsState()
    
    var selectedSlot by remember { mutableStateOf<Int?>(null) }
    var isBooking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog state for Player/Team details
    var showBookingDialog by remember { mutableStateOf(false) }
    var inputPlayerOrTeamName by remember { mutableStateOf("") }
    var inputInGameUid by remember { mutableStateOf("") }
    var adsWatchedForSlot by remember { mutableIntStateOf(0) }
    var isAdLoading by remember { mutableStateOf(false) }
    var applyCoinDiscount by remember { mutableStateOf(false) }
    
    LaunchedEffect(matchId) {
        viewModel.listenToMatchDetails(matchId)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMatchListener()
        }
    }
    
    val match = matchState
    
    if (match == null) {
        Box(modifier = Modifier.fillMaxSize().glassBackground(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.TextPrimary)
        }
        return
    }

    val bookedSlots = match.bookedSlots
    val slotNames = match.slotNames
    val slotUids = match.slotUids
    val slotBadges = match.slotBadges

    val isHeadToHead = match.matchType.equals("CS", ignoreCase = true) ||
                       match.matchType.equals("TDM", ignoreCase = true) ||
                       match.matchType.equals("DUEL", ignoreCase = true) ||
                       match.mode.equals("1v1", ignoreCase = true) ||
                       match.mode.equals("2v2", ignoreCase = true) ||
                       match.mode.equals("4v4", ignoreCase = true)

    val matchMode = when {
        match.mode.isNotBlank() -> match.mode
        match.title.contains("1v1", true) -> "1v1"
        match.title.contains("2v2", true) -> "2v2"
        match.title.contains("4v4", true) -> "4v4"
        match.title.contains("Solo", true) -> "Solo"
        match.title.contains("Duo", true) -> "Duo"
        else -> "Squad"
    }

    val totalSlotsCount = if (match.totalSlots > 0) {
        match.totalSlots
    } else {
        when (matchMode) {
            "1v1" -> 2
            "2v2" -> 4
            "4v4" -> 8
            "Solo" -> 48
            "Duo" -> 24
            else -> 12
        }
    }

    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    val myBookedEntry = bookedSlots.entries.find { it.value == currentUserEmail || it.value == currentUserId }
    val userAlreadyBookedSlot = myBookedEntry?.key?.toIntOrNull()

    // Classy Booking Confirmation Dialog
    if (showBookingDialog && selectedSlot != null) {
        var bookedSlotCount by remember { mutableIntStateOf(1) }
        var inputTeamName by remember { mutableStateOf("") }
        
        val isSquadMode = match.mode.contains("Squad", ignoreCase = true) || matchMode.contains("4v4", ignoreCase = true) || matchMode.contains("Squad", ignoreCase = true)
        val requiresTeamName = bookedSlotCount >= 3 || (isSquadMode && bookedSlotCount > 2)

        val isAdMatch = match.entryType.equals("AD", ignoreCase = true) || match.entry.contains("Ad", ignoreCase = true)
        val isFreeMatch = match.entryType.equals("FREE", ignoreCase = true) || match.entry.equals("Free", ignoreCase = true)
        val requiredAdsCount = if (match.requiredAds > 0) {
            match.requiredAds
        } else if (isAdMatch) {
            match.entry.filter { it.isDigit() }.toIntOrNull() ?: 1
        } else 0

        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { if (!isBooking && !isAdLoading) showBookingDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "CONFIRM SLOT $selectedSlot",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Match: ${match.title} (${match.map})",
                        color = Color(0xFF475569),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Multi-Slot Selection Row (1 to 4 slots)
                    Column {
                        Text("SELECT NUMBER OF SLOTS TO BOOK:", color = Color(0xFF334155), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..4).forEach { count ->
                                val isSelected = bookedSlotCount == count
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF00E676) else Color(0xFFF1F5F9))
                                        .border(1.dp, if (isSelected) Color(0xFF00C853) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                        .clickable { bookedSlotCount = count }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "$count Slot${if (count > 1) "s" else ""}",
                                        color = if (isSelected) Color.Black else Color(0xFF334155),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (requiresTeamName) {
                        OutlinedTextField(
                            value = inputTeamName,
                            onValueChange = { inputTeamName = it },
                            label = { Text("Team Name (Req. for 3-4 Slots)", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold) },
                            placeholder = { Text("e.g. Team Toxic / GodLike", color = Color(0xFF94A3B8)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                cursorColor = Color(0xFF0F172A)
                            ),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = inputPlayerOrTeamName,
                        onValueChange = { inputPlayerOrTeamName = it },
                        label = { Text("Leader / Player In-Game Name (IGN)", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("e.g. ProSniper_99", color = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF0F172A)
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputInGameUid,
                        onValueChange = { inputInGameUid = it },
                        label = { Text("Game In-Game UID", color = Color(0xFF475569), fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("e.g. 192847291", color = Color(0xFF94A3B8)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF0F172A)
                        ),
                        singleLine = true
                    )

                    // Entry Requirements Box
                    val rawEntryFee = match.entry.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                    val userCoins = profile?.appMoney ?: 0
                    val userReal = profile?.realMoney ?: 0
                    val maxDiscountRupees = if (!isFreeMatch && !isAdMatch && rawEntryFee > 0) minOf(userCoins / 10, rawEntryFee / 2) else 0
                    val isDiscountActive = applyCoinDiscount && maxDiscountRupees > 0
                    val discountRupees = if (isDiscountActive) maxDiscountRupees else 0
                    val discountCoins = if (isDiscountActive) maxDiscountRupees * 10 else 0
                    val finalPayable = (rawEntryFee - discountRupees).coerceAtLeast(0)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (isAdMatch) "Entry Method" else if (isFreeMatch) "Entry Type" else "Base Entry Fee",
                                    color = Color(0xFF9E9EA8),
                                    fontSize = 12.sp
                                )
                                Text(
                                    when {
                                        isFreeMatch -> "100% Free"
                                        isAdMatch -> "Watch Ad to Join"
                                        else -> "₹$rawEntryFee"
                                    },
                                    color = if (isFreeMatch) Color(0xFF10B981) else Color(0xFFFFD700),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }

                            if (!isFreeMatch && !isAdMatch && maxDiscountRupees > 0) {
                                Divider(color = AppColors.BorderColor, modifier = Modifier.padding(vertical = 4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (applyCoinDiscount) Color(0xFF2B2513) else Color.White)
                                        .border(1.dp, if (applyCoinDiscount) Color(0xFFFFD700) else AppColors.BorderColor, RoundedCornerShape(8.dp))
                                        .clickable { applyCoinDiscount = !applyCoinDiscount }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "Apply Coin Discount (-₹$maxDiscountRupees)",
                                                color = if (applyCoinDiscount) Color.White else Color(0xFF0F172A),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                "Uses ${maxDiscountRupees * 10} Coins (Balance: $userCoins Coins)",
                                                color = Color(0xFFFFD700),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Checkbox(
                                        checked = applyCoinDiscount,
                                        onCheckedChange = { applyCoinDiscount = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFFFFD700),
                                            checkmarkColor = Color.Black
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Payable from Tournament Balance:", color = Color(0xFF9E9EA8), fontSize = 11.sp)
                                    Text("₹$finalPayable", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                }
                            }

                            if (!isFreeMatch && !isAdMatch) {
                                if (userReal < finalPayable) {
                                    Text(
                                        "Low tournament balance (₹$userReal). Please add cash in Wallet to join.",
                                        color = Color(0xFFFF5252),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        "Tournament Balance: ₹$userReal (Sufficient)",
                                        color = Color(0xFF00E676),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (isAdMatch) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ads Progress:", fontSize = 11.sp, color = Color(0xFF8E92A4))
                                    Text("$adsWatchedForSlot / $requiredAdsCount Completed", fontSize = 11.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { if (requiredAdsCount > 0) adsWatchedForSlot.toFloat() / requiredAdsCount.toFloat() else 1f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFFFFD700),
                                    trackColor = Color(0xFF2C3042)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val slotToBook = selectedSlot ?: return@AlertDialog
                val isFormValid = inputPlayerOrTeamName.isNotBlank() && (!requiresTeamName || inputTeamName.isNotBlank())
                val effectiveDisplayName = if (requiresTeamName && inputTeamName.isNotBlank()) {
                    "[${inputTeamName.trim()}] ${inputPlayerOrTeamName.trim()}"
                } else {
                    inputPlayerOrTeamName.trim()
                }

                val rawEntryFee = match.entry.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                val userCoins = profile?.appMoney ?: 0
                val maxDiscountRupees = if (!isFreeMatch && !isAdMatch && rawEntryFee > 0) minOf(userCoins / 10, rawEntryFee / 2) else 0
                val isDiscountActive = applyCoinDiscount && maxDiscountRupees > 0
                val discountRupees = if (isDiscountActive) maxDiscountRupees else 0
                val discountCoins = if (isDiscountActive) maxDiscountRupees * 10 else 0
                val finalPayable = (rawEntryFee - discountRupees).coerceAtLeast(0)

                if (isAdMatch && adsWatchedForSlot < requiredAdsCount) {
                    // Watch Ad Action Button
                    Button(
                        onClick = {
                            if (!isFormValid) {
                                val msg = if (requiresTeamName && inputTeamName.isBlank()) "Please enter Team Name first" else "Please enter Player In-Game Name first"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val activity = context as? Activity
                            if (activity == null) {
                                Toast.makeText(context, "Activity not available for Ads", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isAdLoading = true
                            UnityAdsManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = {
                                    isAdLoading = false
                                    val newCount = adsWatchedForSlot + 1
                                    adsWatchedForSlot = newCount
                                    Toast.makeText(context, "Ad $newCount/$requiredAdsCount completed!", Toast.LENGTH_SHORT).show()
                                    
                                    if (newCount >= requiredAdsCount) {
                                        // Completed all required ads -> Auto book slot!
                                        isBooking = true
                                        viewModel.bookSlot(
                                            matchId = match.id,
                                            slotNumber = slotToBook,
                                            playerNameOrTeam = effectiveDisplayName,
                                            inGameUid = inputInGameUid.trim(),
                                            coinsDiscountUsed = 0,
                                            cashDiscountRupees = 0,
                                            onSuccess = {
                                                isBooking = false
                                                showBookingDialog = false
                                                selectedSlot = null
                                                adsWatchedForSlot = 0
                                                inputTeamName = ""
                                                inputPlayerOrTeamName = ""
                                                inputInGameUid = ""
                                                Toast.makeText(context, "Slot $slotToBook Booked Successfully via Free Ad Entry!", Toast.LENGTH_LONG).show()
                                            },
                                            onError = { err ->
                                                isBooking = false
                                                errorMessage = err
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                onAdSkipped = {
                                    isAdLoading = false
                                    Toast.makeText(context, "Video skipped! Slot unlock karne ke liye poora ad dekhna zaroori hai.", Toast.LENGTH_LONG).show()
                                },
                                onAdFailed = { err ->
                                    isAdLoading = false
                                    Toast.makeText(context, "Ad loading: $err. Please tap again.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isBooking && !isAdLoading && isFormValid
                    ) {
                        if (isAdLoading || isBooking) {
                            CircularProgressIndicator(color = AppColors.TextPrimary, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "WATCH AD (${adsWatchedForSlot + 1}/$requiredAdsCount)",
                                color = AppColors.TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else {
                    // Standard Free or Paid Confirm Button
                    Button(
                        onClick = {
                            if (!isFormValid) {
                                val msg = if (requiresTeamName && inputTeamName.isBlank()) "Please enter Team Name" else "Please enter Player In-Game Name"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isBooking = true
                            errorMessage = null
                            viewModel.bookSlot(
                                matchId = match.id,
                                slotNumber = slotToBook,
                                playerNameOrTeam = effectiveDisplayName,
                                inGameUid = inputInGameUid.trim(),
                                coinsDiscountUsed = discountCoins,
                                cashDiscountRupees = discountRupees,
                                onSuccess = {
                                    isBooking = false
                                    showBookingDialog = false
                                    selectedSlot = null
                                    adsWatchedForSlot = 0
                                    applyCoinDiscount = false
                                    inputTeamName = ""
                                    inputPlayerOrTeamName = ""
                                    inputInGameUid = ""
                                    Toast.makeText(context, "Slot $slotToBook Booked Successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    isBooking = false
                                    errorMessage = err
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFreeMatch) Color(0xFF10B981) else Color(0xFFFFD700)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isBooking && isFormValid
                    ) {
                        if (isBooking) {
                            CircularProgressIndicator(color = AppColors.TextPrimary, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                when {
                                    isFreeMatch -> "CONFIRM & JOIN (FREE)"
                                    isAdMatch -> "CONFIRM & JOIN (FREE)"
                                    discountRupees > 0 -> "PAY ₹$finalPayable & JOIN (-₹$discountRupees OFF)"
                                    else -> "CONFIRM & PAY ₹$finalPayable"
                                },
                                color = if (isFreeMatch) AppColors.TextPrimary else Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isBooking && !isAdLoading) showBookingDialog = false }) {
                    Text("Cancel", color = Color(0xFF9E9EA8))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F6F8),
        topBar = {
            TopAppBar(
                title = { Text("Match Details", fontWeight = FontWeight.Black, color = Color(0xFF111827)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF111827))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF5F6F8))
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F6F8))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (userAlreadyBookedSlot != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111319)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "SLOT BOOKED",
                                    color = Color(0xFF22C55E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "You are registered for Slot $userAlreadyBookedSlot",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Button(
                    onClick = {
                        if (userAlreadyBookedSlot != null) {
                            Toast.makeText(context, "You have already booked Slot $userAlreadyBookedSlot!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedSlot != null) {
                            showBookingDialog = true
                        }
                    },
                    enabled = selectedSlot != null && !isBooking && userAlreadyBookedSlot == null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF111319),
                        disabledContainerColor = Color(0xFF1E212D),
                        contentColor = Color.White,
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isBooking) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else if (userAlreadyBookedSlot != null) {
                        Text("ALREADY REGISTERED (SLOT $userAlreadyBookedSlot)", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                    } else if (selectedSlot != null) {
                        Text("BOOK SLOT $selectedSlot • ${match.entry}", fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 0.5.sp, color = Color.White)
                    } else {
                        Text("SELECT A SLOT ABOVE TO BOOK", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF9CA3AF))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F6F8))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Title and basic info
            Spacer(modifier = Modifier.height(8.dp))
            Text(match.title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF111827), lineHeight = 30.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(match.map)
                InfoChip(if (isHeadToHead) "${match.matchType} ($matchMode)" else matchMode)
                InfoChip(if (match.badge.isNotBlank()) match.badge else "ESPORTS")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Prize and Entry
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailBox("Prize Pool", match.prize, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                DetailBox("Entry Fee", match.entry, Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Time & Room ID Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .glassCard()
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = AppColors.TextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Match Timing: ${match.time}", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, fontSize = 14.sp)
                    }
                    
                    val hasBooked = userAlreadyBookedSlot != null
                    
                    if (hasBooked && match.roomId.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("ROOM CREDENTIALS UNLOCKED", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Room ID: ${match.roomId}", fontWeight = FontWeight.Black, color = AppColors.TextPrimary, fontSize = 16.sp)
                                if (match.roomPass.isNotEmpty()) {
                                    Text("Password: ${match.roomPass}", fontWeight = FontWeight.Black, color = Color(0xFF00E5FF), fontSize = 15.sp)
                                }
                            }
                        }
                    } else if (hasBooked) {
                        Text(
                            "You are registered in Slot $userAlreadyBookedSlot! Room ID & Password will appear here 15 mins before match starts.",
                            color = Color(0xFF16A34A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            "Book a slot below to unlock Room ID & Password when released by admin.",
                            color = Color(0xFF666677),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            
            // Watch Live stream button if live
            if (match.status == "Live" || match.status == "Ongoing" || match.liveUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        val url = if (match.liveUrl.isNotBlank()) {
                            match.liveUrl
                        } else {
                            "https://www.youtube.com/results?search_query=${Uri.encode(match.title + " Live")}"
                        }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open stream", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF003C), contentColor = AppColors.TextPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Watch Live", modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WATCH LIVE STREAM", fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Slots Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isHeadToHead) "HEAD-TO-HEAD SLOTS" else "SELECT YOUR SLOT", 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Color(0xFF111827), 
                    letterSpacing = 0.5.sp
                )
                Text("${bookedSlots.size}/$totalSlotsCount Booked", fontSize = 12.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // SLOTS RENDERING:
            // If CS/TDM/DUEL: Display VS Face-off Cards
            // Else: Display standard BR slot list
            if (isHeadToHead) {
                HeadToHeadSlotsSection(
                    totalSlots = totalSlotsCount,
                    bookedSlots = bookedSlots,
                    slotNames = slotNames,
                    slotUids = slotUids,
                    slotBadges = slotBadges,
                    selectedSlot = selectedSlot,
                    userAlreadyBookedSlot = userAlreadyBookedSlot,
                    onSelectSlot = { slotNum ->
                        if (userAlreadyBookedSlot != null) {
                            Toast.makeText(context, "You are already booked in Slot $userAlreadyBookedSlot!", Toast.LENGTH_SHORT).show()
                        } else {
                            selectedSlot = slotNum
                        }
                    }
                )
            } else {
                // Standard BR Slot List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val slots = (1..totalSlotsCount).map { i ->
                        val isBooked = bookedSlots.containsKey(i.toString())
                        val bookedName = slotNames[i.toString()] ?: if (isBooked) "Booked Player" else null
                        val inGameId = slotUids[i.toString()]
                        val badgeKey = slotBadges[i.toString()]
                        val bookedUser = bookedSlots[i.toString()]
                        MatchSlot(
                            number = i,
                            isBooked = isBooked,
                            teamName = bookedName,
                            inGameUid = inGameId,
                            badgeKey = badgeKey,
                            bookedUser = bookedUser
                        )
                    }

                    slots.forEach { slot ->
                        val isSelected = selectedSlot == slot.number
                        SlotCard(
                            slot = slot, 
                            isSelected = isSelected,
                            isMySlot = userAlreadyBookedSlot == slot.number
                        ) {
                            if (!slot.isBooked) {
                                if (userAlreadyBookedSlot != null) {
                                    Toast.makeText(context, "You are already booked in Slot $userAlreadyBookedSlot!", Toast.LENGTH_SHORT).show()
                                } else {
                                    selectedSlot = slot.number
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Custom Match Rules Section
            Text("MATCH SPECIFIC RULES", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF111827), letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, AppColors.BorderColor, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (match.rules.isNotBlank()) {
                        Text(
                            match.rules,
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = AppColors.BorderColor)
                    }
                    Text(
                        "• Emulators & Hacks are strictly prohibited (Instant Ban).\n" +
                        "• Join only the designated Slot Number you booked.\n" +
                        "• Room ID & Password must not be shared with external players.\n" +
                        "• Keep screenshot of the match result for verification.",
                        color = Color(0xFF4B5563),
                        fontSize = 12.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * Helper to display authentic player badge in slots
 */
@Composable
fun PlayerSlotBadge(badgeKey: String?, playerName: String?, bookedUid: String?) {
    val isOwner = badgeKey == "OWNER" || 
                  AppSecurityGuard.isSuperOwner(bookedUid) || 
                  playerName?.contains("Owner", ignoreCase = true) == true
    val isMod = badgeKey == "MOD" || 
                playerName?.contains("Mod", ignoreCase = true) == true
    val isXBadge = badgeKey == "X_BADGE" || 
                   playerName?.contains("[X]", ignoreCase = true) == true || 
                   playerName?.contains("X-", ignoreCase = true) == true ||
                   playerName?.contains("GodLike", ignoreCase = true) == true ||
                   playerName?.contains("Total Gaming", ignoreCase = true) == true

    if (isOwner) {
        AdminMasterBadge(isOwner = true, size = XBadgeSize.MINI, showClickInfo = true)
    } else if (isMod) {
        AdminMasterBadge(isOwner = false, size = XBadgeSize.MINI, showClickInfo = true)
    } else if (isXBadge) {
        XBadge(size = XBadgeSize.MINI, isAnimated = false, showClickInfo = true)
    }
}

/**
 * Head to Head (CS, TDM, 1v1) Face-off Layout: Team 1 vs Team 2
 */
@Composable
fun HeadToHeadSlotsSection(
    totalSlots: Int,
    bookedSlots: Map<String, String>,
    slotNames: Map<String, String>,
    slotUids: Map<String, String>,
    slotBadges: Map<String, String>,
    selectedSlot: Int?,
    userAlreadyBookedSlot: Int?,
    onSelectSlot: (Int) -> Unit
) {
    val half = (totalSlots / 2).coerceAtLeast(1)
    val teamASlots = (1..half).toList()
    val teamBSlots = ((half + 1)..totalSlots).toList()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Team A (Side 1) vs Team B (Side 2) Pairs
        for (i in 0 until half) {
            val slotA = teamASlots.getOrNull(i) ?: 1
            val slotB = teamBSlots.getOrNull(i) ?: 2

            val isBookedA = bookedSlots.containsKey(slotA.toString())
            val nameA = slotNames[slotA.toString()]
            val uidA = slotUids[slotA.toString()]
            val badgeA = slotBadges[slotA.toString()]
            val userA = bookedSlots[slotA.toString()]

            val isBookedB = bookedSlots.containsKey(slotB.toString())
            val nameB = slotNames[slotB.toString()]
            val uidB = slotUids[slotB.toString()]
            val badgeB = slotBadges[slotB.toString()]
            val userB = bookedSlots[slotB.toString()]

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, AppColors.BorderColor, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player A / Team A Slot
                    Box(modifier = Modifier.weight(1f)) {
                        HeadToHeadSlotItem(
                            slotNum = slotA,
                            isBooked = isBookedA,
                            playerName = nameA,
                            uid = uidA,
                            badgeKey = badgeA,
                            bookedUserId = userA,
                            isSelected = selectedSlot == slotA,
                            isMySlot = userAlreadyBookedSlot == slotA,
                            onClick = { onSelectSlot(slotA) }
                        )
                    }

                    // VS Badge in center
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E212D))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "VS",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color(0xFFFFD700),
                            letterSpacing = 1.sp
                        )
                    }

                    // Player B / Team B Slot
                    Box(modifier = Modifier.weight(1f)) {
                        HeadToHeadSlotItem(
                            slotNum = slotB,
                            isBooked = isBookedB,
                            playerName = nameB,
                            uid = uidB,
                            badgeKey = badgeB,
                            bookedUserId = userB,
                            isSelected = selectedSlot == slotB,
                            isMySlot = userAlreadyBookedSlot == slotB,
                            onClick = { onSelectSlot(slotB) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeadToHeadSlotItem(
    slotNum: Int,
    isBooked: Boolean,
    playerName: String?,
    uid: String?,
    badgeKey: String?,
    bookedUserId: String?,
    isSelected: Boolean,
    isMySlot: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isMySlot -> Color(0xFF16251C)
        isSelected -> Color(0xFF1E212D)
        isBooked -> Color(0xFF161922)
        else -> Color(0xFF111319)
    }

    val borderColor = when {
        isMySlot -> Color(0xFF22C55E)
        isSelected -> Color.White
        isBooked -> Color(0xFF262A38)
        else -> Color(0xFF262A38)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = !isBooked) { onClick() }
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Slot $slotNum",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color.White else if (isMySlot) Color(0xFF22C55E) else Color(0xFF8E92A4)
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            if (isBooked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PlayerSlotBadge(badgeKey = badgeKey, playerName = playerName, bookedUid = bookedUserId)
                    if (badgeKey in listOf("OWNER", "MOD", "X_BADGE") || playerName?.contains("[X]") == true) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        playerName ?: "Booked",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White,
                        maxLines = 1
                    )
                }
                if (!uid.isNullOrBlank() && uid != "N/A") {
                    Text("UID: $uid", fontSize = 9.5.sp, color = Color(0xFF9CA3AF), maxLines = 1)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    if (isMySlot) "YOU (BOOKED)" else "BOOKED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isMySlot) Color(0xFF22C55E) else Color(0xFF9CA3AF)
                )
            } else {
                Text(
                    "No Player",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    if (isSelected) "SELECTED" else "BOOK SLOT",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) Color.White else Color(0xFF9CA3AF)
                )
            }
        }
    }
}

data class MatchSlot(
    val number: Int,
    val isBooked: Boolean,
    val teamName: String? = null,
    val inGameUid: String? = null,
    val badgeKey: String? = null,
    val bookedUser: String? = null
)

@Composable
fun SlotCard(
    slot: MatchSlot, 
    isSelected: Boolean, 
    isMySlot: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val targetScale = if (isPressed && !slot.isBooked) 0.96f else if (isSelected) 1.02f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
    )

    val bgColor = when {
        isMySlot -> Color(0xFF16251C)
        isSelected -> Color(0xFF1E212D)
        slot.isBooked -> Color(0xFF161922)
        else -> Color(0xFF111319)
    }
        
    val borderColor = when {
        isMySlot -> Color(0xFF22C55E)
        isSelected -> Color.White
        slot.isBooked -> Color(0xFF262A38)
        else -> if (isPressed) Color.White else Color(0xFF262A38)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                enabled = !slot.isBooked,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isMySlot) Color(0xFF22C55E) 
                        else if (isSelected) Color.White 
                        else if (slot.isBooked) Color(0xFF1E212D) 
                        else Color(0xFF1E212D)
                    )
                    .border(1.dp, if (isMySlot) Color(0xFF22C55E) else if (isSelected) Color.White else Color(0xFF262A38), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${slot.number}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (isSelected || isMySlot) Color.Black else Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                if (slot.isBooked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlayerSlotBadge(badgeKey = slot.badgeKey, playerName = slot.teamName, bookedUid = slot.bookedUser)
                        if (slot.badgeKey in listOf("OWNER", "MOD", "X_BADGE") || slot.teamName?.contains("[X]") == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            slot.teamName ?: "Reserved",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    if (!slot.inGameUid.isNullOrBlank() && slot.inGameUid != "N/A") {
                        Text(
                            "UID: ${slot.inGameUid}",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        "Slot ${slot.number}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        "Available",
                        fontSize = 11.sp,
                        color = Color(0xFF06B6D4)
                    )
                }
            }
        }
                
        if (slot.isBooked) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isMySlot) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFF1E212D))
                    .border(1.dp, if (isMySlot) Color(0xFF22C55E) else Color(0xFF262A38), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isMySlot) "HD (You)" else "BOOKED", 
                    color = if (isMySlot) Color(0xFF22C55E) else Color(0xFF9CA3AF), 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("SELECTED", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Text("TAP TO BOOK", color = Color(0xFF9CA3AF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1C1C1E))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text.uppercase(), color = AppColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
    }
}

@Composable
fun DetailBox(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .glassCard()
            .padding(18.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 9.5.sp,
            color = AppColors.TextSecondary,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = AppColors.TextPrimary
        )
    }
}
