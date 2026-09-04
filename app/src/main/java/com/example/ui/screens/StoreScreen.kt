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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.drawBehind
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

    // Dialog for Confirming Purchase
    selectedItemForPurchase?.let { item ->
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
                                    Text("${item.coinPrice} Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                                val remaining = userCoins - item.coinPrice
                                Text(
                                    "$remaining Coins",
                                    color = if (remaining >= 0) Color(0xFF00E676) else Color(0xFFFF5252),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    if (userCoins < item.coinPrice) {
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    enabled = userCoins >= item.coinPrice && !isLoading,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                    } else {
                        Text("UNLOCK WITH ${item.coinPrice} COINS", color = Color.Black, fontWeight = FontWeight.Black)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("ALL PLANS (${jioPlans.size + airtelPlans.size})", 0, null),
                                    Triple("JIO 4G/5G (${jioPlans.size})", 1, "JIO"),
                                    Triple("AIRTEL 5G (${airtelPlans.size})", 2, "AIRTEL")
                                ).forEach { (title, filterIdx, op) ->
                                    val isSelected = selectedOperatorFilter == filterIdx
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) {
                                                    when (op) {
                                                        "JIO" -> Color(0xFF0F3EBD)
                                                        "AIRTEL" -> Color(0xFFE40000)
                                                        else -> Color(0xFFFFD700)
                                                    }
                                                } else Color(0xFF141622)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) Color.White.copy(alpha = 0.6f) else Color(0xFF232738),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedOperatorFilter = filterIdx }
                                            .padding(vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            title,
                                            color = if (isSelected) (if (op == null) Color.Black else Color.White) else Color(0xFF8E93A6),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp
                                        )
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
                            FamPayGooglePlayCard(
                                item = item,
                                userName = userName,
                                userCoins = userCoins,
                                onBuyClick = { selectedItemForPurchase = item }
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
                            TournamentDiscountCard(
                                item = item,
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
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit
) {
    val canAfford = userCoins >= item.coinPrice

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
            .border(0.8.dp, Color(0xFF1E2338), RoundedCornerShape(22.dp))
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

                // Middle Row: Half-Hidden Google Play Scratch/Redeem Code Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF030914).copy(alpha = 0.85f))
                        .border(0.8.dp, Color(0xFF00E5FF).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Protected Code",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "GP •••• - 8K9F - ••••",
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
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                            .border(0.6.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            "${item.badgeText} • LIMIT: 2/DAY",
                            color = Color(0xFFE0F7FA),
                            fontWeight = FontWeight.Black,
                            fontSize = 8.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Info & Purchase Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.5.sp
                )
                Text(
                    item.subtitle,
                    color = Color(0xFF8E93A6),
                    fontSize = 10.5.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuyClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) Color(0xFFFFD700) else Color(0xFF1E2232)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (canAfford) Color.Black else Color(0xFF8E93A6),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${item.coinPrice} COINS",
                        color = if (canAfford) Color.Black else Color(0xFF8E93A6),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// Rich Tournament VIP Pass Card Component (App's Own Card)
// ----------------------------------------------------
@Composable
fun TournamentDiscountCard(
    item: StoreItem,
    userName: String,
    userCoins: Int,
    onBuyClick: () -> Unit
) {
    val canAfford = userCoins >= item.coinPrice

    // Rich saturated background gradients & matching accents
    val (cardBgGradient, borderGradient, accentColor) = when (item.cardColorTheme) {
        "GOD" -> Triple(
            listOf(Color(0xFF380424), Color(0xFF5E093C), Color(0xFF240217), Color(0xFF4A062F)),
            listOf(Color(0xFFFF0055).copy(alpha = 0.5f), Color(0xFFD500F9).copy(alpha = 0.5f), Color(0xFFFFD700).copy(alpha = 0.4f)),
            Color(0xFFFF2A7A)
        )
        "DIAMOND" -> Triple(
            listOf(Color(0xFF03223A), Color(0xFF0A456C), Color(0xFF021727), Color(0xFF063352)),
            listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color(0xFF2979FF).copy(alpha = 0.5f), Color(0xFF00E676).copy(alpha = 0.4f)),
            Color(0xFF00E5FF)
        )
        "GOLD" -> Triple(
            listOf(Color(0xFF382603), Color(0xFF5E4208), Color(0xFF221600), Color(0xFF473204)),
            listOf(Color(0xFFFFD700).copy(alpha = 0.5f), Color(0xFFFFA000).copy(alpha = 0.5f), Color(0xFFFFE082).copy(alpha = 0.4f)),
            Color(0xFFFFD700)
        )
        "SILVER" -> Triple(
            listOf(Color(0xFF1F2937), Color(0xFF374151), Color(0xFF111827), Color(0xFF283548)),
            listOf(Color(0xFFECEFF1).copy(alpha = 0.5f), Color(0xFFB0BEC5).copy(alpha = 0.5f), Color(0xFF78909C).copy(alpha = 0.4f)),
            Color(0xFFECEFF1)
        )
        else -> Triple(
            listOf(Color(0xFF3A1808), Color(0xFF5A270F), Color(0xFF240D04), Color(0xFF461D0A)),
            listOf(Color(0xFFFF8A65).copy(alpha = 0.5f), Color(0xFFD84315).copy(alpha = 0.5f), Color(0xFFFFCCBC).copy(alpha = 0.4f)),
            Color(0xFFFF8A65)
        )
    }

    // Continuous Shimmer / Sheen Animation
    val infiniteTransition = rememberInfiniteTransition(label = "vipPassShimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0C0E17))
            .border(0.8.dp, Color(0xFF1E2338), RoundedCornerShape(22.dp))
            .padding(12.dp)
    ) {
        // Rich Colored VIP Pass
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = cardBgGradient,
                        start = Offset(0f, 0f),
                        end = Offset(900f, 900f)
                    )
                )
                .border(0.8.dp, Brush.linearGradient(borderGradient), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            // Diagonal Hatch Watermark & Dynamic Moving Light Sheen Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Cyber diagonal subtle texture lines
                val stroke = 0.8.dp.toPx()
                var x = 0f
                while (x < w * 2) {
                    drawLine(
                        color = accentColor.copy(alpha = 0.04f),
                        start = Offset(x, 0f),
                        end = Offset(x - h, h),
                        strokeWidth = stroke
                    )
                    x += 20.dp.toPx()
                }

                // Ambient Radial Glow at top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * 0.9f, h * 0.1f),
                        radius = w * 0.45f
                    ),
                    radius = w * 0.45f,
                    center = Offset(w * 0.9f, h * 0.1f)
                )

                // Dynamic Moving Shimmer Sheen Beam
                val sheenWidth = 140f
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.14f),
                        accentColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - sheenWidth, 0f),
                    end = Offset(shimmerTranslate + sheenWidth, h)
                )
                drawRect(brush = sheenBrush)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: App VIP Branding + Big Discount Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(0.8.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(7.dp))
                        Column {
                            Text(
                                "SCRIMX VIP PASS",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                "TOURNAMENT DISCOUNT PASS",
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 7.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Big Discount Amount Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val discText = when {
                            item.discountPercent > 0 -> "${item.discountPercent}% OFF"
                            else -> "₹${item.discountFlatRupees} OFF"
                        }
                        Text(
                            discText,
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }

                // Middle Row: Half-Hidden VIP Discount Pass Code Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(0.8.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ConfirmationNumber,
                                contentDescription = "Pass Code",
                                tint = accentColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val maskedSnippet = if (item.discountPercent > 0) "${item.discountPercent}OFF" else "FLAT${item.discountFlatRupees}"
                            Text(
                                "PASS •••• - $maskedSnippet - ••••",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${item.maxUses} MATCH ENTRIES",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }

                // Bottom Row: Gamer Name & Tier Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "PASS HOLDER",
                            color = Color(0xFFB0BEC5),
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.5.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            userName.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(0.6.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = accentColor, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            item.badgeText,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Info & Purchase Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.5.sp
                )
                Text(
                    item.subtitle,
                    color = Color(0xFF8E93A6),
                    fontSize = 10.5.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuyClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) accentColor else Color(0xFF1E2232)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (canAfford) Color.Black else Color(0xFF8E93A6),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${item.coinPrice} COINS",
                        color = if (canAfford) Color.Black else Color(0xFF8E93A6),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// My Vault Card Item (Purchased Cards & Codes)
// ----------------------------------------------------
@Composable
fun PurchasedCardVaultItem(
    card: UserPurchasedCard,
    userName: String,
    onMarkUsed: () -> Unit
) {
    val context = LocalContext.current
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
                    .border(1.5.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
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
                            Text("Recharge Confirmation Proof", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("Official receipt from admin top-up desk", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                            Text("Ref ID / UTR: $effectiveRechargeTxnId", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("CLOSE PROOF", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Continuous Shimmer / Sheen Animation
    val infiniteTransition = rememberInfiniteTransition(label = "vaultCardShimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vaultShimmerTranslate"
    )

    // Themes for Vault Cards
    val (cardBgGradient, borderGradient, accentColor) = if (isDataRecharge) {
        Triple(
            listOf(Color(0xFF380005), Color(0xFF5E050D), Color(0xFF220003), Color(0xFF450209)),
            listOf(Color(0xFFFF5252).copy(alpha = 0.6f), Color(0xFFFFD700).copy(alpha = 0.5f), Color(0xFFFF5252).copy(alpha = 0.4f)),
            Color(0xFFFF5252)
        )
    } else if (isGooglePlay) {
        Triple(
            listOf(Color(0xFF061520), Color(0xFF0B2433), Color(0xFF04101A), Color(0xFF081C29)),
            listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color(0xFF00E676).copy(alpha = 0.5f), Color(0xFF00B0FF).copy(alpha = 0.4f)),
            Color(0xFF00E5FF)
        )
    } else {
        when (card.cardColorTheme.uppercase()) {
            "GOD" -> Triple(
                listOf(Color(0xFF380424), Color(0xFF5E093C), Color(0xFF240217), Color(0xFF4A062F)),
                listOf(Color(0xFFFF0055).copy(alpha = 0.5f), Color(0xFFD500F9).copy(alpha = 0.5f), Color(0xFFFFD700).copy(alpha = 0.4f)),
                Color(0xFFFF2A7A)
            )
            "DIAMOND" -> Triple(
                listOf(Color(0xFF03223A), Color(0xFF0A456C), Color(0xFF021727), Color(0xFF063352)),
                listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color(0xFF2979FF).copy(alpha = 0.5f), Color(0xFF00E676).copy(alpha = 0.4f)),
                Color(0xFF00E5FF)
            )
            "GOLD" -> Triple(
                listOf(Color(0xFF382603), Color(0xFF5E4208), Color(0xFF221600), Color(0xFF473204)),
                listOf(Color(0xFFFFD700).copy(alpha = 0.5f), Color(0xFFFFA000).copy(alpha = 0.5f), Color(0xFFFFE082).copy(alpha = 0.4f)),
                Color(0xFFFFD700)
            )
            "SILVER" -> Triple(
                listOf(Color(0xFF1F2937), Color(0xFF374151), Color(0xFF111827), Color(0xFF283548)),
                listOf(Color(0xFFECEFF1).copy(alpha = 0.5f), Color(0xFFB0BEC5).copy(alpha = 0.5f), Color(0xFF78909C).copy(alpha = 0.4f)),
                Color(0xFFECEFF1)
            )
            else -> Triple(
                listOf(Color(0xFF3A1808), Color(0xFF5A270F), Color(0xFF240D04), Color(0xFF461D0A)),
                listOf(Color(0xFFFF8A65).copy(alpha = 0.5f), Color(0xFFD84315).copy(alpha = 0.5f), Color(0xFFFFCCBC).copy(alpha = 0.4f)),
                Color(0xFFFF8A65)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0C0E17))
            .border(0.8.dp, if (isUsed) Color(0xFF242938) else Color(0xFF1E2338), RoundedCornerShape(22.dp))
            .padding(12.dp)
    ) {
        // Physical Unlocked Digital Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = cardBgGradient,
                        start = Offset(0f, 0f),
                        end = Offset(900f, 900f)
                    )
                )
                .border(
                    0.8.dp,
                    if (isUsed) Brush.linearGradient(listOf(Color(0xFF3B4055), Color(0xFF262A38)))
                    else Brush.linearGradient(borderGradient),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            // Live Shimmer & Subtle Geometry
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                if (isGooglePlay) {
                    val facetPath = Path().apply {
                        moveTo(w * 0.7f, 0f)
                        lineTo(w, h * 0.45f)
                        lineTo(w * 0.4f, h)
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
                    drawCircle(Color(0xFF00E5FF).copy(alpha = 0.7f), radius = 2.dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                    drawCircle(Color.White.copy(alpha = 0.9f), radius = 1.2.dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                } else {
                    val stroke = 0.8.dp.toPx()
                    var x = 0f
                    while (x < w * 2) {
                        drawLine(
                            color = accentColor.copy(alpha = 0.04f),
                            start = Offset(x, 0f),
                            end = Offset(x - h, h),
                            strokeWidth = stroke
                        )
                        x += 20.dp.toPx()
                    }
                }

                if (!isUsed) {
                    val sheenWidth = 140f
                    val sheenBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.12f),
                            accentColor.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        start = Offset(shimmerTranslate - sheenWidth, 0f),
                        end = Offset(shimmerTranslate + sheenWidth, h)
                    )
                    drawRect(brush = sheenBrush)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Row: Header & Value/Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF020D14).copy(alpha = 0.7f))
                            .border(0.8.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (isDataRecharge) {
                            val isJio = card.title.contains("JIO", ignoreCase = true)
                            if (isJio) {
                                JioBrandLogo(size = 22)
                            } else {
                                AirtelBrandLogo(size = 20)
                            }
                            Spacer(modifier = Modifier.width(7.dp))
                            Column {
                                Text(
                                    if (isJio) "JIO 4G/5G" else "AIRTEL 5G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    "DATA ADD-ON BOOSTER",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 7.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        } else if (isGooglePlay) {
                            GooglePlayLogoIcon(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(7.dp))
                            Column {
                                Text(
                                    "Google Play",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    "OFFICIAL GIFT VOUCHER",
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 7.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(7.dp))
                            Column {
                                Text(
                                    "SCRIMX VIP PASS",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.6.sp
                                )
                                Text(
                                    card.title.uppercase(),
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 7.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    // Status / Value Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isUsed) Color(0xFF262A38) else accentColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val badgeText = if (isDataRecharge) {
                            "₹${card.denominationRupees}"
                        } else if (isUsed) {
                            "REDEEMED"
                        } else if (isGooglePlay) {
                            "₹${card.denominationRupees}"
                        } else {
                            if (card.discountPercent > 0) "${card.discountPercent}% OFF" else "₹${card.discountFlatRupees} OFF"
                        }
                        Text(
                            badgeText,
                            color = if (isUsed) Color(0xFF8E93A6) else Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }

                // Middle Row: UNLOCKED Full Redeem Code & 1-Tap Copy Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF030812).copy(alpha = 0.9f))
                        .border(1.dp, if (isUsed) Color(0xFF262A38) else accentColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isDataRecharge) "RECHARGE MOBILE NUMBER" else if (isGooglePlay) "GOOGLE PLAY REDEEM CODE" else "VIP DISCOUNT PASS CODE",
                                color = Color(0xFF6F8299),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                card.code,
                                color = if (isUsed) Color(0xFF8E93A6) else Color(0xFFFFD700),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.5.sp,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(if (isDataRecharge) "Mobile Number" else "Redeem Code", card.code)
                                clipboard.setPrimaryClip(clip)
                                isCodeCopied = true
                                Toast.makeText(context, if (isDataRecharge) "Mobile Number copied: ${card.code}" else "Code copied to clipboard: ${card.code}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCodeCopied) Color(0xFF00E676) else if (isUsed) Color(0xFF25293A) else accentColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                if (isCodeCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = if (isCodeCopied) Color.Black else if (isUsed) Color.White else Color.Black,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isCodeCopied) "COPIED" else "COPY",
                                color = if (isCodeCopied) Color.Black else if (isUsed) Color.White else Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                // Bottom Info Row: Cardholder & Status / Uses
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "UNLOCKED BY",
                            color = Color(0xFF6F8299),
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.5.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            userName.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isUsed) Color(0xFF262A38) else Color(0xFF00E676).copy(alpha = 0.15f))
                            .border(0.6.dp, if (isUsed) Color(0xFF3B4055) else Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        val usageInfo = if (isDataRecharge) {
                            when (card.status) {
                                "SUCCESS", "RECHARGED" -> "✅ RECHARGE ACTIVE"
                                "REJECTED" -> "❌ REJECTED & REFUNDED"
                                else -> "⏳ RECHARGE QUEUED"
                            }
                        } else if (isUsed) {
                            "USED & ARCHIVED"
                        } else if (isGooglePlay) {
                            "AVAILABLE FOR USE"
                        } else {
                            "VALID FOR ${card.remainingUses}/${card.totalUses} MATCHES"
                        }
                        Text(
                            usageInfo,
                            color = if (isUsed) Color(0xFF8E93A6) else Color(0xFF00E676),
                            fontWeight = FontWeight.Black,
                            fontSize = 8.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons Row Below Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isDataRecharge) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF19060A))
                            .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (card.status == "SUCCESS") "⚡ Data pack is active on your SIM" else if (card.status == "REJECTED") "❌ Recharge cancelled & coins refunded" else "⚡ Recharge request submitted to Admin desk",
                            color = if (card.status == "SUCCESS") Color(0xFF00E676) else if (card.status == "REJECTED") Color(0xFFFF5252) else Color(0xFFFF8A80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // If proof screenshot is attached by admin
                    if (proofBase64String.isNotEmpty()) {
                        Button(
                            onClick = { showProofDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VIEW RECHARGE PROOF / रसीद देखें", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            } else if (isGooglePlay) {
                Button(
                    onClick = {
                        val redeemUrl = "https://play.google.com/redeem?code=${card.code}"
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redeemUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Code copied! Open Play Store -> Payments -> Redeem code", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 9.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REDEEM IN PLAY STORE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }

            if (!isUsed) {
                OutlinedButton(
                    onClick = onMarkUsed,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF32384D)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8E93A6)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = if (!isGooglePlay) Modifier.fillMaxWidth() else Modifier,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, tint = Color(0xFF8E93A6), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark as Used", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Newly Purchased Scratch / Celebration Modal
// ----------------------------------------------------
@Composable
fun CelebrationCardDialog(
    card: UserPurchasedCard,
    userName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val isGooglePlay = card.category.equals("GOOGLE_PLAY", ignoreCase = true)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF10121A))
                .border(2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFF00E676), Color(0xFF00B0FF))), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("UNLOCKED SUCCESSFULLY", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 17.sp)

                Text(
                    if (isGooglePlay) "Your ₹${card.denominationRupees} Google Play Card is Ready!" else "Your ${card.title} VIP Pass is Active!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                // Code Reveal Box with glowing borders
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF05060A))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (isGooglePlay) "GOOGLE PLAY 16-CHAR REDEEM CODE" else "VIP TOURNAMENT PASS CODE",
                            color = Color(0xFF8E93A6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            card.code,
                            color = Color(0xFF00E676),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                // Copy Code Action
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Redeem Code", card.code)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                        Toast.makeText(context, "Code copied: ${card.code}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCopied) Color(0xFF00E676) else Color(0xFFFFD700)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(
                        if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isCopied) "CODE COPIED TO CLIPBOARD" else "COPY CODE",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                if (isGooglePlay) {
                    OutlinedButton(
                        onClick = {
                            val redeemUrl = "https://play.google.com/redeem?code=${card.code}"
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redeemUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Opening Play Store...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REDEEM DIRECTLY IN PLAY STORE", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("VIEW IN MY VAULT", color = Color(0xFF8E93A6), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ----------------------------------------------------
// Visual Components (EMV Chip, Google Play Logo, etc.)
// ----------------------------------------------------
@Composable
fun EmvCardChip(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFF8F00))
                )
            )
            .border(0.5.dp, Color(0xFFFFE082), RoundedCornerShape(4.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 0.8.dp.toPx()
            val c = Color(0xFF6D4C41).copy(alpha = 0.7f)
            // Chip internal circuit lines
            drawLine(c, Offset(size.width * 0.33f, 0f), Offset(size.width * 0.33f, size.height), stroke)
            drawLine(c, Offset(size.width * 0.66f, 0f), Offset(size.width * 0.66f, size.height), stroke)
            drawLine(c, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), stroke)
        }
    }
}

@Composable
fun ContactlessWavesIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.2.dp.toPx()
        val color = Color(0xFFC0C5D6).copy(alpha = 0.8f)
        drawArc(
            color = color,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(stroke),
            topLeft = Offset(-size.width * 0.2f, size.height * 0.1f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.8f)
        )
        drawArc(
            color = color,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(stroke),
            topLeft = Offset(size.width * 0.2f, 0f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.8f, size.height)
        )
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
// Premium Red Silver Foil Paper Mobile Data Card (Jio & Airtel)
// Designed like authentic Metallic Red Silver Foil Scratch Card
// ----------------------------------------------------
@Composable
fun DataRechargeCard(
    plan: DataRechargePlan,
    userCoins: Int,
    onRedeemClick: () -> Unit
) {
    val canAfford = userCoins >= plan.coinPrice
    val isJio = plan.operator.equals("JIO", ignoreCase = true)

    // Animated Holographic Silver/White Light Sweep across the red foil paper
    val infiniteTransition = rememberInfiniteTransition(label = "redSilverFoil")
    val foilShimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -350f,
        targetValue = 1100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "foilShimmerTranslate"
    )

    // Outer Container matching Google Play voucher cards with compact height
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0D0507))
            .border(0.8.dp, Color(0xFF331015), RoundedCornerShape(20.dp))
            .padding(10.dp)
    ) {
        // Red Silver Foil Metallic Paper Physical Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8A0413), // Deep metallic ruby red
                            Color(0xFFD31027), // Bright lustrous silver-red foil
                            Color(0xFF6B020D), // Dark crimson shadow
                            Color(0xFFE52D27), // Gleaming scarlet
                            Color(0xFF7A0410)  // Base metallic red
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 900f)
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF).copy(alpha = 0.85f), // Silver paper edge shine
                            Color(0xFFFFB4A2).copy(alpha = 0.6f),
                            Color(0xFFE2E8F0).copy(alpha = 0.8f),  // Pure silver foil rim
                            Color(0xFFFFD700).copy(alpha = 0.5f),  // Warm specular gleam
                            Color(0xFFFFFFFF).copy(alpha = 0.9f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(800f, 600f)
                    ),
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            // Metallic Red Silver Foil Paper Texture & Sheen Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Silver Foil Micro-Refraction Lines (Brushed Foil Paper Effect)
                var y = 0f
                while (y < h) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.035f),
                        start = Offset(0f, y),
                        end = Offset(w, y + 14.dp.toPx()),
                        strokeWidth = 0.7.dp.toPx()
                    )
                    y += 6.dp.toPx()
                }

                // 2. Silver Paper Crushed Specular Highlights
                val silverFoilGleam = Path().apply {
                    moveTo(w * 0.15f, 0f)
                    lineTo(w * 0.45f, 0f)
                    lineTo(w * 0.25f, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = silverFoilGleam,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.09f),
                            Color(0xFFE2E8F0).copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                )

                // 3. Dynamic Moving Silver Holographic Sheen (Silver Paper Reflection)
                val sheenWidth = 120f
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFFFFFFF).copy(alpha = 0.18f),
                        Color(0xFFE0F7FA).copy(alpha = 0.28f),
                        Color(0xFFFFFFFF).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    start = Offset(foilShimmerTranslate - sheenWidth, 0f),
                    end = Offset(foilShimmerTranslate + sheenWidth, h)
                )
                drawRect(brush = sheenBrush)

                // 4. Sparkling Silver Stars (✦ Specular Glitter on Red Foil)
                drawCircle(Color.White.copy(alpha = 0.95f), radius = 1.8.dp.toPx(), center = Offset(w * 0.88f, h * 0.18f))
                drawCircle(Color(0xFFFFFFFF).copy(alpha = 0.6f), radius = 3.5.dp.toPx(), center = Offset(w * 0.88f, h * 0.18f))
                drawCircle(Color.White.copy(alpha = 0.85f), radius = 1.2.dp.toPx(), center = Offset(w * 0.72f, h * 0.42f))
                drawCircle(Color(0xFFFFE082).copy(alpha = 0.7f), radius = 1.5.dp.toPx(), center = Offset(w * 0.94f, h * 0.72f))
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Brand Logo + Silver Foil Price Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Brand Badge with Glossy Dark Backdrop
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF180104).copy(alpha = 0.75f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (isJio) {
                            JioBrandLogo(size = 22)
                        } else {
                            AirtelBrandLogo(size = 20)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (isJio) "JIO 4G/5G" else "AIRTEL 5G",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "OFFICIAL DATA BOOSTER",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 7.sp,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    // Pure Silver Foil Denomination Pill (Shining Silver Look)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFE2E8F0),
                                        Color(0xFFCBD5E1)
                                    )
                                )
                            )
                            .border(0.8.dp, Color.White, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "₹${plan.priceRupees}",
                            color = Color(0xFF1E0206),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.5.sp
                        )
                    }
                }

                // Center Strip: Metallic Silver Scratch Bar with Data Amount
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF1F0307).copy(alpha = 0.9f),
                                    Color(0xFF2D050B).copy(alpha = 0.95f),
                                    Color(0xFF1F0307).copy(alpha = 0.9f)
                                )
                            )
                        )
                        .border(
                            0.8.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.6f),
                                    Color(0xFFFFD700).copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.6f)
                                )
                            ),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = plan.dataAmount,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = plan.validity,
                                    color = Color(0xFFFFCDD2),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Silver Foil Tag Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(0.6.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = plan.tagText,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }

                // Bottom Row: Speed info & Direct Verification Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            plan.highlightSpeed.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00E676).copy(alpha = 0.2f))
                            .border(0.6.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            "⚡ INSTANT TOP-UP",
                            color = Color(0xFFB9F6CA),
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Info & Action Button Row (Matches Google Play card standard)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${plan.operator} ${plan.dataAmount} Pack",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Text(
                    text = "Official MRP ₹${plan.priceRupees} • ${plan.validity}",
                    color = Color(0xFF8E93A6),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onRedeemClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) Color(0xFFFFD700) else Color(0xFF1E2232)
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (canAfford) Color.Black else Color(0xFF8E93A6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${plan.coinPrice} COINS",
                        color = if (canAfford) Color.Black else Color(0xFF8E93A6),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp
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
