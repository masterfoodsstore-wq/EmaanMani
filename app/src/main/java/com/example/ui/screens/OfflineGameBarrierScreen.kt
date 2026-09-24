package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight

private val DeepEmeraldDark = Color(0xFF030D08)
private val CardBackground = Color(0xFF071910)
private val AlertRed = Color(0xFFFF5252)
private val AlertRedDark = Color(0xFF8B0000)

@Composable
fun OfflineGameBarrierScreen(
    isCheckingConnection: Boolean,
    simulatedOffline: Boolean,
    onRetryConnection: () -> Unit,
    onToggleSimulatedOffline: (Boolean) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F0404),
                        DeepEmeraldDark,
                        Color(0xFF020805)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, Brush.horizontalGradient(listOf(AlertRed, ImperialGold)), RoundedCornerShape(20.dp)),
            color = CardBackground.copy(alpha = 0.96f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Warning Beacon
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(AlertRed.copy(alpha = 0.15f * glowAlpha))
                        .border(2.dp, AlertRed.copy(alpha = glowAlpha), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SignalWifiOff,
                        contentDescription = "No Internet Connection",
                        tint = AlertRed,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Banner
                Text(
                    text = "NO INTERNET CONNECTION",
                    color = AlertRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "GAME IS NOT AVAILABLE IN OFFLINE MODE",
                    color = ImperialGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Detailed message explaining online-only requirement
                Text(
                    text = "Zoo Roulette 3D and Dragon Tiger require an active, validated internet connection to function. Live table synchronization, server-side round seeds, and user account verification are disabled while offline.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Status Diagnostics Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF030E09))
                        .border(1.dp, Color(0xFF1E3A28), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AlertRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Network: Offline",
                            color = Color(0xFFFFB4AB),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AlertRedDark)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Game Engine: Stopped",
                            color = Color(0xFFFFB4AB),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ImperialGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mode: Online Only",
                            color = ImperialGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onRetryConnection,
                        enabled = !isCheckingConnection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImperialGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("retry_connection_button")
                    ) {
                        if (isCheckingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHECKING NETWORK...",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry Connection",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RETRY CONNECTION",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Simulation Toggle for testing directly in development environment
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F2618))
                            .border(1.dp, Color(0xFF2C5E3D), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (simulatedOffline) "Simulated: Offline" else "Simulated: Online",
                            color = if (simulatedOffline) AlertRed else ImperialGoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = simulatedOffline,
                            onCheckedChange = onToggleSimulatedOffline,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AlertRed,
                                checkedTrackColor = AlertRedDark,
                                uncheckedThumbColor = ImperialGold,
                                uncheckedTrackColor = Color(0xFF1E3A28)
                            ),
                            modifier = Modifier.scale(0.75f).testTag("simulated_offline_switch")
                        )
                    }
                }
            }
        }
    }
}
