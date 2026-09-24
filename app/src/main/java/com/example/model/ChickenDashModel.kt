package com.example.model

enum class Difficulty(
    val label: String,
    val collisionChancePercent: Int,
    val stepMultiplierFactor: Float,
    val description: String
) {
    EASY("Easy", 10, 1.12f, "10% Trap Risk • Steady Progression"),
    MEDIUM("Medium", 20, 1.25f, "20% Trap Risk • Balanced Run"),
    HARD("Hard", 35, 1.50f, "35% Trap Risk • High Obstacle Density"),
    HARDCORE("Hardcore", 50, 2.00f, "50% Trap Risk • Extreme Arcade Thrill")
}

data class LaneStep(
    val index: Int,
    val multiplier: Float,
    val scorePoints: Long,
    val isTrap: Boolean = false,
    val isRevealed: Boolean = false
)

enum class GameStatus {
    IDLE,
    RUNNING,
    STEPPING,
    CRASHED,
    COLLECTED
}

data class RunSummary(
    val won: Boolean,
    val pointsCollected: Long,
    val stepsCompleted: Int,
    val multiplier: Float,
    val difficulty: Difficulty
)

data class ChickenDashUiState(
    val balance: Long = 100_000L,
    val stake: Int = 10,
    val difficulty: Difficulty = Difficulty.EASY,
    val gameStatus: GameStatus = GameStatus.IDLE,
    val currentLaneIndex: Int = 0,
    val totalLanes: Int = 12,
    val lanes: List<LaneStep> = emptyList(),
    val currentRunPoints: Long = 0L,
    val highScore: Long = 0L,
    val bestMultiplier: Float = 1.0f,
    val totalRuns: Int = 0,
    val totalWins: Int = 0,
    val soundEnabled: Boolean = true,
    val showHowToPlay: Boolean = false,
    val showStats: Boolean = false,
    val lastRunSummary: RunSummary? = null,
    val trappedLaneIndex: Int? = null,
    val feedbackMessage: String? = null
)
