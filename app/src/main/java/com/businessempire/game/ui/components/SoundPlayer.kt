package com.businessempire.game.ui.components

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/** Short sound effects. Each is a few sine notes generated in code, so there are no audio files. */
enum class Sfx(val notes: List<Pair<Double, Int>>, val volume: Float) {
    TAP(listOf(880.0 to 25), 0.12f),
    PURCHASE(listOf(660.0 to 45, 990.0 to 70), 0.18f),
    ACHIEVEMENT(listOf(523.25 to 80, 659.25 to 80, 783.99 to 160), 0.2f),
}

/**
 * Plays [Sfx] with static AudioTracks built on first use. Failures are swallowed: sound is
 * optional and must never crash the game.
 */
class SoundPlayer {
    private val tracks = mutableMapOf<Sfx, AudioTrack>()

    fun play(sfx: Sfx) {
        runCatching {
            val track = tracks.getOrPut(sfx) { build(sfx) }
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
            track.reloadStaticData()
            track.play()
        }
    }

    fun release() {
        tracks.values.forEach { runCatching { it.release() } }
        tracks.clear()
    }

    private fun build(sfx: Sfx): AudioTrack {
        val pcm = synth(sfx)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        track.write(pcm, 0, pcm.size)
        return track
    }

    private fun synth(sfx: Sfx): ShortArray {
        val samples = sfx.notes.sumOf { it.second } * SAMPLE_RATE / 1000
        val out = ShortArray(samples)
        var offset = 0
        for ((freq, ms) in sfx.notes) {
            val n = ms * SAMPLE_RATE / 1000
            for (i in 0 until n) {
                // Quick attack, smooth decay: no clicks.
                val attack = min(1.0, i / (SAMPLE_RATE * 0.004))
                val decay = 1.0 - i.toDouble() / n
                val v = sin(2 * PI * freq * i / SAMPLE_RATE) * attack * decay * sfx.volume
                out[offset + i] = (v * Short.MAX_VALUE).toInt().toShort()
            }
            offset += n
        }
        return out
    }

    private companion object {
        const val SAMPLE_RATE = 22_050
    }
}

val LocalSoundPlayer = staticCompositionLocalOf<SoundPlayer?> { null }
