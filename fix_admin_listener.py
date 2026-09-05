import re

with open('app/src/main/java/com/example/ui/screens/AdminStoreCodesScreen.kt', 'r') as f:
    content = f.read()

old_listener = """            if (snapshot != null) {
                val map = mutableMapOf<String, Map<String, Any>>()
                for (doc in snapshot.documents) {
                    map[doc.id] = doc.data ?: emptyMap()
                }
                stockDataMap = map
            }
        }"""

new_listener = """            if (snapshot != null) {
                val map = mutableMapOf<String, Map<String, Any>>()
                for (doc in snapshot.documents) {
                    map[doc.id] = doc.data ?: emptyMap()
                }
                stockDataMap = map
            }
        }
        db?.collection("store_restock_requests")?.whereEqualTo("status", "PENDING")?.addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                val reqs = snapshot.documents.mapNotNull { it.toObject(RestockRequest::class.java)?.copy(id = it.id) }
                restockRequests = reqs
            }
        }"""

content = content.replace(old_listener, new_listener)

with open('app/src/main/java/com/example/ui/screens/AdminStoreCodesScreen.kt', 'w') as f:
    f.write(content)
