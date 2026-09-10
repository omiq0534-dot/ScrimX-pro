package com.example.ui.screens
import com.example.ui.theme.AppColors

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageWalletsScreen(navController: NavController) {
    val db = remember { FirebaseHelper.getFirestore() }
    val auth = remember { FirebaseHelper.getAuth() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentEmail = auth?.currentUser?.email ?: ""
    val isOwner = remember(currentEmail) { AppSecurityGuard.isSuperOwner(currentEmail) }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Deposit Requests, 1 = Withdraw Requests, 2 = Manual Edit, 3 = UPI Settings

    var pendingDeposits by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var pendingWithdraws by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var pendingRecharges by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }

    // UPI Settings State
    var upiIdInput by remember { mutableStateOf("6375615586@fam") }
    var upiNameInput by remember { mutableStateOf("Tournament Esports Official") }
    var minDepositInput by remember { mutableStateOf("10") }
    var minWithdrawInput by remember { mutableStateOf("50") }
    var coinRateInput by remember { mutableStateOf("10") }

    // Manual Edit State
    var searchEmail by remember { mutableStateOf("") }
    var foundUser by remember { mutableStateOf<UserProfile?>(null) }
    var coinAmountInput by remember { mutableStateOf("") }
    var cashAmountInput by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf("") }
    var allUsersList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }

    // Listen to real-time transactions and users
    LaunchedEffect(Unit) {
        if (db != null) {
            val uid = auth?.currentUser?.uid
            if (uid != null) {
                AppSecurityGuard.verifyServerAdminStatus(db, uid, currentEmail) { owner, mod ->
                    if (!owner && !mod) {
                        AppSecurityGuard.logSecurityIncident(
                            db = db,
                            auth = auth,
                            incidentType = "WALLET_ADMIN_BREACH_ATTEMPT",
                            details = "Unauthorized wallet access attempt by email: $currentEmail"
                        )
                        Toast.makeText(context, "Access Denied: Admin privileges required!", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                }
            } else if (!isOwner) {
                navController.popBackStack()
            }
            db.collection("transactions").addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val allTx = snap.documents.mapNotNull { it.toObject(TransactionRecord::class.java)?.copy(id = it.id) }
                    pendingDeposits = allTx.filter { it.type == "DEPOSIT" && it.status == "PENDING" }
                        .sortedByDescending { it.timestamp }
                    pendingWithdraws = allTx.filter { it.type == "WITHDRAW" && it.status == "PENDING" }
                        .sortedByDescending { it.timestamp }
                    pendingRecharges = allTx.filter { it.type == "DATA_RECHARGE" && it.status == "PENDING" }
                        .sortedByDescending { it.timestamp }
                }
            }

            db.collection("users").addSnapshotListener { snap, _ ->
                if (snap != null) {
                    val list = snap.documents.mapNotNull { it.toObject(UserProfile::class.java)?.copy(uid = it.id) }
                    allUsersList = list
                    if (foundUser != null) {
                        val u = list.find { it.uid == foundUser!!.uid }
                        if (u != null) foundUser = u
                    }
                }
            }

            // Fetch settings
            db.collection("settings").document("payment").get().addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    upiIdInput = doc.getString("upiId") ?: "6375615586@fam"
                    upiNameInput = doc.getString("upiName") ?: "Tournament Esports Official"
                    minDepositInput = (doc.getLong("minDeposit") ?: 10L).toString()
                    minWithdrawInput = (doc.getLong("minWithdraw") ?: 50L).toString()
                    coinRateInput = (doc.getLong("coinConversionRate") ?: 10L).toString()
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "WALLET & CASHOUT COMMAND",
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextPrimary,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF9FAFB))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "Deposits (${pendingDeposits.size})",
                    "Withdrawals (${pendingWithdraws.size})",
                    "Recharges (${pendingRecharges.size})",
                    "Edit Wallet",
                    "UPI Gateway"
                ).forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFFFFD700) else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title,
                            color = if (isSelected) Color.Black else Color(0xFF8E92A4),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            when (selectedTab) {
                // 1. PENDING DEPOSIT REQUESTS
                0 -> {
                    if (pendingDeposits.isEmpty()) {
                        EmptyAdminPlaceholder("No pending deposits!", "Users' UPI payments will show here for instant approval.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(pendingDeposits) { tx ->
                                DepositRequestCard(
                                    tx = tx,
                                    onApprove = {
                                        scope.launch {
                                            try {
                                                // 1. Update user balance
                                                db?.collection("users")?.document(tx.userId)
                                                    ?.update("realMoney", FieldValue.increment(tx.amount.toLong()))?.await()
                                                // 2. Mark TX success
                                                db?.collection("transactions")?.document(tx.id)
                                                    ?.update("status", "SUCCESS")?.await()
                                                Toast.makeText(context, "✅ Approved ₹${tx.amount} to user!", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Approval error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onReject = {
                                        scope.launch {
                                            db?.collection("transactions")?.document(tx.id)?.update("status", "REJECTED")?.await()
                                            Toast.makeText(context, "❌ Rejected deposit request", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. PENDING WITHDRAW REQUESTS
                1 -> {
                    if (pendingWithdraws.isEmpty()) {
                        EmptyAdminPlaceholder("No pending withdrawals!", "User cashout requests will appear here with their UPI IDs.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(pendingWithdraws) { tx ->
                                WithdrawRequestCard(
                                    tx = tx,
                                    onComplete = {
                                        scope.launch {
                                            db?.collection("transactions")?.document(tx.id)?.update("status", "SUCCESS")?.await()
                                            Toast.makeText(context, "✅ Marked withdrawal as Paid!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onReject = {
                                        scope.launch {
                                            // Refund user balance
                                            db?.collection("users")?.document(tx.userId)
                                                ?.update("realMoney", FieldValue.increment(tx.amount.toLong()))?.await()
                                            db?.collection("transactions")?.document(tx.id)
                                                ?.update("status", "REJECTED")?.await()
                                            Toast.makeText(context, "❌ Rejected & refunded ₹${tx.amount} to user", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. PENDING DATA RECHARGE REQUESTS
                2 -> {
                    if (pendingRecharges.isEmpty()) {
                        EmptyAdminPlaceholder("No pending recharges!", "Users' mobile data booster requests (Jio / Airtel) will show here for instant top-up.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(pendingRecharges) { tx ->
                                RechargeRequestCard(
                                    tx = tx,
                                    onCompleteWithProof = { proofBase64, opTxnId ->
                                        scope.launch {
                                            try {
                                                val txUpdates = mutableMapOf<String, Any>(
                                                    "status" to "SUCCESS",
                                                    "completedTimestamp" to System.currentTimeMillis()
                                                )
                                                if (proofBase64.isNotBlank()) {
                                                    txUpdates["screenshotBase64"] = proofBase64
                                                    txUpdates["proofScreenshotBase64"] = proofBase64
                                                }
                                                if (opTxnId.isNotBlank()) {
                                                    txUpdates["operatorTxnId"] = opTxnId
                                                    txUpdates["rechargeTxnId"] = opTxnId
                                                }
                                                db?.collection("transactions")?.document(tx.id)
                                                    ?.set(txUpdates, com.google.firebase.firestore.SetOptions.merge())?.await()

                                                val invUpdates = mutableMapOf<String, Any>(
                                                    "status" to "SUCCESS",
                                                    "completedTimestamp" to System.currentTimeMillis()
                                                )
                                                if (proofBase64.isNotBlank()) {
                                                    invUpdates["proofScreenshotBase64"] = proofBase64
                                                    invUpdates["screenshotBase64"] = proofBase64
                                                }
                                                if (opTxnId.isNotBlank()) {
                                                    invUpdates["rechargeTxnId"] = opTxnId
                                                    invUpdates["operatorTxnId"] = opTxnId
                                                }
                                                if (tx.userId.isNotBlank()) {
                                                    db?.collection("users")?.document(tx.userId)?.collection("inventory")?.document(tx.id)
                                                        ?.set(invUpdates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                    db?.collection("users")?.document(tx.userId)?.collection("purchased_cards")?.document(tx.id)
                                                        ?.set(invUpdates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                }

                                                Toast.makeText(context, "✅ Recharge Marked SUCCESS with Proof for +91 ${tx.mobileNumber}!", Toast.LENGTH_LONG).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Update error: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onReject = {
                                        scope.launch {
                                            try {
                                                // Refund coins back to user
                                                if (tx.coinAmount > 0) {
                                                    val refundMap = mapOf(
                                                        "appMoney" to FieldValue.increment(tx.coinAmount.toLong()),
                                                        "walletBalance" to FieldValue.increment(tx.coinAmount.toLong())
                                                    )
                                                    db?.collection("users")?.document(tx.userId)
                                                        ?.set(refundMap, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                }
                                                val rejMap = mapOf("status" to "REJECTED")
                                                db?.collection("transactions")?.document(tx.id)
                                                    ?.set(rejMap, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                if (tx.userId.isNotBlank()) {
                                                    db?.collection("users")?.document(tx.userId)?.collection("inventory")?.document(tx.id)
                                                        ?.set(rejMap, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                }
                                                Toast.makeText(context, "❌ Recharge rejected & ${tx.coinAmount} coins refunded!", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Refund error: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 4. MANUAL USER BALANCE & COIN EDIT
                3 -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Search / Selector Card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "Search Player / Manage Wallet",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = AppColors.TextPrimary
                                    )

                                    ClassyDarkInput(
                                        value = searchEmail,
                                        onValueChange = { 
                                            searchEmail = it
                                            if (it.isBlank()) {
                                                searchMessage = ""
                                            }
                                        },
                                        label = "Search by Email or Name",
                                        placeholder = "e.g. gamer@gmail.com or Om"
                                    )

                                    Button(
                                        onClick = {
                                            isSearching = true
                                            searchMessage = ""
                                            val query = searchEmail.trim().lowercase()
                                            if (query.isEmpty()) {
                                                searchMessage = "Please enter email or name to search"
                                                isSearching = false
                                                return@Button
                                            }
                                            
                                            val matched = allUsersList.find { 
                                                it.email.lowercase().contains(query) || it.name.lowercase().contains(query)
                                            }
                                            if (matched != null) {
                                                foundUser = matched
                                                searchMessage = "Player profile loaded!"
                                            } else {
                                                scope.launch {
                                                    try {
                                                        val snap = db?.collection("users")?.whereEqualTo("email", searchEmail.trim())?.get()?.await()
                                                        if (snap != null && !snap.isEmpty) {
                                                            foundUser = snap.documents[0].toObject(UserProfile::class.java)?.copy(uid = snap.documents[0].id)
                                                            searchMessage = "Player profile loaded!"
                                                        } else {
                                                            searchMessage = "No player found with '$searchEmail'!"
                                                            foundUser = null
                                                        }
                                                    } catch (e: Exception) {
                                                        searchMessage = "Error searching player: ${e.message}"
                                                    } finally {
                                                        isSearching = false
                                                    }
                                                }
                                            }
                                            isSearching = false
                                        },
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("FIND PLAYER", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    }

                                    if (searchMessage.isNotEmpty()) {
                                        Text(
                                            searchMessage,
                                            color = if (foundUser != null) Color(0xFF00E676) else Color(0xFFFF5252),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Quick Pick from registered users
                                    if (allUsersList.isNotEmpty() && foundUser == null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Quick Select User (${allUsersList.size} Registered):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8E92A4)
                                        )
                                        
                                        val displayList = if (searchEmail.isBlank()) allUsersList.take(6) else allUsersList.filter { 
                                            it.email.contains(searchEmail, ignoreCase = true) || it.name.contains(searchEmail, ignoreCase = true)
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            displayList.forEach { u ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFF9FAFB))
                                                        .border(1.dp, Color(0xFF1E2230), RoundedCornerShape(10.dp))
                                                        .clickable {
                                                            foundUser = u
                                                            searchEmail = u.email
                                                            searchMessage = "Loaded ${u.name}"
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(u.name.ifBlank { "Player" }, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, fontSize = 12.sp)
                                                        Text(u.email, color = Color(0xFF6B7280), fontSize = 10.sp)
                                                    }
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Text("🪙 ${u.appMoney}", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                                        Text("₹${u.realMoney}", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                                        Text("Edit >", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (foundUser != null) {
                            val user = foundUser!!

                            // User Profile Banner
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(user.name.ifBlank { "Player" }, fontWeight = FontWeight.Black, color = AppColors.TextPrimary, fontSize = 16.sp)
                                                Text(user.email, color = Color(0xFF8E92A4), fontSize = 12.sp)
                                            }
                                            IconButton(
                                                onClick = { 
                                                    foundUser = null 
                                                    searchEmail = ""
                                                    searchMessage = ""
                                                }
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E92A4))
                                            }
                                        }

                                        // Stats Badges
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Coins Box
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF1E1A0F))
                                                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Column {
                                                    Text("🪙 COIN BALANCE", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("${user.appMoney}", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                                }
                                            }

                                            // Cash Box
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF0F1E16))
                                                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Column {
                                                    Text("💵 REAL CASH", color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("₹${user.realMoney}", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 🪙 SECTION 1: MANAGE COINS (appMoney)
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🪙 Manage Coins (App Currency)", fontWeight = FontWeight.Black, color = Color(0xFFFFD700), fontSize = 13.sp)
                                            Text("Current: ${user.appMoney}", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        // Preset Quick Chips
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(50, 100, 500).forEach { amt ->
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            try {
                                                                val newCoins = user.appMoney + amt
                                                                val updates = mapOf("appMoney" to newCoins, "walletBalance" to newCoins)
                                                                db?.collection("users")?.document(user.uid)
                                                                    ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                                foundUser = user.copy(appMoney = newCoins)
                                                                Toast.makeText(context, "Added +$amt Coins! New Total: $newCoins", Toast.LENGTH_SHORT).show()
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Coin update failed: ${e.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262112), contentColor = Color(0xFFFFD700)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f).height(36.dp)
                                                ) {
                                                    Text("+$amt", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                }
                                            }

                                            listOf(50, 100, 500).forEach { amt ->
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            try {
                                                                val newCoins = (user.appMoney - amt).coerceAtLeast(0)
                                                                val updates = mapOf("appMoney" to newCoins, "walletBalance" to newCoins)
                                                                db?.collection("users")?.document(user.uid)
                                                                    ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                                foundUser = user.copy(appMoney = newCoins)
                                                                Toast.makeText(context, "Deducted -$amt Coins! New Total: $newCoins", Toast.LENGTH_SHORT).show()
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Coin update failed: ${e.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1515), contentColor = Color(0xFFFF6B6B)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f).height(36.dp)
                                                ) {
                                                    Text("-$amt", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }

                                        ClassyDarkInput(
                                            value = coinAmountInput,
                                            onValueChange = { coinAmountInput = it },
                                            label = "Custom Coin Amount",
                                            placeholder = "e.g. 250"
                                        )

                                        // Action buttons row: Add, Deduct, Set
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val amt = coinAmountInput.toIntOrNull() ?: return@Button
                                                    scope.launch {
                                                        try {
                                                            val newCoins = user.appMoney + amt
                                                            val updates = mapOf("appMoney" to newCoins, "walletBalance" to newCoins)
                                                            db?.collection("users")?.document(user.uid)
                                                                ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                            foundUser = user.copy(appMoney = newCoins)
                                                            coinAmountInput = ""
                                                            Toast.makeText(context, "🪙 Added +$amt Coins! (Total: $newCoins)", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Failed to add coins: ${e.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            ) {
                                                Text("+ ADD", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    val amt = coinAmountInput.toIntOrNull() ?: return@Button
                                                    scope.launch {
                                                        try {
                                                            val newCoins = (user.appMoney - amt).coerceAtLeast(0)
                                                            val updates = mapOf("appMoney" to newCoins, "walletBalance" to newCoins)
                                                            db?.collection("users")?.document(user.uid)
                                                                ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                            foundUser = user.copy(appMoney = newCoins)
                                                            coinAmountInput = ""
                                                            Toast.makeText(context, "🪙 Deducted -$amt Coins! (Total: $newCoins)", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Failed to deduct coins: ${e.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48), contentColor = Color.White),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            ) {
                                                Text("- DEDUCT", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    val exact = coinAmountInput.toIntOrNull() ?: return@Button
                                                    scope.launch {
                                                        try {
                                                            val newCoins = exact.coerceAtLeast(0)
                                                            val updates = mapOf("appMoney" to newCoins, "walletBalance" to newCoins)
                                                            db?.collection("users")?.document(user.uid)
                                                                ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                            foundUser = user.copy(appMoney = newCoins)
                                                            coinAmountInput = ""
                                                            Toast.makeText(context, "🪙 Coins Set to Exactly: $newCoins", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Failed to set coins: ${e.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            ) {
                                                Text("= SET", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }
                                        }

                                        // Reset Coins to 0 Button
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        val updates = mapOf("appMoney" to 0, "walletBalance" to 0)
                                                        db?.collection("users")?.document(user.uid)
                                                            ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                        foundUser = user.copy(appMoney = 0)
                                                        coinAmountInput = ""
                                                        Toast.makeText(context, "🔄 Coins successfully RESET to 0!", Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Failed to reset coins: ${e.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2230), contentColor = Color(0xFFFF5252)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            Text("🔄 RESET COINS TO 0", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // 💵 SECTION 2: MANAGE CASH (realMoney)
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("💵 Manage Real Cash (₹)", fontWeight = FontWeight.Black, color = Color(0xFF00E676), fontSize = 13.sp)
                                            Text("Current: ₹${user.realMoney}", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        // Preset Quick Chips
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(50, 100, 500).forEach { amt ->
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            try {
                                                                val newCash = user.realMoney + amt
                                                                val updates = mapOf("realMoney" to newCash)
                                                                db?.collection("users")?.document(user.uid)
                                                                    ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                                foundUser = user.copy(realMoney = newCash)
                                                                Toast.makeText(context, "Added +₹$amt Cash! New Total: ₹$newCash", Toast.LENGTH_SHORT).show()
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Cash update failed: ${e.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D2517), contentColor = Color(0xFF00E676)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f).height(36.dp)
                                                ) {
                                                    Text("+₹$amt", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                }
                                            }

                                            listOf(50, 100, 500).forEach { amt ->
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            try {
                                                                val newCash = (user.realMoney - amt).coerceAtLeast(0)
                                                                val updates = mapOf("realMoney" to newCash)
                                                                db?.collection("users")?.document(user.uid)
                                                                    ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                                foundUser = user.copy(realMoney = newCash)
                                                                Toast.makeText(context, "Deducted -₹$amt Cash! New Total: ₹$newCash", Toast.LENGTH_SHORT).show()
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Cash update failed: ${e.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1515), contentColor = Color(0xFFFF6B6B)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.weight(1f).height(36.dp)
                                                ) {
                                                    Text("-₹$amt", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }

                                        ClassyDarkInput(
                                            value = cashAmountInput,
                                            onValueChange = { cashAmountInput = it },
                                            label = "Custom Cash Amount (₹)",
                                            placeholder = "e.g. 500"
                                        )

                                        // Action buttons row: Add, Deduct, Set
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val amt = cashAmountInput.toIntOrNull() ?: return@Button
                                                    scope.launch {
                                                        try {
                                                            val newCash = user.realMoney + amt
                                                            val updates = mapOf("realMoney" to newCash)
                                                            db?.collection("users")?.document(user.uid)
                                                                ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                            foundUser = user.copy(realMoney = newCash)
                                                            cashAmountInput = ""
                                                            Toast.makeText(context, "💵 Added +₹$amt Cash! (Total: ₹$newCash)", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Failed to add cash: ${e.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            ) {
                                                Text("+ ADD", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    val amt = cashAmountInput.toIntOrNull() ?: return@Button
                                                    scope.launch {
                                                        try {
                                                            val newCash = (user.realMoney - amt).coerceAtLeast(0)
                                                            val updates = mapOf("realMoney" to newCash)
                                                            db?.collection("users")?.document(user.uid)
                                                                ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                            foundUser = user.copy(realMoney = newCash)
                                                            cashAmountInput = ""
                                                            Toast.makeText(context, "💵 Deducted -₹$amt Cash! (Total: ₹$newCash)", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Failed to deduct cash: ${e.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48), contentColor = Color.White),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            ) {
                                                Text("- DEDUCT", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    val exact = cashAmountInput.toIntOrNull() ?: return@Button
                                                    scope.launch {
                                                        try {
                                                            val newCash = exact.coerceAtLeast(0)
                                                            val updates = mapOf("realMoney" to newCash)
                                                            db?.collection("users")?.document(user.uid)
                                                                ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                            foundUser = user.copy(realMoney = newCash)
                                                            cashAmountInput = ""
                                                            Toast.makeText(context, "💵 Cash Set to Exactly: ₹$newCash", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Failed to set cash: ${e.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.weight(1f).height(44.dp)
                                            ) {
                                                Text("= SET", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }
                                        }

                                        // Reset Cash to 0 Button
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        val updates = mapOf("realMoney" to 0)
                                                        db?.collection("users")?.document(user.uid)
                                                            ?.set(updates, com.google.firebase.firestore.SetOptions.merge())?.await()
                                                        foundUser = user.copy(realMoney = 0)
                                                        cashAmountInput = ""
                                                        Toast.makeText(context, "🔄 Real Cash successfully RESET to ₹0!", Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Failed to reset cash: ${e.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2230), contentColor = Color(0xFFFF5252)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(42.dp)
                                        ) {
                                            Text("🔄 RESET CASH TO ₹0", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. UPI & QR PAYMENT GATEWAY CONFIG
                4 -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("Official Payment UPI Settings", fontWeight = FontWeight.Black, color = AppColors.TextPrimary, fontSize = 15.sp)

                                    ClassyDarkInput(
                                        value = upiIdInput,
                                        onValueChange = { upiIdInput = it },
                                        label = "Admin UPI ID (GPay/PhonePe/Paytm/FamPay)",
                                        placeholder = "e.g. 6375615586@fam"
                                    )

                                    ClassyDarkInput(
                                        value = upiNameInput,
                                        onValueChange = { upiNameInput = it },
                                        label = "Business / Brand Display Name",
                                        placeholder = "e.g. Tournament Esports Official"
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            ClassyDarkInput(
                                                value = minDepositInput,
                                                onValueChange = { minDepositInput = it },
                                                label = "Min Deposit (₹)",
                                                placeholder = "10"
                                            )
                                        }
                                        Box(modifier = Modifier.weight(1f)) {
                                            ClassyDarkInput(
                                                value = minWithdrawInput,
                                                onValueChange = { minWithdrawInput = it },
                                                label = "Min Withdraw (₹)",
                                                placeholder = "50"
                                            )
                                        }
                                    }

                                    ClassyDarkInput(
                                        value = coinRateInput,
                                        onValueChange = { coinRateInput = it },
                                        label = "Coin Conversion Rate (X Coins = ₹1 Cash)",
                                        placeholder = "10"
                                    )

                                    Button(
                                        onClick = {
                                            val data = mapOf(
                                                "upiId" to upiIdInput.trim(),
                                                "upiName" to upiNameInput.trim(),
                                                "minDeposit" to (minDepositInput.toIntOrNull() ?: 10),
                                                "minWithdraw" to (minWithdrawInput.toIntOrNull() ?: 50),
                                                "coinConversionRate" to (coinRateInput.toIntOrNull() ?: 10)
                                            )
                                            db?.collection("settings")?.document("payment")?.set(data)
                                                ?.addOnSuccessListener {
                                                    Toast.makeText(context, "✅ Payment Settings Saved!", Toast.LENGTH_SHORT).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text("SAVE PAYMENT SETTINGS", color = Color.Black, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepositRequestCard(tx: TransactionRecord, onApprove: () -> Unit, onReject: () -> Unit) {
    val context = LocalContext.current
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
    var showFullScreenshot by remember { mutableStateOf(false) }

    val screenshotBitmap = remember(tx.screenshotBase64) {
        if (!tx.screenshotBase64.isNullOrBlank()) {
            try {
                val decodedBytes = Base64.decode(tx.screenshotBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    if (showFullScreenshot && screenshotBitmap != null) {
        Dialog(onDismissRequest = { showFullScreenshot = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp)
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
                        Text("Payment Proof Screenshot", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = { showFullScreenshot = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Image(
                        bitmap = screenshotBitmap.asImageBitmap(),
                        contentDescription = "Full Payment Screenshot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Text("UTR: ${tx.utrOrUpi} | ₹${tx.amount}", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(tx.userEmail, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("+₹${tx.amount}", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 18.sp)
            }

            // UTR Box with Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("UTR: ${tx.utrOrUpi}", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 12.sp)
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("UTR", tx.utrOrUpi))
                        Toast.makeText(context, "UTR Copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy UTR", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            // Payment Proof Screenshot Thumbnail
            if (screenshotBitmap != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, Color(0xFF2E3244), RoundedCornerShape(10.dp))
                        .clickable { showFullScreenshot = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        bitmap = screenshotBitmap.asImageBitmap(),
                        contentDescription = "Payment Screenshot Thumbnail",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Payment Screenshot Attached", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Text("Tap to view full receipt & verify UTR", color = Color(0xFF8E92A4), fontSize = 10.sp)
                    }
                    Icon(Icons.Default.Visibility, contentDescription = "View", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B1D26))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF8E92A4), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("No screenshot attached (Manual UTR Verification)", color = Color(0xFF8E92A4), fontSize = 10.sp)
                }
            }

            Text(dateStr, color = Color(0xFF75798E), fontSize = 10.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("APPROVE DEPOSIT", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REJECT DEPOSIT", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun WithdrawRequestCard(tx: TransactionRecord, onComplete: () -> Unit, onReject: () -> Unit) {
    val context = LocalContext.current
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(tx.userEmail, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("₹${tx.amount}", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 18.sp)
            }

            // User UPI Box with Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9FAFB))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pay to: ${tx.upiId}", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 12.sp)
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", tx.upiId))
                        Toast.makeText(context, "Player UPI Copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy UPI", tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            Text(dateStr, color = Color(0xFF75798E), fontSize = 10.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("APPROVE & PAID", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REJECT & REFUND", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyAdminPlaceholder(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(18.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(36.dp))
            Text(title, color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text(subtitle, color = Color(0xFF8E92A4), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun RechargeRequestCard(
    tx: TransactionRecord,
    onCompleteWithProof: (proofBase64: String, opTxnId: String) -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))
    val isJio = tx.operator.equals("JIO", ignoreCase = true)

    var proofScreenshotBase64 by remember { mutableStateOf("") }
    var operatorTxnId by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Photo picker launcher for admin recharge receipt screenshot
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = compressUriToBase64(context, uri)
            if (base64.isNotEmpty()) {
                proofScreenshotBase64 = base64
                Toast.makeText(context, "📸 Recharge screenshot attached!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not process image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val proofBitmap = remember(proofScreenshotBase64) {
        if (proofScreenshotBase64.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(proofScreenshotBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    if (showConfirmDialog) {
        Dialog(onDismissRequest = { showConfirmDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Confirm Recharge & Add Proof", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        IconButton(onClick = { showConfirmDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8E92A4))
                        }
                    }

                    Text(
                        "Number: +91 ${tx.mobileNumber} (${tx.operator})\nPack: ${tx.packDetails} (₹${tx.amount})",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Reference or Operator Txn ID (Optional)
                    OutlinedTextField(
                        value = operatorTxnId,
                        onValueChange = { operatorTxnId = it },
                        label = { Text("Operator Ref / UTR / Order ID (Optional)", fontSize = 11.sp) },
                        placeholder = { Text("e.g. GPAY_123456 or JIO_9876", color = Color(0xFF75798E), fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF2E3244),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Attach Screenshot Button
                    if (proofBitmap != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF9FAFB))
                                .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                bitmap = proofBitmap.asImageBitmap(),
                                contentDescription = "Proof",
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("✅ Screenshot Attached", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("Visible to user as proof of recharge", color = Color(0xFF8E92A4), fontSize = 10.sp)
                            }
                            IconButton(onClick = { proofScreenshotBase64 = "" }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ATTACH PAYMENT SCREENSHOT (PROOF)", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Text(
                        "Tip: Attaching a screenshot gives the user 100% indisputable proof that you recharged their phone.",
                        color = Color(0xFF8E92A4),
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showConfirmDialog = false },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", color = Color(0xFF8E92A4), fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                showConfirmDialog = false
                                onCompleteWithProof(proofScreenshotBase64, operatorTxnId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CONFIRM DONE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF160608))
            .border(1.5.dp, Color(0xFFE50914).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Operator Badge & Pack Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isJio) Color(0xFF0A2885) else Color(0xFFE40000))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isJio) "JIO 4G/5G" else "AIRTEL 5G",
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = if (tx.userEmail.isNotEmpty()) tx.userEmail else "Player",
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${tx.amount}",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                    Text(
                        "${tx.coinAmount} Coins",
                        color = Color(0xFFFFA000),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            // Pack Details
            Text(
                text = "⚡ Pack: ${tx.packDetails}",
                color = Color(0xFFFF8A80),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )

            // Mobile Number Box with 1-Tap Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF26080A))
                    .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Recharge Mobile Number:", color = Color(0xFFB0BEC5), fontSize = 10.sp)
                    Text(
                        "+91 ${tx.mobileNumber}",
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Mobile Number", tx.mobileNumber))
                        Toast.makeText(context, "📋 Copied +91 ${tx.mobileNumber}!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("COPY", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }

            Text("Requested: $dateStr", color = Color(0xFF8E92A4), fontSize = 10.sp)

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MARK RECHARGED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("REJECT & REFUND", color = AppColors.TextPrimary, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}
