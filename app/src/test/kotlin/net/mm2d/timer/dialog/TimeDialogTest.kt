/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.dialog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.google.common.truth.Truth.assertThat
import net.mm2d.timer.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TimeDialogTest {
    @get:Rule
    val composeRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun `表示 時表示なしでは分と秒の入力欄だけを表示する`() {
        setContent(hourEnabled = false)

        composeRule.onNodeWithTag("TimeDialog:hour:input").assertDoesNotExist()
        composeRule.onNodeWithTag("TimeDialog:minute:input").assertIsDisplayed()
        composeRule.onNodeWithTag("TimeDialog:second:input").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `表示 通常幅では時分秒を横に並べる`() {
        setContent(hourEnabled = true)

        val hourBounds = composeRule.onNodeWithTag("TimeDialog:hour:input").fetchSemanticsNode().boundsInRoot
        val minuteBounds = composeRule.onNodeWithTag("TimeDialog:minute:input").fetchSemanticsNode().boundsInRoot
        val secondBounds = composeRule.onNodeWithTag("TimeDialog:second:input").fetchSemanticsNode().boundsInRoot
        val increaseBounds = composeRule.onNodeWithTag("TimeDialog:hour:increase").fetchSemanticsNode().boundsInRoot

        assertThat(hourBounds.top).isEqualTo(minuteBounds.top)
        assertThat(minuteBounds.top).isEqualTo(secondBounds.top)
        assertThat(hourBounds.left).isLessThan(minuteBounds.left)
        assertThat(minuteBounds.left).isLessThan(secondBounds.left)
        assertThat(increaseBounds.top).isAtLeast(hourBounds.bottom)
    }

    @Test
    @Config(qualifiers = "w320dp-h800dp")
    fun `表示 狭い幅では時分秒を縦に並べる`() {
        setContent(hourEnabled = true)

        val hourBounds = composeRule.onNodeWithTag("TimeDialog:hour:input").fetchSemanticsNode().boundsInRoot
        val minuteBounds = composeRule.onNodeWithTag("TimeDialog:minute:input").fetchSemanticsNode().boundsInRoot
        val secondBounds = composeRule.onNodeWithTag("TimeDialog:second:input").fetchSemanticsNode().boundsInRoot

        assertThat(hourBounds.top).isLessThan(minuteBounds.top)
        assertThat(minuteBounds.top).isLessThan(secondBounds.top)
    }

    @Test
    fun `操作 プラスボタンで秒から分へ繰り上げる`() {
        setContent(timeMillis = 59_000L, hourEnabled = true)

        composeRule.onNodeWithTag("TimeDialog:second:increase").performClick()

        composeRule.onNodeWithTag("TimeDialog:minute:input").assertTextContains("1")
        composeRule.onNodeWithTag("TimeDialog:second:input").assertTextContains("0")
    }

    @Test
    fun `操作 ソフトキーボード入力を設定時間として通知する`() {
        var selectedTimeMillis: Long? = null
        setContent(
            timeMillis = 30_000L,
            hourEnabled = true,
            onSelectTime = { selectedTimeMillis = it },
        )

        composeRule.onNodeWithTag("TimeDialog:minute:input")
            .performClick()
            .performTextReplacement("12")
        composeRule.onNodeWithTag("TimeDialog:second:input")
            .performClick()
            .performTextReplacement("34")
        composeRule.onNodeWithText("OK").performClick()

        composeRule.runOnIdle {
            assertThat(selectedTimeMillis).isEqualTo(754_000L)
        }
    }

    @Test
    fun `入力 分が範囲外では設定操作を無効にする`() {
        setContent(hourEnabled = true)

        composeRule.onNodeWithTag("TimeDialog:minute:input")
            .performClick()
            .performTextReplacement("60")

        composeRule.onNodeWithText("Enter 0–59").assertIsDisplayed()
        composeRule.onNodeWithText("OK").assertIsNotEnabled()
    }

    @Test
    fun `入力 範囲外のフィールドからフォーカスが外れると0へ戻す`() {
        setContent(hourEnabled = true)
        composeRule.onNodeWithTag("TimeDialog:minute:input")
            .performClick()
            .performTextReplacement("60")

        composeRule.onNodeWithTag("TimeDialog:second:input").performClick()

        composeRule.onNodeWithTag("TimeDialog:minute:input").assertTextContains("0")
    }

    @Test
    fun `操作 エラー入力中のプラスボタンは対象を0として増加する`() {
        setContent(timeMillis = 30_000L, hourEnabled = true)
        composeRule.onNodeWithTag("TimeDialog:minute:input")
            .performClick()
            .performTextReplacement("60")

        composeRule.onNodeWithTag("TimeDialog:minute:increase").performClick()

        composeRule.onNodeWithTag("TimeDialog:minute:input").assertTextContains("1")
        composeRule.onNodeWithTag("TimeDialog:second:input").assertTextContains("30")
    }

    @Test
    fun `操作 3分プリセットを設定時間へ反映する`() {
        var selectedTimeMillis: Long? = null
        setContent(
            timeMillis = 30_000L,
            hourEnabled = true,
            onSelectTime = { selectedTimeMillis = it },
        )

        composeRule.onNodeWithTag("TimeDialog:preset:3").performClick()
        composeRule.onNodeWithText("OK").performClick()

        composeRule.runOnIdle {
            assertThat(selectedTimeMillis).isEqualTo(180_000L)
        }
    }

    private fun setContent(
        timeMillis: Long = 30_000L,
        hourEnabled: Boolean,
        onSelectTime: (Long) -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme {
                TimeDialog(
                    timeMillis = timeMillis,
                    hourEnabled = hourEnabled,
                    onSelectTime = onSelectTime,
                    onDismissRequest = {},
                )
            }
        }
    }
}
