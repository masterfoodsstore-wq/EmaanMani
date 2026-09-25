package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GameRelease
import com.example.model.Screen
import com.example.model.UpdateCheckResult
import com.example.model.UserAccount
import com.example.ui.components.formatCredits

private val GoldLight = Color(0xFFFFE082)
private val GoldCore = Color(0xFFFFD54F)
private val GoldDark = Color(0xFFC49000)
private val EmeraldDark = Color(0xFF04140C)
private val EmeraldCard = Color(0xEE092015)

@Composable
fun GameHomeScreen(
    user: UserAccount?,
    credits: Long = 20L,
    currentVersion: String,
    updateCheckResult: UpdateCheckResult?,
    isCheckingUpdates: Boolean,
    onNavigateToScreen: (Screen) -> Unit,
    onNavigateToDeposit: () -> Unit = {},
    onNavigateToWithdraw: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onCheckForUpdates: () -> Unit,
    onOpenUpdateDialog: (GameRelease) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRules: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseUpdate")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val updateAvailableRelease = (updateCheckResult as? UpdateCheckResult.UpdateAvailable)?.latestRelease

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFF0F3824),
                        Color(0xFF061A10),
                        EmeraldDark
                    ),
                    radius = 1900f
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // =========================================================================
        // 1. TOP HEADER: USER PROFILE & TOP ACTIONS
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0B261A), Color(0xFF14472F), Color(0xFF0B261A))
                    )
                )
                .border(1.dp, GoldCore.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // User Avatar & Name & Joined Info (Clickable for Account Management)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToAuth() }
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldCore)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = user?.avatar ?: "🦁", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user?.username ?: "Guest Player",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (user?.isAdmin == true) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF6A1B9A))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("ADMIN", color = GoldLight, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "Tap to switch account / login",
                        color = Color(0xFFA5D6A7),
                        fontSize = 9.sp
                    )
                }
            }

            // Wallet & Balance Controls (Deposit, Withdraw, Balance Pill)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Balance Pill (Click to open wallet)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF04130A))
                        .border(1.dp, GoldCore, RoundedCornerShape(20.dp))
                        .clickable { onNavigateToWallet() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("header_balance_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🪙", fontSize = 11.sp)
                        Text(
                            text = "PKR ${formatCredits(user?.gamePoints ?: credits)}",
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Deposit Button (+)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF059669), Color(0xFF10B981))
                            )
                        )
                        .border(1.dp, Color(0xFF34D399), RoundedCornerShape(10.dp))
                        .clickable { onNavigateToDeposit() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("home_deposit_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text("DEPOSIT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }

                // Withdraw Button (↗)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
                            )
                        )
                        .border(1.dp, Color(0xFF818CF8), RoundedCornerShape(10.dp))
                        .clickable { onNavigateToWithdraw() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("home_withdraw_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text("WITHDRAW", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            // Top Actions: Wallet, Updates, Settings, Rules, Admin, Logout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Wallet Root Screen
                IconButton(
                    onClick = onNavigateToWallet,
                    modifier = Modifier.size(32.dp).testTag("home_wallet_icon_button")
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet", tint = GoldLight, modifier = Modifier.size(18.dp))
                }

                // Switch / Login Account
                IconButton(
                    onClick = onNavigateToAuth,
                    modifier = Modifier.size(32.dp).testTag("home_auth_button")
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Account", tint = GoldLight, modifier = Modifier.size(18.dp))
                }

                // Update Button in header if available
                if (updateAvailableRelease != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFB300), Color(0xFFE65100))
                                )
                            )
                            .clickable { onOpenUpdateDialog(updateAvailableRelease) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("header_update_alert"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text("UPDATE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Admin Dashboard & Live Telemetry Monitor (Restricted strictly to authenticated admins)
                if (user?.isAdmin == true) {
                    IconButton(
                        onClick = { onNavigateToScreen(Screen.ADMIN_MONITOR) },
                        modifier = Modifier.size(32.dp).testTag("home_admin_button")
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Console",
                            tint = GoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenRules,
                    modifier = Modifier.size(32.dp).testTag("home_rules_button")
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Help & Rules", tint = GoldLight, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(32.dp).testTag("home_settings_button")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = GoldLight, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.size(32.dp).testTag("home_logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color(0xFFFF8A80), modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // =========================================================================
        // 2. MAIN CENTER HERO & GAME CARDS SECTION (LANDSCAPE)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // LEFT PANEL: BRAND LOGO + CURRENT VERSION & LATEST UPDATE INFO
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmeraldCard)
                    .border(1.5.dp, GoldCore, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Game Logo & Branding
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "🦁 🦚 🦈", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ZOO ROULETTE 3D",
                        color = GoldCore,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AAA PHOTOREALISTIC ARCADE EXPERIENCE",
                        color = Color(0xFFA5D6A7),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Update Status & Version Information Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF04120A))
                        .border(1.dp, GoldCore.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GAME VERSION",
                            color = GoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0D3320))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Installed: $currentVersion",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Update Notice Banner
                    if (updateAvailableRelease != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FFB300))
                                .border(1.dp, GoldCore, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "New Version: ${updateAvailableRelease.versionName}",
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "• " + updateAvailableRelease.whatsNew.firstOrNull(),
                                color = Color(0xFFC8E6C9),
                                fontSize = 9.sp
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(GoldCore, GoldDark)
                                        )
                                    )
                                    .clickable { onOpenUpdateDialog(updateAvailableRelease) }
                                    .padding(vertical = 6.dp)
                                    .testTag("btn_home_update_dialog"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "UPDATE NOW (${updateAvailableRelease.versionName})",
                                    color = Color(0xFF1E1000),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    } else if (updateCheckResult is UpdateCheckResult.OfflineError) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text("Offline Mode active (Local game ready)", color = Color.LightGray, fontSize = 9.sp)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                                Text("You're up to date", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Manual Check Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0E301F))
                                    .clickable(enabled = !isCheckingUpdates, onClick = onCheckForUpdates)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("btn_check_updates"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCheckingUpdates) {
                                    CircularProgressIndicator(color = GoldCore, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                } else {
                                    Text("Check Updates", color = GoldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // RIGHT PANEL: GAME MODES SELECTION (3D ZOO ROULETTE, DRAGON TIGER & CHICKEN DASH)
            Row(
                modifier = Modifier
                    .weight(2.0f)
                    .fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // GAME 1: 3D ZOO ROULETTE WHEEL
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EmeraldCard)
                        .border(1.5.dp, GoldCore, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, GoldCore.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_zoo_hero_lion_peacock),
                            contentDescription = "Zoo Roulette 3D",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(bottomEnd = 8.dp))
                                .background(Color(0xDD000000))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("HOT 3D", color = GoldCore, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "3D ZOO ROULETTE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "26 Slots • Golden Toad x100",
                        color = Color(0xFFA5D6A7),
                        fontSize = 8.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(GoldLight, GoldCore, GoldDark)
                                )
                            )
                            .clickable { onNavigateToScreen(Screen.ZOO_ROULETTE) }
                            .padding(vertical = 8.dp)
                            .testTag("btn_play_zoo_roulette"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF241400), modifier = Modifier.size(16.dp))
                            Text(
                                text = "PLAY NOW",
                                color = Color(0xFF241400),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // GAME 2: DRAGON VS TIGER 3D
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EmeraldCard)
                        .border(1.5.dp, GoldCore, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF8B0000), Color(0xFF4A0000))
                                )
                            )
                            .border(1.dp, GoldCore.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐉 ⚔️ 🐅", fontSize = 28.sp)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(bottomEnd = 8.dp))
                                .background(Color(0xDD000000))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PVP DUEL", color = Color(0xFFFF8A80), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "DRAGON VS TIGER",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "High Card Duel • 1:1 Win",
                        color = Color(0xFFA5D6A7),
                        fontSize = 8.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFD32F2F), Color(0xFF8B0000))
                                )
                            )
                            .border(1.dp, GoldCore, RoundedCornerShape(10.dp))
                            .clickable { onNavigateToScreen(Screen.DRAGON_TIGER) }
                            .padding(vertical = 8.dp)
                            .testTag("btn_play_dragon_tiger"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(
                                text = "PLAY NOW",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // GAME 3: CHICKEN DASH ARCADE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EmeraldCard)
                        .border(1.5.dp, Color(0xFF22C55E), RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E253E), Color(0xFF14192B))
                                )
                            )
                            .border(1.dp, Color(0xFF384672), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.chicken_character),
                            contentDescription = "Chicken Dash",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(bottomEnd = 8.dp))
                                .background(Color(0xFF22C55E))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("NEW ARCADE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "CHICKEN DASH",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Road Multipliers • Traps",
                        color = Color(0xFF86EFAC),
                        fontSize = 8.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF22C55E), Color(0xFF15803D))
                                )
                            )
                            .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(10.dp))
                            .clickable { onNavigateToScreen(Screen.CHICKEN_DASH) }
                            .padding(vertical = 8.dp)
                            .testTag("btn_play_chicken_dash"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(
                                text = "PLAY NOW",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // =========================================================================
        // 3. FOOTER: NON-MONETARY DISCLOSURE NOTICE
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🛡️ Pure non-monetary arcade game for entertainment only. No real money, bets, deposits or withdrawals.",
                color = Color(0xFF81C784),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
