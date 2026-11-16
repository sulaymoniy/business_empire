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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import com.businessempire.game.domain.BankEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.content.BankRules
import com.businessempire.game.ui.GameViewModel
import com.businessempire.game.ui.components.AmountInput
import com.businessempire.game.ui.components.IconBadge
import com.businessempire.game.ui.components.MaxWidth
import com.businessempire.game.ui.components.StatRow
import com.businessempire.game.ui.components.parseAmount
import com.businessempire.game.ui.components.rememberGameFeedback
import com.businessempire.game.ui.duration
import com.businessempire.game.ui.money
import com.businessempire.game.ui.num
import com.businessempire.game.ui.theme.GameTheme

@Composable
fun BankScreen(state: GameState, vm: GameViewModel) {
    MaxWidth {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SavingsCard(state, vm)
            LoanCard(state, vm)
        }
    }
}

@Composable
private fun SavingsCard(state: GameState, vm: GameViewModel) {
    var amountText by rememberSaveable { mutableStateOf("") }
    val amount = parseAmount(amountText)
    val feedback = rememberGameFeedback()
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            CardTitle(Icons.Outlined.Savings, stringResource(R.string.bank_savings))
            Text(money(state.bank.savings), style = MaterialTheme.typography.headlineSmall, color = GameTheme.colors.money)
            StatRow(
                stringResource(R.string.bank_interest_rate),
                stringResource(R.string.rate_per_minute, num(BankRules.SAVINGS_RATE_PER_MINUTE * 100)),
            )
            StatRow(stringResource(R.string.bank_interest_earned), money(state.bank.interestEarned))
            Spacer(Modifier.height(8.dp))
            AmountInput(amountText, { amountText = it }, max = maxOf(state.cash, state.bank.savings))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                Button(
                    onClick = { vm.deposit(amount); feedback.purchase(); amountText = "" },
                    enabled = amount > 0 && state.cash > 0,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.bank_deposit)) }
                FilledTonalButton(
                    onClick = { vm.withdraw(amount); feedback.purchase(); amountText = "" },
                    enabled = amount > 0 && state.bank.savings > 0,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.bank_withdraw)) }
            }
            Text(
                stringResource(R.string.bank_savings_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun LoanCard(state: GameState, vm: GameViewModel) {
    var amountText by rememberSaveable { mutableStateOf("") }
    val amount = parseAmount(amountText)
    val credit = BankEngine.availableCredit(state)
    val feedback = rememberGameFeedback()
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            CardTitle(Icons.Outlined.RequestQuote, stringResource(R.string.bank_loan))
            Text(
                money(state.bank.loan),
                style = MaterialTheme.typography.headlineSmall,
                color = if (state.bank.loan > 0) GameTheme.colors.loss else MaterialTheme.colorScheme.onSurface,
            )
            StatRow(
                stringResource(R.string.bank_interest_rate),
                stringResource(R.string.rate_per_minute, num(BankRules.LOAN_RATE_PER_MINUTE * 100)),
            )
            StatRow(stringResource(R.string.bank_available_credit), money(credit))
            if (state.bank.loan > 0) {
                val left = BankRules.OVERDUE_SECONDS - state.bank.loanAgeSeconds
                StatRow(
                    stringResource(R.string.bank_repay_within),
                    if (state.bank.overdue) stringResource(R.string.bank_overdue_short) else duration(left),
                    valueColor = if (state.bank.overdue) GameTheme.colors.loss else null,
                )
            }
            if (state.bank.overdue) {
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.bank_overdue), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(8.dp))
            AmountInput(amountText, { amountText = it }, max = if (state.bank.loan > 0) minOf(state.cash, state.bank.loan) else credit)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                Button(
                    onClick = { vm.borrow(amount); feedback.purchase(); amountText = "" },
                    enabled = amount > 0 && credit > 0,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.bank_borrow)) }
                FilledTonalButton(
                    onClick = { vm.repay(amount); feedback.purchase(); amountText = "" },
                    enabled = amount > 0 && state.bank.loan > 0 && state.cash > 0,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.bank_repay)) }
            }
            Text(
                stringResource(R.string.bank_loan_hint, num(BankRules.MAX_LOAN_FRACTION * 100)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun CardTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        IconBadge(icon, size = 36)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
    }
}
