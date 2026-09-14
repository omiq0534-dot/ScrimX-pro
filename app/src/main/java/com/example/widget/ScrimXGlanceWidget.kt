package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.R

class ToggleBalanceVisibilityAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val prefs = context.getSharedPreferences("scrimx_widget_prefs", Context.MODE_PRIVATE)
        val currentHidden = prefs.getBoolean("widget_balance_hidden", false)
        prefs.edit().putBoolean("widget_balance_hidden", !currentHidden).commit()
        try {
            ScrimXGlanceWidget().update(context, glanceId)
        } catch (_: Exception) {}
        try {
            ScrimXGlanceWidget().updateAll(context)
        } catch (_: Exception) {}
    }
}

val TargetKey = ActionParameters.Key<String>("target_tab")

class NavigateToAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val target = parameters[TargetKey] ?: "home_tab"
        MainActivity.widgetNavTarget.value = target
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", target)
        }
        context.startActivity(intent)
    }
}

class ScrimXGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = context.getSharedPreferences("scrimx_widget_prefs", Context.MODE_PRIVATE)
            val realMoney = prefs.getInt("widget_real_money", 0)
            val isHidden = prefs.getBoolean("widget_balance_hidden", false)

            val cardBgBitmap = remember { WidgetCardRenderer.generateCardBitmap() }

            GlanceTheme {
                // Outer Card Container (Bitmap background with 3D sheen, corner green glow, and cyber lines)
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ImageProvider(cardBgBitmap))
                        .cornerRadius(22.dp)
                        .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 12.dp)
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT SECTION: Flame Logo + Balance Info
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Green Outline Flame Logo - tap opens Home
                            Image(
                                provider = ImageProvider(R.drawable.ic_flame_outline_green),
                                contentDescription = "SCRIMX Flame Logo",
                                modifier = GlanceModifier
                                    .size(32.dp)
                                    .clickable(actionRunCallback<NavigateToAction>(actionParametersOf(TargetKey to "home_tab")))
                            )

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            Text(
                                text = "SCRIMX balance",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF94A3B8)),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = GlanceModifier.clickable(actionRunCallback<NavigateToAction>(actionParametersOf(TargetKey to "wallet_tab")))
                            )

                            Spacer(modifier = GlanceModifier.height(2.dp))

                            // Balance + Working Eye toggle button
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isHidden) "₹••••••" else "₹$realMoney.00",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = GlanceModifier.clickable(actionRunCallback<NavigateToAction>(actionParametersOf(TargetKey to "wallet_tab")))
                                )
                                Spacer(modifier = GlanceModifier.width(4.dp))
                                Box(
                                    modifier = GlanceModifier
                                        .size(32.dp)
                                        .clickable(actionRunCallback<ToggleBalanceVisibilityAction>()),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(
                                            if (isHidden) R.drawable.ic_widget_eye_off else R.drawable.ic_widget_eye
                                        ),
                                        contentDescription = "Toggle Balance Visibility",
                                        modifier = GlanceModifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = GlanceModifier.width(10.dp))

                        // RIGHT SECTION: 3D FamX Style Action Buttons
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Add Money Green Button
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .background(Color(0xFF00E676))
                                    .cornerRadius(12.dp)
                                    .clickable(actionRunCallback<NavigateToAction>(actionParametersOf(TargetKey to "wallet_tab")))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_widget_add),
                                        contentDescription = "Add Money",
                                        modifier = GlanceModifier.size(14.dp)
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = "Add Money",
                                        style = TextStyle(
                                            color = ColorProvider(Color.Black),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.height(5.dp))

                            // 2. Tournaments Dark Button
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .background(Color(0xFF16222F))
                                    .cornerRadius(12.dp)
                                    .clickable(actionRunCallback<NavigateToAction>(actionParametersOf(TargetKey to "matches_tab")))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_widget_game),
                                        contentDescription = "Tournaments",
                                        modifier = GlanceModifier.size(14.dp)
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = "Tournaments",
                                        style = TextStyle(
                                            color = ColorProvider(Color.White),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.height(5.dp))

                            // 3. My Wallet Dark Button
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .background(Color(0xFF16222F))
                                    .cornerRadius(12.dp)
                                    .clickable(actionRunCallback<NavigateToAction>(actionParametersOf(TargetKey to "wallet_tab")))
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_widget_trophy),
                                        contentDescription = "My Wallet",
                                        modifier = GlanceModifier.size(14.dp)
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = "My Wallet",
                                        style = TextStyle(
                                            color = ColorProvider(Color.White),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

class ScrimXGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScrimXGlanceWidget()

    companion object {
        suspend fun updateWidget(context: Context) {
            ScrimXGlanceWidget().updateAll(context)
        }
    }
}
