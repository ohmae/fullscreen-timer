/*
 * Copyright (c) 2022 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.mm2d.timer.main.MainCommand
import net.mm2d.timer.main.MainLaunchRequest
import net.mm2d.timer.main.StopwatchController
import net.mm2d.timer.main.TimeProvider
import net.mm2d.timer.main.TimeUpdate
import net.mm2d.timer.main.TimerController
import net.mm2d.timer.settings.Font
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.Orientation
import net.mm2d.timer.settings.Settings
import net.mm2d.timer.settings.SettingsRepository
import net.mm2d.timer.settings.StopwatchRunningState
import net.mm2d.timer.settings.StopwatchRunningStateRepository
import net.mm2d.timer.settings.TimerRunningState
import net.mm2d.timer.settings.TimerRunningStateRepository
import net.mm2d.timer.util.shouldUseDarkForeground
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val timerStateRepository: TimerRunningStateRepository,
    private val stopwatchStateRepository: StopwatchRunningStateRepository,
    private val timerController: TimerController,
    private val stopwatchController: StopwatchController,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(UiState())
    val uiStateFlow: StateFlow<UiState> = mutableUiState.asStateFlow()

    private val uiEffectChannel = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffectFlow: Flow<UiEffect> = uiEffectChannel.receiveAsFlow()

    private var currentSettings: Settings? = null
    private var pendingLaunchRequest: MainLaunchRequest? = null
    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            observeSettingsAndRestoreState()
        }
    }

    fun onEvent(
        event: UiEvent,
    ) {
        when (event) {
            UiEvent.ClickFirstButton -> onClickFirstButton()
            UiEvent.ClickSecondButton -> onClickSecondButton()
            UiEvent.ClickTime -> onClickTime()
            UiEvent.ClickSettings -> emitEffect(UiEffect.OpenSettings)
            is UiEvent.SelectTimerTime -> selectTimerTime(event.timeMillis)
            is UiEvent.HandleLaunchRequest -> handleLaunchRequest(event.request)
            UiEvent.PersistRunningState -> persistRunningState()
        }
    }

    private suspend fun observeSettingsAndRestoreState() {
        val timerRunningState = timerStateRepository.flow.first()
        val stopwatchRunningState = stopwatchStateRepository.flow.first()
        var initialized = false
        settingsRepository.flow.collect { settings ->
            if (initialized) {
                applySettings(settings)
            } else {
                initialized = true
                initialize(settings, timerRunningState, stopwatchRunningState)
            }
        }
    }

    private suspend fun initialize(
        settings: Settings,
        timerRunningState: TimerRunningState,
        stopwatchRunningState: StopwatchRunningState,
    ) {
        currentSettings = settings
        stopwatchController.setHourEnabled(settings.hourEnabled)
        mutableUiState.value = settings.toUiState(initialized = true)
        activateMode(settings.mode)

        if (timerRunningState.started) {
            timerStateRepository.updateState(TimerRunningState(started = false))
        }
        if (stopwatchRunningState.started) {
            stopwatchStateRepository.updateState(StopwatchRunningState(started = false))
        }
        restoreRunningState(settings.mode, timerRunningState, stopwatchRunningState)

        pendingLaunchRequest?.let(::handleLaunchRequest)
        pendingLaunchRequest = null
    }

    private fun applySettings(
        settings: Settings,
    ) {
        val previousSettings = currentSettings
        currentSettings = settings
        stopwatchController.setHourEnabled(settings.hourEnabled)
        mutableUiState.update { state -> settings.toUiState(state = state) }

        if (settings.mode != mutableUiState.value.mode) {
            activateMode(settings.mode)
        } else if (
            settings.mode == Mode.TIMER &&
            previousSettings?.timerTime != settings.timerTime
        ) {
            stopTicker()
            timerController.deactivate()
            timerController.setTime(settings.timerTime)
            updateTimeState(timeMillis = settings.timerTime, started = false)
        }
    }

    private fun restoreRunningState(
        mode: Mode,
        timerRunningState: TimerRunningState,
        stopwatchRunningState: StopwatchRunningState,
    ) {
        when (mode) {
            Mode.CLOCK -> Unit

            Mode.STOPWATCH -> {
                if (stopwatchController.restore(stopwatchRunningState)) {
                    updateTimeState(stopwatchController.timeMillis, started = true)
                    startTicker()
                }
            }

            Mode.TIMER -> {
                if (timerController.restore(timerRunningState)) {
                    updateTimeState(timerController.timeMillis, started = true)
                    startTicker()
                }
            }
        }
    }

    private fun activateMode(
        mode: Mode,
    ) {
        stopTicker()
        timerController.deactivate()
        stopwatchController.deactivate()
        val timeMillis = when (mode) {
            Mode.CLOCK -> timeProvider.currentTimeMillis()

            Mode.STOPWATCH -> stopwatchController.reset()

            Mode.TIMER -> {
                val timerTimeMillis = currentSettings?.timerTime ?: mutableUiState.value.timerTimeMillis
                timerController.setTime(timerTimeMillis)
                timerTimeMillis
            }
        }
        mutableUiState.update {
            it.copy(
                mode = mode,
                timeMillis = timeMillis,
                started = false,
            )
        }
        if (mode == Mode.CLOCK) startTicker()
    }

    private fun onClickFirstButton() {
        if (!mutableUiState.value.initialized) return
        when (mutableUiState.value.mode) {
            Mode.CLOCK -> Unit

            Mode.STOPWATCH -> {
                if (stopwatchController.started) stopStopwatch() else startStopwatch()
                emitEffect(UiEffect.PlaySound)
            }

            Mode.TIMER -> {
                when {
                    timerController.started -> {
                        stopTimer()
                        emitEffect(UiEffect.PlaySound)
                    }

                    timerController.timeMillis != 0L -> {
                        startTimer()
                        emitEffect(UiEffect.PlaySound)
                    }

                    else -> showTimerDialog()
                }
            }
        }
    }

    private fun onClickSecondButton() {
        if (!mutableUiState.value.initialized) return
        when (mutableUiState.value.mode) {
            Mode.CLOCK -> Unit

            Mode.STOPWATCH -> {
                stopTicker()
                updateTimeState(stopwatchController.reset(), started = false)
                emitEffect(UiEffect.PlaySound)
            }

            Mode.TIMER -> {
                if (timerController.started) {
                    stopTimer()
                    emitEffect(UiEffect.PlaySound)
                }
                showTimerDialog()
            }
        }
    }

    private fun onClickTime() {
        when (mutableUiState.value.mode) {
            Mode.CLOCK -> Unit

            Mode.STOPWATCH -> onClickFirstButton()

            Mode.TIMER -> {
                if (timerController.timeMillis == 0L) {
                    onClickSecondButton()
                } else {
                    onClickFirstButton()
                }
            }
        }
    }

    private fun selectTimerTime(
        timeMillis: Long,
    ) {
        mutableUiState.update { it.copy(timerTimeMillis = timeMillis) }
        if (mutableUiState.value.mode == Mode.TIMER) {
            stopTicker()
            timerController.deactivate()
            timerController.setTime(timeMillis)
            updateTimeState(timeMillis, started = false)
        }
        viewModelScope.launch {
            settingsRepository.updateTimerTime(timeMillis)
        }
    }

    private fun handleLaunchRequest(
        request: MainLaunchRequest,
    ) {
        if (!mutableUiState.value.initialized) {
            pendingLaunchRequest = request
            return
        }
        if (request.mode != mutableUiState.value.mode) activateMode(request.mode)
        viewModelScope.launch {
            settingsRepository.updateMode(request.mode)
        }
        request.command?.let { handleCommand(request.mode, it) }
    }

    private fun handleCommand(
        mode: Mode,
        command: MainCommand,
    ) {
        when (mode) {
            Mode.CLOCK -> Unit
            Mode.STOPWATCH -> handleStopwatchCommand(command)
            Mode.TIMER -> handleTimerCommand(command)
        }
    }

    private fun handleStopwatchCommand(
        command: MainCommand,
    ) {
        when (command) {
            MainCommand.Start -> if (!stopwatchController.started) startStopwatch()

            MainCommand.Stop -> if (stopwatchController.started) stopStopwatch()

            is MainCommand.Set -> {
                if (stopwatchController.started) stopStopwatch()
                stopwatchController.setTime(command.timeMillis)
                updateTimeState(command.timeMillis, started = false)
            }

            is MainCommand.SetAndStart -> {
                stopwatchController.setAndStart(command.timeMillis)
                updateTimeState(command.timeMillis, started = true)
                startTicker()
            }
        }
    }

    private fun handleTimerCommand(
        command: MainCommand,
    ) {
        when (command) {
            MainCommand.Start -> if (!timerController.started) startTimer()

            MainCommand.Stop -> if (timerController.started) stopTimer()

            is MainCommand.Set -> {
                if (timerController.started) stopTimer()
                timerController.setTime(command.timeMillis)
                updateTimeState(command.timeMillis, started = false)
            }

            is MainCommand.SetAndStart -> {
                timerController.setAndStart(command.timeMillis)
                updateTimeState(command.timeMillis, started = true)
                startTicker()
            }
        }
    }

    private fun startStopwatch() {
        stopwatchController.start()
        updateTimeState(stopwatchController.timeMillis, started = true)
        startTicker()
    }

    private fun stopStopwatch() {
        stopTicker()
        updateTimeState(stopwatchController.stop(), started = false)
    }

    private fun startTimer() {
        timerController.start()
        updateTimeState(timerController.timeMillis, started = true)
        startTicker()
    }

    private fun stopTimer() {
        stopTicker()
        updateTimeState(timerController.stop(), started = false)
    }

    private fun startTicker() {
        stopTicker()
        tickerJob = viewModelScope.launch {
            while (true) {
                val nextDelayMillis = updateTick() ?: break
                delay(nextDelayMillis.coerceAtLeast(MINIMUM_DELAY_MILLIS))
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun updateTick(): Long? =
        when (mutableUiState.value.mode) {
            Mode.CLOCK -> {
                val timeMillis = timeProvider.currentTimeMillis()
                updateTimeState(timeMillis, started = false)
                CLOCK_INTERVAL_MILLIS - timeMillis % CLOCK_INTERVAL_MILLIS
            }

            Mode.STOPWATCH -> updateStopwatchTick()

            Mode.TIMER -> updateTimerTick()
        }

    private fun updateStopwatchTick(): Long? =
        when (val update = stopwatchController.tick()) {
            is TimeUpdate.Running -> {
                updateTimeState(update.timeMillis, started = true)
                update.nextDelayMillis
            }

            is TimeUpdate.Finished -> {
                updateTimeState(update.timeMillis, started = false)
                null
            }
        }

    private fun updateTimerTick(): Long? =
        when (val update = timerController.tick()) {
            is TimeUpdate.Running -> {
                updateTimeState(update.timeMillis, started = true)
                update.nextDelayMillis
            }

            is TimeUpdate.Finished -> {
                updateTimeState(update.timeMillis, started = false)
                emitEffect(UiEffect.PlayStopSound)
                null
            }
        }

    private fun updateTimeState(
        timeMillis: Long,
        started: Boolean,
    ) {
        mutableUiState.update {
            it.copy(
                timeMillis = timeMillis,
                started = started,
            )
        }
    }

    private fun showTimerDialog() {
        emitEffect(
            UiEffect.ShowTimerDialog(
                timeMillis = mutableUiState.value.timerTimeMillis,
                hourEnabled = mutableUiState.value.hourEnabled,
            ),
        )
    }

    private fun persistRunningState() {
        when (mutableUiState.value.mode) {
            Mode.CLOCK -> Unit

            Mode.STOPWATCH -> {
                if (!stopwatchController.started) return
                viewModelScope.launch {
                    stopwatchStateRepository.updateState(stopwatchController.createRunningState())
                }
            }

            Mode.TIMER -> {
                if (!timerController.started) return
                viewModelScope.launch {
                    timerStateRepository.updateState(timerController.createRunningState())
                }
            }
        }
    }

    private fun emitEffect(
        effect: UiEffect,
    ) {
        uiEffectChannel.trySend(effect)
    }

    private fun Settings.toUiState(
        initialized: Boolean,
    ): UiState =
        UiState(
            initialized = initialized,
            mode = mode,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor,
            shouldUseDarkForeground = backgroundColor.shouldUseDarkForeground(),
            fullscreen = fullscreen,
            buttonOpacity = buttonOpacity,
            font = font,
            orientation = orientation,
            hourEnabled = hourEnabled,
            hourFormat24 = hourFormat24,
            millisecondEnabled = millisecondEnabled,
            secondEnabled = secondEnabled,
            timerTimeMillis = timerTime,
        )

    private fun Settings.toUiState(
        state: UiState,
    ): UiState =
        state.copy(
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor,
            shouldUseDarkForeground = backgroundColor.shouldUseDarkForeground(),
            fullscreen = fullscreen,
            buttonOpacity = buttonOpacity,
            font = font,
            orientation = orientation,
            hourEnabled = hourEnabled,
            hourFormat24 = hourFormat24,
            millisecondEnabled = millisecondEnabled,
            secondEnabled = secondEnabled,
            timerTimeMillis = timerTime,
        )

    data class UiState(
        val initialized: Boolean = false,
        val mode: Mode = Mode.STOPWATCH,
        val timeMillis: Long = 0L,
        val started: Boolean = false,
        val foregroundColor: Int = Color.WHITE,
        val backgroundColor: Int = Color.BLACK,
        val shouldUseDarkForeground: Boolean = false,
        val fullscreen: Boolean = true,
        val buttonOpacity: Float = 1f,
        val font: Font = Font.LED_7SEGMENT,
        val orientation: Orientation = Orientation.UNSPECIFIED,
        val hourEnabled: Boolean = false,
        val hourFormat24: Boolean = true,
        val millisecondEnabled: Boolean = true,
        val secondEnabled: Boolean = true,
        val timerTimeMillis: Long = SettingsRepository.TIMER_TIME_DEFAULT,
    ) {
        val firstButton: Button
            get() = when (mode) {
                Mode.CLOCK -> Button.HIDDEN

                Mode.STOPWATCH,
                Mode.TIMER,
                    -> if (started) Button.PAUSE else Button.START
            }

        val secondButton: Button
            get() = when (mode) {
                Mode.CLOCK -> Button.HIDDEN
                Mode.STOPWATCH -> Button.RESET
                Mode.TIMER -> Button.TIMER
            }

        val keepScreenOn: Boolean
            get() = mode == Mode.CLOCK || started
    }

    enum class Button {
        HIDDEN,
        START,
        PAUSE,
        RESET,
        TIMER,
        SETTINGS,
    }

    sealed interface UiEvent {
        data object ClickFirstButton : UiEvent

        data object ClickSecondButton : UiEvent

        data object ClickTime : UiEvent

        data object ClickSettings : UiEvent

        data class SelectTimerTime(
            val timeMillis: Long,
        ) : UiEvent

        data class HandleLaunchRequest(
            val request: MainLaunchRequest,
        ) : UiEvent

        data object PersistRunningState : UiEvent
    }

    sealed interface UiEffect {
        data object OpenSettings : UiEffect

        data class ShowTimerDialog(
            val timeMillis: Long,
            val hourEnabled: Boolean,
        ) : UiEffect

        data object PlaySound : UiEffect

        data object PlayStopSound : UiEffect
    }

    private companion object {
        const val CLOCK_INTERVAL_MILLIS = 1_000L
        const val MINIMUM_DELAY_MILLIS = 1L
    }
}
