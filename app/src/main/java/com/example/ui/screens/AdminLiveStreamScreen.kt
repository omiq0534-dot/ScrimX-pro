package com.example.ui.screens

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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveStreamScreen(
    navController: NavController,
    matchesViewModel: MatchesViewModel = viewModel()
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val matches by matchesViewModel.matches.collectAsState()

    var globalLiveUrl by remember { mutableStateOf("") }
    var streamTitle by remember { mutableStateOf("") }
    var isLiveBroadcastActive by remember { mutableStateOf(false) }
    var isSavingGlobal by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Match stream dialog
    var selectedMatchForStream by remember { mutableStateOf<MatchData?>(null) }
    var matchStreamUrl by remember { mutableStateOf("") }
    var isUpdatingMatchStream by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("settings").document("live_stream").get().await()
            if (doc.exists()) {
                globalLiveUrl = doc.getString("url") ?: ""
                streamTitle = doc.getString("title") ?: ""
                isLiveBroadcastActive = doc.getBoolean("isActive") ?: false
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    if (selectedMatchForStream != null) {
        AlertDialog(
            containerColor = Color(0xFF14161F),
            onDismissRequest = { selectedMatchForStream = null },
            title = {
                Text(
                    "ATTACH LIVE STREAM TO MATCH",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        selectedMatchForStream!!.title,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp
                    )
                    ClassyDarkInput(
                        value = matchStreamUrl,
                        onValueChange = { matchStreamUrl = it },
                        label = "YouTube / Twitch Live URL",
                        placeholder = "https://youtube.com/live/..."
                    )

                    if (matchStreamUrl.isNotBlank()) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(matchStreamUrl.trim()))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E212D)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Live Link in Browser/YT", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val matchId = selectedMatchForStream!!.id
                        isUpdatingMatchStream = true
                        scope.launch {
                            try {
                                db.collection("matches").document(matchId)
                                    .update(
                                        "liveUrl", matchStreamUrl.trim(),
                                        "status", if (matchStreamUrl.isNotBlank()) "Live" else selectedMatchForStream!!.status
                                    ).await()
                                selectedMatchForStream = null
                                statusMessage = "Match stream link updated!"
                            } catch (e: Exception) {
                                statusMessage = "Error updating stream link"
                            } finally {
                                isUpdatingMatchStream = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isUpdatingMatchStream
                ) {
                    if (isUpdatingMatchStream) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Save & Broadcast", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMatchForStream = null }) {
                    Text("Cancel", color = Color(0xFF75798E))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFF0C0D12),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GLOBAL LIVE STREAM HUB",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (statusMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF152E20))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(statusMessage!!, color = Color(0xFFB9F6CA), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Global Stream Broadcast Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF14161F))
                    .border(1.dp, Color(0xFF262938), RoundedCornerShape(18.dp))
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
                                    .background(Color(0xFF1A1D28))
                                    .border(1.dp, Color(0xFF2C3042), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Global Stream Link", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
                                Text("Shared broadcast shown on Home Screen", fontSize = 11.sp, color = Color(0xFF75798E))
                            }
                        }

                        Switch(
                            checked = isLiveBroadcastActive,
                            onCheckedChange = { isLiveBroadcastActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = Color(0xFF75798E),
                                uncheckedTrackColor = Color(0xFF1F2332)
                            )
                        )
                    }

                    ClassyDarkInput(
                        value = streamTitle,
                        onValueChange = { streamTitle = it },
                        label = "Broadcast Title / Tournament Name",
                        placeholder = "e.g. Daily Scrims Finals Live"
                    )

                    ClassyDarkInput(
                        value = globalLiveUrl,
                        onValueChange = { globalLiveUrl = it },
                        label = "YouTube / Stream URL",
                        placeholder = "https://youtube.com/live/..."
                    )

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
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2C3042)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                isSavingGlobal = true
                                scope.launch {
                                    try {
                                        db.collection("settings").document("live_stream")
                                            .set(
                                                mapOf(
                                                    "url" to globalLiveUrl.trim(),
                                                    "title" to streamTitle.trim(),
                                                    "isActive" to isLiveBroadcastActive,
                                                    "updatedAt" to System.currentTimeMillis()
                                                )
                                            ).await()
                                        statusMessage = "Global Stream Link Saved Successfully!"
                                    } catch (e: Exception) {
                                        statusMessage = "Error saving stream settings"
                                    } finally {
                                        isSavingGlobal = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.5f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
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

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
