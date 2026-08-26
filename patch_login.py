import re

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'r') as f:
    content = f.read()

# 1. Add imports
imports = """import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.rotate
"""
if "import androidx.compose.animation.core.*" not in content:
    content = content.replace("import androidx.compose.foundation.background", imports + "import androidx.compose.foundation.background")


# 2. Change background to White
content = content.replace(
"""    // White/Black Theme Background with a slight gradient for the glass to pop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Black)
                )
            ),""",
"""    val infiniteTransition = rememberInfiniteTransition(label = "login_glow_transition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "login_glow_angle"
    )

    val animatedSweepBrush = Brush.sweepGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent,
            Color.Black.copy(alpha = 0.1f),
            Color.Black.copy(alpha = 0.9f),
            Color.Black.copy(alpha = 0.1f),
            Color.Transparent,
            Color.Transparent
        )
    )
    
    val staticSweepBrush = Brush.sweepGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.6f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.6f),
            Color.Transparent
        )
    )

    // White Theme Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),"""
)

# 3. Update the container Box
content = content.replace(
"""        // Frosted Glass Container
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.6f))
                .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(32.dp))
                .padding(24.dp),""",
"""        // Card Container
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .border(1.dp, Color.Black, RoundedCornerShape(32.dp))
                .padding(24.dp),"""
)

# 4. Update the Login Button
login_btn_original = """            // Pill Shape Login/Register Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) return@Button
                    
                    if (isLogin) {
                        authViewModel.login(email, password)
                    } else {
                        authViewModel.register(name, email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = CircleShape
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isLogin) "LOGIN" else "REGISTER", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp, 
                        letterSpacing = 1.sp
                    )
                }
            }"""

login_btn_new = """            // Pill Shape Login/Register Button with Animated Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        rotate(angle) {
                            drawCircle(brush = animatedSweepBrush, radius = size.width)
                        }
                    }
                    .padding(2.dp)
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
                            fontWeight = FontWeight.Bold, 
                            fontSize = 16.sp, 
                            letterSpacing = 1.sp
                        )
                    }
                }
            }"""

content = content.replace(login_btn_original, login_btn_new)

# 5. Update Google Button
google_btn_original = """                // Google Button
                Button(
                    onClick = {
                        authViewModel.loginWithGoogle(context)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD)),
                    shape = CircleShape
                ) {
                    Text("Google", fontWeight = FontWeight.Bold)
                }"""

google_btn_new = """                // Google Button with Static Glow
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(CircleShape)
                        .drawBehind {
                            drawCircle(brush = staticSweepBrush, radius = size.width)
                        }
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Button(
                        onClick = {
                            authViewModel.loginWithGoogle(context)
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Google", fontWeight = FontWeight.Bold)
                    }
                }"""

content = content.replace(google_btn_original, google_btn_new)

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'w') as f:
    f.write(content)

print("Patch applied successfully!")
