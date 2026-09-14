package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Compose Shimmer Effect Modifier
 */
@Composable
fun Modifier.shimmerEffect(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp),
    showShimmer: Boolean = true
): Modifier {
    if (!showShimmer) return this

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "translateAnim"
    )

    val shimmerColors = listOf(
        Color(0xFF141E28),
        Color(0xFF2C3E52),
        Color(0xFF141E28)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    return this
        .clip(shape)
        .background(brush)
}

/**
 * Premium Shimmer Skeleton for Tournament Cards
 */
@Composable
fun ShimmerMatchCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0D141C))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(24.dp)
                        .shimmerEffect(RoundedCornerShape(8.dp))
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp)
                        .shimmerEffect(RoundedCornerShape(8.dp))
                )
            }

            // Middle Title & Time
            Column {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(20.dp)
                        .shimmerEffect(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(14.dp)
                        .shimmerEffect(RoundedCornerShape(4.dp))
                )
            }

            // Bottom Row: Prize & Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(36.dp)
                            .shimmerEffect(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(36.dp)
                            .shimmerEffect(RoundedCornerShape(8.dp))
                    )
                }
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(42.dp)
                        .shimmerEffect(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

/**
 * Premium Shimmer Skeleton for Home Banner Carousel
 */
@Composable
fun ShimmerBannerCard(
    height: Dp = 150.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .shimmerEffect(RoundedCornerShape(18.dp))
    )
}

/**
 * Premium Shimmer Skeleton for Wallet Balance Card
 */
@Composable
fun ShimmerWalletCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0D141C))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(16.dp)
                    .shimmerEffect(RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(28.dp)
                    .shimmerEffect(RoundedCornerShape(6.dp))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(32.dp)
                        .shimmerEffect(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}
