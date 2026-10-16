/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.repeatOnLifecycle
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import net.mm2d.timer.ColorSelectViewModel.UiEffect
import net.mm2d.timer.ColorSelectViewModel.UiEvent
import net.mm2d.timer.settings.Font
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.settings.Orientation
import net.mm2d.timer.settings.Settings
import net.mm2d.timer.settings.SettingsRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("NonAsciiCharacters")
class ColorSelectViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)

    private val testSettings = Settings(
        versionAtInstall = 1,
        versionAtLastLaunched = 1,
        versionBeforeUpdate = 0,
        mode = Mode.CLOCK,
        foregroundColor = 0xFF111111.toInt(),
        backgroundColor = 0xFF222222.toInt(),
        hourEnabled = false,
        hourFormat24 = true,
        millisecondEnabled = true,
        secondEnabled = true,
        timerTime = SettingsRepository.TIMER_TIME_DEFAULT,
        soundVolume = 10,
        fullscreen = true,
        font = Font.LED_7SEGMENT,
        orientation = Orientation.UNSPECIFIED,
        buttonOpacity = 1f,
    )

    private lateinit var viewModel: ColorSelectViewModel

    @Before
    fun setUp() {
        every { settingsRepository.flow } returns flowOf(testSettings)
        viewModel = ColorSelectViewModel(
            settingsRepository = settingsRepository,
        )
    }

    @Test
    fun `Load FOREGROUND時に表示色が読み込まれること`() =
        runTest {
            viewModel.onEvent(UiEvent.Load(ColorTarget.FOREGROUND))

            val state = viewModel.getUiStateStream().value
            assertThat(state.initialColor).isEqualTo(Color(0xFF111111))
        }

    @Test
    fun `Load BACKGROUND時に背景色が読み込まれること`() =
        runTest {
            viewModel.onEvent(UiEvent.Load(ColorTarget.BACKGROUND))

            val state = viewModel.getUiStateStream().value
            assertThat(state.initialColor).isEqualTo(Color(0xFF222222))
        }

    @Test
    fun `Load 複数回呼び出されても再読み込みされないこと`() =
        runTest {
            viewModel.onEvent(UiEvent.Load(ColorTarget.FOREGROUND))
            viewModel.onEvent(UiEvent.Load(ColorTarget.BACKGROUND))

            val state = viewModel.getUiStateStream().value
            assertThat(state.initialColor).isEqualTo(Color(0xFF111111))
        }

    @Test
    fun `ClickBack GoBackイベントが発行されリポジトリが更新されないこと`() =
        runTest {
            viewModel.onEvent(UiEvent.ClickBack)

            val effect = viewModel.getUiEffectStream().first()
            assertThat(effect).isEqualTo(UiEffect.GoBack)
            coVerify(exactly = 0) { settingsRepository.updateForegroundColor(any()) }
            coVerify(exactly = 0) { settingsRepository.updateBackgroundColor(any()) }
        }

    @Test
    fun `ClickSave FOREGROUND時に表示色が更新されGoBackが発行されること`() =
        runTest {
            val newColor = Color(0xFFAAAAAA)
            viewModel.onEvent(UiEvent.Load(ColorTarget.FOREGROUND))
            viewModel.onEvent(UiEvent.ChangeColor(newColor))
            viewModel.onEvent(UiEvent.ClickSave(ColorTarget.FOREGROUND))

            coVerify { settingsRepository.updateForegroundColor(newColor.toArgb()) }
            val effect = viewModel.getUiEffectStream().first()
            assertThat(effect).isEqualTo(UiEffect.GoBack)
        }

    @Test
    fun `ClickSave BACKGROUND時に背景色が更新されGoBackが発行されること`() =
        runTest {
            val newColor = Color(0xFFBBBBBB)
            viewModel.onEvent(UiEvent.Load(ColorTarget.BACKGROUND))
            viewModel.onEvent(UiEvent.ChangeColor(newColor))
            viewModel.onEvent(UiEvent.ClickSave(ColorTarget.BACKGROUND))

            coVerify { settingsRepository.updateBackgroundColor(newColor.toArgb()) }
            val effect = viewModel.getUiEffectStream().first()
            assertThat(effect).isEqualTo(UiEffect.GoBack)
        }

    @Test
    fun `ClickSave 色変更がない場合は初期色が保存されること`() =
        runTest {
            viewModel.onEvent(UiEvent.Load(ColorTarget.FOREGROUND))
            viewModel.onEvent(UiEvent.ClickSave(ColorTarget.FOREGROUND))

            coVerify { settingsRepository.updateForegroundColor(Color(0xFF111111).toArgb()) }
            val effect = viewModel.getUiEffectStream().first()
            assertThat(effect).isEqualTo(UiEffect.GoBack)
        }

    @Test
    fun `ClickSave 一時停止中の保存完了は再開後に一度だけ受信できること`() =
        runTest {
            val owner = object : LifecycleOwner {
                override val lifecycle = LifecycleRegistry.createUnsafe(this)
            }
            owner.lifecycle.currentState = Lifecycle.State.RESUMED
            val received = mutableListOf<UiEffect>()
            backgroundScope.launch(mainDispatcherRule.testDispatcher) {
                owner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.getUiEffectStream().collect { received.add(it) }
                }
            }
            val saveCompleted = CompletableDeferred<Unit>()
            coEvery {
                settingsRepository.updateForegroundColor(any())
            } coAnswers { saveCompleted.await() }

            viewModel.onEvent(UiEvent.Load(ColorTarget.FOREGROUND))
            viewModel.onEvent(UiEvent.ClickSave(ColorTarget.FOREGROUND))
            owner.lifecycle.currentState = Lifecycle.State.STARTED
            saveCompleted.complete(Unit)

            assertThat(received).isEmpty()

            owner.lifecycle.currentState = Lifecycle.State.RESUMED

            assertThat(received).containsExactly(UiEffect.GoBack)

            owner.lifecycle.currentState = Lifecycle.State.STARTED
            owner.lifecycle.currentState = Lifecycle.State.RESUMED

            assertThat(received).containsExactly(UiEffect.GoBack)
            owner.lifecycle.currentState = Lifecycle.State.DESTROYED
        }
}
