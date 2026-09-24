package com.example.ui.games

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CHIP_OPTIONS
import com.example.model.ChipDenomination
import com.example.model.ZooAnimal
import com.example.model.ZooBetTarget
import com.example.model.ZooCategory
import com.example.model.ZooGamePhase
import com.example.model.ZooHistoryItem
import com.example.model.ZooPlayerBets
import com.example.model.ZooTrackSlot
import com.example.model.ZOO_TRACK_SLOTS
import com.example.ui.components.BettingChipView
import com.example.ui.components.DragonTigerParticleEffect
import com.example.ui.components.formatCredits

// AAA Photorealistic Gold & Luxury Casino Theme Colors
private val GoldenFrameBright = Color(0xFFFFE082)
private val GoldenFrameCore = Color(0xFFFFD54F)
private val GoldenFrameDark = Color(0xFFC49000)
private val GoldenGlowYellow = Color(0xFFFFF59D)
private val BeastGreenCore = Color(0xFF0F5A2F)
private val BeastGreenDark = Color(0xFF082E18)
private val BirdBlueCore = Color(0xFF102A72)
private val BirdBlueDark = Color(0xFF08153B)
private val CyanNeonText = Color(0xFF00E5FF)
private val BetPillCyan = Color(0xFF00E5FF)

@Composable
fun ZooRouletteGame(
    credits: Long,
    phase: ZooGamePhase,
    countdown: Int,
    activeSlotIndex: Int,
    winningAnimal: ZooAnimal?,
    bets: ZooPlayerBets,
    selectedChip: ChipDenomination,
    historyList: List<ZooHistoryItem>,
    lastWin: Long,
    lastFee: Long,
    winParticleKey: Long,
    onSelectChip: (ChipDenomination) -> Unit,
    onPlaceBet: (ZooBetTarget) -> Unit,
    onClearBets: () -> Unit,
    onDoubleBets: () -> Unit,
    onRebet: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRules: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulse"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF092918),
                        Color(0xFF04140C),
                        Color(0xFF020A06)
                    ),
                    radius = 2000f
                )
            )
    ) {
        // Winning celebration particles
        if (winParticleKey > 0L) {
            DragonTigerParticleEffect(
                triggerKey = winParticleKey,
                winner = com.example.model.RoundWinner.TIE,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Entire outer golden frame with corner foliage styling
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // =========================================================================
            // 1. TOP ORNATE CREST HEADER: "Drawing 18"
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Corner Overlay)
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0x66000000))
                        .testTag("zoo_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldenFrameBright,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Center Ornate Golden "Drawing 18" Banner
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFFEE58),
                                    Color(0xFFFFB300),
                                    Color(0xFFB78103)
                                )
                            )
                        )
                        .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
                        .padding(horizontal = 24.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (phase == ZooGamePhase.SPINNING) "Spinning" else "Drawing",
                            color = Color(0xFF2C1600),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = String.format("%02d", countdown),
                            color = Color(0xFFD50000),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Spacer balance pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x99000000))
                        .border(1.dp, GoldenFrameCore.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PKR ${formatCredits(credits)}",
                        color = GoldenFrameBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // =========================================================================
            // 2. MAIN CENTER BOARD: 26-SLOT OUTER TRACK + INNER BETTING CARDS
            // =========================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC030E08))
                    .border(2.dp, GoldenFrameCore, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // TOP ROW (9 Outer Track Tiles)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (i in 0..8) {
                            val slot = ZOO_TRACK_SLOTS[i]
                            ZooPerimeterTile(
                                slot = slot,
                                isActive = activeSlotIndex == slot.index,
                                beaconPulse = beaconPulse,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // MIDDLE BODY: LEFT TRACK TILES + INNER BETTING BOARD + RECORD BAR
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // LEFT COLUMN (7 Outer Track Tiles)
                        Column(
                            modifier = Modifier
                                .width(44.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            for (i in 19..25) {
                                val slot = ZOO_TRACK_SLOTS[i]
                                ZooPerimeterTile(
                                    slot = slot,
                                    isActive = activeSlotIndex == slot.index,
                                    beaconPulse = beaconPulse,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                        }

                        // INNER BOARD: BEAST (LEFT) + CENTER (HERO + TOTAL BET + SHARK) + BIRD (RIGHT)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // 1. BEAST SECTION (GREEN CARDS)
                            ZooBeastPanel(
                                bets = bets,
                                onPlaceBet = onPlaceBet,
                                modifier = Modifier
                                    .weight(1.05f)
                                    .fillMaxHeight()
                            )

                            // 2. CENTER HERO + TOTAL BET + SHARK ZONE
                            ZooCenterHeroSharkSection(
                                bets = bets,
                                onPlaceBet = onPlaceBet,
                                modifier = Modifier
                                    .weight(1.35f)
                                    .fillMaxHeight()
                            )

                            // 3. BIRD SECTION (BLUE CARDS) + TAKE ALL ORB
                            ZooBirdPanel(
                                bets = bets,
                                onPlaceBet = onPlaceBet,
                                modifier = Modifier
                                    .weight(1.05f)
                                    .fillMaxHeight()
                            )
                        }

                        // RIGHT COLUMN: RECORD PANEL (VERTICAL PREVIOUS OUTCOMES)
                        ZooRecordBar(
                            historyList = historyList,
                            modifier = Modifier
                                .width(44.dp)
                                .fillMaxHeight()
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // BOTTOM ROW (10 Outer Track Tiles: 9..18)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (i in 9..18) {
                            val slot = ZOO_TRACK_SLOTS[i]
                            ZooPerimeterTile(
                                slot = slot,
                                isActive = activeSlotIndex == slot.index,
                                beaconPulse = beaconPulse,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 3. BOTTOM CONTROL DOCK: PROFILE, WATCHING, CHIPS, REBET
            // =========================================================================
            ZooBottomBar(
                credits = credits,
                selectedChip = selectedChip,
                phase = phase,
                onSelectChip = onSelectChip,
                onRebet = onRebet,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =============================================================================
// SUB-PANELS & CARDS
// =============================================================================

@Composable
private fun ZooPerimeterTile(
    slot: ZooTrackSlot,
    isActive: Boolean,
    beaconPulse: Float,
    modifier: Modifier = Modifier
) {
    val isGoldenBox = slot.isGoldenBox
    val isGoldenToad = slot.animal == ZooAnimal.GOLDEN_TOAD
    val isWild = slot.animal == ZooAnimal.WILD_CHEST
    val isShark = slot.animal == ZooAnimal.SHARK

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                when {
                    isActive -> Brush.verticalGradient(
                        listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F), Color(0xFFFF8F00))
                    )
                    isGoldenBox -> Brush.verticalGradient(
                        listOf(Color(0xFFFFD54F), Color(0xFFC49000), Color(0xFF6D4C00))
                    )
                    isGoldenToad -> Brush.verticalGradient(
                        listOf(Color(0xFFFFB300), Color(0xFF6D4C00))
                    )
                    isWild -> Brush.verticalGradient(
                        listOf(Color(0xFF5D4037), Color(0xFF3E2723))
                    )
                    isShark -> Brush.verticalGradient(
                        listOf(Color(0xFF00384D), Color(0xFF001824))
                    )
                    else -> Brush.verticalGradient(
                        listOf(Color(0xFF081B11), Color(0xFF020704))
                    )
                }
            )
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) Color.White else if (isGoldenBox) GoldenFrameBright else GoldenFrameCore.copy(alpha = 0.4f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isGoldenToad) {
            Image(
                painter = painterResource(id = R.drawable.img_golden_toad),
                contentDescription = "Golden Toad x100",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color(0xCC000000))
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "x100",
                    color = GoldenFrameBright,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else if (isWild) {
            Image(
                painter = painterResource(id = R.drawable.img_wild_chest),
                contentDescription = "Wild Chest",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color(0xCC000000))
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Wild",
                    color = GoldenFrameBright,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = slot.animal.emoji,
                    fontSize = 14.sp
                )
            }
        }

        // Active highlighted beacon
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x44FFFFFF))
            )
        }
    }
}

// -----------------------------------------------------------------------------
// BEAST SECTION (GREEN CARDS)
// -----------------------------------------------------------------------------
@Composable
private fun ZooBeastPanel(
    bets: ZooPlayerBets,
    onPlaceBet: (ZooBetTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // TOP ROW: Monkey x8 + Rabbit x6
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            ZooAnimalBetCard(
                name = "Monkey",
                multiplier = "x8",
                emoji = "🐒",
                betAmount = bets.monkey,
                target = ZooBetTarget.MONKEY,
                onPlaceBet = onPlaceBet,
                isBeast = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ZooAnimalBetCard(
                name = "Rabbit",
                multiplier = "x6",
                emoji = "🐇",
                betAmount = bets.rabbit,
                target = ZooBetTarget.RABBIT,
                onPlaceBet = onPlaceBet,
                isBeast = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // MIDDLE ROW: Lion x12 + Panda x8
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            ZooAnimalBetCard(
                name = "Lion",
                multiplier = "x12",
                emoji = "🦁",
                betAmount = bets.lion,
                target = ZooBetTarget.LION,
                onPlaceBet = onPlaceBet,
                isBeast = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ZooAnimalBetCard(
                name = "Panda",
                multiplier = "x8",
                emoji = "🐼",
                betAmount = bets.panda,
                target = ZooBetTarget.PANDA,
                onPlaceBet = onPlaceBet,
                isBeast = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // BOTTOM ROW: Beast x2 (Wide Card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF14532D), Color(0xFF052E16))
                    )
                )
                .border(1.dp, GoldenFrameCore, RoundedCornerShape(8.dp))
                .clickable { onPlaceBet(ZooBetTarget.BEAST_GENERAL) }
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .testTag("zoo_bet_beast_x2"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🐾 ", fontSize = 14.sp)
                Text(
                    text = "Beast x2",
                    color = Color(0xFF86EFAC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
            if (bets.beastGeneral > 0L) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD000000))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = bets.beastGeneral.toString(),
                        color = BetPillCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CENTER HERO + TOTAL BET + SHARK ZONE
// -----------------------------------------------------------------------------
@Composable
private fun ZooCenterHeroSharkSection(
    bets: ZooPlayerBets,
    onPlaceBet: (ZooBetTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // 1. HERO ARCH: 3D Roaring Lion & Blue Peacock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.25f)
                .clip(RoundedCornerShape(10.dp))
                .border(1.5.dp, GoldenFrameCore, RoundedCornerShape(10.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_zoo_hero_lion_peacock),
                contentDescription = "Roaring Lion & Blue Peacock 3D",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // 2. TOTAL BET PILL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1E1035), Color(0xFF3B1E6D), Color(0xFF1E1035))
                    )
                )
                .border(1.dp, GoldenFrameCore, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Total Bet",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (bets.total > 0L) bets.total.toString() else "9550",
                    color = GoldenFrameBright,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // 3. SHARK ZONE CARD: Deep Blue Oceanic + 3D Shark
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF001B30))
                .border(1.dp, CyanNeonText.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .clickable { onPlaceBet(ZooBetTarget.SHARK) }
                .testTag("zoo_bet_shark_card")
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_shark_zone),
                contentDescription = "Shark x24 3D",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Top bet pill badge
            if (bets.shark > 0L) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xDD000000))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = bets.shark.toString(),
                        color = BetPillCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cyan Text Overlay: Shark x24
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Shark x24",
                    color = CyanNeonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// BIRD SECTION (BLUE CARDS) + TAKE ALL ORB
// -----------------------------------------------------------------------------
@Composable
private fun ZooBirdPanel(
    bets: ZooPlayerBets,
    onPlaceBet: (ZooBetTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // TOP ROW: Swallow x8 + Pigeon x8
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            ZooAnimalBetCard(
                name = "Swallow",
                multiplier = "x8",
                emoji = "🐦",
                betAmount = bets.swallow,
                target = ZooBetTarget.SWALLOW,
                onPlaceBet = onPlaceBet,
                isBeast = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ZooAnimalBetCard(
                name = "Pigeon",
                multiplier = "x8",
                emoji = "🕊️",
                betAmount = bets.pigeon,
                target = ZooBetTarget.PIGEON,
                onPlaceBet = onPlaceBet,
                isBeast = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // MIDDLE ROW: Peacock x8 + Eagle x12 + Take All Orb
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ZooAnimalBetCard(
                name = "Peacock",
                multiplier = "x8",
                emoji = "🦚",
                betAmount = bets.peacock,
                target = ZooBetTarget.PEACOCK,
                onPlaceBet = onPlaceBet,
                isBeast = false,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                ZooAnimalBetCard(
                    name = "Eagle",
                    multiplier = "x12",
                    emoji = "🦅",
                    betAmount = bets.eagle,
                    target = ZooBetTarget.EAGLE,
                    onPlaceBet = onPlaceBet,
                    isBeast = false,
                    modifier = Modifier.fillMaxSize()
                )

                // 3D Purple Glowing "Take All" Orb floating over the right edge
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFE040FB),
                                    Color(0xFF7B1FA2),
                                    Color(0xFF311B92)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFFFFD54F), CircleShape)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Take",
                            color = Color(0xFFFFE082),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "All",
                            color = Color(0xFFFFE082),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // BOTTOM ROW: Bird x2 (Wide Card)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                    )
                )
                .border(1.dp, GoldenFrameCore, RoundedCornerShape(8.dp))
                .clickable { onPlaceBet(ZooBetTarget.BIRD_GENERAL) }
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .testTag("zoo_bet_bird_x2"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Bird x2",
                color = Color(0xFF93C5FD),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            if (bets.birdGeneral > 0L) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD000000))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = bets.birdGeneral.toString(),
                        color = BetPillCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// INDIVIDUAL ANIMAL BETTING CARD
// -----------------------------------------------------------------------------
@Composable
private fun ZooAnimalBetCard(
    name: String,
    multiplier: String,
    emoji: String,
    betAmount: Long,
    target: ZooBetTarget,
    onPlaceBet: (ZooBetTarget) -> Unit,
    isBeast: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isBeast) Brush.verticalGradient(listOf(Color(0xFF06331A), Color(0xFF02170B)))
                else Brush.verticalGradient(listOf(Color(0xFF09204A), Color(0xFF030D21)))
            )
            .border(
                1.dp,
                if (isBeast) Color(0xFF22C55E).copy(alpha = 0.7f) else Color(0xFF3B82F6).copy(alpha = 0.7f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onPlaceBet(target) }
            .padding(2.dp)
            .testTag("zoo_card_${target.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        // Top bet amount badge
        if (betAmount > 0L) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xDD000000))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = betAmount.toString(),
                    color = BetPillCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Center Emoji / Animal Icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = multiplier,
                    color = CyanNeonText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// RIGHT RECORD COLUMN
// -----------------------------------------------------------------------------
@Composable
private fun ZooRecordBar(
    historyList: List<ZooHistoryItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xDD05130C))
            .border(1.dp, GoldenFrameCore.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Record Title
        Text(
            text = "Record",
            color = GoldenFrameBright,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(4.dp))

        // History outcome badges matching reference screenshot
        val defaultIcons = listOf("🦁", "🕊️", "🕊️", "🕊️", "🕊️", "🐼", "🐒", "🦁")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (historyList.isEmpty()) {
                items(defaultIcons) { ic ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0x44000000))
                            .border(0.5.dp, GoldenFrameCore.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = ic, fontSize = 12.sp)
                    }
                }
            } else {
                items(historyList.takeLast(8).reversed()) { rec ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0x44000000))
                            .border(0.5.dp, GoldenFrameCore.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = rec.winningAnimal.emoji, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// BOTTOM CONTROL DOCK
// -----------------------------------------------------------------------------
@Composable
private fun ZooBottomBar(
    credits: Long,
    selectedChip: ChipDenomination,
    phase: ZooGamePhase,
    onSelectChip: (ChipDenomination) -> Unit,
    onRebet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Center "Watching..." Leafy Crest Banner
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0D47A1), Color(0xFF1B5E20), Color(0xFF0D47A1))
                    )
                )
                .border(1.dp, GoldenFrameBright, RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp, vertical = 2.dp)
        ) {
            Text(
                text = "Watching...",
                color = GoldenFrameBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Main dock row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xE605150D))
                .border(1.dp, GoldenFrameCore.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: User Avatar & Balance
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD54F))
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👩", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Guest2538041",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "PKR ${formatCredits(credits)}",
                        color = GoldenFrameBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Center Chips (10, 50, 100, 500, 1000)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CHIP_OPTIONS.forEach { chip ->
                    BettingChipView(
                        chip = chip,
                        isSelected = selectedChip == chip,
                        onClick = { onSelectChip(chip) },
                        size = 34.dp,
                        modifier = Modifier.testTag("zoo_chip_${chip.value}")
                    )
                }
            }

            // Right: Chevron and ReBet Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Chevron >>
                Text(
                    text = "»",
                    color = GoldenFrameBright,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                // ReBet Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00796B), Color(0xFF004D40))
                            )
                        )
                        .border(1.dp, Color(0xFF64FFDA), RoundedCornerShape(20.dp))
                        .clickable(enabled = phase == ZooGamePhase.BETTING, onClick = onRebet)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("zoo_rebet_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ReBet",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Bottom instruction notice
        Text(
            text = "While watching, you need 50 chip COINS to play",
            color = Color(0xFFA5D6A7),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}
