/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.mm2d.timer.settings.Font
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.Orientation
import net.mm2d.timer.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiStateFlow: StateFlow<UiState> = settingsRepository.flow
        .map {
            UiState(
                mode = it.mode,
                foregroundColor = it.foregroundColor,
                backgroundColor = it.backgroundColor,
                hourEnabled = it.hourEnabled,
                hourFormat24 = it.hourFormat24,
                millisecondEnabled = it.millisecondEnabled,
                secondEnabled = it.secondEnabled,
                volume = it.soundVolume,
                fullscreen = it.fullscreen,
                font = it.font,
                orientation = it.orientation,
                buttonOpacity = it.buttonOpacity,
                onModeSelected = ::updateMode,
                onForegroundColorRequest = ::requestForegroundColorDialog,
                onBackgroundColorRequest = ::requestBackgroundColorDialog,
                onButtonOpacityChange = ::updateButtonOpacity,
                onVolumeChange = ::updateVolume,
                onHourEnabledChange = ::updateHourEnabled,
                onHourFormatChange = ::updateHourFormat24,
                onMillisecondEnabledChange = ::updateMillisecondEnabled,
                onSecondEnabledChange = ::updateSecondEnabled,
                onFullscreenChange = ::updateFullscreen,
                onFontRequest = ::requestFontDialog,
                onOrientationRequest = ::requestOrientationDialog,
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(),
        )

    data class UiState(
        val mode: Mode = Mode.CLOCK,
        val foregroundColor: Int = android.graphics.Color.WHITE,
        val backgroundColor: Int = android.graphics.Color.BLACK,
        val hourEnabled: Boolean = false,
        val hourFormat24: Boolean = true,
        val millisecondEnabled: Boolean = true,
        val secondEnabled: Boolean = true,
        val volume: Int = 10,
        val fullscreen: Boolean = true,
        val font: Font = Font.LED_7SEGMENT,
        val orientation: Orientation = Orientation.UNSPECIFIED,
        val buttonOpacity: Float = 1f,
        val onModeSelected: (Mode) -> Unit = {},
        val onForegroundColorRequest: (Color) -> Unit = {},
        val onBackgroundColorRequest: (Color) -> Unit = {},
        val onButtonOpacityChange: (Float) -> Unit = {},
        val onVolumeChange: (Int) -> Unit = {},
        val onHourEnabledChange: (Boolean) -> Unit = {},
        val onHourFormatChange: (Boolean) -> Unit = {},
        val onMillisecondEnabledChange: (Boolean) -> Unit = {},
        val onSecondEnabledChange: (Boolean) -> Unit = {},
        val onFullscreenChange: (Boolean) -> Unit = {},
        val onFontRequest: (Font) -> Unit = {},
        val onOrientationRequest: (Orientation) -> Unit = {},
    )

    private fun updateMode(
        mode: Mode,
    ) {
        viewModelScope.launch {
            settingsRepository.updateMode(mode)
        }
    }

    private fun updateForegroundColor(
        color: Int,
    ) {
        viewModelScope.launch {
            settingsRepository.updateForegroundColor(color)
        }
    }

    private fun updateBackgroundColor(
        color: Int,
    ) {
        viewModelScope.launch {
            settingsRepository.updateBackgroundColor(color)
        }
    }

    private fun updateHourEnabled(
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.updateHourEnabled(enabled)
        }
    }

    private fun updateHourFormat24(
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.updateHourFormat24(enabled)
        }
    }

    private fun updateMillisecondEnabled(
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.updateMillisecondEnabled(enabled)
        }
    }

    private fun updateSecondEnabled(
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.updateSecondEnabled(enabled)
        }
    }

    private fun updateVolume(
        volume: Int,
    ) {
        viewModelScope.launch {
            settingsRepository.updateVolume(volume)
        }
    }

    private fun updateFullscreen(
        fullscreen: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.updateFullscreen(fullscreen)
        }
    }

    private fun updateFont(
        font: Font,
    ) {
        viewModelScope.launch {
            settingsRepository.updateFont(font)
        }
    }

    private fun updateOrientation(
        orientation: Orientation,
    ) {
        viewModelScope.launch {
            settingsRepository.updateOrientation(orientation)
        }
    }

    private fun updateButtonOpacity(
        opacity: Float,
    ) {
        viewModelScope.launch {
            settingsRepository.updateButtonOpacity(opacity)
        }
    }

    private val dialogRequestFlow: MutableStateFlow<DialogRequest> = MutableStateFlow(DialogRequest.Dismiss)

    fun getDialogRequestStream(): StateFlow<DialogRequest> = dialogRequestFlow

    private fun requestDialog(
        request: DialogRequest,
    ) {
        dialogRequestFlow.value = request
    }

    private fun dismissDialog() {
        requestDialog(DialogRequest.Dismiss)
    }

    private fun requestForegroundColorDialog(
        color: Color,
    ) {
        requestDialog(
            DialogRequest.ForegroundColorSelect(
                color = color,
                onChooseColor = {
                    updateForegroundColor(it.toArgb())
                    dismissDialog()
                },
                dismissRequest = ::dismissDialog,
            ),
        )
    }

    private fun requestBackgroundColorDialog(
        color: Color,
    ) {
        requestDialog(
            DialogRequest.BackgroundColorSelect(
                color = color,
                onChooseColor = {
                    updateBackgroundColor(it.toArgb())
                    dismissDialog()
                },
                dismissRequest = ::dismissDialog,
            ),
        )
    }

    private fun requestFontDialog(
        font: Font,
    ) {
        requestDialog(
            DialogRequest.FontSelect(
                font = font,
                onChooseFont = {
                    updateFont(it)
                    dismissDialog()
                },
                dismissRequest = ::dismissDialog,
            ),
        )
    }

    private fun requestOrientationDialog(
        orientation: Orientation,
    ) {
        requestDialog(
            DialogRequest.OrientationSelect(
                orientation = orientation,
                onChooseOrientation = {
                    updateOrientation(it)
                    dismissDialog()
                },
                dismissRequest = ::dismissDialog,
            ),
        )
    }

    sealed interface DialogRequest {
        data object Dismiss : DialogRequest

        data class ForegroundColorSelect(
            val color: Color,
            val onChooseColor: (Color) -> Unit,
            val dismissRequest: () -> Unit,
        ) : DialogRequest

        data class BackgroundColorSelect(
            val color: Color,
            val onChooseColor: (Color) -> Unit,
            val dismissRequest: () -> Unit,
        ) : DialogRequest

        data class FontSelect(
            val font: Font,
            val onChooseFont: (Font) -> Unit,
            val dismissRequest: () -> Unit,
        ) : DialogRequest

        data class OrientationSelect(
            val orientation: Orientation,
            val onChooseOrientation: (Orientation) -> Unit,
            val dismissRequest: () -> Unit,
        ) : DialogRequest
    }
}
