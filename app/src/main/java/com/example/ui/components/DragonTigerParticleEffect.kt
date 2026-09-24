package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.RoundWinner
import com.example.ui.theme.DragonBlueLight
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.ImperialGoldLight
import com.example.ui.theme.TieGreen
import com.example.ui.theme.TigerOrange
import com.example.ui.theme.TigerRed
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val angle: Double,
    val speed: Float,
    val size: Float,
    val color: Color,
    val isCoin: Boolean,
    val isStar: Boolean,
    val spinSpeed: Float
)

@Composable
fun DragonTigerParticleEffect(
    winner: RoundWinner,
    triggerKey: Long,
    modifier: Modifier = Modifier
) {
    val progress = remember(triggerKey) { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
        )
    }

    val particleCount = 70
    val particles = remember(triggerKey) {
        val baseColors = when (winner) {
            RoundWinner.DRAGON -> listOf(DragonBlueLight, ImperialGold, Color.White, Color(0xFF00E5FF))
            RoundWinner.TIGER -> listOf(TigerRed, TigerOrange, ImperialGoldLight, Color(0xFFFFD54F))
            RoundWinner.TIE -> listOf(TieGreen, ImperialGold, Color(0xFFB9F6CA), Color.White)
        }

        List(particleCount) { index ->
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val speed = Random.nextFloat() * 450f + 150f
            val size = Random.nextFloat() * 12f + 6f
            val color = baseColors.random()
            val isCoin = index % 3 == 0
            val isStar = index % 5 == 0 && !isCoin
            val spin = (Random.nextFloat() - 0.5f) * 12f

            Particle(
                initialX = 0.5f,
                initialY = 0.5f,
                angle = angle,
                speed = speed,
                size = size,
                color = color,
                isCoin = isCoin,
                isStar = isStar,
                spinSpeed = spin
            )
        }
    }

    if (progress.value in 0.001f..0.999f) {
        val currentProgress = progress.value
        val alpha = if (currentProgress < 0.6f) 1f else (1f - (currentProgress - 0.6f) / 0.4f).coerceIn(0f, 1f)

        Canvas(modifier = modifier.fillMaxSize()) {
            val centerX = size.width * 0.5f
            val centerY = size.height * 0.5f

            particles.forEach { p ->
                val distance = p.speed * currentProgress
                val gravity = 300f * currentProgress * currentProgress
                val px = centerX + (cos(p.angle) * distance).toFloat()
                val py = centerY + (sin(p.angle) * distance).toFloat() + gravity

                val pColor = p.color.copy(alpha = alpha)

                if (p.isCoin) {
                    drawCircle(
                        color = ImperialGold.copy(alpha = alpha),
                        radius = p.size,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = ImperialGoldLight.copy(alpha = alpha),
                        radius = p.size * 0.7f,
                        center = Offset(px, py),
                        style = Stroke(width = 2f)
                    )
                } else if (p.isStar) {
                    drawStar(
                        center = Offset(px, py),
                        size = p.size * 1.5f,
                        color = pColor,
                        rotation = currentProgress * p.spinSpeed * 100f
                    )
                } else {
                    drawCircle(
                        color = pColor,
                        radius = p.size * (1f - currentProgress * 0.5f),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawStar(center: Offset, size: Float, color: Color, rotation: Float) {
    val path = Path()
    val points = 4
    val outerRadius = size
    val innerRadius = size * 0.4f

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (i * Math.PI / points) + Math.toRadians(rotation.toDouble())
        val x = center.x + (cos(angle) * r).toFloat()
        val y = center.y + (sin(angle) * r).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color, style = Fill)
}
