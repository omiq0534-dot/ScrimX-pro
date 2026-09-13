package com.example.ui.screens
import com.example.ui.theme.AppColors

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.FirebaseHelper
import com.example.security.AppSecurityGuard
import com.example.ui.components.AdminMasterBadge

data class StaffMember(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "player", // "owner", "moderator", "player"
    val isModerator: Boolean = false,
    val totalMatches: Int = 0,
    val hasXBadge: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStaffManagementScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }

    var allUsers by remember { mutableStateOf<List<StaffMember>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog state for confirming add / remove moderator
    var userToPromote by remember { mutableStateOf<StaffMember?>(null) }
    var userToRevoke by remember { mutableStateOf<StaffMember?>(null) }
    var showManualAddDialog by remember { mutableStateOf(false) }
    var manualEmailInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (db != null) {
            db.collection("users").addSnapshotListener { snapshot, e ->
                isLoading = false
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val email = doc.getString("email") ?: ""
                        val name = doc.getString("name") ?: "Player"
                        val role = doc.getString("role") ?: "player"
                        val isMod = doc.getBoolean("isModerator") ?: false
                        val matches = doc.getLong("totalMatches")?.toInt() ?: 0
                        val xBadge = doc.getBoolean("hasXBadge") ?: false

                        StaffMember(
                            uid = doc.id,
                            name = name,
                            email = email,
                            role = role,
                            isModerator = isMod || role == "moderator",
                            totalMatches = matches,
                            hasXBadge = xBadge
                        )
                    }
                    allUsers = list
                }
            }
        }
    }

    val activeModerators = allUsers.filter { 
        (it.isModerator || it.role == "moderator") && !AppSecurityGuard.isSuperOwner(it.email)
    }

    val searchedUsers = if (searchQuery.isBlank()) emptyList() else {
        allUsers.filter { user ->
            (user.email.contains(searchQuery, ignoreCase = true) ||
             user.name.contains(searchQuery, ignoreCase = true) ||
             user.uid.contains(searchQuery, ignoreCase = true)) &&
            !AppSecurityGuard.isSuperOwner(user.email)
        }.take(8)
    }

    fun makeModerator(targetUser: StaffMember) {
        if (db == null) return
        db.collection("users").document(targetUser.uid)
            .update(
                mapOf(
                    "role" to "moderator",
                    "isModerator" to true
                )
            )
            .addOnSuccessListener {
                Toast.makeText(context, "${targetUser.name} promoted to Moderator", Toast.LENGTH_SHORT).show()
                userToPromote = null
                showManualAddDialog = false
                manualEmailInput = ""
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    fun revokeModerator(targetUser: StaffMember) {
        if (db == null) return
        db.collection("users").document(targetUser.uid)
            .update(
                mapOf(
                    "role" to "player",
                    "isModerator" to false
                )
            )
            .addOnSuccessListener {
                Toast.makeText(context, "Moderator role revoked for ${targetUser.name}", Toast.LENGTH_SHORT).show()
                userToRevoke = null
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    // Dialog: Confirm Make Moderator
    if (userToPromote != null) {
        val u = userToPromote!!
        AlertDialog(
            onDismissRequest = { userToPromote = null },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ASSIGN MODERATOR", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Promote ${u.name} (${u.email}) to Tournament Moderator?",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1A1D2B))
                            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Permissions Given:", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("• Create and manage match schedules", color = Color(0xFF8E92A4), fontSize = 11.sp)
                            Text("• Enter & release Room ID and Passwords", color = Color(0xFF8E92A4), fontSize = 11.sp)
                            Text("• Answer user support queries", color = Color(0xFF8E92A4), fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { makeModerator(u) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("GRANT ROLE", color = Color.Black, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToPromote = null }) {
                    Text("Cancel", color = Color(0xFF8E92A4))
                }
            }
        )
    }

    // Dialog: Confirm Revoke Moderator
    if (userToRevoke != null) {
        val u = userToRevoke!!
        AlertDialog(
            onDismissRequest = { userToRevoke = null },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("REVOKE MODERATOR", color = Color(0xFFFF5252), fontWeight = FontWeight.Black, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Remove moderator privileges for ${u.name} (${u.email})? They will become a standard player.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { revokeModerator(u) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("REVOKE", color = Color.White, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToRevoke = null }) {
                    Text("Cancel", color = Color(0xFF8E92A4))
                }
            }
        )
    }

    // Dialog: Manual Email Add
    if (showManualAddDialog) {
        AlertDialog(
            onDismissRequest = { showManualAddDialog = false },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD MODERATOR BY EMAIL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter the email address of the moderator:",
                        color = Color(0xFF8E92A4),
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = manualEmailInput,
                        onValueChange = { manualEmailInput = it },
                        placeholder = { Text("helper@gmail.com", color = Color(0xFF75798E)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF23293A),
                            focusedContainerColor = Color(0xFF1A1D2B),
                            unfocusedContainerColor = Color(0xFF1A1D2B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = manualEmailInput.trim().lowercase()
                        val matched = allUsers.firstOrNull { it.email.equals(trimmed, ignoreCase = true) }
                        if (matched != null) {
                            makeModerator(matched)
                        } else if (trimmed.isNotBlank()) {
                            db?.collection("users")?.whereEqualTo("email", trimmed)?.get()
                                ?.addOnSuccessListener { snap ->
                                    if (!snap.isEmpty) {
                                        val doc = snap.documents[0]
                                        db.collection("users").document(doc.id).update(
                                            mapOf("role" to "moderator", "isModerator" to true)
                                        ).addOnSuccessListener {
                                            Toast.makeText(context, "Added moderator successfully", Toast.LENGTH_SHORT).show()
                                            showManualAddDialog = false
                                            manualEmailInput = ""
                                        }
                                    } else {
                                        Toast.makeText(context, "No user found with this email", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ADD MOD", color = Color.Black, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualAddDialog = false }) {
                    Text("Cancel", color = Color(0xFF8E92A4))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0D0F14),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("STAFF & MODERATORS", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp, letterSpacing = 1.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showManualAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Mod", tint = Color(0xFF00E676))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0F14))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // Owner Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A1D2B))
                                .border(1.dp, Color(0xFF00E676), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Owner Account", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                AdminMasterBadge(isOwner = true, showClickInfo = false)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("omiq0534@gmail.com", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Full administrative authority to manage staff.", color = Color(0xFF8E92A4), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Quick Info & Role Capabilities
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF141722))
                        .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("MODERATOR CAPABILITIES", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        Text(
                            "Moderators can create tournaments, update Room ID & Passwords before matches start, and answer user queries.",
                            color = Color(0xFF8E92A4),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Search & Assign New Moderator Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SEARCH USERS", color = Color(0xFF8E92A4), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search player name, email, or UID...", color = Color(0xFF75798E), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00E676)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF8E92A4))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF23293A),
                            focusedContainerColor = Color(0xFF1A1D2B),
                            unfocusedContainerColor = Color(0xFF1A1D2B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF00E676)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Search Results List (if search query entered)
            if (searchQuery.isNotBlank()) {
                if (searchedUsers.isEmpty()) {
                    item {
                        Text("No matching players found.", color = Color(0xFF8E92A4), fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                } else {
                    items(searchedUsers) { user ->
                        val isAlreadyMod = user.isModerator || user.role == "moderator"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141722))
                                .border(1.dp, if (isAlreadyMod) Color(0xFF00E676) else Color(0xFF23293A), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (isAlreadyMod) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            AdminMasterBadge(isOwner = false, showClickInfo = false)
                                        }
                                    }
                                    Text(user.email, color = Color(0xFF8E92A4), fontSize = 11.sp)
                                }

                                if (isAlreadyMod) {
                                    Button(
                                        onClick = { userToRevoke = user },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1515)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("Remove Mod", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { userToPromote = user },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("+ Make Mod", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Current Active Moderators Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ACTIVE MODERATORS (${activeModerators.size})",
                        color = Color(0xFF8E92A4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00E676))
                    }
                }
            } else if (activeModerators.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF141722))
                            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color(0xFF8E92A4), modifier = Modifier.size(36.dp))
                            Text("No Active Moderators", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Search above or tap '+' to add moderators", color = Color(0xFF8E92A4), fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(activeModerators) { mod ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF141722))
                            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1A1D2B))
                                            .border(1.dp, Color(0xFF00E676), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(mod.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            AdminMasterBadge(isOwner = false, showClickInfo = true)
                                        }
                                        Text(mod.email, color = Color(0xFF8E92A4), fontSize = 11.sp)
                                    }
                                }

                                IconButton(onClick = { userToRevoke = mod }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Revoke", tint = Color(0xFFFF5252))
                                }
                            }

                            HorizontalDivider(color = Color(0xFF23293A))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Permissions: Match Rooms & Queries", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("Matches: ${mod.totalMatches}", color = Color(0xFF8E92A4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
