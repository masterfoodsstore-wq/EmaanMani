package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GameStatus
import com.example.model.LaneStep
import kotlin.math.sin

@Composable
fun ChickenRoadCanvas(
    currentLaneIndex: Int,
    lanes: List<LaneStep>,
    gameStatus: GameStatus,
    trappedLaneIndex: Int?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Smoothly scroll camera to keep chicken centered
    LaunchedEffect(currentLaneIndex) {
        if (currentLaneIndex > 2) {
            val targetScroll = (currentLaneIndex - 2) * 110
            scrollState.animateScrollTo(targetScroll)
        } else {
            scrollState.animateScrollTo(0)
        }
    }

    // Idle breathing/bobbing animation
    val infiniteTransition = rememberInfiniteTransition(label = "idle_bob")
    val idleBob by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chicken_bob"
    )

    val flameFlicker by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_flicker"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E253E),
                        Color(0xFF192036),
                        Color(0xFF14192B)
                    )
                )
            )
            .testTag("chicken_road_container")
    ) {
        // Background Brick Texture Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val brickHeight = 36f
            val brickWidth = 90f

            // Faint stone bricks
            var y = 0f
            var row = 0
            while (y < h - 50f) {
                val offsetX = if (row % 2 == 0) 0f else brickWidth / 2
                var x = -brickWidth + offsetX
                while (x < w + brickWidth) {
                    drawRoundRect(
                        color = Color(0x0CFFFFFF),
                        topLeft = Offset(x, y),
                        size = Size(brickWidth - 6f, brickHeight - 6f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    x += brickWidth
                }
                y += brickHeight
                row++
            }

            // Bottom curb line
            drawLine(
                color = Color(0xFF2C3658),
                start = Offset(0f, h - 34f),
                end = Offset(w, h - 34f),
                strokeWidth = 6f
            )
            drawLine(
                color = Color(0xFF0F1321),
                start = Offset(0f, h - 30f),
                end = Offset(w, h - 30f),
                strokeWidth = 4f
            )
        }

        // Horizontal Scrollable Road Lanes
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
                .padding(bottom = 8.dp)
        ) {
            // Lane 0: Start Zone / Safety Pad
            LaneColumn(
                isStartPad = true,
                laneIndex = 0,
                multiplier = 1.0f,
                scorePoints = 0L,
                isCurrent = currentLaneIndex == 0,
                isPassed = currentLaneIndex > 0,
                isTrapped = false,
                isStepping = gameStatus == GameStatus.STEPPING && currentLaneIndex == 0,
                hasChicken = currentLaneIndex == 0,
                gameStatus = gameStatus,
                idleBob = idleBob,
                flameFlicker = flameFlicker
            )

            // Lanes 1 to N
            lanes.forEach { lane ->
                val isCurrent = currentLaneIndex == lane.index
                val isPassed = currentLaneIndex > lane.index
                val isTrapped = trappedLaneIndex == lane.index

                LaneColumn(
                    isStartPad = false,
                    laneIndex = lane.index,
                    multiplier = lane.multiplier,
                    scorePoints = lane.scorePoints,
                    isCurrent = isCurrent,
                    isPassed = isPassed,
                    isTrapped = isTrapped,
                    isStepping = gameStatus == GameStatus.STEPPING && isCurrent,
                    hasChicken = isCurrent,
                    gameStatus = gameStatus,
                    idleBob = idleBob,
                    flameFlicker = flameFlicker
                )
            }
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
private fun LaneColumn(
    isStartPad: Boolean,
    laneIndex: Int,
    multiplier: Float,
    scorePoints: Long,
    isCurrent: Boolean,
    isPassed: Boolean,
    isTrapped: Boolean,
    isStepping: Boolean,
    hasChicken: Boolean,
    gameStatus: GameStatus,
    idleBob: Float,
    flameFlicker: Float
) {
    val laneWidth = 105.dp

    Box(
        modifier = Modifier
            .width(laneWidth)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        // Vertical dashed lane separator on the right
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .width(2.dp)
        ) {
            drawLine(
                color = Color(0x40FFFFFF),
                start = Offset(0f, 10f),
                end = Offset(0f, size.height - 35f),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
            )
        }

        // Active lane highlight glow
        if (isCurrent && !isTrapped) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x0022C55E),
                                Color(0x1822C55E),
                                Color(0x3022C55E),
                                Color(0x0522C55E)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Progression Indicator matching reference
            CircularProgressionBadge(
                isStartPad = isStartPad,
                laneIndex = laneIndex,
                multiplier = multiplier,
                scorePoints = scorePoints,
                isCurrent = isCurrent,
                isPassed = isPassed,
                isTrapped = isTrapped
            )

            Spacer(modifier = Modifier.weight(1f))

            // Chicken Sprite (when positioned on this lane)
            Box(
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (hasChicken) {
                    ChickenSprite(
                        isStepping = isStepping,
                        isCrashed = gameStatus == GameStatus.CRASHED,
                        isCollected = gameStatus == GameStatus.COLLECTED,
                        idleBob = idleBob
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sewer Grate / Trap Hatch at bottom of lane
            SewerGrateHatch(
                isTrapped = isTrapped,
                isPassed = isPassed,
                flameFlicker = flameFlicker
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun CircularProgressionBadge(
    isStartPad: Boolean,
    laneIndex: Int,
    multiplier: Float,
    scorePoints: Long,
    isCurrent: Boolean,
    isPassed: Boolean,
    isTrapped: Boolean
) {
    val badgeSize = 64.dp

    if (isStartPad) {
        // Gold Coin Start Badge matching reference
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFDF00), Color(0xFFD4AF37), Color(0xFF996515))
                    )
                )
                .border(3.dp, Color(0xFFFFF176), CircleShape)
                .border(5.dp, Color(0xFF8D6E14), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Embossed chicken drumstick / coin badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🍗",
                    fontSize = 22.sp
                )
                Text(
                    text = "START",
                    color = Color(0xFF5D4037),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    } else if (isTrapped) {
        // Crashed / Trap Badge
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFEF4444), Color(0xFF991B1B))
                    )
                )
                .border(2.5.dp, Color(0xFFFCA5A5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "💥",
                    fontSize = 20.sp
                )
                Text(
                    text = "TRAP",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    } else if (isCurrent) {
        // Glowing Neon Green Active Badge matching reference
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF22C55E), Color(0xFF16A34A), Color(0xFF14532D))
                    )
                )
                .border(2.5.dp, Color(0xFF86EFAC), CircleShape)
                .border(4.dp, Color(0xFF052E16), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${multiplier}x",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$scorePoints pts",
                    color = Color(0xFFDCFCE7),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else if (isPassed) {
        // Passed milestone badge (subtle golden check)
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2E3B68), Color(0xFF1B233D))
                    )
                )
                .border(2.dp, Color(0xFF4ADE80), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${multiplier}x",
                    color = Color(0xFF86EFAC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "✓ SAFE",
                    color = Color(0xFF4ADE80),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    } else {
        // Inactive / Upcoming Sunken Blue-Gray Badge matching reference
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2C3555), Color(0xFF20273F), Color(0xFF161B2C))
                    )
                )
                .border(2.5.dp, Color(0xFF3B4772), CircleShape)
                .border(4.5.dp, Color(0xFF121625), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${multiplier}x",
                    color = Color(0xFFC7D2FE),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "LANE $laneIndex",
                    color = Color(0xFF7582A8),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ChickenSprite(
    isStepping: Boolean,
    isCrashed: Boolean,
    isCollected: Boolean,
    idleBob: Float
) {
    // Stepping hop arc animation
    val hopOffsetY = if (isStepping) -18f else idleBob
    val rotation = when {
        isCrashed -> 25f
        isStepping -> -8f
        isCollected -> 0f
        else -> 0f
    }
    val scale = when {
        isStepping -> 1.12f
        isCollected -> 1.15f
        else -> 1.0f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset { IntOffset(0, hopOffsetY.toInt()) }
            .rotate(rotation)
            .scale(scale)
    ) {
        if (isCollected) {
            Text(
                text = "✨ WIN! ✨",
                color = Color(0xFFFFD54F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B2138))
                .border(2.dp, if (isCrashed) Color(0xFFEF4444) else Color(0x30FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.chicken_character),
                contentDescription = "Chicken Player",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Cartoon reaction overlay
            if (isCrashed) {
                Text(
                    text = "😵",
                    fontSize = 32.sp
                )
            }
        }
    }
}

@Composable
private fun SewerGrateHatch(
    isTrapped: Boolean,
    isPassed: Boolean,
    flameFlicker: Float
) {
    Box(
        modifier = Modifier
            .width(68.dp)
            .height(34.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (isTrapped) {
            // Trapped lane shows animated flames bursting from the grate, matching reference image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-8).dp)
                    .scale(flameFlicker),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔥",
                    fontSize = 28.sp
                )
            }
        }

        // The Archway sewer drain grate with vertical bars matching reference
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Arch background
            drawRoundRect(
                color = if (isTrapped) Color(0xFF450A0A) else Color(0xFF0F1321),
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(h / 2, h / 2)
            )

            // Arch border
            drawRoundRect(
                color = if (isTrapped) Color(0xFFEF4444) else Color(0xFF26304D),
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = CornerRadius(h / 2, h / 2),
                style = Stroke(width = 3.5f)
            )

            // Vertical iron grill bars
            val barSpacing = w / 7f
            for (i in 1..6) {
                val bx = i * barSpacing
                drawLine(
                    color = if (isTrapped) Color(0xFFF87171) else Color(0xFF1E263D),
                    start = Offset(bx, 4f),
                    end = Offset(bx, h - 3f),
                    strokeWidth = 3f
                )
            }
        }
    }
}
