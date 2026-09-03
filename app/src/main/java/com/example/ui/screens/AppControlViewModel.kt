package com.example.ui.screens

import androidx.lifecycle.ViewModel
import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AppControlConfig(
    val latestVersionCode: Int = 3,
    val latestVersionName: String = "1.2.1",
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
        const val CURRENT_APP_VERSION_CODE = 3
        const val CURRENT_APP_VERSION_NAME = "1.2.1"
        val ADMIN_EMAIL = AppSecurityGuard.MASTER_SUPPORT_EMAIL
    }

    private val _config = MutableStateFlow(AppControlConfig())
    val config: StateFlow<AppControlConfig> = _config

    private var listener: ListenerRegistration? = null

    init {
        listenToSystemConfig()
    }

    private fun listenToSystemConfig() {
        val db = FirebaseHelper.getFirestore() ?: return
        listener = db.collection("system_config").document("app_control")
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val rawVersionCode = doc.get("latestVersionCode") 
                        ?: doc.get("versionCode") 
                        ?: doc.get("version_code") 
                        ?: doc.get("build")
                    val parsedVersionCode = when (rawVersionCode) {
                        is Number -> rawVersionCode.toInt()
                        is String -> rawVersionCode.trim().toIntOrNull() ?: 3
                        else -> 3
                    }

                    val rawVersionName = doc.getString("latestVersionName") 
                        ?: doc.getString("versionName") 
                        ?: doc.getString("version")
                    val parsedVersionName = rawVersionName?.trim()?.ifBlank { "1.2.1" } ?: "1.2.1"

                    _config.value = AppControlConfig(
                        latestVersionCode = parsedVersionCode,
                        latestVersionName = parsedVersionName,
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
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
