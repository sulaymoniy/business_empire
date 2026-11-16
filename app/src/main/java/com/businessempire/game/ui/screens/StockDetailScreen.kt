package com.businessempire.game.ui.screens

import com.businessempire.game.ui.components.PhotoCard
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.MarketEngine
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
import com.businessempire.game.ui.theme.GameTheme

@Composable
fun StockDetailScreen(state: GameState, stockId: String, vm: GameViewModel, onBack: () -> Unit) {
    val def = Stocks.byId[stockId] ?: return
    val s = state.market.stocks[stockId] ?: return
    val name = stringResource(InvestText.stockName(stockId))
    val feedback = rememberGameFeedback()
    var quantityText by rememberSaveable { mutableStateOf("1") }
    val quantity = quantityText.toLongOrNull()?.coerceAtLeast(0) ?: 0L
    val first = s.history.firstOrNull() ?: s.price
    val change = roundedChange(s.price, first)
    val trendColor = if (change >= 0) GameTheme.colors.gain else GameTheme.colors.loss

    MaxWidth {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
        ) {
            Row(Modifier.padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                }
                Column {
                    Text(name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${def.ticker} · ${stringResource(InvestText.sectorName(def.sector))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(Modifier.padding(horizontal = 16.dp)) {
                PhotoCard(
                    imageName = def.imageName,
                    placeholderIcon = InvestText.sectorIcon(def.sector),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .padding(bottom = 12.dp),
                )
                Text(money(s.price), style = MaterialTheme.typography.headlineMedium)
                Text(signedPercent(change), color = trendColor, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(12.dp))
                LineChart(
                    points = s.history,
                    color = trendColor,
                    reference = if (s.shares > 0) s.avgCost else null,
                    referenceColor = MaterialTheme.colorScheme.outline,
                    description = stringResource(R.string.cd_price_chart, name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
                if (s.shares > 0) {
                    Text(
                        stringResource(R.string.chart_avg_cost_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.your_position), style = MaterialTheme.typography.titleSmall)
                        StatRow(stringResource(R.string.shares), num(s.shares))
                        StatRow(stringResource(R.string.avg_cost), if (s.shares > 0) money(s.avgCost) else "-")
                        StatRow(stringResource(R.string.position_value), money(s.holdingValue))
                        StatRow(
                            stringResource(R.string.profit_loss),
                            if (s.shares > 0) signedMoney(s.profit) else "-",
                            valueColor = if (s.shares <= 0) null else if (s.profit >= 0) GameTheme.colors.gain else GameTheme.colors.loss,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { text -> quantityText = text.filter { it.isDigit() }.take(15) },
                    label = { Text(stringResource(R.string.quantity)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                    listOf(1L, 10L, 100L).forEach { q ->
                        AssistChip(onClick = { quantityText = q.toString() }, label = { Text(q.toString()) }, modifier = Modifier.heightIn(min = 48.dp))
                    }
                    AssistChip(
                        onClick = { quantityText = MarketEngine.maxBuyable(state, stockId).toString() },
                        label = { Text(stringResource(R.string.max_buy)) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                    AssistChip(
                        onClick = { quantityText = s.shares.toLong().toString() },
                        label = { Text(stringResource(R.string.all_shares)) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cost = s.price * quantity
                    Button(
                        onClick = { vm.buyStock(stockId, quantity); feedback.purchase() },
                        enabled = quantity > 0 && cost <= state.cash,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.buy), fontWeight = FontWeight.SemiBold)
                            Text(money(cost), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                    FilledTonalButton(
                        onClick = { vm.sellStock(stockId, quantity); feedback.purchase() },
                        enabled = quantity > 0 && s.shares >= quantity,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.sell), fontWeight = FontWeight.SemiBold)
                            Text(money(s.price * quantity), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                }
            }
            val news = state.market.news.filter { it.stockId == stockId }
            if (news.isNotEmpty()) {
                SectionTitle(stringResource(R.string.market_news))
                Column(Modifier.padding(horizontal = 12.dp)) { news.forEach { NewsRow(it) } }
            }
            Spacer(Modifier.width(1.dp))
        }
    }
}
