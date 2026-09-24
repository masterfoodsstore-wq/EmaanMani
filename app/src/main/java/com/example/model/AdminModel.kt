package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Real-time connection status for the Live Game Monitor WebSocket / Streaming engine.
 */
enum class RealtimeConnectionStatus(val label: String, val colorHex: Long) {
    CONNECTING("Connecting...", 0xFFFFB300),
    LIVE("Live", 0xFF00E676),
    RECONNECTING("Reconnecting...", 0xFFFF9100),
    DISCONNECTED("Disconnected", 0xFFFF5252)
}

/**
 * Side Activity statistics for non-monetary game analytics.
 */
data class LiveSideActivity(
    val sideId: String,
    val name: String,
    val simulatedPlays: Int,
    val percentage: Double,
    val isLeading: Boolean = false,
    val colorHex: Long = 0xFFE53935
) {
    val formattedPercentage: String
        get() = String.format(Locale.US, "%.1f%%", percentage)
}

/**
 * Individual simulated play event arriving in real-time.
 */
data class SimulatedPlayEvent(
    val id: String,
    val timestamp: Long,
    val playerAlias: String,
    val sideTarget: String,
    val simulatedAmount: Long,
    val avatar: String = "🦁"
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

/**
 * Live game telemetry payload streamed in real-time to the Admin Dashboard.
 * Strictly analytics-only for non-monetary gameplay.
 */
data class LiveGameTelemetry(
    val roundId: Long = 101L,
    val activePlayers: Int = 120,
    val totalSimulatedPlays: Int = 25,
    val sideA: LiveSideActivity = LiveSideActivity(
        sideId = "SIDE_A",
        name = "Side A (Dragon)",
        simulatedPlays = 20,
        percentage = 80.0,
        isLeading = true,
        colorHex = 0xFFE53935
    ),
    val sideB: LiveSideActivity = LiveSideActivity(
        sideId = "SIDE_B",
        name = "Side B (Tiger)",
        simulatedPlays = 5,
        percentage = 20.0,
        isLeading = false,
        colorHex = 0xFF1E88E5
    ),
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val connectionStatus: RealtimeConnectionStatus = RealtimeConnectionStatus.LIVE,
    val latencyMs: Long = 24L,
    val recentActivityFeed: List<SimulatedPlayEvent> = emptyList(),
    val isPaused: Boolean = false
) {
    val formattedLastUpdate: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(lastUpdateTime))
}

/**
 * Secure Admin Session issued upon server-side authentication.
 * Includes cryptographic session token, expiration timer, and role permissions.
 */
data class AdminSession(
    val token: String,
    val adminId: String,
    val adminName: String,
    val role: String = "SUPER_ADMIN",
    val issuedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30 * 60 * 1000L) // 30 minutes lifetime
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAt

    val remainingSeconds: Long
        get() = maxOf(0L, (expiresAt - System.currentTimeMillis()) / 1000L)

    val formattedExpiresTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(expiresAt))
}

/**
 * Immutable audit log entry recording all administrator operations.
 */
data class AdminAuditLog(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val adminId: String,
    val action: String,
    val details: String,
    val clientEndpoint: String = "127.0.0.1 (Internal Gateway)"
) {
    val formattedTimestamp: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

/**
 * Authentication response from the secure Admin Authorization Service.
 */
sealed class AdminAuthResult {
    data class Success(val session: AdminSession) : AdminAuthResult()
    data class Error(val message: String) : AdminAuthResult()
    data class LockedOut(val retryAfterSeconds: Long) : AdminAuthResult()
}
