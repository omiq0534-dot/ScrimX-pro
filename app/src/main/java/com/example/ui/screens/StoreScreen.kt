package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.animation.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    navController: NavController,
    storeViewModel: StoreViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val profile by userViewModel.profile.collectAsState()
    val purchasedCards by storeViewModel.purchasedCards.collectAsState()
    val isLoading by storeViewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Data Recharge, 1: Google Play Cards, 2: Discount Passes, 3: My Vault
    var selectedOperatorFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Jio, 2: Airtel
    var selectedRechargePlan by remember { mutableStateOf<DataRechargePlan?>(null) }
    var selectedItemForPurchase by remember { mutableStateOf<StoreItem?>(null) }
    var newlyPurchasedCard by remember { mutableStateOf<UserPurchasedCard?>(null) }
    val userCoins = profile?.appMoney ?: 0
    val userName = profile?.name?.ifBlank { "PRO GAMER" } ?: "PRO GAMER"
    val storeSettings by storeViewModel.storeSettings.collectAsState()
    // Dialog for Confirming Purchase
    selectedItemForPurchase?.let { item ->
        val effectivePrice = storeViewModel.getEffectivePrice(item)
        val isItemActive = storeViewModel.isItemEnabled(item)
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { if (!isLoading) selectedItemForPurchase = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (item.category == StoreItemCategory.GOOGLE_PLAY) Icons.Default.PlayArrow else Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Confirm Redemption", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Are you sure you want to unlock ${item.title}?",
                        color = Color.DarkGray,
                        fontSize = 13.sp
                    )
                    // Price Breakdown Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A0B10))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Item Price:", color = Color(0xFF8E93A6), fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("$effectivePrice Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Your Balance:", color = Color(0xFF8E93A6), fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("$userCoins Coins", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Divider(color = Color(0xFFE5E7EB), modifier = Modifier.padding(vertical = 2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining After:", color = Color(0xFF8E93A6), fontSize = 12.sp)
                                val remaining = userCoins - effectivePrice
                                Text(
                                    "$remaining Coins",
                                    color = if (remaining >= 0) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    if (!isItemActive) {
                        Text(
                            "⛔ This item is temporarily out of stock / blocked by Admin.",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (userCoins < effectivePrice) {
                        Text(
                            "You do not have enough coins! Play matches, spin the wheel, or watch videos to earn more.",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        storeViewModel.purchaseItem(
                            item = item,
                            userCoins = userCoins,
                            onSuccess = { purchased ->
                                selectedItemForPurchase = null
                                newlyPurchasedCard = purchased
                                Toast.makeText(context, "${item.title} Unlocked Successfully!", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isItemActive) Color(0xFFFFD700) else Color(0xFF333333)
                    ),
                    enabled = isItemActive && userCoins >= effectivePrice && !isLoading,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            if (!isItemActive) "OUT OF STOCK / BLOCKED" else "UNLOCK WITH $effectivePrice COINS",
                            color = if (isItemActive) Color.Black else Color(0xFF888888),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            },
            dismissButton = {
                if (!isLoading) {
                    TextButton(onClick = { selectedItemForPurchase = null }) {
                        Text("Cancel", color = Color(0xFF8E93A6))
                    }
                }
            }
        )
    }
    // Modal Dialog for Newly Purchased Card (Scratch & Reveal Code!)
    newlyPurchasedCard?.let { card ->
        CelebrationCardDialog(
            card = card,
            userName = userName,
            onDismiss = {
                newlyPurchasedCard = null
                selectedTab = 3 // Switch to My Vault tab
            }
        )
    }
    // Dialog for Mobile Data Recharge Confirmation
    selectedRechargePlan?.let { plan ->
        DataRechargeDialog(
            plan = plan,
            userCoins = userCoins,
            isLoading = isLoading,
            onDismiss = { if (!isLoading) selectedRechargePlan = null },
            onConfirm = { mobileNumber ->
                storeViewModel.requestDataRecharge(
                    plan = plan,
                    mobileNumber = mobileNumber,
                    userCoins = userCoins,
                    userName = userName,
                    onSuccess = { msg ->
                        selectedRechargePlan = null
                        selectedTab = 3 // Switch to vault
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
    Scaffold(
        containerColor = Color(0xFFF7F7FA),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "GAMING REWARDS STORE",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.Black,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            "Jio & Airtel Data Packs, Google Play & VIP Cards",
                            fontSize = 10.sp,
                            color = Color(0xFF8E93A6)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    // Live Coin Balance Pill in Top Bar
                    Row(
                        modifier = Modifier
                            .padding(end = 14.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .clickable {
                                Toast.makeText(context, "Coins Balance: $userCoins\nEarn coins via Spin Wheel & Daily Check-in!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "$userCoins",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Tabs Selector (Clean with no emojis)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141620))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(
                    Triple("Data Packs", Icons.Default.Bolt, 0),
                    Triple("Google Play", Icons.Default.PlayArrow, 1),
                    Triple("VIP Passes", Icons.Default.ConfirmationNumber, 2),
                    Triple("My Vault (${purchasedCards.size})", Icons.Default.Lock, 3)
                )
                tabs.forEachIndexed { index, tabItem ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF262626) else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                tabItem.second,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFF8E93A6),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tabItem.first,
                                color = if (isSelected) Color.White else Color(0xFF8E93A6),
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            // Tab Content
            when (selectedTab) {
                0 -> {
                    // 0. MOBILE DATA RECHARGE (Jio & Airtel)
                    val jioPlans = storeViewModel.jioRechargePlans
                    val airtelPlans = storeViewModel.airtelRechargePlans
                    val displayedPlans = when (selectedOperatorFilter) {
                        1 -> jioPlans
                        2 -> airtelPlans
                        else -> jioPlans + airtelPlans
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Operator Selection Filter Chips
                        item {
                            ScrollableTabRow(
                                selectedTabIndex = selectedOperatorFilter as Int,
                                containerColor = Color.Transparent,
                                divider = {},
                                edgePadding = 0.dp,
                                indicator = {} // We will use custom backgrounds inside the tabs instead of an indicator line
                            ) {
                                listOf(
                                    Triple("ALL PLANS (${jioPlans.size + airtelPlans.size})", 0, null),
                                    Triple("JIO 4G/5G (${jioPlans.size})", 1, "JIO"),
                                    Triple("AIRTEL 5G (${airtelPlans.size})", 2, "AIRTEL")
                                ).forEachIndexed { index, (title, filterIdx, op) ->
                                    val isSelected = selectedOperatorFilter == filterIdx
                                    Tab(
                                        selected = isSelected,
                                        onClick = { selectedOperatorFilter = filterIdx as Int },
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) Color(0xFF141414) else Color.White
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color.Transparent else Color(0xFFE5E7EB),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = title as String,
                                                color = if (isSelected) Color.White else Color(0xFF8E93A6),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Recharge Plan Cards
                        items(displayedPlans) { plan ->
                            DataRechargeCard(
                                plan = plan,
                                userCoins = userCoins,
                                onRedeemClick = { selectedRechargePlan = plan }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
                1 -> {
                    // Google Play Cards List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(storeViewModel.googlePlayCards) { item ->
                            val effectivePrice = storeViewModel.getEffectivePrice(item)
                            val isItemActive = storeViewModel.isItemEnabled(item)
                            val dailyLimit = storeViewModel.getEffectiveDailyLimit(item)
                            val codesCount = storeViewModel.getAvailableCodesCount(item)
                            FamPayGooglePlayCard(
                                item = item,
                                effectivePrice = effectivePrice,
                                isEnabled = isItemActive,
                                dailyLimit = dailyLimit,
                                customCodesCount = codesCount,
                                userName = userName,
                                userCoins = userCoins,
                                onBuyClick = { selectedItemForPurchase = item },
                                onRequestRestock = { 
                                    storeViewModel.requestRestock(item, 
                                        onSuccess = { Toast.makeText(context, "Restock request sent to Admin!", Toast.LENGTH_SHORT).show() },
                                        onError = { Toast.makeText(context, "Failed to send request.", Toast.LENGTH_SHORT).show() }
                                    ) 
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
                2 -> {
                    // App Tournament Discount Cards
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(storeViewModel.appDiscountCards) { item ->
                            val effectivePrice = storeViewModel.getEffectivePrice(item)
                            val isItemActive = storeViewModel.isItemEnabled(item)
                            val dailyLimit = storeViewModel.getEffectiveDailyLimit(item)
                            val codesCount = storeViewModel.getAvailableCodesCount(item)
                            TournamentDiscountCard(
                                item = item,
                                effectivePrice = effectivePrice,
                                isEnabled = isItemActive,
                                dailyLimit = dailyLimit,
                                customCodesCount = codesCount,
                                userName = userName,
                                userCoins = userCoins,
                                onBuyClick = { selectedItemForPurchase = item }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(40.dp)) }
                    }
                }
                3 -> {
                    // My Vault / Purchased Cards
                    if (purchasedCards.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF181B26)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(36.dp))
                                }
                                Text("Your Vault is Empty", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text(
                                    "You haven't unlocked any Google Play cards or VIP Discount Passes yet. Earn coins and redeem your first card!",
                                    color = Color(0xFF8E93A6),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { selectedTab = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("EXPLORE STORE", color = Color.Black, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("My Unlocked Cards & Codes", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                    Text("${purchasedCards.size} Items", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            items(purchasedCards) { card ->
                                PurchasedCardVaultItem(
                                    card = card,
                                    userName = userName,
                                    onMarkUsed = {
                                        storeViewModel.markCardUsed(card.id)
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }
}
// ----------------------------------------------------
// Diamond Transparent Glass Google Play Card Component
// ----------------------------------------------------
@Composable
fun FamPayGooglePlayCard(
    item: StoreItem,
    effectivePrice: Int = item.coinPrice,
    isEnabled: Boolean = true,
    dailyLimit: Int = 2,
    customCodesCount: Int = 0,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit,
    onRequestRestock: () -> Unit
) {
    val canAfford = userCoins >= effectivePrice && isEnabled
    val isOutOfStock = customCodesCount <= 0

    // Animation for metallic sheen
    val infiniteTransition = rememberInfiniteTransition(label = "metalSheen")
    val sheenX by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenX"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFE2E8F0).copy(alpha = 0.2f))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141414)) // Base Dark
            .clickable(enabled = canAfford && !isOutOfStock) { onBuyClick() }
    ) {
        // Metallic Canvas Area
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Base brushed metal gradient
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A2A),
                            Color(0xFF1E1E1E),
                            Color(0xFF2F2F2F),
                            Color(0xFF1A1A1A)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                // Brushed lines texture (subtle)
                for (i in 0..100) {
                    val lineY = (h / 100) * i
                    drawLine(
                        color = Color.White.copy(alpha = 0.02f),
                        start = Offset(0f, lineY),
                        end = Offset(w, lineY),
                        strokeWidth = 1f
                    )
                }

                // Cyber/Icy Blue subtle accent lines
                drawLine(
                    color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                    start = Offset(w * 0.1f, 0f),
                    end = Offset(w * 0.9f, h),
                    strokeWidth = 2f
                )

                // Moving Metallic Sheen
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(sheenX - 200f, -200f),
                    end = Offset(sheenX + 200f, h + 200f)
                )
                drawRect(brush = sheenBrush, blendMode = BlendMode.Screen)
            }

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Logo & Price
                val cleanDenom = when {
                    item.denominationRupees > 0 -> "₹${item.denominationRupees}"
                    item.title.contains("₹") -> {
                        val num = item.title.filter { it.isDigit() }
                        if (num.isNotBlank()) "₹$num" else item.title
                    }
                    else -> {
                        val num = item.title.filter { it.isDigit() }
                        if (num.isNotBlank()) "₹$num" else item.title
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GooglePlayLogoIcon(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "GOOGLE PLAY",
                            color = Color(0xFFE2E8F0), // Silver
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp
                        )
                    }
                    Text(
                        cleanDenom,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Middle: Clean vector aesthetic
                Text(
                    "DIGITAL GIFT VOUCHER",
                    color = Color(0xFFA0AAB2), // Metallic Gray
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 4.sp
                )

                // Bottom: Button Area
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "REQUIRED",
                            color = Color(0xFF555555),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            "$effectivePrice COINS",
                            color = Color(0xFFE2E8F0), // Icy Cyan
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    if (isOutOfStock) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF262626))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .clickable { onRequestRestock() }
                        ) {
                            Text(
                                "OUT OF STOCK",
                                color = Color(0xFFA0AAB2),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (canAfford) Color(0xFFE2E8F0) else Color(0xFF333333))
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                "REDEEM",
                                color = if (canAfford) Color.Black else Color(0xFFA0AAB2),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PurchasedCardVaultItem(
    card: UserPurchasedCard,
    userName: String,
    onMarkUsed: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var isCodeCopied by remember { mutableStateOf(false) }
    var isRevealed by remember { mutableStateOf(false) }

    val isDataRecharge = card.category.equals("DATA_RECHARGE", ignoreCase = true)
    val isGooglePlay = card.category.equals("GOOGLE_PLAY", ignoreCase = true)
    val isUsed = card.status.equals("USED", ignoreCase = true)

    val proofBase64String = card.proofScreenshotBase64.ifBlank { card.screenshotBase64 }
    val effectiveRechargeTxnId = card.rechargeTxnId.ifBlank { card.operatorTxnId }

    var showProofDialog by remember { mutableStateOf(false) }

    val proofBitmap = remember(proofBase64String) {
        if (proofBase64String.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(proofBase64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    if (showProofDialog && proofBitmap != null) {
        Dialog(onDismissRequest = { showProofDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Recharge Confirmation", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("Official receipt from admin", color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showProofDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                        }
                    }

                    Image(
                        bitmap = proofBitmap.asImageBitmap(),
                        contentDescription = "Recharge Proof Screenshot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )

                    if (effectiveRechargeTxnId.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE5E7EB))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ref ID / UTR: $effectiveRechargeTxnId", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = { showProofDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("CLOSE PROOF", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Main Card
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141414)) // Hardcore Black Base
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    card.category.replace("_", " ").uppercase(),
                    color = Color(0xFFA0AAB2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    card.title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, if (isUsed) Color(0xFF444444) else Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isUsed) "USED" else "ACTIVE",
                    color = if (isUsed) Color(0xFF444444) else Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }
        }

        // Details and Blur Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isDataRecharge) {
                // Data Recharge Proof View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "STATUS",
                        color = Color(0xFF555555),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        if (proofBitmap != null) "SUCCESSFUL" else "PENDING",
                        color = if (proofBitmap != null) Color(0xFFE2E8F0) else Color(0xFF8E93A6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF262626))
                        .clickable(enabled = proofBitmap != null) {
                            if (proofBitmap != null) showProofDialog = true
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (proofBitmap != null) "VIEW RECEIPT" else "PROCESSING...",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                }
            } else {
                // Redeem Code / Gift Card View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "REDEEM CODE",
                        color = Color(0xFF555555),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                }

                // Blur container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF262626))
                        .clickable(enabled = card.code.isNotBlank()) {
                            if (!isRevealed && card.code.isNotBlank()) {
                                isRevealed = true
                            } else if (isRevealed && card.code.isNotBlank()) {
                                clipboardManager.setPrimaryClip(ClipData.newPlainText("Code", card.code))
                                isCodeCopied = true
                                Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // We apply blur modifier to the text if it's not revealed
                    val codeToDisplay = card.code.ifBlank { "PENDING..." }
                    
                    Text(
                        text = codeToDisplay,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 4.sp,
                        modifier = Modifier.then(
                            if (!isRevealed && card.code.isNotBlank()) androidx.compose.ui.Modifier.blur(radius = 8.dp) else androidx.compose.ui.Modifier
                        )
                    )
                    
                    // Overlay for Tap to Claim (only when blurred)
                    if (!isRevealed && card.code.isNotBlank()) {
                        Text(
                            "TAP TO CLAIM",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 2.sp,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                if (isRevealed) {
                    Text(
                        text = if (isCodeCopied) "COPIED TO CLIPBOARD" else "TAP TO COPY CODE",
                        color = Color(0xFFA0AAB2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        // Mark Used Button
        if (!isUsed && (card.code.isNotBlank() || proofBitmap != null)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { onMarkUsed() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "MARK AS CLAIMED",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun CelebrationCardDialog(
    card: UserPurchasedCard,
    userName: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉 SUCCESS!", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("You bought ${card.title}", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0))) {
                    Text("AWESOME", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun TournamentDiscountCard(
    item: StoreItem,
    effectivePrice: Int,
    isEnabled: Boolean,
    dailyLimit: Int,
    customCodesCount: Int,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit
) {
    val canAfford = userCoins >= effectivePrice && isEnabled
    val isOutOfStock = customCodesCount <= 0

    val infiniteTransition = rememberInfiniteTransition(label = "titaniumSheen")
    val sheenX by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenX"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color.Black)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D0D0D)) // Titanium Dark
            .clickable(enabled = canAfford && !isOutOfStock) { onBuyClick() }
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Titanium gradient
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1F1F1F),
                            Color(0xFF333333),
                            Color(0xFF141414)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                // Diagonal polished lines
                for (i in -50..150) {
                    val startX = (w / 100) * i
                    drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(startX, 0f),
                        end = Offset(startX + h, h),
                        strokeWidth = 2f
                    )
                }

                // Moving Metallic Sheen
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    start = Offset(sheenX - 100f, 0f),
                    end = Offset(sheenX + 100f, h)
                )
                drawRect(brush = sheenBrush, blendMode = BlendMode.Screen)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFFE2E8F0), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "VIP PASS",
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 3.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "EXCLUSIVE",
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Text(
                    item.title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )

                // Bottom Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "$effectivePrice COINS",
                        color = Color(0xFFA0AAB2),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )

                    if (isOutOfStock) {
                        Text(
                            "OUT OF STOCK",
                            color = Color(0xFF444444),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (canAfford) Color(0xFFE2E8F0) else Color(0xFF333333))
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                "ACQUIRE",
                                color = if (canAfford) Color.Black else Color(0xFFA0AAB2),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GooglePlayLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Google Play 4-Color Official Vector geometry
        // 1. Blue base quadrilateral (left)
        val pathBlue = Path().apply {
            moveTo(0f, h * 0.05f)
            lineTo(w * 0.58f, h * 0.5f)
            lineTo(0f, h * 0.95f)
            close()
        }
        drawPath(pathBlue, color = Color(0xFF00C3FF))
        // 2. Green top triangle
        val pathGreen = Path().apply {
            moveTo(0f, h * 0.05f)
            lineTo(w * 0.78f, h * 0.35f)
            lineTo(w * 0.58f, h * 0.5f)
            close()
        }
        drawPath(pathGreen, color = Color(0xFF00E676))
        // 3. Red bottom triangle
        val pathRed = Path().apply {
            moveTo(0f, h * 0.95f)
            lineTo(w * 0.58f, h * 0.5f)
            lineTo(w * 0.78f, h * 0.65f)
            close()
        }
        drawPath(pathRed, color = Color(0xFFFF334B))
        // 4. Yellow right apex triangle
        val pathYellow = Path().apply {
            moveTo(w * 0.58f, h * 0.5f)
            lineTo(w * 0.78f, h * 0.35f)
            lineTo(w * 0.98f, h * 0.5f)
            lineTo(w * 0.78f, h * 0.65f)
            close()
        }
        drawPath(pathYellow, color = Color(0xFFFFD400))
    }
}
@Composable
fun StoreHeroBanner(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF141724), Color(0xFF0E1018))
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(gradientColors.map { it.copy(alpha = 0.5f) }),
                RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = Color(0xFF8E93A6), fontSize = 11.sp, lineHeight = 15.sp)
            }
        }
    }
}
// ----------------------------------------------------
// Real Jio & Airtel Logos (Official Brand Aesthetics)
// ----------------------------------------------------
@Composable
fun JioBrandLogo(modifier: Modifier = Modifier, size: Int = 36) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F3EBD), Color(0xFF0A2885))
                )
            )
            .border(1.2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Jio",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size * 0.44f).sp,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-0.5).sp
        )
    }
}
@Composable
fun AirtelBrandLogo(modifier: Modifier = Modifier, size: Int = 36) {
    Box(
        modifier = modifier
            .height(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFEE0000), Color(0xFFB30000))
                )
            )
            .border(1.2.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .padding(horizontal = (size * 0.22f).dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "airtel",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size * 0.38f).sp,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-0.3).sp
        )
    }
}
// ----------------------------------------------------
// Premium Glassmorphic Mobile Data Card (Jio & Airtel)
// Designed with Material 3 Glassmorphic Glow Aesthetics
// ----------------------------------------------------
@Composable
fun DataRechargeCard(
    plan: DataRechargePlan,
    userCoins: Int,
    isLoading: Boolean = false,
    onRedeemClick: (DataRechargePlan) -> Unit
) {
    val canAfford = userCoins >= plan.coinPrice && !isLoading
    val isJio = plan.operator.equals("JIO", ignoreCase = true)

    val infiniteTransition = rememberInfiniteTransition(label = "darkSheen")
    val sheenX by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenX"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111)) // Deep Obsidian Black
            .clickable(enabled = canAfford && !isLoading) { onRedeemClick(plan) }
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Tech grid pattern
                val gridSize = 30f
                for (x in 0..(w / gridSize).toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.03f),
                        start = Offset(x * gridSize, 0f),
                        end = Offset(x * gridSize, h),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(h / gridSize).toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.03f),
                        start = Offset(0f, y * gridSize),
                        end = Offset(w, y * gridSize),
                        strokeWidth = 1f
                    )
                }

                // Moving Metallic Sheen
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    start = Offset(sheenX - 300f, -300f),
                    end = Offset(sheenX + 300f, h + 300f)
                )
                drawRect(brush = sheenBrush)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isJio) JioBrandLogo(size = 28) else AirtelBrandLogo(size = 24)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "${plan.operator} DATA",
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 2.sp
                        )
                    }
                    Text(
                        plan.dataAmount,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Middle Details
                Text(
                    ("${plan.operator} PACK - ${plan.validity}").uppercase(),
                    color = Color(0xFFA0AAB2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )

                // Bottom Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "${plan.coinPrice} COINS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (canAfford) Color(0xFFE2E8F0) else Color(0xFF333333))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "TOP UP",
                            color = if (canAfford) Color.Black else Color(0xFFA0AAB2),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Mobile Data Recharge Confirmation Dialog
// ----------------------------------------------------
@Composable
fun DataRechargeDialog(
    plan: DataRechargePlan,
    userCoins: Int,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var mobileNumber by remember { mutableStateOf("") }
    val isJio = plan.operator.equals("JIO", ignoreCase = true)
    val isValid = mobileNumber.length == 10 && mobileNumber[0] in listOf('6', '7', '8', '9')
    val canAfford = userCoins >= plan.coinPrice
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header with Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (isJio) JioBrandLogo(size = 32) else AirtelBrandLogo(size = 28)
                        Column {
                            Text(
                                text = "${plan.operator} Data Recharge",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "₹${plan.priceRupees} Pack • ${plan.dataAmount}",
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, enabled = !isLoading) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
                HorizontalDivider(color = Color.LightGray)
                // Pack Details Highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "⚡ Selected: ${plan.dataAmount} high-speed data",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Validity: ${plan.validity} • Official Price: ₹${plan.priceRupees}",
                            color = Color.DarkGray,
                            fontSize = 10.sp
                        )
                    }
                }
                // Mobile Number Input Field
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ENTER 10-DIGIT ${plan.operator} NUMBER",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 10) mobileNumber = filtered
                        },
                        placeholder = { Text("e.g. 9876543210", color = Color.Gray, fontSize = 13.sp) },
                        prefix = {
                            Text("+91 ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF262626),
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    )
                    if (mobileNumber.isNotEmpty() && !isValid) {
                        Text("Enter a valid 10-digit mobile number", color = Color.Red, fontSize = 10.sp)
                    } else if (isValid) {
                        Text("✅ Valid Indian mobile number", color = Color(0xFF16A34A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                // Coin Deduction Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF9FAFB))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recharge Cost:", color = Color.Gray, fontSize = 11.sp)
                            Text("${plan.coinPrice} Coins", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Your Coins:", color = Color.Gray, fontSize = 11.sp)
                            Text("$userCoins Coins", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("After Recharge:", color = Color.Gray, fontSize = 11.sp)
                            Text("${userCoins - plan.coinPrice} Coins", color = Color(0xFF16A34A), fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
                // Strict Notice Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️ ", fontSize = 10.sp)
                            Text(
                                "महत्वपूर्ण सूचना / Notice:",
                                color = Color.Red,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "अगर मोबाइल नंबर गलत हुआ तो यूजर की खुद की जिम्मेदारी होगी।",
                            color = Color.DarkGray,
                            fontSize = 9.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onConfirm(mobileNumber) },
                        enabled = isValid && canAfford && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF262626), // Black/Dark Gray button
                            disabledContainerColor = Color.LightGray
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).height(42.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = if (isValid && canAfford) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RECHARGE NOW", color = if (isValid && canAfford) Color.White else Color.Gray, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
