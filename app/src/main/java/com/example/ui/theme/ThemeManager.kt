package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK = "is_dark_theme"
    
    // We default to True (Black Theme)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isDarkTheme.value = prefs.getBoolean(KEY_IS_DARK, true)
    }

    fun toggleTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newTheme = !_isDarkTheme.value
        prefs.edit().putBoolean(KEY_IS_DARK, newTheme).apply()
        _isDarkTheme.value = newTheme
    }

    fun setDarkTheme(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DARK, isDark).apply()
        _isDarkTheme.value = isDark
    }
}
