package com.businessempire.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.businessempire.game.R
import java.util.Locale

/**
 * Money amount entry: a number field plus 25% / 50% / 100% shortcuts of [max].
 * The amount is kept as text so the user can type freely.
 */
@Composable
fun AmountInput(
    text: String,
    onTextChange: (String) -> Unit,
    max: Double,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = { raw -> onTextChange(raw.filter { it.isDigit() || it == '.' }.take(20)) },
            label = { Text(stringResource(R.string.amount)) },
            prefix = { Text("$") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0.25 to "25%", 0.5 to "50%", 1.0 to stringResource(R.string.amount_max)).forEach { (fraction, label) ->
                AssistChip(
                    onClick = { onTextChange(formatPlain(max * fraction)) },
                    label = { Text(label) },
                    enabled = max > 0,
                    modifier = Modifier.heightIn(min = 48.dp),
                )
            }
        }
    }
}

/** Plain decimal for the text field (no suffixes), rounded down to cents. */
fun formatPlain(value: Double): String {
    val cents = kotlin.math.floor(value.coerceAtLeast(0.0) * 100.0) / 100.0
    return if (cents >= 1e15) String.format(Locale.ROOT, "%.0f", cents)
    else String.format(Locale.ROOT, "%.2f", cents).trimEnd('0').trimEnd('.')
}

fun parseAmount(text: String): Double = text.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 } ?: 0.0
