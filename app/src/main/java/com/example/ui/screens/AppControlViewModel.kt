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
    val newUserRealMoneyBonus: Int = 5,
    val newUserAppMoneyBonus: Int = 50,
    val adminUpiId: String = "admin@upi",
    val supportWhatsapp: String = "+919876543210",
    val supportTelegram: String = "https://t.me/tournament_support",
    val patchTag: String = "v1.4.0-LIVE",
    val patchNotes: String = "All server systems operational.",
    val isLivePatchActive: Boolean = true,
    val isRegistrationEnabled: Boolean = true,
    val isWithdrawalsEnabled: Boolean = true,
    val isSpinWheelEnabled: Boolean = true,
    val isWatchAdsEnabled: Boolean = true,
    val isYt100GoalUnlocked: Boolean = false,
    val isCustomTaskEnabled: Boolean = false,
    val customTaskTitle: String = "Follow Official Instagram",
    val customTaskReward: Int = 100,
    val customTaskUrl: String = "https://instagram.com/",
    val cacheBustTimestamp: Long = 0L
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
            newUserRealMoneyBonus = doc.getLong("newUserRealMoneyBonus")?.toInt() ?: 5,
            newUserAppMoneyBonus = doc.getLong("newUserAppMoneyBonus")?.toInt() ?: 50,
            adminUpiId = doc.getString("adminUpiId") ?: "admin@upi",
            supportWhatsapp = doc.getString("supportWhatsapp") ?: "+919876543210",
            supportTelegram = doc.getString("supportTelegram") ?: "https://t.me/tournament_support",
            patchTag = doc.getString("patchTag") ?: "v1.4.0-LIVE",
            patchNotes = doc.getString("patchNotes") ?: "All server systems operational.",
            isLivePatchActive = doc.getBoolean("isLivePatchActive") ?: true,
            isRegistrationEnabled = doc.getBoolean("isRegistrationEnabled") ?: true,
            isWithdrawalsEnabled = doc.getBoolean("isWithdrawalsEnabled") ?: true,
            isSpinWheelEnabled = doc.getBoolean("isSpinWheelEnabled") ?: true,
            isWatchAdsEnabled = doc.getBoolean("isWatchAdsEnabled") ?: true,
            isYt100GoalUnlocked = doc.getBoolean("isYt100GoalUnlocked") ?: false,
            isCustomTaskEnabled = doc.getBoolean("isCustomTaskEnabled") ?: false,
            customTaskTitle = doc.getString("customTaskTitle") ?: "Follow Official Instagram",
            customTaskReward = doc.getLong("customTaskReward")?.toInt() ?: 100,
            customTaskUrl = doc.getString("customTaskUrl") ?: "https://instagram.com/",
            cacheBustTimestamp = doc.getLong("cacheBustTimestamp") ?: 0L
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
