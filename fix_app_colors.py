with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "r") as f:
    content = f.read()

content = content.replace("@Composable @ReadOnlyComposable", "@Composable")

with open("app/src/main/java/com/example/ui/theme/AppColors.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    home_content = f.read()

home_content = home_content.replace(
    'val InfiniteTransition = rememberInfiniteTransition()',
    '@Composable\nval InfiniteTransition = rememberInfiniteTransition()'
)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(home_content)

print("AppColors and HomeScreen fixed.")
