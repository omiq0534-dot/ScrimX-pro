import re

with open('app/src/main/java/com/example/ui/screens/AdminStoreCodesScreen.kt', 'r') as f:
    content = f.read()

# Add Data Class for Requests
data_class = """data class RestockRequest(
    val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val timestamp: Long = 0L
)

data class AdminStoreStockItem("""
content = content.replace('data class AdminStoreStockItem(', data_class)

# Add state for restock requests
state_vars = """    var stockDataMap by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }
    var restockRequests by remember { mutableStateOf<List<RestockRequest>>(emptyList()) }"""
content = content.replace('    var stockDataMap by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }', state_vars)

# Add snapshot listener for restock requests
listener_code = """
        db?.collection("store_settings")?.addSnapshotListener { snapshot, error ->
            if (error != null) {
                android.util.Log.e("AdminStoreCodes", "Error listening to store settings: ${error.message}")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val map = mutableMapOf<String, Map<String, Any>>()
                for (doc in snapshot.documents) {
                    map[doc.id] = doc.data ?: emptyMap()
                }
                stockDataMap = map
            }
        }
        
        db?.collection("store_restock_requests")
            ?.whereEqualTo("status", "PENDING")
            ?.addSnapshotListener { snapshot, error ->
                if (snapshot != null) {
                    val requests = snapshot.documents.mapNotNull {
                        it.toObject(RestockRequest::class.java)?.copy(id = it.id)
                    }.sortedBy { it.timestamp }
                    restockRequests = requests
                }
            }
"""
# Need to find the exact place to replace. It's inside LaunchedEffect.
# Wait, currently it's just adding the snapshot listener directly. Let's see the context.
