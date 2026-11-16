package com.businessempire.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.LuxuryEngine
import com.businessempire.game.domain.content.Luxury
import com.businessempire.game.domain.content.LuxuryCategory
import com.businessempire.game.domain.content.LuxuryDef
import com.businessempire.game.domain.content.Ranks
import com.businessempire.game.ui.AssetText
import com.businessempire.game.ui.GameViewModel
import com.businessempire.game.ui.components.MaxWidth
import com.businessempire.game.ui.components.PhotoCard
import com.businessempire.game.ui.components.SectionTitle
import com.businessempire.game.ui.components.StatRow
import com.businessempire.game.ui.components.rememberGameFeedback
import com.businessempire.game.ui.money
import com.businessempire.game.ui.num
import com.businessempire.game.ui.theme.GameTheme
import kotlin.math.log10

@Composable
fun AssetsScreen(state: GameState, vm: GameViewModel, onOpenEmpire: () -> Unit) {
    val economy = remember(state) { Economy(state) }
    MaxWidth {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "empire", span = { GridItemSpan(maxLineSpan) }) { MyEmpireBanner(state, onOpenEmpire) }
            item(key = "rank", span = { GridItemSpan(maxLineSpan) }) { RankCard(state.stats.bestNetWorth) }
            item(key = "networth", span = { GridItemSpan(maxLineSpan) }) { NetWorthCard(state, economy) }
            item(key = "lux_title", span = { GridItemSpan(maxLineSpan) }) {
                SectionTitle(
                    stringResource(R.string.luxury_title, num(LuxuryEngine.bonusPercent(state))),
                    Modifier.padding(top = 8.dp),
                )
            }
            LuxuryCategory.entries.forEach { category ->
                val items = Luxury.all.filter { it.category == category }
                if (items.isEmpty()) return@forEach
                item(key = "cat_$category", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        stringResource(AssetText.categoryName(category)),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                    )
                }
                items(items, key = { it.id }) { def -> LuxuryCard(def, state, vm) }
            }
        }
    }
}

@Composable
private fun MyEmpireBanner(state: GameState, onOpen: () -> Unit) {
    val owned = state.luxury.size + state.properties.size
    Card(
        onClick = onOpen,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Collections, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.empire_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.empire_banner, owned), style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/** Big picture card: photo on top (darkened + lock until owned), then price, bonus and the buy button. */
@Composable
private fun LuxuryCard(def: LuxuryDef, state: GameState, vm: GameViewModel) {
    val owned = def.id in state.luxury
    val feedback = rememberGameFeedback()
    val name = stringResource(AssetText.luxuryName(def.id))
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (owned) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        PhotoCard(
            imageName = def.imageName,
            placeholderIcon = AssetText.categoryIcon(def.category),
            contentDescription = name,
            locked = !owned,
            scrim = true,
            corner = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
        ) {
            Text(
                name,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
            )
            if (owned) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = stringResource(R.string.owned),
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                )
            }
        }
        Column(Modifier.padding(10.dp)) {
            Text(money(def.price), style = MaterialTheme.typography.titleSmall, color = GameTheme.colors.money, maxLines = 1)
            Text(
                stringResource(R.string.luxury_bonus, num(def.bonusPercent)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            if (owned) {
                Text(stringResource(R.string.owned), style = MaterialTheme.typography.labelLarge, modifier = Modifier.heightIn(min = 40.dp).padding(top = 10.dp))
            } else {
                Button(
                    onClick = { vm.buyLuxury(def.id); feedback.purchase() },
                    enabled = state.cash >= def.price,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.buy), maxLines = 1) }
            }
        }
    }
}

@Composable
private fun RankCard(bestNetWorth: Double) {
    val rank = Ranks.forNetWorth(bestNetWorth)
    val next = Ranks.next(bestNetWorth)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.rank_label), style = MaterialTheme.typography.labelLarge)
            Text(stringResource(AssetText.rankName(rank.id)), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (next != null) {
                // Progress on a log scale, so each rank feels like an equal step.
                val lo = log10(maxOf(rank.minNetWorth, 1.0))
                val hi = log10(next.minNetWorth)
                val now = log10(maxOf(bestNetWorth, 1.0))
                val progress = ((now - lo) / (hi - lo)).toFloat().coerceIn(0f, 1f)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
                Text(
                    stringResource(R.string.rank_next, stringResource(AssetText.rankName(next.id)), money(next.minNetWorth)),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            } else {
                Text(stringResource(R.string.rank_top), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun NetWorthCard(state: GameState, economy: Economy) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.net_worth), style = MaterialTheme.typography.labelLarge)
            Text(money(economy.netWorth), style = MaterialTheme.typography.headlineSmall, color = GameTheme.colors.money, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            StatRow(stringResource(R.string.nw_cash), money(state.cash))
            StatRow(stringResource(R.string.nw_businesses), money(economy.businessValue))
            StatRow(stringResource(R.string.nw_properties), money(economy.propertyValue))
            StatRow(stringResource(R.string.nw_stocks), money(economy.portfolioValue))
            StatRow(stringResource(R.string.nw_luxury), money(economy.luxuryValue))
            StatRow(stringResource(R.string.nw_savings), money(state.bank.savings))
            if (state.bank.loan > 0) {
                StatRow(stringResource(R.string.nw_loans), money(-state.bank.loan), valueColor = GameTheme.colors.loss)
            }
        }
    }
}
