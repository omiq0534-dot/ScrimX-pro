import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# Fix FamPayGooglePlayCard
old_fampay = """fun FamPayGooglePlayCard(
    val textPrimaryColor = AppColors.TextPrimary
    item: StoreItem,"""
new_fampay = """fun FamPayGooglePlayCard(
    item: StoreItem,"""
content = content.replace(old_fampay, new_fampay)

# Fix TournamentDiscountCard
old_tourney = """fun TournamentDiscountCard(
    val textPrimaryColor = AppColors.TextPrimary
    item: StoreItem,"""
new_tourney = """fun TournamentDiscountCard(
    item: StoreItem,"""
content = content.replace(old_tourney, new_tourney)

# Fix DataRechargeCard
old_data = """fun DataRechargeCard(
    val textPrimaryColor = AppColors.TextPrimary
    plan: DataPlan,"""
new_data = """fun DataRechargeCard(
    plan: DataPlan,"""
content = content.replace(old_data, new_data)

# Inject the val properly inside the function body
def inject_inside(func_start, content):
    # we find func_start, then find the corresponding '{'
    idx = content.find(func_start)
    if idx == -1: return content
    open_brace = content.find('{', idx)
    if open_brace != -1:
        return content[:open_brace+1] + "\n    val textPrimaryColor = AppColors.TextPrimary" + content[open_brace+1:]
    return content

content = inject_inside("fun FamPayGooglePlayCard(", content)
content = inject_inside("fun TournamentDiscountCard(", content)
content = inject_inside("fun DataRechargeCard(", content)

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)

