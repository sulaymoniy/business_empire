package com.businessempire.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.businessempire.game.domain.RichListEngine
import com.businessempire.game.domain.RichPerson
import com.businessempire.game.ui.art.rememberPhoto

private val AvatarColors = listOf(
    Color(0xFF1B6B4F), Color(0xFF2F6690), Color(0xFF8E6CC0), Color(0xFFC0563F),
    Color(0xFF7A5900), Color(0xFF3D6373), Color(0xFFB0446E), Color(0xFF4F7A28),
)

/**
 * Rich List avatar: the person's licensed photo if one is bundled, otherwise a coloured circle with initials.
 *
 * Photos of real people may only appear inside the Rich List screens (never in notifications, the icon or
 * promo material), so callers outside those screens pass [allowPhoto] = false.
 */
@Composable
fun PersonAvatar(person: RichPerson, size: Dp = 44.dp, modifier: Modifier = Modifier, allowPhoto: Boolean = true) {
    val photo = rememberPhoto(if (allowPhoto) person.imageName else null)
    if (photo != 0) {
        AsyncImage(
            model = photo,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
        )
        return
    }
    InitialsAvatar(person.name, size, modifier)
}

@Composable
fun InitialsAvatar(name: String, size: Dp = 44.dp, modifier: Modifier = Modifier, color: Color? = null) {
    val bg = color ?: AvatarColors[Math.floorMod(name.hashCode(), AvatarColors.size)]
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            RichListEngine.initials(name),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
