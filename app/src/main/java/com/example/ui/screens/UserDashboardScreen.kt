package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DepositRecord
import com.example.model.DepositStatus
import com.example.model.User
import com.example.model.WithdrawalRecord
import com.example.model.WithdrawalStatus
import com.example.viewmodel.AppView
import com.example.viewmodel.PaymentUiState
import com.example.viewmodel.PaymentViewModel

@Composable
fun UserDashboardScreen(
    user: User,
    deposits: List<DepositRecord>,
    withdrawals: List<WithdrawalRecord>,
    onNavigate: (AppView) -> Unit,
    onLogout: () -> Unit,
    onBackToLobby: (() -> Unit)? = null
) {
    val userDeposits = deposits.filter { it.userId == user.id }
    val userWithdrawals = withdrawals.filter { it.userId == user.id }

    val totalDeposited = userDeposits.filter { it.status == DepositStatus.APPROVED }.sumOf { it.amount }
    val totalWithdrawn = userWithdrawals.filter { it.status == WithdrawalStatus.PAID }.sumOf { it.amount }
    val pendingWithdrawals = userWithdrawals.filter { it.status == WithdrawalStatus.PENDING || it.status == WithdrawalStatus.PROCESSING }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top User Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = user.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.mobile,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (user.isWhatsappVerified) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "WhatsApp Verified",
                                    color = Color(0xFF22C55E),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onBackToLobby != null) {
                    Button(
                        onClick = onBackToLobby,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("dashboard_back_to_games_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back to Games", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }

        // Available Balance Card (Gradient)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE BALANCE",
                            color = Color(0xFFC7D2FE),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33FFFFFF))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PKR / Wallet",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "PKR ${String.format("%,.2f", user.availableBalance)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (user.reservedBalance > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reserved for pending withdrawal: PKR ${String.format("%,.2f", user.reservedBalance)}",
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Buttons: Deposit & Withdraw
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(AppView.USER_DEPOSIT) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("deposit_nav_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deposit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = { onNavigate(AppView.USER_WITHDRAW) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("withdraw_nav_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Withdraw", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Quick Navigation Tabs: Transaction History & Profile
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DashboardShortcutCard(
                title = "History",
                subtitle = "Ledger & Receipts",
                icon = Icons.Default.ReceiptLong,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(AppView.USER_TRANSACTIONS) },
                tag = "tx_history_shortcut"
            )
            DashboardShortcutCard(
                title = "Profile",
                subtitle = "Security & WhatsApp",
                icon = Icons.Default.Security,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(AppView.USER_PROFILE) },
                tag = "profile_shortcut"
            )
        }

        // Financial Overview Stats Grid
        Text(
            text = "FINANCIAL SUMMARY",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricBox(
                title = "Total Deposited",
                amount = "PKR ${String.format("%,.0f", totalDeposited)}",
                icon = Icons.Default.ArrowDownward,
                tint = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            StatMetricBox(
                title = "Total Withdrawn",
                amount = "PKR ${String.format("%,.0f", totalWithdrawn)}",
                icon = Icons.Default.ArrowUpward,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricBox(
                title = "Pending Withdrawals",
                amount = "PKR ${String.format("%,.0f", pendingWithdrawals)}",
                icon = Icons.Default.Pending,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.weight(1f)
            )
            StatMetricBox(
                title = "Completed Payouts",
                amount = "${userWithdrawals.count { it.status == WithdrawalStatus.PAID }} Transferred",
                icon = Icons.Default.CheckCircle,
                tint = Color(0xFFA78BFA),
                modifier = Modifier.weight(1f)
            )
        }

        // Recent Activity Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RECENT TRANSACTIONS",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "View All",
                color = Color(0xFF6366F1),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigate(AppView.USER_TRANSACTIONS) }
            )
        }

        if (userDeposits.isEmpty() && userWithdrawals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No deposits or withdrawals yet.\nTap 'Deposit' to fund your account.",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                userDeposits.take(3).forEach { dep ->
                    DepositHistoryItemCard(deposit = dep)
                }
                userWithdrawals.take(2).forEach { w ->
                    WithdrawalHistoryItemCard(withdrawal = w)
                }
            }
        }
    }
}

@Composable
fun DashboardShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(tag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun StatMetricBox(
    title: String,
    amount: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = amount, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DepositHistoryItemCard(deposit: DepositRecord) {
    val statusColor = when (deposit.status) {
        DepositStatus.APPROVED -> Color(0xFF10B981)
        DepositStatus.PENDING -> Color(0xFFF59E0B)
        DepositStatus.REJECTED -> Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Deposit • ${deposit.paymentMethod}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TRX: ${deposit.transactionRef} • ${deposit.formattedDate}",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+PKR ${String.format("%,.2f", deposit.amount)}",
                    color = Color(0xFF10B981),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = deposit.status.name,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WithdrawalHistoryItemCard(withdrawal: WithdrawalRecord) {
    val statusColor = when (withdrawal.status) {
        WithdrawalStatus.PAID -> Color(0xFF10B981)
        WithdrawalStatus.PENDING -> Color(0xFFF59E0B)
        WithdrawalStatus.PROCESSING -> Color(0xFF38BDF8)
        WithdrawalStatus.REJECTED, WithdrawalStatus.CANCELLED -> Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Withdrawal • ${withdrawal.paymentMethod}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${withdrawal.accountNumber} • ${withdrawal.formattedDate}",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-PKR ${String.format("%,.2f", withdrawal.amount)}",
                    color = Color(0xFFF87171),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = withdrawal.status.name,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
