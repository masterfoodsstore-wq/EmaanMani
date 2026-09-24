package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayingCard
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight

@Composable
fun PlayingCardView(
    card: PlayingCard?,
    isRevealed: Boolean,
    isWinning: Boolean,
    glowColor: Color,
    cardBackGradient: List<Color>,
    width: Dp = 68.dp,
    height: Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .then(
                if (isWinning) {
                    Modifier.shadow(16.dp, shape, ambientColor = glowColor, spotColor = glowColor)
                } else {
                    Modifier.shadow(4.dp, shape)
                }
            )
            .clip(shape)
            .border(
                width = if (isWinning) 2.5.dp else 1.5.dp,
                color = if (isWinning) ImperialGoldLight else Color(0x66FFFFFF),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // Card Back (Asian Casino Theme)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(cardBackGradient))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, ImperialGold.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .background(Color(0x22FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎴",
                        fontSize = 24.sp
                    )
                }
            }
        } else {
            // Card Front (Flipped 180 deg, so invert graphicsLayer back)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(Color(0xFFFAFAFA))
                    .padding(5.dp)
            ) {
                if (card != null) {
                    val textColor = if (card.suit.isRed) Color(0xFFD32F2F) else Color(0xFF212121)

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top-left rank and suit
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = card.rankLabel,
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 16.sp
                            )
                            Text(
                                text = card.suit.symbol,
                                color = textColor,
                                fontSize = 14.sp,
                                lineHeight = 14.sp
                            )
                        }

                        // Center big suit watermark
                        Box(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = card.suit.symbol,
                                color = textColor.copy(alpha = 0.85f),
                                fontSize = 28.sp
                            )
                        }

                        // Bottom-right rank and suit (inverted)
                        Column(
                            modifier = Modifier.align(Alignment.End),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = card.suit.symbol,
                                color = textColor,
                                fontSize = 12.sp,
                                lineHeight = 12.sp
                            )
                            Text(
                                text = card.rankLabel,
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
