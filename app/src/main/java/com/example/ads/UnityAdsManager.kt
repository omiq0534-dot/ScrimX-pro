package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.FirebaseHelper
import com.example.utils.TelemetrySyncEngine
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
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
    
    // Default configured with Owner's Game ID & Placements
    var gameId: String = "6183191"
    var testMode: Boolean = true // Test mode for safe testing (can be toggled in Admin settings)
    
    var rewardedPlacementId: String = "Rewarded_Android1"
    var interstitialPlacementId: String = "Interstitial_Android2"
    var bannerPlacementId: String = "Banner_Android3"
    
    var isInitialized = false
        private set

    var lastInitErrorMessage: String? = null
        private set

    private var isAdShowing = false
    private var isInitializing = false

    /**
     * Record real-time ad view & estimated revenue to Firestore for Telemetry Dashboard
     */
    fun recordAdTelemetryEvent(
        adType: String, // "REWARDED", "INTERSTITIAL", "BANNER"
        placementId: String,
        rewardCoins: Double = 0.0
    ) {
        val db = FirebaseHelper.getFirestore() ?: return
        val auth = FirebaseHelper.getAuth()
        val currentUser = auth?.currentUser
        val uid = currentUser?.uid ?: "anonymous"
        val userEmail = currentUser?.email ?: "guest@scrimx.app"
        val now = System.currentTimeMillis()

        // Real estimated revenue calculation per ad type:
        // Rewarded: ~₹0.85 per completion (eCPM ~$10-$12)
        // Interstitial: ~₹0.45 per view (eCPM ~$5-$6)
        // Banner: ~₹0.08 per impression (eCPM ~$1)
        val revenueInr = when (adType.uppercase()) {
            "REWARDED" -> 0.85
            "INTERSTITIAL" -> 0.45
            "BANNER" -> 0.08
            else -> 0.40
        }
        val revenueUsd = revenueInr / 86.50

        try {
            // 1. Add detailed log entry to ad_logs
            val logEntry = hashMapOf<String, Any>(
                "userId" to uid,
                "userEmail" to userEmail,
                "adType" to adType.uppercase(),
                "placementId" to placementId,
                "rewardCoins" to rewardCoins,
                "revenueInr" to revenueInr,
                "revenueUsd" to revenueUsd,
                "timestamp" to now,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("ad_logs").add(logEntry)

            // 2. Prepare atomic increment updates for ad_analytics
            val adAnalyticsUpdates = hashMapOf<String, Any>(
                "totalAdsWatched" to FieldValue.increment(1),
                "adImpressions" to FieldValue.increment(1),
                "adRevenue" to FieldValue.increment(revenueInr),
                "adRevenueInr" to FieldValue.increment(revenueInr),
                "adRevenueUsd" to FieldValue.increment(revenueUsd),
                "lastAdWatchedAt" to FieldValue.serverTimestamp(),
                "lastAdWatchedTimestamp" to now,
                "lastAdType" to adType.uppercase(),
                "lastPlacementId" to placementId,
                "lastUserEmail" to userEmail
            )

            when (adType.uppercase()) {
                "REWARDED" -> adAnalyticsUpdates["rewardedVideoCount"] = FieldValue.increment(1)
                "INTERSTITIAL" -> adAnalyticsUpdates["interstitialCount"] = FieldValue.increment(1)
                "BANNER" -> adAnalyticsUpdates["bannerCount"] = FieldValue.increment(1)
            }

            val docKeys = listOf("overview", "live", "realtime", "summary", "metrics")
            for (key in docKeys) {
                db.collection("ad_analytics").document(key).set(adAnalyticsUpdates, SetOptions.merge())

                // Also reflect in telemetry & financials immediately
                db.collection("telemetry").document(key).set(
                    mapOf(
                        "totalAdsWatched" to FieldValue.increment(1),
                        "adImpressions" to FieldValue.increment(1),
                        "adRevenue" to FieldValue.increment(revenueInr),
                        "lastUpdated" to FieldValue.serverTimestamp(),
                        "lastUpdatedTimestamp" to now
                    ),
                    SetOptions.merge()
                )

                db.collection("financials").document(key).set(
                    mapOf(
                        "adRevenue" to FieldValue.increment(revenueInr),
                        "grossRevenue" to FieldValue.increment(revenueInr),
                        "lastUpdated" to FieldValue.serverTimestamp(),
                        "lastUpdatedTimestamp" to now
                    ),
                    SetOptions.merge()
                )
            }

            // 3. Increment User's profile stats if authenticated
            if (uid.isNotBlank() && uid != "anonymous") {
                db.collection("users").document(uid).update(
                    mapOf(
                        "adsWatchedCount" to FieldValue.increment(1),
                        "totalAdRewardsEarned" to FieldValue.increment(rewardCoins),
                        "lastAdWatchTime" to now
                    )
                )
            }

            Log.d(TAG, "Ad telemetry recorded successfully: type=$adType, revenue=₹$revenueInr, user=$userEmail")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record ad telemetry: ${e.message}")
        }
    }

    fun syncFromFirestore(context: Context) {
        val db = FirebaseHelper.getFirestore() ?: return
        db.collection("settings").document("unity_ads").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val savedGameId = doc.getString("gameId") ?: "6183191"
                    val savedTestMode = doc.getBoolean("testMode") ?: true
                    val savedRewarded = doc.getString("rewardedPlacement") ?: "Rewarded_Android1"
                    val savedInterstitial = doc.getString("interstitialPlacement") ?: "Interstitial_Android2"
                    val savedBanner = doc.getString("bannerPlacement") ?: "Banner_Android3"

                    // If Firestore has old legacy IDs like rewardedVideo or video, sanitize to our new custom ones
                    val cleanRewarded = if (savedRewarded == "rewardedVideo" || savedRewarded == "Rewarded_Android") "Rewarded_Android1" else savedRewarded
                    val cleanInterstitial = if (savedInterstitial == "video" || savedInterstitial == "Interstitial_Android") "Interstitial_Android2" else savedInterstitial
                    val cleanBanner = if (savedBanner == "banner" || savedBanner == "Banner_Android") "Banner_Android3" else savedBanner

                    rewardedPlacementId = cleanRewarded
                    interstitialPlacementId = cleanInterstitial
                    bannerPlacementId = cleanBanner

                    initialize(context, customGameId = savedGameId, isTest = savedTestMode, forceReinit = true)
                } else {
                    rewardedPlacementId = "Rewarded_Android1"
                    interstitialPlacementId = "Interstitial_Android2"
                    bannerPlacementId = "Banner_Android3"
                    initialize(context, customGameId = "6183191", isTest = true, forceReinit = true)
                }
            }
            .addOnFailureListener {
                rewardedPlacementId = "Rewarded_Android1"
                interstitialPlacementId = "Interstitial_Android2"
                bannerPlacementId = "Banner_Android3"
                initialize(context, customGameId = "6183191", isTest = true, forceReinit = true)
            }
    }

    fun initialize(
        context: Context,
        customGameId: String = gameId,
        isTest: Boolean = testMode,
        forceReinit: Boolean = false,
        onComplete: (() -> Unit)? = null,
        onFailed: ((String) -> Unit)? = null
    ) {
        val cleanGameId = customGameId.trim().ifBlank { "6183190" }
        val idChanged = cleanGameId != gameId
        gameId = cleanGameId
        testMode = isTest
        
        if (!forceReinit && !idChanged && UnityAds.isInitialized) {
            isInitialized = true
            isInitializing = false
            lastInitErrorMessage = null
            Log.d(TAG, "Unity Ads already initialized for Game ID: $gameId")
            loadRewardedAd()
            loadInterstitialAd()
            onComplete?.invoke()
            return
        }

        isInitializing = true
        try {
            UnityAds.initialize(context.applicationContext, gameId, testMode, object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    isInitialized = true
                    isInitializing = false
                    lastInitErrorMessage = null
                    Log.d(TAG, "Unity Ads Initialized Successfully with Game ID: $gameId (TestMode: $testMode)")
                    loadRewardedAd()
                    loadInterstitialAd()
                    onComplete?.invoke()
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                    isInitialized = false
                    isInitializing = false
                    val errStr = message ?: error?.name ?: "Unity Ads Initialization Error"
                    lastInitErrorMessage = errStr
                    Log.w(TAG, "Unity Ads Initialization Notice: $error - $message")
                    onFailed?.invoke(errStr)
                }
            })
        } catch (e: Exception) {
            isInitialized = false
            isInitializing = false
            lastInitErrorMessage = e.message
            Log.w(TAG, "Unity Ads init caught exception: ${e.message}")
            onFailed?.invoke(e.message ?: "Exception initializing Unity Ads")
        }
    }

    fun loadRewardedAd(placementId: String = rewardedPlacementId, onLoaded: (() -> Unit)? = null, onFailed: ((String) -> Unit)? = null) {
        if (!UnityAds.isInitialized) {
            onFailed?.invoke("Unity Ads not initialized")
            return
        }
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
        if (!UnityAds.isInitialized) {
            onFailed?.invoke("Unity Ads not initialized")
            return
        }
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

        val showListener = object : IUnityAdsShowListener {
            override fun onUnityAdsShowFailure(placementId: String, error: UnityAds.UnityAdsShowError, message: String) {
                isAdShowing = false
                Log.e(TAG, "Unity Ads Show Failure: $error - $message")
                onAdFailed(message ?: "Ad Show Failed")
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
                    recordAdTelemetryEvent("REWARDED", placementId, rewardCoins = 15.0)
                    onRewardEarned()
                } else {
                    onAdSkipped()
                }
                onAdClosed()
                // Preload next ad
                loadRewardedAd(placementId)
            }
        }

        // Auto-initialize if not ready
        if (!UnityAds.isInitialized) {
            isAdShowing = true
            UnityAds.initialize(activity.applicationContext, gameId, testMode, object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    isInitialized = true
                    // Load and show
                    UnityAds.load(placementId, object : IUnityAdsLoadListener {
                        override fun onUnityAdsAdLoaded(pId: String) {
                            UnityAds.show(activity, placementId, UnityAdsShowOptions(), showListener)
                        }
                        override fun onUnityAdsFailedToLoad(pId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                            isAdShowing = false
                            onAdFailed("Failed to load ad: $message")
                        }
                    })
                }
                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                    isAdShowing = false
                    onAdFailed("Ads initialization error: $message. Check internet connection.")
                }
            })
            return
        }

        // If initialized, load and show
        isAdShowing = true
        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(pId: String) {
                UnityAds.show(activity, placementId, UnityAdsShowOptions(), showListener)
            }
            override fun onUnityAdsFailedToLoad(pId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                isAdShowing = false
                val detailedErr = if (message.isNotBlank()) message else "Unity Ads Load Error: $error"
                Log.e(TAG, "Rewarded Ad Load Failed for placement '$placementId': $detailedErr")
                onAdFailed("$detailedErr (Placement: '$placementId')")
            }
        })
    }

    fun showInterstitialAd(
        activity: Activity,
        placementId: String = interstitialPlacementId,
        onAdClosed: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        if (isAdShowing) return
        if (!UnityAds.isInitialized) {
            initialize(activity.applicationContext, gameId, testMode)
            return
        }

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
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    recordAdTelemetryEvent("INTERSTITIAL", placementId)
                }
                onAdClosed()
                loadInterstitialAd(placementId)
            }
        }

        UnityAds.load(placementId, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(pId: String) {
                UnityAds.show(activity, placementId, UnityAdsShowOptions(), showListener)
            }
            override fun onUnityAdsFailedToLoad(pId: String, error: UnityAds.UnityAdsLoadError, message: String) {
                isAdShowing = false
                onAdFailed(message)
            }
        })
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
    var isLoaded by remember { mutableStateOf(false) }
    var bannerView by remember { mutableStateOf<BannerView?>(null) }

    DisposableEffect(placementId) {
        onDispose {
            bannerView?.destroy()
            bannerView = null
        }
    }

    Box(
        modifier = if (isLoaded) modifier.fillMaxWidth().height(52.dp) else Modifier.size(0.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = if (isLoaded) Modifier.fillMaxWidth().height(50.dp) else Modifier.size(0.dp),
            factory = { context ->
                val activity = context as? Activity
                if (activity != null && UnityAds.isInitialized) {
                    val view = BannerView(activity, placementId, UnityBannerSize(320, 50))
                    view.listener = object : BannerView.IListener {
                        override fun onBannerLoaded(bannerAdView: BannerView?) {
                            Log.d("UnityAds", "Banner loaded successfully")
                            isLoaded = true
                        }

                        override fun onBannerShown(bannerAdView: BannerView?) {
                            Log.d("UnityAds", "Banner shown")
                            UnityAdsManager.recordAdTelemetryEvent("BANNER", placementId)
                        }

                        override fun onBannerClick(bannerAdView: BannerView?) {
                            Log.d("UnityAds", "Banner clicked")
                        }

                        override fun onBannerFailedToLoad(bannerAdView: BannerView?, errorInfo: BannerErrorInfo?) {
                            Log.w("UnityAds", "Banner notice: ${errorInfo?.errorMessage}")
                            isLoaded = false
                        }

                        override fun onBannerLeftApplication(bannerAdView: BannerView?) {
                            Log.d("UnityAds", "Banner left application")
                        }
                    }
                    try {
                        view.load()
                    } catch (e: Exception) {
                        Log.w("UnityAds", "Banner load exception: ${e.message}")
                        isLoaded = false
                    }
                    bannerView = view
                    view
                } else {
                    android.view.View(context)
                }
            }
        )
    }
}
