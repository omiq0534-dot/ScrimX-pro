package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LegalDisclaimerDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Gavel,
                        contentDescription = "Legal",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        "Legal & Fair Play Policy",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                    Text(
                        "Indian Esports & Community Compliance",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegalSectionCard(
                    icon = Icons.Default.Shield,
                    iconTint = Color(0xFF00E676),
                    title = "1. 100% Game of Skill (Non-Gambling)",
                    description = "All tournaments organized on ScrimX are strictly Esports Skill-Based competitions (Free Fire, BGMI). Success depends entirely on player strategy, reaction time, and in-game mastery. We do NOT host or support betting, gambling, casino, or games of chance."
                )

                LegalSectionCard(
                    icon = Icons.Default.Security,
                    iconTint = Color(0xFFFFD700),
                    title = "2. Age & Geographic Restrictions",
                    description = "Participants in paid prize pools must be 18 years of age or older. Due to state legislation on real-money gaming, users residing in Assam, Odisha, Telangana, Andhra Pradesh, Nagaland, and Sikkim are strictly restricted to free-entry practice matches only."
                )

                LegalSectionCard(
                    icon = Icons.Default.CheckCircle,
                    iconTint = Color(0xFF00E5FF),
                    title = "3. Fair Play & Anti-Cheat Policy",
                    description = "Using third-party injectors, scripts, root exploits, team-up in solo tournaments, or modded APKs results in an immediate permanent hardware/device ban and forfeiture of all coins & earnings."
                )

                LegalSectionCard(
                    icon = Icons.Default.Info,
                    iconTint = Color(0xFFFF9100),
                    title = "4. Third-Party IP Disclaimer",
                    description = "ScrimX is an independent community tournament organizer. ScrimX is NOT affiliated with, sponsored by, or endorsed by Garena (Free Fire), Krafton (BGMI), or Google. All game trademarks belong to their respective copyright holders."
                )

                LegalSectionCard(
                    icon = Icons.Default.Gavel,
                    iconTint = Color(0xFFA855F7),
                    title = "5. Rewards & Withdrawal Terms",
                    description = "Coin rewards earned via community participation, watching sponsor video ads, and winning matches are distributed via genuine UPI. Users must maintain an authentic profile to receive verified payout settlements."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "I UNDERSTAND & AGREE",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun LegalSectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.6f))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                description,
                color = Color(0xFFCBD5E1),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}
