package com.example.repository

import com.example.model.LiveGameConfig
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Live Game Remote Configuration Engine (Singleton)
 * Immediately updates downloaded client games in real-time when the Administrator adjusts parameters
 * via Cloud Firestore real-time listeners.
 */
object LiveGameConfigManager {

    private val _config = MutableStateFlow(LiveGameConfig())
    val config: StateFlow<LiveGameConfig> = _config.asStateFlow()

    private var firestoreRegistration: ListenerRegistration? = null

    /**
     * Attaches real-time Cloud Firestore listener to receive instantaneous OTA updates
     * without requiring an APK re-install or update.
     */
    fun startRealtimeSync() {
        if (firestoreRegistration != null) return
        val db = FirebaseManager.firestore ?: return

        try {
            firestoreRegistration = db.collection("app_config")
                .document("live_game_config")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val isGlobalMaintenance = snapshot.getBoolean("isGlobalMaintenance") ?: false
                        val maintenanceTitle = snapshot.getString("maintenanceTitle") ?: "SYSTEM SERVER UPDATE IN PROGRESS"
                        val maintenanceMessage = snapshot.getString("maintenanceMessage") ?: ""
                        val isDragonTigerOnline = snapshot.getBoolean("isDragonTigerOnline") ?: true
                        val isZooRouletteOnline = snapshot.getBoolean("isZooRouletteOnline") ?: true
                        val isCyberSlotsOnline = snapshot.getBoolean("isCyberSlotsOnline") ?: false
                        val houseCommissionPercent = snapshot.getDouble("houseCommissionPercent") ?: 3.0
                        val dragonCountdownSeconds = snapshot.getLong("dragonCountdownSeconds")?.toInt() ?: 15
                        val zooCountdownSeconds = snapshot.getLong("zooCountdownSeconds")?.toInt() ?: 18
                        val minBetLimit = snapshot.getLong("minBetLimit") ?: 10L
                        val maxBetLimit = snapshot.getLong("maxBetLimit") ?: 50000L
                        val isBroadcastActive = snapshot.getBoolean("isBroadcastActive") ?: true
                        val liveBroadcastMessage = snapshot.getString("liveBroadcastMessage") ?: ""
                        val liveAppVersion = snapshot.getString("liveAppVersion") ?: "v1.0.0"
                        val onlinePlayerCount = snapshot.getLong("onlinePlayerCount")?.toInt() ?: 2418
                        val lastAdminEditor = snapshot.getString("lastAdminEditor") ?: "admin@royalx.com"

                        _config.update {
                            it.copy(
                                isGlobalMaintenance = isGlobalMaintenance,
                                maintenanceTitle = maintenanceTitle,
                                maintenanceMessage = maintenanceMessage,
                                isDragonTigerOnline = isDragonTigerOnline,
                                isZooRouletteOnline = isZooRouletteOnline,
                                isCyberSlotsOnline = isCyberSlotsOnline,
                                houseCommissionPercent = houseCommissionPercent,
                                dragonCountdownSeconds = dragonCountdownSeconds,
                                zooCountdownSeconds = zooCountdownSeconds,
                                minBetLimit = minBetLimit,
                                maxBetLimit = maxBetLimit,
                                isBroadcastActive = isBroadcastActive,
                                liveBroadcastMessage = liveBroadcastMessage,
                                liveAppVersion = liveAppVersion,
                                onlinePlayerCount = onlinePlayerCount,
                                lastAdminEditor = lastAdminEditor,
                                lastUpdatedTimestamp = System.currentTimeMillis()
                            )
                        }
                    }
                }
        } catch (_: Exception) {}
    }

    private fun pushToFirestoreIfAvailable(config: LiveGameConfig) {
        val db = FirebaseManager.firestore ?: return
        try {
            val map = hashMapOf(
                "isGlobalMaintenance" to config.isGlobalMaintenance,
                "maintenanceTitle" to config.maintenanceTitle,
                "maintenanceMessage" to config.maintenanceMessage,
                "maintenanceEtaMinutes" to config.maintenanceEtaMinutes,
                "isDragonTigerOnline" to config.isDragonTigerOnline,
                "isZooRouletteOnline" to config.isZooRouletteOnline,
                "isCyberSlotsOnline" to config.isCyberSlotsOnline,
                "houseCommissionPercent" to config.houseCommissionPercent,
                "dragonCountdownSeconds" to config.dragonCountdownSeconds,
                "zooCountdownSeconds" to config.zooCountdownSeconds,
                "minBetLimit" to config.minBetLimit,
                "maxBetLimit" to config.maxBetLimit,
                "isBroadcastActive" to config.isBroadcastActive,
                "liveBroadcastMessage" to config.liveBroadcastMessage,
                "liveAppVersion" to config.liveAppVersion,
                "onlinePlayerCount" to config.onlinePlayerCount,
                "lastAdminEditor" to config.lastAdminEditor,
                "lastUpdatedTimestamp" to System.currentTimeMillis()
            )
            db.collection("app_config").document("live_game_config").set(map)
        } catch (_: Exception) {}
    }

    fun updateGlobalMaintenance(
        isMaintenance: Boolean,
        message: String = _config.value.maintenanceMessage,
        etaMinutes: Int = _config.value.maintenanceEtaMinutes,
        admin: String = "admin@system.com"
    ) {
        _config.update {
            it.copy(
                isGlobalMaintenance = isMaintenance,
                maintenanceMessage = message,
                maintenanceEtaMinutes = etaMinutes,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                lastAdminEditor = admin
            )
        }
        pushToFirestoreIfAvailable(_config.value)
    }

    fun updateGameOnlineStatus(
        dragonTiger: Boolean,
        zooRoulette: Boolean,
        cyberSlots: Boolean,
        admin: String = "admin@system.com"
    ) {
        _config.update {
            it.copy(
                isDragonTigerOnline = dragonTiger,
                isZooRouletteOnline = zooRoulette,
                isCyberSlotsOnline = cyberSlots,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                lastAdminEditor = admin
            )
        }
        pushToFirestoreIfAvailable(_config.value)
    }

    fun updateBroadcastMessage(
        message: String,
        isActive: Boolean = true,
        admin: String = "admin@system.com"
    ) {
        _config.update {
            it.copy(
                liveBroadcastMessage = message.trim(),
                isBroadcastActive = isActive,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                lastAdminEditor = admin
            )
        }
        pushToFirestoreIfAvailable(_config.value)
    }

    fun updateGameEconomy(
        commissionPercent: Double,
        dragonCountdownSec: Int,
        zooCountdownSec: Int,
        minBet: Long,
        maxBet: Long,
        admin: String = "admin@system.com"
    ) {
        _config.update {
            it.copy(
                houseCommissionPercent = commissionPercent.coerceIn(0.0, 15.0),
                dragonCountdownSeconds = dragonCountdownSec.coerceIn(5, 60),
                zooCountdownSeconds = zooCountdownSec.coerceIn(5, 60),
                minBetLimit = minBet.coerceAtLeast(1L),
                maxBetLimit = maxBet.coerceAtLeast(100L),
                lastUpdatedTimestamp = System.currentTimeMillis(),
                lastAdminEditor = admin
            )
        }
    }

    fun updateMultiplierEvent(
        isActive: Boolean,
        multiplierRate: Double,
        title: String,
        admin: String = "admin@system.com"
    ) {
        _config.update {
            it.copy(
                isMultiplierEventActive = isActive,
                multiplierRate = multiplierRate.coerceAtLeast(1.0),
                multiplierTitle = title,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                lastAdminEditor = admin
            )
        }
    }

    fun pushOtaLiveUpdate(
        version: String,
        patchNotes: String,
        admin: String = "admin@system.com"
    ) {
        _config.update {
            it.copy(
                liveAppVersion = version,
                liveBroadcastMessage = "⚡ Live Hot-Update $version Applied: $patchNotes",
                lastUpdatedTimestamp = System.currentTimeMillis(),
                lastAdminEditor = admin
            )
        }
    }
}
