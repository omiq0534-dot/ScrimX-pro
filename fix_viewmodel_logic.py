import re

with open('app/src/main/java/com/example/ui/screens/StoreViewModel.kt', 'r') as f:
    content = f.read()

# Add requestRestock function
new_func = """
    fun requestRestock(item: StoreItem, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = getAuth()?.currentUser ?: return
        val db = getDb() ?: return
        
        val requestRef = db.collection("store_restock_requests").document("${item.id}_${user.uid}")
        
        requestRef.set(
            mapOf(
                "itemId" to item.id,
                "itemTitle" to item.title,
                "userId" to user.uid,
                "userEmail" to user.email,
                "timestamp" to System.currentTimeMillis(),
                "status" to "PENDING"
            )
        ).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onError(it.localizedMessage ?: "Failed to send request.")
        }
    }
"""

# Insert requestRestock right before purchaseItem
content = content.replace('fun purchaseItem(', new_func + '\n    fun purchaseItem(')

# Change logic in purchaseItem to strictly require codes for Google Play
old_logic = """                val codesList = (settingsSnap.get("codes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (codesList.isNotEmpty()) {
                    finalCode = codesList.first()
                    val remainingCodes = codesList.drop(1)
                    tx.update(settingsDocRef, "codes", remainingCodes)
                }"""

new_logic = """                val codesList = (settingsSnap.get("codes") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (codesList.isNotEmpty()) {
                    finalCode = codesList.first()
                    val remainingCodes = codesList.drop(1)
                    tx.update(settingsDocRef, "codes", remainingCodes)
                } else if (item.category == StoreItemCategory.GOOGLE_PLAY) {
                    throw Exception("OUT_OF_STOCK")
                }"""

content = content.replace(old_logic, new_logic)

# In the catch block of runTransaction, if message is OUT_OF_STOCK, return a cleaner message
old_catch = """        } catch (e: Exception) {
            _isLoading.value = false
            onError(e.localizedMessage ?: "Purchase failed. Please try again.")
        }"""
new_catch = """        } catch (e: Exception) {
            _isLoading.value = false
            if (e.message == "OUT_OF_STOCK") {
                onError("Out of Stock! Please request Admin to add new codes.")
            } else {
                onError(e.localizedMessage ?: "Purchase failed. Please try again.")
            }
        }"""
content = content.replace(old_catch, new_catch)

# But wait, what if settingsSnap doesn't exist at all? Then the generated code is still used.
# Let's check where the generated code is generated.
old_gen = """        // Generate stylized Code
        val generatedCode = if (item.category == StoreItemCategory.GOOGLE_PLAY) {
            generateGooglePlayCode()
        } else {
            generateAppDiscountCode(item.cardColorTheme)
        }"""
new_gen = """        // Generate stylized Code
        val generatedCode = if (item.category == StoreItemCategory.GOOGLE_PLAY) {
            "" // Wait for real code inside transaction
        } else {
            generateAppDiscountCode(item.cardColorTheme)
        }"""
content = content.replace(old_gen, new_gen)

with open('app/src/main/java/com/example/ui/screens/StoreViewModel.kt', 'w') as f:
    f.write(content)
