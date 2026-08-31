package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.FirebaseHelper
import com.google.firebase.firestore.FieldValue
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

object UnityAdsManager {
    private const val TAG = "UnityAdsManager"
    
    // Default configured with Owner's Game ID
    var gameId: String = "6183190"
    var testMode: Boolean = true // Test mode for safe testing (can be toggled in Admin settings)
    
    var rewardedPlacementId: String = "Rewarded_Android"
    var interstitialPlacementId: String = "Interstitial_Android"
    var bannerPlacementId: String = "Banner_Android"
    
    var isInitialized = false
        private set

    private var isAdShowing = false

    fun initialize(context: Context, customGameId: String = "6183190", isTest: Boolean = true) {
        gameId = customGameId.ifBlank { "6183190" }
        testMode = isTest
        
        if (UnityAds.isInitialized) {
            isInitialized = true
            Log.d(TAG, "Unity Ads already initialized")
            return
        }

        UnityAds.initialize(context.applicationContext, gameId, testMode, object : IUnityAdsInitializationListener {
            override fun onInitializationComplete() {
                isInitialized = true
                Log.d(TAG, "✅ Unity Ads Initialized Successfully with Game ID: $gameId (TestMode: $testMode)")
                loadRewardedAd()
                loadInterstitialAd()
            }

            override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                isInitialized = false
                Log.e(TAG, "❌ Unity Ads Initialization Failed: $error - $message")
            }
        })
    }

    fun loadRewardedAd(placementId: String = rewardedPlacementId, onLoaded: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Rewarded Ad Loaded: $placementId")
                onLoaded?.invoke()
            }

            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                Log.e(TAG, "Rewarded Ad Failed to Load: $placementId, Error: $error - $message")
                onFailed?.invoke(message)
            }
        })
    }

    fun loadInterstitialAd(placementId: String = interstitialPlacementId, onLoaded: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.d(TAG, "Interstitial Ad Loaded: $placementId")
                onLoaded?.invoke()
            }

            override fun onUnityAdsFailedToLoad(placementId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                Log.e(TAG, "Interstitial Ad Failed to Load: $placementId, Error: $error - $message")
                onFailed?.invoke(message)
            }
        })
    }

    fun showRewardedAd(
        activity: Activity,
        placementId: String = rewardedPlacementId,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit = {},
        onAdSkipped: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        if (isAdShowing) {
            Log.d(TAG, "Ad already presenting, ignoring repeated tap")
            return
        }
        isAdShowing = true

        val showListener = object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                isAdShowing = false
                Log.e(TAG, "Unity Ads Show Failure: $error - $message")
                onAdFailed(message)
            }

            override fun onUnityAdsShowStart(placementId: String) {
                Log.d(TAG, "Unity Ads Show Start: $placementId")
            }

            override fun onUnityAdsShowClick(placementId: String) {
                Log.d(TAG, "Unity Ads Clicked: $placementId")
            }

            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                isAdShowing = false
                Log.d(TAG, "Unity Ads Show Complete: $placementId, State: $state")
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    onRewardEarned()
                } else {
                    onAdSkipped()
                }
                onAdClosed()
                // Preload next ad
                loadRewardedAd(placementId)
            }
        }

        UnityAds.show(activity, placementId, UnityAdsShowOptions(), showListener)
    }

    fun showInterstitialAd(
        activity: Activity,
        placementId: String = interstitialPlacementId,
        onAdClosed: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        if (isAdShowing) return
        isAdShowing = true

        val showListener = object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                isAdShowing = false
                Log.e(TAG, "Interstitial Show Failure: $error - $message")
                onAdFailed(message)
            }

            override fun onUnityAdsShowStart(placementId: String) {
                Log.d(TAG, "Interstitial Show Start: $placementId")
            }

            override fun onUnityAdsShowClick(placementId: String) {
                Log.d(TAG, "Interstitial Clicked: $placementId")
            }

            override fun onUnityAdsShowComplete(placementId: String, state: UnityAds.UnityAdsShowCompletionState) {
                isAdShowing = false
                onAdClosed()
                loadInterstitialAd(placementId)
            }
        }

        UnityAds.show(activity, placementId, UnityAdsShowOptions(), showListener)
    }

    /**
     * Credit reward coin/money to user's wallet in Firestore
     */
    fun creditAdRewardToUser(
        uid: String,
        rewardCoins: Double = 2.0,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val db = FirebaseHelper.getFirestore()
        if (db == null || uid.isBlank()) {
            onFailure("Database or User unavailable")
            return
        }

        val userRef = db.collection("users").document(uid)
        userRef.update(
            mapOf(
                "walletBalance" to FieldValue.increment(rewardCoins),
                "appMoney" to FieldValue.increment(rewardCoins)
            )
        ).addOnSuccessListener {
            // Also log transaction
            val txData = hashMapOf(
                "userId" to uid,
                "amount" to rewardCoins,
                "type" to "AD_REWARD",
                "status" to "Approved",
                "note" to "Watched Unity Video Ad Reward (+₹$rewardCoins)",
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("transactions").add(txData)
            onSuccess()
        }.addOnFailureListener { e ->
            onFailure(e.message ?: "Failed to update wallet")
        }
    }
}

/**
 * Composable Banner Ad component for Jetpack Compose
 */
@Composable
fun UnityBannerAd(
    modifier: Modifier = Modifier,
    placementId: String = UnityAdsManager.bannerPlacementId
) {
    var bannerView by remember { mutableStateOf<BannerView?>(null) }

    DisposableEffect(placementId) {
        onDispose {
            bannerView?.destroy()
            bannerView = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFF14161F)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            factory = { context ->
                val activity = context as? Activity
                if (activity != null) {
                    val view = BannerView(activity, placementId, UnityBannerSize(320, 50))
                    view.listener = object : BannerView.IListener {
                        override fun onBannerLoaded(bannerAdView: BannerView?) {
                            Log.d("UnityAds", "Banner loaded successfully")
                        }

                        override fun onBannerShown(bannerAdView: BannerView?) {
                            Log.d("UnityAds", "Banner shown")
                        }

                        override fun onBannerClick(bannerAdView: BannerView?) {
                            Log.d("UnityAds", "Banner clicked")
                        }

                        override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: BannerErrorInfo?) {
                            Log.e("UnityAds", "Banner failed: ${errorInfo?.errorMessage}")
                        }

                        override fun onBannerLeftApplication(bannerAdView: BannerView?) {
                            Log.d("UnityAds", "Banner left application")
                        }
                    }
                    view.load()
                    bannerView = view
                    view
                } else {
                    android.view.View(context)
                }
            }
        )
    }
}
