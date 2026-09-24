package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Difficulty
import com.example.model.GameStatus

@Composable
fun BottomControls(
    stake: Int,
    onStakeChange: (Int) -> Unit,
    onAdjustStake: (Int) -> Unit,
    onMinStake: () -> Unit,
    onMaxStake: () -> Unit,
    selectedDifficulty: Difficulty,
    onSelectDifficulty: (Difficulty) -> Unit,
    gameStatus: GameStatus,
    currentRunPoints: Long,
    currentLaneIndex: Int,
    onGo: () -> Unit,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIdle = gameStatus == GameStatus.IDLE
    val isRunning = gameStatus == GameStatus.RUNNING
    val canCollect = isRunning && currentLaneIndex > 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF161C2E))
            .border(1.5.dp, Color(0xFF26304E), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("bottom_controls_panel")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT SECTION: Stake / Points Input & Presets matching reference
            StakeControls(
                stake = stake,
                isIdle = isIdle,
                onMinStake = onMinStake,
                onMaxStake = onMaxStake,
                onAdjustStake = onAdjustStake,
                onStakeChange = onStakeChange
            )

            Spacer(modifier = Modifier.width(12.dp))

            // CENTER SECTION: Difficulty Selector matching reference
            DifficultyControls(
                selectedDifficulty = selectedDifficulty,
                isIdle = isIdle,
                onSelectDifficulty = onSelectDifficulty
            )

            Spacer(modifier = Modifier.width(12.dp))

            // RIGHT SECTION: Collect Points & GO Buttons matching reference
            ActionButtons(
                canCollect = canCollect,
                currentRunPoints = currentRunPoints,
                gameStatus = gameStatus,
                onCollect = onCollect,
                onGo = onGo
            )
        }
    }
}

@Composable
private fun StakeControls(
    stake: Int,
    isIdle: Boolean,
    onMinStake: () -> Unit,
    onMaxStake: () -> Unit,
    onAdjustStake: (Int) -> Unit,
    onStakeChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.width(175.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top Row: MIN | [ - STAKE + ] | MAX
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF20273F))
                .border(1.dp, Color(0xFF2D3757), RoundedCornerShape(10.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MIN",
                color = if (isIdle) Color(0xFF94A3B8) else Color(0xFF475569),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = isIdle) { onMinStake() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("stake_min_button")
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = { onAdjustStake(-10) },
                    enabled = isIdle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease Stake",
                        tint = if (isIdle) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(13.dp)
                    )
                }

                Text(
                    text = "$stake",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { onAdjustStake(10) },
                    enabled = isIdle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase Stake",
                        tint = if (isIdle) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Text(
                text = "MAX",
                color = if (isIdle) Color(0xFF94A3B8) else Color(0xFF475569),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = isIdle) { onMaxStake() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("stake_max_button")
            )
        }

        // Bottom Row: Quick preset buttons (10, 20, 50, 100)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(10, 20, 50, 100).forEach { preset ->
                val isSelected = stake == preset
                Box(
                    modifier = Modifier
                        .size(width = 38.dp, height = 24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF384672) else Color(0xFF20273F))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF60A5FA) else Color(0xFF2B3554),
                            CircleShape
                        )
                        .clickable(enabled = isIdle) { onStakeChange(preset) }
                        .testTag("stake_preset_$preset"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$preset",
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyControls(
    selectedDifficulty: Difficulty,
    isIdle: Boolean,
    onSelectDifficulty: (Difficulty) -> Unit
) {
    Column(
        modifier = Modifier.width(280.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Info label: Difficulty | Chance of collision
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Difficulty",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Collision Chance: ${selectedDifficulty.collisionChancePercent}%",
                color = when (selectedDifficulty) {
                    Difficulty.EASY -> Color(0xFF4ADE80)
                    Difficulty.MEDIUM -> Color(0xFFFBBF24)
                    Difficulty.HARD -> Color(0xFFF97316)
                    Difficulty.HARDCORE -> Color(0xFFEF4444)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Difficulty Tabs matching reference image
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF20273F))
                .border(1.dp, Color(0xFF2D3757), RoundedCornerShape(10.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Difficulty.entries.forEach { diff ->
                val isSelected = selectedDifficulty == diff
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) Color(0xFF384672) else Color.Transparent
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF60A5FA) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = isIdle) { onSelectDifficulty(diff) }
                        .testTag("diff_tab_${diff.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = diff.label,
                        color = if (isSelected) Color.White else Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    canCollect: Boolean,
    currentRunPoints: Long,
    gameStatus: GameStatus,
    onCollect: () -> Unit,
    onGo: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Yellow Button: COLLECT POINTS / FINISH RUN matching reference "CASH OUT"
        Button(
            onClick = onCollect,
            enabled = canCollect,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF59E0B),
                disabledContainerColor = Color(0xFF373022)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(135.dp)
                .height(48.dp)
                .testTag("collect_points_button")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "COLLECT",
                    color = if (canCollect) Color(0xFF1E1B0E) else Color(0xFF78716C),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (canCollect) "$currentRunPoints PTS" else "FINISH RUN",
                    color = if (canCollect) Color(0xFF1E1B0E) else Color(0xFF78716C),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Green Button: GO / STEP FORWARD matching reference "GO"
        val goLabel = when (gameStatus) {
            GameStatus.IDLE -> "GO"
            GameStatus.RUNNING -> "STEP"
            GameStatus.STEPPING -> "..."
            GameStatus.CRASHED -> "RETRY"
            GameStatus.COLLECTED -> "PLAY"
        }

        Button(
            onClick = onGo,
            enabled = gameStatus != GameStatus.STEPPING,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22C55E),
                disabledContainerColor = Color(0xFF14532D)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(105.dp)
                .height(48.dp)
                .testTag("go_step_button")
        ) {
            Text(
                text = goLabel,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}
