package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

class TournamentApp : Application() {

    companion object {
        private var instance: TournamentApp? = null
        fun getAppContext(): Context? = instance?.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseHelper.init(this)
        com.example.ads.UnityAdsManager.syncFromFirestore(this)
    }
}
