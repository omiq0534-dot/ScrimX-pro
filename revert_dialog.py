import re

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

# Revert duplicate containerColor
content = content.replace('dismissButton = {\n                TextButton(onClick = { if (!isBooking && !isAdLoading) showBookingDialog = false }) {\n                    Text("Cancel", color = Color(0xFF9E9EA8))\n                }\n            },\n            containerColor = Color.White\n        )', 'dismissButton = {\n                TextButton(onClick = { if (!isBooking && !isAdLoading) showBookingDialog = false }) {\n                    Text("Cancel", color = Color(0xFF9E9EA8))\n                }\n            }\n        )')

# Revert text fields to Dark theme
content = content.replace('focusedTextColor = Color.Black', 'focusedTextColor = AppColors.TextPrimary')
content = content.replace('unfocusedTextColor = Color.Black', 'unfocusedTextColor = AppColors.TextPrimary')
content = content.replace('unfocusedBorderColor = Color(0xFFD1D5DB)', 'unfocusedBorderColor = Color(0xFF2E313D)')
content = content.replace('focusedContainerColor = Color(0xFFFFFFFF)', 'focusedContainerColor = Color(0xFF0C0D11)')
content = content.replace('unfocusedContainerColor = Color(0xFFFFFFFF)', 'unfocusedContainerColor = Color(0xFF0C0D11)')

# Also revert match title color inside dialog?
# "Match: ${match.title} (${match.map})"
content = content.replace('color = Color(0xFF4B5563)', 'color = Color(0xFF9E9EA8)')

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)

print("Reverted AlertDialog")
