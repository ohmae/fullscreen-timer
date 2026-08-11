/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import net.mm2d.timer.MainViewModel.UiEvent
import net.mm2d.timer.MainViewModel.UiState
import net.mm2d.timer.settings.Mode
import net.mm2d.timer.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@Suppress("NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w800dp-h360dp-land")
class MainScreenTest {
    @get:Rule
    val composeRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun `表示 ストップウォッチ停止中の時刻と操作ボタンを表示する`() {
        setContent(
            UiState(
                initialized = true,
                mode = Mode.STOPWATCH,
                timeMillis = 83_456L,
                shouldUseDarkForeground = true,
            ),
        )

        composeRule.onNodeWithContentDescription("01:23.45").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Start")
            .assertIsDisplayed()
            .assertWidthIsEqualTo(80.dp)
            .assertHeightIsEqualTo(40.dp)
        composeRule.onNodeWithContentDescription("Reset").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Open settings").assertIsDisplayed()
    }

    @Test
    fun `表示 時計モードでは設定ボタンだけを表示する`() {
        setContent(
            UiState(
                initialized = true,
                mode = Mode.CLOCK,
                timeMillis = 0L,
                shouldUseDarkForeground = true,
            ),
        )

        composeRule.onAllNodesWithContentDescription("Start").assertCountEquals(0)
        composeRule.onAllNodesWithContentDescription("Reset").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Open settings")
            .assertIsDisplayed()
            .assertLeftPositionInRootIsEqualTo(704.dp)
    }

    @Test
    fun `表示 時計モードの12時間表記と秒を表示する`() {
        setContent(
            UiState(
                initialized = true,
                mode = Mode.CLOCK,
                timeMillis = clockTimeMillis(hour = 13, minute = 24, second = 56),
                hourFormat24 = false,
                secondEnabled = true,
                shouldUseDarkForeground = true,
            ),
        )

        composeRule.onNodeWithContentDescription("01:24:56 PM").assertIsDisplayed()
    }

    @Test
    fun `表示 時計モードで秒を無効にすると時分だけを表示する`() {
        setContent(
            UiState(
                initialized = true,
                mode = Mode.CLOCK,
                timeMillis = clockTimeMillis(hour = 13, minute = 24, second = 56),
                hourFormat24 = true,
                secondEnabled = false,
                shouldUseDarkForeground = true,
            ),
        )

        composeRule.onNodeWithContentDescription("13:24").assertIsDisplayed()
    }

    @Test
    fun `表示 実行中タイマーの時間表示と操作ボタンを表示する`() {
        setContent(
            UiState(
                initialized = true,
                mode = Mode.TIMER,
                timeMillis = 3_661_230L,
                started = true,
                hourEnabled = true,
                shouldUseDarkForeground = true,
            ),
        )

        composeRule.onNodeWithContentDescription("1:01:01.23").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Pause").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Set timer").assertIsDisplayed()
    }

    @Test
    fun `表示 小数秒を無効にすると小数部を表示しない`() {
        setContent(
            UiState(
                initialized = true,
                mode = Mode.STOPWATCH,
                timeMillis = 83_456L,
                millisecondEnabled = false,
                shouldUseDarkForeground = true,
            ),
        )

        composeRule.onNodeWithContentDescription("01:23").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("01:23.45").assertCountEquals(0)
    }

    @Test
    fun `操作 ボタンと時刻タップをUiEventとして通知する`() {
        val events = mutableListOf<UiEvent>()
        setContent(
            uiState = UiState(
                initialized = true,
                mode = Mode.STOPWATCH,
                timeMillis = 83_456L,
                shouldUseDarkForeground = true,
            ),
            onEvent = events::add,
        )

        composeRule.onNodeWithContentDescription("Start").performClick()
        composeRule.onNodeWithContentDescription("Reset").performClick()
        composeRule.onNodeWithContentDescription("Open settings").performClick()
        composeRule.onNodeWithContentDescription("01:23.45").performClick()

        assertThat(events).containsExactly(
            UiEvent.ClickFirstButton,
            UiEvent.ClickSecondButton,
            UiEvent.ClickSettings,
            UiEvent.ClickTime,
        ).inOrder()
    }

    private fun setContent(
        uiState: UiState,
        onEvent: (UiEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme {
                MainScreen(
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        }
    }

    private fun clockTimeMillis(
        hour: Int,
        minute: Int,
        second: Int,
    ): Long =
        Calendar.getInstance().also {
            it.set(2026, Calendar.AUGUST, 11, hour, minute, second)
            it.set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
