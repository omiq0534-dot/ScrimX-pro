import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Replace infinite transition part
old_transition = """    val infiniteTransition = rememberInfiniteTransition(label = "googlePlayShimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )"""

new_transition = """    val infiniteTransition = rememberInfiniteTransition(label = "googlePlayShimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val fastShimmer by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "fastShimmer"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )"""
content = content.replace(old_transition, new_transition)

# Replace the box modifiers
old_box = """        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF061520),
                            Color(0xFF0B2433),
                            Color(0xFF04101A),
                            Color(0xFF081C29)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 900f)
                    )
                )
                .border(
                    0.8.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.5f),
                            Color(0xFF00E676).copy(alpha = 0.5f),
                            Color(0xFF00B0FF).copy(alpha = 0.4f),
                            Color(0xFFFFD700).copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(800f, 600f)
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        )"""

new_box = """        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0A2940),
                            Color(0xFF061520),
                            Color(0xFF02070A)
                        ),
                        center = Offset(400f, 200f),
                        radius = 800f
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = pulseAlpha),
                            Color(0xFF00E676).copy(alpha = pulseAlpha),
                            Color(0xFF00B0FF).copy(alpha = pulseAlpha),
                            Color(0xFF00E5FF).copy(alpha = pulseAlpha)
                        ),
                        start = Offset(shimmerTranslate - 500f, 0f),
                        end = Offset(shimmerTranslate + 500f, 600f)
                    ),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        )"""
content = content.replace(old_box, new_box)

# Replace the Canvas and sheen brush
old_canvas = """            // Diamond Glass Facets & Moving Shimmer Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Subtle diamond crystalline geometry
                val facetPath = Path().apply {
                    moveTo(w * 0.7f, 0f)
                    lineTo(w, h * 0.45f)
                    lineTo(w * 0.5f, h)
                    lineTo(0f, h * 0.35f)
                    close()
                }
                drawPath(
                    path = facetPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.05f), Color.Transparent),
                        start = Offset(w * 0.7f, 0f),
                        end = Offset(0f, h)
                    )
                )

                // Dynamic Moving Shimmer Sheen Beam
                val sheenWidth = 140f
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.12f),
                        Color(0xFF00E5FF).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - sheenWidth, 0f),
                    end = Offset(shimmerTranslate + sheenWidth, h)
                )
                drawRect(brush = sheenBrush)

                // Sparkling Diamond Stars (✦)
                drawCircle(Color(0xFF00E5FF).copy(alpha = 0.7f), radius = 2.dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                drawCircle(Color.White.copy(alpha = 0.9f), radius = 1.2.dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                drawCircle(Color(0xFF69F0AE).copy(alpha = 0.6f), radius = 1.5.dp.toPx(), center = Offset(w * 0.92f, h * 0.38f))
            }"""

new_canvas = """            // Diamond Glass Facets & Ultra-Premium Moving Shimmer Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Holographic crystalline geometry
                val facetPath = Path().apply {
                    moveTo(w * 0.6f, 0f)
                    lineTo(w, h * 0.4f)
                    lineTo(w * 0.4f, h)
                    lineTo(0f, h * 0.4f)
                    close()
                }
                drawPath(
                    path = facetPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.08f), Color.Transparent),
                        start = Offset(w * 0.6f, 0f),
                        end = Offset(0f, h)
                    )
                )

                // Base Slow Shimmer (Wide)
                val sheenWidth = 250f
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00E5FF).copy(alpha = 0.15f * pulseAlpha),
                        Color.White.copy(alpha = 0.2f * pulseAlpha),
                        Color(0xFF00E676).copy(alpha = 0.15f * pulseAlpha),
                        Color.Transparent
                    ),
                    start = Offset(shimmerTranslate - sheenWidth, -sheenWidth),
                    end = Offset(shimmerTranslate + sheenWidth, h + sheenWidth)
                )
                drawRect(brush = sheenBrush)
                
                // Fast Sharp Holographic Flash
                val fastSheenWidth = 60f
                val fastSheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.8f),
                        Color.Transparent
                    ),
                    start = Offset(fastShimmer - fastSheenWidth, 0f),
                    end = Offset(fastShimmer + fastSheenWidth, h)
                )
                drawRect(brush = fastSheenBrush)

                // Sparkling Diamond Stars (✦) with animated pulse
                drawCircle(Color(0xFF00E5FF).copy(alpha = 0.7f * pulseAlpha), radius = (2.5f * pulseAlpha).dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                drawCircle(Color.White.copy(alpha = 0.9f * pulseAlpha), radius = (1.5f * pulseAlpha).dp.toPx(), center = Offset(w * 0.85f, h * 0.2f))
                
                drawCircle(Color(0xFF69F0AE).copy(alpha = 0.6f * pulseAlpha), radius = (2f * pulseAlpha).dp.toPx(), center = Offset(w * 0.92f, h * 0.38f))
                drawCircle(Color.White, radius = 1.dp.toPx(), center = Offset(w * 0.92f, h * 0.38f))
            }"""
content = content.replace(old_canvas, new_canvas)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

