package com.businessempire.game.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** Player-facing feedback toggles (haptics, sound), provided once at the root from Settings. */
data class FeedbackSettings(val haptics: Boolean = true, val sound: Boolean = true)

val LocalFeedbackSettings = compositionLocalOf { FeedbackSettings() }

/** Haptics + sound for player actions. Both respect the player's settings. */
class GameFeedback(
    private val tapHaptic: () -> Unit,
    private val confirmHaptic: () -> Unit,
    private val sound: (Sfx) -> Unit,
) {
    fun tap() {
        tapHaptic()
        sound(Sfx.TAP)
    }

    fun purchase() {
        confirmHaptic()
        sound(Sfx.PURCHASE)
    }

    fun celebrate() {
        confirmHaptic()
        sound(Sfx.ACHIEVEMENT)
    }
}

@Composable
fun rememberGameFeedback(): GameFeedback {
    val haptics = LocalHapticFeedback.current
    val settings = LocalFeedbackSettings.current
    val player = LocalSoundPlayer.current
    return remember(haptics, settings, player) {
        GameFeedback(
            tapHaptic = { if (settings.haptics) haptics.performHapticFeedback(HapticFeedbackType.KeyboardTap) },
            confirmHaptic = { if (settings.haptics) haptics.performHapticFeedback(HapticFeedbackType.Confirm) },
            sound = { if (settings.sound) player?.play(it) },
        )
    }
}
