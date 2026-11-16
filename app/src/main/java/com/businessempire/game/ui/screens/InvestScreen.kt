@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.businessempire.game.ui.screens

import androidx.compose.foundation.layout.aspectRatio
import com.businessempire.game.ui.components.PhotoCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.NewsItem
import com.businessempire.game.domain.PropertyEngine
import com.businessempire.game.domain.StockState
import com.businessempire.game.domain.content.Properties
import com.businessempire.game.domain.content.PropertyDef
import com.businessempire.game.domain.content.StockDef
import com.businessempire.game.domain.content.Stocks
import com.businessempire.game.ui.GameViewModel
import com.businessempire.game.ui.InvestText
import com.businessempire.game.ui.components.LineChart
import com.businessempire.game.ui.components.MaxWidth
import com.businessempire.game.ui.components.SectionTitle
import com.businessempire.game.ui.components.StatRow
import com.businessempire.game.ui.components.rememberGameFeedback
import com.businessempire.game.ui.money
import com.businessempire.game.ui.num
import com.businessempire.game.ui.percent
import com.businessempire.game.ui.theme.GameTheme

@Composable
fun InvestScreen(state: GameState, vm: GameViewModel, onOpenStock: (String) -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    MaxWidth {
        Column(Modifier.fillMaxSize()) {
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                val labels = listOf(R.string.invest_page_stocks, R.string.invest_page_real_estate)
                labels.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = page == index,
                        onClick = { page = index },
                        shape = SegmentedButtonDefaults.itemShape(index, labels.size),
                    ) { Text(stringResource(label)) }
                }
            }
            if (page == 0) StockList(state, onOpenStock) else PropertyList(state, vm)
        }
    }
}

// --- Stocks --------------------------------------------------------------------------------------

@Composable
private fun StockList(state: GameState, onOpenStock: (String) -> Unit) {
    val economy = remember(state) { Economy(state) }
    val holdings = state.market.stocks.values.filter { it.shares > 0 }
    val totalProfit = holdings.sumOf { it.profit }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "portfolio") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.portfolio_value), style = MaterialTheme.typography.labelLarge)
                    Text(money(economy.portfolioValue), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (holdings.isNotEmpty()) {
                        Text(
                            stringResource(R.string.portfolio_profit, signedMoney(totalProfit)),
                            color = if (totalProfit >= 0) GameTheme.colors.gain else GameTheme.colors.loss,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        Text(stringResource(R.string.portfolio_empty), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        if (state.market.news.isNotEmpty()) {
            item(key = "news_title") { SectionTitle(stringResource(R.string.market_news)) }
            items(state.market.news.take(3), key = { "news_${it.stockId}_${it.at}_${it.headline}" }) { NewsRow(it) }
        }
        item(key = "stocks_title") { SectionTitle(stringResource(R.string.market_stocks)) }
        items(Stocks.all, key = { it.id }) { def ->
            val s = state.market.stocks[def.id] ?: return@items
            StockRow(def, s) { onOpenStock(def.id) }
        }
    }
}

@Composable
fun NewsRow(item: NewsItem) {
    val name = stringResource(InvestText.stockName(item.stockId))
    Row(Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Newspaper, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(InvestText.headline(item.headline), name), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            signedPercent(item.change * 100),
            color = if (item.change >= 0) GameTheme.colors.gain else GameTheme.colors.loss,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun StockRow(def: StockDef, s: StockState, onClick: () -> Unit) {
    val first = s.history.firstOrNull() ?: s.price
    val change = roundedChange(s.price, first)
    val up = change >= 0
    val trendColor = if (up) GameTheme.colors.gain else GameTheme.colors.loss
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PhotoCard(
                imageName = def.imageName,
                placeholderIcon = InvestText.sectorIcon(def.sector),
                contentDescription = null,
                corner = 10.dp,
                modifier = Modifier
                    .width(56.dp)
                    .aspectRatio(4f / 3f),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(InvestText.stockName(def.id)), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(
                    "${def.ticker} · ${stringResource(InvestText.sectorName(def.sector))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (s.shares > 0) {
                    Text(
                        stringResource(R.string.shares_owned, num(s.shares)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            LineChart(
                points = s.history.takeLast(60),
                color = trendColor,
                showFill = false,
                strokeWidthDp = 1.5f,
                modifier = Modifier
                    .width(64.dp)
                    .height(32.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(money(s.price), style = MaterialTheme.typography.titleSmall)
                Text(signedPercent(change), color = trendColor, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/** Percent change rounded to one decimal, so colour and sign always agree. */
fun roundedChange(price: Double, from: Double): Double =
    if (from > 0) Math.round((price / from - 1.0) * 1000.0) / 10.0 else 0.0

@Composable
fun signedMoney(value: Double): String = if (value >= 0) "+" + money(value) else money(value)

@Composable
fun signedPercent(value: Double): String {
    val rounded = Math.round(value * 10.0) / 10.0
    return (if (rounded >= 0) "+" else "") + percent(rounded)
}

// --- Real estate ---------------------------------------------------------------------------------

@Composable
private fun PropertyList(state: GameState, vm: GameViewModel) {
    val economy = remember(state) { Economy(state) }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "summary") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    StatRow(stringResource(R.string.real_estate_value), money(economy.propertyValue))
                    StatRow(stringResource(R.string.real_estate_rent), stringResource(R.string.per_minute, money(economy.rentPerSecond * 60)))
                }
            }
        }
        items(Properties.all, key = { it.id }) { def -> PropertyCard(def, state, vm) }
    }
}

@Composable
private fun PropertyCard(def: PropertyDef, state: GameState, vm: GameViewModel) {
    val owned = state.properties[def.id]
    val feedback = rememberGameFeedback()
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PhotoCard(
                    imageName = def.imageName,
                    placeholderIcon = InvestText.propertyIcon(def.id),
                    contentDescription = null,
                    corner = 12.dp,
                    modifier = Modifier
                        .width(104.dp)
                        .aspectRatio(4f / 3f),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(InvestText.propertyName(def.id)), style = MaterialTheme.typography.titleSmall)
                    val rent = if (owned != null) PropertyEngine.rentPerSecond(def, owned) * 60 else def.rentPerMinute
                    Text(
                        stringResource(R.string.rent_per_minute, money(rent)),
                        style = MaterialTheme.typography.bodySmall,
                        color = GameTheme.colors.money,
                    )
                    if (owned != null) {
                        Text(
                            stringResource(R.string.property_level, owned.level, Properties.MAX_LEVEL),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (owned != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.property_value), style = MaterialTheme.typography.labelSmall)
                        Text(money(PropertyEngine.value(def, owned)), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (owned == null) {
                Button(
                    onClick = { vm.buyProperty(def.id); feedback.purchase() },
                    enabled = state.cash >= def.price,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.buy_for, money(def.price))) }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val renovation = PropertyEngine.renovationCost(def, owned)
                    Button(
                        onClick = { vm.renovateProperty(def.id); feedback.purchase() },
                        enabled = renovation != null && state.cash >= renovation,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                    ) {
                        Text(
                            if (renovation == null) stringResource(R.string.renovation_maxed)
                            else stringResource(R.string.renovate_for, money(renovation)),
                            maxLines = 1,
                        )
                    }
                    OutlinedButton(
                        onClick = { vm.sellProperty(def.id); feedback.purchase() },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                    ) { Text(stringResource(R.string.sell), maxLines = 1) }
                }
            }
        }
    }
}
