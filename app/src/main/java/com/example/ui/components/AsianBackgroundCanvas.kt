package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.TableTheme

@Composable
fun AsianBackgroundCanvas(
    tableTheme: TableTheme,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lantern_sway")
    val lanternSway by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lantern_sway_anim"
    )

    val mistPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mist_pulse_anim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Base Night/Temple Gradient
        val bgBrush = Brush.verticalGradient(
            colors = listOf(
                tableTheme.primaryBg,
                tableTheme.tableFelt.copy(alpha = 0.85f),
                Color(0xFF040204)
            )
        )
        drawRect(brush = bgBrush, size = size)

        // 2. Distant mountain silhouettes
        val mountainPath = Path().apply {
            moveTo(0f, h * 0.45f)
            cubicTo(w * 0.25f, h * 0.35f, w * 0.45f, h * 0.42f, w * 0.65f, h * 0.32f)
            cubicTo(w * 0.85f, h * 0.38f, w * 0.95f, h * 0.30f, w, h * 0.38f)
            lineTo(w, h * 0.6f)
            lineTo(0f, h * 0.6f)
            close()
        }
        drawPath(
            mountainPath,
            color = tableTheme.tableFelt.copy(alpha = 0.4f),
            style = Fill
        )

        // 3. Pagoda / Palace Silhouettes on left and right
        drawPagodaSilhouette(
            isLeft = true,
            width = w,
            height = h,
            color = Color(0xFF09060A).copy(alpha = 0.85f),
            roofAccent = tableTheme.accentGold.copy(alpha = 0.25f)
        )

        drawPagodaSilhouette(
            isLeft = false,
            width = w,
            height = h,
            color = Color(0xFF09060A).copy(alpha = 0.85f),
            roofAccent = tableTheme.accentGold.copy(alpha = 0.25f)
        )

        // 4. Floating Asian Paper Lanterns
        val lantern1X = w * 0.12f + lanternSway
        val lantern1Y = h * 0.18f
        drawLantern(lantern1X, lantern1Y, sway = lanternSway, glowColor = Color(0xFFFF5722))

        val lantern2X = w * 0.88f - lanternSway
        val lantern2Y = h * 0.18f
        drawLantern(lantern2X, lantern2Y, sway = -lanternSway, glowColor = Color(0xFFFF9800))

        // 5. Ambient Dragon vs Tiger Golden Glow in center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tableTheme.accentGold.copy(alpha = mistPulse * 0.25f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.35f),
                radius = w * 0.45f
            ),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.35f)
        )
    }
}

private fun DrawScope.drawPagodaSilhouette(
    isLeft: Boolean,
    width: Float,
    height: Float,
    color: Color,
    roofAccent: Color
) {
    val baseX = if (isLeft) 0f else width * 0.78f
    val pw = width * 0.22f
    val baseY = height * 0.22f

    // 3 tiered roofs
    for (tier in 0..2) {
        val tierY = baseY + tier * (height * 0.08f)
        val roofW = pw * (1.2f - tier * 0.15f)
        val roofX = if (isLeft) 0f else width - roofW

        val roofPath = Path().apply {
            moveTo(roofX, tierY + 20f)
            // Curving Chinese eave up
            quadraticBezierTo(
                if (isLeft) roofX + roofW * 0.6f else roofX + roofW * 0.4f,
                tierY - 12f,
                if (isLeft) roofX + roofW else roofX,
                tierY + 8f
            )
            lineTo(if (isLeft) roofX + roofW - 10f else roofX + 10f, tierY + 35f)
            lineTo(roofX, tierY + 35f)
            close()
        }
        drawPath(roofPath, color)
        drawPath(roofPath, roofAccent, style = Stroke(width = 1.5f))

        // Columns beneath roof
        val colWidth = 8f
        val colHeight = height * 0.05f
        if (isLeft) {
            drawRect(color, Offset(roofX + 25f, tierY + 35f), Size(colWidth, colHeight))
            drawRect(color, Offset(roofX + roofW - 40f, tierY + 35f), Size(colWidth, colHeight))
        } else {
            drawRect(color, Offset(roofX + 30f, tierY + 35f), Size(colWidth, colHeight))
            drawRect(color, Offset(roofX + roofW - 25f, tierY + 35f), Size(colWidth, colHeight))
        }
    }
}

private fun DrawScope.drawLantern(x: Float, y: Float, sway: Float, glowColor: Color) {
    // Hanging cord
    drawLine(
        color = ImperialGold.copy(alpha = 0.6f),
        start = Offset(x, y - 40f),
        end = Offset(x, y),
        strokeWidth = 2f
    )

    // Lantern Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor.copy(alpha = 0.45f), Color.Transparent),
            center = Offset(x, y + 20f),
            radius = 35f
        ),
        radius = 35f,
        center = Offset(x, y + 20f)
    )

    // Red/Gold Oval Body
    drawRoundRect(
        color = glowColor,
        topLeft = Offset(x - 14f, y),
        size = Size(28f, 38f),
        cornerRadius = CornerRadius(14f, 18f)
    )
    drawRoundRect(
        color = ImperialGold,
        topLeft = Offset(x - 14f, y),
        size = Size(28f, 38f),
        cornerRadius = CornerRadius(14f, 18f),
        style = Stroke(width = 1.5f)
    )

    // Rib lines
    drawLine(
        color = ImperialGold.copy(alpha = 0.7f),
        start = Offset(x, y),
        end = Offset(x, y + 38f),
        strokeWidth = 1f
    )

    // Bottom Tassel
    drawLine(
        color = ImperialGold,
        start = Offset(x, y + 38f),
        end = Offset(x + sway, y + 58f),
        strokeWidth = 2.5f
    )
}
