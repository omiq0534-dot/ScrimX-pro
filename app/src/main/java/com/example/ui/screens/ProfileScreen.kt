package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userViewModel: UserViewModel = viewModel()) {
    val profile by userViewModel.profile.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out of your account?") },
            confirmButton = {
                Button(
                    onClick = { 
                        showLogoutDialog = false
                        userViewModel.logout()
                        navController.navigate("login") { 
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes, Log Out", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Game Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    label = { Text("Enter your name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (editNameText.isNotBlank()) {
                        userViewModel.updateName(editNameText)
                    }
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Black, fontSize = 26.sp, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAFAFA),
                    scrolledContainerColor = Color(0xFFFAFAFA)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ProfileHeader(profile?.name ?: "Loading...", profile?.email ?: "loading...")
            StatsCard()
            SettingsList(
                isAdmin = profile?.email == "omiq0534@gmail.com",
                onAdminClick = {
                    navController.navigate("admin_dashboard")
                },
                onEditProfileClick = {
                    editNameText = profile?.name ?: ""
                    showEditDialog = true
                },
                onLogoutClick = {
                    showLogoutDialog = true
                }
            )
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProfileHeader(name: String, email: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Avatar", tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(name, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(email, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatsCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(title = "Matches", value = "42")
        HorizontalDivider(color = Color.Black, modifier = Modifier.height(40.dp).width(1.dp))
        StatItem(title = "Wins", value = "15")
        HorizontalDivider(color = Color.Black, modifier = Modifier.height(40.dp).width(1.dp))
        StatItem(title = "Kills", value = "120")
    }
}

@Composable
fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsList(isAdmin: Boolean, onAdminClick: () -> Unit, onEditProfileClick: () -> Unit, onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .border(1.dp, Color.Black, RoundedCornerShape(28.dp))
            .padding(16.dp)
    ) {
        if (isAdmin) {
            SettingsRow(icon = Icons.Default.Security, title = "Admin Panel (Owner)", onClick = onAdminClick)
            HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
        }
        SettingsRow(icon = Icons.Default.Edit, title = "Edit Profile", onClick = onEditProfileClick)
        HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
        SettingsRow(icon = Icons.Default.Settings, title = "App Settings", onClick = { /* TODO */ })
        HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
        SettingsRow(icon = Icons.Default.Help, title = "Help & Support", onClick = { /* TODO */ })
        HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
        SettingsRow(icon = Icons.AutoMirrored.Filled.Logout, title = "Log Out", isDestructive = true, onClick = onLogoutClick)
    }
}

@Composable
fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isDestructive) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = if (isDestructive) Color(0xFFF44336) else Color.Black, 
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 16.sp, 
                color = if (isDestructive) Color(0xFFF44336) else Color.Black
            )
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Black)
    }
}
