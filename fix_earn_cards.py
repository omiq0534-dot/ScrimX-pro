import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# Add rememberInfiniteTransition, infiniteRepeatable, tween etc. if not there
if "import androidx.compose.animation.core.rememberInfiniteTransition" not in content:
    content = content.replace("import androidx.compose.animation.core.*", "import androidx.compose.animation.core.*\nimport androidx.compose.animation.core.rememberInfiniteTransition\nimport androidx.compose.animation.core.animateFloat\nimport androidx.compose.animation.core.infiniteRepeatable\nimport androidx.compose.animation.core.tween")

earning_zone_regex = r'(@Composable\nfun EarningZone\([\s\S]*?fun EarnCard\([\s\S]*?\n\})'

new_earning_zone = '''@Composable
fun EarningZone(
    onDailyClick: () -> Unit = {},
    onSpinClick: () -> Unit = {},
    onWatchClick: () -> Unit = {}
) {
    Column {
        Text("Earning Zone", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            EarnCard(
                title = "Daily",
                modifier = Modifier.weight(1f),
                onClick = onDailyClick
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = "Daily", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            EarnCard(
                title = "Spin",
                modifier = Modifier.weight(1f),
                onClick = onSpinClick
            ) {
                SpinWheelIcon(modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            EarnCard(
                title = "Video",
                modifier = Modifier.weight(1f),
                onClick = onWatchClick
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Watch", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun EarnCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    iconContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "ScaleAnim")
    
    // Hardcore Animation: Pulsing White Light
    val infiniteTransition = rememberInfiniteTransition(label = "WhiteLightPulse")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IntensityAnim"
    )

    val baseGlowColor = Color.White

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .background(Color(0xFF14161F))
            .border(1.dp, Color(0xFF262A38), RoundedCornerShape(24.dp))
    ) {
        // Bottom Inner Glow and 3D effects
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Soft animated white glow from bottom up
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        baseGlowColor.copy(alpha = glowIntensity * 0.3f), // animated middle alpha
                        baseGlowColor.copy(alpha = glowIntensity)         // animated bottom alpha
                    ),
                    startY = h * 0.3f,
                    endY = h
                )
            )

            // Bright Rim light at the bottom for 3D depth (also animating slightly)
            drawLine(
                color = baseGlowColor.copy(alpha = 0.7f + (glowIntensity * 0.2f)),
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 12f
            )

            // Top highlight
            drawLine(
                color = Color.White.copy(alpha = 0.05f),
                start = Offset(0f, 0f),
                end = Offset(w, 0f),
                strokeWidth = 4f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B1E2B))
                    .border(1.dp, baseGlowColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White, letterSpacing = 0.5.sp)
        }
    }
}'''

content = re.sub(earning_zone_regex, new_earning_zone, content)

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
