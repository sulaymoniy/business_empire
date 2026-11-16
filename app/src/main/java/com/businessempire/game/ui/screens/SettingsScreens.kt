@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.businessempire.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.businessempire.game.BuildConfig
import com.businessempire.game.R
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.NumberFormatter
import com.businessempire.game.domain.NumberStyle
import com.businessempire.game.monetization.ProductIds
import com.businessempire.game.ui.GameViewModel
import com.businessempire.game.ui.components.MaxWidth
import com.businessempire.game.ui.components.SectionTitle

@Composable
fun SettingsScreen(state: GameState, vm: GameViewModel, onBack: () -> Unit, onAbout: () -> Unit) {
    var confirmReset by remember { mutableStateOf(false) }
    MaxWidth {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SubPageHeader(stringResource(R.string.settings_title), onBack)
            SettingsCard {
                ToggleRow(stringResource(R.string.settings_sound), state.settings.sound, vm::setSound)
                ToggleRow(stringResource(R.string.settings_haptics), state.settings.haptics, vm::setHaptics)
            }
            SectionTitle(stringResource(R.string.settings_number_format))
            SettingsCard {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val options = listOf(NumberStyle.SHORT to R.string.settings_short, NumberStyle.SCIENTIFIC to R.string.settings_scientific)
                    options.forEachIndexed { index, (style, label) ->
                        SegmentedButton(
                            selected = state.settings.numberStyle == style,
                            onClick = { vm.setNumberStyle(style) },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        ) { Text(stringResource(label)) }
                    }
                }
                Text(
                    stringResource(R.string.settings_number_example, NumberFormatter.format(1_234_567_890.0, state.settings.numberStyle)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            SectionTitle(stringResource(R.string.settings_more))
            SettingsCard {
                FilledTonalButton(onClick = onAbout, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.about_title))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { confirmReset = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.settings_reset)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.reset_confirm_title)) },
            text = { Text(stringResource(R.string.reset_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    vm.resetGame()
                }) { Text(stringResource(R.string.reset_confirm_yes), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
    ) { Column(Modifier.padding(16.dp)) { content() } }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
fun AboutScreen(richList: com.businessempire.game.domain.RichList, onBack: () -> Unit, onCredits: () -> Unit) {
    MaxWidth {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SubPageHeader(stringResource(R.string.about_title), onBack)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.about_body), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.about_fiction), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.about_privacy), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                RichListDisclaimer(richList)
                FilledTonalButton(onClick = onCredits, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.credits_title))
                }
            }
        }
    }
}

/**
 * Store front. Hidden unless BuildConfig.STORE_ENABLED is true.
 * MONETIZATION: replace "Coming soon" with real products from MonetizationService once billing exists.
 */
@Composable
fun ShopScreen(onBack: () -> Unit) {
    MaxWidth {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SubPageHeader(stringResource(R.string.shop_title), onBack)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.shop_coming_soon), style = MaterialTheme.typography.titleMedium)
                // MONETIZATION: "2x income forever" -> MonetizationService.purchase(ProductIds.PREMIUM_2X_INCOME)
                ShopItem(stringResource(R.string.shop_premium), ProductIds.PREMIUM_2X_INCOME)
                // MONETIZATION: consumable cash pack -> MonetizationService.purchase(ProductIds.CASH_PACK_SMALL)
                ShopItem(stringResource(R.string.shop_cash_pack), ProductIds.CASH_PACK_SMALL)
            }
        }
    }
}

@Composable
private fun ShopItem(title: String, @Suppress("UNUSED_PARAMETER") productId: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Button(onClick = {}, enabled = false) { Text(stringResource(R.string.shop_soon)) }
        }
    }
}

/** Debug-only testing tools. Never shown in release builds (BuildConfig.DEBUG gate in ProfileScreen and AppRoot). */
@Composable
fun CheatsScreen(state: GameState, vm: GameViewModel, onBack: () -> Unit) {
    MaxWidth {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SubPageHeader(stringResource(R.string.cheats_title), onBack)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.cheats_subtitle), style = MaterialTheme.typography.bodyMedium)
                CheatButton(stringResource(R.string.cheat_add_cash_small)) { vm.debugAddCash(1e6) }
                CheatButton(stringResource(R.string.cheat_add_cash_x10)) { vm.debugAddCash(maxOf(state.cash * 9, 1e6)) }
                CheatButton(stringResource(R.string.cheat_skip_hour)) { vm.debugSkipTime(3600.0) }
                CheatButton(stringResource(R.string.cheat_unlock_all)) { vm.debugUnlockAll() }
                CheatButton(stringResource(R.string.cheat_trigger_event)) { vm.debugTriggerEvent() }
            }
        }
    }
}

@Composable
private fun CheatButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(label) }
}
