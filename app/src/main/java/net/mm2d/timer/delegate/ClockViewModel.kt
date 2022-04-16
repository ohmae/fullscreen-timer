package net.mm2d.timer.delegate

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
class ClockViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiStateLiveData: LiveData<UiState> = settingsRepository.flow
        .map { UiState(it.mode) }
        .distinctUntilChanged()
        .asLiveData()

    data class UiState(
        val mode: Mode,
    )
}
