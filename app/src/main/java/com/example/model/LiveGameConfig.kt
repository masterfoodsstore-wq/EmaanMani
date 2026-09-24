package com.example.model

data class LiveGameConfig(
    // System Availability & Maintenance Mode
    val isGlobalMaintenance: Boolean = false,
    val maintenanceTitle: String = "SYSTEM SERVER UPDATE IN PROGRESS",
    val maintenanceMessage: String = "The game server is currently applying an Over-The-Air hot-patch. Live tables will resume in a few moments without requiring APK re-download.",
    val maintenanceEtaMinutes: Int = 5,

    // Per-Game Live Switch
    val isDragonTigerOnline: Boolean = true,
    val isZooRouletteOnline: Boolean = true,
    val isCyberSlotsOnline: Boolean = false,
    val dragonTigerStatusNote: String = "Live Dealing Active",
    val zooRouletteStatusNote: String = "Perimeter Wheel Running",
    val cyberSlotsStatusNote: String = "Under Scheduled Maintenance",

    // Live Game Economy & Timing
    val houseCommissionPercent: Double = 3.0,
    val dragonCountdownSeconds: Int = 15,
    val zooCountdownSeconds: Int = 18,
    val minBetLimit: Long = 10L,
    val maxBetLimit: Long = 50_000L,

    // Live Promotional Multiplier Event
    val isMultiplierEventActive: Boolean = false,
    val multiplierRate: Double = 1.0,
    val multiplierTitle: String = "🔥 2X GOLDEN EVENT ACTIVE",

    // Live Broadcast Announcement Ticker
    val isBroadcastActive: Boolean = true,
    val liveBroadcastMessage: String = "🔥 Welcome to Dragon vs Tiger! EasyPaisa & JazzCash Instant Deposits Active • 24/7 Instant Cashier Payouts!",

    // Over-The-Air (OTA) Remote Push
    val liveAppVersion: String = "v2.5.0-OTA",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val onlinePlayerCount: Int = 2418,
    val lastAdminEditor: String = "admin@system.com"
)
