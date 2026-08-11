/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import com.google.common.truth.Truth.assertThat
import net.mm2d.timer.MainViewModel.Button
import net.mm2d.timer.MainViewModel.UiState
import net.mm2d.timer.settings.Mode
import org.junit.Test

@Suppress("NonAsciiCharacters")
class MainViewModelUiStateTest {
    @Test
    fun `firstButton 時計モードでは非表示を返す`() {
        val state = UiState(mode = Mode.CLOCK)

        assertThat(state.firstButton).isEqualTo(Button.HIDDEN)
        assertThat(state.secondButton).isEqualTo(Button.HIDDEN)
        assertThat(state.keepScreenOn).isTrue()
    }

    @Test
    fun `firstButton ストップウォッチ実行中は一時停止を返す`() {
        val state = UiState(mode = Mode.STOPWATCH, started = true)

        assertThat(state.firstButton).isEqualTo(Button.PAUSE)
        assertThat(state.secondButton).isEqualTo(Button.RESET)
        assertThat(state.keepScreenOn).isTrue()
    }

    @Test
    fun `secondButton タイマーモードでは時間設定を返す`() {
        val state = UiState(mode = Mode.TIMER)

        assertThat(state.firstButton).isEqualTo(Button.START)
        assertThat(state.secondButton).isEqualTo(Button.TIMER)
        assertThat(state.keepScreenOn).isFalse()
    }
}
