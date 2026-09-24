package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PublishedWithChanges
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdminAuditLog
import com.example.model.AdminSession
import com.example.model.GameRelease
import com.example.model.LiveGameTelemetry
import com.example.model.RealtimeConnectionStatus
import java.util.Locale

private val DarkSlateBg = Color(0xFF090D18)
private val DarkCardBg = Color(0xFF111827)
private val CyanAccent = Color(0xFF00E5FF)
private val NeonRedSideA = Color(0xFFFF3366)
private val NeonBlueSideB = Color(0xFF00B0FF)
private val GoldCore = Color(0xFFFFD54F)
private val GreenLive = Color(0xFF00E676)

@Composable
fun AdminDashboardScreen(
    session: AdminSession?,
    telemetry: LiveGameTelemetry,
    auditLogs: List<AdminAuditLog>,
    releases: List<GameRelease>,
    currentVersion: String,
    onLogout: () -> Unit,
    onExtendSession: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onSimulateDisconnect: () -> Unit,
    onReconnectStream: () -> Unit,
    onTogglePauseStream: () -> Unit,
    onAdvanceRound: () -> Unit,
    onInjectSimulatedPlays: (sideAExtra: Int, sideBExtra: Int) -> Unit,
    onTogglePublishRelease: (String, Boolean) -> Unit,
    onSetInstalledVersion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // =========================================================================
    // 1. MANDATORY SERVER-SIDE AUTHORIZATION GUARD
    // =========================================================================
    if (session == null || session.isExpired) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkSlateBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF5252)),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Access Denied",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "ADMINISTRATIVE ACCESS RESTRICTED",
                        color = Color(0xFFFF5252),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (session?.isExpired == true)
                            "Your administrative session has expired for security. Please re-authenticate."
                        else
                            "Server authorization required. Normal players and unauthenticated users cannot access this administrative portal.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanAccent)
                                .clickable(onClick = onNavigateToLogin)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("btn_redirect_admin_login")
                        ) {
                            Text("Open Admin Login", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF23304B))
                                .clickable(onClick = onNavigateBack)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("Return to Lobby", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Pulsing indicator for live stream
    val infiniteTransition = rememberInfiniteTransition(label = "pulseBeacon")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSlateBg)
    ) {
        // =========================================================================
        // 2. ADMIN HEADER: STATUS, SESSION TIMER, LOGOUT
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1524))
                .border(1.dp, Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back & Title
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .testTag("admin_header_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ADMIN CONSOLE",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyanAccent.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("NON-MONETARY ANALYTICS", color = CyanAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Telemetry Observer • Read-Only Analytics",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp
                    )
                }
            }

            // Center: Connection Status Pill
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF06181C))
                        .border(1.dp, Color(telemetry.connectionStatus.colorHex).copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    Color(telemetry.connectionStatus.colorHex).copy(
                                        alpha = if (telemetry.connectionStatus == RealtimeConnectionStatus.LIVE) beaconAlpha else 1f
                                    )
                                )
                        )
                        Text(
                            text = telemetry.connectionStatus.label.uppercase(),
                            color = Color(telemetry.connectionStatus.colorHex),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${telemetry.latencyMs}ms",
                            color = Color.LightGray,
                            fontSize = 9.sp
                        )
                    }
                }

                // WebSocket Drop/Recovery test button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .clickable {
                            if (telemetry.connectionStatus == RealtimeConnectionStatus.LIVE) {
                                onSimulateDisconnect()
                            } else {
                                onReconnectStream()
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_toggle_connection"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (telemetry.connectionStatus == RealtimeConnectionStatus.LIVE) "Drop WS" else "Reconnect",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right: Session Info & Logout
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Session timer chip
                val remMins = session.remainingSeconds / 60
                val remSecs = session.remainingSeconds % 60
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF16233B))
                        .border(1.dp, GoldCore.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = GoldCore, modifier = Modifier.size(12.dp))
                        Text(
                            text = "TTL: ${remMins}m ${remSecs}s",
                            color = GoldCore,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Extend Session Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F3B2C))
                        .clickable(onClick = onExtendSession)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_extend_session")
                ) {
                    Text("+30m", color = GreenLive, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Logout Button
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FF5252))
                        .testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFFF8A80),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // =========================================================================
        // 3. TABS NAVIGATION
        // =========================================================================
        val tabs = listOf("LIVE GAME MONITOR", "SECURITY AUDIT LOGS", "RELEASES & CATALOG")
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF0C1322),
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = CyanAccent,
                    height = 2.5.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Black else FontWeight.Bold,
                            color = if (selectedTabIndex == index) CyanAccent else Color(0xFF94A3B8)
                        )
                    }
                )
            }
        }

        // =========================================================================
        // 4. TAB CONTENTS
        // =========================================================================
        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            when (selectedTabIndex) {
                0 -> LiveGameMonitorTab(
                    telemetry = telemetry,
                    onTogglePauseStream = onTogglePauseStream,
                    onAdvanceRound = onAdvanceRound,
                    onInjectSimulatedPlays = onInjectSimulatedPlays
                )
                1 -> SecurityAuditLogsTab(auditLogs = auditLogs)
                2 -> GameReleasesTab(
                    releases = releases,
                    currentVersion = currentVersion,
                    onTogglePublishRelease = onTogglePublishRelease,
                    onSetInstalledVersion = onSetInstalledVersion
                )
            }
        }
    }
}

/**
 * Tab 1: Real-time Live Game Monitor with side-by-side activity and live bar chart.
 */
@Composable
private fun LiveGameMonitorTab(
    telemetry: LiveGameTelemetry,
    onTogglePauseStream: () -> Unit,
    onAdvanceRound: () -> Unit,
    onInjectSimulatedPlays: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // A. TOP METRICS CARDS: Current round, Number of players, Total simulated plays, Last update time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "CURRENT ROUND",
                value = "Round #${telemetry.roundId}",
                subtitle = "Active Cycle",
                icon = Icons.Default.Analytics,
                accentColor = GoldCore,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "ACTIVE PLAYERS",
                value = "${telemetry.activePlayers}",
                subtitle = "Virtual Participants",
                icon = Icons.Default.Speed,
                accentColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "TOTAL SIMULATED PLAYS",
                value = "${telemetry.totalSimulatedPlays}",
                subtitle = "Current Round Volume",
                icon = Icons.Default.BarChart,
                accentColor = GreenLive,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "LAST UPDATE TIME",
                value = telemetry.formattedLastUpdate,
                subtitle = if (telemetry.isPaused) "STREAM PAUSED" else "Live WebSocket Stream",
                icon = Icons.Default.AccessTime,
                accentColor = if (telemetry.isPaused) Color(0xFFFFB300) else Color(0xFF64B5F6),
                modifier = Modifier.weight(1f)
            )
        }

        // B. SIDE-BY-SIDE ACTIVITY CARDS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SIDE A CARD
            SideActivityCard(
                sideLabel = "Side A",
                gameName = "Dragon / Red",
                simulatedPlays = telemetry.sideA.simulatedPlays,
                percentage = telemetry.sideA.percentage,
                isLeading = telemetry.sideA.isLeading,
                accentColor = NeonRedSideA,
                modifier = Modifier.weight(1f)
            )

            // SIDE B CARD
            SideActivityCard(
                sideLabel = "Side B",
                gameName = "Tiger / Blue",
                simulatedPlays = telemetry.sideB.simulatedPlays,
                percentage = telemetry.sideB.percentage,
                isLeading = telemetry.sideB.isLeading,
                accentColor = NeonBlueSideB,
                modifier = Modifier.weight(1f)
            )
        }

        // C. LARGE LIVE BAR CHART & COMPARATIVE VISUALIZATION
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardBg),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with dynamic leader highlight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                        Text(
                            text = "LIVE ACTIVITY DISTRIBUTION CHART",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Highlight indicator for the side receiving more activity
                    val leaderName = if (telemetry.sideA.isLeading) "Side A" else if (telemetry.sideB.isLeading) "Side B" else "Balanced"
                    val leaderColor = if (telemetry.sideA.isLeading) NeonRedSideA else if (telemetry.sideB.isLeading) NeonBlueSideB else GoldCore
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(leaderColor.copy(alpha = 0.2f))
                            .border(1.dp, leaderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🔥 HIGHER SIMULATED ACTIVITY: $leaderName",
                            color = leaderColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Smoothly animated progress widths
                val animatedPctA by animateFloatAsState(
                    targetValue = (telemetry.sideA.percentage.toFloat() / 100f).coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    label = "barA"
                )
                val animatedPctB = (1f - animatedPctA).coerceIn(0f, 1f)

                // Large Dual-Side Comparative Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0B101D))
                            .border(1.dp, Color(0xFF23304B), RoundedCornerShape(8.dp))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Side A Bar fill
                            if (animatedPctA > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .weight(animatedPctA.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFD50000), NeonRedSideA)
                                            )
                                        )
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "Side A: ${telemetry.sideA.formattedPercentage}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            // Side B Bar fill
                            if (animatedPctB > 0.01f) {
                                Box(
                                    modifier = Modifier
                                        .weight(animatedPctB.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(NeonBlueSideB, Color(0xFF0091EA))
                                            )
                                        )
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = "Side B: ${telemetry.sideB.formattedPercentage}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // Bar Footer: Exact numbers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Side A: ${telemetry.sideA.simulatedPlays} plays (${telemetry.sideA.formattedPercentage})",
                            color = NeonRedSideA,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total: ${telemetry.totalSimulatedPlays} simulated plays",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "Side B: ${telemetry.sideB.simulatedPlays} plays (${telemetry.sideB.formattedPercentage})",
                            color = NeonBlueSideB,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // D. RECENT ACTIVITY FEED & SIMULATION CONTROLS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recent Activity Feed List
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .weight(1.4f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.History, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Text(
                                text = "RECENT ACTIVITY FEED (REAL-TIME)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Auto-updating stream",
                            color = GreenLive,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(telemetry.recentActivityFeed) { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF090D18))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = event.avatar, fontSize = 13.sp)
                                    Text(
                                        text = event.playerAlias,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val isSideA = event.sideTarget.contains("A")
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSideA) NeonRedSideA.copy(alpha = 0.2f) else NeonBlueSideB.copy(alpha = 0.2f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = event.sideTarget,
                                            color = if (isSideA) NeonRedSideA else NeonBlueSideB,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (event.simulatedAmount > 0L) {
                                        Text(
                                            text = "+${event.simulatedAmount} pts",
                                            color = GoldCore,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = event.formattedTime,
                                        color = Color(0xFF64748B),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real-time Simulation Controls Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "TELEMETRY SIMULATION CONTROLS",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Simulate real-time stream shifts and test chart transitions.",
                            color = Color.LightGray,
                            fontSize = 9.sp
                        )

                        // Quick Inject Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonRedSideA.copy(alpha = 0.25f))
                                    .border(1.dp, NeonRedSideA, RoundedCornerShape(6.dp))
                                    .clickable { onInjectSimulatedPlays(5, 0) }
                                    .padding(vertical = 6.dp)
                                    .testTag("btn_inject_side_a"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+5 Side A", color = NeonRedSideA, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonBlueSideB.copy(alpha = 0.25f))
                                    .border(1.dp, NeonBlueSideB, RoundedCornerShape(6.dp))
                                    .clickable { onInjectSimulatedPlays(0, 5) }
                                    .padding(vertical = 6.dp)
                                    .testTag("btn_inject_side_b"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+5 Side B", color = NeonBlueSideB, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Pause / Resume Stream
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                                .clickable(onClick = onTogglePauseStream)
                                .padding(vertical = 6.dp)
                                .testTag("btn_toggle_pause_stream"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = if (telemetry.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (telemetry.isPaused) "Resume WebSocket Stream" else "Pause WebSocket Stream",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Advance Round manually
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF152238))
                                .clickable(onClick = onAdvanceRound)
                                .padding(vertical = 6.dp)
                                .testTag("btn_advance_round"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                Text("Advance Simulated Round", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // E. STRICT COMPLIANCE DISCLAIMER NOTICE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                Text(
                    text = "Analytics-Only Dashboard: Non-monetary game monitor. Strictly for telemetry visualization. Does not predict outcomes, alter game logic, or process real-money transactions.",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.5.sp
                )
            }
        }
    }
}

/**
 * Top KPI Metric Card
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Black)
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
            }
            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(text = subtitle, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Side Activity Card (Side A vs Side B)
 */
@Composable
private fun SideActivityCard(
    sideLabel: String,
    gameName: String,
    simulatedPlays: Int,
    percentage: Double,
    isLeading: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isLeading) accentColor else Color(0xFF1E293B),
        label = "borderColor"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(if (isLeading) 2.dp else 1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sideLabel,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = gameName,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }

                if (isLeading) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LEADING ACTIVITY 🔥",
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Exact prompt metrics format:
            // Side A: Simulated plays: 20, Percentage: 80%
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090D18))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Simulated plays:", color = Color.LightGray, fontSize = 12.sp)
                    Text(
                        text = "$simulatedPlays",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Percentage:", color = Color.LightGray, fontSize = 12.sp)
                    Text(
                        text = String.format(Locale.US, "%.1f%%", percentage),
                        color = accentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

/**
 * Tab 2: Security Audit Logs Viewer (Immutable audit trail)
 */
@Composable
private fun SecurityAuditLogsTab(auditLogs: List<AdminAuditLog>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                Text(
                    text = "SERVER AUDIT TRAIL (${auditLogs.size} ENTRIES)",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = "Tamper-Resistant Log Record",
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }

        if (auditLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCardBg),
                contentAlignment = Alignment.Center
            ) {
                Text("No administrative audit logs recorded yet.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(auditLogs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val isFail = log.action.contains("FAIL") || log.action.contains("LOCKOUT")
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isFail) Color(0x33FF5252) else Color(0x3300E676))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = log.action,
                                            color = if (isFail) Color(0xFFFF5252) else GreenLive,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Text(
                                        text = "Admin: ${log.adminId}",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = log.formattedTimestamp,
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp
                                )
                            }
                            Text(
                                text = log.details,
                                color = Color.White,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 3: Centralized Game Releases & Updates Management
 */
@Composable
private fun GameReleasesTab(
    releases: List<GameRelease>,
    currentVersion: String,
    onTogglePublishRelease: (String, Boolean) -> Unit,
    onSetInstalledVersion: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(releases) { release ->
            val isCurrent = release.versionName == currentVersion
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrent) CyanAccent else Color(0xFF1E293B)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "${release.versionName} (${release.title})",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyanAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text("CURRENT", color = CyanAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        Text(
                            text = "Release: ${release.releaseDate} • Code: ${release.versionCode}",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Switch(
                            checked = release.isPublished,
                            onCheckedChange = { onTogglePublishRelease(release.versionName, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyanAccent
                            )
                        )
                        if (!isCurrent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E293B))
                                    .clickable { onSetInstalledVersion(release.versionName) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Simulate Rollback", color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
