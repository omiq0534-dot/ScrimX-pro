package com.example.ui.theme

import androidx.compose.ui.composed
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.glassCard(
    cornerRadius: Dp = 16.dp,
    alpha: Float = 1.0f
): Modifier = composed {
    this
        .shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = Color.Black.copy(alpha = 0.5f),
            spotColor = Color.Black.copy(alpha = 0.5f)
        )
        .background(AppColors.CardBackground, RoundedCornerShape(cornerRadius))
        .border(
            width = 1.dp,
            color = AppColors.BorderColor,
            shape = RoundedCornerShape(cornerRadius)
        )
}

fun Modifier.glassBackground(): Modifier = composed {
    this.background(AppColors.ScreenBackground)
}
