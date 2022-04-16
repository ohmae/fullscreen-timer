/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.SettingsRepository
import net.mm2d.timer.settings.StopwatchRunningState
import net.mm2d.timer.settings.StopwatchRunningStateRepository
import net.mm2d.timer.sound.SoundEffect
import javax.inject.Inject

@HiltViewModel
class StopwatchViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val stateRepository: StopwatchRunningStateRepository,
    private val soundEffect: SoundEffect,
) : ViewModel() {
    val uiStateLiveData: LiveData<UiState> = settingsRepository.flow
        .map {
            UiState(
                mode = it.mode,
                hourEnabled = it.hourEnabled,
            )
        }
        .distinctUntilChanged()
        .asLiveData()

    data class UiState(
        val mode: Mode,
        val hourEnabled: Boolean,
    )

    val runningStateLiveData: LiveData<StopwatchRunningState> = stateRepository.flow
        .asLiveData()

    fun updateState(state: StopwatchRunningState) {
        stateRepository.updateState(state)
    }

    fun playSound() {
        soundEffect.play()
    }
}
