import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

earning_zone_regex = r'(@Composable\nfun EarnCard\([\s\S]*?\n\})'

new_earn_card = '''@Composable
fun EarnCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    iconContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "ScaleAnim")
    
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
        // Bottom Inner Glow and 3D effects (Static and Softer)
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Soft static white glow from bottom up (lighter)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        baseGlowColor.copy(alpha = 0.02f), // soft middle alpha
                        baseGlowColor.copy(alpha = 0.1f)   // soft bottom alpha
                    ),
                    startY = h * 0.4f,
                    endY = h
                )
            )

            // Subtle Rim light at the bottom for 3D depth
            drawLine(
                color = baseGlowColor.copy(alpha = 0.25f),
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 8f
            )

            // Top highlight
            drawLine(
                color = Color.White.copy(alpha = 0.04f),
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
                    .border(1.dp, baseGlowColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White, letterSpacing = 0.5.sp)
        }
    }
}'''

content = re.sub(earning_zone_regex, new_earn_card, content)

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
