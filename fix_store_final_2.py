import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

funcs = [
    "fun FamPayGooglePlayCard(",
    "fun TournamentDiscountCard(",
    "fun DataRechargeCard("
]

for func in funcs:
    content = content.replace(
        func,
        func + "\n    val textPrimaryColor = AppColors.TextPrimary"
    )

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)

