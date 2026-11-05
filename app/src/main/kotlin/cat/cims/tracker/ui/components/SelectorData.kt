package cat.cims.tracker.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cat.cims.tracker.R
import cat.cims.tracker.ui.theme.Colors

private const val MS_PER_DIA = 86_400_000L

/** Tria un dia, fins avui (els registres són de coses que ja has fet). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorData(inicial: Long, avui: Long, onTria: (Long) -> Unit, onTanca: () -> Unit) {
    val estat = rememberDatePickerState(
        initialSelectedDateMillis = inicial * MS_PER_DIA,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis / MS_PER_DIA <= avui
        },
    )
    DatePickerDialog(
        onDismissRequest = onTanca,
        confirmButton = {
            TextButton(onClick = { estat.selectedDateMillis?.let { onTria(it / MS_PER_DIA) }; onTanca() }) {
                Text(stringResource(R.string.accepta), color = Colors.Text)
            }
        },
        dismissButton = { TextButton(onClick = onTanca) { Text(stringResource(R.string.cancela), color = Colors.TextSecundari) } },
    ) { DatePicker(estat) }
}
