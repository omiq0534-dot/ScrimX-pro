package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FilterItem(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val count: Int? = null,
    val isLiveBadge: Boolean = false
)

@Composable
fun GlassFilterPills(
    filters: List<FilterItem>,
    selectedFilterId: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    ) {
        items(filters, key = { it.id }) { item ->
            val isSelected = item.id == selectedFilterId
            GlassFilterPill(
                item = item,
                isSelected = isSelected,
                onClick = { onFilterSelected(item.id) }
            )
        }
    }
}

@Composable
fun GlassFilterPill(
    item: FilterItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 550f),
        label = "pillScale"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 550f),
        label = "pillOffsetY"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else if (isSelected) 7.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 550f),
        label = "pillElevation"
    )

    // 3D Glass Pill Outer Container
    Box(
        modifier = Modifier
            .offset(y = offsetY)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(14.dp),
                spotColor = if (isSelected) Color(0xFF00E676).copy(alpha = if (isPressed) 0.2f else 0.45f)
                            else Color(0xFF000000).copy(alpha = 0.35f),
                ambientColor = Color(0xFF000000).copy(alpha = 0.70f)
            )
            // 3D Bottom Base Chassis Lip
            .background(
                color = if (isSelected) {
                    if (isPressed) Color(0xFF031006) else Color(0xFF062211)
                } else {
                    if (isPressed) Color(0xFF080B10) else Color(0xFF141A24)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .padding(bottom = if (isPressed) 0.dp else 2.dp)
    ) {
        // Face Glass Plate
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(13.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onClick() }
                    )
                }
                .background(
                    brush = if (isSelected) {
                        Brush.verticalGradient(
                            colors = if (isPressed) listOf(Color(0xFF041409), Color(0xFF082613))
                            else listOf(Color(0xFF0F3B20), Color(0xFF06180D))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = if (isPressed) listOf(Color(0xFF0A0D13), Color(0xFF0E131C))
                            else listOf(Color(0xFF171E2B), Color(0xFF0D121B))
                        )
                    },
                    shape = RoundedCornerShape(13.dp)
                )
                .border(
                    brush = if (isSelected) {
                        Brush.verticalGradient(
                            colors = if (isPressed) listOf(
                                Color(0xFF00E676).copy(alpha = 0.6f),
                                Color(0x33FFFFFF)
                            ) else listOf(
                                Color(0xFF80FFC0),                   // 3D Top bevel light sheen
                                Color(0xFF00E676),                   // Radiant neon green
                                Color(0x3300E676)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x40FFFFFF),                   // Subtle specular glass edge
                                Color(0x15FFFFFF),
                                Color(0x05FFFFFF)
                            )
                        )
                    },
                    width = if (isSelected) 1.4.dp else 1.1.dp,
                    shape = RoundedCornerShape(13.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.5.dp),
            contentAlignment = Alignment.Center
        ) {
            // Internal 3D Frosted Glass Glare Canvas
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Top Bevel Specular Sheen
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isSelected) 0.50f else 0.25f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, 1f),
                    end = Offset(w * 0.7f, 1f),
                    strokeWidth = 1.8f
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Live Dot or Custom Icon
                if (item.isLiveBadge) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30))
                    )
                } else if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF00E676) else Color(0xFF94A3B8),
                        modifier = Modifier.size(13.5.dp)
                    )
                }

                // Filter Label
                Text(
                    text = item.label,
                    color = if (isSelected) Color(0xFF00E676) else Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )

                // Count Badge (if present)
                if (item.count != null && item.count > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) Color(0xFF00E676).copy(alpha = 0.20f)
                                else Color(0x33FFFFFF)
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${item.count}",
                            color = if (isSelected) Color(0xFF00E676) else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
