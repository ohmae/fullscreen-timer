/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.dialog

import com.google.common.truth.Truth.assertThat
import net.mm2d.timer.dialog.TimeDialogInput.Unit
import org.junit.Test

@Suppress("NonAsciiCharacters")
class TimeDialogInputTest {
    @Test
    fun `fromMillis 時表示ありでは時分秒へ分解する`() {
        val input = TimeDialogInput.fromMillis(
            timeMillis = 3_661_999L,
            hourEnabled = true,
        )

        assertThat(input.hour).isEqualTo("1")
        assertThat(input.minute).isEqualTo("1")
        assertThat(input.second).isEqualTo("1")
        assertThat(input.timeMillisOrNull).isEqualTo(3_661_000L)
    }

    @Test
    fun `fromMillis 時表示なしでは分秒へ分解して上限に収める`() {
        val input = TimeDialogInput.fromMillis(
            timeMillis = Long.MAX_VALUE,
            hourEnabled = false,
        )

        assertThat(input.hour).isEqualTo("0")
        assertThat(input.minute).isEqualTo("99")
        assertThat(input.second).isEqualTo("59")
    }

    @Test
    fun `update 数字だけを対象の単位へ反映する`() {
        val input = TimeDialogInput.fromMillis(timeMillis = 0L, hourEnabled = true)

        val updated = input
            .update(Unit.HOUR, "2")
            .update(Unit.MINUTE, "34")
            .update(Unit.SECOND, "56")

        assertThat(updated.timeMillisOrNull).isEqualTo(9_296_000L)
        assertThat(updated.update(Unit.MINUTE, "3a")).isEqualTo(updated)
        assertThat(updated.update(Unit.SECOND, "123")).isEqualTo(updated)
    }

    @Test
    fun `update 範囲外または空文字では入力を無効とする`() {
        val input = TimeDialogInput.fromMillis(timeMillis = 0L, hourEnabled = true)

        assertThat(input.update(Unit.MINUTE, "60").timeMillisOrNull).isNull()
        assertThat(input.update(Unit.SECOND, "").timeMillisOrNull).isNull()
    }

    @Test
    fun `adjust 秒の増減で分へ繰り上げ繰り下げする`() {
        val input = TimeDialogInput.fromMillis(timeMillis = 59_000L, hourEnabled = true)

        val incremented = input.adjust(Unit.SECOND, 1)
        val decremented = incremented.adjust(Unit.SECOND, -1)

        assertThat(incremented.minute).isEqualTo("1")
        assertThat(incremented.second).isEqualTo("0")
        assertThat(decremented.minute).isEqualTo("0")
        assertThat(decremented.second).isEqualTo("59")
    }

    @Test
    fun `adjust 入力範囲の上下限で停止する`() {
        val minimum = TimeDialogInput.fromMillis(timeMillis = 0L, hourEnabled = true)
        val maximum = TimeDialogInput.fromMillis(timeMillis = Long.MAX_VALUE, hourEnabled = true)

        assertThat(minimum.adjust(Unit.SECOND, -1)).isEqualTo(minimum)
        assertThat(maximum.adjust(Unit.HOUR, 1)).isEqualTo(maximum)
    }

    @Test
    fun `resetIfInvalid 範囲外の単位だけを0へ戻す`() {
        val input = TimeDialogInput.fromMillis(timeMillis = 3_630_000L, hourEnabled = true)
            .update(Unit.MINUTE, "60")

        val normalized = input.resetIfInvalid(Unit.MINUTE)

        assertThat(normalized.hour).isEqualTo("1")
        assertThat(normalized.minute).isEqualTo("0")
        assertThat(normalized.second).isEqualTo("30")
    }

    @Test
    fun `adjust 範囲外の単位を0とみなして増減する`() {
        val input = TimeDialogInput.fromMillis(timeMillis = 30_000L, hourEnabled = true)
            .update(Unit.MINUTE, "60")

        val adjusted = input.adjust(Unit.MINUTE, 1)

        assertThat(adjusted.hour).isEqualTo("0")
        assertThat(adjusted.minute).isEqualTo("1")
        assertThat(adjusted.second).isEqualTo("30")
    }
}
