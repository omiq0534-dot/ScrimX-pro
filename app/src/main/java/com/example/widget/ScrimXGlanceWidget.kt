package com.example.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
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

class ScrimXGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("scrimx_widget_prefs", Context.MODE_PRIVATE)
        val realMoney = prefs.getInt("widget_real_money", 0)

        provideContent {
            GlanceTheme {
                // Card Container with rounded corners and FamX green gradient background
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ImageProvider(R.drawable.bg_famx_widget_card))
                        .cornerRadius(24.dp)
                        .padding(start = 20.dp, top = 18.dp, end = 16.dp, bottom = 18.dp)
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT SECTION: Flame Logo + Balance Info (clicking opens Home)
                        val mainActivityComponent = ComponentName(context, MainActivity::class.java)
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxSize()
                                .clickable(actionStartActivity(mainActivityComponent)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Exact Green Outline Flame Logo (from user screenshot)
                            Image(
                                provider = ImageProvider(R.drawable.ic_flame_outline_green),
                                contentDescription = "SCRIMX Flame Logo",
                                modifier = GlanceModifier.size(38.dp)
                            )

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            Text(
                                text = "SCRIMX balance",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF94A3B8)),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )

                            Spacer(modifier = GlanceModifier.height(3.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₹$realMoney.00",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = GlanceModifier.width(8.dp))
                                Image(
                                    provider = ImageProvider(R.drawable.ic_widget_eye),
                                    contentDescription = "Balance Indicator",
                                    modifier = GlanceModifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.width(12.dp))

                        // RIGHT SECTION: 3 FamX Style Quick Actions
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Add Money Action
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .clickable(actionStartActivity(mainActivityComponent))
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .size(32.dp)
                                        .background(ImageProvider(R.drawable.bg_widget_action_circle)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_widget_add),
                                        contentDescription = "Add Money",
                                        modifier = GlanceModifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = GlanceModifier.width(10.dp))
                                Text(
                                    text = "Add Money",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.height(6.dp))

                            // 2. Tournaments Action
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .clickable(actionStartActivity(mainActivityComponent))
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .size(32.dp)
                                        .background(ImageProvider(R.drawable.bg_widget_action_circle)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_widget_game),
                                        contentDescription = "Tournaments",
                                        modifier = GlanceModifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = GlanceModifier.width(10.dp))
                                Text(
                                    text = "Tournaments",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.height(6.dp))

                            // 3. My Wallet Action
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .clickable(actionStartActivity(mainActivityComponent))
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .size(32.dp)
                                        .background(ImageProvider(R.drawable.bg_widget_action_circle)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_widget_trophy),
                                        contentDescription = "My Wallet",
                                        modifier = GlanceModifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = GlanceModifier.width(10.dp))
                                Text(
                                    text = "My Wallet",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
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

class ScrimXGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScrimXGlanceWidget()

    companion object {
        suspend fun updateWidget(context: Context) {
            ScrimXGlanceWidget().updateAll(context)
        }
    }
}
