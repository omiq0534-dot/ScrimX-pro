package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard

data class MatchData(
    val id: String = "",
    val title: String = "",
    val time: String = "",
    val joinTime: String = "",               // e.g. "07:30 PM" - time when registration opens
    val resultTime: String = "",             // e.g. "08:45 PM" - time when results / points table declare
    val isResultDeclared: Boolean = false,   // Admin toggle to declare results
    val pointsTableImageUrl: String = "",    // Admin screenshot URL or image
    val pointsTableNotes: String = "",       // Admin summary or MVP notes
    val pointsTableRanks: String = "",       // Formatted standings: "1 | Team Toxic | 14 Kills | ₹300\n2 | ..."
    val resultPublicDelayMinutes: Int = 10,  // Delay for non-joined players
    val prize: String = "",
    val entry: String = "",
    val entryType: String = "PAID", // "FREE", "AD", "PAID"
    val requiredAds: Int = 1,        // Number of ads required when entryType == "AD"
    val badge: String = "",
    val status: String = "Upcoming",
    val roomId: String = "",
    val roomPass: String = "",
    val liveUrl: String = "",
    val map: String = "Bermuda",
    val mode: String = "Squad", // "Solo", "Duo", "Squad", "1v1", "2v2", "4v4"
    val matchType: String = "BR", // "BR", "CS", "TDM", "DUEL"
    val rules: String = "", // Custom match rules set by admin for this specific match
    val totalSlots: Int = 12,   // 48 for Solo, 24 for Duo, 12 for Squad, 2 for 1v1, 8 for 4v4
    val bookedSlots: Map<String, String> = emptyMap(), // slotNumber -> user UID/email
    val slotNames: Map<String, String> = emptyMap(),   // slotNumber -> Player IGN / Team Name
    val slotUids: Map<String, String> = emptyMap(),     // slotNumber -> In-game numeric UID
    val slotBadges: Map<String, String> = emptyMap()   // slotNumber -> badge key ("OWNER", "MOD", "X_BADGE", "NONE")
)

class MatchesViewModel : ViewModel() {
    private fun getDb(): FirebaseFirestore? = FirebaseHelper.getFirestore()
    private fun getAuth(): FirebaseAuth? = FirebaseHelper.getAuth()
    
    private val _matches = MutableStateFlow<List<MatchData>>(emptyList())
    val matches: StateFlow<List<MatchData>> = _matches

    private val _isLoading = MutableStateFlow<Boolean>(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _currentMatch = MutableStateFlow<MatchData?>(null)
    val currentMatch: StateFlow<MatchData?> = _currentMatch
    
    private var matchesListener: ListenerRegistration? = null
    private var currentMatchListener: ListenerRegistration? = null
    
    init {
        listenToMatches()
    }
    
    private fun listenToMatches() {
        try {
            val currentDb = getDb() ?: run {
                _isLoading.value = false
                return
            }
            matchesListener = currentDb.collection("matches").addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null || snapshot == null) return@addSnapshotListener
                try {
                    val list = snapshot.documents.mapNotNull { it.toObject(MatchData::class.java)?.copy(id = it.id) }
                    _matches.value = list
                } catch (ex: Exception) {
                    // Safe
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
        }
    }
    
    fun listenToMatchDetails(matchId: String) {
        currentMatchListener?.remove()
        val currentDb = getDb() ?: return
        currentMatchListener = currentDb.collection("matches").document(matchId).addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            _currentMatch.value = snapshot.toObject(MatchData::class.java)?.copy(id = snapshot.id)
        }
    }
    
    fun clearMatchListener() {
        currentMatchListener?.remove()
        _currentMatch.value = null
    }

    fun bookSlot(
        matchId: String,
        slotNumber: Int,
        playerNameOrTeam: String,
        inGameUid: String,
        coinsDiscountUsed: Int = 0,
        cashDiscountRupees: Int = 0,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = getAuth()?.currentUser
        if (user == null) {
            onError("Please login first!")
            return
        }
        
        val currentDb = getDb()
        if (currentDb == null) {
            onError("Database not initialized")
            return
        }
        
        val docRef = currentDb.collection("matches").document(matchId)
        val userRef = currentDb.collection("users").document(user.uid)
        
        currentDb.runTransaction { transaction ->
            // Check Match
            val snapshot = transaction.get(docRef)
            var match = snapshot.toObject(MatchData::class.java)
            if (match == null) {
                val fallback = _currentMatch.value ?: throw Exception("Match not found")
                transaction.set(docRef, fallback)
                match = fallback
            }
            
            // Check Wallet
            val userSnap = transaction.get(userRef)
            if (!userSnap.exists()) {
                throw Exception("Wallet is syncing. Try again in 2 seconds.")
            }
            val userProfile = userSnap.toObject(UserProfile::class.java) ?: throw Exception("Profile error")
            
            val isFreeOrAdMatch = match.entryType.equals("FREE", ignoreCase = true) || 
                                  match.entryType.equals("AD", ignoreCase = true) || 
                                  match.entry.equals("Free", ignoreCase = true) || 
                                  match.entry.contains("Ad", ignoreCase = true)

            val rawEntryFee = if (isFreeOrAdMatch) 0 else (match.entry.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0)
            val finalCashEntryFee = if (isFreeOrAdMatch) 0 else (rawEntryFee - cashDiscountRupees).coerceAtLeast(0)

            if (!isFreeOrAdMatch) {
                if (coinsDiscountUsed > 0 && userProfile.appMoney < coinsDiscountUsed) {
                    throw Exception("Not enough coins for discount! You have ${userProfile.appMoney} coins.")
                }
                if (userProfile.realMoney < finalCashEntryFee) {
                    throw Exception("Not enough balance! You need ₹$finalCashEntryFee but have ₹${userProfile.realMoney}")
                }
            }
            
            val currentSlots = match.bookedSlots.toMutableMap()
            val currentNames = match.slotNames.toMutableMap()
            val currentUids = match.slotUids.toMutableMap()
            val currentBadges = match.slotBadges.toMutableMap()

            // Check if user already booked a slot in this match
            val userIdentifier = user.email ?: user.uid
            val alreadyBookedSlot = currentSlots.entries.find { it.value == userIdentifier || it.value == user.uid }
            if (alreadyBookedSlot != null) {
                throw Exception("You have already booked Slot ${alreadyBookedSlot.key} in this match! One player can only book 1 slot.")
            }

            if (currentSlots.containsKey(slotNumber.toString())) {
                throw Exception("Slot $slotNumber is already booked by another player!")
            }

            val isOwner = AppSecurityGuard.isSuperOwner(userProfile.email)
            val isModerator = (userProfile.isModerator || userProfile.role.equals("moderator", ignoreCase = true)) && !isOwner
            val hasXBadge = userProfile.hasXBadge

            val userBadgeKey = when {
                isOwner -> "OWNER"
                isModerator -> "MOD"
                hasXBadge -> "X_BADGE"
                else -> "NONE"
            }
            
            val key = slotNumber.toString()
            currentSlots[key] = user.email ?: user.uid
            currentNames[key] = playerNameOrTeam.ifBlank { userProfile.name.ifBlank { "Player $slotNumber" } }
            currentUids[key] = inGameUid.ifBlank { "N/A" }
            currentBadges[key] = userBadgeKey

            transaction.update(
                docRef,
                mapOf(
                    "bookedSlots" to currentSlots,
                    "slotNames" to currentNames,
                    "slotUids" to currentUids,
                    "slotBadges" to currentBadges
                )
            )

            val updatedReal = if (isFreeOrAdMatch) userProfile.realMoney else (userProfile.realMoney - finalCashEntryFee).coerceAtLeast(0)
            val updatedCoins = if (coinsDiscountUsed > 0) (userProfile.appMoney - coinsDiscountUsed).coerceAtLeast(0) else userProfile.appMoney

            transaction.update(
                userRef,
                mapOf(
                    "realMoney" to updatedReal,
                    "appMoney" to updatedCoins,
                    "totalMatches" to (userProfile.totalMatches + 1)
                )
            )

            if (finalCashEntryFee > 0 || coinsDiscountUsed > 0) {
                val txId = UUID.randomUUID().toString()
                val noteStr = if (coinsDiscountUsed > 0) {
                    "Match Entry: ${match.title} (Slot $slotNumber) [Coupon: ₹$cashDiscountRupees OFF via $coinsDiscountUsed Coins]"
                } else {
                    "Match Entry: ${match.title} (Slot $slotNumber)"
                }
                val txRecord = mapOf(
                    "id" to txId,
                    "userId" to user.uid,
                    "userEmail" to (user.email ?: ""),
                    "type" to "ENTRY_FEE",
                    "amount" to finalCashEntryFee,
                    "status" to "SUCCESS",
                    "timestamp" to System.currentTimeMillis(),
                    "note" to noteStr
                )
                transaction.set(currentDb.collection("transactions").document(txId), txRecord)
            }
            
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onError(it.message ?: "Booking failed")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        matchesListener?.remove()
        currentMatchListener?.remove()
    }
}
