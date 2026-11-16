package com.businessempire.game.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.businessempire.game.R
import com.businessempire.game.domain.EventEngine
import com.businessempire.game.domain.GameState
import com.businessempire.game.domain.PendingEvent
import com.businessempire.game.domain.content.TutorialStep
import com.businessempire.game.ui.LocalNumberStyle
import com.businessempire.game.ui.MetaText
import com.businessempire.game.ui.money

/** A random event's choice dialog. It can't be dismissed without choosing. */
@Composable
fun EventDialog(state: GameState, event: PendingEvent, onChoose: (accept: Boolean) -> Unit) {
    val context = LocalResources.current
    val style = LocalNumberStyle.current
    val canAccept = EventEngine.canAccept(state, event)
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(stringResource(MetaText.eventTitle(event.type))) },
        text = {
            Column {
                Text(MetaText.eventBody(context, event, state.profile.companyName, style))
                if (!canAccept) {
                    Text(
                        stringResource(R.string.event_cant_afford, money(EventEngine.acceptCost(event))),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onChoose(true) }, enabled = canAccept, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(MetaText.eventAccept(event.type)))
            }
        },
        dismissButton = {
            TextButton(onClick = { onChoose(false) }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(MetaText.eventDecline(event.type)))
            }
        },
    )
}

/** First-run coach mark shown above the current screen. */
@Composable
fun TutorialBanner(step: TutorialStep, onSkip: () -> Unit) {
    if (step == TutorialStep.DONE) return
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Lightbulb, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.tutorial_step, step.ordinal + 1, TutorialStep.DONE.ordinal),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(stringResource(MetaText.tutorial(step)), style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onSkip, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.tutorial_skip)) }
        }
    }
}
