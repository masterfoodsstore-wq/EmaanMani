package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameStatus
import com.example.ui.components.BottomControls
import com.example.ui.components.ChickenRoadCanvas
import com.example.ui.components.HowToPlayDialog
import com.example.ui.components.RunResultOverlay
import com.example.ui.components.StatsHistoryDialog
import com.example.ui.components.TopNavBar
import kotlinx.coroutines.delay

@Composable
fun ChickenDashGameScreen(
    onNavigateBackToLobby: () -> Unit = {},
    viewModel: ChickenDashViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Auto-clear transient feedback message after 3.5 seconds
    LaunchedEffect(uiState.feedbackMessage) {
        if (uiState.feedbackMessage != null) {
            delay(3500)
            viewModel.clearFeedback()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14192B))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("chicken_dash_main_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TOP BAR: Logo, How to Play, Balance, Sound, Stats, Back to Lobby
            TopNavBar(
                balance = uiState.balance,
                soundEnabled = uiState.soundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onOpenHowToPlay = { viewModel.setShowHowToPlay(true) },
                onOpenStats = { viewModel.setShowStats(true) },
                onClaimRefill = { viewModel.claimFreeRefill() },
                onNavigateBackToLobby = onNavigateBackToLobby
            )

            // ROAD GAMEPLAY AREA: Center road with lanes, chicken, obstacles, progression
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                ChickenRoadCanvas(
                    currentLaneIndex = uiState.currentLaneIndex,
                    lanes = uiState.lanes,
                    gameStatus = uiState.gameStatus,
                    trappedLaneIndex = uiState.trappedLaneIndex,
                    modifier = Modifier.fillMaxSize()
                )

                // Run Result Overlay (when game is Collected or Crashed)
                RunResultOverlay(
                    gameStatus = uiState.gameStatus,
                    summary = uiState.lastRunSummary,
                    onPlayAgain = { viewModel.resetToIdle() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                )

                // Feedback Floating Pill
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.feedbackMessage != null && uiState.gameStatus == GameStatus.RUNNING,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                ) {
                    uiState.feedbackMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xEE1E293B))
                                .border(1.dp, Color(0xFF475569), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = msg,
                                color = Color(0xFFF1F5F9),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // BOTTOM CONTROL PANEL: Stake, Difficulty, Collect Points & GO buttons
            BottomControls(
                stake = uiState.stake,
                onStakeChange = { viewModel.setStake(it) },
                onAdjustStake = { viewModel.adjustStakeBy(it) },
                onMinStake = { viewModel.setMinStake() },
                onMaxStake = { viewModel.setMaxStake() },
                selectedDifficulty = uiState.difficulty,
                onSelectDifficulty = { viewModel.setDifficulty(it) },
                gameStatus = uiState.gameStatus,
                currentRunPoints = uiState.currentRunPoints,
                currentLaneIndex = uiState.currentLaneIndex,
                onGo = { viewModel.onGoOrStep() },
                onCollect = { viewModel.collectPoints() }
            )
        }

        // Modals
        if (uiState.showHowToPlay) {
            HowToPlayDialog(onDismiss = { viewModel.setShowHowToPlay(false) })
        }

        if (uiState.showStats) {
            StatsHistoryDialog(
                highScore = uiState.highScore,
                bestMultiplier = uiState.bestMultiplier,
                totalRuns = uiState.totalRuns,
                totalWins = uiState.totalWins,
                balance = uiState.balance,
                onClaimRefill = { viewModel.claimFreeRefill() },
                onDismiss = { viewModel.setShowStats(false) }
            )
        }
    }
}
