package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*

@Composable
fun TransactionHistoryScreen(
    deposits: List<DepositRecord>,
    withdrawals: List<WithdrawalRecord>,
    ledger: List<LedgerTransaction>,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Ledger, 1: Deposits, 2: Withdrawals

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .testTag("history_back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Transaction History",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Audit Ledger & Direct Receipts",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        // Sub-Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E293B))
                .padding(4.dp)
        ) {
            listOf("Ledger (${ledger.size})", "Deposits (${deposits.size})", "Payouts (${withdrawals.size})").forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF6366F1) else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                if (ledger.isEmpty()) {
                    EmptyHistoryPlaceholder("No ledger entries found")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ledger) { tx ->
                            LedgerItemCard(tx)
                        }
                    }
                }
            }
            1 -> {
                if (deposits.isEmpty()) {
                    EmptyHistoryPlaceholder("No deposit requests submitted yet")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(deposits) { dep ->
                            DepositDetailedCard(dep)
                        }
                    }
                }
            }
            2 -> {
                if (withdrawals.isEmpty()) {
                    EmptyHistoryPlaceholder("No withdrawal requests found")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(withdrawals) { w ->
                            WithdrawalDetailedCard(w)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerItemCard(tx: LedgerTransaction) {
    val isPositive = tx.type == TransactionType.DEPOSIT || tx.type == TransactionType.WITHDRAWAL_REFUND
    val amountColor = if (isPositive) Color(0xFF10B981) else Color(0xFFF87171)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tx.type.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (isPositive) "+" else "-"}PKR ${String.format("%,.2f", tx.amount)}",
                    color = amountColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Text(
                text = tx.description,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp
            )

            Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bal: PKR ${String.format("%,.0f", tx.balanceBefore)} -> PKR ${String.format("%,.0f", tx.balanceAfter)}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Text(
                    text = tx.formattedDate,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun DepositDetailedCard(dep: DepositRecord) {
    val statusColor = when (dep.status) {
        DepositStatus.APPROVED -> Color(0xFF10B981)
        DepositStatus.PENDING -> Color(0xFFF59E0B)
        DepositStatus.REJECTED -> Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deposit #${dep.id} • ${dep.paymentMethod}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = dep.status.name, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Amount: PKR ${String.format("%,.2f", dep.amount)}",
                color = Color(0xFF10B981),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Sender: ${dep.senderNumber} | TRX Ref: ${dep.transactionRef}",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp
            )

            if (!dep.adminNote.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F172A))
                        .padding(8.dp)
                ) {
                    Text(text = "Note: ${dep.adminNote}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }

            Text(
                text = "Submitted: ${dep.formattedDate}",
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun WithdrawalDetailedCard(w: WithdrawalRecord) {
    val statusColor = when (w.status) {
        WithdrawalStatus.PAID -> Color(0xFF10B981)
        WithdrawalStatus.PENDING -> Color(0xFFF59E0B)
        WithdrawalStatus.PROCESSING -> Color(0xFF38BDF8)
        WithdrawalStatus.REJECTED, WithdrawalStatus.CANCELLED -> Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cashout #${w.id} • ${w.paymentMethod}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = w.status.name, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Net: PKR ${String.format("%,.2f", w.netAmount)}",
                    color = Color(0xFF38BDF8),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Fee: PKR ${String.format("%,.2f", w.fee)}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Text(
                text = "Beneficiary: ${w.accountName} (${w.accountNumber})",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp
            )

            if (!w.transactionRef.isNullOrBlank()) {
                Text(
                    text = "Bank Payout Ref: ${w.transactionRef}",
                    color = Color(0xFF34D399),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Date: ${w.formattedDate}",
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun EmptyHistoryPlaceholder(msg: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E293B))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = msg, color = Color(0xFF94A3B8), fontSize = 13.sp)
        }
    }
}
