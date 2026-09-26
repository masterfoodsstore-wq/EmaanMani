package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Screen
import com.example.ui.GamingViewModel
import com.example.viewmodel.AppView
import com.example.ui.components.AppHeader
import com.example.ui.components.GameHistoryDialog
import com.example.ui.components.HelpRulesDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.games.DragonTigerGame
import com.example.ui.games.ZooRouletteGame
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AdminUpdateControlScreen
import com.example.ui.screens.GameHomeScreen
import com.example.ui.screens.GameUpdateDialog
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.OfflineGameBarrierScreen
import com.example.ui.screens.UserAuthScreen
import com.example.ui.screens.WalletRootScreen
import com.example.ui.ChickenDashGameScreen
import android.content.pm.ActivityInfo
import com.example.ui.screens.CyberSlotsGameScreen
import com.example.ui.screens.FiversCanGameScreen
import com.example.ui.theme.DragonVsTigerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            DragonVsTigerTheme {
                DragonVsTigerApp()
            }
        }
    }
}

@Composable
fun DragonVsTigerApp(
    viewModel: GamingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val showTopBar = uiState.currentScreen == Screen.DRAGON_TIGER ||
            uiState.currentScreen == Screen.LOBBY

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (showTopBar) {
                AppHeader(
                    currentScreen = uiState.currentScreen,
                    tableTheme = uiState.tableTheme,
                    credits = uiState.credits,
                    soundEnabled = uiState.soundEnabled,
                    onToggleSound = { viewModel.toggleSound(!uiState.soundEnabled) },
                    onOpenSettings = { viewModel.openSettings(true) },
                    onOpenRules = { viewModel.openRules(true) },
                    onNavigateBack = { viewModel.navigateToScreen(Screen.HOME) },
                    onNavigateToWallet = { viewModel.navigateToWallet(AppView.USER_DASHBOARD) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            when (uiState.currentScreen) {
                Screen.AUTH -> {
                    UserAuthScreen(
                        onLogin = { user, pass, rem -> viewModel.login(user, pass, rem) },
                        onRegister = { user, email, pass, av -> viewModel.register(user, email, pass, av) },
                        onForgotPassword = { email, newPw -> viewModel.forgotPassword(email, newPw) },
                        onQuickDemoLogin = { isAdmin -> viewModel.quickDemoLogin(isAdmin) },
                        onNavigateBack = if (uiState.currentUser != null) { { viewModel.navigateToScreen(Screen.HOME) } } else null,
                        errorMessage = uiState.authError
                    )
                }
                Screen.HOME -> {
                    GameHomeScreen(
                        user = uiState.currentUser,
                        credits = uiState.credits,
                        currentVersion = uiState.currentVersion,
                        updateCheckResult = uiState.updateCheckResult,
                        isCheckingUpdates = uiState.isCheckingUpdates,
                        onNavigateToScreen = { screen -> viewModel.navigateToScreen(screen) },
                        onNavigateToDeposit = { viewModel.navigateToWallet(AppView.USER_DEPOSIT) },
                        onNavigateToWithdraw = { viewModel.navigateToWallet(AppView.USER_WITHDRAW) },
                        onNavigateToWallet = { viewModel.navigateToWallet(AppView.USER_DASHBOARD) },
                        onNavigateToAuth = { viewModel.navigateToAuth() },
                        onCheckForUpdates = { viewModel.checkForUpdates() },
                        onOpenUpdateDialog = { release -> viewModel.openUpdateDialog(true, release) },
                        onOpenSettings = { viewModel.openSettings(true) },
                        onOpenRules = { viewModel.openRules(true) },
                        onLogout = { viewModel.logout() }
                    )
                }
                Screen.ADMIN_LOGIN -> {
                    AdminLoginScreen(
                        onAuthenticate = { id, pass, rem -> viewModel.authenticateAdmin(id, pass, rem) },
                        onNavigateBack = { viewModel.navigateToScreen(Screen.HOME) },
                        errorMessage = uiState.adminAuthError,
                        lockoutSeconds = uiState.adminLockoutSeconds,
                        isLoading = uiState.isAdminAuthLoading
                    )
                }
                Screen.ADMIN_MONITOR -> {
                    AdminDashboardScreen(
                        session = uiState.adminSession,
                        telemetry = uiState.liveTelemetry,
                        auditLogs = uiState.adminAuditLogs,
                        releases = uiState.releasesCatalog,
                        currentVersion = uiState.currentVersion,
                        onLogout = { viewModel.logoutAdmin() },
                        onExtendSession = { viewModel.extendAdminSession() },
                        onNavigateBack = { viewModel.navigateToScreen(Screen.HOME) },
                        onNavigateToLogin = { viewModel.navigateToScreen(Screen.ADMIN_LOGIN) },
                        onSimulateDisconnect = { viewModel.simulateDisconnectStream() },
                        onReconnectStream = { viewModel.reconnectStream() },
                        onTogglePauseStream = { viewModel.togglePauseStream() },
                        onAdvanceRound = { viewModel.advanceSimulatedRound() },
                        onInjectSimulatedPlays = { a, b -> viewModel.injectSimulatedPlays(a, b) },
                        onTogglePublishRelease = { ver, pub -> viewModel.adminTogglePublish(ver, pub) },
                        onSetInstalledVersion = { ver -> viewModel.adminSetInstalledVersion(ver) }
                    )
                }
                Screen.ADMIN_UPDATES -> {
                    AdminUpdateControlScreen(
                        user = uiState.currentUser,
                        currentVersion = uiState.currentVersion,
                        releases = uiState.releasesCatalog,
                        onTogglePublish = { ver, pub -> viewModel.adminTogglePublish(ver, pub) },
                        onSetInstalledVersion = { ver -> viewModel.adminSetInstalledVersion(ver) },
                        onNavigateBack = { viewModel.navigateToScreen(Screen.HOME) }
                    )
                }
                Screen.LOBBY -> {
                    LobbyScreen(
                        tableTheme = uiState.tableTheme,
                        credits = uiState.credits,
                        onNavigateToGame = { screen -> viewModel.navigateToScreen(screen) }
                    )
                }
                Screen.DRAGON_TIGER -> {
                    DragonTigerGame(
                        tableTheme = uiState.tableTheme,
                        credits = uiState.credits,
                        gamePhase = uiState.gamePhase,
                        countdownSeconds = uiState.countdownConfigSeconds,
                        countdownRemaining = uiState.countdownRemaining,
                        dragonCard = uiState.dragonCard,
                        tigerCard = uiState.tigerCard,
                        isCardsRevealed = uiState.isCardsRevealed,
                        roundWinner = uiState.roundWinner,
                        playerBet = uiState.currentBet,
                        selectedChip = uiState.selectedChip,
                        historyList = uiState.historyList,
                        lastWinAmount = uiState.lastWinAmount,
                        lastFeeDeducted = uiState.lastFeeDeducted,
                        showWinParticleKey = uiState.showWinParticleKey,
                        onSelectChip = { chip -> viewModel.selectChip(chip) },
                        onPlaceBet = { option -> viewModel.placeBet(option) },
                        onClearBets = { viewModel.clearBets() },
                        onDoubleBets = { viewModel.doubleBets() },
                        onRebet = { viewModel.rebetPrevious() },
                        onOpenHistory = { viewModel.openHistory(true) },
                        onOpenSettings = { viewModel.openSettings(true) },
                        onNavigateToLobby = { viewModel.navigateToScreen(Screen.HOME) }
                    )
                }
                Screen.CYBER_SLOTS -> {
                    FiversCanGameScreen(
                        onNavigateBack = { viewModel.navigateToScreen(Screen.HOME) }
                    )
                }
                Screen.ZOO_ROULETTE -> {
                    ZooRouletteGame(
                        credits = uiState.credits,
                        phase = uiState.zooPhase,
                        countdown = uiState.zooCountdown,
                        activeSlotIndex = uiState.zooActiveSlotIndex,
                        winningAnimal = uiState.zooWinningAnimal,
                        bets = uiState.zooBets,
                        selectedChip = uiState.selectedChip,
                        historyList = uiState.zooHistory,
                        lastWin = uiState.zooLastWin,
                        lastFee = uiState.zooLastFee,
                        winParticleKey = uiState.zooWinParticleKey,
                        onSelectChip = { chip -> viewModel.selectChip(chip) },
                        onPlaceBet = { target -> viewModel.placeZooBet(target) },
                        onClearBets = { viewModel.clearZooBets() },
                        onDoubleBets = { viewModel.doubleZooBets() },
                        onRebet = { viewModel.rebetZooPrevious() },
                        onNavigateBack = { viewModel.navigateToScreen(Screen.HOME) },
                        onOpenSettings = { viewModel.openSettings(true) }
                    )
                }
                Screen.WALLET -> {
                    WalletRootScreen(
                        initialView = uiState.walletInitialView,
                        onNavigateBackToLobby = { viewModel.navigateToScreen(Screen.HOME) }
                    )
                }
                Screen.CHICKEN_DASH -> {
                    ChickenDashGameScreen(
                        onNavigateBackToLobby = { viewModel.navigateToScreen(Screen.HOME) }
                    )
                }
            }

            // 3D Game-style Update Dialog
            if (uiState.isUpdateDialogOpen && uiState.selectedReleaseForUpdate != null) {
                GameUpdateDialog(
                    currentVersion = uiState.currentVersion,
                    latestRelease = uiState.selectedReleaseForUpdate!!,
                    progressState = uiState.updateProgress,
                    onStartUpdate = { release -> viewModel.startDownloadAndApplyUpdate(release) },
                    onRestartToNewVersion = { viewModel.restartGameToNewVersion() },
                    onDismiss = { viewModel.openUpdateDialog(false) }
                )
            }

            // User Settings Dialog (Theme toggle, countdown timer, sound, fast deal, updates)
            if (uiState.isSettingsOpen) {
                SettingsDialog(
                    currentTheme = uiState.tableTheme,
                    onSelectTheme = { theme -> viewModel.selectTheme(theme) },
                    countdownSeconds = uiState.countdownConfigSeconds,
                    onSelectCountdown = { sec -> viewModel.setCountdownDuration(sec) },
                    soundEnabled = uiState.soundEnabled,
                    onToggleSound = { enabled -> viewModel.toggleSound(enabled) },
                    fastDealMode = uiState.fastDealMode,
                    onToggleFastDeal = { fast -> viewModel.toggleFastDeal(fast) },
                    onReloadDemoCredits = { viewModel.reloadDemoCredits() },
                    currentVersion = uiState.currentVersion,
                    updateResult = uiState.updateCheckResult,
                    isCheckingUpdate = uiState.isCheckingConnection,
                    onCheckForUpdates = { viewModel.checkForUpdates(forceUserCheck = true) },
                    onTriggerUpdate = { release ->
                        viewModel.openSettings(false)
                        viewModel.openUpdateDialog(true, release)
                    },
                    onDismiss = { viewModel.openSettings(false) }
                )
            }

            // Game History & Statistics Roadmap Dialog
            if (uiState.isHistoryOpen) {
                GameHistoryDialog(
                    historyList = uiState.historyList,
                    onDismiss = { viewModel.openHistory(false) }
                )
            }

            // Help & Rules Dialog
            if (uiState.isRulesOpen) {
                HelpRulesDialog(
                    onDismiss = { viewModel.openRules(false) }
                )
            }

            // CRITICAL: Offline Mode Barrier - Game stops functioning when offline
            if (!uiState.isOnline) {
                OfflineGameBarrierScreen(
                    isCheckingConnection = uiState.isCheckingConnection,
                    simulatedOffline = uiState.simulatedOffline,
                    onRetryConnection = { viewModel.retryConnection() },
                    onToggleSimulatedOffline = { sim -> viewModel.toggleSimulatedOffline(sim) }
                )
            }
        }
    }
}
