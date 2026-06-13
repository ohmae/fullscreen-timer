/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import net.mm2d.color.chooser.compose.ColorChooserDialog
import net.mm2d.timer.SettingsViewModel.DialogUiState
import net.mm2d.timer.SettingsViewModel.UiEffect.ToLicense
import net.mm2d.timer.SettingsViewModel.UiEffect.ToPlayStore
import net.mm2d.timer.SettingsViewModel.UiEffect.ToPrivacyPolicy
import net.mm2d.timer.SettingsViewModel.UiEffect.ToSourceCode
import net.mm2d.timer.SettingsViewModel.UiEffect.ToUp
import net.mm2d.timer.SettingsViewModel.UiEvent
import net.mm2d.timer.SettingsViewModel.UiState
import net.mm2d.timer.dialog.FontDialog
import net.mm2d.timer.dialog.OrientationDialog
import net.mm2d.timer.settings.Font
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.Orientation
import net.mm2d.timer.ui.theme.AppTheme
import net.mm2d.timer.util.Launcher

@Composable
fun SettingsScreen(
    goBack: () -> Unit,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val currentGoBack by rememberUpdatedState(goBack)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        viewModel.getUiEffectStream()
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .collect { effect ->
                when (effect) {
                    ToUp ->
                        currentGoBack()

                    ToLicense ->
                        LicenseActivity.start(context)

                    ToPrivacyPolicy ->
                        Launcher.openPrivacyPolicy(context)

                    ToSourceCode ->
                        Launcher.openSourceCode(context)

                    ToPlayStore ->
                        Launcher.openGooglePlay(context)
                }
            }
    }

    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    SettingsScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )

    val dialogUiState by viewModel.getDialogUiStateStream().collectAsStateWithLifecycle()
    DialogContent(
        dialogUiState = dialogUiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun DialogContent(
    dialogUiState: DialogUiState,
    onEvent: (UiEvent) -> Unit,
) {
    when (dialogUiState) {
        is DialogUiState.Dismiss -> Unit

        is DialogUiState.BackgroundColorSelect -> ColorChooserDialog(
            initialColor = dialogUiState.color,
            onChooseColor = { onEvent(UiEvent.SelectBackgroundColor(it)) },
            onDismissRequest = { onEvent(UiEvent.DismissDialog) },
        )

        is DialogUiState.ForegroundColorSelect -> ColorChooserDialog(
            initialColor = dialogUiState.color,
            onChooseColor = { onEvent(UiEvent.SelectForegroundColor(it)) },
            onDismissRequest = { onEvent(UiEvent.DismissDialog) },
        )

        is DialogUiState.FontSelect -> FontDialog(
            selectedFont = dialogUiState.font,
            onChooseFont = { onEvent(UiEvent.SelectFont(it)) },
            onDismissRequest = { onEvent(UiEvent.DismissDialog) },
        )

        is DialogUiState.OrientationSelect -> OrientationDialog(
            selectedOrientation = dialogUiState.orientation,
            onChooseOrientation = { onEvent(UiEvent.SelectOrientation(it)) },
            onDismissRequest = { onEvent(UiEvent.DismissDialog) },
        )
    }
}

@Composable
private fun SettingsScreenContent(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SettingsTopAppBar(onEvent)
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            settingsItems(
                uiState = uiState,
                onEvent = onEvent,
            )
        }
    }
}

private fun LazyListScope.settingsItems(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    item(key = "mode") {
        ModeSelector(
            modifier = Modifier.animateItem(),
            selectedMode = uiState.mode.selectedMode,
            onModeSelected = { onEvent(UiEvent.SelectMode(it)) },
        )
    }
    item(key = "foreground_color") {
        val color = Color(uiState.foregroundColor.color)
        ColorMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_foreground_color,
            descriptionRes = R.string.menu_description_foreground_color,
            color = color,
            onClick = { onEvent(UiEvent.ClickForegroundColor(it)) },
        )
    }
    item(key = "background_color") {
        val color = Color(uiState.backgroundColor.color)
        ColorMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_background_color,
            descriptionRes = R.string.menu_description_background_color,
            color = color,
            onClick = { onEvent(UiEvent.ClickBackgroundColor(it)) },
        )
    }
    item(key = "button_opacity") {
        SliderMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_button_opacity,
            value = uiState.buttonOpacity.value,
            valueRange = 0f..100f,
            steps = 99,
            valueFormatter = { "${it.toInt()}%" },
            onValueChange = { onEvent(UiEvent.SelectButtonOpacity(it / 100f)) },
        )
    }
    item(key = "volume") {
        SliderMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_volume,
            value = uiState.volume.value,
            valueRange = 0f..10f,
            steps = 9,
            valueFormatter = { it.toInt().toString() },
            onValueChange = { onEvent(UiEvent.SelectVolume(it.toInt())) },
        )
    }
    if (uiState.mode.selectedMode == Mode.CLOCK) {
        clockSettingsItems(
            uiState = uiState,
            onEvent = onEvent,
        )
    } else {
        timerStopwatchSettingsItems(
            uiState = uiState,
            onEvent = onEvent,
        )
    }
    item(key = "fullscreen") {
        SwitchMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_fullscreen,
            descriptionRes = uiState.fullscreen.enabled.descriptionRes(
                enabledRes = R.string.menu_description_fullscreen_on,
                disabledRes = R.string.menu_description_fullscreen_off,
            ),
            checked = uiState.fullscreen.enabled,
            onCheckedChange = { onEvent(UiEvent.SelectFullscreen(it)) },
        )
    }
    item(key = "font") {
        TextMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_font,
            descriptionRes = fontDescription(uiState.font.font),
            onClick = { onEvent(UiEvent.ClickFontMenu(uiState.font.font)) },
        )
    }
    item(key = "orientation") {
        TextWithIconMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_orientation,
            descriptionRes = uiState.orientation.orientation.description,
            iconRes = uiState.orientation.orientation.icon,
            onClick = { onEvent(UiEvent.ClickOrientationMenu(uiState.orientation.orientation)) },
        )
    }
    item(key = "version") {
        FixedTextMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_version,
            description = BuildConfig.VERSION_NAME,
        )
    }
}

private fun LazyListScope.clockSettingsItems(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    item(key = "hour_format") {
        SwitchMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_hour_notation,
            descriptionRes = uiState.clock.hourFormat24.enabled.descriptionRes(
                enabledRes = R.string.menu_description_hour_notation_on,
                disabledRes = R.string.menu_description_hour_notation_off,
            ),
            checked = uiState.clock.hourFormat24.enabled,
            onCheckedChange = { onEvent(UiEvent.SelectHourFormat24(it)) },
        )
    }
    item(key = "second_enabled") {
        SwitchMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_second_enabled,
            descriptionRes = uiState.clock.secondEnabled.enabled.descriptionRes(
                enabledRes = R.string.menu_description_second_enabled_on,
                disabledRes = R.string.menu_description_second_enabled_off,
            ),
            checked = uiState.clock.secondEnabled.enabled,
            onCheckedChange = { onEvent(UiEvent.SelectSecondEnabled(it)) },
        )
    }
}

private fun LazyListScope.timerStopwatchSettingsItems(
    uiState: UiState,
    onEvent: (UiEvent) -> Unit,
) {
    item(key = "hour_enabled") {
        SwitchMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_hour_enabled,
            descriptionRes = uiState.timerStopwatch.hourEnabled.enabled.descriptionRes(
                enabledRes = R.string.menu_description_hour_enabled_on,
                disabledRes = R.string.menu_description_hour_enabled_off,
            ),
            checked = uiState.timerStopwatch.hourEnabled.enabled,
            onCheckedChange = { onEvent(UiEvent.SelectHourEnabled(it)) },
        )
    }
    item(key = "millisecond_enabled") {
        SwitchMenuRow(
            modifier = Modifier.animateItem(),
            titleRes = R.string.menu_title_millisecond_enabled,
            descriptionRes = uiState.timerStopwatch.millisecondEnabled.enabled.descriptionRes(
                enabledRes = R.string.menu_description_millisecond_enabled_on,
                disabledRes = R.string.menu_description_millisecond_enabled_off,
            ),
            checked = uiState.timerStopwatch.millisecondEnabled.enabled,
            onCheckedChange = { onEvent(UiEvent.SelectMillisecondEnabled(it)) },
        )
    }
}

@StringRes
private fun Boolean.descriptionRes(
    @StringRes enabledRes: Int,
    @StringRes disabledRes: Int,
): Int =
    if (this) {
        enabledRes
    } else {
        disabledRes
    }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsTopAppBar(
    onEvent: (UiEvent) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        navigationIcon = {
            IconButton(onClick = { onEvent(UiEvent.ClickUp) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                SettingsMenuItem(R.string.options_menu_license) {
                    expanded = false
                    onEvent(UiEvent.ClickLicense)
                }
                SettingsMenuItem(R.string.options_menu_source_code) {
                    expanded = false
                    onEvent(UiEvent.ClickSourceCode)
                }
                SettingsMenuItem(R.string.options_menu_privacy_policy) {
                    expanded = false
                    onEvent(UiEvent.ClickPrivacyPolicy)
                }
                SettingsMenuItem(R.string.options_menu_play_store) {
                    expanded = false
                    onEvent(UiEvent.ClickPlayStore)
                }
            }
        },
    )
}

@Composable
private fun SettingsMenuItem(
    @StringRes textRes: Int,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(textRes),
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun ModeSelector(
    modifier: Modifier = Modifier,
    selectedMode: Mode,
    onModeSelected: (Mode) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Mode.entries.forEach { mode ->
            ModeItem(
                modifier = Modifier.weight(1f),
                mode = mode,
                selected = mode == selectedMode,
                onClick = { onModeSelected(mode) },
            )
        }
    }
}

@Composable
private fun ModeItem(
    modifier: Modifier = Modifier,
    mode: Mode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.Transparent
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(mode.iconRes()),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(mode.labelRes()),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun ColorMenuRow(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    color: Color,
    onClick: (Color) -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = { onClick(color) })
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
}

@Composable
private fun SliderMenuRow(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueFormatter: (Float) -> String,
    onValueChange: (Float) -> Unit,
) {
    var tempValue by remember(value) { mutableFloatStateOf(value.toFloat()) }
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    modifier = Modifier.weight(1f),
                    value = tempValue,
                    onValueChange = { tempValue = it },
                    onValueChangeFinished = { onValueChange(tempValue) },
                    valueRange = valueRange,
                    steps = steps,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = valueFormatter(tempValue),
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SwitchMenuRow(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = { onCheckedChange(!checked) })
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
}

@Composable
private fun TextMenuRow(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
}

@Composable
private fun FixedTextMenuRow(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    description: String,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
}

@Composable
private fun TextWithIconMenuRow(
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
}

@StringRes
private fun fontDescription(
    font: Font,
): Int =
    when (font) {
        Font.LED_7SEGMENT -> R.string.menu_description_font_led_7segment
        Font.ROBOTO -> R.string.menu_description_font_roboto
    }

@StringRes
private fun Mode.labelRes(): Int =
    when (this) {
        Mode.CLOCK -> R.string.label_clock
        Mode.STOPWATCH -> R.string.label_stopwatch
        Mode.TIMER -> R.string.label_timer
    }

@DrawableRes
private fun Mode.iconRes(): Int =
    when (this) {
        Mode.CLOCK -> R.drawable.ic_clock
        Mode.STOPWATCH -> R.drawable.ic_stopwatch
        Mode.TIMER -> R.drawable.ic_timer
    }

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreenContent(
            uiState = UiState(
                mode = SettingsViewModel.ModeSetting(Mode.TIMER),
                buttonOpacity = SettingsViewModel.SliderSetting(72),
                volume = SettingsViewModel.SliderSetting(7),
                timerStopwatch = SettingsViewModel.TimerStopwatchSettings(
                    hourEnabled = SettingsViewModel.ToggleSetting(true),
                    millisecondEnabled = SettingsViewModel.ToggleSetting(true),
                ),
                fullscreen = SettingsViewModel.ToggleSetting(true),
                orientation = SettingsViewModel.OrientationSetting(Orientation.LANDSCAPE),
            ),
            onEvent = {},
        )
    }
}
