package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ChickenDashUiState
import com.example.model.Difficulty
import com.example.model.GameStatus
import com.example.model.LaneStep
import com.example.model.RunSummary
import com.example.util.SoundEffectsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class ChickenDashViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("chicken_dash_prefs", Context.MODE_PRIVATE)
    val soundHelper = SoundEffectsHelper(application)

    private val _uiState = MutableStateFlow(ChickenDashUiState())
    val uiState: StateFlow<ChickenDashUiState> = _uiState.asStateFlow()

    init {
        loadPersistedData()
        generateInitialTrack()
    }

    private fun loadPersistedData() {
        val balance = prefs.getLong("cd_balance", 100_000L)
        val highScore = prefs.getLong("cd_high_score", 0L)
        val bestMult = prefs.getFloat("cd_best_mult", 1.0f)
        val runs = prefs.getInt("cd_total_runs", 0)
        val wins = prefs.getInt("cd_total_wins", 0)
        val sound = prefs.getBoolean("cd_sound", true)

        _uiState.update {
            it.copy(
                balance = balance,
                highScore = highScore,
                bestMultiplier = bestMult,
                totalRuns = runs,
                totalWins = wins,
                soundEnabled = sound
            )
        }
    }

    private fun savePersistedData() {
        val state = _uiState.value
        prefs.edit()
            .putLong("cd_balance", state.balance)
            .putLong("cd_high_score", state.highScore)
            .putFloat("cd_best_mult", state.bestMultiplier)
            .putInt("cd_total_runs", state.totalRuns)
            .putInt("cd_total_wins", state.totalWins)
            .putBoolean("cd_sound", state.soundEnabled)
            .apply()
    }

    private fun generateInitialTrack() {
        val state = _uiState.value
        val trackLanes = buildTrackLanes(state.difficulty, state.stake, state.totalLanes)
        _uiState.update {
            it.copy(
                lanes = trackLanes,
                currentLaneIndex = 0,
                currentRunPoints = 0L,
                gameStatus = GameStatus.IDLE,
                trappedLaneIndex = null,
                lastRunSummary = null,
                feedbackMessage = null
            )
        }
    }

    private fun buildTrackLanes(diff: Difficulty, stake: Int, count: Int): List<LaneStep> {
        val list = mutableListOf<LaneStep>()
        var mult = 1.0f

        val stepInc = when (diff) {
            Difficulty.EASY -> 0.06f
            Difficulty.MEDIUM -> 0.12f
            Difficulty.HARD -> 0.22f
            Difficulty.HARDCORE -> 0.45f
        }

        for (i in 1..count) {
            // Progressive multiplier calculation
            mult += stepInc + (i * 0.012f)
            val roundedMult = (Math.round(mult * 100.0) / 100.0).toFloat()
            val points = (stake * roundedMult).toLong()

            // Pre-seed trap possibilities based on difficulty percentage
            // Lane 1 has a lower trap chance so early game feels fair and engaging
            val trapChance = if (i == 1) diff.collisionChancePercent / 2 else diff.collisionChancePercent
            val isTrap = Random.nextInt(100) < trapChance

            list.add(
                LaneStep(
                    index = i,
                    multiplier = roundedMult,
                    scorePoints = points,
                    isTrap = isTrap,
                    isRevealed = false
                )
            )
        }
        return list
    }

    fun setDifficulty(difficulty: Difficulty) {
        if (_uiState.value.gameStatus != GameStatus.IDLE) return
        soundHelper.playButtonTap(_uiState.value.soundEnabled)
        _uiState.update { it.copy(difficulty = difficulty) }
        generateInitialTrack()
    }

    fun setStake(amount: Int) {
        if (_uiState.value.gameStatus != GameStatus.IDLE) return
        val clamped = amount.coerceIn(1, 10_000)
        soundHelper.playButtonTap(_uiState.value.soundEnabled)
        _uiState.update { it.copy(stake = clamped) }
        generateInitialTrack()
    }

    fun adjustStakeBy(delta: Int) {
        if (_uiState.value.gameStatus != GameStatus.IDLE) return
        val newStake = (_uiState.value.stake + delta).coerceIn(1, 10_000)
        setStake(newStake)
    }

    fun setMinStake() = setStake(1)
    fun setMaxStake() {
        val maxAvailable = (_uiState.value.balance.coerceAtMost(10_000L)).toInt().coerceAtLeast(1)
        setStake(maxAvailable)
    }

    fun onGoOrStep() {
        val state = _uiState.value
        when (state.gameStatus) {
            GameStatus.IDLE -> startRun()
            GameStatus.RUNNING -> advanceNextStep()
            GameStatus.CRASHED, GameStatus.COLLECTED -> {
                // Quick start new round
                resetToIdle()
                startRun()
            }
            GameStatus.STEPPING -> {
                // Ignore while stepping animation is active
            }
        }
    }

    private fun startRun() {
        val state = _uiState.value
        if (state.balance < state.stake) {
            _uiState.update { it.copy(feedbackMessage = "Not enough virtual points! Claim free refill.") }
            return
        }

        val newBalance = state.balance - state.stake
        val trackLanes = buildTrackLanes(state.difficulty, state.stake, state.totalLanes)

        soundHelper.playStepSound(state.soundEnabled)

        _uiState.update {
            it.copy(
                balance = newBalance,
                lanes = trackLanes,
                currentLaneIndex = 0,
                currentRunPoints = 0L,
                gameStatus = GameStatus.RUNNING,
                trappedLaneIndex = null,
                lastRunSummary = null,
                totalRuns = it.totalRuns + 1,
                feedbackMessage = null
            )
        }
        savePersistedData()

        // Automatically make the first hop into Lane 1
        advanceNextStep()
    }

    private fun advanceNextStep() {
        val state = _uiState.value
        if (state.gameStatus != GameStatus.RUNNING && state.currentLaneIndex != 0) return

        val nextLaneIndex = state.currentLaneIndex + 1
        if (nextLaneIndex > state.totalLanes) {
            collectPoints()
            return
        }

        val targetLane = state.lanes.find { it.index == nextLaneIndex } ?: return

        _uiState.update { it.copy(gameStatus = GameStatus.STEPPING) }
        soundHelper.playStepSound(state.soundEnabled)

        viewModelScope.launch {
            // Hop animation duration
            delay(320)

            if (targetLane.isTrap) {
                // Trap hit! Collision occurred
                soundHelper.playCollisionSound(_uiState.value.soundEnabled)
                val summary = RunSummary(
                    won = false,
                    pointsCollected = 0L,
                    stepsCompleted = state.currentLaneIndex,
                    multiplier = 1.0f,
                    difficulty = state.difficulty
                )

                _uiState.update {
                    it.copy(
                        gameStatus = GameStatus.CRASHED,
                        trappedLaneIndex = nextLaneIndex,
                        currentLaneIndex = nextLaneIndex,
                        lastRunSummary = summary,
                        feedbackMessage = "Oops! Obstacle hit on Lane $nextLaneIndex!"
                    )
                }
                savePersistedData()
            } else {
                // Safe step!
                soundHelper.playSuccessMilestoneSound(_uiState.value.soundEnabled)
                val updatedLanes = _uiState.value.lanes.map {
                    if (it.index == nextLaneIndex) it.copy(isRevealed = true) else it
                }

                _uiState.update {
                    it.copy(
                        gameStatus = GameStatus.RUNNING,
                        currentLaneIndex = nextLaneIndex,
                        currentRunPoints = targetLane.scorePoints,
                        lanes = updatedLanes,
                        feedbackMessage = "Safe! +${targetLane.scorePoints} PTS (${targetLane.multiplier}x)"
                    )
                }

                // If completed the final lane, auto collect
                if (nextLaneIndex == state.totalLanes) {
                    delay(300)
                    collectPoints()
                }
            }
        }
    }

    fun collectPoints() {
        val state = _uiState.value
        if (state.currentLaneIndex <= 0 || state.gameStatus != GameStatus.RUNNING) return

        val earned = state.currentRunPoints
        val newBalance = state.balance + earned
        val currentStep = state.lanes.find { it.index == state.currentLaneIndex }
        val mult = currentStep?.multiplier ?: 1.0f

        val newHighScore = maxOf(state.highScore, earned)
        val newBestMult = maxOf(state.bestMultiplier, mult)

        soundHelper.playCollectSound(state.soundEnabled)

        val summary = RunSummary(
            won = true,
            pointsCollected = earned,
            stepsCompleted = state.currentLaneIndex,
            multiplier = mult,
            difficulty = state.difficulty
        )

        _uiState.update {
            it.copy(
                balance = newBalance,
                highScore = newHighScore,
                bestMultiplier = newBestMult,
                totalWins = it.totalWins + 1,
                gameStatus = GameStatus.COLLECTED,
                lastRunSummary = summary,
                feedbackMessage = "RUN FINISHED! Collected +$earned PTS!"
            )
        }
        savePersistedData()
    }

    fun resetToIdle() {
        soundHelper.playButtonTap(_uiState.value.soundEnabled)
        generateInitialTrack()
    }

    fun claimFreeRefill() {
        soundHelper.playCollectSound(_uiState.value.soundEnabled)
        _uiState.update {
            it.copy(
                balance = it.balance + 10_000L,
                feedbackMessage = "+10,000 Free Arcade Points Refilled!"
            )
        }
        savePersistedData()
    }

    fun toggleSound() {
        val newSound = !_uiState.value.soundEnabled
        _uiState.update { it.copy(soundEnabled = newSound) }
        savePersistedData()
    }

    fun setShowHowToPlay(show: Boolean) {
        soundHelper.playButtonTap(_uiState.value.soundEnabled)
        _uiState.update { it.copy(showHowToPlay = show) }
    }

    fun setShowStats(show: Boolean) {
        soundHelper.playButtonTap(_uiState.value.soundEnabled)
        _uiState.update { it.copy(showStats = show) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        soundHelper.release()
    }
}
