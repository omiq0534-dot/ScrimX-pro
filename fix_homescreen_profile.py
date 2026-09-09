import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Fix nullable profile access
content = content.replace("profile.name", "(profile?.name ?: \"User\")")
content = content.replace("profile.hasXBadge", "(profile?.hasXBadge == true)")
content = content.replace("profile.uid", "(profile?.uid ?: \"000000\")")
content = content.replace("profile.coinBalance", "(profile?.coins ?: 0)")

# Fix XBadge size
content = content.replace("XBadge(size = XBadgeSize.SMALL)", "XBadge()")

# Fix .value on states since they are accessed via 'by' or just normal state without 'by'
content = content.replace("currentStreak.value", "currentStreak")
content = content.replace("spinsRemaining.value", "spinsRemaining")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

