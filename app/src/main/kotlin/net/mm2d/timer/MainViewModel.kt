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
import net.mm2d.timer.util.shouldUseDarkForeground
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiStateFlow: Flow<UiState> = settingsRepository.flow
        .map {
            UiState(
                mode = it.mode,
                foregroundColor = it.foregroundColor,
                backgroundColor = it.backgroundColor,
                shouldUseDarkForeground = it.backgroundColor.shouldUseDarkForeground(),
                fullscreen = it.fullscreen,
                buttonOpacity = it.buttonOpacity,
            )
        }
        .distinctUntilChanged()

    val orientationFlow: Flow<Orientation> = settingsRepository.flow
        .map { it.orientation }.distinctUntilChanged()

    fun updateMode(
        mode: Mode,
    ) {
        viewModelScope.launch {
            settingsRepository.updateMode(mode)
        }
    }

    data class UiState(
        val mode: Mode,
        val foregroundColor: Int,
        val backgroundColor: Int,
        val shouldUseDarkForeground: Boolean,
        val fullscreen: Boolean,
        val buttonOpacity: Float,
    )
}
