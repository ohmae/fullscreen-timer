/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.Orientation
import net.mm2d.timer.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiStateFlow: Flow<UiState> = settingsRepository.flow
        .map {
            UiState(
                mode = it.mode,
                foregroundColor = it.foregroundColor,
                backgroundColor = it.backgroundColor,
                hourEnabled = it.hourEnabled,
                hourFormat24 = it.hourFormat24,
                volume = it.soundVolume,
                fullscreen = it.fullscreen,
                orientation = it.orientation,
                buttonOpacity = it.buttonOpacity,
            )
        }
        .distinctUntilChanged()

    data class UiState(
        val mode: Mode,
        val foregroundColor: Int,
        val backgroundColor: Int,
        val hourEnabled: Boolean,
        val hourFormat24: Boolean,
        val volume: Int,
        val fullscreen: Boolean,
        val orientation: Orientation,
        val buttonOpacity: Float,
    )

    fun updateMode(
        mode: Mode,
    ) {
        viewModelScope.launch {
            settingsRepository.updateMode(mode)
        }
    }

    fun updateForegroundColor(
        color: Int,
    ) {
        viewModelScope.launch {
            settingsRepository.updateForegroundColor(color)
        }
    }

    fun updateBackgroundColor(
        color: Int,
    ) {
        viewModelScope.launch {
            settingsRepository.updateBackgroundColor(color)
        }
    }

    fun updateHourEnabled(
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.updateHourEnabled(enabled)
        }
    }

    fun updateHourFormat24(
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.updateHourFormat24(enabled)
        }
    }

    fun updateVolume(
        volume: Int,
    ) {
        viewModelScope.launch {
            settingsRepository.updateVolume(volume)
        }
    }

    fun updateFullscreen(
        fullscreen: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.updateFullscreen(fullscreen)
        }
    }

    fun updateOrientation(
        orientation: Orientation,
    ) {
        viewModelScope.launch {
            settingsRepository.updateOrientation(orientation)
        }
    }

    fun updateButtonOpacity(
        opacity: Float,
    ) {
        viewModelScope.launch {
            settingsRepository.updateButtonOpacity(opacity)
        }
    }
}
