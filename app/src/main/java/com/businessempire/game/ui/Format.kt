package com.businessempire.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import com.businessempire.game.R
import com.businessempire.game.domain.NumberFormatter
import com.businessempire.game.domain.NumberStyle
import kotlin.math.floor

/** The player's chosen number style, provided once at the root. */
val LocalNumberStyle = staticCompositionLocalOf { NumberStyle.SHORT }

@Composable
fun num(value: Double): String = NumberFormatter.format(value, LocalNumberStyle.current)

@Composable
fun money(value: Double): String =
    if (value < 0) stringResource(R.string.money_negative, num(-value))
    else stringResource(R.string.money, num(value))

@Composable
fun perSecond(value: Double): String = stringResource(R.string.per_second, money(value))

@Composable
fun percent(value: Double): String = stringResource(R.string.percent, num(value))

/** "2d 4h", "1h 05m", "4m 09s", "12s", "0.6s". */
@Composable
fun duration(seconds: Double): String {
    val s = seconds.coerceAtLeast(0.0)
    if (s < 10.0 && s != floor(s)) return stringResource(R.string.duration_seconds_decimal, s)
    val total = s.toLong()
    val days = total / 86_400
    val hours = (total % 86_400) / 3_600
    val minutes = (total % 3_600) / 60
    val secs = total % 60
    return when {
        days > 0 -> stringResource(R.string.duration_days_hours, days, hours)
        hours > 0 -> stringResource(R.string.duration_hours_minutes, hours, minutes)
        minutes > 0 -> stringResource(R.string.duration_minutes_seconds, minutes, secs)
        else -> stringResource(R.string.duration_seconds, secs)
    }
}
