/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.view

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import net.mm2d.timer.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClockViewTest {
    private lateinit var clockView: ClockView

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        clockView = ClockView(context)
    }

    @Test
    fun `updateTime 時間なしの時分秒ミリ秒を各桁へ設定する`() {
        clockView.setDigit(third = false, small = true)

        clockView.updateTime(12 * 60_000L + 34_560L)

        assertDigits(
            R.id.second_10 to 1,
            R.id.second_1 to 2,
            R.id.first_10 to 3,
            R.id.first_1 to 4,
            R.id.small_10 to 5,
            R.id.small_1 to 6,
        )
        assertThat(clockView.findViewById<View>(R.id.third_1).visibility).isEqualTo(View.GONE)
        assertThat(clockView.findViewById<View>(R.id.colon_2).visibility).isEqualTo(View.GONE)
    }

    @Test
    fun `updateTime 時間ありの時分秒ミリ秒を各桁へ設定する`() {
        clockView.setDigit(third = true, small = true)

        clockView.updateTime(3_723_450L)

        assertDigits(
            R.id.third_1 to 1,
            R.id.second_10 to 0,
            R.id.second_1 to 2,
            R.id.first_10 to 0,
            R.id.first_1 to 3,
            R.id.small_10 to 4,
            R.id.small_1 to 5,
        )
        assertThat(clockView.findViewById<View>(R.id.third_1).visibility).isEqualTo(View.VISIBLE)
        assertThat(clockView.findViewById<View>(R.id.colon_2).visibility).isEqualTo(View.VISIBLE)
    }

    private fun assertDigits(
        vararg expected: Pair<Int, Int>,
    ) {
        expected.forEach { (viewId, digit) ->
            assertThat(clockView.findViewById<ImageView>(viewId).tag).isEqualTo(digit)
        }
    }
}
