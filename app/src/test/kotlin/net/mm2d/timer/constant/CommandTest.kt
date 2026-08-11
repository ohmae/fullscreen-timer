/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.constant

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CommandTest {
    @Test
    fun `fromIntentExtra 有効なコマンド名を返す`() {
        val intent = Intent().putExtra(Constants.EXTRA_COMMAND, Command.SET_AND_START.name)

        assertThat(Command.fromIntentExtra(intent)).isEqualTo(Command.SET_AND_START)
    }

    @Test
    fun `fromIntentExtra 型が異なる場合はnullを返す`() {
        val intent = Intent().putExtra(Constants.EXTRA_COMMAND, 1)

        assertThat(Command.fromIntentExtra(intent)).isNull()
    }
}
