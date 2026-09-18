package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Realtime Presence & Active Players Tracker.
 * Periodically sends a heartbeat timestamp to Firestore
 * and calculates the count of players active in the last 5 minutes.
 */
object PresenceTracker {

    private const val TAG = "PresenceTracker"
    private const val HEARTBEAT_INTERVAL_MS = 30_000L // Send heartbeat every 30s
    private const val ACTIVE_WINDOW_MS = 300_000L // 5 minutes active window

    private val _onlinePlayersCount = MutableStateFlow(1)
    val onlinePlayersCount: StateFlow<Int> = _onlinePlayersCount.asStateFlow()

    private var heartbeatJob: Job? = null
    private var listenerRegistration: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var deviceInstallationId: String = ""

    fun init(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences("presence_prefs", Context.MODE_PRIVATE)
        var savedId = prefs.getString("device_presence_id", null)
        if (savedId.isNullOrBlank()) {
            savedId = "dev_" + UUID.randomUUID().toString().take(12)
            prefs.edit().putString("device_presence_id", savedId).apply()
        }
        deviceInstallationId = savedId
        startTracking()
    }

    fun startTracking(userId: String = "") {
        // 1. Start periodic Heartbeat to Firestore
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            val db = FirebaseHelper.getFirestore() ?: return@launch
            while (isActive) {
                try {
                    val authUser = FirebaseAuth.getInstance().currentUser
                    val currentUid = when {
                        authUser?.uid?.isNotBlank() == true -> authUser.uid
                        userId.isNotBlank() -> userId
                        deviceInstallationId.isNotBlank() -> deviceInstallationId
                        else -> "guest_" + System.currentTimeMillis()
                    }
                    val email = authUser?.email ?: ""
                    val presenceRef = db.collection("app_presence").document(currentUid)
                    
                    val data = mapOf(
                        "uid" to currentUid,
                        "email" to email,
                        "deviceId" to deviceInstallationId,
                        "lastSeenTimestamp" to System.currentTimeMillis(),
                        "lastSeenServer" to FieldValue.serverTimestamp()
                    )
                    
                    presenceRef.set(data, SetOptions.merge())

                    // Also update user's lastActive in users collection if authenticated
                    if (authUser?.uid?.isNotBlank() == true) {
                        db.collection("users").document(authUser.uid).update(
                            mapOf(
                                "lastSeenTimestamp" to System.currentTimeMillis(),
                                "isOnline" to true
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update heartbeat: ${e.message}")
                }
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }

        // 2. Listen to active players count from Firestore app_presence collection
        if (listenerRegistration == null) {
            val db = FirebaseHelper.getFirestore()
            listenerRegistration = db?.collection("app_presence")
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Presence listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val cutoffTime = System.currentTimeMillis() - ACTIVE_WINDOW_MS
                        val activeUids = mutableSetOf<String>()

                        for (doc in snapshot.documents) {
                            val lastSeen = doc.getLong("lastSeenTimestamp") ?: 0L
                            if (lastSeen >= cutoffTime) {
                                activeUids.add(doc.id)
                            }
                        }

                        // Always display at least 1 (the current device/user)
                        val totalActive = activeUids.size.coerceAtLeast(1)
                        _onlinePlayersCount.value = totalActive
                        Log.d(TAG, "Online active players count: $totalActive")
                    }
                }
        }
    }

    fun stopTracking() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}

