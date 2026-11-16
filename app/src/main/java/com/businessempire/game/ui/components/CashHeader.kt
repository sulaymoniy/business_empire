package com.businessempire.game.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import com.businessempire.game.ui.money
import com.businessempire.game.ui.perSecond
import com.businessempire.game.ui.theme.GameTheme
import com.businessempire.game.ui.theme.MoneyTextStyle

/** Always-visible cash + income strip at the top of the app. */
@Composable
fun CashHeader(cash: Double, incomePerSecond: Double, netWorth: Double) {
    val cashText = money(cash)
    val incomeText = perSecond(incomePerSecond)
    val netWorthText = money(netWorth)
    val description = stringResource(R.string.cd_cash_header, cashText, incomeText, netWorthText)
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 2.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .semantics(mergeDescendants = true) { contentDescription = description },
        ) {
            Text(
                text = cashText,
                style = MoneyTextStyle,
                color = GameTheme.colors.money,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = incomeText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.header_net_worth, netWorthText),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}
