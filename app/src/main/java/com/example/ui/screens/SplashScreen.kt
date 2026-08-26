package com.example.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

import com.example.FirebaseHelper

@Composable
fun SplashScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // Animation states
    val slideAnimation = remember { Animatable(500f) } // Slide from right
    val alphaAnimation = remember { Animatable(0f) }
    val scaleAnimation = remember { Animatable(1f) }
    val flashAmount = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        FirebaseHelper.init(context)
        // Fade in "SCRIM"
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        // Slide "X" from the side smoothly
        slideAnimation.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = { OvershootInterpolator(1.2f).getInterpolation(it) }
            )
        )
        // Flash effect
        flashAmount.animateTo(1f, animationSpec = tween(100))
        scaleAnimation.animateTo(1.1f, animationSpec = tween(100))
        
        flashAmount.animateTo(0f, animationSpec = tween(200))
        scaleAnimation.animateTo(1f, animationSpec = tween(200))

        delay(600)

        // Check if user is logged in safely
        val destination = try {
            val user = FirebaseHelper.getAuth()?.currentUser
            if (user != null) "home" else "login"
        } catch (e: Exception) {
            "login"
        }
        
        try {
            navController.navigate(destination) {
                popUpTo("splash") { inclusive = true }
            }
        } catch (e: Exception) {
            // fallback navigation if needed
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.scale(scaleAnimation.value)
        ) {
            Text(
                text = "SCRIM ",
                color = Color.White.copy(alpha = alphaAnimation.value),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            
            val currentColor = androidx.compose.ui.graphics.lerp(
                Color(0xFFFF003C), 
                Color.White, 
                flashAmount.value
            )
            
            Text(
                text = "X",
                color = currentColor,
                fontSize = 88.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.offset(x = slideAnimation.value.dp)
            )
        }
    }
}
