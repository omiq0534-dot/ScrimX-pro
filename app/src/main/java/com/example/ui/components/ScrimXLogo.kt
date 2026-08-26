package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScrimXLogo(
    modifier: Modifier = Modifier,
    isDarkBackground: Boolean = true,
    fontSize: TextUnit = 28.sp,
    showSubtext: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SCRIM",
                color = if (isDarkBackground) Color.White else Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = (-0.5).sp,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "X",
                color = Color(0xFFFF003F), // Fiery Electric Ruby Red
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                letterSpacing = (-0.5).sp,
                fontFamily = FontFamily.SansSerif
            )
        }
        if (showSubtext) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "ESPORTS ARENA",
                color = if (isDarkBackground) Color(0xFF8E92A4) else Color(0xFF6B7280),
                fontWeight = FontWeight.Bold,
                fontSize = (fontSize.value * 0.35f).sp,
                letterSpacing = 2.sp
            )
        }
    }
}
