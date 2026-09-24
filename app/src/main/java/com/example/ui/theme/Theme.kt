package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ImperialGold,
    secondary = DragonBlue,
    tertiary = TigerRed,
    background = ObsidianBg,
    surface = CardSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextLight,
    onSurface = TextLight
)

private val ChickenDashColorScheme = darkColorScheme(
    primary = Color(0xFF22C55E),
    secondary = Color(0xFFF59E0B),
    tertiary = Color(0xFF3B82F6),
    background = Color(0xFF14192B),
    surface = Color(0xFF1E253E),
    onPrimary = Color.White,
    onSecondary = Color(0xFF1E1B0E),
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun ChickenDashTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChickenDashColorScheme,
        content = content
    )
}

@Composable
fun DragonVsTigerTheme(
    content: @Composable () -> Unit
) {
    ChickenDashTheme(content = content)
}
