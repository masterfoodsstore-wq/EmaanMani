package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
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
import com.example.ui.theme.ImperialGold

@Composable
fun HelpRulesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, ImperialGold, RoundedCornerShape(16.dp))
                .testTag("help_rules_surface"),
            color = Color(0xFF140F18)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Rules",
                            tint = ImperialGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dragon vs Tiger Rules",
                            color = ImperialGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("help_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                RuleSection(
                    title = "OBJECTIVE",
                    body = "Dragon vs Tiger is an Asian two-card casino game. One card is dealt to the Dragon side and one card to the Tiger side. Players bet on which side will reveal the higher card, or if it will be a Tie."
                )

                RuleSection(
                    title = "CARD RANKINGS",
                    body = "• Ace is the lowest card (Rank 1)\n• King is the highest card (Rank 13)\n• Order: A - 2 - 3 - 4 - 5 - 6 - 7 - 8 - 9 - 10 - J - Q - K\n• Suits have no bearing on the outcome unless ranks are identical."
                )

                RuleSection(
                    title = "PAYOUT RATIOS & 3% TABLE FEE",
                    body = "• DRAGON Bet: Pays 1:1 (Even money, minus 3% win fee)\n• TIGER Bet: Pays 1:1 (Even money, minus 3% win fee)\n• TIE Bet: Pays 1:8 (When Dragon and Tiger tie in rank, minus 3% win fee)\n• 3% Commission Fee: A 3% house fee is automatically deducted from all winning bet profits before crediting your account balance."
                )

                RuleSection(
                    title = "VIRTUAL DEMO PLAY",
                    body = "This game runs exclusively with free virtual entertainment credits. No real money or payments are used."
                )
            }
        }
    }
}

@Composable
private fun RuleSection(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            color = ImperialGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = body,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}
