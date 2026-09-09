import re

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "r") as f:
    content = f.read()

# I see what happened. It was `plan: DataRechargePlan`, not `DataPlan`.
# I will replace `val textPrimaryColor = AppColors.TextPrimary\n    plan: DataRechargePlan,` with `plan: DataRechargePlan,`

content = content.replace(
    "val textPrimaryColor = AppColors.TextPrimary\n    plan: DataRechargePlan,",
    "plan: DataRechargePlan,"
)

with open("app/src/main/java/com/example/ui/screens/StoreScreen.kt", "w") as f:
    f.write(content)

