package com.example.utils

import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

object TournamentTimeHelper {

    fun parseTimeToMillis(timeStr: String?): Long? {
        if (timeStr.isNullOrBlank()) return null
        val clean = timeStr.trim()

        val formats = listOf(
            "hh:mm a",
            "h:mm a",
            "HH:mm",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd hh:mm a",
            "dd MMM yyyy hh:mm a",
            "dd MMM hh:mm a",
            "dd/MM/yyyy HH:mm",
            "dd/MM/yyyy hh:mm a"
        )

        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                sdf.isLenient = true
                val date = sdf.parse(clean) ?: continue

                val calParsed = Calendar.getInstance().apply { time = date }
                val now = Calendar.getInstance()

                // If only time was parsed (year is near 1970)
                if (calParsed.get(Calendar.YEAR) <= 1970) {
                    val finalCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, calParsed.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, calParsed.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    return finalCal.timeInMillis
                } else {
                    return date.time
                }
            } catch (_: Exception) { }
        }
        return null
    }

    /**
     * Checks if Join Window has not started yet (i.e. joinTime is strictly in future)
     */
    fun isJoinPending(joinTime: String?): Boolean {
        if (joinTime.isNullOrBlank()) return false
        val millis = parseTimeToMillis(joinTime) ?: return false
        return millis > System.currentTimeMillis()
    }

    /**
     * Checks if Result Time has been reached or Admin manually declared result
     */
    fun isResultReady(resultTime: String?, isDeclared: Boolean, status: String = ""): Boolean {
        if (isDeclared) return true
        if (status.equals("Completed", ignoreCase = true)) return true
        if (resultTime.isNullOrBlank()) return false
        val millis = parseTimeToMillis(resultTime) ?: return false
        return System.currentTimeMillis() >= millis
    }

    /**
     * Checks if a normal non-joined user can view the points table
     * (Joined users always get instant access once result is ready;
     *  Normal users get access after [delayMinutes])
     */
    fun isPublicAccessAllowed(
        resultTime: String?,
        isDeclared: Boolean,
        delayMinutes: Int,
        isJoined: Boolean,
        status: String = ""
    ): Boolean {
        if (!isResultReady(resultTime, isDeclared, status)) return false
        if (isJoined) return true // Priority VIP access for joined players

        val resMillis = parseTimeToMillis(resultTime)
        if (resMillis == null) return true // If no explicit resultTime, allow public once declared

        val publicUnlockMillis = resMillis + (delayMinutes * 60 * 1000L)
        return System.currentTimeMillis() >= publicUnlockMillis
    }

    /**
     * Format milliseconds delta into MM:SS or HH:MM:SS
     */
    fun formatDuration(diffMillis: Long): String {
        if (diffMillis <= 0) return "00:00"
        val totalSecs = diffMillis / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60

        return if (hours > 0) {
            String.format(Locale.ENGLISH, "%02dh %02dm %02ds", hours, mins, secs)
        } else {
            String.format(Locale.ENGLISH, "%02dm %02ds", mins, secs)
        }
    }
}

/**
 * Composable reactive countdown state for Join Window Timer
 */
@Composable
fun rememberJoinCountdown(joinTime: String?): String? {
    if (joinTime.isNullOrBlank()) return null
    var countdown by remember(joinTime) { mutableStateOf<String?>(null) }

    LaunchedEffect(joinTime) {
        while (true) {
            val target = TournamentTimeHelper.parseTimeToMillis(joinTime)
            if (target != null) {
                val diff = target - System.currentTimeMillis()
                if (diff > 0) {
                    countdown = TournamentTimeHelper.formatDuration(diff)
                } else {
                    countdown = null
                    break
                }
            } else {
                countdown = null
                break
            }
            delay(1000)
        }
    }
    return countdown
}

/**
 * Composable reactive countdown state for Result Declaration Timer
 */
@Composable
fun rememberResultCountdown(resultTime: String?, isDeclared: Boolean): String? {
    if (isDeclared || resultTime.isNullOrBlank()) return null
    var countdown by remember(resultTime, isDeclared) { mutableStateOf<String?>(null) }

    LaunchedEffect(resultTime, isDeclared) {
        while (true) {
            val target = TournamentTimeHelper.parseTimeToMillis(resultTime)
            if (target != null) {
                val diff = target - System.currentTimeMillis()
                if (diff > 0) {
                    countdown = TournamentTimeHelper.formatDuration(diff)
                } else {
                    countdown = null
                    break
                }
            } else {
                countdown = null
                break
            }
            delay(1000)
        }
    }
    return countdown
}
