with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "r") as f:
    lines = f.readlines()

# find line 253 (index 252)
# insert "    } // End GlassScaffold\n" before it

lines.insert(252, "    } // End GlassScaffold\n")

with open("app/src/main/java/com/example/ui/screens/MainScreen.kt", "w") as f:
    f.writelines(lines)
