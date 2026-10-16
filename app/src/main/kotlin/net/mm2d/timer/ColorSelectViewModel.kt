/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.mm2d.timer.settings.Settings
import net.mm2d.timer.settings.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class ColorSelectViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    data class UiState(
        val initialColor: Color? = null,
    )

    sealed interface UiEvent {
        data class Load(
            val target: ColorTarget,
        ) : UiEvent

        data class ChangeColor(
            val color: Color,
        ) : UiEvent

        data class ClickSave(
            val target: ColorTarget,
        ) : UiEvent

        data object ClickBack : UiEvent
    }

    sealed interface UiEffect {
        data object GoBack : UiEffect
    }

    private val uiStateFlow = MutableStateFlow(UiState())
    fun getUiStateStream(): StateFlow<UiState> = uiStateFlow.asStateFlow()

    private val uiEffectChannel: Channel<UiEffect> = Channel(Channel.CONFLATED)
    fun getUiEffectStream(): Flow<UiEffect> = uiEffectChannel.receiveAsFlow()

    private var selectedColor: Color? = null
    private var loadedTarget: ColorTarget? = null

    fun onEvent(
        event: UiEvent,
    ) {
        when (event) {
            is UiEvent.Load -> load(event.target)
            is UiEvent.ChangeColor -> changeColor(event.color)
            is UiEvent.ClickSave -> saveColor(event.target)
            UiEvent.ClickBack -> clickBack()
        }
    }

    private fun load(
        target: ColorTarget,
    ) {
        if (loadedTarget != null) return
        loadedTarget = target
        viewModelScope.launch {
            val settings = settingsRepository.flow.first()
            val color = settings.getColor(target)
            selectedColor = color
            uiStateFlow.value = UiState(initialColor = color)
        }
    }

    private fun changeColor(
        color: Color,
    ) {
        selectedColor = color
    }

    private fun saveColor(
        target: ColorTarget,
    ) {
        val color = selectedColor ?: uiStateFlow.value.initialColor ?: return
        val chosen = color.toArgb()
        viewModelScope.launch {
            when (target) {
                ColorTarget.FOREGROUND -> settingsRepository.updateForegroundColor(chosen)
                ColorTarget.BACKGROUND -> settingsRepository.updateBackgroundColor(chosen)
            }
            uiEffectChannel.trySend(UiEffect.GoBack)
        }
    }

    private fun clickBack() {
        uiEffectChannel.trySend(UiEffect.GoBack)
    }

    private fun Settings.getColor(
        target: ColorTarget,
    ): Color =
        when (target) {
            ColorTarget.FOREGROUND -> Color(foregroundColor)
            ColorTarget.BACKGROUND -> Color(backgroundColor)
        }
}
