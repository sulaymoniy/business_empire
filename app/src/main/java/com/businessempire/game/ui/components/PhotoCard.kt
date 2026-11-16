package com.businessempire.game.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.businessempire.game.ui.art.rememberPhoto

/** Gradient pairs for placeholders, picked by image name so each item keeps its colour. */
private val PlaceholderGradients = listOf(
    Color(0xFF1B6B4F) to Color(0xFF3FA37B),
    Color(0xFF2F6690) to Color(0xFF58B6DE),
    Color(0xFF7A5900) to Color(0xFFF2C14E),
    Color(0xFF6B3FA0) to Color(0xFFB49AE0),
    Color(0xFFB53A2E) to Color(0xFFF29A4A),
    Color(0xFF1D7A73) to Color(0xFF7FD3C8),
    Color(0xFF3D4A57) to Color(0xFF8FA3B5),
)

private val Darken = ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(0.45f, 0.45f, 0.45f, 1f) })

/**
 * A rounded photo for a content item, loaded with Coil from the bundled drawable called [imageName].
 * While the photo is missing it shows a coloured gradient with [placeholderIcon].
 * [locked] items are darkened and blurred, with a lock on top.
 * [scrim] adds a soft dark gradient at the bottom so white [overlay] text stays readable.
 */
@Composable
fun PhotoCard(
    imageName: String?,
    placeholderIcon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    scrim: Boolean = false,
    corner: Dp = 16.dp,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val photo = rememberPhoto(imageName)
    Box(modifier.clip(RoundedCornerShape(corner))) {
        if (photo != 0) {
            AsyncImage(
                model = photo,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                colorFilter = if (locked) Darken else null,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (locked && Build.VERSION.SDK_INT >= 31) Modifier.blur(6.dp) else Modifier),
            )
        } else {
            val (a, b) = PlaceholderGradients[Math.floorMod((imageName ?: "").hashCode(), PlaceholderGradients.size)]
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(if (locked) listOf(a.darker(), b.darker()) else listOf(a, b))),
                contentAlignment = Alignment.Center,
            ) {
                // Locked placeholders show only the lock (drawn below), so the card isn't busy.
                if (!locked) {
                    Icon(
                        placeholderIcon,
                        contentDescription = contentDescription,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxSize(0.38f),
                    )
                }
            }
        }
        if (scrim) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.62f)))),
            )
        }
        if (locked) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp),
            )
        }
        overlay()
    }
}

private fun Color.darker() = copy(red = red * 0.45f, green = green * 0.45f, blue = blue * 0.45f)
