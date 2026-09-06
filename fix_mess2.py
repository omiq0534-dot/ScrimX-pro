import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Fix TournamentDiscountCard
def fix_tourn(match):
    text = match.group(0)
    text = text.replace('("${plan.operator} PACK - ${plan.validity}").uppercase()', 'item.title.uppercase()')
    text = text.replace('"${plan.coinPrice} COINS"', '"$effectivePrice COINS"')
    return text

content = re.sub(r'fun TournamentDiscountCard.*?\{.*?(?=\n@Composable|\n// ---|\Z)', fix_tourn, content, flags=re.MULTILINE|re.DOTALL)

# Let's check FamPayGooglePlayCard
def fix_fampay(match):
    text = match.group(0)
    text = text.replace('("${plan.operator} PACK - ${plan.validity}").uppercase()', 'item.title.uppercase()')
    text = text.replace('"${plan.coinPrice} COINS"', '"$effectivePrice COINS"')
    return text

content = re.sub(r'fun FamPayGooglePlayCard.*?\{.*?(?=\n@Composable|\n// ---|\Z)', fix_fampay, content, flags=re.MULTILINE|re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)

