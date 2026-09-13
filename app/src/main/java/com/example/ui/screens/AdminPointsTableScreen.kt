package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.FirebaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPointsTableScreen(
    matchId: String,
    navController: NavController,
    matchesViewModel: MatchesViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentMatch by matchesViewModel.currentMatch.collectAsState()
    val scope = rememberCoroutineScope()
    val db = remember { FirebaseHelper.getFirestore() }

    LaunchedEffect(matchId) {
        matchesViewModel.listenToMatchDetails(matchId)
    }

    DisposableEffect(Unit) {
        onDispose {
            matchesViewModel.clearMatchListener()
        }
    }

    if (currentMatch == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0D14)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF00E676))
        }
        return
    }

    val match = currentMatch!!

    var joinTime by remember(match) { mutableStateOf(match.joinTime) }
    var resultTime by remember(match) { mutableStateOf(match.resultTime) }
    var isResultDeclared by remember(match) { mutableStateOf(match.isResultDeclared) }
    var pointsTableImageUrl by remember(match) { mutableStateOf(match.pointsTableImageUrl) }
    var pointsTableNotes by remember(match) { mutableStateOf(match.pointsTableNotes) }
    var pointsTableRanks by remember(match) { mutableStateOf(match.pointsTableRanks) }
    var publicDelayMinutes by remember(match) { mutableStateOf(match.resultPublicDelayMinutes.toString()) }
    var matchStatus by remember(match) { mutableStateOf(match.status) }

    var isSaving by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Photo picker launcher for picking screenshot directly from phone gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isUploadingImage = true
                try {
                    val base64DataUrl = withContext(Dispatchers.IO) {
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(context.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                                decoder.isMutableRequired = true
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        }

                        // Resize if too large to fit in Firestore safely
                        val maxDimension = 1080
                        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
                            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
                        } else {
                            bitmap
                        }

                        val outputStream = ByteArrayOutputStream()
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                        val bytes = outputStream.toByteArray()
                        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                    }

                    pointsTableImageUrl = base64DataUrl
                    Toast.makeText(context, "Screenshot loaded from Gallery!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to load image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploadingImage = false
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF080B11),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                            )
                            Text(
                                "MANAGE TOURNAMENT RESULTS",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            match.title,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF080B11))
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
            // Section 1: Timers & Declaration
            Text("1. TIMERS & ACCESS WINDOW", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = joinTime,
                    onValueChange = { joinTime = it },
                    label = { Text("Join Time (e.g. 07:30 PM)", fontSize = 11.sp) },
                    placeholder = { Text("07:30 PM", color = Color(0xFF6B7280)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF242A38),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF121722),
                        unfocusedContainerColor = Color(0xFF121722)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = resultTime,
                    onValueChange = { resultTime = it },
                    label = { Text("Result Time (e.g. 08:45 PM)", fontSize = 11.sp) },
                    placeholder = { Text("08:45 PM", color = Color(0xFF6B7280)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF242A38),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF121722),
                        unfocusedContainerColor = Color(0xFF121722)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Quick Result Declaration Toggle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF121722))
                    .border(1.dp, if (isResultDeclared) Color(0xFF00E676) else Color(0xFF242A38), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Declare Results Instantly",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            "Switches match button to 'VIEW RESULTS' for all users immediately.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isResultDeclared,
                        onCheckedChange = { isResultDeclared = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E676),
                            checkedTrackColor = Color(0xFF00E676).copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Public Delay Window
            OutlinedTextField(
                value = publicDelayMinutes,
                onValueChange = { publicDelayMinutes = it },
                label = { Text("Public User Delay (Minutes)", fontSize = 11.sp) },
                supportingText = { Text("Joined players see results instantly. Non-joined users see results after this delay.", color = Color(0xFF94A3B8), fontSize = 10.5.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF242A38),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF121722),
                    unfocusedContainerColor = Color(0xFF121722)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Section 2: Screenshot Upload & URL
            Text("2. SCORECARD SCREENSHOT (GALLERY OR URL)", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)

            // Gallery Upload Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF121722))
                    .border(1.dp, Color(0xFF242A38), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pick from Gallery",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                "Upload actual match end-screenshot directly from phone",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isUploadingImage,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (isUploadingImage) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Select Photo", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }

                    // Image preview if selected/entered
                    if (pointsTableImageUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0A0D14))
                                .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = pointsTableImageUrl,
                                contentDescription = "Screenshot Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            IconButton(
                                onClick = { pointsTableImageUrl = "" },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = pointsTableImageUrl,
                onValueChange = { pointsTableImageUrl = it },
                label = { Text("Or Paste Screenshot Image URL", fontSize = 11.sp) },
                placeholder = { Text("https://i.imgur.com/example.png", color = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF242A38),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF121722),
                    unfocusedContainerColor = Color(0xFF121722)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Section 3: Structured Points Table / Standings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("3. STANDINGS & POINTS MATRIX", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)
                TextButton(
                    onClick = {
                        pointsTableRanks = "1 | Team Toxic | 14 Kills | ₹300\n2 | Mafia Gang | 9 Kills | ₹150\n3 | Black Shadows | 6 Kills | ₹50\n4 | Royal Esports | 4 Kills | ₹0\n5 | GodLike Clan | 3 Kills | ₹0"
                    }
                ) {
                    Text("+ Insert Template", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = pointsTableRanks,
                onValueChange = { pointsTableRanks = it },
                label = { Text("Format: Rank | Team | Kills | Prize (Points auto-calculated)", fontSize = 11.sp) },
                placeholder = { Text("1 | Team Toxic | 14 Kills | ₹300\n2 | Mafia Gang | 9 Kills | ₹150\n(Optional with custom points: 1 | Team | 14 Kills | 26 Pts | ₹300)", color = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                maxLines = 10,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF242A38),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF121722),
                    unfocusedContainerColor = Color(0xFF121722)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Section 4: Admin MVP / Notes
            Text("4. ADMIN SUMMARY & MVP HIGHLIGHTS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF8E92A4), letterSpacing = 1.2.sp)

            OutlinedTextField(
                value = pointsTableNotes,
                onValueChange = { pointsTableNotes = it },
                label = { Text("Match Notes / MVP / Highlights", fontSize = 11.sp) },
                placeholder = { Text("Booyah by Team Toxic! MVP: ToxicShadow with 8 kills. Winnings credited.", color = Color(0xFF6B7280)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E676),
                    unfocusedBorderColor = Color(0xFF242A38),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF121722),
                    unfocusedContainerColor = Color(0xFF121722)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save & Publish Button
            Button(
                onClick = {
                    isSaving = true
                    val delayVal = publicDelayMinutes.toIntOrNull() ?: 10
                    val updates = mapOf<String, Any>(
                        "joinTime" to joinTime.trim(),
                        "resultTime" to resultTime.trim(),
                        "isResultDeclared" to isResultDeclared,
                        "pointsTableImageUrl" to pointsTableImageUrl.trim(),
                        "pointsTableNotes" to pointsTableNotes.trim(),
                        "pointsTableRanks" to pointsTableRanks.trim(),
                        "resultPublicDelayMinutes" to delayVal,
                        "status" to if (isResultDeclared) "Completed" else matchStatus
                    )

                    scope.launch {
                        try {
                            db?.collection("matches")?.document(matchId)?.update(updates)
                            Toast.makeText(context, "Tournament Results & Points Table Updated!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error updating: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Publish, contentDescription = null, tint = Color.Black)
                        Text(
                            "SAVE & PUBLISH SCORECARD",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
