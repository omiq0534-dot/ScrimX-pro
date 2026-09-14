package com.example.ui.screens

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val realMoney: Int = 5, // Joining cash bonus (admin configurable)
    val appMoney: Int = 50, // Joining coins bonus (admin configurable)
    val referralCode: String = "",
    val referredBy: String = "",
    val referralCount: Int = 0,
    val referralEarnings: Int = 0,
    val isBanned: Boolean = false,
    val banType: String = "none", // "none", "temporary", "permanent"
    val banReason: String = "",
    val banUntil: Long = 0L,
    val totalMatches: Int = 0,
    val totalWins: Int = 0,
    val totalKills: Int = 0,
    val hasXBadge: Boolean = false, // 👑 ScrimX Verified [X] Badge
    val role: String = "player", // "owner", "moderator", "player"
    val isModerator: Boolean = false
)

class UserViewModel : ViewModel() {
    private fun getDb(): FirebaseFirestore? = FirebaseHelper.getFirestore()
    private fun getAuth(): FirebaseAuth? = FirebaseHelper.getAuth()
    
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile
    
    private var listener: ListenerRegistration? = null
    
    init {
        listenToUser()
    }
    
    private fun listenToUser() {
        try {
            val currentAuth = getAuth() ?: return
            val currentDb = getDb() ?: return
            val currentUser = currentAuth.currentUser ?: return
            val docRef = currentDb.collection("users").document(currentUser.uid)
            
            listener = docRef.addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                try {
                    if (snapshot != null && snapshot.exists()) {
                        var p = snapshot.toObject(UserProfile::class.java)?.copy(uid = snapshot.id)
                        if (p != null) {
                            var needsUpdate = false
                            val updates = mutableMapOf<String, Any>()

                            if (p.name.isBlank() || p.name.equals("New Player", ignoreCase = true)) {
                                val derivedName = when {
                                    !currentUser.displayName.isNullOrBlank() -> currentUser.displayName!!
                                    !currentUser.email.isNullOrBlank() -> currentUser.email!!.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    else -> "Player"
                                }
                                p = p.copy(name = derivedName)
                                updates["name"] = derivedName
                                needsUpdate = true
                            }

                            if (p.referralCode.isBlank()) {
                                val genCode = "REF" + (currentUser.uid.take(4) + (1000..9999).random().toString()).uppercase()
                                p = p.copy(referralCode = genCode)
                                updates["referralCode"] = genCode
                                needsUpdate = true
                            }

                            // 👑 Automatic Pro Milestone: 10+ Tournament Wins or Verified Owner unlocks [X] Badge
                            if (!p.hasXBadge && (p.totalWins >= 10 || AppSecurityGuard.isSuperOwner(p.email))) {
                                p = p.copy(hasXBadge = true)
                                updates["hasXBadge"] = true
                                needsUpdate = true
                            }

                            if (needsUpdate) {
                                docRef.update(updates)
                            }
                            _profile.value = p
                            com.example.TournamentApp.getAppContext()?.let { ctx ->
                                val prefs = ctx.getSharedPreferences("scrimx_widget_prefs", android.content.Context.MODE_PRIVATE)
                                prefs.edit()
                                    .putInt("widget_real_money", p.realMoney)
                                    .putInt("widget_app_coins", p.appMoney)
                                    .putString("widget_player_name", p.name.ifBlank { "Gamer" })
                                    .putInt("widget_total_wins", p.totalWins)
                                    .apply()
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    try {
                                        com.example.widget.ScrimXGlanceWidgetReceiver.updateWidget(ctx)
                                    } catch (_: Exception) {}
                                }
                            }
                        }
                    } else {
                        val derivedName = when {
                            !currentUser.displayName.isNullOrBlank() -> currentUser.displayName!!
                            !currentUser.email.isNullOrBlank() -> currentUser.email!!.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            else -> "Player"
                        }
                        val genCode = "REF" + (currentUser.uid.take(4) + (1000..9999).random().toString()).uppercase()

                        currentDb.collection("system_config").document("app_control").get()
                            .addOnSuccessListener { cfgDoc ->
                                val bonusCash = cfgDoc?.getLong("newUserRealMoneyBonus")?.toInt() ?: 5
                                val bonusCoins = cfgDoc?.getLong("newUserAppMoneyBonus")?.toInt() ?: 50
                                val newUser = UserProfile(
                                    uid = currentUser.uid,
                                    email = currentUser.email ?: "",
                                    name = derivedName,
                                    realMoney = bonusCash,
                                    appMoney = bonusCoins,
                                    referralCode = genCode,
                                    referredBy = "",
                                    referralCount = 0,
                                    referralEarnings = 0
                                )
                                docRef.set(newUser)
                            }
                            .addOnFailureListener {
                                val newUser = UserProfile(
                                    uid = currentUser.uid,
                                    email = currentUser.email ?: "",
                                    name = derivedName,
                                    realMoney = 5,
                                    appMoney = 50,
                                    referralCode = genCode,
                                    referredBy = "",
                                    referralCount = 0,
                                    referralEarnings = 0
                                )
                                docRef.set(newUser)
                            }
                    }
                } catch (ex: Exception) {
                    // Safe catch
                }
            }
        } catch (e: Exception) {
            // Firebase safety catch
        }
    }
    
    fun updateName(newName: String) {
        val currentUser = getAuth()?.currentUser ?: return
        getDb()?.collection("users")?.document(currentUser.uid)?.update("name", newName)
    }

    fun addAppMoney(amount: Int) {
        val currentUser = getAuth()?.currentUser ?: return
        getDb()?.collection("users")?.document(currentUser.uid)?.update("appMoney", FieldValue.increment(amount.toLong()))
    }

    fun addRealMoney(amount: Int) {
        val currentUser = getAuth()?.currentUser ?: return
        getDb()?.collection("users")?.document(currentUser.uid)?.update("realMoney", FieldValue.increment(amount.toLong()))
    }

    fun claimReferralCode(enteredCode: String, bonusCoins: Int = 300, onResult: (Boolean, String) -> Unit) {
        val currentUser = getAuth()?.currentUser ?: run {
            onResult(false, "Please log in first")
            return
        }
        val db = getDb() ?: run {
            onResult(false, "Database connection unavailable")
            return
        }
        val cleanCode = enteredCode.trim().uppercase()
        if (cleanCode.isBlank()) {
            onResult(false, "Please enter a valid referral code")
            return
        }

        val myProfile = _profile.value
        if (myProfile != null) {
            if (myProfile.referredBy.isNotBlank()) {
                onResult(false, "You have already claimed a referral bonus!")
                return
            }
            if (myProfile.referralCode.equals(cleanCode, ignoreCase = true)) {
                onResult(false, "You cannot claim your own referral code!")
                return
            }
        }

        // Find user with this referral code
        db.collection("users")
            .whereEqualTo("referralCode", cleanCode)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onResult(false, "Invalid referral code! Please check and try again.")
                } else {
                    val referrerDoc = querySnapshot.documents[0]
                    val referrerUid = referrerDoc.id

                    if (referrerUid == currentUser.uid) {
                        onResult(false, "You cannot use your own referral code!")
                        return@addOnSuccessListener
                    }

                    // Reward Current User (Claimer)
                    val myDocRef = db.collection("users").document(currentUser.uid)
                    myDocRef.update(
                        mapOf(
                            "referredBy" to cleanCode,
                            "appMoney" to FieldValue.increment(bonusCoins.toLong())
                        )
                    ).addOnSuccessListener {
                        // Reward the Referrer (Inviter)
                        val referrerDocRef = db.collection("users").document(referrerUid)
                        referrerDocRef.update(
                            mapOf(
                                "appMoney" to FieldValue.increment(bonusCoins.toLong()),
                                "referralCount" to FieldValue.increment(1L),
                                "referralEarnings" to FieldValue.increment(bonusCoins.toLong())
                            )
                        )

                        onResult(true, "🎉 Success! +$bonusCoins Bonus Coins added to your wallet!")
                    }.addOnFailureListener {
                        onResult(false, "Failed to claim reward: ${it.localizedMessage}")
                    }
                }
            }
            .addOnFailureListener {
                onResult(false, "Error verifying referral code: ${it.localizedMessage}")
            }
    }
    
    fun logout() {
        getAuth()?.signOut()
        _profile.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
