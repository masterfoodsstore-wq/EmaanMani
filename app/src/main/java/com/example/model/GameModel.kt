package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class CardSuit(val symbol: String, val isRed: Boolean) {
    SPADES("♠", false),
    HEARTS("♥", true),
    CLUBS("♣", false),
    DIAMONDS("♦", true)
}

data class PlayingCard(
    val suit: CardSuit,
    val rank: Int // 1 (Ace) to 13 (King)
) {
    val rankLabel: String
        get() = when (rank) {
            1 -> "A"
            11 -> "J"
            12 -> "Q"
            13 -> "K"
            else -> rank.toString()
        }

    val rankName: String
        get() = when (rank) {
            1 -> "Ace"
            11 -> "Jack"
            12 -> "Queen"
            13 -> "King"
            else -> rank.toString()
        }

    companion object {
        fun random(): PlayingCard {
            val suit = CardSuit.values().random()
            val rank = Random.nextInt(1, 14)
            return PlayingCard(suit, rank)
        }
    }
}

enum class BetOption(val title: String, val payoutMultiplier: Double, val ratioLabel: String) {
    DRAGON("Dragon", 2.0, "1:1"), // Returns original bet + 1x win = 2x payout
    TIE("Tie", 9.0, "1:8"),       // Returns original bet + 8x win = 9x payout
    TIGER("Tiger", 2.0, "1:1")
}

enum class RoundWinner {
    DRAGON,
    TIGER,
    TIE
}

enum class GamePhase {
    BETTING_OPEN,
    STOP_BETTING,
    DEALING_CARDS,
    RESULT
}

data class HistoryRecord(
    val roundId: Long,
    val winner: RoundWinner,
    val dragonCard: PlayingCard,
    val tigerCard: PlayingCard,
    val isDragonHighSuit: Boolean = false,
    val netWinAmount: Long = 0L,
    val feeDeducted: Long = 0L
)

data class ChipDenomination(
    val value: Long,
    val label: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

val CHIP_OPTIONS = listOf(
    ChipDenomination(5L, "5", Color(0xFFFBC02D), Color(0xFFFFF9C4)),
    ChipDenomination(10L, "10", Color(0xFF1E88E5), Color(0xFFBBDEFB)),
    ChipDenomination(50L, "50", Color(0xFF00ACC1), Color(0xFFB2EBF2)),
    ChipDenomination(100L, "100", Color(0xFF8E24AA), Color(0xFFE1BEE7)),
    ChipDenomination(500L, "500", Color(0xFFE53935), Color(0xFFFFCDD2)),
    ChipDenomination(1000L, "1K", Color(0xFFFF8F00), Color(0xFFFFE082)),
    ChipDenomination(5000L, "5K", Color(0xFFD81B60), Color(0xFFF8BBD0))
)

data class PlayerBet(
    val dragon: Long = 0L,
    val tie: Long = 0L,
    val tiger: Long = 0L
) {
    val total: Long get() = dragon + tie + tiger
    fun hasBets(): Boolean = total > 0L
}

enum class Screen {
    HOME,
    AUTH,
    LOBBY,
    DRAGON_TIGER,
    ZOO_ROULETTE,
    CYBER_SLOTS,
    CHICKEN_DASH,
    ADMIN_UPDATES,
    ADMIN_LOGIN,
    ADMIN_MONITOR,
    WALLET
}
