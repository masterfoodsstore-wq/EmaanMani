package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameStatus
import com.example.model.RunSummary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RunResultOverlay(
    gameStatus: GameStatus,
    summary: RunSummary?,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = (gameStatus == GameStatus.COLLECTED || gameStatus == GameStatus.CRASHED) && summary != null

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        if (summary == null) return@AnimatedVisibility

        val isWin = summary.won
        val formattedPoints = NumberFormat.getNumberInstance(Locale.US).format(summary.pointsCollected)

        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isWin) {
                        Brush.verticalGradient(
                            listOf(Color(0xFF0F3D24), Color(0xFF14532D))
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0xFF3F1414), Color(0xFF5A1C1C))
                        )
                    }
                )
                .border(
                    2.dp,
                    if (isWin) Color(0xFF4ADE80) else Color(0xFFEF4444),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("run_result_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isWin) "🎉" else "💥",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isWin) "RUN COMPLETED!" else "OBSTACLE HIT!",
                            color = if (isWin) Color(0xFF86EFAC) else Color(0xFFFCA5A5),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isWin) {
                                "+$formattedPoints PTS (${summary.multiplier}x) Banked!"
                            } else {
                                "Sewer obstacle caught chicken on Lane ${summary.stepsCompleted + 1}"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWin) Color(0xFF22C55E) else Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (isWin) "NEXT RUN" else "TRY AGAIN",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
