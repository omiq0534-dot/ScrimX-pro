package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.ReadOnlyComposable

object AppColors {
    val isDark: Boolean
        @Composable
        get() = ThemeManager.isDarkTheme.collectAsState().value

    // Backgrounds
    val ScreenBackground: Color
        @Composable
        get() = if (isDark) Color(0xFF000000) else Color(0xFFF2F4F8)

    val CardBackground: Color
        @Composable
        get() = if (isDark) Color(0xFF131316) else Color(0xCCFFFFFF)

    val SubCardBackground: Color
        @Composable
        get() = if (isDark) Color(0xFF1B1B1F) else Color(0xFFF3F4F6)

    // Borders
    val BorderColor: Color
        @Composable
        get() = if (isDark) Color(0xFF26262D) else Color(0xFFE5E7EB)

    // Text
    val TextPrimary: Color
        @Composable
        get() = if (isDark) Color.White else Color(0xFF111827)

    val TextSecondary: Color
        @Composable
        get() = if (isDark) Color(0xFFAAAAAA) else Color(0xFF6B7280)

    val TextHighlight: Color
        @Composable
        get() = if (isDark) Color(0xFFFFD700) else Color(0xFFD97706)

    // Accents
    val PrimaryAccent: Color = Color(0xFFFFD700)
    val PrimaryAccentText: Color = Color.Black
    
    val Positive: Color = Color(0xFF00E676)
    val Negative: Color = Color(0xFFFF3366)
    val Info: Color = Color(0xFF00E5FF)
    
    val Divider: Color
        @Composable
        get() = if (isDark) Color(0xFF1D1D23) else Color(0xFFE5E7EB)

    val ButtonContainer: Color
        @Composable
        get() = if (isDark) Color.White else Color.Black

    val ButtonContent: Color
        @Composable
        get() = if (isDark) Color.Black else Color.White
}
