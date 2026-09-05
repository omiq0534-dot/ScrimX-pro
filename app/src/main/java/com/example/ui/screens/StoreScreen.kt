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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
            containerColor = Color(0xFF13151F),
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
                    Text("Confirm Redemption", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Are you sure you want to unlock ${item.title}?",
                        color = Color(0xFFC4C8D8),
                        fontSize = 13.sp
                    )
                    // Price Breakdown Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A0B10))
                            .border(1.dp, Color(0xFF262938), RoundedCornerShape(12.dp))
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
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("$userCoins Coins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Divider(color = Color(0xFF262938), modifier = Modifier.padding(vertical = 2.dp))
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
        containerColor = Color(0xFF0B0C10),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "GAMING REWARDS STORE",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Live Coin Balance Pill in Top Bar
                    Row(
                        modifier = Modifier
                            .padding(end = 14.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF171924))
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
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0C10)
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
                            .background(if (isSelected) Color(0xFFFFD700) else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                tabItem.second,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else Color(0xFF8E93A6),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tabItem.first,
                                color = if (isSelected) Color.Black else Color(0xFF8E93A6),
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
                                                    if (isSelected) {
                                                        when (op) {
                                                            "JIO" -> Color(0xFFD32F2F) // Vibrant Crimson
                                                            "AIRTEL" -> Color(0xFFE53935)
                                                            else -> Color(0xFFFFC107) // Neon Amber/Yellow
                                                        }
                                                    } else Color(0xFF131622)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color.Transparent else Color(0xFF262A3C),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = title as String,
                                                color = if (isSelected) (if (op == null) Color.Black else Color.White) else Color(0xFF8E93A6),
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
                                Text("Your Vault is Empty", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
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
                                    Text("My Unlocked Cards & Codes", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
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
    var isBought by remember { mutableStateOf(false) }

    // Continuous Shimmer / Sheen Animation
    val infiniteTransition = rememberInfiniteTransition(label = "googlePlayShimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0C0E17))
            .border(1.dp, if (isBought) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFF1E2338), RoundedCornerShape(22.dp))
            .padding(12.dp)
    ) {
        // Physical Diamond Glass Gift Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF061520),
                            Color(0xFF0B2433),
                            Color(0xFF04101A),
                            Color(0xFF081C29)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 900f)
                    )
                )
                .border(
                    0.8.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.5f),
                            Color(0xFF00E676).copy(alpha = 0.5f),
                            Color(0xFF00B0FF).copy(alpha = 0.4f),
                            Color(0xFFFFD700).copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(800f, 600f)
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            // Diamond Glass Facets & Moving Shimmer Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Subtle diamond crystalline geometry
                val facetPath = Path().apply {
                    moveTo(w * 0.7f, 0f)
                    lineTo(w, h * 0.45f)
                    lineTo(w * 0.5f, h)
                    lineTo(0f, h * 0.35f)
                    close()
                }
                drawPath(
                    path = facetPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.05f), Color.Transparent),
                        start = Offset(w * 0.7f, 0f),
                        end = Offset(0f, h)
                    )
                )

                // Dynamic Moving Shimmer Sheen Beam
                val sheenWidth = 140f
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.12f),
                        Color(0xFF00E5FF).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - sheenWidth, 0f),
                    end = Offset(shimmerTranslate + sheenWidth, h)
                )
                drawRect(brush = sheenBrush)

                // Sparkling Diamond Stars (✦)
                drawCircle(Color(0xFF00E5FF).copy(alpha = 0.7f), radius = 2.dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                drawCircle(Color.White.copy(alpha = 0.9f), radius = 1.2.dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                drawCircle(Color(0xFF69F0AE).copy(alpha = 0.6f), radius = 1.5.dp.toPx(), center = Offset(w * 0.92f, h * 0.38f))
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Authentic Google Play Logo + Denomination Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google Play Brand Logo & Text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF020D14).copy(alpha = 0.7f))
                            .border(0.8.dp, Color(0xFF00E5FF).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        GooglePlayLogoIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(7.dp))
                        Column {
                            Text(
                                "Google Play",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.5.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "DIGITAL GIFT VOUCHER",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 7.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Diamond Glass Denomination Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF00E5FF), Color(0xFF00E676))
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "₹${item.denominationRupees}",
                            color = Color(0xFF02131D),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                // Middle Row: Buy-To-Reveal Code Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF030914).copy(alpha = 0.85f))
                        .border(1.dp, if (isBought) Color(0xFF06B6D4) else Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Crossfade(targetState = isBought, animationSpec = tween(durationMillis = 600), label = "Reveal") { revealed ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!revealed) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Protected Code",
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "GP •••• 8K9F ••••",
                                        color = Color(0xFFE0F7FA),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.2.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "16-CHAR CODE",
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.5.sp
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy Code",
                                        tint = Color(0xFF06B6D4),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "GPK3-9M2X-8K9F-2B4L",
                                        color = Color(0xFF06B6D4),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Row: Gamer Name & Limit / In-Stock Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "CLAIMANT",
                            color = Color(0xFF6F8299),
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.5.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            userName.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF14161F))
                            .border(0.5.dp, Color(0xFF262A38), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (customCodesCount > 0) "IN STOCK" else "LIMIT $dailyLimit/DAY",
                            color = if (customCodesCount > 0) Color(0xFF00E676) else Color(0xFF6F8299),
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Store Purchase Interaction
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(targetValue = if (isPressed && !isBought) 0.95f else 1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f))

        var hasRequestedRestock by remember { mutableStateOf(false) }
        val isOutOfStock = customCodesCount <= 0

        Button(
            onClick = { 
                if (isOutOfStock && !hasRequestedRestock) {
                    hasRequestedRestock = true
                    onRequestRestock()
                } else if (!isOutOfStock && !isBought) {
                    isBought = true
                    onBuyClick() 
                }
            },
            enabled = (canAfford || isBought || isOutOfStock) && !hasRequestedRestock,
            interactionSource = interactionSource,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isBought) Color(0xFF22C55E).copy(alpha = 0.15f) else if (isOutOfStock) Color(0xFFE11D48).copy(alpha = 0.15f) else Color(0xFFFFD700),
                disabledContainerColor = if (isBought) Color(0xFF22C55E).copy(alpha = 0.15f) else if (isOutOfStock) Color(0xFFE11D48).copy(alpha = 0.15f) else Color(0xFF1E2338)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .scale(scale)
                .border(
                    1.dp,
                    if (isBought) Color(0xFF22C55E) else if (isOutOfStock) Color(0xFFE11D48).copy(alpha = 0.5f) else Color.Transparent,
                    RoundedCornerShape(14.dp)
                )
        ) {
            if (isBought) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "CLAIMED SUCCESSFULLY",
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            } else if (isOutOfStock) {
                if (hasRequestedRestock) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "RESTOCK REQUESTED",
                        color = Color(0xFFE11D48),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "OUT OF STOCK - REQUEST ADMIN",
                        color = Color(0xFFE11D48),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                Text(
                    "REDEEM FOR",
                    color = if (canAfford) Color.Black else Color(0xFF8E92A4),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = if (canAfford) Color.Black else Color(0xFF8E92A4),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "$effectivePrice",
                    color = if (canAfford) Color.Black else Color(0xFF8E92A4),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
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
                    .background(Color(0xFF14161F))
                    .border(1.5.dp, Color(0xFF22C55E).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
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
                            Text("Recharge Confirmation", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("Official receipt from admin", color = Color(0xFF22C55E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showProofDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
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
                                .background(Color(0xFF0A0C14))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ref ID / UTR: $effectiveRechargeTxnId", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Text(
                        "Number: ${card.code} • Plan: ${card.title}",
                        color = Color(0xFFB0BEC5),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { showProofDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("CLOSE PROOF", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Vault Card UI (Ultra-Premium Redesign)
    val cardColor = if (isUsed) Color(0xFF1E2338) else if (isDataRecharge) Color(0xFFE11D48) else Color(0xFF06B6D4)
    val glowColor = if (isUsed) Color.Transparent else cardColor.copy(alpha = 0.3f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0B0F14))
            .border(1.dp, glowColor, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(cardColor.copy(alpha = 0.15f))
                        .border(1.dp, cardColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isDataRecharge) Icons.Default.PhoneIphone else Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = cardColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        card.title.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        card.category,
                        color = cardColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isUsed) Color(0xFF1E2338) else Color(0xFF22C55E).copy(alpha = 0.15f))
                    .border(1.dp, if (isUsed) Color.Transparent else Color(0xFF22C55E).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isUsed) "USED" else "ACTIVE",
                    color = if (isUsed) Color(0xFF6F8299) else Color(0xFF22C55E),
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Code / Details Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF131922))
                .border(1.dp, Color(0xFF1E2338), RoundedCornerShape(12.dp))
                .clickable(enabled = card.code.isNotBlank() && !isDataRecharge) {
                    if (card.code.isNotBlank()) {
                        val clip = ClipData.newPlainText("Redeem Code", card.code)
                        clipboardManager.setPrimaryClip(clip)
                        isCodeCopied = true
                        Toast.makeText(context, "Code Copied!", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (isDataRecharge) "TARGET NUMBER" else "REDEEM CODE",
                        color = Color(0xFF6F8299),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        card.code.ifBlank { "PENDING ADMIN APPROVAL" },
                        color = if (card.code.isNotBlank()) (if (isUsed) Color(0xFF8E92A4) else Color(0xFFFACC15)) else Color(0xFF6F8299),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
                
                if (!isDataRecharge && card.code.isNotBlank()) {
                    Icon(
                        if (isCodeCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = if (isCodeCopied) Color(0xFF22C55E) else Color(0xFF6F8299),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // View Proof Button (For Data Recharge)
            if (isDataRecharge && proofBitmap != null) {
                OutlinedButton(
                    onClick = { showProofDialog = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF22C55E))
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RECEIPT", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
            
            // Mark as Used Button
            if (card.code.isNotBlank() && !isUsed) {
                Button(
                    onClick = onMarkUsed,
                    colors = ButtonDefaults.buttonColors(containerColor = cardColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("MARK AS USED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
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
                .background(Color(0xFF14161F))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉 SUCCESS!", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("You bought ${card.title}", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))) {
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF14161F))
            .border(1.dp, Color(0xFF262A38), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(item.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(item.subtitle, color = Color(0xFF6F8299), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onBuyClick,
            enabled = canAfford,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("BUY FOR ${effectivePrice} COINS", color = Color.Black, fontWeight = FontWeight.Black)
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
                Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
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
    onRedeemClick: () -> Unit
) {
    val canAfford = userCoins >= plan.coinPrice
    val isJio = plan.operator.equals("JIO", ignoreCase = true)
    val brandColor = if (isJio) Color(0xFFE53935) else Color(0xFFE53935) // Deep Crimson Red
    val ambientGlow = if (isJio) Color(0xFFD32F2F) else Color(0xFFD32F2F)
    // Interaction scale animation placeholder
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF13151F), Color(0xFF0B0D14))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        ambientGlow.copy(alpha = 0.4f),
                        Color(0xFF262A3C),
                        ambientGlow.copy(alpha = 0.1f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom ripple handled inside or via scale
                onClick = onRedeemClick
            )
            .padding(14.dp)
    ) {
        // Inner Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ambientGlow.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
                .background(Color(0xFF05060A).copy(alpha = 0.6f))
                .border(
                    0.5.dp,
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header: Brand & Price Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Operator Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF13151F))
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        if (isJio) {
                            JioBrandLogo(size = 16)
                        } else {
                            AirtelBrandLogo(size = 14)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = plan.operator.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    // Price Glass Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFFFFFFF).copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "₹${plan.priceRupees}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Center Highlight: Big Data Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = plan.dataAmount.replace(" GB", "").replace(" MB", ""),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 42.sp,
                        letterSpacing = (-1).sp,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (plan.dataAmount.contains("GB")) "GB" else "MB",
                        color = brandColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = "High-Speed Data Booster • ${plan.validity}",
                    color = Color(0xFF8E93A6),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Bottom Tags (Bestseller / Instant Top-Up)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("INSTANT", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFC107).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(plan.tagText, color = Color(0xFFFFC107), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Bottom Action Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Redeem via Coins",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            // Coin Action Button
            Button(
                onClick = onRedeemClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) Color(0xFFFFC107).copy(alpha = 0.1f) else Color(0xFF1E2232)
                ),
                border = BorderStroke(
                    1.dp,
                    if (canAfford) Color(0xFFFFC107).copy(alpha = 0.6f) else Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (canAfford) Color(0xFFFFC107) else Color(0xFF8E93A6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${plan.coinPrice} COINS",
                        color = if (canAfford) Color(0xFFFFC107) else Color(0xFF8E93A6),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
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
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF140508))
                .border(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFFFF5252), Color(0xFFFFD700))), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Header with Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (isJio) JioBrandLogo(size = 34) else AirtelBrandLogo(size = 30)
                        Column {
                            Text(
                                text = "${plan.operator} Data Recharge",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "₹${plan.priceRupees} Pack • ${plan.dataAmount}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, enabled = !isLoading) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E93A6))
                    }
                }
                HorizontalDivider(color = Color(0xFF2E0A10))
                // Pack Details Highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF22070C))
                        .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "⚡ Selected: ${plan.dataAmount} high-speed data",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Validity: ${plan.validity} • Official Price: ₹${plan.priceRupees}",
                            color = Color(0xFFFFCDD2),
                            fontSize = 11.sp
                        )
                    }
                }
                // Mobile Number Input Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ENTER 10-DIGIT ${plan.operator} NUMBER",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 10) mobileNumber = filtered
                        },
                        placeholder = { Text("e.g. 9876543210", color = Color(0xFF75798E)) },
                        prefix = {
                            Text("+91 ", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 15.sp)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF4A1017),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E070B),
                            unfocusedContainerColor = Color(0xFF1A0508)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (mobileNumber.isNotEmpty() && !isValid) {
                        Text(
                            "Enter a valid 10-digit mobile number starting with 6, 7, 8, or 9",
                            color = Color(0xFFFF5252),
                            fontSize = 10.sp
                        )
                    } else if (isValid) {
                        Text(
                            "✅ Valid Indian mobile number",
                            color = Color(0xFF00E676),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Coin Deduction Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF19060A))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recharge Cost:", color = Color(0xFF8E93A6), fontSize = 11.sp)
                            Text("${plan.coinPrice} Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Your Current Coins:", color = Color(0xFF8E93A6), fontSize = 11.sp)
                            Text("$userCoins Coins", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Coins After Recharge:", color = Color(0xFF8E93A6), fontSize = 11.sp)
                            Text("${userCoins - plan.coinPrice} Coins", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
                // Strict Notice Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2B0A0D))
                        .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️ ", fontSize = 11.sp)
                            Text(
                                "महत्वपूर्ण सूचना / Notice:",
                                color = Color(0xFFFF5252),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "अगर मोबाइल नंबर गलत हुआ तो यूजर की खुद की जिम्मेदारी होगी। कृपया अपना 10-अंकों का नंबर सही से जांच लें। रिचार्ज की पुष्टि रसीद/स्क्रीनशॉट एडमिन द्वारा प्रदान की जाएगी।",
                            color = Color(0xFFFFCDD2),
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4A1017))
                    ) {
                        Text("CANCEL", color = Color(0xFFB0BEC5), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onConfirm(mobileNumber) },
                        enabled = isValid && canAfford && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            disabledContainerColor = Color(0xFF2E0A10)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).height(46.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RECHARGE NOW", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
