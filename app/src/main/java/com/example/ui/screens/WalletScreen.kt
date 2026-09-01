package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ads.UnityAdsManager
import com.example.ads.UnityBannerAd
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.example.FirebaseHelper

data class TransactionRecord(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val type: String = "DEPOSIT", // "DEPOSIT", "WITHDRAW", "ENTRY_FEE", "WINNING", "CONVERT", "REDEEM"
    val amount: Int = 0,
    val status: String = "SUCCESS", // "PENDING", "SUCCESS", "REJECTED"
    val utrOrUpi: String = "",
    val upiId: String = "",
    val screenshotBase64: String = "",
    val screenshotUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

fun compressUriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        if (bitmap == null) return ""
        val maxDimension = 800
        val scale = Math.min(1.0, maxDimension.toDouble() / Math.max(bitmap.width, bitmap.height))
        val newWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val newHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val bytes = outputStream.toByteArray()
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        ""
    }
}

fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

data class PaymentSettings(
    val upiId: String = "6375615586@fam",
    val upiName: String = "Tournament Esports Official", // Privacy safe branding
    val minDeposit: Int = 10,
    val minWithdraw: Int = 50,
    val coinConversionRate: Int = 10 // 10 coins = 1 rupee
)

class WalletViewModel : ViewModel() {
    private fun getDb(): FirebaseFirestore? = FirebaseHelper.getFirestore()
    private fun getAuth(): FirebaseAuth? = FirebaseHelper.getAuth()

    private val _transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())
    val transactions: StateFlow<List<TransactionRecord>> = _transactions

    private val _paymentSettings = MutableStateFlow(PaymentSettings())
    val paymentSettings: StateFlow<PaymentSettings> = _paymentSettings

    private var txListener: ListenerRegistration? = null

    init {
        listenToTransactions()
        listenToPaymentSettings()
    }

    private fun listenToPaymentSettings() {
        try {
            val currentDb = getDb() ?: return
            currentDb.collection("settings").document("payment").addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    _paymentSettings.value = PaymentSettings(
                        upiId = doc.getString("upiId") ?: "6375615586@fam",
                        upiName = doc.getString("upiName") ?: "Tournament Esports Official",
                        minDeposit = (doc.getLong("minDeposit") ?: 10L).toInt(),
                        minWithdraw = (doc.getLong("minWithdraw") ?: 50L).toInt(),
                        coinConversionRate = (doc.getLong("coinConversionRate") ?: 10L).toInt()
                    )
                } else {
                    // Initialize default with Om's UPI & safe business brand
                    val defaultSettings = PaymentSettings(
                        upiId = "6375615586@fam",
                        upiName = "Tournament Esports Official",
                        minDeposit = 10,
                        minWithdraw = 50,
                        coinConversionRate = 10
                    )
                    currentDb.collection("settings").document("payment").set(defaultSettings)
                }
            }
        } catch (e: Exception) {
            // safe catch
        }
    }

    private fun listenToTransactions() {
        try {
            val currentAuth = getAuth() ?: return
            val currentDb = getDb() ?: return
            val user = currentAuth.currentUser ?: return
            txListener = currentDb.collection("transactions")
                .whereEqualTo("userId", user.uid)
                .addSnapshotListener { snap, _ ->
                    if (snap != null) {
                        val list = snap.documents.mapNotNull { it.toObject(TransactionRecord::class.java)?.copy(id = it.id) }
                            .sortedByDescending { it.timestamp }
                        _transactions.value = list
                    }
                }
        } catch (e: Exception) {
            // safe catch
        }
    }

    fun submitDepositRequest(
        amount: Int,
        utr: String,
        screenshotBase64: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = getAuth()?.currentUser ?: return onError("Please login first")
        val currentDb = getDb() ?: return onError("Database not available")
        if (utr.isBlank() || utr.length < 6) return onError("Please enter valid 12-digit UTR / Ref Number")
        if (amount < _paymentSettings.value.minDeposit) return onError("Minimum deposit is ₹${_paymentSettings.value.minDeposit}")

        val recordId = UUID.randomUUID().toString()
        val record = TransactionRecord(
            id = recordId,
            userId = user.uid,
            userEmail = user.email ?: "",
            type = "DEPOSIT",
            amount = amount,
            status = "PENDING",
            utrOrUpi = utr.trim(),
            screenshotBase64 = screenshotBase64,
            timestamp = System.currentTimeMillis(),
            note = if (screenshotBase64.isNotBlank()) "UPI Deposit Request (Screenshot Attached)" else "UPI Deposit Request (Pending Admin Verification)"
        )

        currentDb.collection("transactions").document(recordId).set(record)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Failed to submit deposit") }
    }

    fun submitWithdrawRequest(
        amount: Int,
        upiId: String,
        currentRealBalance: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = getAuth()?.currentUser ?: return onError("Please login first")
        val currentDb = getDb() ?: return onError("Database not available")
        if (upiId.isBlank() || !upiId.contains("@")) return onError("Please enter valid UPI ID (e.g. mobile@upi or name@okaxis)")
        if (amount < _paymentSettings.value.minWithdraw) return onError("Minimum withdrawal is ₹${_paymentSettings.value.minWithdraw}")
        if (currentRealBalance < amount) return onError("Insufficient balance! You have ₹$currentRealBalance")

        val recordId = UUID.randomUUID().toString()
        val userRef = currentDb.collection("users").document(user.uid)
        val txRef = currentDb.collection("transactions").document(recordId)

        currentDb.runTransaction { transaction ->
            val userSnap = transaction.get(userRef)
            val realMoney = (userSnap.getLong("realMoney") ?: 0L).toInt()
            if (realMoney < amount) {
                throw Exception("Insufficient balance")
            }

            // Deduct immediately and put in pending withdraw
            transaction.update(userRef, "realMoney", realMoney - amount)
            val record = TransactionRecord(
                id = recordId,
                userId = user.uid,
                userEmail = user.email ?: "",
                type = "WITHDRAW",
                amount = amount,
                status = "PENDING",
                upiId = upiId.trim(),
                timestamp = System.currentTimeMillis(),
                note = "Withdraw to $upiId"
            )
            transaction.set(txRef, record)
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onError(it.message ?: "Withdrawal failed")
        }
    }

    fun convertCoinsToCash(
        coinsToConvert: Int,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = getAuth()?.currentUser ?: return onError("Please login first")
        val currentDb = getDb() ?: return onError("Database not available")
        val rate = _paymentSettings.value.coinConversionRate
        if (coinsToConvert < rate) {
            return onError("Minimum $rate Coins required to convert to ₹1 Real Cash")
        }
        val cashGained = coinsToConvert / rate
        val coinsDeducted = cashGained * rate

        val userRef = currentDb.collection("users").document(user.uid)
        val recordId = UUID.randomUUID().toString()

        currentDb.runTransaction { transaction ->
            val userSnap = transaction.get(userRef)
            val appMoney = (userSnap.getLong("appMoney") ?: 0L).toInt()
            val realMoney = (userSnap.getLong("realMoney") ?: 0L).toInt()

            if (appMoney < coinsDeducted) {
                throw Exception("Not enough Coins in wallet!")
            }

            transaction.update(userRef, "appMoney", appMoney - coinsDeducted)
            transaction.update(userRef, "realMoney", realMoney + cashGained)

            val record = TransactionRecord(
                id = recordId,
                userId = user.uid,
                userEmail = user.email ?: "",
                type = "CONVERT",
                amount = cashGained,
                status = "SUCCESS",
                timestamp = System.currentTimeMillis(),
                note = "Converted $coinsDeducted Coins -> ₹$cashGained Cash"
            )
            transaction.set(currentDb.collection("transactions").document(recordId), record)
        }.addOnSuccessListener {
            onSuccess(cashGained)
        }.addOnFailureListener {
            onError(it.message ?: "Conversion failed")
        }
    }

    override fun onCleared() {
        super.onCleared()
        txListener?.remove()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel(),
    walletViewModel: WalletViewModel = viewModel()
) {
    val profile by userViewModel.profile.collectAsState()
    val transactions by walletViewModel.transactions.collectAsState()
    val paymentSettings by walletViewModel.paymentSettings.collectAsState()
    val context = LocalContext.current

    // Dialogs
    var showDepositDialog by remember { mutableStateOf(false) }
    var depositAmount by remember { mutableStateOf("50") }
    var depositUtr by remember { mutableStateOf("") }
    var depositScreenshotBase64 by remember { mutableStateOf("") }
    var isSubmittingDeposit by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val b64 = compressUriToBase64(context, uri)
            if (b64.isNotEmpty()) {
                depositScreenshotBase64 = b64
                Toast.makeText(context, "✅ Payment Screenshot Attached!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not load image, please try another file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showWithdrawDialog by remember { mutableStateOf(false) }
    var withdrawAmount by remember { mutableStateOf("100") }
    var withdrawUpiId by remember { mutableStateOf("") }
    var isSubmittingWithdraw by remember { mutableStateOf(false) }

    var showConvertDialog by remember { mutableStateOf(false) }

    // 1. UPI Deposit Dialog with Auto UPI Intent & Live High-Res QR
    if (showDepositDialog) {
        val targetUpi = paymentSettings.upiId
        val targetName = paymentSettings.upiName
        val selectedAmt = depositAmount.toIntOrNull() ?: 50

        // Dynamic UPI URI & Online High-Resolution QR Generator
        val upiUri = "upi://pay?pa=$targetUpi&pn=${Uri.encode(targetName)}&am=$selectedAmt&cu=INR&tn=FreeFireTournament"
        val qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&margin=10&data=${URLEncoder.encode(upiUri, StandardCharsets.UTF_8.toString())}"

        AlertDialog(
            containerColor = Color(0xFF14161F),
            onDismissRequest = { if (!isSubmittingDeposit) showDepositDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFD700)))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("ADD CASH VIA QR / UPI", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Step 1: Scan QR or Click to Pay using GPay / PhonePe / Paytm",
                        color = Color(0xFFC0C4D6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick Amount Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("20", "50", "100", "200").forEach { amt ->
                            val isSelected = depositAmount == amt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF222636) else Color(0xFF0C0D12))
                                    .border(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF262938), RoundedCornerShape(10.dp))
                                    .clickable { depositAmount = amt }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("₹$amt", color = if (isSelected) Color(0xFFFFD700) else Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                        }
                    }

                    ClassyDarkInput(
                        value = depositAmount,
                        onValueChange = { depositAmount = it },
                        label = "Deposit Amount (₹)",
                        placeholder = "e.g. 50"
                    )

                    // High-Res Scanner QR Code Box
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = qrApiUrl,
                            contentDescription = "Scan to Pay QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        "Scan QR code with any UPI App",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    // Direct UPI App Trigger Button
                    Button(
                        onClick = {
                            val uri = Uri.parse(upiUri)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(Intent.createChooser(intent, "Pay using UPI App"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No UPI App found. Scan QR or copy UPI ID.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PAY ₹$selectedAmt VIA GPAY / PHONEPE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }

                    // UPI ID Box with Copy Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C0D12))
                            .border(1.dp, Color(0xFF262938), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("OFFICIAL TOURNAMENT UPI ID", color = Color(0xFF75798E), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(targetUpi, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("UPI ID", targetUpi)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "UPI ID Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(34.dp).background(Color(0xFF1E212D), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Step 2: Enter the 12-digit UTR / Ref Number from payment receipt:",
                        color = Color(0xFFC0C4D6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ClassyDarkInput(
                        value = depositUtr,
                        onValueChange = { depositUtr = it },
                        label = "12-Digit UTR / Transaction Ref ID",
                        placeholder = "e.g. 423871928374"
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Step 3: Attach Payment Screenshot (Recommended for Instant Approval):",
                        color = Color(0xFFC0C4D6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (depositScreenshotBase64.isNotEmpty()) {
                        val previewBitmap = remember(depositScreenshotBase64) { decodeBase64ToBitmap(depositScreenshotBase64) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0C0D12))
                                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    if (previewBitmap != null) {
                                        Image(
                                            bitmap = previewBitmap.asImageBitmap(),
                                            contentDescription = "Receipt Preview",
                                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }
                                    Column {
                                        Text("Screenshot Attached ✅", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Ready to submit with deposit", color = Color(0xFF8E92A4), fontSize = 10.sp)
                                    }
                                }
                                IconButton(
                                    onClick = { depositScreenshotBase64 = "" },
                                    modifier = Modifier.size(32.dp).background(Color(0xFF262112), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("UPLOAD PAYMENT SCREENSHOT 📸", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = depositAmount.toIntOrNull() ?: 0
                        isSubmittingDeposit = true
                        walletViewModel.submitDepositRequest(
                            amount = amt,
                            utr = depositUtr,
                            screenshotBase64 = depositScreenshotBase64,
                            onSuccess = {
                                isSubmittingDeposit = false
                                showDepositDialog = false
                                depositUtr = ""
                                depositScreenshotBase64 = ""
                                Toast.makeText(context, "Deposit Request Submitted! Admin will verify and add cash.", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                isSubmittingDeposit = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isSubmittingDeposit && depositUtr.isNotBlank()
                ) {
                    if (isSubmittingDeposit) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                    } else {
                        Text("SUBMIT DEPOSIT", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isSubmittingDeposit) showDepositDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E92A4))
                }
            }
        )
    }

    // 2. Withdrawal Dialog
    if (showWithdrawDialog) {
        val currentBalance = profile?.realMoney ?: 0
        AlertDialog(
            containerColor = Color(0xFF14161F),
            onDismissRequest = { if (!isSubmittingWithdraw) showWithdrawDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF00E676)))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("INSTANT WITHDRAWAL", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0C0D12))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Available Real Balance", color = Color(0xFF75798E), fontSize = 12.sp)
                            Text("₹$currentBalance", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }

                    ClassyDarkInput(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = "Withdraw Amount (₹)",
                        placeholder = "Min ₹${paymentSettings.minWithdraw}"
                    )

                    ClassyDarkInput(
                        value = withdrawUpiId,
                        onValueChange = { withdrawUpiId = it },
                        label = "Your UPI ID (GPay/PhonePe)",
                        placeholder = "e.g. mobile@upi or name@okaxis"
                    )

                    Text(
                        "• Minimum withdrawal limit is ₹${paymentSettings.minWithdraw}.\n• Funds will be credited directly to your UPI address.",
                        color = Color(0xFF8E92A4),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = withdrawAmount.toIntOrNull() ?: 0
                        isSubmittingWithdraw = true
                        walletViewModel.submitWithdrawRequest(
                            amount = amt,
                            upiId = withdrawUpiId,
                            currentRealBalance = currentBalance,
                            onSuccess = {
                                isSubmittingWithdraw = false
                                showWithdrawDialog = false
                                withdrawUpiId = ""
                                Toast.makeText(context, "Withdraw Request Placed Successfully!", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                isSubmittingWithdraw = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isSubmittingWithdraw && withdrawAmount.isNotBlank() && withdrawUpiId.isNotBlank()
                ) {
                    if (isSubmittingWithdraw) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                    } else {
                        Text("CONFIRM WITHDRAW", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isSubmittingWithdraw) showWithdrawDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E92A4))
                }
            }
        )
    }

    // 3. Coin Perks & Discount Info Dialog
    if (showConvertDialog) {
        val userCoins = profile?.appMoney ?: 0

        AlertDialog(
            containerColor = Color(0xFF14161F),
            onDismissRequest = { showConvertDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🪙 VIRTUAL GAME COINS", fontWeight = FontWeight.Black, color = Color(0xFFFFD700), fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0C0D12))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Your Free Coins:", color = Color(0xFF8E92A4), fontSize = 12.sp)
                                Text("$userCoins 🪙", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                            Divider(color = Color(0xFF262938))
                            Text(
                                "🎁 HOW TO USE YOUR COINS:",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                "1. 🏷️ Tournament Entry Discounts:\nWhen joining paid matches (e.g. ₹20 Entry), apply your coins to get up to ₹3 - ₹5 OFF! Remaining entry is paid from your deposit balance.\n\n" +
                                "2. 🎟️ Google Play & Redeem Codes:\nRedeem special gaming gift vouchers and passes when available in the store.\n\n" +
                                "3. 🛡️ Safe & Fair Policy:\nCoins are virtual skill perks and cannot be directly withdrawn to bank/UPI. Real match prizes are won from tournament gameplay!",
                                color = Color(0xFFC0C4D6),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showConvertDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("GOT IT", color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", fontWeight = FontWeight.Black, fontSize = 26.sp, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAFAFA))
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

            // Real Money Card
            RealMoneyCardV2(
                balance = profile?.realMoney ?: 0,
                onAddCash = { showDepositDialog = true },
                onWithdraw = { showWithdrawDialog = true }
            )

            // App Money (Coins) Card
            AppMoneyCardV2(
                balance = profile?.appMoney ?: 0,
                onOpenStore = { navController.navigate("store") },
                onConvert = { showConvertDialog = true }
            )

            // Watch & Earn Free Coins Card (Unity Video Ad)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1B1834), Color(0xFF2B1B48))
                        )
                    )
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎬", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WATCH & EARN", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Get +15 Free Coins per ad!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Watch sponsor videos to join matches without depositing cash", color = Color(0xFFC0C4D6), fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                Toast.makeText(context, "🎬 Loading Video Ad...", Toast.LENGTH_SHORT).show()
                                UnityAdsManager.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = {
                                        userViewModel.addAppMoney(15)
                                        Toast.makeText(context, "🎉 +15 Free Coins Credited to Wallet!", Toast.LENGTH_LONG).show()
                                    },
                                    onAdFailed = {
                                        Toast.makeText(context, "Ad loading... Please try again in 5 seconds", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WATCH", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }

            // Unity Banner Ad in Wallet
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF14161F))
            ) {
                UnityBannerAd(modifier = Modifier.fillMaxWidth())
            }

            // Transaction History
            TransactionHistoryV2(transactions)

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun RealMoneyCardV2(balance: Int, onAddCash: () -> Unit, onWithdraw: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = Color(0x66000000),
                ambientColor = Color(0x33000000)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF12141D),
                        Color(0xFF1C1E2A),
                        Color(0xFF141622)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.15f),
                        Color(0xFFFFD700).copy(alpha = 0.35f)
                    )
                ),
                RoundedCornerShape(26.dp)
            )
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF252838))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "PRO WALLET",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        "REAL CASH BALANCE",
                        color = Color.White.copy(alpha = 0.75f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("INR (₹)", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("₹$balance", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = (-0.5).sp)
        }
        Spacer(modifier = Modifier.height(22.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onAddCash,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF111827))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Cash", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
            
            Button(
                onClick = onWithdraw,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF272A3B), contentColor = Color.White)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Withdraw", fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun AppMoneyCardV2(balance: Int, onOpenStore: () -> Unit = {}, onConvert: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x26000000),
                ambientColor = Color(0x1A000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF111319))
            .border(1.2.dp, Color(0xFF262A38), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E212D))
                        .border(1.dp, Color(0xFF2D3244), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("App Coins", fontWeight = FontWeight.Black, fontSize = 17.sp, color = Color.White)
                    Text("Earned from Watch Ad & Spin", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                }
            }
            Text("$balance 🪙", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Big Prominent Button: Redeem Google Play & VIP Discount Cards
        Button(
            onClick = onOpenStore,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
        ) {
            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("OPEN REWARDS & CARDS STORE 🛍️", fontWeight = FontWeight.Black, fontSize = 12.5.sp, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))
        
        OutlinedButton(
            onClick = onConvert,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = CircleShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3348))
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF8E92A4), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("How to use Coins for Match Discounts", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF8E92A4))
        }
    }
}

@Composable
fun TransactionHistoryV2(transactions: List<TransactionRecord>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Transactions", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Text("${transactions.size} Records", fontSize = 11.sp, color = Color(0xFF8E8E93), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF111319))
                    .border(1.dp, Color(0xFF262A38), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions yet. Add cash or join matches to view history.", color = Color(0xFF9CA3AF), fontSize = 12.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                transactions.take(15).forEach { tx ->
                    val isPositive = tx.type == "DEPOSIT" || tx.type == "WINNING" || tx.type == "CONVERT"
                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))

                    val sign = if (isPositive) "+" else "-"
                    val displayAmt = "$sign₹${tx.amount}"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF111319))
                            .border(1.dp, Color(0xFF262A38), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (tx.status) {
                                            "PENDING" -> Color(0xFFFFF8E1)
                                            "REJECTED" -> Color(0xFFFFEBEE)
                                            else -> if (isPositive) Color(0xFFE8F5E9) else Color(0xFFF2F2F7)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (tx.type) {
                                        "DEPOSIT" -> Icons.Default.Add
                                        "WITHDRAW" -> Icons.Default.ArrowUpward
                                        "CONVERT" -> Icons.Default.SwapHoriz
                                        else -> Icons.Default.SportsEsports
                                    },
                                    contentDescription = null,
                                    tint = when (tx.status) {
                                        "PENDING" -> Color(0xFFFFA000)
                                        "REJECTED" -> Color(0xFFD32F2F)
                                        else -> if (isPositive) Color(0xFF2E7D32) else Color.Black
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    when (tx.type) {
                                        "DEPOSIT" -> "UPI Deposit"
                                        "WITHDRAW" -> "Cash Withdrawal"
                                        "CONVERT" -> "Coins Converted"
                                        "ENTRY_FEE" -> "Match Entry Fee"
                                        "WINNING" -> "Tournament Prize"
                                        else -> tx.type
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(dateStr, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                                if (tx.utrOrUpi.isNotBlank()) {
                                    Text("Ref: ${tx.utrOrUpi}", fontSize = 10.sp, color = Color(0xFF666677))
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                displayAmt,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isPositive) Color(0xFF2E7D32) else Color.Black
                            )
                            if (tx.status == "PENDING") {
                                Text("PENDING", color = Color(0xFFFFA000), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            } else if (tx.status == "REJECTED") {
                                Text("REJECTED", color = Color(0xFFD32F2F), fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
