import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# Make sure imports are there
if "import androidx.compose.ui.draw.scale" not in content:
    content = content.replace("import androidx.compose.ui.draw.clip", "import androidx.compose.ui.draw.clip\nimport androidx.compose.ui.draw.scale")

if "import androidx.compose.foundation.interaction.MutableInteractionSource" not in content:
    content = content.replace("import androidx.compose.foundation.clickable", "import androidx.compose.foundation.clickable\nimport androidx.compose.foundation.interaction.MutableInteractionSource\nimport androidx.compose.foundation.interaction.collectIsPressedAsState")

# Replace EarningZone and EarnCard
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
                glowColor = Color(0xFFFFD700), // Gold
                modifier = Modifier.weight(1f),
                onClick = onDailyClick
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = "Daily", tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            EarnCard(
                title = "Spin",
                glowColor = Color(0xFFE91E63), // Pink
                modifier = Modifier.weight(1f),
                onClick = onSpinClick
            ) {
                SpinWheelIcon(modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            EarnCard(
                title = "Video",
                glowColor = Color(0xFF00E5FF), // Cyan
                modifier = Modifier.weight(1f),
                onClick = onWatchClick
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Watch", tint = Color(0xFF00E5FF), modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun EarnCard(
    title: String,
    glowColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    iconContent: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "ScaleAnim")

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

            // Soft glow from bottom up
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        glowColor.copy(alpha = 0.05f),
                        glowColor.copy(alpha = 0.35f)
                    ),
                    startY = h * 0.4f,
                    endY = h
                )
            )

            // Bright Rim light at the bottom for 3D depth
            drawLine(
                color = glowColor.copy(alpha = 0.9f),
                start = Offset(0f, h),
                end = Offset(w, h),
                strokeWidth = 10f
            )

            // Top highlight
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
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
                    .border(1.dp, glowColor.copy(alpha = 0.4f), CircleShape),
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
