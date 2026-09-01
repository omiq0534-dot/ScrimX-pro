package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.R
import com.example.ui.components.LegalDisclaimerDialog

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf(false) }
    var resetEmailInput by remember { mutableStateOf("") }
    var resetStatusMessage by remember { mutableStateOf<String?>(null) }
    var isResetLoading by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "login_glow_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "login_glow_angle"
    )

    // Animated Rotating White Sweep Gradient Glowing Line
    val whiteGlowBrush = Brush.sweepGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent,
            Color.White,
            Color.White,
            Color.Transparent,
            Color.Transparent
        )
    )

    val buttonSweepBrush = Brush.sweepGradient(
        colors = listOf(
            Color.Transparent,
            Color.White,
            Color.Transparent,
            Color.White,
            Color.Transparent
        )
    )

    // White Screen Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // High-Contrast Deep Black Floating Card
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = Color(0x66000000),
                    ambientColor = Color(0x33000000)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF111319))
                .border(1.5.dp, Color(0xFF262A38), RoundedCornerShape(32.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo SCRIM X
            com.example.ui.components.ScrimXLogo(
                isDarkBackground = true,
                fontSize = 32.sp,
                showSubtext = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Toggle Login / Register
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Color(0xFF1E212D))
                    .border(1.dp, Color(0xFF2D3244), CircleShape)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (isLogin) Color.White else Color.Transparent)
                        .clickable {
                            isLogin = true
                            authViewModel.clearError()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Login",
                        color = if (isLogin) Color.Black else Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (!isLogin) Color.White else Color.Transparent)
                        .clickable {
                            isLogin = false
                            authViewModel.clearError()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Register",
                        color = if (!isLogin) Color.Black else Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                text = if (isLogin) "Welcome Back" else "Create Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isLogin) "Login to join Free Fire tournament scrims" else "Set your email and password to start",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )
            
            Spacer(modifier = Modifier.height(26.dp))

            if (!isLogin) {
                // Name Field for Registration
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    placeholder = { Text("Full Name / Gamer Tag", color = Color(0xFF6B7280)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1A1D27),
                        unfocusedContainerColor = Color(0xFF1A1D27),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF2E3346), CircleShape)
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Capsule TextField - Email
            TextField(
                value = email,
                onValueChange = { 
                    email = it.replace(" ", "").replace("\n", "").lowercase()
                    authViewModel.clearError()
                },
                singleLine = true,
                placeholder = { Text("Email Address", color = Color(0xFF6B7280)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.White) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1D27),
                    unfocusedContainerColor = Color(0xFF1A1D27),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3346), CircleShape)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Capsule TextField - Password
            TextField(
                value = password,
                onValueChange = { 
                    password = it.replace("\n", "")
                    authViewModel.clearError()
                },
                singleLine = true,
                placeholder = { Text("Password", color = Color(0xFF6B7280)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1A1D27),
                    unfocusedContainerColor = Color(0xFF1A1D27),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E3346), CircleShape)
            )

            if (isLogin) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot Password?",
                        color = Color(0xFF93C5FD),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                resetEmailInput = email
                                resetStatusMessage = null
                                showForgotPasswordDialog = true
                            }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Show Error Message
            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = Color(0xFFFF4D4D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // High-Contrast White Action Button (Black Text on White)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        rotate(angle) {
                            drawCircle(brush = buttonSweepBrush, radius = size.width)
                        }
                    }
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) return@Button
                        
                        if (isLogin) {
                            authViewModel.login(email, password)
                        } else {
                            authViewModel.register(name, email, password)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = CircleShape
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isLogin) "LOGIN" else "REGISTER", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 15.sp, 
                            letterSpacing = 1.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            
            Text(
                text = "OR CONTINUE WITH",
                fontSize = 11.sp,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            val context = androidx.compose.ui.platform.LocalContext.current

            // Google Button with Rotating 4-Color Glowing Border & Authentic Vector G
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color(0x4D4285F4),
                        ambientColor = Color(0x33000000)
                    )
                    .clip(CircleShape)
                    .drawBehind {
                        rotate(angle) {
                            drawCircle(brush = whiteGlowBrush, radius = size.width)
                        }
                    }
                    .padding(3.dp) // Thickness of glowing rotating border
                    .clip(CircleShape)
                    .background(Color(0xFF151822))
            ) {
                Button(
                    onClick = {
                        authViewModel.loginWithGoogle(context)
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF151822),
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Authentic 4-Color Google Logo
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "Google Logo",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Continue with Google",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 📜 Legal & Fair Play Disclaimer Footer Link
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showLegalDialog = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "By continuing, you accept our ",
                    fontSize = 10.5.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "Legal Terms & Fair Play Policy",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }
        }
    }

    if (showLegalDialog) {
        LegalDisclaimerDialog(
            onDismiss = { showLegalDialog = false }
        )
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    "Reset Password",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        "Enter your registered email address. We'll send you a link to reset your password.",
                        color = Color(0xFF9CA3AF),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TextField(
                        value = resetEmailInput,
                        onValueChange = { resetEmailInput = it.replace(" ", "").replace("\n", "").lowercase() },
                        singleLine = true,
                        placeholder = { Text("your-email@gmail.com", color = Color(0xFF6B7280)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1A1D27),
                            unfocusedContainerColor = Color(0xFF1A1D27),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (resetStatusMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = resetStatusMessage!!,
                            color = if (resetStatusMessage!!.startsWith("Password reset email sent")) Color(0xFF10B981) else Color(0xFFFF4D4D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetEmailInput.isBlank()) {
                            resetStatusMessage = "Please enter your email"
                            return@Button
                        }
                        isResetLoading = true
                        authViewModel.resetPassword(resetEmailInput) { success, msg ->
                            isResetLoading = false
                            resetStatusMessage = msg
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isResetLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Send Link", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Close", color = Color(0xFF9CA3AF))
                }
            },
            containerColor = Color(0xFF161922),
            shape = RoundedCornerShape(20.dp)
        )
    }
}

