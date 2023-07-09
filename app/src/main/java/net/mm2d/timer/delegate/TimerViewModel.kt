/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.delegate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.SettingsRepository
import net.mm2d.timer.settings.TimerRunningState
import net.mm2d.timer.settings.TimerRunningStateRepository
import net.mm2d.timer.sound.SoundEffect
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val stateRepository: TimerRunningStateRepository,
    private val soundEffect: SoundEffect,
) : ViewModel() {
    val uiStateFlow: Flow<UiState> = settingsRepository.flow
        .map {
            UiState(
                mode = it.mode,
                hourEnabled = it.hourEnabled,
                timerTime = it.timerTime,
            )
        }
        .distinctUntilChanged()

    data class UiState(
        val mode: Mode,
        val hourEnabled: Boolean,
        val timerTime: Long,
    )

    val runningStateFlow: Flow<TimerRunningState> = stateRepository.flow
        .shareIn(viewModelScope, SharingStarted.Lazily, 1)

    fun updateState(state: TimerRunningState) {
        viewModelScope.launch {
            stateRepository.updateState(state)
        }
    }

    fun updateTimerTime(time: Long) {
        viewModelScope.launch {
            settingsRepository.updateTimerTime(time)
        }
    }

    fun playSound() {
        soundEffect.play()
    }

    fun playStopSound() {
        soundEffect.playStop()
    }
}
