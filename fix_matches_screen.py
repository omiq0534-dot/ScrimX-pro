import re

with open("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "r") as f:
    content = f.read()

# Fix MyJoinedMatchCard background
# From .background(AppColors.TextPrimary) to .background(AppColors.CardBackground)
content = content.replace(".background(AppColors.TextPrimary)", ".background(AppColors.CardBackground)")

# Also, the text inside MyJoinedMatchCard might be using AppColors.TextPrimary, which is correct for a Dark card.
# But let's check the title in MatchesScreen (like "All Scrims" text)
# Top text on the screen should be Black.
content = content.replace('color = if (selectedTab == 0) AppColors.TextPrimary else Color(0xFF9CA3AF),', 'color = if (selectedTab == 0) Color.White else Color(0xFF9CA3AF),')
content = content.replace('color = if (selectedTab == 1) AppColors.TextPrimary else Color(0xFF9CA3AF),', 'color = if (selectedTab == 1) Color.White else Color(0xFF9CA3AF),')
# Wait, the tabs background:
# .background(Color(0xFF1E2130)) -> Dark background for the tabs container.
# So text inside should be White.

with open("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "w") as f:
    f.write(content)

print("Fixed MatchesScreen")
