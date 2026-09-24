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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.AppView
import com.example.viewmodel.PaymentUiState
import com.example.viewmodel.PaymentViewModel

@Composable
fun WalletAdminDashboardScreen(
    uiState: PaymentUiState,
    viewModel: PaymentViewModel,
    users: List<User>,
    deposits: List<DepositRecord>,
    withdrawals: List<WithdrawalRecord>,
    providers: List<PaymentProviderConfig>,
    auditLogs: List<AuditLog>,
    onExitAdmin: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Deposits", "Withdrawals", "Users", "Logs")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Admin Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("Cashier Ledger Admin", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Financial verification & player limits", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }

            Button(
                onClick = onExitAdmin,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Exit Admin", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFF6366F1),
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTab == index) Color(0xFF818CF8) else Color(0xFF94A3B8),
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Body Content by Tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> WalletAdminOverviewTab(users, deposits, withdrawals)
                1 -> WalletAdminDepositsTab(deposits, onApprove = { viewModel.approveDeposit(it) }, onReject = { viewModel.rejectDeposit(it) })
                2 -> WalletAdminWithdrawalsTab(withdrawals, onApprove = { viewModel.markWithdrawalPaid(it) }, onReject = { viewModel.rejectWithdrawal(it) })
                3 -> WalletAdminUsersTab(users, onToggleStatus = { viewModel.toggleUserStatus(it) })
                4 -> WalletAdminLogsTab(auditLogs)
            }
        }
    }
}

@Composable
private fun WalletAdminOverviewTab(
    users: List<User>,
    deposits: List<DepositRecord>,
    withdrawals: List<WithdrawalRecord>
) {
    val pendingDep = deposits.count { it.status == DepositStatus.PENDING }
    val pendingWith = withdrawals.count { it.status == WithdrawalStatus.PENDING }
    val totalDepVol = deposits.filter { it.status == DepositStatus.APPROVED }.sumOf { it.amount }
    val totalWithVol = withdrawals.filter { it.status == WithdrawalStatus.PAID }.sumOf { it.amount }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Registered Players", users.size.toString(), Color(0xFF38BDF8), Modifier.weight(1f))
            MetricCard("Pending Deposits", pendingDep.toString(), Color(0xFFFBBF24), Modifier.weight(1f))
            MetricCard("Pending Payouts", pendingWith.toString(), Color(0xFFF43F5E), Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("Total Deposit Vol", "PKR $totalDepVol", Color(0xFF34D399), Modifier.weight(1f))
            MetricCard("Total Paid Vol", "PKR $totalWithVol", Color(0xFFA78BFA), Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(accent.copy(0.4f), Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = Color(0xFF94A3B8), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun WalletAdminDepositsTab(
    deposits: List<DepositRecord>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(deposits, key = { it.id }) { dep ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PKR ${dep.amount.toInt()} • ${dep.paymentMethod}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Trx: ${dep.transactionRef} • Sender: ${dep.senderNumber}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("Status: ${dep.status.name}", color = if (dep.status == DepositStatus.APPROVED) Color(0xFF34D399) else Color(0xFFFBBF24), fontSize = 11.sp)
                    }

                    if (dep.status == DepositStatus.PENDING) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onApprove(dep.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Approve", color = Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onReject(dep.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Reject", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletAdminWithdrawalsTab(
    withdrawals: List<WithdrawalRecord>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(withdrawals, key = { it.id }) { with ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PKR ${with.amount.toInt()} • ${with.paymentMethod}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Acc: ${with.accountNumber} (${with.accountName})", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("Status: ${with.status.name}", color = if (with.status == WithdrawalStatus.PAID) Color(0xFF34D399) else Color(0xFFF43F5E), fontSize = 11.sp)
                    }

                    if (with.status == WithdrawalStatus.PENDING || with.status == WithdrawalStatus.PROCESSING) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onApprove(with.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Mark Paid", color = Color.White, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onReject(with.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Reject", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletAdminUsersTab(
    users: List<User>,
    onToggleStatus: (Long) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(users, key = { it.id }) { u ->
            val isActive = u.status == AccountStatus.ACTIVE
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(u.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${u.mobile} • Points: ${u.balance.toInt()}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onToggleStatus(u.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isActive) Color(0xFF475569) else Color(0xFF10B981)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(if (isActive) "Suspend" else "Activate", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletAdminLogsTab(auditLogs: List<AuditLog>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(auditLogs, key = { it.id }) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(log.action, color = Color(0xFF818CF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(log.adminEmail, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    Text(log.details, color = Color(0xFFE2E8F0), fontSize = 11.sp)
                }
            }
        }
    }
}
