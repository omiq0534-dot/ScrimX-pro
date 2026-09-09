with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("profile.coinBalance", "profile.appMoney")
content = content.replace("profile?.coinBalance ?: 0", "profile?.appMoney ?: 0")
content = content.replace("profile.coins", "profile.appMoney")
content = content.replace("profile?.coins ?: 0", "profile?.appMoney ?: 0")
content = content.replace("coinBalance", "appMoney")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
