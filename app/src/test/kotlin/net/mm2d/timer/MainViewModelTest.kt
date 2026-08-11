/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import net.mm2d.timer.MainViewModel.UiEffect
import net.mm2d.timer.MainViewModel.UiEvent
import net.mm2d.timer.main.MainCommand
import net.mm2d.timer.main.MainLaunchRequest
import net.mm2d.timer.main.StopwatchController
import net.mm2d.timer.main.TimeProvider
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("NonAsciiCharacters")
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository: SettingsRepository = mockk()
    private val timerStateRepository: TimerRunningStateRepository = mockk()
    private val stopwatchStateRepository: StopwatchRunningStateRepository = mockk()
    private val settingsFlow = MutableStateFlow(createSettings())
    private val timeProvider = FakeTimeProvider()

    @Before
    fun setUp() {
        every { settingsRepository.flow } returns settingsFlow
        every { timerStateRepository.flow } returns flowOf(TimerRunningState(started = false))
        every { stopwatchStateRepository.flow } returns flowOf(StopwatchRunningState(started = false))
        coEvery { settingsRepository.updateMode(any()) } returns Unit
        coEvery { settingsRepository.updateTimerTime(any()) } returns Unit
        coEvery { timerStateRepository.updateState(any()) } returns Unit
        coEvery { stopwatchStateRepository.updateState(any()) } returns Unit
    }

    @Test
    fun `初期化 Settingsを単一UiStateへ反映する`() =
        runTest(mainDispatcherRule.testDispatcher) {
            settingsFlow.value = createSettings(
                mode = Mode.STOPWATCH,
                foregroundColor = 0xFF123456.toInt(),
                timerTime = 30_000L,
            )

            val viewModel = createViewModel()
            runCurrent()

            assertThat(viewModel.uiStateFlow.value.initialized).isTrue()
            assertThat(viewModel.uiStateFlow.value.mode).isEqualTo(Mode.STOPWATCH)
            assertThat(viewModel.uiStateFlow.value.foregroundColor).isEqualTo(0xFF123456.toInt())
            assertThat(viewModel.uiStateFlow.value.timerTimeMillis).isEqualTo(30_000L)
            assertThat(viewModel.uiStateFlow.value.timeMillis).isEqualTo(0L)
        }

    @Test
    fun `onEvent ストップウォッチを開始して停止する`() =
        runTest(mainDispatcherRule.testDispatcher) {
            timeProvider.currentTimeMillis = 1_000L
            val viewModel = createViewModel()
            runCurrent()

            viewModel.onEvent(UiEvent.ClickFirstButton)
            runCurrent()

            assertThat(viewModel.uiStateFlow.value.started).isTrue()
            assertThat(viewModel.uiEffectFlow.first()).isEqualTo(UiEffect.PlaySound)

            timeProvider.currentTimeMillis = 2_250L
            viewModel.onEvent(UiEvent.ClickFirstButton)

            assertThat(viewModel.uiStateFlow.value.started).isFalse()
            assertThat(viewModel.uiStateFlow.value.timeMillis).isEqualTo(1_250L)
            assertThat(viewModel.uiEffectFlow.first()).isEqualTo(UiEffect.PlaySound)
        }

    @Test
    fun `onEvent タイマー完了時に停止音Effectを送る`() =
        runTest(mainDispatcherRule.testDispatcher) {
            settingsFlow.value = createSettings(mode = Mode.TIMER, timerTime = 100L)
            val viewModel = createViewModel()
            runCurrent()

            viewModel.onEvent(UiEvent.ClickFirstButton)
            runCurrent()
            assertThat(viewModel.uiEffectFlow.first()).isEqualTo(UiEffect.PlaySound)

            timeProvider.currentTimeMillis = 100L
            mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(1L)
            runCurrent()

            assertThat(viewModel.uiStateFlow.value.started).isFalse()
            assertThat(viewModel.uiStateFlow.value.timeMillis).isEqualTo(0L)
            assertThat(viewModel.uiEffectFlow.first()).isEqualTo(UiEffect.PlayStopSound)
        }

    @Test
    fun `初期化 実行中タイマーを復元して保存状態を消費する`() =
        runTest(mainDispatcherRule.testDispatcher) {
            settingsFlow.value = createSettings(mode = Mode.TIMER, timerTime = 1_000L)
            every { timerStateRepository.flow } returns flowOf(
                TimerRunningState(
                    started = true,
                    start = 100L,
                    milestone = 1_000L,
                ),
            )
            timeProvider.currentTimeMillis = 400L

            val viewModel = createViewModel()
            runCurrent()

            assertThat(viewModel.uiStateFlow.value.started).isTrue()
            assertThat(viewModel.uiStateFlow.value.timeMillis).isEqualTo(700L)
            coVerify(exactly = 1) {
                timerStateRepository.updateState(TimerRunningState(started = false))
            }
            viewModel.onEvent(UiEvent.ClickFirstButton)
        }

    @Test
    fun `onEvent 実行中状態をDataStoreへ保存する`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel()
            runCurrent()
            timeProvider.currentTimeMillis = 2_000L
            viewModel.onEvent(UiEvent.ClickFirstButton)
            runCurrent()

            viewModel.onEvent(UiEvent.PersistRunningState)
            runCurrent()

            coVerify(exactly = 1) {
                stopwatchStateRepository.updateState(
                    StopwatchRunningState(
                        started = true,
                        start = 2_000L,
                        milestone = 0L,
                    ),
                )
            }
            viewModel.onEvent(UiEvent.ClickFirstButton)
        }

    @Test
    fun `onEvent 外部要求のモードと設定開始コマンドを反映する`() =
        runTest(mainDispatcherRule.testDispatcher) {
            timeProvider.currentTimeMillis = 1_000L
            val viewModel = createViewModel()
            runCurrent()

            viewModel.onEvent(
                UiEvent.HandleLaunchRequest(
                    MainLaunchRequest(
                        mode = Mode.TIMER,
                        command = MainCommand.SetAndStart(30_000L),
                    ),
                ),
            )
            runCurrent()

            assertThat(viewModel.uiStateFlow.value.mode).isEqualTo(Mode.TIMER)
            assertThat(viewModel.uiStateFlow.value.started).isTrue()
            assertThat(viewModel.uiStateFlow.value.timeMillis).isEqualTo(30_000L)
            coVerify(exactly = 1) { settingsRepository.updateMode(Mode.TIMER) }

            viewModel.onEvent(
                UiEvent.HandleLaunchRequest(
                    MainLaunchRequest(
                        mode = Mode.TIMER,
                        command = MainCommand.Stop,
                    ),
                ),
            )
            assertThat(viewModel.uiStateFlow.value.started).isFalse()
        }

    private fun createViewModel(): MainViewModel =
        MainViewModel(
            settingsRepository = settingsRepository,
            timerStateRepository = timerStateRepository,
            stopwatchStateRepository = stopwatchStateRepository,
            timerController = TimerController(timeProvider),
            stopwatchController = StopwatchController(timeProvider),
            timeProvider = timeProvider,
        )

    private class FakeTimeProvider : TimeProvider {
        var currentTimeMillis: Long = 0L

        override fun currentTimeMillis(): Long = currentTimeMillis
    }

    private companion object {
        fun createSettings(
            mode: Mode = Mode.STOPWATCH,
            foregroundColor: Int = 0xFFFFFFFF.toInt(),
            timerTime: Long = 60_000L,
        ): Settings =
            Settings(
                versionAtInstall = 1,
                versionAtLastLaunched = 1,
                versionBeforeUpdate = 0,
                mode = mode,
                foregroundColor = foregroundColor,
                backgroundColor = 0xFF000000.toInt(),
                hourEnabled = false,
                hourFormat24 = true,
                millisecondEnabled = true,
                secondEnabled = true,
                timerTime = timerTime,
                soundVolume = 10,
                fullscreen = true,
                font = Font.LED_7SEGMENT,
                orientation = Orientation.UNSPECIFIED,
                buttonOpacity = 1f,
            )
    }
}
