package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import com.example.model.HistoryRecord
import com.example.model.RoundWinner
import com.example.ui.theme.DragonBlue
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.TieGreen
import com.example.ui.theme.TigerRed

@Composable
fun GameHistoryDialog(
    historyList: List<HistoryRecord>,
    onDismiss: () -> Unit
) {
    val total = historyList.size.coerceAtLeast(1)
    val dragonWins = historyList.count { it.winner == RoundWinner.DRAGON }
    val tigerWins = historyList.count { it.winner == RoundWinner.TIGER }
    val tieWins = historyList.count { it.winner == RoundWinner.TIE }

    val dragonPercent = (dragonWins * 100) / total
    val tigerPercent = (tigerWins * 100) / total
    val tiePercent = (tieWins * 100) / total

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, ImperialGold, RoundedCornerShape(16.dp))
                .testTag("history_dialog_surface"),
            color = Color(0xFF140F18)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = ImperialGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Game Roadmap & Trends",
                            color = ImperialGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("history_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stat Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Dragon", "$dragonWins ($dragonPercent%)", DragonBlue, Modifier.weight(1f))
                    StatCard("Tie", "$tieWins ($tiePercent%)", TieGreen, Modifier.weight(1f))
                    StatCard("Tiger", "$tigerWins ($tigerPercent%)", TigerRed, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "RECENT ROUND RESULTS",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    items(historyList) { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1D1624))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val badgeColor = when (record.winner) {
                                    RoundWinner.DRAGON -> DragonBlue
                                    RoundWinner.TIGER -> TigerRed
                                    RoundWinner.TIE -> TieGreen
                                }
                                val badgeLetter = when (record.winner) {
                                    RoundWinner.DRAGON -> "D"
                                    RoundWinner.TIGER -> "T"
                                    RoundWinner.TIE -> "Tie"
                                }

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = badgeLetter,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Round #${record.roundId}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "D: ${record.dragonCard.rankLabel}${record.dragonCard.suit.symbol}",
                                            color = DragonBlue,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "vs",
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "T: ${record.tigerCard.rankLabel}${record.tigerCard.suit.symbol}",
                                            color = TigerRed,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (record.netWinAmount > 0) {
                                        Text(
                                            text = "+${record.netWinAmount} PTS (-${record.feeDeducted} 3% fee)",
                                            color = ImperialGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 11.sp)
        }
    }
}
