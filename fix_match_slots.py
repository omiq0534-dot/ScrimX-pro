import re

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

# Make SlotCard proper light theme
content = content.replace('Color(0xFF131922)', 'Color(0xFFF9FAFB)') # Normal slot background
content = content.replace('Color(0xFF08080A).copy(alpha = 0.5f)', 'Color(0xFFF3F4F6)') # Booked slot background
content = content.replace('Color(0xFF1E2430)', 'Color(0xFFE5E7EB)') # Booked slot border
content = content.replace('Color(0xFF262A38)', 'Color(0xFFD1D5DB)') # Normal slot border inside badge
content = content.replace('Color(0xFF08080A)', 'Color(0xFFF3F4F6)') # Available slot badge

content = content.replace('color = if (isSelected || isMySlot) Color.Black else if (slot.isBooked) Color(0xFF666677) else AppColors.TextPrimary', 'color = if (isSelected || isMySlot) Color.Black else if (slot.isBooked) Color(0xFF666677) else Color.Black')

content = content.replace('color = if (isMySlot) AppColors.TextPrimary else Color(0xFFE0E0E0)', 'color = if (isMySlot) Color.Black else Color.Black')

content = content.replace('color = if (isSelected) Color(0xFFFACC15) else AppColors.TextPrimary', 'color = if (isSelected) Color(0xFFFACC15) else Color.Black')

content = content.replace('Color(0xFF1F222C)', 'Color(0xFFF3F4F6)') # Requirements box background
content = content.replace('color = Color(0xFFAAAAAA)', 'color = Color(0xFF4B5563)') # Requirements text
content = content.replace('color = Color(0xFF0C0D11)', 'color = Color(0xFFFFFFFF)') # Text field backgrounds

# Make HeadToHead slot item proper light theme
content = content.replace('backgroundColor = if (isMySlot) Color(0xFF22C55E).copy(alpha = 0.15f)\n    else if (isBooked) Color(0xFF0F1118)\n    else Color(0xFF131922)', 'backgroundColor = if (isMySlot) Color(0xFF22C55E).copy(alpha = 0.15f)\n    else if (isBooked) Color(0xFFF3F4F6)\n    else Color(0xFFF9FAFB)')

content = content.replace('borderColor = if (isMySlot) Color(0xFF22C55E)\n    else if (isBooked) Color(0xFF1E2430)\n    else Color(0xFF1E2430)', 'borderColor = if (isMySlot) Color(0xFF22C55E)\n    else if (isBooked) Color(0xFFE5E7EB)\n    else Color(0xFFE5E7EB)')

content = content.replace('color = if (isBooked) AppColors.TextPrimary else Color(0xFF666677)', 'color = if (isBooked) Color.Black else Color(0xFF666677)')


with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)

print("Fixed MatchDetailsScreen Slots")
