package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.DragonBlue
import com.example.ui.theme.DragonBlueDark
import com.example.ui.theme.DragonBlueLight
import com.example.ui.theme.DragonGlow
import com.example.ui.theme.ImperialGold

@Composable
fun AnimatedDragonCanvas(
    isWinning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dragon_anim")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_float"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dragon_aura"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerY = h * 0.5f + floatOffset

        // Aura glow behind dragon
        val glowRadius = if (isWinning) w * 0.65f else w * 0.45f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    DragonGlow.copy(alpha = if (isWinning) 0.65f else 0.25f * auraPulse),
                    DragonBlueDark.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = Offset(w * 0.55f, centerY),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(w * 0.55f, centerY)
        )

        // Serpentine Dragon Body (Graceful sinuous curves)
        val bodyPath = Path().apply {
            moveTo(w * 0.1f, centerY + 20f)
            cubicTo(
                w * 0.3f, centerY - 45f,
                w * 0.55f, centerY + 35f,
                w * 0.75f, centerY - 25f
            )
            cubicTo(
                w * 0.85f, centerY - 50f,
                w * 0.95f, centerY - 10f,
                w * 0.85f, centerY + 20f
            )
            cubicTo(
                w * 0.65f, centerY + 50f,
                w * 0.35f, centerY - 20f,
                w * 0.1f, centerY + 20f
            )
            close()
        }

        val bodyBrush = Brush.horizontalGradient(
            colors = listOf(DragonBlueDark, DragonBlue, DragonBlueLight)
        )
        drawPath(bodyPath, brush = bodyBrush)
        drawPath(
            bodyPath,
            color = if (isWinning) ImperialGold else DragonGlow.copy(alpha = 0.8f),
            style = Stroke(width = if (isWinning) 3f else 2f)
        )

        // Dragon Spines along back
        drawDragonSpines(w, centerY)

        // Dragon Head (Majestic snout, horns, fiery whiskers)
        val headX = w * 0.82f
        val headY = centerY - 25f

        val headPath = Path().apply {
            moveTo(headX, headY)
            lineTo(headX + 28f, headY - 12f) // upper snout
            lineTo(headX + 42f, headY + 2f)  // jaw tip
            lineTo(headX + 28f, headY + 14f) // lower jaw
            lineTo(headX - 5f, headY + 12f)
            close()
        }
        drawPath(headPath, brush = bodyBrush)
        drawPath(headPath, color = DragonGlow, style = Stroke(width = 2f))

        // Dragon Antlers/Horns (Golden imperial horns)
        val hornPath = Path().apply {
            moveTo(headX + 10f, headY - 10f)
            cubicTo(headX - 10f, headY - 35f, headX - 18f, headY - 45f, headX - 30f, headY - 40f)
            cubicTo(headX - 16f, headY - 32f, headX - 5f, headY - 20f, headX + 6f, headY - 8f)
            close()
        }
        drawPath(hornPath, color = ImperialGold)

        // Whisker tendrils (Floating)
        val whiskerPath = Path().apply {
            moveTo(headX + 38f, headY + 2f)
            cubicTo(headX + 55f, headY + 15f + floatOffset * 0.5f, headX + 45f, headY + 40f, headX + 25f, headY + 35f)
        }
        drawPath(whiskerPath, color = ImperialGold, style = Stroke(width = 2.5f))

        // Glowing Dragon Eye
        drawCircle(
            color = ImperialGold,
            radius = 4f,
            center = Offset(headX + 22f, headY - 2f)
        )
        drawCircle(
            color = Color.White,
            radius = 1.5f,
            center = Offset(headX + 22f, headY - 2f)
        )

        // Dragon Claws (Imperial 4-toed talons)
        drawClaw(Offset(w * 0.45f, centerY + 25f))
        drawClaw(Offset(w * 0.72f, centerY + 18f))

        // Dragon Wing / Fin (Upper spread)
        val wingPath = Path().apply {
            moveTo(w * 0.35f, centerY - 15f)
            cubicTo(w * 0.28f, centerY - 65f, w * 0.42f, centerY - 80f, w * 0.55f, centerY - 55f)
            cubicTo(w * 0.48f, centerY - 45f, w * 0.42f, centerY - 30f, w * 0.38f, centerY - 10f)
            close()
        }
        drawPath(
            wingPath,
            brush = Brush.verticalGradient(listOf(DragonGlow.copy(alpha = 0.8f), DragonBlueDark))
        )
        drawPath(wingPath, color = DragonBlueLight, style = Stroke(width = 1.5f))
    }
}

private fun DrawScope.drawDragonSpines(w: Float, centerY: Float) {
    val spineCount = 7
    for (i in 0 until spineCount) {
        val sx = w * (0.2f + i * 0.08f)
        val sy = centerY - 12f - (if (i % 2 == 0) 10f else 4f)
        val spinePath = Path().apply {
            moveTo(sx - 6f, sy + 8f)
            lineTo(sx, sy - 10f)
            lineTo(sx + 6f, sy + 8f)
            close()
        }
        drawPath(spinePath, color = ImperialGold)
    }
}

private fun DrawScope.drawClaw(base: Offset) {
    val clawPath = Path().apply {
        moveTo(base.x, base.y)
        lineTo(base.x - 12f, base.y + 16f)
        lineTo(base.x - 8f, base.y + 12f)
        lineTo(base.x, base.y + 18f)
        lineTo(base.x + 6f, base.y + 12f)
        lineTo(base.x + 12f, base.y + 16f)
        lineTo(base.x + 8f, base.y)
        close()
    }
    drawPath(clawPath, color = ImperialGold)
    drawPath(clawPath, color = DragonBlueDark, style = Stroke(width = 1f))
}
