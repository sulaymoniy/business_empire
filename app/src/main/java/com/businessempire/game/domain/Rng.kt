package com.businessempire.game.domain

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Small deterministic RNG (SplitMix64). Its whole state is one Long, which lives in [GameState.rngState],
 * so a saved game always replays the same random sequence. Engine code creates an Rng from the state,
 * draws numbers, then writes [state] back.
 */
class Rng(var state: Long) {
    fun nextLong(): Long {
        state += -0x61c8864680b583ebL // 0x9E3779B97F4A7C15
        var z = state
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L // 0xBF58476D1CE4E5B9
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L // 0x94D049BB133111EB
        return z xor (z ushr 31)
    }

    /** Uniform in [0, 1). */
    fun nextDouble(): Double = (nextLong() ushr 11) * (1.0 / (1L shl 53))

    /** Uniform in [0, bound). */
    fun nextInt(bound: Int): Int {
        require(bound > 0)
        return ((nextLong() ushr 1) % bound).toInt()
    }

    fun range(min: Double, max: Double): Double = min + (max - min) * nextDouble()

    fun chance(probability: Double): Boolean = nextDouble() < probability

    /** Standard normal sample (Box-Muller). */
    fun nextGaussian(): Double {
        val u1 = nextDouble().coerceAtLeast(1e-12)
        val u2 = nextDouble()
        return sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
    }

    fun <T> pick(list: List<T>): T = list[nextInt(list.size)]
}
