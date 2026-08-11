/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.mm2d.timer.R
import net.mm2d.timer.ui.theme.AppTheme
import net.mm2d.timer.dialog.TimeDialogInput.Unit as TimeUnit

@Composable
fun TimeDialog(
    timeMillis: Long,
    hourEnabled: Boolean,
    onSelectTime: (Long) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var input by rememberSaveable(
        timeMillis,
        hourEnabled,
        stateSaver = TimeDialogInputSaver,
    ) {
        mutableStateOf(TimeDialogInput.fromMillis(timeMillis, hourEnabled))
    }
    val selectedTimeMillis = input.timeMillisOrNull

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .sizeIn(minWidth = DialogMinWidth, maxWidth = TimeDialogMaxWidth),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_set_timer),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
                TimeInputFields(
                    input = input,
                    onInputChange = { input = it },
                )
                TimePresetButtons(
                    onSelectPreset = { minutes ->
                        input = TimeDialogInput.fromMillis(
                            timeMillis = minutes * MILLIS_PER_MINUTE,
                            hourEnabled = hourEnabled,
                        )
                    },
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                ) {
                    TextButton(
                        onClick = {
                            input = TimeDialogInput.fromMillis(
                                timeMillis = 0L,
                                hourEnabled = hourEnabled,
                            )
                        },
                    ) {
                        Text(stringResource(R.string.reset))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(
                        enabled = selectedTimeMillis != null && selectedTimeMillis > 0L,
                        onClick = { selectedTimeMillis?.let(onSelectTime) },
                    ) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeInputFields(
    input: TimeDialogInput,
    onInputChange: (TimeDialogInput) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        val horizontal = !input.hourEnabled || maxWidth >= ThreeColumnMinWidth
        val controls = @Composable { modifier: Modifier, compact: Boolean ->
            if (input.hourEnabled) {
                TimeUnitInput(
                    value = input.hour,
                    unit = TimeUnit.HOUR,
                    maximumValue = 9,
                    imeAction = ImeAction.Next,
                    isValid = input.isValid(TimeUnit.HOUR),
                    compact = compact,
                    onValueChange = { onInputChange(input.update(TimeUnit.HOUR, it)) },
                    onAdjust = { onInputChange(input.adjust(TimeUnit.HOUR, it)) },
                    onFocusLost = { onInputChange(input.resetIfInvalid(TimeUnit.HOUR)) },
                    modifier = modifier,
                )
            }
            TimeUnitInput(
                value = input.minute,
                unit = TimeUnit.MINUTE,
                maximumValue = input.maxMinute,
                imeAction = ImeAction.Next,
                isValid = input.isValid(TimeUnit.MINUTE),
                compact = compact,
                onValueChange = { onInputChange(input.update(TimeUnit.MINUTE, it)) },
                onAdjust = { onInputChange(input.adjust(TimeUnit.MINUTE, it)) },
                onFocusLost = { onInputChange(input.resetIfInvalid(TimeUnit.MINUTE)) },
                modifier = modifier,
            )
            TimeUnitInput(
                value = input.second,
                unit = TimeUnit.SECOND,
                maximumValue = 59,
                imeAction = ImeAction.Done,
                isValid = input.isValid(TimeUnit.SECOND),
                compact = compact,
                onValueChange = { onInputChange(input.update(TimeUnit.SECOND, it)) },
                onAdjust = { onInputChange(input.adjust(TimeUnit.SECOND, it)) },
                onFocusLost = { onInputChange(input.resetIfInvalid(TimeUnit.SECOND)) },
                modifier = modifier,
            )
        }
        if (horizontal) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                controls(Modifier.weight(1f), false)
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                controls(Modifier.fillMaxWidth(), true)
            }
        }
    }
}

@Composable
private fun TimePresetButtons(
    onSelectPreset: (Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PRESET_MINUTES.forEach { minutes ->
            FilledTonalButton(
                modifier = Modifier
                    .weight(1f)
                    .testTag("TimeDialog:preset:$minutes"),
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSelectPreset(minutes)
                },
            ) {
                Text(stringResource(R.string.timer_time_preset_minutes, minutes))
            }
        }
    }
}

@Composable
private fun TimeUnitInput(
    value: String,
    unit: TimeUnit,
    maximumValue: Int,
    imeAction: ImeAction,
    isValid: Boolean,
    compact: Boolean,
    onValueChange: (String) -> Unit,
    onAdjust: (Int) -> Unit,
    onFocusLost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(unit.labelRes)
    val decreaseContentDescription = stringResource(R.string.timer_time_decrease, label)
    val increaseContentDescription = stringResource(R.string.timer_time_increase, label)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        if (fieldValue.text != value) {
            fieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    val textField = @Composable { textFieldModifier: Modifier ->
        OutlinedTextField(
            value = fieldValue,
            onValueChange = {
                if (it.text.length <= unit.maxDigits && it.text.all(Char::isDigit)) {
                    fieldValue = it
                    onValueChange(it.text)
                }
            },
            modifier = textFieldModifier
                .testTag(unit.testTag("input"))
                .onFocusChanged {
                    if (it.isFocused && !focused) {
                        fieldValue = fieldValue.copy(selection = TextRange(0, fieldValue.text.length))
                    }
                    if (!it.isFocused && focused && !isValid) {
                        onFocusLost()
                    }
                    focused = it.isFocused
                },
            label = { Text(label) },
            supportingText = if (isValid) {
                null
            } else {
                { Text(stringResource(R.string.timer_time_value_range, maximumValue)) }
            },
            isError = !isValid,
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) },
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
            ),
        )
    }
    val adjustButtons = @Composable { buttonsModifier: Modifier ->
        Row(
            modifier = buttonsModifier,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TimeAdjustButton(
                text = "−",
                contentDescription = decreaseContentDescription,
                testTag = unit.testTag("decrease"),
                onClick = { onAdjust(-1) },
            )
            TimeAdjustButton(
                text = "+",
                contentDescription = increaseContentDescription,
                testTag = unit.testTag("increase"),
                onClick = { onAdjust(1) },
            )
        }
    }
    if (compact) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeAdjustButton(
                text = "−",
                contentDescription = decreaseContentDescription,
                testTag = unit.testTag("decrease"),
                onClick = { onAdjust(-1) },
            )
            textField(Modifier.weight(1f))
            TimeAdjustButton(
                text = "+",
                contentDescription = increaseContentDescription,
                testTag = unit.testTag("increase"),
                onClick = { onAdjust(1) },
            )
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            textField(Modifier.fillMaxWidth())
            adjustButtons(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun TimeAdjustButton(
    text: String,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        modifier = Modifier
            .testTag(testTag)
            .semantics { this.contentDescription = contentDescription },
        onClick = onClick,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

private val TimeUnit.labelRes: Int
    get() =
        when (this) {
            TimeUnit.HOUR -> R.string.timer_time_hour
            TimeUnit.MINUTE -> R.string.timer_time_minute
            TimeUnit.SECOND -> R.string.timer_time_second
        }

private fun TimeUnit.testTag(
    action: String,
): String = "TimeDialog:${name.lowercase()}:$action"

private val TimeDialogInputSaver = Saver<TimeDialogInput, List<Any>>(
    save = { listOf(it.hour, it.minute, it.second, it.hourEnabled) },
    restore = {
        TimeDialogInput(
            hour = it[0] as String,
            minute = it[1] as String,
            second = it[2] as String,
            hourEnabled = it[3] as Boolean,
        )
    },
)

private val TimeDialogMaxWidth = 400.dp
private val ThreeColumnMinWidth = 304.dp
private val PRESET_MINUTES = listOf(1, 3, 5)
private const val MILLIS_PER_MINUTE = 60_000L

@Preview(showBackground = true)
@Composable
private fun TimeDialogPreview() {
    AppTheme {
        TimeDialog(
            timeMillis = 330_000L,
            hourEnabled = true,
            onSelectTime = {},
            onDismissRequest = {},
        )
    }
}
