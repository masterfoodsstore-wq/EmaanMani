package com.example.repository

import com.example.model.LiveGameConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Live Game Remote Configuration Engine (Singleton)
 * Immediately updates downloaded client games in real-time when the Administrator adjusts parameters.
 */
object LiveGameConfigManager {

    private val _config = MutableStateFlow(LiveGameConfig())
    val config: StateFlow<LiveGameConfig> = _config.asStateFlow()

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
