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
    APP_DISCOUNT
}

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
    val cardColorTheme: String = "GOLD"
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
