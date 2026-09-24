package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.AppView
import com.example.viewmodel.PaymentViewModel

@Composable
fun WalletRootScreen(
    initialView: AppView = AppView.USER_DASHBOARD,
    onNavigateBackToLobby: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel()
) {
    val uiState by paymentViewModel.uiState.collectAsState()
    val users by paymentViewModel.users.collectAsState()
    val deposits by paymentViewModel.deposits.collectAsState()
    val withdrawals by paymentViewModel.withdrawals.collectAsState()
    val transactions by paymentViewModel.transactions.collectAsState()
    val providers by paymentViewModel.providers.collectAsState()
    val auditLogs by paymentViewModel.auditLogs.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(initialView) {
        paymentViewModel.navigateTo(initialView)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            paymentViewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F172A))
        ) {
            when (uiState.currentView) {
                AppView.AUTH_LOGIN -> {
                    AuthScreen(
                        uiState = uiState,
                        viewModel = paymentViewModel,
                        isRegister = false
                    )
                }
                AppView.AUTH_REGISTER, AppView.AUTH_FORGOT_PASSWORD -> {
                    AuthScreen(
                        uiState = uiState,
                        viewModel = paymentViewModel,
                        isRegister = true
                    )
                }
                AppView.USER_DASHBOARD -> {
                    val user = uiState.currentUser
                    if (user != null) {
                        UserDashboardScreen(
                            user = user,
                            deposits = deposits,
                            withdrawals = withdrawals,
                            onNavigate = { view -> paymentViewModel.navigateTo(view) },
                            onLogout = { paymentViewModel.logout() },
                            onBackToLobby = onNavigateBackToLobby
                        )
                    } else {
                        AuthScreen(
                            uiState = uiState,
                            viewModel = paymentViewModel,
                            isRegister = false
                        )
                    }
                }
                AppView.USER_DEPOSIT -> {
                    DepositScreen(
                        uiState = uiState,
                        viewModel = paymentViewModel,
                        providers = providers,
                        onBack = { paymentViewModel.navigateTo(AppView.USER_DASHBOARD) },
                        onBackToLobby = onNavigateBackToLobby
                    )
                }
                AppView.USER_WITHDRAW -> {
                    val user = uiState.currentUser
                    if (user != null) {
                        WithdrawalScreen(
                            uiState = uiState,
                            viewModel = paymentViewModel,
                            user = user,
                            providers = providers,
                            onBack = { paymentViewModel.navigateTo(AppView.USER_DASHBOARD) },
                            onBackToLobby = onNavigateBackToLobby
                        )
                    } else {
                        paymentViewModel.navigateTo(AppView.AUTH_LOGIN)
                    }
                }
                AppView.USER_TRANSACTIONS -> {
                    val currentUserId = uiState.currentUser?.id
                    val userDeposits = deposits.filter { it.userId == currentUserId }
                    val userWithdrawals = withdrawals.filter { it.userId == currentUserId }
                    val userLedger = transactions.filter { it.userId == currentUserId }

                    TransactionHistoryScreen(
                        deposits = userDeposits,
                        withdrawals = userWithdrawals,
                        ledger = userLedger,
                        onBack = { paymentViewModel.navigateTo(AppView.USER_DASHBOARD) }
                    )
                }
                AppView.USER_PROFILE -> {
                    val user = uiState.currentUser
                    if (user != null) {
                        ProfileScreen(
                            user = user,
                            onBack = { paymentViewModel.navigateTo(AppView.USER_DASHBOARD) },
                            onLogout = { paymentViewModel.logout() }
                        )
                    }
                }
                AppView.ADMIN_DASHBOARD,
                AppView.ADMIN_USERS,
                AppView.ADMIN_DEPOSITS,
                AppView.ADMIN_WITHDRAWALS,
                AppView.ADMIN_PAYMENT_CONFIG,
                AppView.ADMIN_AUDIT_LOGS -> {
                    WalletAdminDashboardScreen(
                        uiState = uiState,
                        viewModel = paymentViewModel,
                        users = users,
                        deposits = deposits,
                        withdrawals = withdrawals,
                        providers = providers,
                        auditLogs = auditLogs,
                        onExitAdmin = { paymentViewModel.toggleAdminMode(false) }
                    )
                }
            }

            // Quick Floating Admin / User View Toggle (to inspect either experience easily)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        paymentViewModel.toggleAdminMode(!uiState.isAdminMode)
                    },
                    containerColor = if (uiState.isAdminMode) Color(0xFF6366F1) else Color(0xFFDC2626),
                    contentColor = Color.White
                ) {
                    Text(
                        text = if (uiState.isAdminMode) "👤 User" else "🛡️ Admin",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
