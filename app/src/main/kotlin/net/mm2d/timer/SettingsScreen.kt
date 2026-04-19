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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.mm2d.color.chooser.compose.ColorChooserDialog
import net.mm2d.timer.SettingsViewModel.UiState
import net.mm2d.timer.dialog.FontDialog
import net.mm2d.timer.dialog.OrientationDialog
import net.mm2d.timer.settings.Font
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.Orientation
import net.mm2d.timer.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigate: (NavigationDirection) -> Unit,
) {
    AppTheme {
        val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()
        SettingsScreen(
            uiState = uiState,
            onNavigate = onNavigate,
        )
        val dialogRequest by viewModel.getDialogRequestStream().collectAsStateWithLifecycle()
        DialogContent(dialogRequest)
    }
}

@Composable
private fun DialogContent(
    dialogRequest: SettingsViewModel.DialogRequest,
) {
    when (dialogRequest) {
        is SettingsViewModel.DialogRequest.Dismiss -> Unit

        is SettingsViewModel.DialogRequest.BackgroundColorSelect -> ColorChooserDialog(
            initialColor = dialogRequest.color,
            onChooseColor = dialogRequest.onChooseColor,
            onDismissRequest = dialogRequest.dismissRequest,
        )

        is SettingsViewModel.DialogRequest.ForegroundColorSelect -> ColorChooserDialog(
            initialColor = dialogRequest.color,
            onChooseColor = dialogRequest.onChooseColor,
            onDismissRequest = dialogRequest.dismissRequest,
        )

        is SettingsViewModel.DialogRequest.FontSelect -> FontDialog(
            selectedFont = dialogRequest.font,
            onChooseFont = dialogRequest.onChooseFont,
            onDismissRequest = dialogRequest.dismissRequest,
        )

        is SettingsViewModel.DialogRequest.OrientationSelect -> OrientationDialog(
            selectedOrientation = dialogRequest.orientation,
            onChooseOrientation = dialogRequest.onChooseOrientation,
            onDismissRequest = dialogRequest.dismissRequest,
        )
    }
}

private sealed class MenuItem {
    abstract val key: String

    data class ModeMenu(
        override val key: String,
        val mode: Mode,
        val onModeSelected: (Mode) -> Unit,
    ) : MenuItem()

    data class ColorMenu(
        override val key: String,
        val titleRes: Int,
        val descriptionRes: Int,
        val color: Color,
        val onChangeRequest: (Color) -> Unit,
    ) : MenuItem()

    data class SliderMenu(
        override val key: String,
        val titleRes: Int,
        val value: Int,
        val valueRange: ClosedFloatingPointRange<Float>,
        val steps: Int,
        val valueFormatter: (Float) -> String,
        val onValueChange: (Float) -> Unit,
    ) : MenuItem()

    data class SwitchMenu(
        override val key: String,
        val titleRes: Int,
        val descriptionRes: Int,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : MenuItem()

    data class TextMenu(
        override val key: String,
        val titleRes: Int,
        val descriptionRes: Int,
        val onClick: () -> Unit,
    ) : MenuItem()

    data class TextWithIconMenu(
        override val key: String,
        val titleRes: Int,
        val descriptionRes: Int,
        @DrawableRes val iconRes: Int,
        val onClick: () -> Unit,
    ) : MenuItem()

    data class FixedTextMenu(
        override val key: String,
        val titleRes: Int,
        val description: String,
    ) : MenuItem()
}

private fun UiState.toMenuItems(): List<MenuItem> =
    buildList {
        this += MenuItem.ModeMenu(
            key = "mode",
            mode = mode,
            onModeSelected = onModeSelected,
        )
        this += MenuItem.ColorMenu(
            key = "foreground_color",
            titleRes = R.string.menu_title_foreground_color,
            descriptionRes = R.string.menu_description_foreground_color,
            color = Color(foregroundColor),
            onChangeRequest = onForegroundColorRequest,
        )
        this += MenuItem.ColorMenu(
            key = "background_color",
            titleRes = R.string.menu_title_background_color,
            descriptionRes = R.string.menu_description_background_color,
            color = Color(backgroundColor),
            onChangeRequest = onBackgroundColorRequest,
        )
        this += MenuItem.SliderMenu(
            key = "button_opacity",
            titleRes = R.string.menu_title_button_opacity,
            value = (buttonOpacity * 100).toInt(),
            valueRange = 0f..100f,
            steps = 99,
            valueFormatter = { "${it.toInt()}%" },
            onValueChange = { onButtonOpacityChange(it / 100f) },
        )
        this += MenuItem.SliderMenu(
            key = "volume",
            titleRes = R.string.menu_title_volume,
            value = volume,
            valueRange = 0f..10f,
            steps = 9,
            valueFormatter = { it.toInt().toString() },
            onValueChange = { onVolumeChange(it.toInt()) },
        )
        if (mode == Mode.CLOCK) {
            this += MenuItem.SwitchMenu(
                key = "hour_format",
                titleRes = R.string.menu_title_hour_notation,
                descriptionRes =
                    if (hourFormat24) {
                        R.string.menu_description_hour_notation_on
                    } else {
                        R.string.menu_description_hour_notation_off
                    },
                checked = hourFormat24,
                onCheckedChange = onHourFormatChange,
            )
            this += MenuItem.SwitchMenu(
                key = "second_enabled",
                titleRes = R.string.menu_title_second_enabled,
                descriptionRes =
                    if (secondEnabled) {
                        R.string.menu_description_second_enabled_on
                    } else {
                        R.string.menu_description_second_enabled_off
                    },
                checked = secondEnabled,
                onCheckedChange = onSecondEnabledChange,
            )
        } else {
            this += MenuItem.SwitchMenu(
                key = "hour_enabled",
                titleRes = R.string.menu_title_hour_enabled,
                descriptionRes = (
                    if (hourEnabled) {
                        R.string.menu_description_hour_enabled_on
                    } else {
                        R.string.menu_description_hour_enabled_off
                    }
                    ),
                checked = hourEnabled,
                onCheckedChange = onHourEnabledChange,
            )
            this += MenuItem.SwitchMenu(
                key = "millisecond_enabled",
                titleRes = R.string.menu_title_millisecond_enabled,
                descriptionRes = if (millisecondEnabled) {
                    R.string.menu_description_millisecond_enabled_on
                } else {
                    R.string.menu_description_millisecond_enabled_off
                },
                checked = millisecondEnabled,
                onCheckedChange = onMillisecondEnabledChange,
            )
        }
        this += MenuItem.SwitchMenu(
            key = "fullscreen",
            titleRes = R.string.menu_title_fullscreen,
            descriptionRes =
                if (fullscreen) {
                    R.string.menu_description_fullscreen_on
                } else {
                    R.string.menu_description_fullscreen_off
                },
            checked = fullscreen,
            onCheckedChange = onFullscreenChange,
        )
        this += MenuItem.TextMenu(
            key = "font",
            titleRes = R.string.menu_title_font,
            descriptionRes = fontDescription(font),
            onClick = { onFontRequest(font) },
        )
        this += MenuItem.TextWithIconMenu(
            key = "orientation",
            titleRes = R.string.menu_title_orientation,
            descriptionRes = orientation.description,
            iconRes = orientation.icon,
            onClick = { onOrientationRequest(orientation) },
        )
        this += MenuItem.FixedTextMenu(
            key = "version",
            titleRes = R.string.menu_title_version,
            description = BuildConfig.VERSION_NAME,
        )
    }

@Composable
private fun SettingsScreen(
    uiState: UiState,
    onNavigate: (NavigationDirection) -> Unit,
) {
    val menuItems = uiState.toMenuItems()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SettingsTopAppBar(onNavigate)
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(
                items = menuItems,
                key = { it.key },
            ) {
                when (it) {
                    is MenuItem.ModeMenu -> {
                        ModeSelector(
                            modifier = Modifier.animateItem(),
                            selectedMode = it.mode,
                            onModeSelected = it.onModeSelected,
                        )
                    }

                    is MenuItem.ColorMenu -> {
                        ColorMenuRow(
                            modifier = Modifier.animateItem(),
                            titleRes = it.titleRes,
                            descriptionRes = it.descriptionRes,
                            color = it.color,
                            onClick = it.onChangeRequest,
                        )
                    }

                    is MenuItem.SliderMenu -> {
                        SliderMenuRow(
                            modifier = Modifier.animateItem(),
                            titleRes = it.titleRes,
                            value = it.value,
                            valueRange = it.valueRange,
                            steps = it.steps,
                            valueFormatter = it.valueFormatter,
                            onValueChange = it.onValueChange,
                        )
                    }

                    is MenuItem.SwitchMenu -> {
                        SwitchMenuRow(
                            modifier = Modifier.animateItem(),
                            titleRes = it.titleRes,
                            descriptionRes = it.descriptionRes,
                            checked = it.checked,
                            onCheckedChange = it.onCheckedChange,
                        )
                    }

                    is MenuItem.TextMenu -> {
                        TextMenuRow(
                            modifier = Modifier.animateItem(),
                            titleRes = it.titleRes,
                            descriptionRes = it.descriptionRes,
                            onClick = it.onClick,
                        )
                    }

                    is MenuItem.TextWithIconMenu -> {
                        TextWithIconMenuRow(
                            modifier = Modifier.animateItem(),
                            titleRes = it.titleRes,
                            descriptionRes = it.descriptionRes,
                            iconRes = it.iconRes,
                            onClick = it.onClick,
                        )
                    }

                    is MenuItem.FixedTextMenu -> {
                        FixedTextMenuRow(
                            modifier = Modifier.animateItem(),
                            titleRes = it.titleRes,
                            description = it.description,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsTopAppBar(
    onNavigate: (NavigationDirection) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.app_name))
        },
        navigationIcon = {
            IconButton(onClick = { onNavigate(NavigationDirection.UP) }) {
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
                    onNavigate(NavigationDirection.TO_LICENSE)
                }
                SettingsMenuItem(R.string.options_menu_source_code) {
                    expanded = false
                    onNavigate(NavigationDirection.TO_SOURCE_CODE)
                }
                SettingsMenuItem(R.string.options_menu_privacy_policy) {
                    expanded = false
                    onNavigate(NavigationDirection.TO_PRIVACY_POLICY)
                }
                SettingsMenuItem(R.string.options_menu_play_store) {
                    expanded = false
                    onNavigate(NavigationDirection.TO_PLAY_STORE)
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
        SettingsScreen(
            uiState = UiState(
                mode = Mode.TIMER,
                hourEnabled = true,
                millisecondEnabled = true,
                volume = 7,
                fullscreen = true,
                orientation = Orientation.LANDSCAPE,
                buttonOpacity = 0.72f,
            ),
            onNavigate = {},
        )
    }
}
