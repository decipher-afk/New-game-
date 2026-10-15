package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object ArcadeSoundManager {
    var isMuted: Boolean = false
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 22050

    /**
     * Synthesizes and plays a procedural PCM 16-bit mono audio tone or sweep.
     */
    private fun playPcm(
        durationMs: Int,
        startFreq: Float,
        endFreq: Float = startFreq,
        waveform: String = "sine",
        volume: Float = 0.5f
    ) {
        if (isMuted) return

        scope.launch {
            try {
                val numSamples = (SAMPLE_RATE * (durationMs / 1000f)).toInt().coerceAtLeast(1)
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * progress
                    val time = i.toDouble() / SAMPLE_RATE
                    val angle = 2.0 * PI * currentFreq * time

                    // Envelope: linear decay to prevent clicking
                    val envelope = (1f - progress) * volume

                    val sampleValue = when (waveform) {
                        "square" -> if (sin(angle) >= 0) 1.0 else -1.0
                        "noise" -> (Random.nextDouble() * 2.0 - 1.0)
                        else -> sin(angle) // sine
                    }

                    buffer[i] = (sampleValue * envelope * Short.MAX_VALUE).toInt().toShort()
                }

                val audioTrack = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                // Clean up track after completion
                launch {
                    kotlinx.coroutines.delay(durationMs.toLong() + 50)
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
                // Graceful fallback if device audio pipeline is busy
            }
        }
    }

    // ==========================================
    // CYBER PONG SOUNDS
    // ==========================================
    fun playPongPaddleHit() {
        playPcm(durationMs = 60, startFreq = 480f, endFreq = 720f, waveform = "square", volume = 0.4f)
    }

    fun playPongWallHit() {
        playPcm(durationMs = 40, startFreq = 300f, endFreq = 260f, waveform = "sine", volume = 0.3f)
    }

    fun playPongGoalScored() {
        scope.launch {
            playPcm(durationMs = 120, startFreq = 523f, endFreq = 659f, waveform = "sine", volume = 0.5f)
            kotlinx.coroutines.delay(100)
            playPcm(durationMs = 180, startFreq = 784f, endFreq = 1046f, waveform = "sine", volume = 0.6f)
        }
    }

    // ==========================================
    // RETRO SNAKE ARENA SOUNDS
    // ==========================================
    fun playSnakeEatPellet() {
        scope.launch {
            playPcm(durationMs = 50, startFreq = 659f, endFreq = 880f, waveform = "sine", volume = 0.45f)
            kotlinx.coroutines.delay(45)
            playPcm(durationMs = 90, startFreq = 1174f, endFreq = 1318f, waveform = "sine", volume = 0.5f)
        }
    }

    fun playSnakeBoost() {
        playPcm(durationMs = 90, startFreq = 180f, endFreq = 320f, waveform = "square", volume = 0.35f)
    }

    fun playSnakeCrash() {
        playPcm(durationMs = 250, startFreq = 220f, endFreq = 70f, waveform = "noise", volume = 0.6f)
    }

    // ==========================================
    // SPACE BLASTER COMBAT SOUNDS
    // ==========================================
    fun playSpaceLaser() {
        playPcm(durationMs = 85, startFreq = 1100f, endFreq = 220f, waveform = "square", volume = 0.4f)
    }

    fun playMeteorExplosion() {
        scope.launch {
            playPcm(durationMs = 180, startFreq = 180f, endFreq = 50f, waveform = "noise", volume = 0.65f)
            playPcm(durationMs = 100, startFreq = 140f, endFreq = 60f, waveform = "square", volume = 0.4f)
        }
    }

    fun playShipDamaged() {
        playPcm(durationMs = 150, startFreq = 350f, endFreq = 120f, waveform = "square", volume = 0.55f)
    }

    // ==========================================
    // TAP BLITZ DUEL SOUNDS
    // ==========================================
    fun playReflexTap(streak: Int) {
        val baseFreq = (440f + (streak * 65f)).coerceAtMost(1600f)
        playPcm(durationMs = 60, startFreq = baseFreq, endFreq = baseFreq + 80f, waveform = "sine", volume = 0.45f)
    }

    fun playReflexMiss() {
        playPcm(durationMs = 120, startFreq = 220f, endFreq = 110f, waveform = "square", volume = 0.4f)
    }

    // ==========================================
    // UI & GENERAL REWARD SOUNDS
    // ==========================================
    fun playVictoryFanfare() {
        scope.launch {
            val notes = listOf(523f, 659f, 784f, 1046f)
            notes.forEach { note ->
                playPcm(durationMs = 90, startFreq = note, endFreq = note, waveform = "sine", volume = 0.5f)
                kotlinx.coroutines.delay(80)
            }
        }
    }

    fun playCheerSound() {
        scope.launch {
            playPcm(durationMs = 100, startFreq = 880f, endFreq = 1200f, waveform = "sine", volume = 0.4f)
            kotlinx.coroutines.delay(60)
            playPcm(durationMs = 120, startFreq = 1046f, endFreq = 1400f, waveform = "sine", volume = 0.5f)
        }
    }

    fun playButtonClick() {
        playPcm(durationMs = 30, startFreq = 600f, endFreq = 800f, waveform = "sine", volume = 0.25f)
    }
}
