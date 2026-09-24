package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundEffectsHelper(context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playStepSound(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 70)
        } catch (_: Exception) {}
        vibrate(30)
    }

    fun playSuccessMilestoneSound(soundEnabled: Boolean) {
        if (!soundEnabled) return
        audioScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 60)
                delay(80)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
            } catch (_: Exception) {}
        }
        vibrate(50)
    }

    fun playCollectSound(soundEnabled: Boolean) {
        if (!soundEnabled) return
        audioScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 80)
                delay(90)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
                delay(100)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
            } catch (_: Exception) {}
        }
        vibrate(80)
    }

    fun playCollisionSound(soundEnabled: Boolean) {
        if (!soundEnabled) return
        audioScope.launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 140)
                delay(120)
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 220)
            } catch (_: Exception) {}
        }
        vibrateDouble(70, 90)
    }

    fun playButtonTap(soundEnabled: Boolean) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (_: Exception) {}
        vibrate(15)
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateDouble(d1: Long, d2: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, d1, 50, d2), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, d1, 50, d2), -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
