package com.example

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {
    private const val API_KEY = "AIzaSyC8jvoyMB4lhG4Pjq2jO1GXDAtI9mlgFL0"
    private const val APP_ID = "1:404122407805:android:5d930cdd110810c6bb7214"
    private const val PROJECT_ID = "scrimx-a1b09"
    private const val DB_URL = "https://scrimx-a1b09-default-rtdb.firebaseio.com"
    private const val STORAGE_BUCKET = "scrimx-a1b09.firebasestorage.app"

    fun init(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(API_KEY)
                    .setApplicationId(APP_ID)
                    .setProjectId(PROJECT_ID)
                    .setDatabaseUrl(DB_URL)
                    .setStorageBucket(STORAGE_BUCKET)
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d("FirebaseHelper", "FirebaseApp initialized with explicit options successfully")
            } else {
                Log.d("FirebaseHelper", "FirebaseApp already initialized by provider")
            }
        } catch (e: Exception) {
            Log.e("FirebaseHelper", "Error initializing FirebaseApp", e)
            try {
                FirebaseApp.initializeApp(context.applicationContext)
            } catch (ex: Exception) {
                Log.e("FirebaseHelper", "Fallback initializeApp failed", ex)
            }
        }
    }

    fun getAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            try {
                val app = FirebaseApp.getInstance()
                FirebaseAuth.getInstance(app)
            } catch (ex: Exception) {
                Log.e("FirebaseHelper", "Failed to get FirebaseAuth", ex)
                null
            }
        }
    }

    fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            try {
                val app = FirebaseApp.getInstance()
                FirebaseFirestore.getInstance(app)
            } catch (ex: Exception) {
                Log.e("FirebaseHelper", "Failed to get FirebaseFirestore", ex)
                null
            }
        }
    }
}
