package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveGameConfig
import com.example.ui.theme.ImperialGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminLiveGameControlTab(
    config: LiveGameConfig,
    onToggleGlobalMaintenance: (Boolean, String, Int) -> Unit,
    onUpdateGameStatus: (Boolean, Boolean, Boolean) -> Unit,
    onBroadcastMessage: (String) -> Unit,
    onUpdateEconomy: (Double, Int, Int, Long, Long) -> Unit,
    onToggleMultiplier: (Boolean, Double, String) -> Unit,
    onPushOtaUpdate: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var broadcastInput by remember(config.liveBroadcastMessage) {
        mutableStateOf(config.liveBroadcastMessage)
    }
    var maintenanceMsgInput by remember(config.maintenanceMessage) {
        mutableStateOf(config.maintenanceMessage)
    }
    var maintenanceEtaInput by remember(config.maintenanceEtaMinutes) {
        mutableStateOf(config.maintenanceEtaMinutes.toString())
    }

    var selectedCommission by remember(config.houseCommissionPercent) {
        mutableStateOf(config.houseCommissionPercent)
    }
    var selectedDragonSeconds by remember(config.dragonCountdownSeconds) {
        mutableStateOf(config.dragonCountdownSeconds)
    }
    var selectedZooSeconds by remember(config.zooCountdownSeconds) {
        mutableStateOf(config.zooCountdownSeconds)
    }
    var maxBetInput by remember(config.maxBetLimit) {
        mutableStateOf(config.maxBetLimit.toString())
    }

    var eventTitleInput by remember(config.multiplierTitle) {
        mutableStateOf(config.multiplierTitle)
    }
    var selectedMultiplier by remember(config.multiplierRate) {
        mutableStateOf(config.multiplierRate)
    }

    var otaVersionInput by remember { mutableStateOf("v2.5.1-HOTFIX") }
    var otaNotesInput by remember { mutableStateOf("Over-The-Air engine balance update & real-time odds sync.") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Status Header Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("admin_live_header_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161F30)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (config.isGlobalMaintenance) Color(0xFFEF4444) else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (config.isGlobalMaintenance) "SYSTEM STATUS: MAINTENANCE" else "SYSTEM STATUS: ALL TABLES ONLINE",
                            color = if (config.isGlobalMaintenance) Color(0xFFF87171) else Color(0xFF34D399),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${config.onlinePlayerCount} Downloaded Players",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                val timeStr = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date(config.lastUpdatedTimestamp))
                Text(
                    text = "Last OTA Sync: $timeStr by ${config.lastAdminEditor}",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }
        }

        // Section 1: Global Master Switch & Maintenance
        Card(
            modifier = Modifier.fillMaxWidth().testTag("admin_global_maintenance_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B2E)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Global System Maintenance",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Instantly redirects all active user devices to the maintenance holding screen.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = config.isGlobalMaintenance,
                        onCheckedChange = { checked ->
                            onToggleGlobalMaintenance(
                                checked,
                                maintenanceMsgInput,
                                maintenanceEtaInput.toIntOrNull() ?: 10
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFDC2626)
                        ),
                        modifier = Modifier.testTag("admin_maintenance_switch")
                    )
                }

                if (config.isGlobalMaintenance) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = maintenanceMsgInput,
                        onValueChange = { maintenanceMsgInput = it },
                        label = { Text("Maintenance Notice Displayed to Users") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFDC2626)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = maintenanceEtaInput,
                            onValueChange = { maintenanceEtaInput = it },
                            label = { Text("Estimated Minutes (ETA)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Button(
                            onClick = {
                                onToggleGlobalMaintenance(
                                    true,
                                    maintenanceMsgInput,
                                    maintenanceEtaInput.toIntOrNull() ?: 10
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text("Update Notice")
                        }
                    }
                }
            }
        }

        // Section 2: Individual Game Switchboard
        Card(
            modifier = Modifier.fillMaxWidth().testTag("admin_game_switchboard_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2D)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Live Game Table Switchboard",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Enable or disable games in real-time. Changes apply instantly to all downloaded apps.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dragon vs Tiger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A263D))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🐉", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Dragon vs Tiger", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (config.isDragonTigerOnline) "ONLINE • 1:8 Tie Active" else "OFFLINE • Table Reshuffle",
                                color = if (config.isDragonTigerOnline) Color(0xFF34D399) else Color(0xFFF87171),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = config.isDragonTigerOnline,
                        onCheckedChange = { isChecked ->
                            onUpdateGameStatus(isChecked, config.isZooRouletteOnline, config.isCyberSlotsOnline)
                        },
                        modifier = Modifier.testTag("admin_switch_dragon_tiger")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Zoo Roulette
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A263D))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🦁", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Zoo Roulette", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (config.isZooRouletteOnline) "ONLINE • 24-Track Wheel" else "OFFLINE • Maintenance",
                                color = if (config.isZooRouletteOnline) Color(0xFF34D399) else Color(0xFFF87171),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = config.isZooRouletteOnline,
                        onCheckedChange = { isChecked ->
                            onUpdateGameStatus(config.isDragonTigerOnline, isChecked, config.isCyberSlotsOnline)
                        },
                        modifier = Modifier.testTag("admin_switch_zoo_roulette")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cyber Slots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A263D))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎰", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cyber Slots", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (config.isCyberSlotsOnline) "ONLINE • 5-Reel Classic" else "OFFLINE • In Development",
                                color = if (config.isCyberSlotsOnline) Color(0xFF34D399) else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Switch(
                        checked = config.isCyberSlotsOnline,
                        onCheckedChange = { isChecked ->
                            onUpdateGameStatus(config.isDragonTigerOnline, config.isZooRouletteOnline, isChecked)
                        },
                        modifier = Modifier.testTag("admin_switch_cyber_slots")
                    )
                }
            }
        }

        // Section 3: Live Broadcast Announcement Ticker
        Card(
            modifier = Modifier.fillMaxWidth().testTag("admin_broadcast_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1627)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = ImperialGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Instant Live Marquee Broadcast",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Pushes a scrolling notification banner to all active player game screens immediately.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = broadcastInput,
                    onValueChange = { broadcastInput = it },
                    label = { Text("Broadcast Message") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_broadcast_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ImperialGold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Templates
                Text("Quick Preset Messages:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "🔥 2X Jackpots Active!",
                        "⚡ Instant EasyPaisa Rebates!",
                        "🛠️ 5 Min Table Reshuffle"
                    )
                    presets.forEach { preset ->
                        AssistChip(
                            onClick = { broadcastInput = preset },
                            label = { Text(preset, fontSize = 10.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = Color.White,
                                containerColor = Color(0xFF2A1C36)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onBroadcastMessage(broadcastInput) },
                    modifier = Modifier.fillMaxWidth().testTag("admin_broadcast_send_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = ImperialGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PUSH LIVE BROADCAST TO ALL USERS", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        // Section 4: Game Economy & Dynamic Timers
        Card(
            modifier = Modifier.fillMaxWidth().testTag("admin_game_economy_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14202E)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Game Economy & Timer Configuration",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Adjust countdown durations, house commission cut, and max bet ceilings in real-time.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // House Commission %
                Text("House Commission / Win Cut: ${selectedCommission.toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.0, 1.0, 3.0, 5.0, 8.0).forEach { rate ->
                        FilterChip(
                            selected = selectedCommission == rate,
                            onClick = { selectedCommission = rate },
                            label = { Text("${rate.toInt()}%") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6),
                                selectedLabelColor = Color.White,
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dragon vs Tiger Betting Timer
                Text("Dragon vs Tiger Countdown: ${selectedDragonSeconds}s", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { sec ->
                        FilterChip(
                            selected = selectedDragonSeconds == sec,
                            onClick = { selectedDragonSeconds = sec },
                            label = { Text("${sec}s ${if (sec == 5) "Blitz" else if (sec == 15) "Standard" else ""}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.White,
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Zoo Roulette Countdown Timer
                Text("Zoo Roulette Countdown: ${selectedZooSeconds}s", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(10, 18, 25, 30).forEach { sec ->
                        FilterChip(
                            selected = selectedZooSeconds == sec,
                            onClick = { selectedZooSeconds = sec },
                            label = { Text("${sec}s") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF59E0B),
                                selectedLabelColor = Color.Black,
                                labelColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Max Bet Ceiling
                OutlinedTextField(
                    value = maxBetInput,
                    onValueChange = { maxBetInput = it },
                    label = { Text("Max Bet Limit (Coins/PKR)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val maxBet = maxBetInput.toLongOrNull() ?: 50_000L
                        onUpdateEconomy(
                            selectedCommission,
                            selectedDragonSeconds,
                            selectedZooSeconds,
                            10L,
                            maxBet
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("admin_save_economy_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SYNC LIVE TIMERS & COMMISSIONS TO ALL PLAYERS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Section 5: Promotional Multiplier Event
        Card(
            modifier = Modifier.fillMaxWidth().testTag("admin_multiplier_event_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF28181A)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Promotional Win Multiplier Event",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Multiplies gross winning payouts for all active players live on the fly.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = config.isMultiplierEventActive,
                        onCheckedChange = { active ->
                            onToggleMultiplier(active, selectedMultiplier, eventTitleInput)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFE11D48)
                        ),
                        modifier = Modifier.testTag("admin_multiplier_switch")
                    )
                }

                if (config.isMultiplierEventActive) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = eventTitleInput,
                        onValueChange = { eventTitleInput = it },
                        label = { Text("Event Banner Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Multiplier Rate: ${selectedMultiplier}x", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1.25, 1.5, 2.0, 3.0).forEach { rate ->
                            FilterChip(
                                selected = selectedMultiplier == rate,
                                onClick = {
                                    selectedMultiplier = rate
                                    onToggleMultiplier(true, rate, eventTitleInput)
                                },
                                label = { Text("${rate}x Boost") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE11D48),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color(0xFF94A3B8)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Section 6: Over-The-Air (OTA) Game Hot-Update Dispatcher
        Card(
            modifier = Modifier.fillMaxWidth().testTag("admin_ota_patch_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E261E)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OTA Live Hot-Patch Dispatcher",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Publish client updates directly to downloaded games without requiring APK reinstall.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF131D13))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Current Live Release:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text(config.liveAppVersion, color = Color(0xFF34D399), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = otaVersionInput,
                    onValueChange = { otaVersionInput = it },
                    label = { Text("Target OTA Version") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = otaNotesInput,
                    onValueChange = { otaNotesInput = it },
                    label = { Text("Patch Notes & Balance Log") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onPushOtaUpdate(otaVersionInput, otaNotesInput) },
                    modifier = Modifier.fillMaxWidth().testTag("admin_push_ota_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DISPATCH OTA HOT-PATCH TO ALL ACTIVE CLIENTS", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}
