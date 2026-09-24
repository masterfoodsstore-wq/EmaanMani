package com.example.repository

import com.example.model.LiveGameTelemetry
import com.example.model.LiveSideActivity
import com.example.model.RealtimeConnectionStatus
import com.example.model.SimulatedPlayEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

/**
 * Real-time Streaming Game Monitor Service.
 *
 * Implements a high-resilience simulated WebSocket pipeline delivering continuous
 * telemetry events for the non-monetary game suite.
 *
 * Analytics-Only Design:
 * - Strictly observes simulated game activity.
 * - Does not predict outcomes, alter game logic, or process monetary transactions.
 */
class RealtimeGameMonitorService {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var streamingJob: Job? = null
    private var heartbeatJob: Job? = null

    private val _telemetry = MutableStateFlow(
        LiveGameTelemetry(
            roundId = 512L,
            activePlayers = 138,
            totalSimulatedPlays = 25,
            sideA = LiveSideActivity(
                sideId = "SIDE_A",
                name = "Side A (Dragon)",
                simulatedPlays = 20,
                percentage = 80.0,
                isLeading = true,
                colorHex = 0xFFE53935
            ),
            sideB = LiveSideActivity(
                sideId = "SIDE_B",
                name = "Side B (Tiger)",
                simulatedPlays = 5,
                percentage = 20.0,
                isLeading = false,
                colorHex = 0xFF1E88E5
            ),
            lastUpdateTime = System.currentTimeMillis(),
            connectionStatus = RealtimeConnectionStatus.LIVE,
            latencyMs = 22L,
            recentActivityFeed = listOf(
                SimulatedPlayEvent("EV-1", System.currentTimeMillis() - 8000, "Player_#891", "Side A", 100L, "🦁"),
                SimulatedPlayEvent("EV-2", System.currentTimeMillis() - 6000, "Player_#442", "Side A", 250L, "🐯"),
                SimulatedPlayEvent("EV-3", System.currentTimeMillis() - 4000, "Player_#109", "Side B", 50L, "🐼"),
                SimulatedPlayEvent("EV-4", System.currentTimeMillis() - 2000, "Player_#723", "Side A", 150L, "🦅"),
                SimulatedPlayEvent("EV-5", System.currentTimeMillis() - 500, "Player_#318", "Side B", 80L, "🦈")
            )
        )
    )
    val telemetry: StateFlow<LiveGameTelemetry> = _telemetry.asStateFlow()

    private val playerAliases = listOf(
        "DragonMaster_88", "TigerStrike_99", "PhoenixRider", "CyberWolf", "LuckyLion_7",
        "ApexPredator", "ShadowHunter", "NeonSamurai", "GoldenViper", "ZenMaster_42",
        "Valkyrie_X", "QuantumRacer", "StormBringer", "FalconEye", "Viper_007"
    )

    private val avatars = listOf("🦁", "🐯", "🦅", "🦈", "🐼", "🦚", "👑", "⚡", "🥷")

    init {
        startRealtimeStream()
        startHeartbeat()
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            while (isActive) {
                delay(3000)
                if (_telemetry.value.connectionStatus == RealtimeConnectionStatus.LIVE) {
                    val jitterLatency = Random.nextLong(14, 38)
                    val activeFluctuation = Random.nextInt(125, 155)
                    _telemetry.update {
                        it.copy(
                            latencyMs = jitterLatency,
                            activePlayers = activeFluctuation
                        )
                    }
                }
            }
        }
    }

    fun startRealtimeStream() {
        if (streamingJob?.isActive == true) return

        streamingJob = serviceScope.launch {
            _telemetry.update { it.copy(connectionStatus = RealtimeConnectionStatus.LIVE, isPaused = false) }

            var roundTimerSeconds = 0

            while (isActive) {
                // Incoming simulated activity arrives every 1.5 to 2.8 seconds
                val stepDelay = Random.nextLong(1500, 2800)
                delay(stepDelay)

                if (_telemetry.value.isPaused || _telemetry.value.connectionStatus != RealtimeConnectionStatus.LIVE) {
                    continue
                }

                roundTimerSeconds += 2

                // Periodic round reset/advance every ~45 seconds
                if (roundTimerSeconds >= 45) {
                    roundTimerSeconds = 0
                    advanceRoundInternal()
                    continue
                }

                // Generate new simulated activity event
                // Slight weight to create dynamic percentage shifts between Side A and Side B
                val chooseSideA = Random.nextInt(100) < 65
                val sideTarget = if (chooseSideA) "Side A" else "Side B"
                val amount = listOf(25L, 50L, 100L, 250L, 500L, 1000L).random()
                val player = playerAliases.random()
                val avatar = avatars.random()

                val newEvent = SimulatedPlayEvent(
                    id = "EV-${UUID.randomUUID().toString().take(6)}",
                    timestamp = System.currentTimeMillis(),
                    playerAlias = player,
                    sideTarget = sideTarget,
                    simulatedAmount = amount,
                    avatar = avatar
                )

                _telemetry.update { current ->
                    val newSideAPlays = current.sideA.simulatedPlays + (if (chooseSideA) 1 else 0)
                    val newSideBPlays = current.sideB.simulatedPlays + (if (!chooseSideA) 1 else 0)
                    val newTotal = newSideAPlays + newSideBPlays

                    val pctA = if (newTotal > 0) (newSideAPlays.toDouble() / newTotal) * 100.0 else 50.0
                    val pctB = if (newTotal > 0) (newSideBPlays.toDouble() / newTotal) * 100.0 else 50.0

                    val updatedFeed = (listOf(newEvent) + current.recentActivityFeed).take(30)

                    current.copy(
                        totalSimulatedPlays = newTotal,
                        sideA = current.sideA.copy(
                            simulatedPlays = newSideAPlays,
                            percentage = pctA,
                            isLeading = newSideAPlays > newSideBPlays
                        ),
                        sideB = current.sideB.copy(
                            simulatedPlays = newSideBPlays,
                            percentage = pctB,
                            isLeading = newSideBPlays > newSideAPlays
                        ),
                        lastUpdateTime = System.currentTimeMillis(),
                        recentActivityFeed = updatedFeed
                    )
                }
            }
        }
    }

    private fun advanceRoundInternal() {
        _telemetry.update { current ->
            val nextRound = current.roundId + 1L
            // Reset counters for new round
            val initialA = Random.nextInt(15, 25)
            val initialB = Random.nextInt(5, 12)
            val initialTotal = initialA + initialB
            val pctA = (initialA.toDouble() / initialTotal) * 100.0
            val pctB = (initialB.toDouble() / initialTotal) * 100.0

            val roundEvent = SimulatedPlayEvent(
                id = "ROUND-${nextRound}",
                timestamp = System.currentTimeMillis(),
                playerAlias = "SYSTEM",
                sideTarget = "Round #$nextRound Started",
                simulatedAmount = 0L,
                avatar = "🔄"
            )

            current.copy(
                roundId = nextRound,
                totalSimulatedPlays = initialTotal,
                sideA = current.sideA.copy(
                    simulatedPlays = initialA,
                    percentage = pctA,
                    isLeading = initialA > initialB
                ),
                sideB = current.sideB.copy(
                    simulatedPlays = initialB,
                    percentage = pctB,
                    isLeading = initialB > initialA
                ),
                lastUpdateTime = System.currentTimeMillis(),
                recentActivityFeed = (listOf(roundEvent) + current.recentActivityFeed).take(30)
            )
        }
    }

    /**
     * Simulate network disconnect to test graceful WebSocket handling.
     */
    fun simulateDisconnect() {
        _telemetry.update { it.copy(connectionStatus = RealtimeConnectionStatus.DISCONNECTED) }
    }

    /**
     * Reconnect to the real-time stream with automatic handshake.
     */
    fun reconnect() {
        serviceScope.launch {
            _telemetry.update { it.copy(connectionStatus = RealtimeConnectionStatus.RECONNECTING) }
            delay(1200)
            _telemetry.update {
                it.copy(
                    connectionStatus = RealtimeConnectionStatus.LIVE,
                    latencyMs = Random.nextLong(16, 28),
                    lastUpdateTime = System.currentTimeMillis()
                )
            }
            startRealtimeStream()
        }
    }

    /**
     * Pause / Resume live stream.
     */
    fun togglePauseStream() {
        _telemetry.update { it.copy(isPaused = !it.isPaused) }
    }

    /**
     * Manually advance to next round for testing.
     */
    fun resetOrAdvanceRound() {
        advanceRoundInternal()
    }

    /**
     * Manually inject simulated plays to demonstrate dynamic percentage shifts.
     */
    fun injectSimulatedPlays(sideAExtra: Int, sideBExtra: Int) {
        _telemetry.update { current ->
            val newA = current.sideA.simulatedPlays + sideAExtra
            val newB = current.sideB.simulatedPlays + sideBExtra
            val total = newA + newB
            val pctA = if (total > 0) (newA.toDouble() / total) * 100.0 else 50.0
            val pctB = if (total > 0) (newB.toDouble() / total) * 100.0 else 50.0

            val sampleEvents = mutableListOf<SimulatedPlayEvent>()
            if (sideAExtra > 0) {
                sampleEvents.add(
                    SimulatedPlayEvent(
                        id = "EV-${UUID.randomUUID().toString().take(6)}",
                        timestamp = System.currentTimeMillis(),
                        playerAlias = playerAliases.random(),
                        sideTarget = "Side A",
                        simulatedAmount = 250L,
                        avatar = avatars.random()
                    )
                )
            }
            if (sideBExtra > 0) {
                sampleEvents.add(
                    SimulatedPlayEvent(
                        id = "EV-${UUID.randomUUID().toString().take(6)}",
                        timestamp = System.currentTimeMillis(),
                        playerAlias = playerAliases.random(),
                        sideTarget = "Side B",
                        simulatedAmount = 150L,
                        avatar = avatars.random()
                    )
                )
            }

            current.copy(
                totalSimulatedPlays = total,
                sideA = current.sideA.copy(
                    simulatedPlays = newA,
                    percentage = pctA,
                    isLeading = newA > newB
                ),
                sideB = current.sideB.copy(
                    simulatedPlays = newB,
                    percentage = pctB,
                    isLeading = newB > newA
                ),
                lastUpdateTime = System.currentTimeMillis(),
                recentActivityFeed = (sampleEvents + current.recentActivityFeed).take(30)
            )
        }
    }
}
