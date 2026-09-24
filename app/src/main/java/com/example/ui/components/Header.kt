package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Screen
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldDark
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.TableTheme

@Composable
fun AppHeader(
    currentScreen: Screen,
    tableTheme: TableTheme,
    credits: Long,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRules: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToWallet: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_header"),
        color = Color(0xDD0D0812)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Back / Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentScreen != Screen.LOBBY) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .testTag("header_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Lobby",
                            tint = ImperialGoldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Brand Emblem & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(tableTheme.dragonColor, tableTheme.tigerColor)
                                )
                            )
                            .border(1.dp, ImperialGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (currentScreen) {
                                Screen.ZOO_ROULETTE -> "🦁"
                                Screen.DRAGON_TIGER -> "龍"
                                else -> "👑"
                            },
                            color = Color.White,
                            fontSize = if (currentScreen == Screen.ZOO_ROULETTE) 18.sp else 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = when (currentScreen) {
                                Screen.DRAGON_TIGER -> "DRAGON vs TIGER"
                                Screen.ZOO_ROULETTE -> "ZOO ROULETTE"
                                Screen.CYBER_SLOTS -> "CYBER SLOTS"
                                Screen.CHICKEN_DASH -> "CHICKEN DASH"
                                Screen.WALLET -> "SECURE CASHIER"
                                Screen.LOBBY -> "ROYAL ARCADE"
                                Screen.HOME -> "ZOO ROULETTE 3D"
                                Screen.AUTH -> "AUTHENTICATION"
                                Screen.ADMIN_UPDATES -> "ADMIN RELEASES"
                                Screen.ADMIN_LOGIN -> "ADMIN SECURITY LOGIN"
                                Screen.ADMIN_MONITOR -> "LIVE MONITOR DASHBOARD"
                            },
                            color = ImperialGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = when (currentScreen) {
                                Screen.ZOO_ROULETTE -> "飛禽走獸 • BIRDS & BEASTS"
                                Screen.DRAGON_TIGER -> "LIVE TABLE • VIP"
                                Screen.WALLET -> "WALLET & FINANCIAL LEDGER"
                                else -> "CASINO LOBBY • VIP"
                            },
                            color = tableTheme.accentGold.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Right Side: Credits Pill & Quick Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Credits Chip Badge (Clickable to open Wallet)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF201726))
                        .border(1.dp, ImperialGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .clickable(enabled = onNavigateToWallet != null) { onNavigateToWallet?.invoke() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = ImperialGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PKR ${formatCredits(credits)}",
                            color = ImperialGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+",
                            color = Color(0xFF10B981),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Sound Toggle
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1724))
                        .testTag("header_sound_button")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Sound",
                        tint = if (soundEnabled) ImperialGold else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Theme / Settings Dialog Toggle
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1724))
                        .border(1.dp, tableTheme.accentGold.copy(alpha = 0.4f), CircleShape)
                        .testTag("header_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme Settings",
                        tint = tableTheme.accentGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Rules Dialog
                IconButton(
                    onClick = onOpenRules,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1724))
                        .testTag("header_rules_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Game Rules",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

fun formatCredits(amount: Long): String {
    return when {
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000.0)
        amount >= 1_000 -> String.format("%,d", amount)
        else -> amount.toString()
    }
}
