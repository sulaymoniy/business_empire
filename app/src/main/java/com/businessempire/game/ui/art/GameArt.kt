package com.businessempire.game.ui.art

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Finds a bundled photo by its image name (see ART_LIST.md), e.g. `lux_day_cruiser` ->
 * `res/drawable-nodpi/lux_day_cruiser.webp`. Returns 0 when the photo hasn't been added yet, and the UI
 * shows a placeholder instead, so the game works with any subset of photos.
 *
 * Photos are looked up by name, so res/raw/keep_photos.xml keeps them through release resource shrinking.
 */
object GameArt {
    private val cache = HashMap<String, Int>()

    @SuppressLint("DiscouragedApi") // Lookup by name is the point: drop in a file, no code change needed.
    @DrawableRes
    fun resolve(context: Context, name: String): Int = synchronized(cache) {
        cache.getOrPut(name) { context.resources.getIdentifier(name, "drawable", context.packageName) }
    }
}

/** Resource id of the photo called [name], or 0 if it isn't bundled. */
@Composable
fun rememberPhoto(name: String?): Int {
    val context = LocalContext.current
    return remember(name) { if (name == null) 0 else GameArt.resolve(context, name) }
}
