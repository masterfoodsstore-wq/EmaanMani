package com.example.model

import java.io.File

/**
 * Release and update models for the GitHub Release compatible update engine.
 */
data class GameRelease(
    val versionName: String, // e.g. "v1.0.0", "v1.1.0", "v1.2.0"
    val versionCode: Int = 1,
    val releaseDate: String = "",
    val title: String = "",
    val whatsNew: List<String> = emptyList(),
    val bugFixes: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val downloadUrl: String = "",
    val assetSizeMb: Double = 0.0,
    val sha256Checksum: String = "",
    val isPublished: Boolean = true,
    val apkFileName: String = "",
    val releaseNotesRaw: String = ""
)

sealed class UpdateCheckResult {
    data class UpdateAvailable(val currentVersion: String, val latestRelease: GameRelease) : UpdateCheckResult()
    data class UpToDate(val currentVersion: String) : UpdateCheckResult()
    data class OfflineError(val currentVersion: String, val message: String) : UpdateCheckResult()
    data class Error(val currentVersion: String, val message: String) : UpdateCheckResult()
}

sealed class UpdateProgressState {
    object Idle : UpdateProgressState()
    data class Downloading(val progressPercent: Int, val downloadedMb: Double, val totalMb: Double) : UpdateProgressState()
    data class Verifying(val status: String) : UpdateProgressState()
    data class ReadyToInstall(val apkFile: File, val release: GameRelease) : UpdateProgressState()
    data class ReadyToRestart(val newVersion: GameRelease) : UpdateProgressState()
    data class PermissionNeeded(val permissionType: String) : UpdateProgressState()
    data class Failed(val error: String) : UpdateProgressState()
}
