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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.TableTheme

import androidx.compose.material.icons.filled.Refresh
import com.example.model.GameRelease
import com.example.model.UpdateCheckResult

@Composable
fun SettingsDialog(
    currentTheme: TableTheme,
    onSelectTheme: (TableTheme) -> Unit,
    countdownSeconds: Int,
    onSelectCountdown: (Int) -> Unit,
    soundEnabled: Boolean,
    onToggleSound: (Boolean) -> Unit,
    fastDealMode: Boolean,
    onToggleFastDeal: (Boolean) -> Unit,
    onReloadDemoCredits: () -> Unit,
    currentVersion: String = "1.0.0",
    updateResult: UpdateCheckResult? = null,
    isCheckingUpdate: Boolean = false,
    onCheckForUpdates: () -> Unit = {},
    onTriggerUpdate: (GameRelease) -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, ImperialGold, RoundedCornerShape(16.dp))
                .testTag("settings_dialog_surface"),
            color = Color(0xFF140F18)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Visual Themes",
                            tint = ImperialGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Game Table Settings",
                            color = ImperialGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("settings_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Visual Themes
                Text(
                    text = "VISUAL THEMES",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                TableTheme.values().forEach { theme ->
                    val isSelected = theme == currentTheme
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) theme.tableFelt.copy(alpha = 0.9f) else Color(0xFF1E1724)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) ImperialGold else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectTheme(theme) }
                            .padding(12.dp)
                            .testTag("theme_option_${theme.name}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Theme Color Swatches preview
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF09060A))
                                        .padding(3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(theme.dragonColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(theme.tieColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(theme.tigerColor)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = theme.displayName,
                                        color = if (isSelected) ImperialGold else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = theme.description,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(ImperialGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Countdown Timer duration
                Text(
                    text = "BETTING COUNTDOWN DURATION",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 15, 20).forEach { seconds ->
                        val isSelected = seconds == countdownSeconds
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ImperialGold else Color(0xFF221A28))
                                .border(
                                    1.dp,
                                    if (isSelected) ImperialGold else Color(0x33FFFFFF),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectCountdown(seconds) }
                                .padding(vertical = 10.dp)
                                .testTag("countdown_option_${seconds}s"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${seconds}s",
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Sound & Animation Speed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Sound Effects",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sound Effects & Haptics",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onToggleSound,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ImperialGold
                        ),
                        modifier = Modifier.testTag("sound_toggle_switch")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Fast Deal",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fast Dealing Speed",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Switch(
                        checked = fastDealMode,
                        onCheckedChange = onToggleFastDeal,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = ImperialGold
                        ),
                        modifier = Modifier.testTag("fast_deal_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Table Commission Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF221A28))
                        .border(1.dp, ImperialGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚖️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Table Commission: 3% on Winning Bets",
                                color = ImperialGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "A 3% house fee is automatically deducted from all winning bet profits, and net winnings are credited to your account balance.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Demo Balance Reload
                Button(
                    onClick = {
                        onReloadDemoCredits()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("reload_demo_credits_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImperialGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reload Balance",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset / Reload Demo Credits (+10,000 PTS)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 5: Updates (GitHub Releases Distribution)
                Text(
                    text = "UPDATES",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E1724))
                        .border(1.dp, ImperialGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Current Version",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = currentVersion.removePrefix("v"),
                                    color = ImperialGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Button(
                                onClick = onCheckForUpdates,
                                enabled = !isCheckingUpdate,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2C2235),
                                    contentColor = ImperialGold
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ImperialGold.copy(alpha = 0.6f)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_check_for_updates")
                            ) {
                                if (isCheckingUpdate) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        color = ImperialGold,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Checking...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("CHECK FOR UPDATES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Status card depending on updateResult
                        when (updateResult) {
                            is UpdateCheckResult.UpdateAvailable -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F2E1E))
                                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "New version available",
                                                color = Color(0xFF81C784),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = updateResult.latestRelease.versionName.removePrefix("v"),
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Button(
                                            onClick = { onTriggerUpdate(updateResult.latestRelease) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF00E676),
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("btn_settings_update_now")
                                        ) {
                                            Text("UPDATE NOW", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                            is UpdateCheckResult.UpToDate -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x3300E676))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "You're up to date.",
                                        color = Color(0xFFC8E6C9),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            is UpdateCheckResult.OfflineError -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33FFB74D))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = updateResult.message,
                                        color = Color(0xFFFFCC80),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            is UpdateCheckResult.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33FF5252))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = updateResult.message,
                                        color = Color(0xFFFF8A80),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            null -> {
                                Text(
                                    text = "Ready to check GitHub Releases.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
