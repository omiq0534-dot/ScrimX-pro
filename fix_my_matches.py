import re

with open("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "r") as f:
    content = f.read()

# Fix Player IGN Details Box background
content = content.replace('.background(Color(0xFFF8F9FA))', '.background(AppColors.SubCardBackground)')
content = content.replace('.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))', '.border(1.dp, AppColors.BorderColor, RoundedCornerShape(12.dp))')

# The text inside Player IGN Details Box uses `AppColors.ScreenBackground` (White). That is fine for a dark card.
# The title match.title also uses `AppColors.ScreenBackground` (White). That is fine for a dark card.

# In EmptyMatchesCard: "You haven't joined any matches yet." text
content = content.replace('color = AppColors.TextPrimary', 'color = Color.Black')

with open("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "w") as f:
    f.write(content)

print("Fixed MatchesScreen details")
