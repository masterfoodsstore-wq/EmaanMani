package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import com.example.model.AuthResult
import com.example.model.BetOption
import com.example.model.CHIP_OPTIONS
import com.example.model.ChipDenomination
import com.example.model.GamePhase
import com.example.model.GameRelease
import com.example.model.HistoryRecord
import com.example.model.PlayerBet
import com.example.model.PlayingCard
import com.example.model.RoundWinner
import com.example.model.Screen
import com.example.model.UpdateCheckResult
import com.example.model.UpdateProgressState
import com.example.model.UserAccount
import com.example.model.ZooAnimal
import com.example.model.ZooBetTarget
import com.example.model.ZooGamePhase
import com.example.model.ZooHistoryItem
import com.example.model.ZooPlayerBets
import com.example.model.ZOO_TRACK_SLOTS
import com.example.model.LiveGameConfig
import com.example.model.AdminAuditLog
import com.example.model.AdminAuthResult
import com.example.model.AdminSession
import com.example.model.LiveGameTelemetry
import com.example.repository.AdminSecurityService
import com.example.repository.GameUpdateManager
import com.example.repository.LiveGameConfigManager
import com.example.repository.NetworkConnectivityObserver
import com.example.repository.PaymentRepository
import com.example.repository.RealtimeGameMonitorService
import com.example.repository.UserAccountRepository
import com.example.ui.theme.TableTheme
import com.example.viewmodel.AppView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GamingUiState(
    val currentScreen: Screen = Screen.AUTH,
    val walletInitialView: AppView = AppView.USER_DASHBOARD,
    val isOnline: Boolean = true,
    val isCheckingConnection: Boolean = false,
    val simulatedOffline: Boolean = false,

    val currentUser: UserAccount? = null,
    val authError: String? = null,
    val currentVersion: String = "v1.0.0",
    val updateCheckResult: UpdateCheckResult? = null,
    val updateProgress: UpdateProgressState = UpdateProgressState.Idle,
    val isCheckingUpdates: Boolean = false,
    val isUpdateDialogOpen: Boolean = false,
    val selectedReleaseForUpdate: GameRelease? = null,
    val releasesCatalog: List<GameRelease> = emptyList(),

    val tableTheme: TableTheme = TableTheme.IMPERIAL_GOLD,
    val liveConfig: LiveGameConfig = LiveGameConfig(),
    val credits: Long = 20L,
    val soundEnabled: Boolean = true,
    val fastDealMode: Boolean = false,
    val countdownConfigSeconds: Int = 15,
    val countdownRemaining: Int = 15,
    val gamePhase: GamePhase = GamePhase.BETTING_OPEN,
    val selectedChip: ChipDenomination = CHIP_OPTIONS[0], // 5 RS chip
    val currentBet: PlayerBet = PlayerBet(),
    val previousBet: PlayerBet = PlayerBet(),
    val dragonCard: PlayingCard? = null,
    val tigerCard: PlayingCard? = null,
    val isCardsRevealed: Boolean = false,
    val roundWinner: RoundWinner? = null,
    val roundNumber: Long = 1L,
    val historyList: List<HistoryRecord> = emptyList(),
    val lastWinAmount: Long = 0L,
    val lastGrossWinAmount: Long = 0L,
    val lastFeeDeducted: Long = 0L,
    val totalFeesCollected: Long = 0L,
    val showWinParticleKey: Long = 0L,
    val isSettingsOpen: Boolean = false,
    val isHistoryOpen: Boolean = false,
    val isRulesOpen: Boolean = false,

    // Zoo Roulette State
    val zooPhase: ZooGamePhase = ZooGamePhase.BETTING,
    val zooCountdown: Int = 18,
    val zooActiveSlotIndex: Int = 0,
    val zooWinningSlotIndex: Int = 0,
    val zooWinningAnimal: ZooAnimal? = null,
    val zooBets: ZooPlayerBets = ZooPlayerBets(),
    val zooPreviousBets: ZooPlayerBets = ZooPlayerBets(),
    val zooHistory: List<ZooHistoryItem> = emptyList(),
    val zooLastWin: Long = 0L,
    val zooLastGrossWin: Long = 0L,
    val zooLastFee: Long = 0L,
    val zooWinParticleKey: Long = 0L,

    // Admin Security & Live Monitor State
    val adminSession: AdminSession? = null,
    val adminAuthError: String? = null,
    val adminLockoutSeconds: Long = 0L,
    val isAdminAuthLoading: Boolean = false,
    val liveTelemetry: LiveGameTelemetry = LiveGameTelemetry(),
    val adminAuditLogs: List<AdminAuditLog> = emptyList()
)

class GamingViewModel(application: Application) : AndroidViewModel(application) {

    private val paymentRepo = PaymentRepository.shared
    val userAccountRepo = UserAccountRepository(application.applicationContext)
    val updateManager = GameUpdateManager(application.applicationContext)
    val networkObserver = NetworkConnectivityObserver(application.applicationContext)
    val adminSecurityService = AdminSecurityService(application.applicationContext)
    val realtimeGameMonitorService = RealtimeGameMonitorService()

    private val _uiState = MutableStateFlow(GamingUiState())
    val uiState: StateFlow<GamingUiState> = _uiState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var zooGameLoopJob: Job? = null

    init {
        // Start real-time Firestore synchronization for live remote game config & broadcast ticker on IO thread
        viewModelScope.launch(Dispatchers.IO) {
            LiveGameConfigManager.startRealtimeSync()
        }

        // Collect server-side admin session state
        viewModelScope.launch {
            adminSecurityService.currentSession.collect { sess ->
                _uiState.update { it.copy(adminSession = sess) }
            }
        }

        // Collect server-side admin audit logs
        viewModelScope.launch {
            adminSecurityService.auditLogs.collect { logs ->
                _uiState.update { it.copy(adminAuditLogs = logs) }
            }
        }

        // Collect live game telemetry from real-time stream
        viewModelScope.launch {
            realtimeGameMonitorService.telemetry.collect { tele ->
                _uiState.update { it.copy(liveTelemetry = tele) }
            }
        }

        // Real-time network connectivity monitoring
        viewModelScope.launch {
            networkObserver.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }

        viewModelScope.launch {
            networkObserver.simulatedOffline.collect { sim ->
                _uiState.update { it.copy(simulatedOffline = sim) }
            }
        }

        // Collect current user state & synchronize points
        viewModelScope.launch {
            userAccountRepo.currentUser.collect { user ->
                _uiState.update { s ->
                    s.copy(
                        currentUser = user,
                        credits = user?.gamePoints ?: s.credits,
                        currentScreen = if (user == null) Screen.AUTH else if (s.currentScreen == Screen.AUTH) Screen.HOME else s.currentScreen
                    )
                }
            }
        }

        // Collect current installed version
        viewModelScope.launch {
            updateManager.currentVersion.collect { ver ->
                _uiState.update { it.copy(currentVersion = ver) }
            }
        }

        // Collect update check results
        viewModelScope.launch {
            updateManager.updateCheckResult.collect { res ->
                _uiState.update { it.copy(updateCheckResult = res) }
            }
        }

        // Collect download & verification progress
        viewModelScope.launch {
            updateManager.updateProgress.collect { prog ->
                _uiState.update { it.copy(updateProgress = prog) }
            }
        }

        // Collect releases catalog for admin and release notes
        viewModelScope.launch {
            updateManager.releases.collect { rels ->
                _uiState.update { it.copy(releasesCatalog = rels) }
            }
        }

        // Auto check for updates on startup
        checkForUpdates()

        // Seed initial history roadmap
        seedInitialHistory()
        seedInitialZooHistory()

        // Synchronize Remote Live Game Configuration in real-time
        viewModelScope.launch {
            LiveGameConfigManager.config.collect { cfg ->
                _uiState.update { s ->
                    s.copy(
                        liveConfig = cfg,
                        countdownConfigSeconds = cfg.dragonCountdownSeconds
                    )
                }
            }
        }

        // Start game loops
        startGameLoop()
        startZooGameLoop()
    }

    private fun seedInitialZooHistory() {
        val initialList = mutableListOf<ZooHistoryItem>()
        val sampleSlots = listOf(15, 7, 5, 18, 22, 16, 8, 4, 19, 21)
        for ((idx, slotIdx) in sampleSlots.withIndex()) {
            val animal = ZOO_TRACK_SLOTS[slotIdx % ZOO_TRACK_SLOTS.size].animal
            initialList.add(
                ZooHistoryItem(
                    id = (idx + 1).toLong(),
                    winningSlot = slotIdx,
                    winningAnimal = animal
                )
            )
        }
        _uiState.update { it.copy(zooHistory = initialList) }
    }

    fun startZooGameLoop() {
        zooGameLoopJob?.cancel()
        zooGameLoopJob = viewModelScope.launch {
            while (true) {
                // Online connectivity gate: pause game loop when offline
                while (!_uiState.value.isOnline) {
                    delay(1000)
                }

                // 1. Betting Open Phase (Synced with Remote Admin Live Config)
                val zooDuration = _uiState.value.liveConfig.zooCountdownSeconds
                _uiState.update {
                    it.copy(
                        zooPhase = ZooGamePhase.BETTING,
                        zooCountdown = zooDuration,
                        zooWinningAnimal = null,
                        zooLastWin = 0L,
                        zooLastGrossWin = 0L,
                        zooLastFee = 0L
                    )
                }

                for (sec in zooDuration downTo 1) {
                    while (!_uiState.value.isOnline) {
                        delay(1000)
                    }
                    _uiState.update { it.copy(zooCountdown = sec) }
                    delay(1000)
                }

                // 2. Spinning Phase
                val targetSlotIndex = (0 until ZOO_TRACK_SLOTS.size).random()
                val winningAnimal = ZOO_TRACK_SLOTS[targetSlotIndex].animal

                _uiState.update {
                    it.copy(
                        zooPhase = ZooGamePhase.SPINNING,
                        zooWinningSlotIndex = targetSlotIndex,
                        zooWinningAnimal = winningAnimal
                    )
                }

                // Smooth deceleration around the 24 perimeter slots
                val currentSlot = _uiState.value.zooActiveSlotIndex
                val totalSteps = (ZOO_TRACK_SLOTS.size * 2) + ((targetSlotIndex - currentSlot + ZOO_TRACK_SLOTS.size) % ZOO_TRACK_SLOTS.size)
                var currentDelay = 45L

                for (step in 0 until totalSteps) {
                    _uiState.update {
                        it.copy(zooActiveSlotIndex = (it.zooActiveSlotIndex + 1) % ZOO_TRACK_SLOTS.size)
                    }
                    val remainingSteps = totalSteps - step
                    if (remainingSteps < 12) {
                        currentDelay += (18L + (12 - remainingSteps) * 8L)
                    }
                    delay(currentDelay)
                }

                // Final lock on target slot
                _uiState.update { it.copy(zooActiveSlotIndex = targetSlotIndex) }
                delay(600)

                // 3. Result & Payout Calculation (with 3% fee cut)
                val bets = _uiState.value.zooBets
                var grossWin = 0L

                when (winningAnimal) {
                    ZooAnimal.MONKEY -> {
                        grossWin += bets.monkey * 8 + (bets.beastGeneral * 2)
                    }
                    ZooAnimal.RABBIT -> {
                        grossWin += bets.rabbit * 6 + (bets.beastGeneral * 2)
                    }
                    ZooAnimal.LION -> {
                        grossWin += bets.lion * 12 + (bets.beastGeneral * 2)
                    }
                    ZooAnimal.PANDA -> {
                        grossWin += bets.panda * 8 + (bets.beastGeneral * 2)
                    }
                    ZooAnimal.SWALLOW -> {
                        grossWin += bets.swallow * 8 + (bets.birdGeneral * 2)
                    }
                    ZooAnimal.PIGEON -> {
                        grossWin += bets.pigeon * 8 + (bets.birdGeneral * 2)
                    }
                    ZooAnimal.PEACOCK -> {
                        grossWin += bets.peacock * 8 + (bets.birdGeneral * 2)
                    }
                    ZooAnimal.EAGLE -> {
                        grossWin += bets.eagle * 12 + (bets.birdGeneral * 2)
                    }
                    ZooAnimal.SHARK -> {
                        grossWin += bets.shark * 24
                    }
                    ZooAnimal.GOLDEN_TOAD -> {
                        // Golden Toad 100x bonus payout
                        grossWin += (bets.shark * 24) + ((bets.beastGeneral + bets.birdGeneral) * 2) + 1000L
                    }
                    ZooAnimal.WILD_CHEST -> {
                        grossWin += bets.wild * 50 + ((bets.beastGeneral + bets.birdGeneral) * 2)
                    }
                }

                // Dynamic Live Commission Fee & Promotional Multiplier Calculation
                val cfg = _uiState.value.liveConfig
                val multiplier = if (cfg.isMultiplierEventActive) cfg.multiplierRate else 1.0
                val totalGross = (grossWin * multiplier).toLong()
                val commissionRate = cfg.houseCommissionPercent / 100.0
                val fee = if (totalGross > 0 && commissionRate > 0.0) {
                    kotlin.math.max(1L, kotlin.math.round(totalGross * commissionRate).toLong())
                } else 0L
                val netWin = totalGross - fee

                val newRecord = ZooHistoryItem(
                    id = System.currentTimeMillis(),
                    winningSlot = targetSlotIndex,
                    winningAnimal = winningAnimal,
                    multiplier = winningAnimal.baseMultiplier,
                    netWin = netWin,
                    feeDeducted = fee
                )

                if (netWin > 0) {
                    adjustUnifiedBalance(netWin, "Zoo Roulette win payout ($winningAnimal)")
                }

                _uiState.update { state ->
                    val newCredits = state.credits + netWin
                    val newTotalFees = state.totalFeesCollected + fee
                    state.copy(
                        zooPhase = ZooGamePhase.RESULT,
                        credits = newCredits,
                        totalFeesCollected = newTotalFees,
                        zooLastWin = netWin,
                        zooLastGrossWin = totalGross,
                        zooLastFee = fee,
                        zooWinParticleKey = if (netWin > 0) System.currentTimeMillis() else 0L,
                        zooPreviousBets = state.zooBets,
                        zooBets = ZooPlayerBets(),
                        zooHistory = (state.zooHistory + newRecord).takeLast(25)
                    )
                }

                delay(4000)
            }
        }
    }

    private fun adjustUnifiedBalance(delta: Long, reason: String) {
        val user = _uiState.value.currentUser
        if (user != null) {
            userAccountRepo.adjustPoints(delta)
        }
        val activeUserId = paymentRepo.getActiveUser()?.id ?: 1001L
        paymentRepo.adjustBalanceForGame(activeUserId, delta.toDouble(), reason)
    }

    // Zoo Roulette Betting Actions
    fun placeZooBet(target: ZooBetTarget) {
        val state = _uiState.value
        // Online Requirement & Remote Game Availability Check
        if (!state.isOnline) return
        if (state.liveConfig.isGlobalMaintenance || !state.liveConfig.isZooRouletteOnline) return
        if (state.zooPhase != ZooGamePhase.BETTING) return
        val amount = state.selectedChip.value
        if (state.credits < amount) return
        // Remote Max Bet Limit Check
        if (state.zooBets.total + amount > state.liveConfig.maxBetLimit) return

        adjustUnifiedBalance(-amount, "Zoo Roulette bet placed on $target")
        _uiState.update { s ->
            val updatedCredits = s.credits - amount
            val current = s.zooBets
            val updatedBets = when (target) {
                ZooBetTarget.MONKEY -> current.copy(monkey = current.monkey + amount)
                ZooBetTarget.RABBIT -> current.copy(rabbit = current.rabbit + amount)
                ZooBetTarget.LION -> current.copy(lion = current.lion + amount)
                ZooBetTarget.PANDA -> current.copy(panda = current.panda + amount)
                ZooBetTarget.BEAST_GENERAL -> current.copy(beastGeneral = current.beastGeneral + amount)
                ZooBetTarget.SWALLOW -> current.copy(swallow = current.swallow + amount)
                ZooBetTarget.PIGEON -> current.copy(pigeon = current.pigeon + amount)
                ZooBetTarget.PEACOCK -> current.copy(peacock = current.peacock + amount)
                ZooBetTarget.EAGLE -> current.copy(eagle = current.eagle + amount)
                ZooBetTarget.BIRD_GENERAL -> current.copy(birdGeneral = current.birdGeneral + amount)
                ZooBetTarget.SHARK -> current.copy(shark = current.shark + amount)
                ZooBetTarget.WILD -> current.copy(wild = current.wild + amount)
            }
            s.copy(credits = updatedCredits, zooBets = updatedBets)
        }
    }

    fun clearZooBets() {
        val state = _uiState.value
        if (!state.isOnline) return
        if (state.zooPhase != ZooGamePhase.BETTING) return
        val refund = state.zooBets.total
        if (refund > 0) {
            adjustUnifiedBalance(refund, "Zoo Roulette bets cleared refund")
        }
        _uiState.update { s ->
            s.copy(
                credits = s.credits + refund,
                zooBets = ZooPlayerBets()
            )
        }
    }

    fun doubleZooBets() {
        val state = _uiState.value
        if (!state.isOnline) return
        if (state.zooPhase != ZooGamePhase.BETTING) return
        val extraCost = state.zooBets.total
        if (extraCost == 0L || state.credits < extraCost) return

        adjustUnifiedBalance(-extraCost, "Zoo Roulette double bets")
        _uiState.update { s ->
            val b = s.zooBets
            s.copy(
                credits = s.credits - extraCost,
                zooBets = b.copy(
                    monkey = b.monkey * 2,
                    rabbit = b.rabbit * 2,
                    lion = b.lion * 2,
                    panda = b.panda * 2,
                    beastGeneral = b.beastGeneral * 2,
                    swallow = b.swallow * 2,
                    pigeon = b.pigeon * 2,
                    peacock = b.peacock * 2,
                    eagle = b.eagle * 2,
                    birdGeneral = b.birdGeneral * 2,
                    shark = b.shark * 2,
                    wild = b.wild * 2
                )
            )
        }
    }

    fun rebetZooPrevious() {
        val state = _uiState.value
        if (!state.isOnline) return
        if (state.zooPhase != ZooGamePhase.BETTING) return
        val cost = state.zooPreviousBets.total
        if (cost == 0L || state.credits < cost) return

        adjustUnifiedBalance(-cost, "Zoo Roulette rebet previous")
        _uiState.update { s ->
            s.copy(
                credits = s.credits - cost,
                zooBets = s.zooPreviousBets
            )
        }
    }

    private fun seedInitialHistory() {
        val initialList = mutableListOf<HistoryRecord>()
        for (i in 1..12) {
            val dCard = PlayingCard.random()
            val tCard = PlayingCard.random()
            val winner = when {
                dCard.rank > tCard.rank -> RoundWinner.DRAGON
                tCard.rank > dCard.rank -> RoundWinner.TIGER
                else -> RoundWinner.TIE
            }
            initialList.add(
                HistoryRecord(
                    roundId = i.toLong(),
                    winner = winner,
                    dragonCard = dCard,
                    tigerCard = tCard
                )
            )
        }
        _uiState.update { it.copy(historyList = initialList, roundNumber = 13L) }
    }

    fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                // Online connectivity gate: pause game loop when offline
                while (!_uiState.value.isOnline) {
                    delay(1000)
                }

                // 1. Betting Open Phase (Synced with Remote Admin Live Config)
                val countdown = _uiState.value.liveConfig.dragonCountdownSeconds
                _uiState.update {
                    it.copy(
                        gamePhase = GamePhase.BETTING_OPEN,
                        countdownRemaining = countdown,
                        isCardsRevealed = false,
                        roundWinner = null,
                        lastWinAmount = 0L,
                        lastGrossWinAmount = 0L,
                        lastFeeDeducted = 0L
                    )
                }

                for (sec in countdown downTo 1) {
                    while (!_uiState.value.isOnline) {
                        delay(1000)
                    }
                    _uiState.update { it.copy(countdownRemaining = sec) }
                    delay(1000)
                }

                // 2. Stop Betting Phase
                _uiState.update {
                    it.copy(
                        gamePhase = GamePhase.STOP_BETTING,
                        countdownRemaining = 0
                    )
                }
                delay(1200)

                // 3. Dealing Cards Phase
                val dCard = PlayingCard.random()
                val tCard = PlayingCard.random()
                val isFast = _uiState.value.fastDealMode

                _uiState.update {
                    it.copy(
                        gamePhase = GamePhase.DEALING_CARDS,
                        dragonCard = dCard,
                        tigerCard = tCard,
                        isCardsRevealed = false
                    )
                }
                delay(if (isFast) 600 else 1100)

                // Reveal Cards
                _uiState.update { it.copy(isCardsRevealed = true) }
                delay(if (isFast) 700 else 1200)

                // 4. Calculate Result
                val winner = when {
                    dCard.rank > tCard.rank -> RoundWinner.DRAGON
                    tCard.rank > dCard.rank -> RoundWinner.TIGER
                    else -> RoundWinner.TIE
                }

                val userBet = _uiState.value.currentBet
                var grossPayout = 0L
                var feeDeducted = 0L
                var netWinAmount = 0L

                // Dynamic Live fee cut and multiplier from Admin configuration
                val cfg = _uiState.value.liveConfig
                val commissionRate = cfg.houseCommissionPercent / 100.0
                val multiplier = if (cfg.isMultiplierEventActive) cfg.multiplierRate else 1.0

                when (winner) {
                    RoundWinner.DRAGON -> {
                        if (userBet.dragon > 0) {
                            val bet = userBet.dragon
                            val profit = (bet * multiplier).toLong()
                            val fee = if (profit > 0 && commissionRate > 0.0) kotlin.math.max(1L, kotlin.math.round(profit * commissionRate).toLong()) else 0L
                            val gross = bet + profit
                            val net = gross - fee

                            grossPayout += gross
                            feeDeducted += fee
                            netWinAmount += net
                        }
                    }
                    RoundWinner.TIGER -> {
                        if (userBet.tiger > 0) {
                            val bet = userBet.tiger
                            val profit = (bet * multiplier).toLong()
                            val fee = if (profit > 0 && commissionRate > 0.0) kotlin.math.max(1L, kotlin.math.round(profit * commissionRate).toLong()) else 0L
                            val gross = bet + profit
                            val net = gross - fee

                            grossPayout += gross
                            feeDeducted += fee
                            netWinAmount += net
                        }
                    }
                    RoundWinner.TIE -> {
                        if (userBet.tie > 0) {
                            val bet = userBet.tie
                            val profit = (bet * 8 * multiplier).toLong()
                            val fee = if (profit > 0 && commissionRate > 0.0) kotlin.math.max(1L, kotlin.math.round(profit * commissionRate).toLong()) else 0L
                            val gross = bet + profit
                            val net = gross - fee

                            grossPayout += gross
                            feeDeducted += fee
                            netWinAmount += net
                        }
                        // Refund 50% on Dragon or Tiger bets during Tie (no fee on refund push)
                        if (userBet.dragon > 0) {
                            val refund = userBet.dragon / 2
                            netWinAmount += refund
                            grossPayout += refund
                        }
                        if (userBet.tiger > 0) {
                            val refund = userBet.tiger / 2
                            netWinAmount += refund
                            grossPayout += refund
                        }
                    }
                }

                val currentRound = _uiState.value.roundNumber
                val newRecord = HistoryRecord(
                    roundId = currentRound,
                    winner = winner,
                    dragonCard = dCard,
                    tigerCard = tCard,
                    netWinAmount = netWinAmount,
                    feeDeducted = feeDeducted
                )

                if (netWinAmount > 0) {
                    adjustUnifiedBalance(netWinAmount, "Dragon vs Tiger win payout ($winner)")
                }

                _uiState.update { state ->
                    // Crediting the user's account with the net winnings after 3% fee deduction
                    val updatedCredits = state.credits + netWinAmount
                    val updatedTotalFees = state.totalFeesCollected + feeDeducted
                    state.copy(
                        gamePhase = GamePhase.RESULT,
                        roundWinner = winner,
                        credits = updatedCredits,
                        lastWinAmount = netWinAmount,
                        lastGrossWinAmount = grossPayout,
                        lastFeeDeducted = feeDeducted,
                        totalFeesCollected = updatedTotalFees,
                        showWinParticleKey = if (netWinAmount > 0) System.currentTimeMillis() else 0L,
                        previousBet = state.currentBet,
                        currentBet = PlayerBet(),
                        roundNumber = currentRound + 1,
                        historyList = state.historyList + newRecord
                    )
                }

                delay(if (isFast) 2200 else 3500)
            }
        }
    }

    // Player Actions
    fun placeBet(option: BetOption) {
        val state = _uiState.value
        // Online Requirement & Remote Game Availability Check
        if (!state.isOnline) return
        if (state.liveConfig.isGlobalMaintenance || !state.liveConfig.isDragonTigerOnline) return
        if (state.gamePhase != GamePhase.BETTING_OPEN) return
        val amount = state.selectedChip.value
        if (state.credits < amount) return
        // Remote Max Bet Limit Check
        if (state.currentBet.total + amount > state.liveConfig.maxBetLimit) return

        adjustUnifiedBalance(-amount, "Dragon vs Tiger bet on $option")
        _uiState.update { s ->
            val updatedCredits = s.credits - amount
            val newBet = when (option) {
                BetOption.DRAGON -> s.currentBet.copy(dragon = s.currentBet.dragon + amount)
                BetOption.TIE -> s.currentBet.copy(tie = s.currentBet.tie + amount)
                BetOption.TIGER -> s.currentBet.copy(tiger = s.currentBet.tiger + amount)
            }
            s.copy(credits = updatedCredits, currentBet = newBet)
        }
    }

    fun clearBets() {
        val state = _uiState.value
        if (!state.isOnline) return
        if (state.gamePhase != GamePhase.BETTING_OPEN) return
        val refund = state.currentBet.total
        if (refund > 0) {
            adjustUnifiedBalance(refund, "Dragon vs Tiger bets cleared refund")
        }
        _uiState.update { s ->
            s.copy(
                credits = s.credits + refund,
                currentBet = PlayerBet()
            )
        }
    }

    fun doubleBets() {
        val state = _uiState.value
        if (!state.isOnline) return
        if (state.gamePhase != GamePhase.BETTING_OPEN) return
        val extraCost = state.currentBet.total
        if (extraCost == 0L || state.credits < extraCost) return

        adjustUnifiedBalance(-extraCost, "Dragon vs Tiger double bets")
        _uiState.update { s ->
            s.copy(
                credits = s.credits - extraCost,
                currentBet = s.currentBet.copy(
                    dragon = s.currentBet.dragon * 2,
                    tie = s.currentBet.tie * 2,
                    tiger = s.currentBet.tiger * 2
                )
            )
        }
    }

    fun rebetPrevious() {
        val state = _uiState.value
        if (!state.isOnline) return
        if (state.gamePhase != GamePhase.BETTING_OPEN) return
        val cost = state.previousBet.total
        if (cost == 0L || state.credits < cost) return

        adjustUnifiedBalance(-cost, "Dragon vs Tiger rebet previous")
        _uiState.update { s ->
            s.copy(
                credits = s.credits - cost,
                currentBet = s.previousBet
            )
        }
    }

    fun selectChip(chip: ChipDenomination) {
        _uiState.update { it.copy(selectedChip = chip) }
    }

    fun selectTheme(theme: TableTheme) {
        _uiState.update { it.copy(tableTheme = theme) }
    }

    fun setCountdownDuration(seconds: Int) {
        _uiState.update { it.copy(countdownConfigSeconds = seconds) }
    }

    fun toggleSound(enabled: Boolean) {
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun toggleFastDeal(enabled: Boolean) {
        _uiState.update { it.copy(fastDealMode = enabled) }
    }

    fun reloadDemoCredits() {
        adjustUnifiedBalance(10_000L, "Reload Demo Balance")
        _uiState.update { it.copy(credits = it.credits + 10_000L) }
    }

    fun navigateToScreen(screen: Screen) {
        if (screen == Screen.ADMIN_MONITOR || screen == Screen.ADMIN_UPDATES) {
            val user = _uiState.value.currentUser
            if (user?.isAdmin != true || !adminSecurityService.isSessionActive()) {
                _uiState.update {
                    it.copy(
                        currentScreen = Screen.ADMIN_LOGIN,
                        adminAuthError = "Server verification required. Please authenticate as administrator."
                    )
                }
                return
            }
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun navigateToAdminPortal() {
        val user = _uiState.value.currentUser
        if (user?.isAdmin == true && adminSecurityService.isSessionActive()) {
            adminSecurityService.logAction(
                adminId = _uiState.value.adminSession?.adminId ?: "admin",
                action = "INSPECT_MONITOR",
                details = "Admin accessed live game monitor dashboard"
            )
            _uiState.update { it.copy(currentScreen = Screen.ADMIN_MONITOR) }
        } else {
            _uiState.update { it.copy(currentScreen = Screen.ADMIN_LOGIN, adminAuthError = null) }
        }
    }

    fun navigateToWallet(targetView: AppView = AppView.USER_DASHBOARD) {
        _uiState.update { it.copy(currentScreen = Screen.WALLET, walletInitialView = targetView) }
    }

    fun navigateToAuth() {
        _uiState.update { it.copy(currentScreen = Screen.AUTH, authError = null) }
    }

    fun openSettings(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun openHistory(open: Boolean) {
        _uiState.update { it.copy(isHistoryOpen = open) }
    }

    fun openRules(open: Boolean) {
        _uiState.update { it.copy(isRulesOpen = open) }
    }

    // --- Authentication & User Profile ---
    fun login(usernameOrEmail: String, password: String, rememberMe: Boolean) {
        when (val res = userAccountRepo.login(usernameOrEmail, password, rememberMe)) {
            is AuthResult.Success -> {
                if (res.user.isAdmin) {
                    adminSecurityService.authenticate(usernameOrEmail, password, rememberMe)
                    _uiState.update {
                        it.copy(
                            currentUser = res.user,
                            credits = res.user.gamePoints,
                            authError = null,
                            currentScreen = Screen.ADMIN_MONITOR
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            currentUser = res.user,
                            credits = res.user.gamePoints,
                            authError = null,
                            currentScreen = Screen.HOME
                        )
                    }
                }
            }
            is AuthResult.Error -> {
                _uiState.update { it.copy(authError = res.message) }
            }
        }
    }

    fun register(username: String, email: String, password: String, avatar: String) {
        when (val res = userAccountRepo.register(username, email, password, avatar)) {
            is AuthResult.Success -> {
                _uiState.update {
                    it.copy(
                        currentUser = res.user,
                        credits = res.user.gamePoints,
                        authError = null,
                        currentScreen = Screen.HOME
                    )
                }
            }
            is AuthResult.Error -> {
                _uiState.update { it.copy(authError = res.message) }
            }
        }
    }

    fun logout() {
        userAccountRepo.logout()
        _uiState.update { it.copy(currentUser = null, currentScreen = Screen.AUTH) }
    }

    fun forgotPassword(email: String, newPw: String) {
        when (val res = userAccountRepo.forgotPassword(email, newPw)) {
            is AuthResult.Success -> {
                _uiState.update { it.copy(authError = "Password successfully reset! Please log in.") }
            }
            is AuthResult.Error -> {
                _uiState.update { it.copy(authError = res.message) }
            }
        }
    }

    fun quickDemoLogin(isAdmin: Boolean) {
        // Demo shortcuts disabled as per security requirements
    }

    // --- GitHub Releases Update Engine ---
    fun checkForUpdates(forceUserCheck: Boolean = false) {
        viewModelScope.launch {
            val res = updateManager.checkForUpdates(forceUserCheck = forceUserCheck)
            _uiState.update { it.copy(updateCheckResult = res) }
        }
    }

    fun openUpdateDialog(open: Boolean, release: GameRelease? = null) {
        _uiState.update {
            it.copy(
                isUpdateDialogOpen = open,
                selectedReleaseForUpdate = release ?: (it.updateCheckResult as? UpdateCheckResult.UpdateAvailable)?.latestRelease
            )
        }
    }

    fun startDownloadAndApplyUpdate(release: GameRelease) {
        viewModelScope.launch {
            updateManager.downloadAndApplyUpdate(release)
        }
    }

    fun restartGameToNewVersion() {
        updateManager.restartGameToNewVersion()
        _uiState.update {
            it.copy(
                isUpdateDialogOpen = false,
                currentVersion = updateManager.currentVersion.value
            )
        }
    }

    fun adminTogglePublish(versionName: String, isPublished: Boolean) {
        updateManager.adminPublishRelease(versionName, isPublished)
    }

    fun adminSetInstalledVersion(versionName: String) {
        updateManager.adminSetInstalledVersion(versionName)
        checkForUpdates()
    }

    // --- Administrator Server-Side Authentication & Session Management ---
    fun authenticateAdmin(adminId: String, pass: String, rememberMe: Boolean) {
        _uiState.update { it.copy(isAdminAuthLoading = true, adminAuthError = null) }
        viewModelScope.launch {
            delay(350)
            when (val res = adminSecurityService.authenticate(adminId, pass, rememberMe)) {
                is AdminAuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAdminAuthLoading = false,
                            adminAuthError = null,
                            adminLockoutSeconds = 0L,
                            adminSession = res.session,
                            currentScreen = Screen.ADMIN_MONITOR
                        )
                    }
                }
                is AdminAuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAdminAuthLoading = false,
                            adminAuthError = res.message
                        )
                    }
                }
                is AdminAuthResult.LockedOut -> {
                    _uiState.update {
                        it.copy(
                            isAdminAuthLoading = false,
                            adminLockoutSeconds = res.retryAfterSeconds,
                            adminAuthError = "Rate limit active. Please wait ${res.retryAfterSeconds} seconds."
                        )
                    }
                }
            }
        }
    }

    fun logoutAdmin() {
        adminSecurityService.logout()
        _uiState.update {
            it.copy(
                adminSession = null,
                currentScreen = Screen.HOME
            )
        }
    }

    fun extendAdminSession() {
        adminSecurityService.extendSession()
    }

    // --- Real-time Game Telemetry Stream Controls ---
    fun simulateDisconnectStream() {
        adminSecurityService.logAction(
            adminId = _uiState.value.adminSession?.adminId ?: "admin",
            action = "DISCONNECT_STREAM",
            details = "Admin simulated WebSocket connection disconnect"
        )
        realtimeGameMonitorService.simulateDisconnect()
    }

    fun reconnectStream() {
        adminSecurityService.logAction(
            adminId = _uiState.value.adminSession?.adminId ?: "admin",
            action = "RECONNECT_STREAM",
            details = "Admin reconnected real-time telemetry stream"
        )
        realtimeGameMonitorService.reconnect()
    }

    fun togglePauseStream() {
        adminSecurityService.logAction(
            adminId = _uiState.value.adminSession?.adminId ?: "admin",
            action = if (_uiState.value.liveTelemetry.isPaused) "RESUME_STREAM" else "PAUSE_STREAM",
            details = "Admin toggled real-time telemetry stream"
        )
        realtimeGameMonitorService.togglePauseStream()
    }

    fun advanceSimulatedRound() {
        adminSecurityService.logAction(
            adminId = _uiState.value.adminSession?.adminId ?: "admin",
            action = "ADVANCE_ROUND",
            details = "Admin manually advanced simulated telemetry cycle"
        )
        realtimeGameMonitorService.resetOrAdvanceRound()
    }

    fun injectSimulatedPlays(sideAExtra: Int, sideBExtra: Int) {
        realtimeGameMonitorService.injectSimulatedPlays(sideAExtra, sideBExtra)
    }

    // --- Online Network Connectivity Gate ---
    fun retryConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingConnection = true) }
            delay(1000)
            val isNowOnline = networkObserver.refreshNetworkStatus()
            _uiState.update { it.copy(isOnline = isNowOnline, isCheckingConnection = false) }
        }
    }

    fun toggleSimulatedOffline(offline: Boolean) {
        networkObserver.setSimulatedOffline(offline)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        zooGameLoopJob?.cancel()
    }
}
