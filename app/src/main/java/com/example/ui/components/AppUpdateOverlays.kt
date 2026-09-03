package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.screens.AppControlConfig

@Composable
fun AppUpdateDialog(
    config: AppControlConfig,
    installedVersionCode: Int = com.example.BuildConfig.VERSION_CODE,
    installedVersionName: String = com.example.BuildConfig.VERSION_NAME,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val canDismiss = config.allowLaterButton && !config.isForceUpdate

    Dialog(
        onDismissRequest = {
            if (canDismiss) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141622))
                .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Icon with pulsing glow
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (config.isForceUpdate || !config.allowLaterButton) "CRITICAL UPDATE REQUIRED" else "NEW UPDATE AVAILABLE",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 17.sp,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Please update ScrimX to continue playing",
                        color = Color(0xFF8E92A4),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Version comparison pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B1E2D))
                        .border(1.dp, Color(0xFF2C324B), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "YOUR INSTALLED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E92A4)
                        )
                        Text(
                            "v$installedVersionName (Build $installedVersionCode)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF8A80)
                        )
                    }

                    Text("➔", color = Color(0xFF00E5FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "NEW AVAILABLE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E92A4)
                        )
                        Text(
                            "v${config.latestVersionName} (Build ${config.latestVersionCode})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }
                }

                // Changelog Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0C0D12))
                        .border(1.dp, Color(0xFF262938), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "WHAT'S NEW",
                            color = Color(0xFF8E92A4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            config.whatsNew.ifBlank { "• Performance fixes & smooth room access" },
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val url = config.apkDownloadUrl.trim()
                            if (url.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open download link", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Download link being generated by admin...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "UPDATE & DOWNLOAD NOW",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    // Only show "Later" button if Admin explicitly allowed it in Admin Panel
                    if (config.allowLaterButton && !config.isForceUpdate) {
                        TextButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Later / Skip for now", color = Color(0xFF8E92A4), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserBannedLockScreen(
    banType: String,
    banReason: String,
    banUntil: Long,
    onLogout: () -> Unit
) {
    val isTemp = banType == "temporary"
    val remainingHours = remember(banUntil) {
        val diff = banUntil - System.currentTimeMillis()
        if (diff > 0) (diff / (1000 * 3600)).toInt() + 1 else 0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0C10))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF0055).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFFFF0055), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Block,
                    contentDescription = null,
                    tint = Color(0xFFFF3366),
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                if (isTemp) "ACCOUNT TEMPORARILY SUSPENDED" else "ACCOUNT PERMANENTLY BANNED",
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 17.sp,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161822))
                    .border(1.dp, Color(0xFFFF3366).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "SUSPENSION REASON",
                        color = Color(0xFFFF8A80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        banReason.ifBlank { "Violation of tournament fair-play policy or unauthorized modified APK usage." },
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    if (isTemp && remainingHours > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "⏱️ Ban expires in approximately: $remainingHours hour(s)",
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262938))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOGOUT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ServerMaintenanceScreen(
    message: String,
    onRefresh: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "server_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090A0F))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size((90 * scale).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFFFF9800), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                "SERVER MAINTENANCE",
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = 20.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141622))
                    .border(1.dp, Color(0xFF262938), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Text(
                    message.ifBlank { "We are upgrading servers for better latency and faster match rooms. Please check back shortly!" },
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = onRefresh,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("REFRESH STATUS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun LiveAnnouncementMarquee(
    notice: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF881337), Color(0xFF4C0519), Color(0xFF1E1B4B))
                )
            )
            .border(1.dp, Color(0xFFF43F5E).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFF0055))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "LIVE",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                notice,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AdminVipTopBanner(
    text: String,
    onAdminClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
            .clickable { onAdminClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF60A5FA))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text,
                    color = Color(0xFF93C5FD),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }
            Text(
                "OPEN HQ →",
                color = Color(0xFF60A5FA),
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
            )
        }
    }
}
