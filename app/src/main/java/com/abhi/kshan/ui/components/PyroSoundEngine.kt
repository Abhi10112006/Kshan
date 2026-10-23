package com.abhi.kshan.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural Pyrotechnic Audio & Multi-Stage Haptic Synthesizer.
 * Synthesizes realistic mortar launch whistles, distance-delayed sub-bass shockwaves,
 * and crackle pops without external audio asset bloat.
 */
object PyroSoundEngine {
    private const val TAG = "KshanPyroSound"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private const val SAMPLE_RATE = 22050

    fun playLaunchWhoosh(enabled: Boolean) {
        if (!enabled) return
        scope.launch {
            try {
                val durationMs = 200
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                var phase = 0.0
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val env = sin(progress * Math.PI).toFloat()
                    // Rising mortar whistle + turbine air rush
                    val freq = 240.0 + (progress * 820.0)
                    phase += (2.0 * Math.PI * freq) / SAMPLE_RATE
                    val whistle = sin(phase).toFloat() * 0.42f
                    val airNoise = (Random.nextFloat() * 2f - 1f) * 0.58f
                    val sample = ((whistle + airNoise) * env * 10500f).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    buffer[i] = sample.toShort()
                }
                playPcmBuffer(buffer)
            } catch (e: Exception) {
                Log.w(TAG, "AudioTrack launch whoosh synthesis skipped", e)
            }
        }
    }

    /**
     * Simulates speed-of-sound acoustic propagation delay:
     * Light from the sky flash arrives at t=0ms, while the deep mortar boom & haptic shockwave
     * arrive after 70ms–240ms depending on burst altitude.
     */
    fun scheduleDistanceDelayedBoom(
        context: Context,
        view: View,
        enabled: Boolean,
        burstY: Float,
        canvasHeight: Float,
        scale: Float,
        isCrackleOrCrossette: Boolean
    ) {
        if (!enabled) return
        val normalizedAltitude = (1f - (burstY / canvasHeight.coerceAtLeast(1f))).coerceIn(0.1f, 0.95f)
        val acousticDelayMs = (normalizedAltitude * 210L).toLong().coerceIn(65L, 235L)

        scope.launch {
            delay(acousticDelayMs)
            playExplosionBoomInternal(scale, isCrackleOrCrossette)
            triggerExplosionHaptics(context, view, enabled = true, isHeavy = scale >= 1.0f)
        }
    }

    private fun playExplosionBoomInternal(scale: Float = 1.0f, withCrackle: Boolean = true) {
        try {
            val durationMs = if (withCrackle) 460 else 360
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            var bassPhase = 0.0
            var punchPhase = 0.0
            val amp = (scale.coerceIn(0.6f, 1.45f) * 23000f)

            for (i in 0 until numSamples) {
                val t = i.toFloat() / numSamples
                // Deep chest-thumping sub-bass envelope (85Hz -> 28Hz)
                val boomEnv = exp(-t * 8.2).toFloat()
                val bassFreq = 85.0 * exp(-t * 4.2) + 26.0
                bassPhase += (2.0 * Math.PI * bassFreq) / SAMPLE_RATE
                val subBass = sin(bassPhase).toFloat() * 0.68f

                // Mid-range acoustic chest punch (155Hz -> 48Hz)
                val punchEnv = exp(-t * 15.0).toFloat()
                val punchFreq = 155.0 * exp(-t * 6.0) + 42.0
                punchPhase += (2.0 * Math.PI * punchFreq) / SAMPLE_RATE
                val midPunch = sin(punchPhase).toFloat() * 0.35f * punchEnv

                // Shockwave air roar + delayed crackle stars
                val noiseEnv = exp(-t * 5.2).toFloat()
                val crackleBurst = if (withCrackle && t > 0.16f && Random.nextFloat() > 0.89f) {
                    (Random.nextFloat() * 2f - 1f) * 1.15f
                } else {
                    0f
                }
                val roar = (Random.nextFloat() * 2f - 1f) * 0.42f * noiseEnv

                val mixed = (subBass * boomEnv) + midPunch + roar + (crackleBurst * exp(-t * 2.6).toFloat())
                val sample = (mixed * amp).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[i] = sample.toShort()
            }
            playPcmBuffer(buffer)
        } catch (e: Exception) {
            Log.w(TAG, "AudioTrack explosion boom synthesis skipped", e)
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        val byteSize = buffer.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteSize.coerceAtLeast(2048))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(buffer, 0, buffer.size)
        track.play()
        Thread.sleep(((buffer.size * 1000L) / SAMPLE_RATE) + 40L)
        track.stop()
        track.release()
    }

    fun triggerLaunchHaptic(view: View, enabled: Boolean) {
        if (!enabled) return
        view.post {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (e: Exception) {
                Log.w(TAG, "Launch haptic feedback failed", e)
            }
        }
    }

    fun triggerExplosionHaptics(
        context: Context,
        view: View,
        enabled: Boolean,
        isHeavy: Boolean = true
    ) {
        if (!enabled) return
        view.post {
            try {
                view.performHapticFeedback(
                    if (isHeavy) HapticFeedbackConstants.LONG_PRESS
                    else HapticFeedbackConstants.CLOCK_TICK
                )
            } catch (e: Exception) {
                Log.w(TAG, "View explosion haptic feedback failed", e)
            }
        }

        try {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = if (isHeavy) longArrayOf(0, 42, 32, 24, 40, 14) else longArrayOf(0, 24, 20, 12)
                    val amplitudes = if (isHeavy) intArrayOf(0, 255, 0, 165, 0, 95) else intArrayOf(0, 180, 0, 90)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isHeavy) 45L else 20L)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibrator waveform effect skipped", e)
        }
    }
}
