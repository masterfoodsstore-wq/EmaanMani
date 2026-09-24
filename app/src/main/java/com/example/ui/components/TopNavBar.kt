package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TopNavBar(
    balance: Long,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onOpenHowToPlay: () -> Unit,
    onOpenStats: () -> Unit,
    onClaimRefill: () -> Unit,
    onNavigateBackToLobby: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val formattedBalance = NumberFormat.getNumberInstance(Locale.US).format(balance)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo & Back to Lobby
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("app_logo_row")
        ) {
            if (onNavigateBackToLobby != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF232A44))
                        .border(1.dp, Color(0xFF384672), RoundedCornerShape(12.dp))
                        .clickable { onNavigateBackToLobby() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("chicken_dash_back_to_lobby"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Lobby",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LOBBY",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFFF57C00))
                        )
                    )
                    .border(1.5.dp, Color(0xFFFFECB3), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🐔",
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CHICKEN DASH",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        // Right HUD Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "How to play?" button matching reference
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF232A44))
                    .clickable { onOpenHowToPlay() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("how_to_play_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "How to play",
                    tint = Color(0xFF9AA7C7),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "How to play?",
                    color = Color(0xFFC4D0EC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Virtual Balance Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1B2138))
                    .border(1.dp, Color(0xFF34406B), RoundedCornerShape(16.dp))
                    .clickable { onClaimRefill() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("balance_pill"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Virtual Points",
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedBalance,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PTS",
                    color = Color(0xFFFFCA28),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Sound Toggle
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF232A44))
                    .testTag("sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (soundEnabled) "Mute Sound" else "Unmute Sound",
                    tint = if (soundEnabled) Color(0xFF4ADE80) else Color(0xFF9AA7C7),
                    modifier = Modifier.size(18.dp)
                )
            }

            // High Score / Stats
            IconButton(
                onClick = onOpenStats,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF232A44))
                    .testTag("stats_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = "Stats & Records",
                    tint = Color(0xFFFFCA28),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
