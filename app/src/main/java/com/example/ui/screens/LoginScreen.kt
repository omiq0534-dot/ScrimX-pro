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
                        .clickable { isLogin = true }
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
                        .clickable { isLogin = false }
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
                    placeholder = { Text("Full Name / Gamer Tag", color = Color(0xFF6B7280)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) },
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
                onValueChange = { email = it },
                placeholder = { Text("Email Address", color = Color(0xFF6B7280)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.White) },
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
                onValueChange = { password = it },
                placeholder = { Text("Password", color = Color(0xFF6B7280)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
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

            Spacer(modifier = Modifier.height(16.dp))

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
        }
    }
}

