import re

with open('app/src/main/java/com/example/ui/screens/AdminStoreCodesScreen.kt', 'r') as f:
    content = f.read()

data_class = """
data class RestockRequest(
    val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val timestamp: Long = 0L
)

data class AdminStoreStockItem(
"""

content = content.replace('data class AdminStoreStockItem(', data_class)

# The state `var restockRequests` probably didn't get injected either. Let's check.
state_vars = """    var stockDataMap by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }
    var restockRequests by remember { mutableStateOf<List<RestockRequest>>(emptyList()) }"""
content = content.replace('    var stockDataMap by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }', state_vars)

with open('app/src/main/java/com/example/ui/screens/AdminStoreCodesScreen.kt', 'w') as f:
    f.write(content)
