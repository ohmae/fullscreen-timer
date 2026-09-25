/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.util

import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ColorExtensionsTest {
    @Test
    fun `isDarkColor 暗い背景色の場合はtrueを返す`() {
        assertThat(Color.BLACK.isDarkColor()).isTrue()
        assertThat(Color.DKGRAY.isDarkColor()).isTrue()
    }

    @Test
    fun `isDarkColor 明るい背景色の場合はfalseを返す`() {
        assertThat(Color.WHITE.isDarkColor()).isFalse()
        assertThat(Color.LTGRAY.isDarkColor()).isFalse()
    }
}
