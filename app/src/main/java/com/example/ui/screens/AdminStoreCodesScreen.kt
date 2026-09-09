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


data class RestockRequest(
    val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val timestamp: Long = 0L
)

data class AdminStoreStockItem(

    val itemId: String = "",
    val title: String = "",
    val denominationRupees: Int = 0,
    val coinPrice: Int = 0,
    val inStock: Boolean = true,
    val dailyLimitPerUser: Int = 2,
    val customCodesPool: List<String> = emptyList()
)


@Composable
fun RestockRequestCard(request: RestockRequest, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E070B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.itemTitle, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(request.userEmail, color = Color(0xFFB0BEC5), fontSize = 11.sp)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color(0xFFE11D48))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStoreCodesScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }
    val auth = remember { FirebaseHelper.getAuth() }
    val currentUserEmail = auth?.currentUser?.email?.lowercase() ?: ""
    val currentUid = auth?.currentUser?.uid
    val isOwner = remember(currentUserEmail, currentUid) { AppSecurityGuard.isSuperOwner(currentUserEmail, currentUid) }
    var isAuthorized by remember { mutableStateOf(isOwner) }

    var selectedItemForCodeEntry by remember { mutableStateOf<StoreItem?>(null) }
    var codeInputText by remember { mutableStateOf("") }
    var dailyLimitInput by remember { mutableStateOf("2") }
    var coinPriceInput by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val storeViewModel = remember { StoreViewModel() }
    var stockDataMap by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }
    var restockRequests by remember { mutableStateOf<List<RestockRequest>>(emptyList()) }

    LaunchedEffect(currentUid) {
        if (isOwner) {
            isAuthorized = true
        } else if (currentUid != null && db != null) {
            db.collection("users").document(currentUid).get().addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val role = doc.getString("role")?.lowercase() ?: "player"
                    val isMod = doc.getBoolean("isModerator") ?: false
                    val emailInDoc = doc.getString("email")?.lowercase() ?: ""
                    if (role in listOf("owner", "admin", "moderator") || isMod || AppSecurityGuard.isSuperOwner(emailInDoc, currentUid)) {
                        isAuthorized = true
                    } else {
                        Toast.makeText(context, "Access Denied: Admin Exclusive Tool", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                } else {
                    Toast.makeText(context, "Access Denied: Admin Exclusive Tool", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Authentication verification failed", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        } else {
            Toast.makeText(context, "Access Denied: Admin Exclusive Tool", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }

        db?.collection("store_settings")?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("AdminStoreCodes", "Error listening to store settings: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val map = mutableMapOf<String, Map<String, Any>>()
                for (doc in snapshot.documents) {
                    map[doc.id] = doc.data ?: emptyMap()
                }
                stockDataMap = map
            }
        }
        db?.collection("store_restock_requests")?.whereEqualTo("status", "PENDING")?.addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                val reqs = snapshot.documents.mapNotNull { it.toObject(RestockRequest::class.java)?.copy(id = it.id) }
                restockRequests = reqs
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Store Codes & Stock Manager",
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        Text(
                            "Add Real Google Play Codes & Set Limits",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0D14))
            )
        },
        containerColor = Color(0xFF07090E)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0A2B3D), Color(0xFF061520), Color(0xFF0D1E2C))
                            )
                        )
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "AUTOMATIC CODE DISPATCH ENGINE",
                                color = AppColors.TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Add batch Google Play codes. When players redeem, the system instantly delivers available codes from your pool!",
                                color = Color(0xFFB0BEC5),
                                fontSize = 11.sp,
                                lineHeight = 14.5.sp
                            )
                        }
                    }
                }
            }

            if (restockRequests.isNotEmpty()) {
                item {
                    Text(
                        "RESTOCK REQUESTS",
                        color = Color(0xFFE11D48),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp,
                        letterSpacing = 1.2.sp
                    )
                }
                items(restockRequests) { req ->
                    RestockRequestCard(
                        request = req,
                        onDismiss = {
                            FirebaseHelper.getFirestore()?.collection("store_restock_requests")?.document(req.id)?.update("status", "DISMISSED")
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            item {
                Text(
                    "GOOGLE PLAY GIFT CARDS",
                    color = Color(0xFF75798E),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.5.sp,
                    letterSpacing = 1.2.sp
                )
            }

            items(storeViewModel.googlePlayCards) { item ->
                val itemSettings = stockDataMap[item.id]
                val customCodes = (itemSettings?.get("codes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val currentDailyLimit = (itemSettings?.get("dailyLimit") as? Long)?.toInt() ?: 2
                val currentPrice = (itemSettings?.get("coinPrice") as? Long)?.toInt() ?: item.coinPrice
                val isItemEnabled = itemSettings?.get("enabled") as? Boolean ?: true

                AdminStoreItemRow(
                    item = item,
                    customCodesCount = customCodes.size,
                    dailyLimit = currentDailyLimit,
                    coinPrice = currentPrice,
                    isEnabled = isItemEnabled,
                    onManageClick = {
                        selectedItemForCodeEntry = item
                        codeInputText = customCodes.joinToString("\n")
                        dailyLimitInput = currentDailyLimit.toString()
                        coinPriceInput = currentPrice.toString()
                    },
                    onToggleEnabled = {
                        db?.collection("store_settings")?.document(item.id)?.set(
                            mapOf("enabled" to !isItemEnabled),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "TOURNAMENT VIP DISCOUNT PASSES",
                    color = Color(0xFF75798E),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.5.sp,
                    letterSpacing = 1.2.sp
                )
            }

            items(storeViewModel.appDiscountCards) { item ->
                val itemSettings = stockDataMap[item.id]
                val customCodes = (itemSettings?.get("codes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val currentDailyLimit = (itemSettings?.get("dailyLimit") as? Long)?.toInt() ?: 3
                val currentPrice = (itemSettings?.get("coinPrice") as? Long)?.toInt() ?: item.coinPrice
                val isItemEnabled = itemSettings?.get("enabled") as? Boolean ?: true

                AdminStoreItemRow(
                    item = item,
                    customCodesCount = customCodes.size,
                    dailyLimit = currentDailyLimit,
                    coinPrice = currentPrice,
                    isEnabled = isItemEnabled,
                    onManageClick = {
                        selectedItemForCodeEntry = item
                        codeInputText = customCodes.joinToString("\n")
                        dailyLimitInput = currentDailyLimit.toString()
                        coinPriceInput = currentPrice.toString()
                    },
                    onToggleEnabled = {
                        db?.collection("store_settings")?.document(item.id)?.set(
                            mapOf("enabled" to !isItemEnabled),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    // Modal Sheet / Dialog to enter codes & limits
    if (selectedItemForCodeEntry != null) {
        val targetItem = selectedItemForCodeEntry!!

        AlertDialog(
            onDismissRequest = { if (!isSaving) selectedItemForCodeEntry = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configure ${targetItem.title}", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter Custom Redeem Codes (1 code per line):",
                        color = Color(0xFFB0B5C9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = codeInputText,
                        onValueChange = { codeInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = {
                            Text(
                                "GPLY-87FA-29HK-0012\nGPLY-3341-99AA-7761",
                                color = Color(0xFF555B6E),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF262C3D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 11.5.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = coinPriceInput,
                            onValueChange = { coinPriceInput = it },
                            label = { Text("Coin Price", color = Color(0xFF8E93A6), fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF262C3D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = dailyLimitInput,
                            onValueChange = { dailyLimitInput = it },
                            label = { Text("Daily Limit / User", color = Color(0xFF8E93A6), fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF262C3D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSaving = true
                        val parsedCodes = codeInputText.lines().map { it.trim() }.filter { it.isNotEmpty() }
                        val parsedPrice = coinPriceInput.toIntOrNull() ?: targetItem.coinPrice
                        val parsedLimit = dailyLimitInput.toIntOrNull() ?: 2

                        val data = mapOf(
                            "codes" to parsedCodes,
                            "coinPrice" to parsedPrice,
                            "dailyLimit" to parsedLimit,
                            "enabled" to true,
                            "updatedAt" to System.currentTimeMillis()
                        )

                        db?.collection("store_settings")?.document(targetItem.id)
                            ?.set(data, com.google.firebase.firestore.SetOptions.merge())
                            ?.addOnSuccessListener {
                                isSaving = false
                                Toast.makeText(context, "✅ Saved settings for ${targetItem.title}", Toast.LENGTH_SHORT).show()
                                selectedItemForCodeEntry = null
                            }
                            ?.addOnFailureListener { e ->
                                isSaving = false
                                Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("SAVE CODES & LIMITS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemForCodeEntry = null }) {
                    Text("CANCEL", color = Color(0xFF8E93A6), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF121520),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun AdminStoreItemRow(
    item: StoreItem,
    customCodesCount: Int,
    dailyLimit: Int,
    coinPrice: Int,
    isEnabled: Boolean,
    onManageClick: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF10131D))
            .border(1.dp, if (isEnabled) Color(0xFF1E2335) else Color(0xFF381B22), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        color = if (isEnabled) Color.White else Color(0xFF75798E),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (!isEnabled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF0055).copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("DISABLED", color = Color(0xFFFF0055), fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    "Price: $coinPrice Coins • Limit: $dailyLimit/day • Custom Codes in Pool: $customCodesCount",
                    color = Color(0xFF8E93A6),
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onManageClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1D2235))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onToggleEnabled,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isEnabled) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFFF0055).copy(alpha = 0.15f))
                ) {
                    Icon(
                        if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = "Toggle",
                        tint = if (isEnabled) Color(0xFF00E676) else Color(0xFFFF0055),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
