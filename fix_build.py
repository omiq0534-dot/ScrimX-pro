import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_content = f.read()

import_collect = "import androidx.compose.runtime.collectAsState\n"
if "import androidx.compose.runtime.collectAsState" not in main_content:
    main_content = main_content.replace("import androidx.compose.runtime.CompositionLocalProvider", import_collect + "import androidx.compose.runtime.CompositionLocalProvider")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(main_content)

with open("app/src/main/java/com/example/ui/screens/ProfileScreen.kt", "r") as f:
    profile_content = f.read()

# There might be another SettingsList call?
# Let's see how many there are.
print(profile_content.count("SettingsList("))
