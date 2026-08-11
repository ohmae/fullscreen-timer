/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import net.mm2d.timer.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ComposeTestInfrastructureTest {
    @get:Rule
    val composeRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun `setContent Composeノードを表示できる`() {
        composeRule.setContent {
            AppTheme {
                Text(TEST_TEXT)
            }
        }

        composeRule.onNodeWithText(TEST_TEXT).assertIsDisplayed()
    }

    private companion object {
        const val TEST_TEXT = "Compose test infrastructure"
    }
}
