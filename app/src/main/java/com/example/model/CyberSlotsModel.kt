package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.repository.RapidApiStatus
import com.example.repository.RapidSpinVerification

enum class SlotSymbol(
    val emoji: String,
    val displayName: String,
    val tripleMultiplier: Double,
    val accentColor: Color
) {
    SEVEN("7️⃣", "Cyber 7", 50.0, Color(0xFFFF1744)),
    DIAMOND("💎", "Neon Diamond", 25.0, Color(0xFF00E5FF)),
    CROWN("👑", "Royal Crown", 15.0, Color(0xFFFFD700)),
    BELL("🔔", "Gold Bell", 10.0, Color(0xFFFFB300)),
    STAR("⭐", "Cyber Star", 8.0, Color(0xFFFF9100)),
    CLOVER("🍀", "Lucky Clover", 5.0, Color(0xFF00E676)),
    CHERRY("🍒", "Ruby Cherry", 3.0, Color(0xFFFF5252)),
    BOLT("⚡", "Quantum Bolt", 2.0, Color(0xFF7C4DFF));

    companion object {
        fun fromIndex(index: Int): SlotSymbol {
            val values = values()
            val safeIndex = (index % values.size + values.size) % values.size
            return values[safeIndex]
        }
    }
}

data class SlotRoundHistory(
    val roundId: String,
    val symbols: List<SlotSymbol>,
    val betAmount: Long,
    val winAmount: Long,
    val multiplier: Double,
    val timestamp: Long,
    val isRapidApiVerified: Boolean,
    val latencyMs: Long
)

data class CyberSlotsUiState(
    val reels: List<SlotSymbol> = listOf(SlotSymbol.SEVEN, SlotSymbol.CROWN, SlotSymbol.DIAMOND),
    val isSpinning: Boolean = false,
    val credits: Long = 10000L,
    val selectedBet: Long = 100L,
    val lastWin: Long = 0L,
    val winMultiplier: Double = 0.0,
    val jackpotPool: Long = 250000L,
    val spinHistory: List<SlotRoundHistory> = emptyList(),
    val apiStatus: RapidApiStatus = RapidApiStatus(),
    val lastVerification: RapidSpinVerification? = null,
    val soundEnabled: Boolean = true,
    val autoSpinsRemaining: Int = 0,
    val isPaytableOpen: Boolean = false,
    val isApiDetailsOpen: Boolean = false,
    val statusBanner: String = "Ready to Spin • Powered by RapidAPI"
)
