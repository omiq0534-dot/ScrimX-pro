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

data class MatchData(
    val id: String = "",
    val title: String = "",
    val time: String = "",
    val prize: String = "",
    val entry: String = "",
    val badge: String = "",
    val status: String = "Upcoming",
    val roomId: String = "",
    val roomPass: String = "",
    val liveUrl: String = "",
    val map: String = "Bermuda",
    val mode: String = "Squad", // "Solo", "Duo", "Squad"
    val totalSlots: Int = 12,   // 48 for Solo, 24 for Duo, 12 for Squad
    val bookedSlots: Map<String, String> = emptyMap(), // slotNumber -> user UID
    val slotNames: Map<String, String> = emptyMap(),   // slotNumber -> Player IGN / Team Name
    val slotUids: Map<String, String> = emptyMap()     // slotNumber -> In-game numeric UID
)

class MatchesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _matches = MutableStateFlow<List<MatchData>>(emptyList())
    val matches: StateFlow<List<MatchData>> = _matches
    
    private val _currentMatch = MutableStateFlow<MatchData?>(null)
    val currentMatch: StateFlow<MatchData?> = _currentMatch
    
    private var matchesListener: ListenerRegistration? = null
    private var currentMatchListener: ListenerRegistration? = null
    
    init {
        listenToMatches()
    }
    
    private fun listenToMatches() {
        try {
            matchesListener = db.collection("matches").addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                try {
                    val list = snapshot.documents.mapNotNull { it.toObject(MatchData::class.java)?.copy(id = it.id) }
                    _matches.value = list
                } catch (ex: Exception) {
                    // Safe
                }
            }
        } catch (e: Exception) {
            // Safe
        }
    }
    
    fun listenToMatchDetails(matchId: String) {
        currentMatchListener?.remove()
        currentMatchListener = db.collection("matches").document(matchId).addSnapshotListener { snapshot, e ->
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
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError("Please login first!")
            return
        }
        
        val docRef = db.collection("matches").document(matchId)
        val userRef = db.collection("users").document(user.uid)
        
        db.runTransaction { transaction ->
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
            
            val entryFee = match.entry.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
            if (userProfile.realMoney < entryFee) {
                throw Exception("Not enough balance! You need ₹$entryFee but have ₹${userProfile.realMoney}")
            }
            
            val currentSlots = match.bookedSlots.toMutableMap()
            val currentNames = match.slotNames.toMutableMap()
            val currentUids = match.slotUids.toMutableMap()

            if (currentSlots.containsKey(slotNumber.toString())) {
                throw Exception("Slot already booked!")
            }
            
            val key = slotNumber.toString()
            currentSlots[key] = user.email ?: user.uid
            currentNames[key] = playerNameOrTeam.ifBlank { userProfile.name.ifBlank { "Player $slotNumber" } }
            currentUids[key] = inGameUid.ifBlank { "N/A" }

            transaction.update(
                docRef,
                mapOf(
                    "bookedSlots" to currentSlots,
                    "slotNames" to currentNames,
                    "slotUids" to currentUids
                )
            )
            transaction.update(userRef, "realMoney", userProfile.realMoney - entryFee)
            
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
