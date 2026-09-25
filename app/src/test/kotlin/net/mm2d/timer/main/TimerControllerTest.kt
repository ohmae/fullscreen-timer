/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.main

import com.google.common.truth.Truth.assertThat
import net.mm2d.timer.settings.TimerRunningState
import org.junit.Before
import org.junit.Test

@Suppress("NonAsciiCharacters")
class TimerControllerTest {
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var controller: TimerController

    @Before
    fun setUp() {
        timeProvider = FakeTimeProvider()
        controller = TimerController(timeProvider)
    }

    @Test
    fun `tick 開始からの経過時間を設定値から減算する`() {
        timeProvider.currentTimeMillis = 1_000L
        controller.setTime(60_000L)
        controller.start()
        timeProvider.currentTimeMillis = 2_253L

        val update = controller.tick()

        assertThat(update).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 58_747L,
                nextDelayMillis = 8L,
            ),
        )
    }

    @Test
    fun `tick ミリ秒無効時は1秒インターバルで次の遅延を計算する`() {
        controller.setMillisecondEnabled(false)
        timeProvider.currentTimeMillis = 1_000L
        controller.setTime(60_000L)
        controller.start()
        timeProvider.currentTimeMillis = 1_250L

        val update = controller.tick()

        assertThat(update).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 59_750L,
                nextDelayMillis = 751L,
            ),
        )
    }

    @Test
    fun `tick ミリ秒無効時は表示秒数が変わる境界で更新する`() {
        controller.setMillisecondEnabled(false)
        controller.setTime(59_000L)
        controller.start()

        assertThat(controller.tick()).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 59_000L,
                nextDelayMillis = 1L,
            ),
        )
        timeProvider.currentTimeMillis = 1L
        assertThat(controller.tick()).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 58_999L,
                nextDelayMillis = 1_000L,
            ),
        )
    }

    @Test
    fun `tick ミリ秒無効時も終了時刻を超えて待機しない`() {
        controller.setMillisecondEnabled(false)
        controller.setTime(999L)
        controller.start()

        assertThat(controller.tick()).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 999L,
                nextDelayMillis = 999L,
            ),
        )
    }

    @Test
    fun `stop 残り時間を保存して停止する`() {
        controller.setTime(60_000L)
        controller.start()
        timeProvider.currentTimeMillis = 12_345L

        val timeMillis = controller.stop()

        assertThat(timeMillis).isEqualTo(47_655L)
        assertThat(controller.timeMillis).isEqualTo(47_655L)
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `stop 経過時間が設定値を超えた場合は0で停止する`() {
        controller.setTime(100L)
        controller.start()
        timeProvider.currentTimeMillis = 101L

        assertThat(controller.stop()).isEqualTo(0L)
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `tick 残り時間が0以下になると完了する`() {
        controller.setTime(100L)
        controller.start()
        timeProvider.currentTimeMillis = 100L

        val update = controller.tick()

        assertThat(update).isEqualTo(TimeUpdate.Finished(timeMillis = 0L))
        assertThat(controller.timeMillis).isEqualTo(0L)
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `tick 10ms境界ちょうどの場合は表示が変わる1ms後に更新する`() {
        timeProvider.currentTimeMillis = 1_000L
        controller.setTime(60_000L)
        controller.start()
        timeProvider.currentTimeMillis = 2_000L

        val update = controller.tick()

        assertThat(update).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 59_000L,
                nextDelayMillis = 1L,
            ),
        )
        timeProvider.currentTimeMillis = 2_001L
        assertThat(controller.tick()).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 58_999L,
                nextDelayMillis = 10L,
            ),
        )
    }

    @Test
    fun `restore 実行中の保存状態を復元する`() {
        timeProvider.currentTimeMillis = 4_000L

        val restored = controller.restore(
            TimerRunningState(
                started = true,
                start = 1_000L,
                milestone = 10_000L,
            ),
        )

        assertThat(restored).isTrue()
        assertThat(controller.tick()).isEqualTo(
            TimeUpdate.Running(
                timeMillis = 7_000L,
                nextDelayMillis = 1L,
            ),
        )
    }

    @Test
    fun `restore 期限切れの保存状態は復元しない`() {
        timeProvider.currentTimeMillis = 15_000L

        val restored = controller.restore(
            TimerRunningState(
                started = true,
                start = 1_000L,
                milestone = 10_000L,
            ),
        )

        assertThat(restored).isFalse()
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `restore 停止中の保存状態は復元しない`() {
        val restored = controller.restore(TimerRunningState(started = false))

        assertThat(restored).isFalse()
        assertThat(controller.started).isFalse()
    }

    @Test
    fun `createRunningState 開始時刻と設定時間を返す`() {
        timeProvider.currentTimeMillis = 2_000L
        controller.setAndStart(30_000L)

        assertThat(controller.createRunningState()).isEqualTo(
            TimerRunningState(
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
