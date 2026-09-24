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
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.TigerGlow
import com.example.ui.theme.TigerOrange
import com.example.ui.theme.TigerRed
import com.example.ui.theme.TigerRedDark

@Composable
fun AnimatedTigerCanvas(
    isWinning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tiger_anim")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiger_breathe"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiger_aura"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerY = h * 0.55f

        // Aura glow behind Tiger
        val glowRadius = if (isWinning) w * 0.65f else w * 0.45f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    TigerGlow.copy(alpha = if (isWinning) 0.65f else 0.25f * auraPulse),
                    TigerRedDark.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = Offset(w * 0.45f, centerY),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = Offset(w * 0.45f, centerY)
        )

        // Tiger Body (Powerful feline stalking silhouette facing left)
        val bodyPath = Path().apply {
            moveTo(w * 0.85f, centerY + 15f) // rump
            cubicTo(
                w * 0.75f, centerY - 25f * breatheScale, // back curve
                w * 0.45f, centerY - 20f * breatheScale, // shoulder arch
                w * 0.25f, centerY - 5f // neck
            )
            lineTo(w * 0.18f, centerY - 15f) // ear top
            lineTo(w * 0.12f, centerY) // snout
            lineTo(w * 0.18f, centerY + 15f) // jaw
            cubicTo(
                w * 0.28f, centerY + 35f, // throat
                w * 0.45f, centerY + 45f * breatheScale, // chest & belly
                w * 0.75f, centerY + 30f // flank
            )
            close()
        }

        val tigerBrush = Brush.horizontalGradient(
            colors = listOf(TigerOrange, TigerRed, TigerRedDark)
        )
        drawPath(bodyPath, brush = tigerBrush)
        drawPath(
            bodyPath,
            color = if (isWinning) ImperialGold else TigerGlow.copy(alpha = 0.8f),
            style = Stroke(width = if (isWinning) 3f else 2f)
        )

        // Tiger White belly/throat accent
        val bellyPath = Path().apply {
            moveTo(w * 0.22f, centerY + 10f)
            cubicTo(w * 0.35f, centerY + 25f, w * 0.55f, centerY + 30f, w * 0.72f, centerY + 20f)
            cubicTo(w * 0.55f, centerY + 38f, w * 0.35f, centerY + 32f, w * 0.22f, centerY + 10f)
            close()
        }
        drawPath(bellyPath, color = Color(0xFFFFF3E0).copy(alpha = 0.85f))

        // Tiger Stripes (Distinctive black/dark red tiger markings)
        drawTigerStripes(w, centerY)

        // Tiger Head Details: Snout, Whiskers, Golden Eye
        drawCircle(
            color = ImperialGold,
            radius = 4.5f,
            center = Offset(w * 0.18f, centerY - 2f)
        )
        drawCircle(
            color = Color.Black,
            radius = 2.5f,
            center = Offset(w * 0.17f, centerY - 2f)
        )

        // Forelegs (Pouncing paws)
        drawPaw(Offset(w * 0.32f, centerY + 28f), isFront = true)
        drawPaw(Offset(w * 0.72f, centerY + 25f), isFront = false)

        // Tiger Tail (Majestic curved S-tail curling upward)
        val tailPath = Path().apply {
            moveTo(w * 0.82f, centerY + 5f)
            cubicTo(w * 0.92f, centerY - 10f, w * 0.98f, centerY - 35f, w * 0.88f, centerY - 45f)
            cubicTo(w * 0.94f, centerY - 38f, w * 0.88f, centerY - 15f, w * 0.80f, centerY + 12f)
            close()
        }
        drawPath(tailPath, brush = tigerBrush)
        drawPath(tailPath, color = TigerRedDark, style = Stroke(width = 1.5f))

        // Tail tip stripes
        drawLine(
            color = Color(0xFF1A0A0A),
            start = Offset(w * 0.89f, centerY - 40f),
            end = Offset(w * 0.93f, centerY - 36f),
            strokeWidth = 3f
        )
    }
}

private fun DrawScope.drawTigerStripes(w: Float, centerY: Float) {
    val stripeColor = Color(0xFF260808).copy(alpha = 0.85f)
    val stripeXs = listOf(0.35f, 0.45f, 0.55f, 0.65f, 0.75f)

    stripeXs.forEachIndexed { i, factor ->
        val sx = w * factor
        val sy = centerY - 18f
        val stripePath = Path().apply {
            moveTo(sx, sy)
            lineTo(sx - 4f, sy + 25f + (i % 2) * 8f)
            lineTo(sx + 2f, sy + 25f + (i % 2) * 8f)
            close()
        }
        drawPath(stripePath, color = stripeColor)
    }
}

private fun DrawScope.drawPaw(base: Offset, isFront: Boolean) {
    val pawPath = Path().apply {
        moveTo(base.x - 8f, base.y)
        lineTo(base.x - 12f, base.y + 22f)
        lineTo(base.x + 8f, base.y + 22f)
        lineTo(base.x + 6f, base.y)
        close()
    }
    drawPath(pawPath, color = TigerRed)
    drawPath(pawPath, color = ImperialGold, style = Stroke(width = 1.5f))
}
