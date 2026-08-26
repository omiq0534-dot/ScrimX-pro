package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class TournamentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d("TournamentApp", "FirebaseApp initialized successfully")
        } catch (e: Exception) {
            Log.e("TournamentApp", "Failed to initialize FirebaseApp", e)
        }
    }
}
