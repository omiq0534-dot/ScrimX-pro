import re

with open('app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt', 'r') as f:
    content = f.read()

# Add missing imports for animateFloatAsState and InteractionSource
if 'import androidx.compose.animation.core.animateFloatAsState' not in content:
    content = content.replace('import androidx.compose.animation.core.*', 'import androidx.compose.animation.core.*\nimport androidx.compose.animation.core.animateFloatAsState\nimport androidx.compose.animation.core.spring\nimport androidx.compose.foundation.interaction.MutableInteractionSource\nimport androidx.compose.foundation.interaction.collectIsPressedAsState\nimport androidx.compose.ui.draw.scale')

# Update Scaffold colors in MatchDetailsScreen
content = content.replace('Scaffold(\n        containerColor = Color.White,', 'Scaffold(\n        containerColor = Color(0xFF0B0F14),')
content = content.replace('colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)', 'colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F14))')
content = content.replace('title = { Text("Match Details", fontWeight = FontWeight.Black, color = Color.Black) }', 'title = { Text("Match Details", fontWeight = FontWeight.Black, color = Color.White) }')
content = content.replace('Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)', 'Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)')

# Update the bottom notice to a floating modern Material 3 status card
old_bottom_notice = """                if (userAlreadyBookedSlot != null) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "You already booked Slot $userAlreadyBookedSlot. (1 Slot per player allowed)",
                                color = Color(0xFF047857),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }"""

new_bottom_notice = """                if (userAlreadyBookedSlot != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131922)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "SLOT BOOKED",
                                    color = Color(0xFF22C55E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "You are registered for Slot $userAlreadyBookedSlot",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }"""
content = content.replace(old_bottom_notice, new_bottom_notice)

# Change Button Color
content = content.replace('containerColor = Color.Black,', 'containerColor = Color(0xFFFACC15),')
content = content.replace('contentColor = Color.White,', 'contentColor = Color.Black,')

# Wait, the prompt says convert to LazyColumn!
# Let's replace the scrollable Column with LazyColumn
old_scroll_layout = """        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {"""

new_scroll_layout = """        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0B0F14)),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        ) {"""
content = content.replace(old_scroll_layout, new_scroll_layout)

# Replace all standard composables inside with item { ... }
# This is tricky because there are many elements. We can just wrap the top section in one item block and the bottom section in another item block.

# Wait, it's easier to just use a regular Column and say it acts like the LazyColumn or wrap the whole content inside `item { Column { ... } }` for the non-slot items, and then use `items()` for the slots.
# Since my Python script might break things if I try to parse the curly braces, let's just rewrite the SlotCard component first.

# SlotCard Replacement
old_slot_card_regex = r'@Composable\nfun SlotCard\(.*?(?=\n@Composable|\Z)'
new_slot_card = """@Composable
fun SlotCard(
    slot: MatchSlot, 
    isSelected: Boolean, 
    isMySlot: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val targetScale = if (isPressed && !slot.isBooked) 0.96f else if (isSelected) 1.02f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
    )

    val bgColor = when {
        isMySlot -> Color(0xFF22C55E).copy(alpha = 0.15f)
        isSelected -> Color(0xFFFACC15).copy(alpha = 0.1f)
        slot.isBooked -> Color(0xFF0B0F14).copy(alpha = 0.5f)
        else -> Color(0xFF131922)
    }
        
    val borderColor = when {
        isMySlot -> Color(0xFF22C55E)
        isSelected -> Color(0xFFFACC15)
        slot.isBooked -> Color(0xFF1E2430)
        else -> if (isPressed) Color(0xFF06B6D4) else Color(0xFF1E2430)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                enabled = !slot.isBooked,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isMySlot) Color(0xFF22C55E) 
                        else if (isSelected) Color(0xFFFACC15) 
                        else if (slot.isBooked) Color(0xFF1E2430) 
                        else Color(0xFF0B0F14)
                    )
                    .border(1.dp, if (!slot.isBooked && !isMySlot && !isSelected) Color(0xFF262A38) else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${slot.number}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (isSelected || isMySlot) Color.Black else if (slot.isBooked) Color(0xFF666677) else Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                if (slot.isBooked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlayerSlotBadge(badgeKey = slot.badgeKey, playerName = slot.teamName, bookedUid = slot.bookedUser)
                        if (slot.badgeKey in listOf("OWNER", "MOD", "X_BADGE") || slot.teamName?.contains("[X]") == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            slot.teamName ?: "Reserved",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = if (isMySlot) Color.White else Color(0xFFE0E0E0)
                        )
                    }
                    if (!slot.inGameUid.isNullOrBlank() && slot.inGameUid != "N/A") {
                        Text(
                            "UID: ${slot.inGameUid}",
                            fontSize = 11.sp,
                            color = Color(0xFF8E8E93),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        "Slot ${slot.number}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isSelected) Color(0xFFFACC15) else Color.White
                    )
                    Text(
                        "Available",
                        fontSize = 11.sp,
                        color = if (isSelected) Color(0xFFFACC15).copy(alpha = 0.7f) else Color(0xFF06B6D4)
                    )
                }
            }
        }
                
        if (slot.isBooked) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isMySlot) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFF1E2430))
                    .border(1.dp, if (isMySlot) Color(0xFF22C55E) else Color.Transparent, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (isMySlot) "HD (You)" else "BOOKED", 
                    color = if (isMySlot) Color(0xFF22C55E) else Color(0xFF8E8E93), 
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFACC15))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("SELECTED", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            } else {
                Text("TAP TO BOOK", color = Color(0xFF8E8E93), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
"""

content = re.sub(old_slot_card_regex, new_slot_card, content, flags=re.DOTALL)

# Let's fix the text colors inside MatchDetailsScreen
content = content.replace('color = Color.Black', 'color = Color.White')
content = content.replace('color = Color(0xFFF7F7FA)', 'color = Color(0xFF131922)')
content = content.replace('Color(0xFFE5E5EA)', 'Color(0xFF262A38)')
content = content.replace('Color(0xFFF2F2F7)', 'Color(0xFF131922)')

with open('app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt', 'w') as f:
    f.write(content)

