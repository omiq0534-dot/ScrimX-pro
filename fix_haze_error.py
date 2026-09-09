import re

with open("app/src/main/java/com/example/ui/theme/GlassTheme.kt", "r") as f:
    content = f.read()

# Make sure HazeMaterials is used correctly (it might need the right import depending on version, 
# but 1.3.0 has dev.chrisbanes.haze.materials.HazeMaterials)

