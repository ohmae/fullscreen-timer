/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.main

import com.google.common.truth.Truth.assertThat
import net.mm2d.timer.settings.StopwatchRunningState
import org.junit.Before
import org.junit.Test

@Suppress("NonAsciiCharacters")
class StopwatchControllerTest {
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var controller: StopwatchController

    @Before
    fun setUp() {
        timeProvider = FakeTimeProvider()
        controller = StopwatchController(timeProvider)
    }

    @Test
    fun `tick 開始からの経過時間をマイルストーンへ加算する`() {
        controller.setTime(1_000L)
        controller.start()
        timeProvider.currentTimeMillis = 1_253L

        val update = controller.tick()

        assertThat(update).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 2_253L,
                nextDelayMillis = 7L,
            ),
        )
    }

    @Test
    fun `stop 経過時間を保存して停止する`() {
        controller.setTime(1_000L)
        controller.start()
        timeProvider.currentTimeMillis = 2_345L

        val timeMillis = controller.stop()

        assertThat(timeMillis).isEqualTo(3_345L)
        assertThat(controller.timeMillis).isEqualTo(3_345L)
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `reset 実行中でも0で停止する`() {
        controller.setTime(1_000L)
        controller.start()
        timeProvider.currentTimeMillis = 2_345L

        assertThat(controller.reset()).isEqualTo(0L)
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `tick 時間なしの最大値に達すると完了する`() {
        controller.setTime(5_999_980L)
        controller.start()
        timeProvider.currentTimeMillis = 10L

        val update = controller.tick()

        assertThat(update).isEqualTo(TimeUpdate.Finished(timeMillis = 5_999_990L))
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `tick 時間ありの最大値に達すると完了する`() {
        controller.setHourEnabled(true)
        controller.setTime(35_999_980L)
        controller.start()
        timeProvider.currentTimeMillis = 10L

        val update = controller.tick()

        assertThat(update).isEqualTo(TimeUpdate.Finished(timeMillis = 35_999_990L))
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `restore 最大値以内の実行状態を復元する`() {
        timeProvider.currentTimeMillis = 4_000L

        val restored = controller.restore(
            StopwatchRunningState(
                started = true,
                start = 1_000L,
                milestone = 10_000L,
            ),
        )

        assertThat(restored).isTrue()
        assertThat(controller.tick()).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 13_000L,
                nextDelayMillis = 10L,
            ),
        )
    }

    @Test
    fun `restore 最大値を超えた実行状態を復元しない`() {
        timeProvider.currentTimeMillis = 6_000_000L

        val restored = controller.restore(
            StopwatchRunningState(
                started = true,
                start = 0L,
                milestone = 0L,
            ),
        )

        assertThat(restored).isFalse()
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `createRunningState 開始時刻とマイルストーンを返す`() {
        timeProvider.currentTimeMillis = 2_000L
        controller.setAndStart(30_000L)

        assertThat(controller.createRunningState()).isEqualTo(
            StopwatchRunningState(
                started = true,
                start = 2_000L,
                milestone = 30_000L,
            ),
        )
    }

    private class FakeTimeProvider : TimeProvider {
        var currentTimeMillis: Long = 0L

        override fun currentTimeMillis(): Long = currentTimeMillis
    }
}
