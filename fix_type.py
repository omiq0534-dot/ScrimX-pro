with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

bad_when = """    val (gradientColors, textColor, buttonColor, iconColor) = when (item.cardColorTheme) {
        "GOLD" -> listOf(
            listOf(Color(0xFFFFF9E6), Color(0xFFFBE49D), Color(0xFFE5B935), Color(0xFFC0931B)), // Gradient
            Color(0xFF452B05), // Text
            Color(0xFF2C1A02), // Button
            Color(0xFFE5B935)  // Icon accent
        )
        "DIAMOND" -> listOf(
            listOf(Color(0xFFF0FBFF), Color(0xFFD8F2FB), Color(0xFFA1E3F9), Color(0xFF4AC4E9)),
            Color(0xFF0C3852),
            Color(0xFF062336),
            Color(0xFF4AC4E9)
        )
        else -> listOf( // SILVER
            listOf(Color(0xFFF8F9FA), Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8)),
            Color(0xFF1E293B),
            Color(0xFF0F172A),
            Color(0xFF94A3B8)
        )
    }"""

good_when = """    data class ThemeColors(val gradient: List<Color>, val text: Color, val button: Color, val icon: Color)
    val theme = when (item.cardColorTheme) {
        "GOLD" -> ThemeColors(
            listOf(Color(0xFFFFF9E6), Color(0xFFFBE49D), Color(0xFFE5B935), Color(0xFFC0931B)),
            Color(0xFF452B05), Color(0xFF2C1A02), Color(0xFFE5B935)
        )
        "DIAMOND" -> ThemeColors(
            listOf(Color(0xFFF0FBFF), Color(0xFFD8F2FB), Color(0xFFA1E3F9), Color(0xFF4AC4E9)),
            Color(0xFF0C3852), Color(0xFF062336), Color(0xFF4AC4E9)
        )
        else -> ThemeColors( // SILVER
            listOf(Color(0xFFF8F9FA), Color(0xFFE2E8F0), Color(0xFFCBD5E1), Color(0xFF94A3B8)),
            Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF94A3B8)
        )
    }
    val gradientColors = theme.gradient
    val textColor = theme.text
    val buttonColor = theme.button
    val iconColor = theme.icon"""

content = content.replace(bad_when, good_when)

# Also fix `(textColor as Color)` or `(buttonColor as Color)` since they might be there
content = content.replace('(textColor as Color)', 'textColor')
content = content.replace('(buttonColor as Color)', 'buttonColor')
content = content.replace('(gradientColors as List<Color>)', 'gradientColors')

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
