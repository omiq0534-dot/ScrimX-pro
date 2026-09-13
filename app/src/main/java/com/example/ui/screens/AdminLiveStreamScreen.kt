package com.example.ui.screens
import com.example.ui.theme.AppColors

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import com.example.FirebaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveStreamScreen(
    navController: NavController,
    matchesViewModel: MatchesViewModel = viewModel()
) {
    val context = LocalContext.current
    val db = remember { FirebaseHelper.getFirestore() }
    val scope = rememberCoroutineScope()

    var globalLiveUrl by remember { mutableStateOf("") }
    var streamTitle by remember { mutableStateOf("") }
    var isLiveBroadcastActive by remember { mutableStateOf(false) }
    var isSavingGlobal by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val doc = db?.collection("settings")?.document("live_stream")?.get()?.await()
            if (doc != null && doc.exists()) {
                globalLiveUrl = doc.getString("url") ?: ""
                streamTitle = doc.getString("title") ?: ""
                isLiveBroadcastActive = doc.getBoolean("isActive") ?: false
            }
        } catch (e: Exception) {
            // Ignore
        }
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
                        Text(
                            "LIVE STREAM BROADCAST",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0F14))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (statusMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D2517))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(statusMessage!!, color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Global Stream Broadcast Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF141722))
                    .border(1.dp, Color(0xFF23293A), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1A1D2B))
                                    .border(1.dp, Color(0xFF23293A), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Global Stream Link", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                                Text("Broadcast shown on User Home Screen", fontSize = 11.sp, color = Color(0xFF8E92A4))
                            }
                        }

                        Switch(
                            checked = isLiveBroadcastActive,
                            onCheckedChange = { isLiveBroadcastActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color(0xFF00E676),
                                uncheckedThumbColor = Color(0xFF8E92A4),
                                uncheckedTrackColor = Color(0xFF1A1D2B)
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Broadcast Title / Tournament Name", color = Color(0xFF8E92A4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = streamTitle,
                            onValueChange = { streamTitle = it },
                            placeholder = { Text("e.g. Daily Scrims Finals Live", color = Color(0xFF75798E)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF23293A),
                                focusedContainerColor = Color(0xFF1A1D2B),
                                unfocusedContainerColor = Color(0xFF1A1D2B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("YouTube / Stream URL", color = Color(0xFF8E92A4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = globalLiveUrl,
                            onValueChange = { globalLiveUrl = it },
                            placeholder = { Text("https://youtube.com/live/...", color = Color(0xFF75798E)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF23293A),
                                focusedContainerColor = Color(0xFF1A1D2B),
                                unfocusedContainerColor = Color(0xFF1A1D2B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (globalLiveUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(globalLiveUrl.trim()))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23293A)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                isSavingGlobal = true
                                scope.launch {
                                    try {
                                        db?.collection("settings")?.document("live_stream")
                                            ?.set(
                                                mapOf(
                                                    "url" to globalLiveUrl.trim(),
                                                    "title" to streamTitle.trim(),
                                                    "isActive" to isLiveBroadcastActive,
                                                    "updatedAt" to System.currentTimeMillis()
                                                )
                                            )?.await()
                                        statusMessage = "Live Stream Broadcast Saved!"
                                    } catch (e: Exception) {
                                        statusMessage = "Error saving stream settings"
                                    } finally {
                                        isSavingGlobal = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.5f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isSavingGlobal
                        ) {
                            if (isSavingGlobal) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                            } else {
                                Text("Save Broadcast", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
