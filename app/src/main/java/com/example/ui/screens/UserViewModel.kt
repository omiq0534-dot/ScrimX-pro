package com.example.ui.screens

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import com.example.FirebaseHelper

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val realMoney: Int = 100, // Free joining bonus for testing
    val appMoney: Int = 0
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
                        val p = snapshot.toObject(UserProfile::class.java)?.copy(uid = snapshot.id)
                        if (p != null) {
                            if (p.name.isBlank() || p.name.equals("New Player", ignoreCase = true)) {
                                val derivedName = when {
                                    !currentUser.displayName.isNullOrBlank() -> currentUser.displayName!!
                                    !currentUser.email.isNullOrBlank() -> currentUser.email!!.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    else -> "Player"
                                }
                                _profile.value = p.copy(name = derivedName)
                                docRef.update("name", derivedName)
                            } else {
                                _profile.value = p
                            }
                        }
                    } else {
                        val derivedName = when {
                            !currentUser.displayName.isNullOrBlank() -> currentUser.displayName!!
                            !currentUser.email.isNullOrBlank() -> currentUser.email!!.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            else -> "Player"
                        }

                        val newUser = UserProfile(
                            uid = currentUser.uid,
                            email = currentUser.email ?: "",
                            name = derivedName,
                            realMoney = 100,
                            appMoney = 0
                        )
                        docRef.set(newUser)
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
    
    fun logout() {
        getAuth()?.signOut()
        _profile.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
