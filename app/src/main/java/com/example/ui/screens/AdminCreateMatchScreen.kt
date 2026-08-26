package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.FirebaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateMatchScreen(navController: NavController) {
    val db = remember { FirebaseHelper.getFirestore() }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var customMap by remember { mutableStateOf("Bermuda") }
    var selectedMode by remember { mutableStateOf("Squad") }
    var customSlots by remember { mutableStateOf("12") }
    var time by remember { mutableStateOf("") }
    var prize by remember { mutableStateOf("₹500") }
    var entry by remember { mutableStateOf("₹10") }
    var badge by remember { mutableStateOf("FREE FIRE T3") }
    var status by remember { mutableStateOf("Upcoming") }
    var roomId by remember { mutableStateOf("") }
    var roomPass by remember { mutableStateOf("") }
    var liveUrl by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val commonMapSuggestions = listOf("Bermuda", "Purgatory", "Kalahari", "Alpine", "Erangel", "Miramar")
    val modes = listOf(
        "Solo" to 48,
        "Duo" to 24,
        "Squad" to 12
    )

    Scaffold(
        containerColor = Color(0xFF0C0D12),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CREATE TOURNAMENT MATCH",
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
            // Mode Selector & Slot auto-set
            Text("MATCH MODE & SLOTS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF75798E), letterSpacing = 1.5.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                modes.forEach { (mode, defaultSlots) ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF1F2332) else Color(0xFF14161F))
                            .border(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF262938), RoundedCornerShape(14.dp))
                            .clickable {
                                selectedMode = mode
                                customSlots = defaultSlots.toString()
                                if (title.isBlank() || title.contains("Scrims", true)) {
                                    title = "$customMap $mode Scrims"
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                mode,
                                color = if (isSelected) Color(0xFFFFD700) else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                "$defaultSlots Slots",
                                color = Color(0xFF8E92A4),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Custom Map Name Input + Suggestions
            ClassyDarkInput(
                value = customMap,
                onValueChange = { 
                    customMap = it
                    if (title.isBlank() || title.contains("Scrims", true)) {
                        title = "$customMap $selectedMode Scrims"
                    }
                },
                label = "Map Name (Custom: e.g. Bermuda / Kalahari)",
                placeholder = "Type custom map name..."
            )

            // Quick Map Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                commonMapSuggestions.take(4).forEach { mapSug ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A1D28))
                            .border(1.dp, Color(0xFF2C3042), RoundedCornerShape(8.dp))
                            .clickable {
                                customMap = mapSug
                                title = "$mapSug $selectedMode Scrims"
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(mapSug, color = Color(0xFFC0C4D6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Title & Total Slots Row
            ClassyDarkInput(
                value = title,
                onValueChange = { title = it },
                label = "Match Title",
                placeholder = "e.g. Bermuda Squad Scrims #1"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = time,
                        onValueChange = { time = it },
                        label = "Time & Date",
                        placeholder = "e.g. Today 08:30 PM"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = customSlots,
                        onValueChange = { customSlots = it },
                        label = "Total Slots",
                        placeholder = "12 or 48"
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = prize,
                        onValueChange = { prize = it },
                        label = "Prize Pool",
                        placeholder = "₹500"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = entry,
                        onValueChange = { entry = it },
                        label = "Entry Fee",
                        placeholder = "₹10 / Free"
                    )
                }
            }

            ClassyDarkInput(
                value = badge,
                onValueChange = { badge = it },
                label = "Tournament Badge / Tier",
                placeholder = "e.g. FREE FIRE T3 / BGMI PRO"
            )

            // Live Stream link for this match
            ClassyDarkInput(
                value = liveUrl,
                onValueChange = { liveUrl = it },
                label = "Match Live Stream URL (Optional)",
                placeholder = "https://youtube.com/live/..."
            )

            // Room Credentials
            Text("ROOM CREDENTIALS (Can update later)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF75798E), letterSpacing = 1.5.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = roomId,
                        onValueChange = { roomId = it },
                        label = "Room ID",
                        placeholder = "e.g. 5892144"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = roomPass,
                        onValueChange = { roomPass = it },
                        label = "Room Password",
                        placeholder = "e.g. 1234"
                    )
                }
            }

            // Match Status
            Text("INITIAL STATUS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF75798E), letterSpacing = 1.5.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Upcoming", "Live", "Completed").forEach { st ->
                    val isSelected = status == st
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1F2332) else Color(0xFF14161F))
                            .border(1.dp, if (isSelected) Color.White else Color(0xFF262938), RoundedCornerShape(12.dp))
                            .clickable { status = st }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            st,
                            color = if (isSelected) Color.White else Color(0xFF8E92A4),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        val newId = UUID.randomUUID().toString()
                        val calculatedSlots = customSlots.toIntOrNull() ?: when (selectedMode) {
                            "Solo" -> 48
                            "Duo" -> 24
                            else -> 12
                        }

                        val newMatch = MatchData(
                            id = newId,
                            title = if (title.isNotBlank()) title else "$customMap $selectedMode Scrims",
                            time = time,
                            prize = prize,
                            entry = entry,
                            badge = badge,
                            status = status,
                            roomId = roomId.trim(),
                            roomPass = roomPass.trim(),
                            liveUrl = liveUrl.trim(),
                            map = customMap.trim().ifBlank { "Bermuda" },
                            mode = selectedMode,
                            totalSlots = calculatedSlots,
                            bookedSlots = emptyMap(),
                            slotNames = emptyMap(),
                            slotUids = emptyMap()
                        )

                        if (db == null) {
                            isLoading = false
                            return@launch
                        }
                        db.collection("matches").document(newId).set(newMatch)
                            .addOnSuccessListener {
                                isLoading = false
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                isLoading = false
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading && time.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black)
                } else {
                    Icon(Icons.Default.Publish, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PUBLISH TOURNAMENT", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ClassyDarkInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFF75798E)) },
        placeholder = { Text(placeholder, color = Color(0xFF3E4254)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFFFD700),
            unfocusedBorderColor = Color(0xFF262938),
            focusedContainerColor = Color(0xFF14161F),
            unfocusedContainerColor = Color(0xFF14161F),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFFFFD700)
        ),
        singleLine = true
    )
}
