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
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("NonAsciiCharacters")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MainCommandParserTest {
    @Test
    fun `parse STARTを開始コマンドへ変換する`() {
        val intent = commandIntent(Command.START)

        assertThat(MainCommandParser.parse(intent)).isEqualTo(MainCommand.Start)
    }

    @Test
    fun `parse STOPを停止コマンドへ変換する`() {
        val intent = commandIntent(Command.STOP)

        assertThat(MainCommandParser.parse(intent)).isEqualTo(MainCommand.Stop)
    }

    @Test
    fun `parse SETと時間を設定コマンドへ変換する`() {
        val intent = commandIntent(Command.SET)
            .putExtra(Constants.EXTRA_TIME, 12_345L)

        assertThat(MainCommandParser.parse(intent)).isEqualTo(MainCommand.Set(12_345L))
    }

    @Test
    fun `parse SET_AND_STARTと時間を設定開始コマンドへ変換する`() {
        val intent = commandIntent(Command.SET_AND_START)
            .putExtra(Constants.EXTRA_TIME, 12_345L)

        assertThat(MainCommandParser.parse(intent)).isEqualTo(MainCommand.SetAndStart(12_345L))
    }

    @Test
    fun `parse SETに時間がない場合はnullを返す`() {
        val intent = commandIntent(Command.SET)

        assertThat(MainCommandParser.parse(intent)).isNull()
    }

    @Test
    fun `parse 時間の型が異なる場合は既存の安全取得と同じく0へ変換する`() {
        val intent = commandIntent(Command.SET_AND_START)
            .putExtra(Constants.EXTRA_TIME, "12_345")

        assertThat(MainCommandParser.parse(intent)).isEqualTo(MainCommand.SetAndStart(0L))
    }

    @Test
    fun `parse コマンドがない場合はnullを返す`() {
        assertThat(MainCommandParser.parse(Intent())).isNull()
    }

    private fun commandIntent(
        command: Command,
    ): Intent = Intent().putExtra(Constants.EXTRA_COMMAND, command.name)
}
