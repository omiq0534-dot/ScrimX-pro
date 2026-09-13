package com.example.utils

import android.util.Log
import com.example.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Realtime Presence & Active Players Tracker.
 * Periodically updates the current device's heartbeat timestamp in Firestore
 * and calculates the number of players active in the last 2.5 minutes.
 */
object PresenceTracker {

    private const val TAG = "PresenceTracker"
    private const val HEARTBEAT_INTERVAL_MS = 40_000L // Send heartbeat every 40s
    private const val ACTIVE_WINDOW_MS = 150_000L // 2.5 minutes window

    private val _onlinePlayersCount = MutableStateFlow(1)
    val onlinePlayersCount: StateFlow<Int> = _onlinePlayersCount.asStateFlow()

    private var heartbeatJob: Job? = null
    private var listenerRegistration: ListenerRegistration? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startTracking(userId: String) {
        if (userId.isBlank()) return

        // 1. Start periodic Heartbeat to Firestore
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            val db = FirebaseHelper.getFirestore() ?: return@launch
            while (isActive) {
                try {
                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: userId
                    val presenceRef = db.collection("app_presence").document(currentUid)
                    
                    val data = mapOf(
                        "uid" to currentUid,
                        "email" to (FirebaseAuth.getInstance().currentUser?.email ?: ""),
                        "lastSeenTimestamp" to System.currentTimeMillis(),
                        "lastSeenServer" to FieldValue.serverTimestamp()
                    )
                    
                    presenceRef.set(data)
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
                        var activeCount = 0

                        for (doc in snapshot.documents) {
                            val lastSeen = doc.getLong("lastSeenTimestamp") ?: 0L
                            if (lastSeen >= cutoffTime) {
                                activeCount++
                            }
                        }

                        // Always display at least 1 (the current user)
                        _onlinePlayersCount.value = activeCount.coerceAtLeast(1)
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
