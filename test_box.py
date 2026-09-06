import re
with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if 'fun FamPayGooglePlayCard' in line:
        start = i
        for j in range(start, start + 250):
            if '// Store Purchase Interaction' in lines[j]:
                print(f"Store Purchase Interaction at {j}")
                print(lines[j-5:j+5])
                break
        break
