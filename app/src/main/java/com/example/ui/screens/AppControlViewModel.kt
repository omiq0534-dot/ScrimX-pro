package com.example.ui.screens

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import com.example.BuildConfig
import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AppControlConfig(
    val latestVersionCode: Int = BuildConfig.VERSION_CODE,
    val latestVersionName: String = BuildConfig.VERSION_NAME,
    val apkDownloadUrl: String = "https://website-scrim-x-pro.vercel.app/",
    val whatsNew: String = "• Regular performance updates\n• Fast tournament rooms",
    val isForceUpdate: Boolean = false,
    val allowLaterButton: Boolean = false,
    val autoBanOutdatedUsers: Boolean = false,
    val isMaintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Server is currently undergoing scheduled maintenance. We'll be back shortly!",
    val isAnnouncementActive: Boolean = true,
    val announcementNotice: String = "🔥 Register for daily Free Fire & BGMI tournaments!",
    val spinJackpot: Int = 200,
    val dailyFreeSpins: Int = 2,
    val watchVideoCoins: Int = 10,
    val dailyRewardCoins: Int = 15,
    val adminUpiId: String = "admin@upi",
    val supportWhatsapp: String = "+919876543210",
    val supportTelegram: String = "https://t.me/tournament_support"
)

class AppControlViewModel : ViewModel() {
    companion object {
        const val CURRENT_APP_VERSION_CODE = BuildConfig.VERSION_CODE
        const val CURRENT_APP_VERSION_NAME = BuildConfig.VERSION_NAME
        val ADMIN_EMAIL = AppSecurityGuard.MASTER_SUPPORT_EMAIL

        fun getInstalledVersionCode(context: Context): Int {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0).versionCode
                }
            } catch (e: Exception) {
                BuildConfig.VERSION_CODE
            }
        }

        fun getInstalledVersionName(context: Context): String {
            return try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: BuildConfig.VERSION_NAME
            } catch (e: Exception) {
                BuildConfig.VERSION_NAME
            }
        }
    }

    private val _config = MutableStateFlow(AppControlConfig())
    val config: StateFlow<AppControlConfig> = _config

    private var listener: ListenerRegistration? = null
    private var fallbackListener: ListenerRegistration? = null

    init {
        listenToSystemConfig()
    }

    private fun parseConfigDoc(doc: DocumentSnapshot) {
        val rawVersionCode = doc.get("latestVersionCode") 
            ?: doc.get("versionCode") 
            ?: doc.get("version_code") 
            ?: doc.get("build")
        val parsedVersionCode = when (rawVersionCode) {
            is Number -> rawVersionCode.toInt()
            is String -> rawVersionCode.trim().toIntOrNull() ?: BuildConfig.VERSION_CODE
            else -> BuildConfig.VERSION_CODE
        }

        val rawVersionName = doc.getString("latestVersionName") 
            ?: doc.getString("versionName") 
            ?: doc.getString("version")
        val parsedVersionName = rawVersionName?.trim()?.ifBlank { BuildConfig.VERSION_NAME } ?: BuildConfig.VERSION_NAME

        // Clamp to current compiled release if server code was accidentally set to a non-existent build
        val safeVersionCode = if (parsedVersionCode > BuildConfig.VERSION_CODE) {
            // Auto-heal Firestore in background so ghost build (like Build 4) is reset to current Build 3
            try {
                val db = FirebaseHelper.getFirestore()
                if (db != null) {
                    val fixMap = mapOf(
                        "latestVersionCode" to BuildConfig.VERSION_CODE,
                        "latestVersionName" to BuildConfig.VERSION_NAME
                    )
                    db.collection("system_config").document("app_control").update(fixMap)
                    db.collection("settings").document("app_control").update(fixMap)
                }
            } catch (e: Exception) {}
            BuildConfig.VERSION_CODE
        } else {
            parsedVersionCode
        }

        val safeVersionName = if (parsedVersionCode > BuildConfig.VERSION_CODE) {
            BuildConfig.VERSION_NAME
        } else {
            parsedVersionName
        }

        _config.value = AppControlConfig(
            latestVersionCode = safeVersionCode,
            latestVersionName = safeVersionName,
            apkDownloadUrl = doc.getString("apkDownloadUrl") ?: "",
            whatsNew = doc.getString("whatsNew") ?: "• Bug fixes & improvements",
            isForceUpdate = doc.getBoolean("isForceUpdate") ?: false,
            allowLaterButton = doc.getBoolean("allowLaterButton") ?: false,
            autoBanOutdatedUsers = doc.getBoolean("autoBanOutdatedUsers") ?: false,
            isMaintenanceMode = doc.getBoolean("isMaintenanceMode") ?: false,
            maintenanceMessage = doc.getString("maintenanceMessage") ?: "Server Maintenance in progress",
            isAnnouncementActive = doc.getBoolean("isAnnouncementActive") ?: true,
            announcementNotice = doc.getString("announcementNotice") ?: "",
            spinJackpot = doc.getLong("spinJackpot")?.toInt() ?: 200,
            dailyFreeSpins = doc.getLong("dailyFreeSpins")?.toInt() ?: 2,
            watchVideoCoins = doc.getLong("watchVideoCoins")?.toInt() ?: 10,
            dailyRewardCoins = doc.getLong("dailyRewardCoins")?.toInt() ?: 15,
            adminUpiId = doc.getString("adminUpiId") ?: "admin@upi",
            supportWhatsapp = doc.getString("supportWhatsapp") ?: "+919876543210",
            supportTelegram = doc.getString("supportTelegram") ?: "https://t.me/tournament_support"
        )
    }

    private fun listenToSystemConfig() {
        val db = FirebaseHelper.getFirestore() ?: return
        listener = db.collection("system_config").document("app_control")
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    parseConfigDoc(doc)
                } else {
                    // Fallback to settings/app_control
                    if (fallbackListener == null) {
                        fallbackListener = db.collection("settings").document("app_control")
                            .addSnapshotListener { fDoc, _ ->
                                if (fDoc != null && fDoc.exists()) {
                                    parseConfigDoc(fDoc)
                                }
                            }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
        fallbackListener?.remove()
    }
}
