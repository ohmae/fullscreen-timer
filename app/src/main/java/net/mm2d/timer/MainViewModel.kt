/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiStateLiveData: LiveData<UiState> = settingsRepository.flow
        .map {
            UiState(
                mode = it.mode,
                foregroundColor = it.foregroundColor,
                fullscreen = it.fullscreen,
            )
        }
        .distinctUntilChanged()
        .asLiveData()

    data class UiState(
        val mode: Mode,
        val foregroundColor: Int,
        val fullscreen: Boolean,
    )
}
