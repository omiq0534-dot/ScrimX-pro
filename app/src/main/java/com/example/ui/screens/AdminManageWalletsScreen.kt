package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageWalletsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = Deposit Requests, 1 = Withdraw Requests, 2 = Manual Edit, 3 = UPI Settings

    var pendingDeposits by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }
    var pendingWithdraws by remember { mutableStateOf<List<TransactionRecord>>(emptyList()) }

    // UPI Settings State
    var upiIdInput by remember { mutableStateOf("6375615586@fam") }
    var upiNameInput by remember { mutableStateOf("Tournament Esports Official") }
    var minDepositInput by remember { mutableStateOf("10") }
    var minWithdrawInput by remember { mutableStateOf("50") }
    var coinRateInput by remember { mutableStateOf("10") }

    // Manual Edit State
    var searchEmail by remember { mutableStateOf("") }
    var foundUser by remember { mutableStateOf<UserProfile?>(null) }
    var amountToAdd by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchMessage by remember { mutableStateOf("") }

    // Listen to real-time transactions
    LaunchedEffect(Unit) {
        db.collection("transactions").addSnapshotListener { snap, _ ->
            if (snap != null) {
                val allTx = snap.documents.mapNotNull { it.toObject(TransactionRecord::class.java)?.copy(id = it.id) }
                pendingDeposits = allTx.filter { it.type == "DEPOSIT" && it.status == "PENDING" }
                    .sortedByDescending { it.timestamp }
                pendingWithdraws = allTx.filter { it.type == "WITHDRAW" && it.status == "PENDING" }
                    .sortedByDescending { it.timestamp }
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

    Scaffold(
        containerColor = Color(0xFF0C0D12),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "WALLET & CASHOUT COMMAND",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF14161F))
                    .border(1.dp, Color(0xFF262938), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "Deposits (${pendingDeposits.size})",
                    "Withdraws (${pendingWithdraws.size})",
                    "Manual",
                    "UPI Setup"
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
                                                db.collection("users").document(tx.userId)
                                                    .update("realMoney", FieldValue.increment(tx.amount.toLong())).await()
                                                // 2. Mark TX success
                                                db.collection("transactions").document(tx.id)
                                                    .update("status", "SUCCESS").await()
                                                Toast.makeText(context, "✅ Approved ₹${tx.amount} to user!", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Approval error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onReject = {
                                        scope.launch {
                                            db.collection("transactions").document(tx.id).update("status", "REJECTED").await()
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
                                            db.collection("transactions").document(tx.id).update("status", "SUCCESS").await()
                                            Toast.makeText(context, "✅ Marked withdrawal as Paid!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onReject = {
                                        scope.launch {
                                            // Refund user balance
                                            db.collection("users").document(tx.userId)
                                                .update("realMoney", FieldValue.increment(tx.amount.toLong())).await()
                                            db.collection("transactions").document(tx.id)
                                                .update("status", "REJECTED").await()
                                            Toast.makeText(context, "❌ Rejected & refunded ₹${tx.amount} to user", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. MANUAL USER BALANCE EDIT
                2 -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF14161F))
                                    .border(1.dp, Color(0xFF262938), RoundedCornerShape(18.dp))
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Find Player to Edit Wallet", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                                    ClassyDarkInput(
                                        value = searchEmail,
                                        onValueChange = { searchEmail = it },
                                        label = "Player Email Address",
                                        placeholder = "e.g. gamer@gmail.com"
                                    )
                                    Button(
                                        onClick = {
                                            isSearching = true
                                            searchMessage = ""
                                            scope.launch {
                                                try {
                                                    val snapshot = db.collection("users").whereEqualTo("email", searchEmail.trim()).get().await()
                                                    if (snapshot.isEmpty) {
                                                        searchMessage = "No player found with this email!"
                                                        foundUser = null
                                                    } else {
                                                        foundUser = snapshot.documents[0].toObject(UserProfile::class.java)?.copy(uid = snapshot.documents[0].id)
                                                        searchMessage = "Player profile loaded!"
                                                    }
                                                } catch (e: Exception) {
                                                    searchMessage = "Error searching user."
                                                } finally {
                                                    isSearching = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("FIND PLAYER", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                    }

                                    if (searchMessage.isNotEmpty()) {
                                        Text(searchMessage, color = if (foundUser != null) Color(0xFF00E676) else Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (foundUser != null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0xFF14161F))
                                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                        .padding(18.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(foundUser!!.name, fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                                                Text(foundUser!!.email, color = Color(0xFF8E92A4), fontSize = 12.sp)
                                            }
                                            Text("₹${foundUser!!.realMoney}", fontWeight = FontWeight.Black, color = Color(0xFFFFD700), fontSize = 18.sp)
                                        }

                                        ClassyDarkInput(
                                            value = amountToAdd,
                                            onValueChange = { amountToAdd = it },
                                            label = "Amount to Add (use - for deduct)",
                                            placeholder = "e.g. 100 or -50"
                                        )

                                        Button(
                                            onClick = {
                                                val amt = amountToAdd.toIntOrNull() ?: return@Button
                                                scope.launch {
                                                    val newBal = foundUser!!.realMoney + amt
                                                    db.collection("users").document(foundUser!!.uid).update("realMoney", newBal).await()
                                                    foundUser = foundUser!!.copy(realMoney = newBal)
                                                    amountToAdd = ""
                                                    Toast.makeText(context, "Balance updated to ₹$newBal", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().height(48.dp)
                                        ) {
                                            Text("UPDATE BALANCE", color = Color.Black, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. UPI & QR PAYMENT GATEWAY CONFIG
                3 -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF14161F))
                                    .border(1.dp, Color(0xFF262938), RoundedCornerShape(18.dp))
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("Official Payment UPI Settings", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)

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
                                            db.collection("settings").document("payment").set(data)
                                                .addOnSuccessListener {
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF14161F))
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(tx.userEmail, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("+₹${tx.amount}", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 18.sp)
            }

            // UTR Box with Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0C0D12))
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

            Text(dateStr, color = Color(0xFF75798E), fontSize = 10.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("APPROVE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("REJECT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
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
            .background(Color(0xFF14161F))
            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(tx.userEmail, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("₹${tx.amount}", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 18.sp)
            }

            // User UPI Box with Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0C0D12))
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
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("MARK PAID", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("REJECT & REFUND", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
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
            .background(Color(0xFF14161F))
            .border(1.dp, Color(0xFF262938), RoundedCornerShape(18.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(36.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text(subtitle, color = Color(0xFF8E92A4), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
