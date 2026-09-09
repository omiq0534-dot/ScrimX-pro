import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# Instead of passing the Composable property `AppColors.TextPrimary` in via `val textPrimaryColor = AppColors.TextPrimary`,
# which causes `Unresolved reference 'textPrimaryColor'`?
# Why is it unresolved? Probably because the injection point failed.
# Let's see the context of line 671.
