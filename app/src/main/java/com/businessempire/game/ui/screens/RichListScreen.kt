package com.businessempire.game.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import com.businessempire.game.data.ImageCredit
import com.businessempire.game.data.ImageCreditsRepository
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.RichList
import com.businessempire.game.domain.RichListEngine
import com.businessempire.game.domain.RichPerson
import com.businessempire.game.domain.content.Comparisons
import com.businessempire.game.domain.content.WealthMilestones
import com.businessempire.game.online.Board
import com.businessempire.game.online.LeaderboardEntry
import com.businessempire.game.online.LeaderboardService
import com.businessempire.game.ui.RichText
import com.businessempire.game.ui.components.InitialsAvatar
import com.businessempire.game.ui.components.MaxWidth
import com.businessempire.game.ui.components.PersonAvatar
import com.businessempire.game.ui.components.SectionTitle
import com.businessempire.game.ui.components.StatRow
import com.businessempire.game.ui.duration
import com.businessempire.game.ui.money
import com.businessempire.game.ui.num
import com.businessempire.game.ui.theme.GameTheme
import kotlin.math.log10

/** The Rich List: real-world milestones, comparisons, the list of people, and the player's records. */
@Composable
fun RichListScreen(state: GameState, richList: RichList, leaderboard: LeaderboardService, onBack: () -> Unit) {
    val netWorth = state.stats.bestNetWorth
    val rank = RichListEngine.playerRank(richList, netWorth)
    val playerIndex = rank - 1
    var runs by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    LaunchedEffect(state.runHistory.size) { runs = leaderboard.entries(Board.PRESTIGE_RUNS) }
    val context = LocalContext.current
    var credits by remember { mutableStateOf<Map<String, ImageCredit>>(emptyMap()) }
    LaunchedEffect(Unit) { credits = ImageCreditsRepository(context.applicationContext).load().associateBy { it.imageName } }
    var selected by remember { mutableStateOf<RichPerson?>(null) }

    MaxWidth {
        Column(Modifier.fillMaxSize()) {
            SubPageHeader(stringResource(R.string.rich_list_title), onBack)
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(key = "you") { StandingCard(state, netWorth, rank, richList.people.size + 1) }
                item(key = "compare") { ComparisonsCard(netWorth) }
                RichListEngine.nextToBeat(richList, netWorth)?.let { next ->
                    item(key = "next") { NextToBeatCard(next, netWorth) }
                }
                item(key = "list_title") { SectionTitle(stringResource(R.string.rich_list_people)) }
                val rows = richList.people.toMutableList<RichPerson?>().apply { add(playerIndex, null) }
                itemsIndexed(rows, key = { i, p -> p?.name?.let { "p_${it}_$i" } ?: "player" }) { i, person ->
                    if (person == null) PlayerRow(state.profile.companyName, netWorth, i + 1)
                    else PersonRow(person, i + 1, passed = person.netWorth <= netWorth, onClick = { selected = person })
                }
                item(key = "records") { RecordsCard(state, runs, leaderboard.isOnline) }
                item(key = "disclaimer") { RichListDisclaimer(richList) }
            }
        }
    }
    selected?.let { person ->
        PersonDetailDialog(person, netWorth, person.imageName?.let { credits[it] }, onDismiss = { selected = null })
    }
}

/** Larger photo (if licensed and bundled) plus facts. Photos of real people appear only in the Rich List. */
@Composable
private fun PersonDetailDialog(person: RichPerson, netWorth: Double, credit: ImageCredit?, onDismiss: () -> Unit) {
    val res = LocalResources.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        title = null,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                PersonAvatar(person, size = 160.dp)
                Spacer(Modifier.height(12.dp))
                Text(person.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(RichText.description(res, person), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.rich_list_worth, money(person.netWorth)), style = MaterialTheme.typography.titleMedium)
                Text(
                    if (person.netWorth > netWorth) stringResource(R.string.rich_list_gap_ahead, money(person.netWorth - netWorth))
                    else stringResource(R.string.rich_list_gap_behind, money(netWorth - person.netWorth)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (credit != null) {
                    Text(
                        stringResource(R.string.rich_list_photo_credit, credit.author, credit.licence),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun StandingCard(state: GameState, netWorth: Double, rank: Int, total: Int) {
    val current = WealthMilestones.current(netWorth)
    val next = WealthMilestones.next(netWorth)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.rich_list_best_net_worth), style = MaterialTheme.typography.labelLarge)
            Text(money(netWorth), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.rich_list_rank, rank, total), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            if (current != null) {
                Text(
                    stringResource(R.string.rich_list_you_are, stringResource(RichText.milestoneName(current.id))),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                val percent = WealthMilestones.all.filter { netWorth >= it.netWorth }.mapNotNull { it.richerThanPercent }.maxOrNull()
                if (percent != null) {
                    Text(stringResource(R.string.rich_list_richer_than, num(percent)), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (next != null) {
                val lo = log10(maxOf(current?.netWorth ?: 1.0, 1.0))
                val hi = log10(next.netWorth)
                val p = ((log10(maxOf(netWorth, 1.0)) - lo) / (hi - lo)).toFloat().coerceIn(0f, 1f)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { p },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
                Text(
                    stringResource(R.string.rich_list_next_milestone, stringResource(RichText.milestoneName(next.id)), money(next.netWorth)),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ComparisonsCard(netWorth: Double) {
    val items = Comparisons.readable(netWorth)
    if (items.isEmpty()) return
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.rich_list_could_buy), style = MaterialTheme.typography.titleSmall)
            items.forEach { (c, n) ->
                val count = n.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                Text(
                    "• " + pluralStringResource(RichText.comparison(c.id), count, num(n.toDouble())),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun NextToBeatCard(person: RichPerson, netWorth: Double, modifier: Modifier = Modifier, allowPhoto: Boolean = true) {
    val res = LocalResources.current
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            PersonAvatar(person, size = 48.dp, allowPhoto = allowPhoto)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rich_list_next_to_beat), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(person.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(RichText.description(res, person), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    stringResource(R.string.rich_list_need_more, money(person.netWorth), money(person.netWorth - netWorth)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun PersonRow(person: RichPerson, position: Int, passed: Boolean, onClick: () -> Unit) {
    val res = LocalResources.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("#$position", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(40.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        PersonAvatar(person, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(person.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                RichText.description(res, person),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(money(person.netWorth), style = MaterialTheme.typography.labelLarge)
            if (passed) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = stringResource(R.string.cd_passed),
                    tint = GameTheme.colors.gain,
                    modifier = Modifier.padding(top = 2.dp).width(16.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayerRow(company: String, netWorth: Double, position: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, GameTheme.colors.money, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("#$position", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
        InitialsAvatar(company, size = 40.dp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.rich_list_you), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(company, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(money(netWorth), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = GameTheme.colors.money)
    }
}

@Composable
private fun RecordsCard(state: GameState, runs: List<LeaderboardEntry>, online: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.records_title), style = MaterialTheme.typography.titleSmall)
            StatRow(stringResource(R.string.records_best_net_worth), money(state.stats.bestNetWorth))
            StatRow(stringResource(R.string.records_runs), state.stats.prestigeCount.toString())
            runs.take(5).forEach { run ->
                StatRow(
                    stringResource(R.string.records_run_line, run.rank, run.label, duration(run.durationSeconds ?: 0.0)),
                    money(run.value),
                )
            }
            if (!online) {
                Text(
                    stringResource(R.string.records_offline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
fun RichListDisclaimer(richList: RichList, modifier: Modifier = Modifier) {
    Row(modifier.padding(top = 8.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.width(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            if (richList.real) stringResource(R.string.rich_list_disclaimer, richList.asOf ?: "-")
            else stringResource(R.string.rich_list_fictional_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
