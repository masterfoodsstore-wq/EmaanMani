package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CyberSlotsUiState
import com.example.model.SlotRoundHistory
import com.example.model.SlotSymbol
import com.example.repository.RapidGameApiService
import com.example.repository.UserAccountRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CyberSlotsViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RapidGameApiService()
    private val userRepository = UserAccountRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(CyberSlotsUiState())
    val uiState: StateFlow<CyberSlotsUiState> = _uiState.asStateFlow()

    private var autoSpinJob: Job? = null

    init {
        // Sync user balance from local persistent repository
        val currentUser = userRepository.currentUser.value
        val initialCredits = currentUser?.gamePoints ?: 15000L
        _uiState.update { it.copy(credits = initialCredits) }

        // Test RapidAPI endpoint connectivity on startup
        refreshApiConnection()
    }

    fun refreshApiConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(statusBanner = "Verifying RapidAPI endpoint connection...") }
            val status = apiService.checkApiHealth()
            _uiState.update {
                it.copy(
                    apiStatus = status,
                    statusBanner = if (status.isConnected) {
                        "Connected to RapidAPI (${status.pingMs}ms) • Key Verified"
                    } else {
                        "RapidAPI: ${status.statusMessage}"
                    }
                )
            }
        }
    }

    fun selectBet(amount: Long) {
        if (_uiState.value.isSpinning) return
        _uiState.update { it.copy(selectedBet = amount) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun setPaytableOpen(open: Boolean) {
        _uiState.update { it.copy(isPaytableOpen = open) }
    }

    fun setApiDetailsOpen(open: Boolean) {
        _uiState.update { it.copy(isApiDetailsOpen = open) }
    }

    fun stopAutoSpin() {
        autoSpinJob?.cancel()
        autoSpinJob = null
        _uiState.update { it.copy(autoSpinsRemaining = 0) }
    }

    fun startAutoSpin(count: Int) {
        if (_uiState.value.isSpinning) return
        _uiState.update { it.copy(autoSpinsRemaining = count) }
        autoSpinJob?.cancel()
        autoSpinJob = viewModelScope.launch {
            while (_uiState.value.autoSpinsRemaining > 0 && _uiState.value.credits >= _uiState.value.selectedBet) {
                spinInternal()
                delay(1200)
                _uiState.update { it.copy(autoSpinsRemaining = (it.autoSpinsRemaining - 1).coerceAtLeast(0)) }
            }
            _uiState.update { it.copy(autoSpinsRemaining = 0) }
        }
    }

    fun spin() {
        if (_uiState.value.isSpinning) return
        if (_uiState.value.credits < _uiState.value.selectedBet) {
            _uiState.update { it.copy(statusBanner = "Insufficient balance! Please select a smaller bet or deposit.") }
            return
        }
        viewModelScope.launch {
            spinInternal()
        }
    }

    private suspend fun spinInternal() {
        val currentBet = _uiState.value.selectedBet
        if (_uiState.value.credits < currentBet) return

        // 1. Deduct bet and start spinning
        val newCredits = _uiState.value.credits - currentBet
        _uiState.update {
            it.copy(
                credits = newCredits,
                isSpinning = true,
                lastWin = 0L,
                winMultiplier = 0.0,
                jackpotPool = it.jackpotPool + (currentBet * 0.05).toLong(),
                statusBanner = "Spinning • Transacting with RapidAPI..."
            )
        }

        // 2. Query RapidAPI for verified spin round & cryptographic seed
        val verification = apiService.executeSpinVerification(currentBet)

        // 3. Visual spinning delay
        delay(900)

        // 4. Map reel indices to symbols
        val r1 = SlotSymbol.fromIndex(verification.reelIndices[0])
        val r2 = SlotSymbol.fromIndex(verification.reelIndices[1])
        val r3 = SlotSymbol.fromIndex(verification.reelIndices[2])
        val finalReels = listOf(r1, r2, r3)

        // 5. Calculate payout
        var multiplier = 0.0
        if (r1 == r2 && r2 == r3) {
            multiplier = r1.tripleMultiplier
        } else if (r1 == r2 || r2 == r3 || r1 == r3) {
            val matching = if (r1 == r2 || r1 == r3) r1 else r2
            multiplier = 1.5
        } else if (finalReels.any { it == SlotSymbol.SEVEN }) {
            multiplier = 1.2
        }

        val winAmount = (currentBet * multiplier).toLong()
        val updatedCredits = newCredits + winAmount

        // 6. Update user repository persistent state
        userRepository.updateBalance(updatedCredits)

        val historyEntry = SlotRoundHistory(
            roundId = verification.roundId,
            symbols = finalReels,
            betAmount = currentBet,
            winAmount = winAmount,
            multiplier = multiplier,
            timestamp = System.currentTimeMillis(),
            isRapidApiVerified = verification.isVerified,
            latencyMs = verification.latencyMs
        )

        _uiState.update {
            it.copy(
                reels = finalReels,
                credits = updatedCredits,
                isSpinning = false,
                lastWin = winAmount,
                winMultiplier = multiplier,
                lastVerification = verification,
                spinHistory = (listOf(historyEntry) + it.spinHistory).take(30),
                statusBanner = if (winAmount > 0) {
                    "🎉 WIN! PKR $winAmount (${multiplier}x) • Verified: ${verification.roundId}"
                } else {
                    "No win this round • ${verification.statusMessage}"
                }
            )
        }
    }
}
