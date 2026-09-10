import re

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

content = content.replace('''    val bgColor = when {
        isMySlot -> Color(0xFF10B981).copy(alpha = 0.15f)
        isSelected -> Color(0xFFFFD700).copy(alpha = 0.15f)
        isBooked -> Color(0xFF1C1F2B)
        else -> Color(0xFF161922)
    }''', '''    val bgColor = when {
        isMySlot -> Color(0xFF10B981).copy(alpha = 0.15f)
        isSelected -> Color(0xFFFFD700).copy(alpha = 0.15f)
        isBooked -> Color(0xFFF3F4F6)
        else -> Color(0xFFF9FAFB)
    }''')

content = content.replace('''    val borderColor = when {
        isMySlot -> Color(0xFF10B981)
        isSelected -> Color(0xFFFFD700)
        isBooked -> Color(0xFF2E3346)
        else -> Color(0xFFD1D5DB)
    }''', '''    val borderColor = when {
        isMySlot -> Color(0xFF10B981)
        isSelected -> Color(0xFFFFD700)
        isBooked -> Color(0xFFE5E7EB)
        else -> Color(0xFFD1D5DB)
    }''')

# Text(playerName ?: "Booked", ... color = AppColors.TextPrimary) -> color = Color.Black
content = content.replace('color = AppColors.TextPrimary,\n                        maxLines = 1', 'color = Color.Black,\n                        maxLines = 1')

# Text("Available", color = Color(0xFF10B981)) -> color = Color.Black
# Let's check where "Available" is in HeadToHeadSlotItem
with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)

print("Fixed HeadToHeadSlotItem")
with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

content = content.replace('color = if (isSelected) Color(0xFFFFD700) else AppColors.TextPrimary', 'color = if (isSelected) Color(0xFFFFD700) else Color.Black')

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)

print("Fixed HeadToHeadSlotItem remaining")
