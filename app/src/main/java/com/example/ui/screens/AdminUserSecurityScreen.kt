package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.FirebaseHelper

data class BannedUserData(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val isBanned: Boolean = false,
    val banType: String = "none", // "none", "temporary", "permanent"
    val banReason: String = "",
    val banUntil: Long = 0L // timestamp in ms for temporary ban
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserSecurityScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }

    var usersList by remember { mutableStateOf<List<BannedUserData>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Ban Dialog State
    var selectedUserForBan by remember { mutableStateOf<BannedUserData?>(null) }
    var selectedBanType by remember { mutableStateOf("temporary") } // "temporary" or "permanent"
    var tempBanHours by remember { mutableStateOf("24") }
    var banReasonInput by remember { mutableStateOf("Using unauthorized outdated APK / Terms violation") }
    var isProcessingBan by remember { mutableStateOf(false) }

    fun loadUsers() {
        isLoading = true
        db?.collection("users")?.get()?.addOnSuccessListener { snap ->
            val list = snap.documents.mapNotNull { doc ->
                val uid = doc.id
                val email = doc.getString("email") ?: ""
                val name = doc.getString("name") ?: "Player"
                val isBanned = doc.getBoolean("isBanned") ?: false
                val banType = doc.getString("banType") ?: if (isBanned) "permanent" else "none"
                val banReason = doc.getString("banReason") ?: ""
                val banUntil = doc.getLong("banUntil") ?: 0L
                BannedUserData(
                    uid = uid,
                    email = email,
                    name = name,
                    isBanned = isBanned,
                    banType = banType,
                    banReason = banReason,
                    banUntil = banUntil
                )
            }
            usersList = list
            isLoading = false
        }?.addOnFailureListener {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadUsers()
    }

    fun applyBan(user: BannedUserData, banType: String, hours: Int, reason: String) {
        if (db == null) return
        isProcessingBan = true
        val banUntilTimestamp = if (banType == "temporary") {
            System.currentTimeMillis() + (hours * 3600 * 1000L)
        } else {
            0L
        }

        val updates = hashMapOf<String, Any>(
            "isBanned" to true,
            "banType" to banType,
            "banReason" to reason.trim(),
            "banUntil" to banUntilTimestamp
        )

        db.collection("users").document(user.uid).update(updates)
            .addOnSuccessListener {
                isProcessingBan = false
                selectedUserForBan = null
                Toast.makeText(context, "User ${user.name} banned successfully ($banType)", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                isProcessingBan = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun unbanUser(user: BannedUserData) {
        if (db == null) return
        db.collection("users").document(user.uid).update(
            mapOf(
                "isBanned" to false,
                "banType" to "none",
                "banReason" to "",
                "banUntil" to 0L
            )
        ).addOnSuccessListener {
            Toast.makeText(context, "User ${user.name} unbanned!", Toast.LENGTH_SHORT).show()
            loadUsers()
        }
    }

    val filteredList = remember(usersList, searchQuery) {
        if (searchQuery.isBlank()) usersList
        else usersList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.email.contains(searchQuery, ignoreCase = true) ||
            it.uid.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Color(0xFF0C0D12),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF3366))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "SECURITY & USER BAN CONTROL",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { loadUsers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0C0D12))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Security Info Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF3B0764).copy(alpha = 0.5f), Color(0xFF1E1B4B))
                        )
                    )
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("BAN & SECURITY ENFORCER", color = Color(0xFFDDD6FE), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        Text("Apply Temporary or Permanent bans to violators & unauthorized APK bypassers.", color = Color(0xFFC4B5FD), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by player name or email...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E92A4)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF3366),
                    unfocusedBorderColor = Color(0xFF262938),
                    focusedLabelColor = Color(0xFFFF3366),
                    unfocusedLabelColor = Color(0xFF8E92A4),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF14161F),
                    unfocusedContainerColor = Color(0xFF14161F)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF3366))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(filteredList, key = { it.uid }) { user ->
                        val isUserBanned = user.isBanned && (user.banType != "temporary" || user.banUntil > System.currentTimeMillis())
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isUserBanned) Color(0xFF260D12) else Color(0xFF14161F))
                                .border(
                                    1.dp,
                                    if (isUserBanned) Color(0xFFFF3366).copy(alpha = 0.6f) else Color(0xFF262938),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            user.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (isUserBanned) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFFF0055))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    if (user.banType == "temporary") "TEMP BAN" else "PERMANENT BAN",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF00E676).copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    "ACTIVE",
                                                    color = Color(0xFF00E676),
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        user.email.ifBlank { "No email (${user.uid.take(8)}...)" },
                                        color = Color(0xFF8E92A4),
                                        fontSize = 11.sp
                                    )
                                    if (isUserBanned && user.banReason.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Reason: ${user.banReason}",
                                            color = Color(0xFFFF8A80),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (isUserBanned) {
                                    Button(
                                        onClick = { unbanUser(user) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                                    ) {
                                        Text("UNBAN", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            selectedUserForBan = user
                                            selectedBanType = "temporary"
                                            tempBanHours = "24"
                                            banReasonInput = "Using unauthorized outdated APK / Terms violation"
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366))
                                    ) {
                                        Text("BAN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Ban Action Dialog
    if (selectedUserForBan != null) {
        val target = selectedUserForBan!!
        AlertDialog(
            onDismissRequest = { if (!isProcessingBan) selectedUserForBan = null },
            containerColor = Color(0xFF181B26),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFF3366))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("BAN PLAYER: ${target.name}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Select Ban Duration & Type:", color = Color(0xFFC4C8D8), fontSize = 12.sp)

                    // Ban Type Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedBanType = "temporary" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedBanType == "temporary") Color(0xFFFF9800).copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedBanType == "temporary") Color(0xFFFF9800) else Color(0xFF33384C)
                            )
                        ) {
                            Text("TEMPORARY", color = if (selectedBanType == "temporary") Color(0xFFFFB74D) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { selectedBanType = "permanent" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedBanType == "permanent") Color(0xFFFF0055).copy(alpha = 0.2f) else Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedBanType == "permanent") Color(0xFFFF0055) else Color(0xFF33384C)
                            )
                        ) {
                            Text("PERMANENT", color = if (selectedBanType == "permanent") Color(0xFFFF5252) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    if (selectedBanType == "temporary") {
                        OutlinedTextField(
                            value = tempBanHours,
                            onValueChange = { tempBanHours = it },
                            label = { Text("Ban Duration (in Hours)") },
                            placeholder = { Text("24") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9800),
                                unfocusedBorderColor = Color(0xFF33384C),
                                focusedLabelColor = Color(0xFFFF9800),
                                unfocusedLabelColor = Color(0xFF8E92A4),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = banReasonInput,
                        onValueChange = { banReasonInput = it },
                        label = { Text("Ban Reason (Shown to player)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF3366),
                            unfocusedBorderColor = Color(0xFF33384C),
                            focusedLabelColor = Color(0xFFFF3366),
                            unfocusedLabelColor = Color(0xFF8E92A4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = tempBanHours.toIntOrNull() ?: 24
                        applyBan(target, selectedBanType, hours, banReasonInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedBanType == "permanent") Color(0xFFFF0055) else Color(0xFFFF9800)),
                    enabled = !isProcessingBan
                ) {
                    Text("CONFIRM BAN", color = Color.White, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForBan = null }, enabled = !isProcessingBan) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }
}
