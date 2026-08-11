/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.main

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import net.mm2d.timer.constant.Command
import net.mm2d.timer.constant.Constants
import net.mm2d.timer.settings.Mode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainLaunchRequestParserTest {
    @Test
    fun `parse モードとコマンドを起動要求へ変換する`() {
        val intent = Intent()
            .putExtra(Constants.EXTRA_MODE, Mode.TIMER.name)
            .putExtra(Constants.EXTRA_COMMAND, Command.SET_AND_START.name)
            .putExtra(Constants.EXTRA_TIME, 30_000L)

        assertThat(MainLaunchRequestParser.parse(intent)).isEqualTo(
            MainLaunchRequest(
                mode = Mode.TIMER,
                command = MainCommand.SetAndStart(30_000L),
            ),
        )
    }

    @Test
    fun `parse モードがない場合はnullを返す`() {
        assertThat(MainLaunchRequestParser.parse(Intent())).isNull()
    }
}
