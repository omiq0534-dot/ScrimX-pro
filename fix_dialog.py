import re

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "r") as f:
    content = f.read()

# Make AlertDialog fully white theme
content = content.replace('focusedTextColor = AppColors.TextPrimary', 'focusedTextColor = Color.Black')
content = content.replace('unfocusedTextColor = AppColors.TextPrimary', 'unfocusedTextColor = Color.Black')

# Also fix OutlinedTextField border colors
content = content.replace('unfocusedBorderColor = Color(0xFF2E313D)', 'unfocusedBorderColor = Color(0xFFD1D5DB)')

# Fix AlertDialog background
# Look for dismissButton = { ... } )
content = content.replace('dismissButton = {\n                TextButton(onClick = { if (!isBooking && !isAdLoading) showBookingDialog = false }) {\n                    Text("Cancel", color = Color(0xFF9E9EA8))\n                }\n            }\n        )', 'dismissButton = {\n                TextButton(onClick = { if (!isBooking && !isAdLoading) showBookingDialog = false }) {\n                    Text("Cancel", color = Color(0xFF9E9EA8))\n                }\n            },\n            containerColor = Color.White\n        )')

with open("app/src/main/java/com/example/ui/screens/MatchDetailsScreen.kt", "w") as f:
    f.write(content)

print("Fixed AlertDialog")
