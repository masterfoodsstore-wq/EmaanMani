package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default Dragon & Tiger Palette
val DragonBlue = Color(0xFF1E88E5)
val DragonBlueLight = Color(0xFF64B5F6)
val DragonBlueDark = Color(0xFF0D47A1)
val DragonGlow = Color(0xFF40C4FF)

val TigerRed = Color(0xFFE53935)
val TigerOrange = Color(0xFFFF6D00)
val TigerRedDark = Color(0xFFB71C1C)
val TigerGlow = Color(0xFFFF5252)

val TieGreen = Color(0xFF00E676)
val TieGreenDark = Color(0xFF1B5E20)
val TieGlow = Color(0xFF69F0AE)

val ImperialGold = Color(0xFFFFD700)
val ImperialGoldDark = Color(0xFFC69214)
val ImperialGoldLight = Color(0xFFFFF176)

val ObsidianBg = Color(0xFF0A0708)
val DarkTableFelt = Color(0xFF130F15)
val CardSurface = Color(0xFF1E1720)
val CardBorder = Color(0xFF3E2D44)

val TextGold = Color(0xFFFFE082)
val TextLight = Color(0xFFEEEEEE)
val TextDim = Color(0xFF9E9E9E)

// Themes
enum class TableTheme(
    val displayName: String,
    val description: String,
    val primaryBg: Color,
    val tableFelt: Color,
    val accentGold: Color,
    val dragonColor: Color,
    val tigerColor: Color,
    val tieColor: Color,
    val cardBackGradient: List<Color>
) {
    IMPERIAL_GOLD(
        displayName = "Imperial Palace",
        description = "Classic ruby crimson felt with royal gold borders",
        primaryBg = Color(0xFF140808),
        tableFelt = Color(0xFF2B0E10),
        accentGold = Color(0xFFFFD54F),
        dragonColor = Color(0xFF1E88E5),
        tigerColor = Color(0xFFE53935),
        tieColor = Color(0xFF00C853),
        cardBackGradient = listOf(Color(0xFF8B0000), Color(0xFF3E0A0A))
    ),
    CELESTIAL_JADE(
        displayName = "Celestial Shrine",
        description = "Mystic sapphire velvet with jade & silver trim",
        primaryBg = Color(0xFF061019),
        tableFelt = Color(0xFF0E2233),
        accentGold = Color(0xFF4DD0E1),
        dragonColor = Color(0xFF00B0FF),
        tigerColor = Color(0xFFFF3D00),
        tieColor = Color(0xFF00E676),
        cardBackGradient = listOf(Color(0xFF0D47A1), Color(0xFF002171))
    ),
    CYBER_DYNASTY(
        displayName = "Neo Dynasty",
        description = "Futuristic obsidian felt with neon cyber glow",
        primaryBg = Color(0xFF0A0812),
        tableFelt = Color(0xFF171326),
        accentGold = Color(0xFFFF007F),
        dragonColor = Color(0xFF00F0FF),
        tigerColor = Color(0xFFFF0055),
        tieColor = Color(0xFF00FF66),
        cardBackGradient = listOf(Color(0xFF4A148C), Color(0xFF1A0033))
    ),
    DRAGON_OBSIDIAN(
        displayName = "Obsidian High-Roller",
        description = "Sleek charcoal felt with radiant molten gold",
        primaryBg = Color(0xFF0D0D0D),
        tableFelt = Color(0xFF1C1A17),
        accentGold = Color(0xFFFFB300),
        dragonColor = Color(0xFF2979FF),
        tigerColor = Color(0xFFFF5722),
        tieColor = Color(0xFF1DE9B6),
        cardBackGradient = listOf(Color(0xFF37474F), Color(0xFF212121))
    )
}
