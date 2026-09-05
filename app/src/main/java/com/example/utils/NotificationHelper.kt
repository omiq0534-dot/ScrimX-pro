package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    const val CHANNEL_ROOM_ALERTS = "scrimx_room_alerts"
    const val CHANNEL_GENERAL = "scrimx_general_alerts"
    private const val PREFS_NOTIFICATIONS = "scrimx_notif_prefs"

    fun initNotificationChannels(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (notificationManager != null) {
                    // Room Credentials Channel (High Priority with Sound & Heads-up Banner)
                    val roomChannel = NotificationChannel(
                        CHANNEL_ROOM_ALERTS,
                        "Room ID & Credentials Alerts",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Instant alerts when Custom Room ID and Password are published"
                        enableVibration(true)
                        enableLights(true)
                        setShowBadge(true)
                    }

                    // General Channel
                    val generalChannel = NotificationChannel(
                        CHANNEL_GENERAL,
                        "Tournament & Match Announcements",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "General tournament alerts and winning announcements"
                        setShowBadge(true)
                    }

                    notificationManager.createNotificationChannel(roomChannel)
                    notificationManager.createNotificationChannel(generalChannel)
                    Log.d(TAG, "Notification channels initialized successfully")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init notification channels", e)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun showRoomCredentialsNotification(
        context: Context,
        matchId: String,
        matchTitle: String,
        roomId: String,
        roomPass: String,
        gameMode: String = "Tournament"
    ) {
        try {
            initNotificationChannels(context)

            // Check if already notified for this exact roomId to prevent spamming
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NOTIFICATIONS, Context.MODE_PRIVATE)
            val notifKey = "notified_room_${matchId}_${roomId.trim()}_${roomPass.trim()}"
            if (prefs.getBoolean(notifKey, false)) {
                return
            }

            // Check permission on Android 13+
            if (!hasNotificationPermission(context)) {
                Log.w(TAG, "Notification permission not granted, skipping system alert")
                return
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("OPEN_MATCH_ID", matchId)
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                matchId.hashCode(),
                intent,
                pendingIntentFlags
            )

            val displayTitle = if (matchTitle.isNotBlank()) matchTitle else "Match Tournament"
            val bigTextContent = "🎮 $displayTitle ($gameMode)\n\n" +
                    "🔑 ROOM ID: $roomId\n" +
                    "🔒 PASSWORD: $roomPass\n\n" +
                    "⚡ Tap here to open ScrimX Pro, copy credentials & join the custom room now!"

            val notification = NotificationCompat.Builder(context, CHANNEL_ROOM_ALERTS)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("🚨 ROOM ID & PASS IS LIVE!")
                .setContentText("Room ID: $roomId | Pass: $roomPass ($displayTitle)")
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextContent))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            val notifId = (System.currentTimeMillis() % 100000).toInt() + 100
            notificationManager.notify(notifId, notification)

            // Save to prefs so it is not resent repeatedly
            prefs.edit().putBoolean(notifKey, true).apply()
            Log.d(TAG, "Room notification sent successfully for match $matchId")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying room notification", e)
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun showGeneralAnnouncementNotification(
        context: Context,
        title: String,
        message: String
    ) {
        try {
            initNotificationChannels(context)

            if (!hasNotificationPermission(context)) {
                Log.w(TAG, "Notification permission not granted, skipping announcement")
                return
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                pendingIntentFlags
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 250, 150, 250))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            val notifId = (System.currentTimeMillis() % 100000).toInt() + 200
            notificationManager.notify(notifId, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying announcement notification", e)
        }
    }

    fun openGameApp(context: Context, gameMode: String) {
        try {
            val pm = context.packageManager
            val packagesToTry = when {
                gameMode.contains("Free Fire", ignoreCase = true) || gameMode.contains("FF", ignoreCase = true) -> {
                    listOf("com.dts.freefiremax", "com.dts.freefireth")
                }
                gameMode.contains("BGMI", ignoreCase = true) || gameMode.contains("PUBG", ignoreCase = true) -> {
                    listOf("com.pubg.imobile", "com.tencent.ig")
                }
                else -> {
                    listOf("com.dts.freefiremax", "com.dts.freefireth", "com.pubg.imobile")
                }
            }

            for (pkg in packagesToTry) {
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    return
                }
            }

            // Fallback: Launch general Play Store search or default intent
            val webIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(webIntent, "Open Game"))
        } catch (e: Exception) {
            Log.e(TAG, "Error launching game app", e)
        }
    }
}
