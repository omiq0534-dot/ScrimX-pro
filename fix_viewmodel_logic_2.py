import re

with open('app/src/main/java/com/example/ui/screens/StoreViewModel.kt', 'r') as f:
    content = f.read()

old_logic = """                val codesList = (settingsSnap.get("codes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (codesList.isNotEmpty()) {
                    finalCode = codesList.first()
                    val remainingCodes = codesList.drop(1)
                    tx.update(settingsDocRef, "codes", remainingCodes)
                } else if (item.category == StoreItemCategory.GOOGLE_PLAY) {
                    throw Exception("OUT_OF_STOCK")
                }
            }

            if (currentCoins < txnEffectivePrice) {"""

new_logic = """                val codesList = (settingsSnap.get("codes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (codesList.isNotEmpty()) {
                    finalCode = codesList.first()
                    val remainingCodes = codesList.drop(1)
                    tx.update(settingsDocRef, "codes", remainingCodes)
                } else if (item.category == StoreItemCategory.GOOGLE_PLAY) {
                    throw Exception("OUT_OF_STOCK")
                }
            } else {
                if (item.category == StoreItemCategory.GOOGLE_PLAY) {
                    throw Exception("OUT_OF_STOCK")
                }
            }

            if (currentCoins < txnEffectivePrice) {"""
content = content.replace(old_logic, new_logic)

with open('app/src/main/java/com/example/ui/screens/StoreViewModel.kt', 'w') as f:
    f.write(content)

