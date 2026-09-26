package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.BuildConfig
import com.example.model.SlotSymbol
import com.example.ui.CyberSlotsViewModel
import kotlinx.coroutines.delay

@Composable
fun CyberSlotsGameScreen(
    onNavigateBack: () -> Unit,
    viewModel: CyberSlotsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Shuffling symbols for visual spinning animation
    var animSymbol1 by remember { mutableStateOf(SlotSymbol.SEVEN) }
    var animSymbol2 by remember { mutableStateOf(SlotSymbol.CROWN) }
    var animSymbol3 by remember { mutableStateOf(SlotSymbol.DIAMOND) }

    LaunchedEffect(uiState.isSpinning) {
        if (uiState.isSpinning) {
            val allSymbols = SlotSymbol.values()
            var counter = 0
            while (uiState.isSpinning) {
                animSymbol1 = allSymbols[(counter) % allSymbols.size]
                animSymbol2 = allSymbols[(counter + 2) % allSymbols.size]
                animSymbol3 = allSymbols[(counter + 4) % allSymbols.size]
                counter++
                delay(60)
            }
        }
    }

    val glowTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F081D),
                        Color(0xFF1B0B2E),
                        Color(0xFF0B0414)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("cyber_slots_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR: Navigation, Title & RapidAPI Integration Pill, Balance, Sound, Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1038))
                    .border(1.dp, Color(0xFF9C27B0).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(36.dp).testTag("cyber_slots_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Lobby",
                            tint = Color(0xFFFFD54F)
                        )
                    }

                    Column {
                        Text(
                            text = "CYBER SLOTS 777",
                            color = Color(0xFFFFD54F),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable { viewModel.setApiDetailsOpen(true) }
                                .testTag("btn_api_details")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.apiStatus.isConnected) Color(0xFF00E676) else Color(0xFFFF9100))
                            )
                            Text(
                                text = "rapidapi.com • ${if (uiState.apiStatus.isConnected) "${uiState.apiStatus.pingMs}ms" else "Checking"}",
                                color = if (uiState.apiStatus.isConnected) Color(0xFF81C784) else Color(0xFFFFB74D),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Balance Pill & Controls
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2E1752))
                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("cyber_slots_balance")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🪙", fontSize = 11.sp)
                            Text(
                                text = "PKR ${uiState.credits}",
                                color = Color(0xFFFFD54F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.setPaytableOpen(true) },
                        modifier = Modifier.size(32.dp).testTag("cyber_slots_paytable_button")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Paytable", tint = Color.White)
                    }

                    IconButton(
                        onClick = { viewModel.toggleSound() },
                        modifier = Modifier.size(32.dp).testTag("cyber_slots_sound_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Sound Toggle",
                            tint = Color.White
                        )
                    }
                }
            }

            // JACKPOT BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF4A148C),
                                Color(0xFF880E4F),
                                Color(0xFF4A148C)
                            )
                        )
                    )
                    .border(1.dp, Color(0xFFFFD54F).copy(alpha = glowAlpha), RoundedCornerShape(8.dp))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⚡ RAPID JACKPOT:", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("PKR ${uiState.jackpotPool}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }

            // CENTER SLOT REELS CABINET
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF120822))
                    .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val displaySymbols = if (uiState.isSpinning) {
                        listOf(animSymbol1, animSymbol2, animSymbol3)
                    } else {
                        uiState.reels
                    }

                    displaySymbols.forEachIndexed { index, symbol ->
                        SlotReelCard(
                            symbol = symbol,
                            isSpinning = uiState.isSpinning,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            // STATUS & WIN FEEDBACK BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (uiState.lastWin > 0) Color(0xFF1B5E20) else Color(0xFF26133E)
                    )
                    .border(
                        1.dp,
                        if (uiState.lastWin > 0) Color(0xFF00E676) else Color(0xFF7B1FA2),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.statusBanner,
                    color = if (uiState.lastWin > 0) Color(0xFFA7FFEB) else Color(0xFFE1BEE7),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // BOTTOM CONTROLS: Bet Chips, Auto-Spin, Spin Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF190C2C))
                    .border(1.dp, Color(0xFF6A1B9A), RoundedCornerShape(14.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Quick Bet Selector Chips
                Column {
                    Text("SELECT BET (PKR)", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(50L, 100L, 250L, 500L, 1000L).forEach { bet ->
                            val isSelected = uiState.selectedBet == bet
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFFFFD54F) else Color(0xFF2E1752))
                                    .border(1.dp, if (isSelected) Color.White else Color(0xFF7B1FA2), RoundedCornerShape(6.dp))
                                    .clickable(enabled = !uiState.isSpinning) { viewModel.selectBet(bet) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("bet_chip_$bet"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$bet",
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Action Buttons: Auto Spin & Big Spin Button
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Auto-spin toggle
                    if (uiState.autoSpinsRemaining > 0) {
                        Button(
                            onClick = { viewModel.stopAutoSpin() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(44.dp).testTag("btn_stop_auto_spin")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("STOP (${uiState.autoSpinsRemaining})", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startAutoSpin(20) },
                            enabled = !uiState.isSpinning,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(44.dp).testTag("btn_auto_spin")
                        ) {
                            Text("AUTO (20)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                        }
                    }

                    // Main Spin Button
                    Button(
                        onClick = { viewModel.spin() },
                        enabled = !uiState.isSpinning && uiState.autoSpinsRemaining == 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .width(100.dp)
                            .testTag("btn_main_spin")
                    ) {
                        if (uiState.isSpinning) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("SPIN", fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    // PAYTABLE DIALOG
    if (uiState.isPaytableOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setPaytableOpen(false) },
            title = {
                Text("🎰 CYBER SLOTS PAYTABLE", fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Payout Multipliers (3 of a kind):", color = Color.LightGray, fontSize = 12.sp)
                    SlotSymbol.values().forEach { symbol ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(symbol.emoji, fontSize = 18.sp)
                                Text(symbol.displayName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("${symbol.tripleMultiplier}X", color = Color(0xFFFFD54F), fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Any 2 Matching Symbols: 1.5X", color = Color(0xFFA5D6A7), fontSize = 11.sp)
                    Text("• Any Wild 7️⃣ on reel: 1.2X", color = Color(0xFFA5D6A7), fontSize = 11.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setPaytableOpen(false) }) {
                    Text("CLOSE", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E1038)
        )
    }

    // RAPIDAPI TECHNICAL DETAILS DIALOG
    if (uiState.isApiDetailsOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setApiDetailsOpen(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF00E676))
                    Text("RapidAPI Integration", fontWeight = FontWeight.Black, color = Color(0xFFFFD54F))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dedicated Second Game API Configuration:", color = Color.LightGray, fontSize = 11.sp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF120822))
                            .border(1.dp, Color(0xFF7B1FA2), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Website URL: ${BuildConfig.RAPID_API_URL}", color = Color.White, fontSize = 11.sp)
                            Text("API Key: ${BuildConfig.RAPID_API_KEY.take(12)}...${BuildConfig.RAPID_API_KEY.takeLast(6)}", color = Color(0xFF81C784), fontSize = 10.sp)
                            Text("Status: ${uiState.apiStatus.statusMessage}", color = Color(0xFFFFD54F), fontSize = 10.sp)
                            Text("Latency: ${uiState.apiStatus.pingMs} ms", color = Color.White, fontSize = 10.sp)
                            Text("Server: ${uiState.apiStatus.serverRegion}", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }

                    if (uiState.lastVerification != null) {
                        val ver = uiState.lastVerification!!
                        Text("Last Verified Round:", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("• Round ID: ${ver.roundId}", color = Color.White, fontSize = 10.sp)
                        Text("• Server Seed: ${ver.serverSeed}", color = Color(0xFFA5D6A7), fontSize = 10.sp)
                        Text("• HTTP Code: ${ver.httpCode} OK", color = Color(0xFF81C784), fontSize = 10.sp)
                    }

                    Button(
                        onClick = { viewModel.refreshApiConnection() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-test RapidAPI Ping", fontSize = 11.sp, color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setApiDetailsOpen(false) }) {
                    Text("DONE", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E1038)
        )
    }
}

@Composable
private fun SlotReelCard(
    symbol: SlotSymbol,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF281347),
                        Color(0xFF180A2E),
                        Color(0xFF281347)
                    )
                )
            )
            .border(
                1.5.dp,
                if (isSpinning) Color(0xFFFFD54F).copy(alpha = 0.6f) else symbol.accentColor.copy(alpha = 0.8f),
                RoundedCornerShape(12.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = symbol.emoji,
                fontSize = 42.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = symbol.displayName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "${symbol.tripleMultiplier}X",
                color = symbol.accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
