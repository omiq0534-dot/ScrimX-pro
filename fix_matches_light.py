import os

def replace_in_file(filepath, old, new):
    if not os.path.exists(filepath): return
    with open(filepath, "r") as f:
        content = f.read()
    content = content.replace(old, new)
    with open(filepath, "w") as f:
        f.write(content)

# Top Tab Bar background
replace_in_file("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "Color(0xFF1E2130)", "Color.White")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "Color(0xFF33384D)", "Color(0xFFF3F4F6)")

# Room ID & Pass Container
replace_in_file("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "Color(0xFF14161F)", "Color.White")
replace_in_file("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "Color(0xFF262938)", "Color(0xFFF3F4F6)")

# Empty Matches Card Icon Box
replace_in_file("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "Color(0xFF1A1C24)", "Color(0xFFF3F4F6)")

# Wait, `AppColors.ScreenBackground` in `MatchesScreen.kt` was being used as the card's text color.
# Now `AppColors.TextPrimary` is `Color(0xFF111827)`. So we should replace `color = AppColors.ScreenBackground` with `color = AppColors.TextPrimary` where it's used for text.
replace_in_file("app/src/main/java/com/example/ui/screens/MatchesScreen.kt", "color = AppColors.ScreenBackground", "color = AppColors.TextPrimary")

print("Fixed MatchesScreen light theme")
