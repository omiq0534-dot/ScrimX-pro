package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class TournamentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseHelper.init(this)
    }
}
