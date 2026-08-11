/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.settings

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import net.mm2d.timer.constant.Constants
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ModeTest {
    @Test
    fun `fromIntentExtra 有効なモード名を返す`() {
        val intent = Intent().putExtra(Constants.EXTRA_MODE, Mode.TIMER.name)

        assertThat(Mode.fromIntentExtra(intent)).isEqualTo(Mode.TIMER)
    }

    @Test
    fun `fromIntentExtra 不明なモード名ではnullを返す`() {
        val intent = Intent().putExtra(Constants.EXTRA_MODE, "UNKNOWN")

        assertThat(Mode.fromIntentExtra(intent)).isNull()
    }
}
