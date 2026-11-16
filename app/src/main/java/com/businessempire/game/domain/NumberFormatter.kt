package com.businessempire.game.domain

import kotlinx.serialization.Serializable
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Serializable
enum class NumberStyle { SHORT, SCIENTIFIC }

/**
 * Formats big game numbers: 950, 1.23K, 45.6M, 789B, 1.00T, then Qa, Qi, Sx, Sp, Oc, No, Dc,
 * then two-letter suffixes aa, ab, ... az, ba, ...
 */
object NumberFormatter {
    private val NAMED = listOf("", "K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc")

    fun format(value: Double, style: NumberStyle = NumberStyle.SHORT): String = when (style) {
        NumberStyle.SHORT -> short(value)
        NumberStyle.SCIENTIFIC -> scientific(value)
    }

    fun short(value: Double): String {
        if (value.isNaN()) return "0"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        val sign = if (value < 0) "-" else ""
        val a = abs(value)
        if (a < 1000.0) return sign + small(a)

        var group = floor(log10(a) / 3.0).toInt()
        var mantissa = a / 1000.0.pow(group)
        // Rounding can push 999.996K to "1000K": move up a group instead.
        if (mantissa >= 999.995) {
            group += 1
            mantissa /= 1000.0
        }
        return sign + threeDigits(mantissa) + suffix(group)
    }

    fun scientific(value: Double): String {
        if (value.isNaN()) return "0"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        val sign = if (value < 0) "-" else ""
        val a = abs(value)
        if (a < 1000.0) return sign + small(a)
        var exp = floor(log10(a)).toInt()
        var mantissa = a / 10.0.pow(exp)
        if (mantissa >= 9.995) {
            exp += 1
            mantissa /= 10.0
        }
        return sign + String.format(Locale.ROOT, "%.2f", mantissa) + "e" + exp
    }

    /** Suffix for a 1000^group magnitude. Group 0 = none, 1 = K, ... 11 = Dc, 12 = aa, 13 = ab ... */
    fun suffix(group: Int): String {
        if (group < NAMED.size) return NAMED[group]
        val idx = group - NAMED.size
        val first = 'a' + (idx / 26) % 26
        val second = 'a' + idx % 26
        return "$first$second"
    }

    private fun small(a: Double): String {
        if (a == floor(a)) return String.format(Locale.ROOT, "%.0f", a)
        val text = String.format(Locale.ROOT, "%.2f", a)
        return text.trimEnd('0').trimEnd('.')
    }

    private fun threeDigits(m: Double): String = when {
        m < 10.0 -> String.format(Locale.ROOT, "%.2f", m)
        m < 100.0 -> String.format(Locale.ROOT, "%.1f", m)
        else -> String.format(Locale.ROOT, "%.0f", m)
    }
}
