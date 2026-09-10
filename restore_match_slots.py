import re

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

content = content.replace('Color(0xFFF9FAFB)', 'Color(0xFF131922)') # Normal slot background
content = content.replace('Color(0xFFF3F4F6)', 'Color(0xFF08080A).copy(alpha = 0.5f)') # Booked slot background / H2H background
content = content.replace('Color(0xFFE5E7EB)', 'Color(0xFF1E2430)') # Booked slot border / H2H border
content = content.replace('Color(0xFFD1D5DB)', 'Color(0xFF262A38)') # Normal slot border inside badge
content = content.replace('Color(0xFFF3F4F6)', 'Color(0xFF08080A)') # Available slot badge / Requirements box

content = content.replace('color = if (isSelected || isMySlot) Color.Black else if (slot.isBooked) Color(0xFF666677) else Color.Black', 'color = if (isSelected || isMySlot) Color.Black else if (slot.isBooked) Color(0xFF666677) else AppColors.TextPrimary')

content = content.replace('color = if (isMySlot) Color.Black else Color.Black', 'color = if (isMySlot) AppColors.TextPrimary else Color(0xFFE0E0E0)')

content = content.replace('color = if (isSelected) Color(0xFFFACC15) else Color.Black', 'color = if (isSelected) Color(0xFFFACC15) else AppColors.TextPrimary')

content = content.replace('color = Color(0xFF4B5563)', 'color = Color(0xFFAAAAAA)') # Requirements text
content = content.replace('color = Color(0xFFFFFFFF)', 'color = Color(0xFF0C0D11)') # Text field backgrounds

content = content.replace('color = if (isBooked) Color.Black else Color(0xFF666677)', 'color = if (isBooked) AppColors.TextPrimary else Color(0xFF666677)')

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)

print("Restored MatchDetailsScreen Slots")
