import re

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'r') as f:
    content = f.read()

# Check if haptic is used
if "import androidx.compose.ui.hapticfeedback.HapticFeedbackType" not in content:
    content = content.replace("import androidx.compose.ui.platform.LocalContext", "import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.platform.LocalHapticFeedback\nimport androidx.compose.ui.hapticfeedback.HapticFeedbackType")

# In PurchasedCardVaultItem, add haptic variable
vault_item_regex = r'val context = LocalContext\.current\n    val clipboardManager = context\.getSystemService\(Context\.CLIPBOARD_SERVICE\) as ClipboardManager'
vault_item_repl = 'val context = LocalContext.current\n    val haptic = LocalHapticFeedback.current\n    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager'
content = re.sub(vault_item_regex, vault_item_repl, content)

# Find the click handler for CLAIM button in Vault and add Haptic
# .clickable(enabled = card.code.isNotBlank()) {
#     if (card.code.isNotBlank()) {
#         if (!isRevealed) {
#             isRevealed = true
#             onMarkUsed() // Optional: Mark as used automatically when revealed
#         } else {
#             clipboardManager.setPrimaryClip(ClipData.newPlainText("Code", card.code))
#             Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
#         }
#     }
# }
click_regex = r'(\.clickable\(enabled = card\.code\.isNotBlank\(\)\) \{\s*if \(card\.code\.isNotBlank\(\)\) \{\s*if \(!isRevealed\) \{\s*isRevealed = true)'
click_repl = r'\1\n                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)'
content = re.sub(click_regex, click_repl, content)

with open('app/src/main/java/com/example/ui/screens/StoreScreen.kt', 'w') as f:
    f.write(content)
