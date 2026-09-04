package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class StoreItemCategory {
    GOOGLE_PLAY,
    APP_DISCOUNT,
    DATA_RECHARGE
}

data class DataRechargePlan(
    val id: String,
    val operator: String, // "JIO" or "AIRTEL"
    val dataAmount: String, // "10 GB", "1.5 GB", "2.5 GB", "25 GB", "1 GB", "2 GB", "High-Speed Bulk"
    val priceRupees: Int,
    val coinPrice: Int,
    val validity: String,
    val tagText: String,
    val description: String,
    val highlightSpeed: String = "4G / 5G High Speed"
)

data class StoreItem(
    val id: String,
    val category: StoreItemCategory,
    val title: String,
    val subtitle: String,
    val coinPrice: Int,
    val denominationRupees: Int = 0, // e.g. ₹10, ₹25, ₹50, ₹100 for Google Play
    val discountPercent: Int = 0, // e.g. 15%, 30%, 50%, 100%
    val discountFlatRupees: Int = 0, // e.g. ₹5, ₹15
    val maxUses: Int = 1, // e.g. 1, 3, 5 matches
    val cardColorTheme: String = "GOLD", // "GOOGLE_PLAY", "BRONZE", "SILVER", "GOLD", "DIAMOND", "LEGEND", "GOD"
    val badgeText: String = "POPULAR",
    val description: String = ""
)

data class UserPurchasedCard(
    val id: String = "",
    val userId: String = "",
    val itemId: String = "",
    val category: String = "GOOGLE_PLAY", // "GOOGLE_PLAY" or "APP_DISCOUNT"
    val title: String = "",
    val denominationRupees: Int = 0,
    val discountPercent: Int = 0,
    val discountFlatRupees: Int = 0,
    val code: String = "", // 16-character Google Play code or VIP Pass Code
    val coinsPaid: Int = 0,
    val status: String = "ACTIVE", // "ACTIVE", "USED", "EXPIRED"
    val totalUses: Int = 1,
    val remainingUses: Int = 1,
    val purchaseTimestamp: Long = System.currentTimeMillis(),
    val cardColorTheme: String = "GOLD",
    val proofScreenshotBase64: String = "",
    val screenshotBase64: String = "",
    val rechargeTxnId: String = "",
    val operatorTxnId: String = ""
)

class StoreViewModel : ViewModel() {
    private fun getDb(): FirebaseFirestore? = FirebaseHelper.getFirestore()
    private fun getAuth(): FirebaseAuth? = FirebaseHelper.getAuth()

    private val _purchasedCards = MutableStateFlow<List<UserPurchasedCard>>(emptyList())
    val purchasedCards: StateFlow<List<UserPurchasedCard>> = _purchasedCards

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var cardsListener: ListenerRegistration? = null

    val googlePlayCards = listOf(
        StoreItem(
            id = "gp_10",
            category = StoreItemCategory.GOOGLE_PLAY,
            title = "Google Play ₹10 Code",
            subtitle = "Instant Play Store Balance Code",
            coinPrice = 100,
            denominationRupees = 10,
            cardColorTheme = "GOOGLE_PLAY",
            badgeText = "STARTER",
            description = "Get ₹10 Google Play balance directly to purchase Free Fire Diamonds, Airdrops, or play store apps!"
        ),
        StoreItem(
            id = "gp_25",
            category = StoreItemCategory.GOOGLE_PLAY,
            title = "Google Play ₹25 Code",
            subtitle = "Special Airdrop Pass Code",
            coinPrice = 240,
            denominationRupees = 25,
            cardColorTheme = "GOOGLE_PLAY",
            badgeText = "HOT SELLER 🔥",
            description = "Perfect for ₹29 Free Fire Special Airdrop boxes and character bundles!"
        ),
        StoreItem(
            id = "gp_50",
            category = StoreItemCategory.GOOGLE_PLAY,
            title = "Google Play ₹50 Code",
            subtitle = "Weekly Top-Up Voucher",
            coinPrice = 480,
            denominationRupees = 50,
            cardColorTheme = "GOOGLE_PLAY",
            badgeText = "POPULAR ⭐",
            description = "Redeem ₹50 directly in your Google Play Store account with 1-tap copy!"
        ),
        StoreItem(
            id = "gp_100",
            category = StoreItemCategory.GOOGLE_PLAY,
            title = "Google Play ₹100 Code",
            subtitle = "Pro Gamer Balance Code",
            coinPrice = 950,
            denominationRupees = 100,
            cardColorTheme = "GOOGLE_PLAY",
            badgeText = "BEST VALUE 💎",
            description = "Get 100 Diamonds + bonus top-up in Free Fire or any game on Google Play."
        ),
        StoreItem(
            id = "gp_250",
            category = StoreItemCategory.GOOGLE_PLAY,
            title = "Google Play ₹250 Code",
            subtitle = "Mega Diamond Pass Voucher",
            coinPrice = 2350,
            denominationRupees = 250,
            cardColorTheme = "GOOGLE_PLAY",
            badgeText = "VIP PASS 👑",
            description = "Redeem ₹250 instant Google Play balance for Elite Passes and Mega Bundles!"
        )
    )

    val appDiscountCards = listOf(
        StoreItem(
            id = "disc_flat_5",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "Bronze Flat ₹5 OFF Card",
            subtitle = "Save ₹5 on your next 3 match entries",
            coinPrice = 40,
            discountFlatRupees = 5,
            maxUses = 3,
            cardColorTheme = "BRONZE",
            badgeText = "3 USES",
            description = "Gives you a Flat ₹5 instant deduction on the next 3 tournament scrims you join!"
        ),
        StoreItem(
            id = "disc_flat_10",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "Silver Flat ₹10 OFF Card",
            subtitle = "Save ₹10 on your next 3 match entries",
            coinPrice = 75,
            discountFlatRupees = 10,
            maxUses = 3,
            cardColorTheme = "SILVER",
            badgeText = "SAVE ₹30 TOTAL",
            description = "Save ₹10 on every match you join for 3 consecutive entries."
        ),
        StoreItem(
            id = "disc_pct_25",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "Gold 25% OFF VIP Pass",
            subtitle = "25% discount on all entry fees",
            coinPrice = 110,
            discountPercent = 25,
            maxUses = 5,
            cardColorTheme = "GOLD",
            badgeText = "5 MATCHES VIP",
            description = "Get 25% OFF on up to 5 tournament entry tickets. High savings on mega prize pools!"
        ),
        StoreItem(
            id = "disc_pct_50",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "Diamond 50% Half-Price Pass",
            subtitle = "Play top tournaments at half price",
            coinPrice = 190,
            discountPercent = 50,
            maxUses = 3,
            cardColorTheme = "DIAMOND",
            badgeText = "HALF PRICE 🔥",
            description = "Pay only 50% entry fee on 3 mega tournaments. Top choice for competitive esports players!"
        ),
        StoreItem(
            id = "disc_free_100",
            category = StoreItemCategory.APP_DISCOUNT,
            title = "God-Tier 100% Free Pass",
            subtitle = "100% Free Entry for any 1 Match",
            coinPrice = 280,
            discountPercent = 100,
            maxUses = 1,
            cardColorTheme = "GOD",
            badgeText = "100% FREE ENTRY 👑",
            description = "Enjoy completely free entry to any paid tournament of your choice without spending real money!"
        )
    )

    // JIO Official Data Booster Plans (2024-2026 Latest)
    val jioRechargePlans = listOf(
        DataRechargePlan(
            id = "jio_data_11",
            operator = "JIO",
            dataAmount = "10 GB",
            priceRupees = 11,
            coinPrice = 130,
            validity = "1 Hour Validity",
            tagText = "⚡ 10GB 1-HOUR BOOSTER",
            description = "10GB High-Speed 4G/5G data valid for 1 hour. Ideal for high-stakes tournament matches!",
            highlightSpeed = "True 5G / 4G Speed"
        ),
        DataRechargePlan(
            id = "jio_data_19",
            operator = "JIO",
            dataAmount = "1.5 GB",
            priceRupees = 19,
            coinPrice = 230,
            validity = "Active Base Plan Validity",
            tagText = "🔥 BESTSELLER",
            description = "1.5GB High-Speed 4G/5G data booster. Valid till your active daily pack ends.",
            highlightSpeed = "True 5G / 4G Speed"
        ),
        DataRechargePlan(
            id = "jio_data_29",
            operator = "JIO",
            dataAmount = "2.5 GB",
            priceRupees = 29,
            coinPrice = 340,
            validity = "Active Base Plan Validity",
            tagText = "💎 EXTRA VALUE",
            description = "2.5GB High-Speed 4G/5G data booster. Perfect for intense scrims & live streams.",
            highlightSpeed = "True 5G / 4G Speed"
        ),
        DataRechargePlan(
            id = "jio_data_49",
            operator = "JIO",
            dataAmount = "25 GB",
            priceRupees = 49,
            coinPrice = 570,
            validity = "1 Day (24 Hours)",
            tagText = "🚀 25GB POWERPACK",
            description = "Massive 25GB High-Speed data valid for full 24 hours. Play all day with zero ping issues!",
            highlightSpeed = "True 5G / 4G Speed"
        )
    )

    // AIRTEL Official Real Data Booster Plans (₹22 1GB, ₹33 2GB, ₹65 4GB, ₹77 5GB, ₹121 6GB)
    val airtelRechargePlans = listOf(
        DataRechargePlan(
            id = "airtel_data_22",
            operator = "AIRTEL",
            dataAmount = "1 GB",
            priceRupees = 22,
            coinPrice = 250,
            validity = "1 Day Validity",
            tagText = "🔥 POPULAR CHOICE",
            description = "1GB High-Speed 4G/5G data booster valid for 1 day. Keep your games connected seamlessly.",
            highlightSpeed = "Airtel 5G Plus / 4G"
        ),
        DataRechargePlan(
            id = "airtel_data_33",
            operator = "AIRTEL",
            dataAmount = "2 GB",
            priceRupees = 33,
            coinPrice = 370,
            validity = "1 Day Validity",
            tagText = "💎 PRO GAMER PACK",
            description = "2GB High-Speed 4G/5G data booster valid for 1 day. Extra data for custom tournaments.",
            highlightSpeed = "Airtel 5G Plus / 4G"
        ),
        DataRechargePlan(
            id = "airtel_data_65",
            operator = "AIRTEL",
            dataAmount = "4 GB",
            priceRupees = 65,
            coinPrice = 730,
            validity = "Active Base Plan Validity",
            tagText = "⚡ BEST VALUE",
            description = "4GB High-Speed data booster valid till your current active base pack ends.",
            highlightSpeed = "Airtel 5G Plus / 4G"
        ),
        DataRechargePlan(
            id = "airtel_data_77",
            operator = "AIRTEL",
            dataAmount = "5 GB",
            priceRupees = 77,
            coinPrice = 860,
            validity = "Active Base Plan Validity",
            tagText = "🚀 HEAVY DATA PACK",
            description = "5GB High-Speed 4G/5G data with active pack validity. Zero ping drop in gaming.",
            highlightSpeed = "Airtel 5G Plus / 4G"
        )
    )

    init {
        listenToUserInventory()
    }

    private fun listenToUserInventory() {
        val auth = getAuth() ?: return
        val db = getDb() ?: return
        val user = auth.currentUser ?: return

        cardsListener?.remove()
        cardsListener = db.collection("users")
            .document(user.uid)
            .collection("inventory")
            .orderBy("purchaseTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(UserPurchasedCard::class.java)?.copy(id = doc.id)
                    }
                    _purchasedCards.value = list
                }
            }
    }

    fun purchaseItem(
        item: StoreItem,
        userCoins: Int,
        onSuccess: (UserPurchasedCard) -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = getAuth() ?: run {
            onError("Authentication not available")
            return
        }
        val db = getDb() ?: run {
            onError("Database not available")
            return
        }
        val user = auth.currentUser ?: run {
            onError("Please log in to purchase")
            return
        }

        if (userCoins < item.coinPrice) {
            onError("Insufficient Coins! You need ${item.coinPrice} coins but have $userCoins.")
            return
        }

        _isLoading.value = true

        val userDocRef = db.collection("users").document(user.uid)
        val inventoryCollRef = userDocRef.collection("inventory")
        val newCardId = UUID.randomUUID().toString()

        // Generate stylized Code
        val generatedCode = if (item.category == StoreItemCategory.GOOGLE_PLAY) {
            generateGooglePlayCode()
        } else {
            generateAppDiscountCode(item.cardColorTheme)
        }

        val purchasedCard = UserPurchasedCard(
            id = newCardId,
            userId = user.uid,
            itemId = item.id,
            category = item.category.name,
            title = item.title,
            denominationRupees = item.denominationRupees,
            discountPercent = item.discountPercent,
            discountFlatRupees = item.discountFlatRupees,
            code = generatedCode,
            coinsPaid = item.coinPrice,
            status = "ACTIVE",
            totalUses = item.maxUses,
            remainingUses = item.maxUses,
            purchaseTimestamp = System.currentTimeMillis(),
            cardColorTheme = item.cardColorTheme
        )

        db.runTransaction { tx ->
            val userSnap = tx.get(userDocRef)
            val currentCoins = userSnap.getLong("appMoney")?.toInt() ?: 0
            if (currentCoins < item.coinPrice) {
                throw Exception("Insufficient Coins balance! Need ${item.coinPrice} coins.")
            }

            // Check if admin has set custom settings or code pool
            val settingsDocRef = db.collection("store_settings").document(item.id)
            val settingsSnap = tx.get(settingsDocRef)
            var finalCode = generatedCode

            if (settingsSnap.exists()) {
                val isEnabled = settingsSnap.getBoolean("enabled") ?: true
                if (!isEnabled) {
                    throw Exception("This item is temporarily unavailable.")
                }

                val codesList = (settingsSnap.get("codes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (codesList.isNotEmpty()) {
                    finalCode = codesList.first()
                    val remainingCodes = codesList.drop(1)
                    tx.update(settingsDocRef, "codes", remainingCodes)
                }
            }

            // Deduct Coins
            tx.update(userDocRef, "appMoney", currentCoins - item.coinPrice)

            // Save to User Inventory
            val cardDocRef = inventoryCollRef.document(newCardId)
            val finalCard = purchasedCard.copy(code = finalCode)
            tx.set(cardDocRef, finalCard)

            // Record in global transactions
            val txId = UUID.randomUUID().toString()
            val txRecord = mapOf(
                "id" to txId,
                "userId" to user.uid,
                "userEmail" to (user.email ?: ""),
                "type" to "STORE_REDEEM",
                "amount" to item.coinPrice,
                "status" to "SUCCESS",
                "timestamp" to System.currentTimeMillis(),
                "note" to "Store Purchase: ${item.title} (Code: ${finalCode.take(6)}...)"
            )
            tx.set(db.collection("transactions").document(txId), txRecord)
        }.addOnSuccessListener {
            _isLoading.value = false
            onSuccess(purchasedCard)
        }.addOnFailureListener { e ->
            _isLoading.value = false
            onError(e.localizedMessage ?: "Purchase failed. Please try again.")
        }
    }

    fun requestDataRecharge(
        plan: DataRechargePlan,
        mobileNumber: String,
        userCoins: Int,
        userName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanNumber = mobileNumber.trim().replace("+91", "").replace(" ", "").replace("-", "")
        if (cleanNumber.length != 10 || !cleanNumber.all { it.isDigit() } || cleanNumber[0] !in listOf('6', '7', '8', '9')) {
            onError("Please enter a valid 10-digit Indian mobile number (starts with 6, 7, 8, or 9)")
            return
        }

        if (userCoins < plan.coinPrice) {
            onError("Insufficient Coins! You need ${plan.coinPrice} coins but have $userCoins.")
            return
        }

        val auth = getAuth() ?: run {
            onError("Authentication not available")
            return
        }
        val db = getDb() ?: run {
            onError("Database not connected")
            return
        }
        val user = auth.currentUser ?: run {
            onError("User not logged in")
            return
        }

        _isLoading.value = true

        val txId = UUID.randomUUID().toString()
        val userDocRef = db.collection("users").document(user.uid)
        val packDesc = "${plan.operator} ₹${plan.priceRupees} (${plan.dataAmount})"

        db.runTransaction { tx ->
            val userSnap = tx.get(userDocRef)
            val currentCoins = (userSnap.getLong("appMoney") ?: 0L).toInt()

            if (currentCoins < plan.coinPrice) {
                throw Exception("Insufficient Coins balance! Need ${plan.coinPrice} coins.")
            }

            // 1. Deduct coins from user
            tx.update(userDocRef, "appMoney", currentCoins - plan.coinPrice)

            // 2. Add to user's inventory
            val inventoryDocRef = userDocRef.collection("inventory").document(txId)
            val orderRecord = UserPurchasedCard(
                id = txId,
                userId = user.uid,
                itemId = plan.id,
                category = "DATA_RECHARGE",
                title = packDesc,
                denominationRupees = plan.priceRupees,
                code = "Mobile: +91 $cleanNumber",
                coinsPaid = plan.coinPrice,
                status = "PENDING_RECHARGE",
                cardColorTheme = "RED_RECHARGE",
                purchaseTimestamp = System.currentTimeMillis()
            )
            tx.set(inventoryDocRef, orderRecord)

            // 3. Log in transactions for Admin tracking
            val txRecord = mapOf(
                "id" to txId,
                "userId" to user.uid,
                "userEmail" to (user.email ?: ""),
                "userName" to userName,
                "type" to "DATA_RECHARGE",
                "amount" to plan.priceRupees,
                "coinAmount" to plan.coinPrice,
                "operator" to plan.operator,
                "mobileNumber" to cleanNumber,
                "packDetails" to packDesc,
                "status" to "PENDING",
                "timestamp" to System.currentTimeMillis(),
                "note" to "Mobile Recharge Request: $packDesc for +91 $cleanNumber"
            )
            tx.set(db.collection("transactions").document(txId), txRecord)
        }.addOnSuccessListener {
            _isLoading.value = false
            onSuccess("Recharge request for +91 $cleanNumber ($packDesc) placed successfully! Admin will process it shortly.")
        }.addOnFailureListener { e ->
            _isLoading.value = false
            onError(e.localizedMessage ?: "Recharge request failed. Please try again.")
        }
    }

    private fun generateGooglePlayCode(): String {
        // Generates realistic 16-char format: 4 groups of 4 uppercase chars/digits
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // Safe alphanumeric without confusing O, 0, 1, I
        fun randBlock(len: Int) = (1..len).map { chars.random() }.joinToString("")
        return "${randBlock(4)}-${randBlock(4)}-${randBlock(4)}-${randBlock(4)}"
    }

    private fun generateAppDiscountCode(theme: String): String {
        val chars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        fun randBlock(len: Int) = (1..len).map { chars.random() }.joinToString("")
        val prefix = when (theme) {
            "GOD" -> "FREE"
            "DIAMOND" -> "HALF"
            "GOLD" -> "GOLD"
            "SILVER" -> "SLVR"
            else -> "DISC"
        }
        return "$prefix-${randBlock(4)}-${randBlock(4)}"
    }

    fun markCardUsed(cardId: String) {
        val user = getAuth()?.currentUser ?: return
        val db = getDb() ?: return
        db.collection("users").document(user.uid).collection("inventory").document(cardId).update("status", "USED")
    }

    override fun onCleared() {
        super.onCleared()
        cardsListener?.remove()
    }
}
