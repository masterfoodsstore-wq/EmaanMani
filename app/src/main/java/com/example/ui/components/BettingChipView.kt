package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChipDenomination
import com.example.ui.theme.ImperialGold
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BettingChipView(
    chip: ChipDenomination,
    isSelected: Boolean,
    onClick: () -> Unit,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-6).dp else 0.dp,
        label = "chip_lift"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .size(size)
            .then(
                if (isSelected) {
                    Modifier.shadow(8.dp, CircleShape, spotColor = ImperialGold)
                } else {
                    Modifier.shadow(2.dp, CircleShape)
                }
            )
            .clip(CircleShape)
            .background(chip.primaryColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            )
            .testTag("betting_chip_${chip.label}")
    ) {
        // Draw casino edge stripes and concentric gold grooves
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f

            // Edge notches
            val notchCount = 8
            for (i in 0 until notchCount) {
                val angle = (i * 2 * Math.PI / notchCount).toFloat()
                val nx = center.x + cos(angle) * (radius - 3f)
                val ny = center.y + sin(angle) * (radius - 3f)
                drawCircle(
                    color = chip.secondaryColor,
                    radius = 3.5f,
                    center = Offset(nx, ny)
                )
            }

            // Inner gold ring
            drawCircle(
                color = ImperialGold.copy(alpha = 0.9f),
                radius = radius * 0.72f,
                style = Stroke(width = 2.5f)
            )

            // Inner dark inlay
            drawCircle(
                color = Color(0x33000000),
                radius = radius * 0.65f
            )
        }

        // Center denomination text
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chip.label,
                color = Color.White,
                fontSize = if (chip.label.length >= 3) 11.sp else 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
        }

        // Selected halo
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(size)
                    .border(2.5.dp, ImperialGold, CircleShape)
            )
        }
    }
}
