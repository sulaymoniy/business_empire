@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.businessempire.game.ui.screens

import androidx.compose.foundation.layout.aspectRatio
import com.businessempire.game.ui.components.PhotoCard
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import com.businessempire.game.domain.BuyMode
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.Formulas
import com.businessempire.game.domain.GameEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.content.BusinessDef
import com.businessempire.game.domain.content.Businesses
import com.businessempire.game.domain.content.MilestoneKind
import com.businessempire.game.domain.content.UpgradeDef
import com.businessempire.game.domain.content.UpgradeKind
import com.businessempire.game.ui.ContentText
import com.businessempire.game.ui.GameViewModel
import com.businessempire.game.ui.components.EmptyState
import com.businessempire.game.ui.components.IconBadge
import com.businessempire.game.ui.components.MaxWidth
import com.businessempire.game.ui.components.rememberGameFeedback
import com.businessempire.game.ui.duration
import com.businessempire.game.ui.money
import com.businessempire.game.ui.num
import com.businessempire.game.ui.theme.GameTheme
import kotlinx.coroutines.launch

@Composable
fun BusinessScreen(state: GameState, buyMode: BuyMode, vm: GameViewModel) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val economy = remember(state) { Economy(state) }
    MaxWidth {
        Column(Modifier.fillMaxSize()) {
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                val labels = listOf(R.string.business_page_businesses, R.string.business_page_upgrades)
                labels.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = page == index,
                        onClick = { page = index },
                        shape = SegmentedButtonDefaults.itemShape(index, labels.size),
                    ) { Text(stringResource(label)) }
                }
            }
            if (page == 0) BusinessList(state, economy, buyMode, vm) else UpgradeList(state, vm)
        }
    }
}

@Composable
private fun BusinessList(state: GameState, economy: Economy, buyMode: BuyMode, vm: GameViewModel) {
    val visible = Businesses.all.filter { GameEngine.isUnlocked(state, it.id) }
    val teaser = Businesses.all.getOrNull(visible.size)
    Column(Modifier.fillMaxSize()) {
        BuyModeRow(buyMode, vm::setBuyMode)
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(visible, key = { it.id }) { def ->
                BusinessCard(def, state, economy, buyMode, vm)
            }
            if (teaser != null) {
                item(key = "teaser") { LockedBusinessCard(teaser) }
            }
        }
    }
}

@Composable
private fun BuyModeRow(selected: BuyMode, onSelect: (BuyMode) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.buy_mode_label), style = MaterialTheme.typography.labelLarge)
        BuyMode.entries.forEach { mode ->
            FilterChip(
                selected = mode == selected,
                onClick = { onSelect(mode) },
                label = { Text(stringResource(buyModeLabel(mode))) },
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
    }
}

private fun buyModeLabel(mode: BuyMode) = when (mode) {
    BuyMode.ONE -> R.string.buy_x1
    BuyMode.TEN -> R.string.buy_x10
    BuyMode.HUNDRED -> R.string.buy_x100
    BuyMode.MAX -> R.string.buy_max
}

@Composable
private fun BusinessCard(def: BusinessDef, state: GameState, economy: Economy, buyMode: BuyMode, vm: GameViewModel) {
    val b = state.business(def.id)
    val name = stringResource(ContentText.businessName(def.id))
    val feedback = rememberGameFeedback()
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val cycle = economy.cycleSeconds(def.id)
    val canTap = b.owned > 0 && !b.manager && !b.running

    val onTap = {
        if (b.owned > 0) {
            vm.tapBusiness(def.id)
            feedback.tap()
            scope.launch {
                scale.animateTo(0.94f, tween(60))
                scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
        }
    }
    val runLabel = stringResource(R.string.cd_run_business, name)

    Card(
        onClick = onTap,
        enabled = b.owned > 0,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .semantics { onClick(label = runLabel) { onTap(); true } },
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PhotoCard(
                    imageName = def.imageName(b.owned),
                    placeholderIcon = ContentText.businessIcon(def.id),
                    contentDescription = null,
                    corner = 12.dp,
                    modifier = Modifier
                        .width(96.dp)
                        .aspectRatio(4f / 3f),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(
                            stringResource(R.string.owned_count, b.owned),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    val progress = when {
                        b.owned == 0 -> 0f
                        cycle < 0.25 && (b.running || b.manager) -> 1f
                        else -> (b.progress / cycle).toFloat().coerceIn(0f, 1f)
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row {
                        Text(
                            stringResource(R.string.per_cycle, money(economy.revenuePerCycle(def.id))),
                            style = MaterialTheme.typography.bodySmall,
                            color = GameTheme.colors.money,
                            modifier = Modifier.weight(1f),
                        )
                        val status = when {
                            b.owned == 0 -> stringResource(R.string.status_not_owned)
                            canTap -> stringResource(R.string.status_tap_to_run)
                            else -> duration(cycle - b.progress)
                        }
                        Text(status, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            MilestoneLine(b.owned)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val (count, cost) = GameEngine.buyCost(state, def.id, buyMode)
                Button(
                    onClick = {
                        vm.buyBusiness(def.id)
                        feedback.purchase()
                    },
                    enabled = state.cash >= cost,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.buy_count, count), style = MaterialTheme.typography.labelLarge)
                        Text(money(cost), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
                if (b.owned > 0 && !b.manager) {
                    OutlinedButton(
                        onClick = {
                            vm.hireManager(def.id)
                            feedback.purchase()
                        },
                        enabled = state.cash >= def.managerCost,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.hire_manager), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                            Text(money(def.managerCost), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                } else if (b.manager) {
                    Row(
                        Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Outlined.TaskAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.manager_hired), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneLine(owned: Int) {
    val next = Formulas.nextMilestone(owned) ?: return
    val bonus = when (next.kind) {
        MilestoneKind.SPEED -> stringResource(R.string.milestone_speed, num(next.factor))
        MilestoneKind.REVENUE -> stringResource(R.string.milestone_revenue, num(next.factor))
    }
    Text(
        stringResource(R.string.milestone_next, next.owned, bonus),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
private fun LockedBusinessCard(def: BusinessDef) {
    val previous = Businesses.all.getOrNull(Businesses.indexOf(def.id) - 1)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.Lock, size = 52, modifier = Modifier.alpha(0.6f))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.locked_business_title), style = MaterialTheme.typography.titleMedium)
                if (previous != null) {
                    Text(
                        stringResource(R.string.locked_business_hint, stringResource(ContentText.businessName(previous.id))),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun UpgradeList(state: GameState, vm: GameViewModel) {
    val visible = remember(state.upgrades, state.businesses.mapValues { it.value.owned > 0 }) {
        GameEngine.visibleUpgrades(state)
    }
    val feedback = rememberGameFeedback()
    if (visible.isEmpty()) {
        EmptyState(
            Icons.Outlined.TaskAlt,
            stringResource(R.string.upgrades_empty_title),
            stringResource(R.string.upgrades_empty_body),
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(visible, key = { it.id }) { u ->
            UpgradeRow(u, affordable = state.cash >= u.cost) {
                vm.buyUpgrade(u.id)
                feedback.purchase()
            }
        }
    }
}

@Composable
private fun UpgradeRow(u: UpgradeDef, affordable: Boolean, onBuy: () -> Unit) {
    val title = when (u.kind) {
        UpgradeKind.BUSINESS_REVENUE -> stringResource(
            R.string.upgrade_business,
            stringResource(ContentText.businessName(u.businessId!!)),
            num(u.factor),
        )
        UpgradeKind.GLOBAL_REVENUE -> stringResource(R.string.upgrade_global, num(u.factor))
        UpgradeKind.OFFLINE_CAP -> stringResource(R.string.upgrade_offline, num(u.factor))
    }
    val icon = if (u.businessId != null) ContentText.businessIcon(u.businessId) else Icons.Outlined.TaskAlt
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, size = 40)
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            val buyLabel = stringResource(R.string.cd_buy_for, title, money(u.cost))
            Button(
                onClick = onBuy,
                enabled = affordable,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .semantics { contentDescription = buyLabel },
            ) { Text(money(u.cost), textAlign = TextAlign.Center, maxLines = 1) }
        }
    }
}
