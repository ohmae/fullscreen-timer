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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val dialogUiStateFlow: MutableStateFlow<DialogUiState> = MutableStateFlow(DialogUiState.Dismiss)

    fun getDialogUiStateStream(): StateFlow<DialogUiState> = dialogUiStateFlow

    private fun dismissDialog() {
        dialogUiStateFlow.value = DialogUiState.Dismiss
    }

    private fun requestForegroundColorDialog(
        color: Color,
    ) {
        dialogUiStateFlow.value =
            DialogUiState.ForegroundColorSelect(
                color = color,
            )
    }

    private fun requestBackgroundColorDialog(
        color: Color,
    ) {
        dialogUiStateFlow.value =
            DialogUiState.BackgroundColorSelect(
                color = color,
            )
    }

    private fun requestFontDialog(
        font: Font,
    ) {
        dialogUiStateFlow.value =
            DialogUiState.FontSelect(
                font = font,
            )
    }

    private fun requestOrientationDialog(
        orientation: Orientation,
    ) {
        dialogUiStateFlow.value =
            DialogUiState.OrientationSelect(
                orientation = orientation,
            )
    }

    sealed interface DialogUiState {
        data object Dismiss : DialogUiState

        data class ForegroundColorSelect(
            val color: Color,
        ) : DialogUiState

        data class BackgroundColorSelect(
            val color: Color,
        ) : DialogUiState

        data class FontSelect(
            val font: Font,
        ) : DialogUiState

        data class OrientationSelect(
            val orientation: Orientation,
        ) : DialogUiState
    }

    fun onEvent(
        event: UiEvent,
    ) {
        when (event) {
            UiEvent.ClickUp ->
                uiEffectChannel.trySend(UiEffect.ToUp)

            UiEvent.ClickLicense ->
                uiEffectChannel.trySend(UiEffect.ToLicense)

            UiEvent.ClickSourceCode ->
                uiEffectChannel.trySend(UiEffect.ToSourceCode)

            UiEvent.ClickPrivacyPolicy ->
                uiEffectChannel.trySend(UiEffect.ToPrivacyPolicy)

            UiEvent.ClickPlayStore ->
                uiEffectChannel.trySend(UiEffect.ToPlayStore)

            is UiEvent.SelectForegroundColor -> {
                updateForegroundColor(event.color.toArgb())
                dismissDialog()
            }

            is UiEvent.SelectBackgroundColor -> {
                updateBackgroundColor(event.color.toArgb())
                dismissDialog()
            }

            is UiEvent.SelectFont -> {
                updateFont(event.font)
                dismissDialog()
            }

            is UiEvent.SelectOrientation -> {
                updateOrientation(event.orientation)
                dismissDialog()
            }

            UiEvent.DismissDialog -> dismissDialog()
        }
    }

    sealed interface UiEvent {
        data object ClickUp : UiEvent
        data object ClickLicense : UiEvent
        data object ClickSourceCode : UiEvent
        data object ClickPrivacyPolicy : UiEvent
        data object ClickPlayStore : UiEvent
        data class SelectForegroundColor(
            val color: Color,
        ) : UiEvent

        data class SelectBackgroundColor(
            val color: Color,
        ) : UiEvent

        data class SelectFont(
            val font: Font,
        ) : UiEvent

        data class SelectOrientation(
            val orientation: Orientation,
        ) : UiEvent

        data object DismissDialog : UiEvent
    }

    private val uiEffectChannel: Channel<UiEffect> = Channel(Channel.CONFLATED)
    fun getUiEffectStream(): Flow<UiEffect> = uiEffectChannel.receiveAsFlow()

    sealed interface UiEffect {
        data object ToUp : UiEffect
        data object ToLicense : UiEffect
        data object ToSourceCode : UiEffect
        data object ToPrivacyPolicy : UiEffect
        data object ToPlayStore : UiEffect
    }
}
