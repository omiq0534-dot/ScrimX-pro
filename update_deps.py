with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

if "haze = " not in content:
    content = content.replace(
        "[versions]\n",
        "[versions]\nhaze = \"1.3.0\"\n"
    )
    content = content.replace(
        "[libraries]\n",
        "[libraries]\nhaze = { group = \"dev.chrisbanes.haze\", name = \"haze\", version.ref = \"haze\" }\nhaze-materials = { group = \"dev.chrisbanes.haze\", name = \"haze-materials\", version.ref = \"haze\" }\n"
    )

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

if "implementation(libs.haze)" not in content:
    content = content.replace(
        "implementation(libs.unity.ads)",
        "implementation(libs.unity.ads)\n  implementation(libs.haze)\n  implementation(libs.haze.materials)"
    )

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
