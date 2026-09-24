package com.example.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BetOption
import com.example.model.CHIP_OPTIONS
import com.example.model.ChipDenomination
import com.example.model.GamePhase
import com.example.model.HistoryRecord
import com.example.model.PlayerBet
import com.example.model.PlayingCard
import com.example.model.RoundWinner
import com.example.ui.components.AnimatedDragonCanvas
import com.example.ui.components.AnimatedTigerCanvas
import com.example.ui.components.AsianBackgroundCanvas
import com.example.ui.components.BettingChipView
import com.example.ui.components.DragonTigerParticleEffect
import com.example.ui.components.PlayingCardView
import com.example.ui.components.formatCredits
import com.example.ui.theme.DragonBlue
import com.example.ui.theme.DragonGlow
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.TableTheme
import com.example.ui.theme.TieGreen
import com.example.ui.theme.TieGlow
import com.example.ui.theme.TigerGlow
import com.example.ui.theme.TigerOrange
import com.example.ui.theme.TigerRed

@Composable
fun DragonTigerGame(
    tableTheme: TableTheme,
    credits: Long,
    gamePhase: GamePhase,
    countdownSeconds: Int,
    countdownRemaining: Int,
    dragonCard: PlayingCard?,
    tigerCard: PlayingCard?,
    isCardsRevealed: Boolean,
    roundWinner: RoundWinner?,
    playerBet: PlayerBet,
    selectedChip: ChipDenomination,
    historyList: List<HistoryRecord>,
    lastWinAmount: Long,
    lastFeeDeducted: Long = 0L,
    showWinParticleKey: Long,
    onSelectChip: (ChipDenomination) -> Unit,
    onPlaceBet: (BetOption) -> Unit,
    onClearBets: () -> Unit,
    onDoubleBets: () -> Unit,
    onRebet: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToLobby: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val maxHeight = maxHeight

        // 1. Full-screen Asian Atmosphere Background
        AsianBackgroundCanvas(tableTheme = tableTheme)

        // 2. Particle Explosion Overlay when Player Wins
        if (showWinParticleKey > 0L && roundWinner != null) {
            DragonTigerParticleEffect(
                winner = roundWinner,
                triggerKey = showWinParticleKey,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 3. Main Game Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Dragon & Tiger Mythical Illustrations + Playing Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mythical Beasts & Center Cards
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Left: Animated Azure Dragon
                    AnimatedDragonCanvas(
                        isWinning = roundWinner == RoundWinner.DRAGON,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(width = 120.dp, height = 110.dp)
                    )

                    // Right: Animated Fiery Tiger
                    AnimatedTigerCanvas(
                        isWinning = roundWinner == RoundWinner.TIGER,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(width = 120.dp, height = 110.dp)
                    )

                    // Center: Two Large Playing Cards with VS emblem
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Dragon Card
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DRAGON",
                                color = tableTheme.dragonColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            PlayingCardView(
                                card = dragonCard,
                                isRevealed = isCardsRevealed,
                                isWinning = roundWinner == RoundWinner.DRAGON,
                                glowColor = DragonGlow,
                                cardBackGradient = tableTheme.cardBackGradient,
                                width = 58.dp,
                                height = 82.dp
                            )
                        }

                        // Glowing VS Emblem
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22111E))
                                .border(1.5.dp, ImperialGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "VS",
                                color = ImperialGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Tiger Card
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TIGER",
                                color = tableTheme.tigerColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            PlayingCardView(
                                card = tigerCard,
                                isRevealed = isCardsRevealed,
                                isWinning = roundWinner == RoundWinner.TIGER,
                                glowColor = TigerGlow,
                                cardBackGradient = tableTheme.cardBackGradient,
                                width = 58.dp,
                                height = 82.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Horizontal Result-History Bar underneath the cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xBB110B17))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ROADMAP:",
                            color = ImperialGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Circular indicators for history
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.width(180.dp)
                        ) {
                            items(historyList.takeLast(10).reversed()) { item ->
                                val (color, label) = when (item.winner) {
                                    RoundWinner.DRAGON -> tableTheme.dragonColor to "D"
                                    RoundWinner.TIGER -> tableTheme.tigerColor to "T"
                                    RoundWinner.TIE -> tableTheme.tieColor to "Tie"
                                }
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(0.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // Open Roadmap modal button
                    Row(
                        modifier = Modifier
                            .clickable(onClick = onOpenHistory)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "All History",
                            tint = ImperialGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "STATS",
                            color = ImperialGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Central Status Indicator ("Betting Open" / "Stop Betting" / "Dealing Cards" / "Result")
                RoundStatusBanner(
                    gamePhase = gamePhase,
                    winner = roundWinner,
                    lastWin = lastWinAmount,
                    lastFee = lastFeeDeducted
                )
            }

            // MIDDLE SECTION: 3 Large Selectable Betting Panels (Dragon, Tie, Tiger)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                // Dragon vs Tiger Ratio Indicator Bar
                val totalRounds = historyList.size.coerceAtLeast(1)
                val dragonWinsCount = historyList.count { it.winner == RoundWinner.DRAGON }
                val tigerWinsCount = historyList.count { it.winner == RoundWinner.TIGER }
                val dragonRatio = (dragonWinsCount.toFloat() / totalRounds).coerceIn(0.1f, 0.9f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Dragon ${((dragonRatio) * 100).toInt()}%",
                        color = tableTheme.dragonColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tie 1:8",
                        color = tableTheme.tieColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tiger ${((1f - dragonRatio) * 100).toInt()}%",
                        color = tableTheme.tigerColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Smooth ratio bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(tableTheme.tigerColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(dragonRatio)
                            .height(6.dp)
                            .background(tableTheme.dragonColor)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The 3 Betting Panels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. DRAGON PANEL (Left)
                    BetPanel(
                        title = "DRAGON",
                        payout = "1:1",
                        chineseTitle = "龍",
                        userBet = playerBet.dragon,
                        baseColor = tableTheme.dragonColor,
                        glowColor = DragonGlow,
                        isWinning = roundWinner == RoundWinner.DRAGON,
                        isBettingOpen = gamePhase == GamePhase.BETTING_OPEN,
                        modifier = Modifier.weight(1.1f),
                        onClick = { onPlaceBet(BetOption.DRAGON) }
                    )

                    // 2. TIE PANEL (Center) with Countdown Timer
                    TieBetPanel(
                        userBet = playerBet.tie,
                        countdownRemaining = countdownRemaining,
                        isBettingOpen = gamePhase == GamePhase.BETTING_OPEN,
                        isWinning = roundWinner == RoundWinner.TIE,
                        tieColor = tableTheme.tieColor,
                        modifier = Modifier.weight(0.9f),
                        onClick = { onPlaceBet(BetOption.TIE) }
                    )

                    // 3. TIGER PANEL (Right)
                    BetPanel(
                        title = "TIGER",
                        payout = "1:1",
                        chineseTitle = "虎",
                        userBet = playerBet.tiger,
                        baseColor = tableTheme.tigerColor,
                        glowColor = TigerGlow,
                        isWinning = roundWinner == RoundWinner.TIGER,
                        isBettingOpen = gamePhase == GamePhase.BETTING_OPEN,
                        modifier = Modifier.weight(1.1f),
                        onClick = { onPlaceBet(BetOption.TIGER) }
                    )
                }
            }

            // BOTTOM SECTION: Chips Selector, Balance, and Betting Action Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xE60D0914))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Action Controls: Clear, Double, Rebet, Total Bet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onClearBets,
                            enabled = playerBet.hasBets() && gamePhase == GamePhase.BETTING_OPEN,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("clear_bets_button")
                        ) {
                            Text("Clear", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onDoubleBets,
                            enabled = playerBet.hasBets() && gamePhase == GamePhase.BETTING_OPEN,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("double_bets_button")
                        ) {
                            Text("2x Double", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onRebet,
                            enabled = !playerBet.hasBets() && gamePhase == GamePhase.BETTING_OPEN,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("rebet_button")
                        ) {
                            Text("Rebet", fontSize = 11.sp)
                        }
                    }

                    // Total Bet on Table
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TOTAL BET",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${playerBet.total} PTS",
                            color = ImperialGoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Casino Chip Denominations Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CHIP_OPTIONS.forEach { chip ->
                        BettingChipView(
                            chip = chip,
                            isSelected = selectedChip == chip,
                            onClick = { onSelectChip(chip) },
                            size = 44.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Player Balance & Quick Theme/Settings bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🪙", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Balance: ",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${formatCredits(credits)} PTS",
                            color = ImperialGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Lobby button
                        if (onNavigateToLobby != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF23192B))
                                    .border(1.dp, tableTheme.accentGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable(onClick = onNavigateToLobby)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("bottom_lobby_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Lobby",
                                        tint = tableTheme.accentGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LOBBY",
                                        color = tableTheme.accentGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Quick theme button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF23192B))
                                .border(1.dp, tableTheme.accentGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenSettings)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("bottom_theme_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Theme",
                                    tint = tableTheme.accentGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tableTheme.displayName,
                                    color = tableTheme.accentGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BetPanel(
    title: String,
    payout: String,
    chineseTitle: String,
    userBet: Long,
    baseColor: Color,
    glowColor: Color,
    isWinning: Boolean,
    isBettingOpen: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "panel_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .height(115.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        baseColor.copy(alpha = if (isWinning) 0.6f else 0.25f),
                        Color(0xFF130A18)
                    )
                )
            )
            .border(
                width = if (isWinning) 2.5.dp else 1.dp,
                color = if (isWinning) ImperialGold else if (userBet > 0) baseColor else Color(0x44FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = isBettingOpen,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = glowColor),
                onClick = onClick
            )
            .padding(8.dp)
            .testTag("bet_panel_$title"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Title & Chinese Character
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = if (isWinning) ImperialGoldLight else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = chineseTitle,
                    color = baseColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Payout Information
            Text(
                text = "PAYS $payout (3% Fee)",
                color = ImperialGold.copy(alpha = 0.9f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )

            // Player's Bet Badge
            if (userBet > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(baseColor)
                        .border(1.dp, ImperialGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$userBet PTS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Text(
                    text = if (isBettingOpen) "TAP TO BET" else "LOCKED",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TieBetPanel(
    userBet: Long,
    countdownRemaining: Int,
    isBettingOpen: Boolean,
    isWinning: Boolean,
    tieColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(115.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        tieColor.copy(alpha = if (isWinning) 0.6f else 0.2f),
                        Color(0xFF0F1412)
                    )
                )
            )
            .border(
                width = if (isWinning) 2.5.dp else 1.dp,
                color = if (isWinning) ImperialGold else if (userBet > 0) tieColor else Color(0x44FFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = isBettingOpen,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = TieGlow),
                onClick = onClick
            )
            .padding(8.dp)
            .testTag("bet_panel_TIE"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Title
            Text(
                text = "TIE 和",
                color = if (isWinning) ImperialGoldLight else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )

            // Payout Badge (1:8)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(ImperialGold)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "1:8 (3% Fee)",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Countdown Timer or Bet Amount
            if (userBet > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(tieColor)
                        .border(1.dp, ImperialGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$userBet PTS",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else if (isBettingOpen) {
                // Countdown ring
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⏳ ${countdownRemaining}s",
                        color = if (countdownRemaining <= 3) Color(0xFFFF5252) else ImperialGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "LOCKED",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RoundStatusBanner(
    gamePhase: GamePhase,
    winner: RoundWinner?,
    lastWin: Long,
    lastFee: Long = 0L
) {
    val (bgColor, borderCol, statusText) = when (gamePhase) {
        GamePhase.BETTING_OPEN -> Triple(
            Color(0xEE1A1322),
            ImperialGold,
            "BETTING OPEN • PLACE YOUR CHIPS"
        )
        GamePhase.STOP_BETTING -> Triple(
            Color(0xEE331111),
            Color(0xFFFF5252),
            "STOP BETTING • SHUFFLING SHOE"
        )
        GamePhase.DEALING_CARDS -> Triple(
            Color(0xEE171C2E),
            DragonGlow,
            "DEALING CARDS..."
        )
        GamePhase.RESULT -> {
            val winMsg = when (winner) {
                RoundWinner.DRAGON -> "DRAGON WINS! 🐉"
                RoundWinner.TIGER -> "TIGER WINS! 🐅"
                RoundWinner.TIE -> "TIE GAME! ⚡ (1:8 Payout)"
                null -> "ROUND COMPLETE"
            }
            val winBonus = if (lastWin > 0) {
                if (lastFee > 0) " • WON +$lastWin PTS (-$lastFee 3% Fee) 🎉"
                else " • WON +$lastWin PTS! 🎉"
            } else ""
            Triple(
                Color(0xEE2A1E08),
                ImperialGoldLight,
                winMsg + winBonus
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.5.dp, borderCol, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("round_status_banner"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusText,
            color = borderCol,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
    }
}
