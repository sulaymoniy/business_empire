package com.businessempire.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import com.businessempire.game.data.ImageCredit
import com.businessempire.game.data.ImageCreditsRepository
import com.businessempire.game.ui.components.EmptyState
import com.businessempire.game.ui.components.MaxWidth

/** Settings > About > Photo credits. Lists author, source and licence for every bundled photo. */
@Composable
fun CreditsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var credits by remember { mutableStateOf<List<ImageCredit>?>(null) }
    LaunchedEffect(Unit) { credits = ImageCreditsRepository(context.applicationContext).load() }
    MaxWidth {
        Column(Modifier.fillMaxSize()) {
            SubPageHeader(stringResource(R.string.credits_title), onBack)
            val list = credits ?: return@Column
            if (list.isEmpty()) {
                EmptyState(Icons.Outlined.PhotoLibrary, stringResource(R.string.credits_empty_title), stringResource(R.string.credits_empty_body))
                return@Column
            }
            Text(
                stringResource(R.string.credits_intro),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(list, key = { it.imageName }) { CreditRow(it) }
            }
        }
    }
}

@Composable
private fun CreditRow(c: ImageCredit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        SelectionContainer {
            Column(Modifier.padding(12.dp)) {
                Text(c.title, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.credits_author, c.author), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.credits_licence, c.licence), style = MaterialTheme.typography.bodySmall)
                if (c.modified.isNotBlank()) {
                    Text(stringResource(R.string.credits_modified, c.modified), style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    c.sourceUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (c.licenceUrl.isNotBlank()) {
                    Text(c.licenceUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    c.imageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
