package com.businessempire.game.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.LuxuryEngine
import com.businessempire.game.domain.content.Businesses
import com.businessempire.game.domain.content.Luxury
import com.businessempire.game.domain.content.LuxuryCategory
import com.businessempire.game.domain.content.Properties
import com.businessempire.game.ui.AssetText
import com.businessempire.game.ui.ContentText
import com.businessempire.game.ui.InvestText
import com.businessempire.game.ui.components.MaxWidth
import com.businessempire.game.ui.components.PhotoCard
import com.businessempire.game.ui.money
import com.businessempire.game.ui.num
import kotlinx.coroutines.delay

/** One picture in the showcase. */
private data class Showpiece(val key: String, val imageName: String, val title: String, val icon: ImageVector)

/**
 * "My Empire": everything the player owns as a visual gallery. Sections stay visible even when empty,
 * with a dashed "not yet" slot, so there is always something to aim for.
 */
@Composable
fun MyEmpireScreen(state: GameState, onBack: () -> Unit) {
    val economy = remember(state) { Economy(state) }
    val sections = buildList {
        val order = listOf(
            LuxuryCategory.HOME to R.string.empire_homes,
            LuxuryCategory.CAR to R.string.empire_garage,
            LuxuryCategory.YACHT to R.string.empire_marina,
            LuxuryCategory.JET to R.string.empire_hangar,
            LuxuryCategory.ISLAND to R.string.empire_islands,
            LuxuryCategory.ART to R.string.empire_art,
            LuxuryCategory.EXTRA to R.string.empire_rare,
        )
        for ((category, title) in order) {
            val owned = Luxury.all.filter { it.category == category && it.id in state.luxury }.map {
                Showpiece(it.id, it.imageName, stringResource(AssetText.luxuryName(it.id)), AssetText.categoryIcon(category))
            }
            add(Triple(title, AssetText.categoryIcon(category), owned))
        }
        add(
            Triple(
                R.string.empire_property,
                Icons.Outlined.Apartment,
                Properties.all.filter { it.id in state.properties }.map {
                    Showpiece(it.id, it.imageName, stringResource(InvestText.propertyName(it.id)), InvestText.propertyIcon(it.id))
                },
            ),
        )
        add(
            Triple(
                R.string.empire_businesses,
                Icons.Outlined.Storefront,
                Businesses.all.filter { state.business(it.id).owned > 0 }.map {
                    Showpiece(it.id, it.imageName(state.business(it.id).owned), stringResource(ContentText.businessName(it.id)), ContentText.businessIcon(it.id))
                },
            ),
        )
    }
    val hero = Luxury.all.filter { it.id in state.luxury }.maxByOrNull { it.price }
    val ownedCount = state.luxury.size + state.properties.size

    MaxWidth {
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp), modifier = Modifier.fillMaxSize()) {
            item(key = "header") { SubPageHeader(stringResource(R.string.empire_title), onBack) }
            item(key = "hero") {
                PhotoCard(
                    imageName = hero?.imageName,
                    placeholderIcon = hero?.let { AssetText.categoryIcon(it.category) } ?: Icons.Outlined.Storefront,
                    contentDescription = null,
                    scrim = true,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                ) {
                    Column(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                    ) {
                        Text(state.profile.companyName, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            stringResource(R.string.empire_summary, ownedCount, money(economy.luxuryValue + economy.propertyValue)),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            stringResource(R.string.empire_bonus, num(LuxuryEngine.bonusPercent(state))),
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            sections.forEachIndexed { index, (title, icon, pieces) ->
                item(key = "section_$index") { ShowcaseSection(stringResource(title), icon, pieces, index) }
            }
        }
    }
}

@Composable
private fun ShowcaseSection(title: String, icon: ImageVector, pieces: List<Showpiece>, index: Int) {
    Column(Modifier.padding(top = 16.dp)) {
        Text(
            if (pieces.isEmpty()) title else stringResource(R.string.empire_section_count, title, pieces.size),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (pieces.isEmpty()) {
                item { EmptySlot(icon) }
            } else {
                items(pieces, key = { it.key }) { piece -> FadeIn(index) { ShowcaseCard(piece) } }
            }
        }
    }
}

@Composable
private fun ShowcaseCard(piece: Showpiece) {
    PhotoCard(
        imageName = piece.imageName,
        placeholderIcon = piece.icon,
        contentDescription = piece.title,
        scrim = true,
        modifier = Modifier
            .width(220.dp)
            .aspectRatio(4f / 3f),
    ) {
        Text(
            piece.title,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
        )
    }
}

@Composable
private fun EmptySlot(icon: ImageVector) {
    OutlinedCard(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(220.dp)
            .aspectRatio(4f / 3f),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
                Text(
                    stringResource(R.string.empire_empty_slot),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

/** Gentle staggered fade/slide-in so the gallery feels alive when opened. */
@Composable
private fun FadeIn(index: Int, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(60L * index)
        progress.animateTo(1f, tween(320))
    }
    Box(
        Modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 24f
        },
    ) { content() }
}
