package com.businessempire.game.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.businessempire.game.BuildConfig
import com.businessempire.game.R
import com.businessempire.game.domain.AchievementEngine
import com.businessempire.game.domain.Economy
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.PrestigeEngine
import com.businessempire.game.domain.RichListEngine
import com.businessempire.game.domain.content.Achievements
import com.businessempire.game.domain.content.PrestigeRules
import com.businessempire.game.domain.content.Ranks
import com.businessempire.game.ui.AssetText
import com.businessempire.game.ui.GameViewModel
import com.businessempire.game.ui.LocalNumberStyle
import com.businessempire.game.ui.MetaText
import com.businessempire.game.ui.components.MaxWidth
import com.businessempire.game.ui.components.StatRow
import com.businessempire.game.ui.components.rememberGameFeedback
import com.businessempire.game.ui.duration
import com.businessempire.game.ui.money
import com.businessempire.game.ui.num
import com.businessempire.game.ui.percent
import com.businessempire.game.ui.theme.GameTheme

/** Where the Profile hub can navigate. */
enum class ProfileDestination { RICH_LIST, ACHIEVEMENTS, STATISTICS, SETTINGS, PRESTIGE, SHOP, CHEATS }

// --- First launch ----------------------------------------------------------------------------------

@Composable
fun OnboardingScreen(onStart: (ceo: String, company: String) -> Unit) {
    var ceo by rememberSaveable { mutableStateOf("") }
    var company by rememberSaveable { mutableStateOf("") }
    val ready = ceo.isNotBlank() && company.isNotBlank()
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier
                    .widthIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(112.dp)) {
                    Icon(
                        painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                    )
                }
                Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(
                    stringResource(R.string.onboarding_body),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = ceo,
                    onValueChange = { ceo = it.take(24) },
                    label = { Text(stringResource(R.string.onboarding_ceo)) },
                    placeholder = { Text(stringResource(R.string.onboarding_ceo_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it.take(32) },
                    label = { Text(stringResource(R.string.onboarding_company)) },
                    placeholder = { Text(stringResource(R.string.onboarding_company_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { onStart(ceo, company) },
                    enabled = ready,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                ) { Text(stringResource(R.string.onboarding_start)) }
            }
        }
    }
}

// --- Profile hub ---------------------------------------------------------------------------------

@Composable
fun ProfileScreen(state: GameState, vm: GameViewModel, onOpen: (ProfileDestination) -> Unit) {
    val economy = remember(state) { Economy(state) }
    val rank = Ranks.forNetWorth(state.stats.bestNetWorth)
    val feedback = rememberGameFeedback()
    MaxWidth {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(state.profile.companyName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.profile_ceo, state.profile.ceoName), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.profile_rank, stringResource(AssetText.rankName(rank.id))),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(R.string.header_net_worth, money(economy.netWorth)), style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Daily reward
            val canClaim = vm.canClaimDaily(state)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CardGiftcard, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.daily_title), style = MaterialTheme.typography.titleMedium)
                        if (canClaim) {
                            val (streak, reward) = vm.dailyPreview(state)
                            Text(stringResource(R.string.daily_ready, streak, money(reward)), style = MaterialTheme.typography.bodyMedium)
                        } else {
                            Text(pluralStringResource(R.plurals.daily_claimed, state.daily.streak, state.daily.streak), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Button(
                        onClick = { vm.claimDaily(); feedback.purchase() },
                        enabled = canClaim,
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text(stringResource(R.string.daily_claim)) }
                }
            }

            // Rich List teaser
            val richList by vm.richList.collectAsStateWithLifecycle()
            RichListEngine.nextToBeat(richList, state.stats.bestNetWorth)?.let { next ->
                Box(Modifier.clickable { onOpen(ProfileDestination.RICH_LIST) }) {
                    NextToBeatCard(next, state.stats.bestNetWorth, allowPhoto = false)
                }
            }

            // Prestige summary
            Card(
                onClick = { onOpen(ProfileDestination.PRESTIGE) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = GameTheme.colors.money, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.prestige_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(
                                R.string.prestige_summary,
                                num(state.legacyPoints),
                                percent(state.legacyPoints * PrestigeRules.BONUS_PER_POINT * 100),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), modifier = Modifier.fillMaxWidth()) {
                MenuRow(
                    Icons.Outlined.Leaderboard,
                    stringResource(R.string.rich_list_title),
                    stringResource(
                        R.string.rich_list_menu_subtitle,
                        RichListEngine.playerRank(richList, state.stats.bestNetWorth),
                        richList.people.size + 1,
                    ),
                ) { onOpen(ProfileDestination.RICH_LIST) }
                HorizontalDivider()
                MenuRow(
                    Icons.Outlined.EmojiEvents,
                    stringResource(R.string.achievements_title),
                    stringResource(R.string.achievements_count, state.achievements.size, Achievements.all.size),
                ) { onOpen(ProfileDestination.ACHIEVEMENTS) }
                HorizontalDivider()
                MenuRow(Icons.Outlined.BarChart, stringResource(R.string.stats_title)) { onOpen(ProfileDestination.STATISTICS) }
                HorizontalDivider()
                MenuRow(Icons.Outlined.Settings, stringResource(R.string.settings_title)) { onOpen(ProfileDestination.SETTINGS) }
                // MONETIZATION: the Shop entry only appears once BuildConfig.STORE_ENABLED is true.
                if (BuildConfig.STORE_ENABLED) {
                    HorizontalDivider()
                    MenuRow(Icons.Outlined.ShoppingCart, stringResource(R.string.shop_title)) { onOpen(ProfileDestination.SHOP) }
                }
                if (BuildConfig.DEBUG) {
                    HorizontalDivider()
                    MenuRow(Icons.Outlined.BugReport, stringResource(R.string.cheats_title), stringResource(R.string.cheats_subtitle)) {
                        onOpen(ProfileDestination.CHEATS)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick),
    )
}

/** Shared top bar for Profile sub-pages. */
@Composable
fun SubPageHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cd_back))
        }
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

// --- Prestige --------------------------------------------------------------------------------------

@Composable
fun PrestigeScreen(state: GameState, vm: GameViewModel, onBack: () -> Unit) {
    val economy = remember(state) { Economy(state) }
    val available = PrestigeEngine.pointsAvailable(state)
    val canPrestige = PrestigeEngine.canPrestige(state)
    var confirm by remember { mutableStateOf(false) }
    MaxWidth {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SubPageHeader(stringResource(R.string.prestige_title), onBack)
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.prestige_explain, percent(PrestigeRules.BONUS_PER_POINT * 100)), style = MaterialTheme.typography.bodyLarge)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        StatRow(stringResource(R.string.prestige_points_now), num(state.legacyPoints))
                        StatRow(stringResource(R.string.prestige_bonus_now), "+" + percent(state.legacyPoints * PrestigeRules.BONUS_PER_POINT * 100))
                        StatRow(stringResource(R.string.prestige_points_gain), "+" + num(available), valueColor = GameTheme.colors.gain)
                        StatRow(stringResource(R.string.prestige_count), state.stats.prestigeCount.toString())
                    }
                }
                val progress = (economy.netWorth / PrestigeRules.UNLOCK_NET_WORTH).toFloat().coerceIn(0f, 1f)
                Text(
                    stringResource(R.string.prestige_requirement, money(PrestigeRules.UNLOCK_NET_WORTH), money(economy.netWorth)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                )
                Text(stringResource(R.string.prestige_keeps), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.prestige_resets), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = { confirm = true },
                    enabled = canPrestige,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                ) { Text(stringResource(R.string.prestige_button, num(available))) }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(stringResource(R.string.prestige_confirm_title)) },
            text = { Text(stringResource(R.string.prestige_confirm_body, num(available))) },
            confirmButton = {
                TextButton(onClick = {
                    confirm = false
                    vm.prestige()
                    onBack()
                }) { Text(stringResource(R.string.prestige_confirm_yes)) }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

// --- Achievements ----------------------------------------------------------------------------------

@Composable
fun AchievementsScreen(state: GameState, onBack: () -> Unit) {
    val context = LocalResources.current
    val style = LocalNumberStyle.current
    val sorted = remember(state.achievements) {
        Achievements.all.sortedBy { if (it.id in state.achievements) 1 else 0 }
    }
    MaxWidth {
        Column(Modifier.fillMaxSize()) {
            SubPageHeader(stringResource(R.string.achievements_title), onBack)
            Text(
                stringResource(R.string.achievements_bonus, percent(AchievementEngine.bonusPercent(state))),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(sorted, key = { it.id }) { def ->
                    val unlocked = def.id in state.achievements
                    val progress = (AchievementEngine.progress(state, def) / def.target).toFloat().coerceIn(0f, 1f)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (unlocked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (unlocked) Icons.Outlined.EmojiEvents else Icons.Outlined.Lock,
                                contentDescription = stringResource(if (unlocked) R.string.cd_unlocked else R.string.cd_locked),
                                tint = if (unlocked) GameTheme.colors.money else MaterialTheme.colorScheme.outline,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(MetaText.achievementDescription(context, def, style), style = MaterialTheme.typography.titleSmall)
                                Text(MetaText.achievementReward(context, def), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                if (!unlocked) {
                                    Spacer(Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Statistics ------------------------------------------------------------------------------------

@Composable
fun StatisticsScreen(state: GameState, onBack: () -> Unit) {
    val s = state.stats
    MaxWidth {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SubPageHeader(stringResource(R.string.stats_title), onBack)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    StatRow(stringResource(R.string.stat_lifetime_earnings), money(s.lifetimeEarnings))
                    StatRow(stringResource(R.string.stat_run_earnings), money(state.runEarnings))
                    StatRow(stringResource(R.string.stat_best_net_worth), money(s.bestNetWorth))
                    StatRow(stringResource(R.string.stat_time_played), duration(s.timePlayedSeconds))
                    StatRow(stringResource(R.string.stat_taps), num(s.taps.toDouble()))
                    StatRow(stringResource(R.string.stat_units_bought), num(s.unitsBought.toDouble()))
                    StatRow(stringResource(R.string.stat_managers_hired), s.managersHired.toString())
                    StatRow(stringResource(R.string.stat_stock_trades), num(s.stockTrades.toDouble()))
                    StatRow(stringResource(R.string.stat_events), s.eventsResolved.toString())
                    StatRow(stringResource(R.string.stat_loans_repaid), s.loansRepaid.toString())
                    StatRow(stringResource(R.string.stat_interest_earned), money(state.bank.interestEarned))
                    StatRow(stringResource(R.string.stat_best_streak), s.bestStreak.toString())
                    StatRow(stringResource(R.string.stat_prestige_count), s.prestigeCount.toString())
                    StatRow(stringResource(R.string.stat_legacy_points), num(state.legacyPoints))
                }
            }
        }
    }
}
