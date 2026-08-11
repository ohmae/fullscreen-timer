/*
 * Copyright (c) 2026 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.timer.main

import net.mm2d.timer.settings.TimerRunningState
import javax.inject.Inject

class TimerController @Inject constructor(
    private val timeProvider: TimeProvider,
) {
    var started: Boolean = false
        private set

    var timeMillis: Long = 0L
        private set

    private var startTimeMillis: Long = 0L

    fun start() {
        started = true
        startTimeMillis = timeProvider.currentTimeMillis()
    }

    fun stop(): Long {
        if (started) {
            timeMillis = calculateTime().coerceAtLeast(0L)
        }
        started = false
        return timeMillis
    }

    fun setTime(
        timeMillis: Long,
    ) {
        this.timeMillis = timeMillis
    }

    fun setAndStart(
        timeMillis: Long,
    ) {
        setTime(timeMillis)
        start()
    }

    fun tick(): TimeUpdate {
        val currentTimeMillis = calculateTime()
        if (currentTimeMillis <= 0L) {
            timeMillis = 0L
            started = false
            return TimeUpdate.Finished(timeMillis = 0L)
        }
        return TimeUpdate.Running(
            timeMillis = currentTimeMillis,
            nextDelayMillis = currentTimeMillis % TIMER_INTERVAL_MILLIS,
        )
    }

    fun restore(
        state: TimerRunningState,
    ): Boolean {
        if (!state.started) return false
        started = true
        startTimeMillis = state.start
        timeMillis = state.milestone
        return true
    }

    fun createRunningState(): TimerRunningState =
        TimerRunningState(
            started = started,
            start = startTimeMillis,
            milestone = timeMillis,
        )

    fun deactivate() {
        started = false
    }

    private fun calculateTime(): Long = timeMillis - (timeProvider.currentTimeMillis() - startTimeMillis)

    private companion object {
        const val TIMER_INTERVAL_MILLIS = 10L
    }
}
