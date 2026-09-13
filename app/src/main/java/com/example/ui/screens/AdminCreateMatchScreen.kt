package com.example.ui.screens
import com.example.ui.theme.AppColors

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
import kotlinx.coroutines.launch
import java.util.UUID

import com.example.FirebaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateMatchScreen(navController: NavController) {
    val db = remember { FirebaseHelper.getFirestore() }
    val scope = rememberCoroutineScope()

    var matchType by remember { mutableStateOf("BR") } // "BR", "CS", "TDM", "DUEL"
    var title by remember { mutableStateOf("") }
    var customMap by remember { mutableStateOf("Bermuda") }
    var selectedMode by remember { mutableStateOf("Squad") }
    var customSlots by remember { mutableStateOf("12") }
    var time by remember { mutableStateOf("") }
    var prize by remember { mutableStateOf("₹500") }
    var entryType by remember { mutableStateOf("PAID") } // "FREE", "AD", "PAID"
    var paidEntryFee by remember { mutableStateOf("10") }
    var requiredAdsInput by remember { mutableStateOf("1") }
    var badge by remember { mutableStateOf("FREE FIRE T3") }
    var status by remember { mutableStateOf("Upcoming") }
    var roomId by remember { mutableStateOf("") }
    var roomPass by remember { mutableStateOf("") }
    var liveUrl by remember { mutableStateOf("") }
    var joinTime by remember { mutableStateOf("") }
    var resultTime by remember { mutableStateOf("") }
    var customRules by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val brMapSuggestions = listOf("Bermuda", "Purgatory", "Kalahari", "Alpine", "Erangel", "Miramar")
    val csTdmMapSuggestions = listOf("Clash Squad Bermuda", "CS Kalahari", "TDM Warehouse", "CS Purgatory", "Custom Duel Arena")

    val matchTypeOptions = listOf(
        "BR" to "Battle Royale",
        "CS" to "Clash Squad",
        "TDM" to "TDM / Warehouse",
        "DUEL" to "1v1 Head-to-Head"
    )

    val quickRuleTemplates = listOf(
        "Standard Scrims (No Hacks/Emulators)",
        "Desert Eagle / Pistol Only • 13 Rounds",
        "Sniper Only • No Grenades • Limited Ammo: No",
        "Gun Property OFF • Character Skill OFF",
        "TDM M416 / AKM Only • 40 Kills Target"
    )

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
                            "CREATE TOURNAMENT MATCH",
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
            // Match Type Format
            Text("1. SELECT MATCH FORMAT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                matchTypeOptions.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOptions.forEach { (typeKey, label) ->
                            val isSelected = matchType == typeKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF0D2517) else Color(0xFF141722))
                                    .border(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF23293A), RoundedCornerShape(12.dp))
                                    .clickable {
                                        matchType = typeKey
                                        when (typeKey) {
                                            "BR" -> {
                                                selectedMode = "Squad"
                                                customSlots = "12"
                                                customMap = "Bermuda"
                                                customRules = "Standard Scrims (No Hacks/Emulators)"
                                                title = "Bermuda Squad Scrims"
                                            }
                                            "CS" -> {
                                                selectedMode = "4v4"
                                                customSlots = "8"
                                                customMap = "CS Bermuda"
                                                customRules = "13 Rounds • Gun Property OFF • Character Skill OFF"
                                                title = "Clash Squad 4v4 Face-off"
                                            }
                                            "TDM" -> {
                                                selectedMode = "4v4"
                                                customSlots = "8"
                                                customMap = "TDM Warehouse"
                                                customRules = "M416/AKM Only • 40 Kills • No Grenade"
                                                title = "TDM Warehouse 4v4 Battle"
                                            }
                                            "DUEL" -> {
                                                selectedMode = "1v1"
                                                customSlots = "2"
                                                customMap = "CS Duel"
                                                customRules = "1v1 Head-to-Head • Desert Eagle/Sniper Only • 7 Rounds"
                                                title = "1v1 Head-to-Head Duel"
                                            }
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (isSelected) Color(0xFF00E676) else Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Mode Selector based on matchType
            Text("2. MODE & SLOTS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)
            val availableModes = when (matchType) {
                "BR" -> listOf("Solo" to "48", "Duo" to "24", "Squad" to "12")
                "CS", "TDM" -> listOf("1v1" to "2", "2v2" to "4", "4v4" to "8")
                else -> listOf("1v1" to "2")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableModes.forEach { (mode, defaultSlots) ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF0D2517) else Color(0xFF141722))
                            .border(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF23293A), RoundedCornerShape(12.dp))
                            .clickable {
                                selectedMode = mode
                                customSlots = defaultSlots
                                title = "$customMap $mode Match"
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                mode,
                                color = if (isSelected) Color(0xFF00E676) else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                if (matchType == "CS" || matchType == "TDM" || matchType == "DUEL") "$defaultSlots Players" else "$defaultSlots Slots",
                                color = if (isSelected) Color(0xFF00E676) else Color(0xFF8E92A4),
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
                    if (title.isBlank() || title.contains("Match", true) || title.contains("Scrims", true)) {
                        title = "$customMap $selectedMode Match"
                    }
                },
                label = "Map Name",
                placeholder = "Type map name..."
            )

            // Quick Map Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = if (matchType == "BR") brMapSuggestions else csTdmMapSuggestions
                suggestions.take(4).forEach { mapSug ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF141722))
                            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(8.dp))
                            .clickable {
                                customMap = mapSug
                                title = "$mapSug $selectedMode Match"
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(mapSug, color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Title & Total Slots Row
            ClassyDarkInput(
                value = title,
                onValueChange = { title = it },
                label = "Match Title",
                placeholder = "e.g. Clash Squad 4v4 Face-off #1"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = time,
                        onValueChange = { time = it },
                        label = "Match Start Time",
                        placeholder = "e.g. 08:30 PM"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = customSlots,
                        onValueChange = { customSlots = it },
                        label = "Total Slots / Players",
                        placeholder = "2, 8, 12, 48"
                    )
                }
            }

            // Join Time & Result Time Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = joinTime,
                        onValueChange = { joinTime = it },
                        label = "Join Start Time (Timer)",
                        placeholder = "e.g. 08:00 PM"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ClassyDarkInput(
                        value = resultTime,
                        onValueChange = { resultTime = it },
                        label = "Result Time (Switch)",
                        placeholder = "e.g. 09:30 PM"
                    )
                }
            }

            // PRIZE & ENTRY SECTION
            Text("3. PRIZE POOL & ENTRY TYPE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)

            ClassyDarkInput(
                value = prize,
                onValueChange = { prize = it },
                label = "Prize Pool",
                placeholder = "e.g. ₹500, ₹1000, ₹2500"
            )

            // 3 Entry Type Options
            val entryTypeOptions = listOf(
                Triple("FREE", "100% Free", "0 Coins • 0 Ads"),
                Triple("AD", "Watch Ad", "Free via Ads"),
                Triple("PAID", "Paid Entry", "Deduct Wallet")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                entryTypeOptions.forEach { (typeKey, titleLabel, subLabel) ->
                    val isSelected = entryType == typeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF0D2517) else Color(0xFF141722))
                            .border(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF23293A), RoundedCornerShape(12.dp))
                            .clickable { entryType = typeKey }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                titleLabel,
                                color = if (isSelected) Color(0xFF00E676) else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                subLabel,
                                color = if (isSelected) Color(0xFF00E676) else Color(0xFF8E92A4),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Sub-options based on Entry Type
            when (entryType) {
                "FREE" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0D2517))
                            .border(1.dp, Color(0xFF00E676), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "100% Free Match: Players can join directly with 0 coins and no ads.",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                "AD" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClassyDarkInput(
                            value = requiredAdsInput,
                            onValueChange = { requiredAdsInput = it },
                            label = "Number of Sponsor Ads to Watch",
                            placeholder = "e.g. 1, 2, 3, 5"
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1 Ad", "2 Ads", "3 Ads", "5 Ads").forEach { adPreset ->
                                val num = adPreset.filter { it.isDigit() }
                                val isPresetSel = requiredAdsInput == num
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isPresetSel) Color(0xFF0D2517) else Color(0xFF141722))
                                        .border(1.dp, if (isPresetSel) Color(0xFF00E676) else Color(0xFF23293A), RoundedCornerShape(8.dp))
                                    .clickable { requiredAdsInput = num }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(adPreset, color = if (isPresetSel) Color(0xFF00E676) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                "PAID" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClassyDarkInput(
                            value = paidEntryFee,
                            onValueChange = { paidEntryFee = it },
                            label = "Entry Fee (Coins)",
                            placeholder = "e.g. 10, 20, 50, 100"
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("5", "10", "20", "50", "100").forEach { feePreset ->
                                val isFeeSel = paidEntryFee == feePreset
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isFeeSel) Color(0xFF0D2517) else Color(0xFF141722))
                                        .border(1.dp, if (isFeeSel) Color(0xFF00E676) else Color(0xFF23293A), RoundedCornerShape(8.dp))
                                    .clickable { paidEntryFee = feePreset }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("₹$feePreset", color = if (isFeeSel) Color(0xFF00E676) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            ClassyDarkInput(
                value = badge,
                onValueChange = { badge = it },
                label = "Tournament Badge / Tier",
                placeholder = "e.g. FREE FIRE CS / BGMI TDM / T3"
            )

            // CUSTOM RULES FOR THIS MATCH
            Text("MATCH RULES", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)
            OutlinedTextField(
                value = customRules,
                onValueChange = { customRules = it },
                label = { Text("Custom Rules for this match", color = Color(0xFF8E92A4)) },
                placeholder = { Text("e.g. Desert Eagle Only, 13 Rounds, No Grenades...", color = Color(0xFF75798E)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF23293A),
                    focusedContainerColor = Color(0xFF1A1D2B),
                    unfocusedContainerColor = Color(0xFF1A1D2B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF00E676)
                ),
                minLines = 3,
                maxLines = 6
            )

            // Quick Rule Templates Chips
            Text("Quick Rule Templates:", fontSize = 10.sp, color = Color(0xFF8E92A4), fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                quickRuleTemplates.forEach { ruleTpl ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF141722))
                            .border(1.dp, Color(0xFF23293A), RoundedCornerShape(8.dp))
                            .clickable { customRules = ruleTpl }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(ruleTpl, color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            // Live Stream link for this match
            ClassyDarkInput(
                value = liveUrl,
                onValueChange = { liveUrl = it },
                label = "Match Live Stream URL (Optional)",
                placeholder = "https://youtube.com/live/..."
            )

            // Room Credentials
            Text("ROOM CREDENTIALS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)
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
                        label = "Password",
                        placeholder = "e.g. 1234"
                    )
                }
            }

            // Match Status
            Text("INITIAL STATUS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Upcoming", "Live", "Completed").forEach { st ->
                    val isSelected = status == st
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF0D2517) else Color(0xFF141722))
                            .border(1.dp, if (isSelected) Color(0xFF00E676) else Color(0xFF23293A), RoundedCornerShape(12.dp))
                            .clickable { status = st }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            st,
                            color = if (isSelected) Color(0xFF00E676) else Color.White,
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
                            "1v1" -> 2
                            "2v2" -> 4
                            "4v4" -> 8
                            "Solo" -> 48
                            "Duo" -> 24
                            else -> 12
                        }

                        val computedEntry = when (entryType) {
                            "FREE" -> "FREE"
                            "AD" -> {
                                val adsCount = requiredAdsInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                "Free ($adsCount Ad${if (adsCount > 1) "s" else ""})"
                            }
                            else -> "₹${paidEntryFee.trim().removePrefix("₹").ifBlank { "10" }}"
                        }
                        val computedRequiredAds = if (entryType == "AD") (requiredAdsInput.toIntOrNull()?.coerceAtLeast(1) ?: 1) else 0

                        val newMatch = MatchData(
                            id = newId,
                            title = if (title.isNotBlank()) title else "$customMap $selectedMode Match",
                            time = time,
                            prize = prize,
                            entry = computedEntry,
                            entryType = entryType,
                            requiredAds = computedRequiredAds,
                            badge = badge,
                            status = status,
                            roomId = roomId.trim(),
                            roomPass = roomPass.trim(),
                            liveUrl = liveUrl.trim(),
                            map = customMap.trim().ifBlank { "Bermuda" },
                            mode = selectedMode,
                            matchType = matchType,
                            rules = customRules.trim(),
                            totalSlots = calculatedSlots,
                            bookedSlots = emptyMap(),
                            slotNames = emptyMap(),
                            slotUids = emptyMap(),
                            joinTime = joinTime.trim(),
                            resultTime = resultTime.trim()
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
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading && time.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                } else {
                    Icon(Icons.Default.Publish, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PUBLISH TOURNAMENT", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Black, letterSpacing = 0.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
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
        label = { Text(label, color = Color(0xFF8E92A4)) },
        placeholder = { Text(placeholder, color = Color(0xFF75798E)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF00E676),
            unfocusedBorderColor = Color(0xFF23293A),
            focusedContainerColor = Color(0xFF1A1D2B),
            unfocusedContainerColor = Color(0xFF1A1D2B),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF00E676)
        ),
        singleLine = true
    )
}
